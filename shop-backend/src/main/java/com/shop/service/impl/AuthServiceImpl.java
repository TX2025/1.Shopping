package com.shop.service.impl;

import com.shop.common.Constants;
import com.shop.dto.request.*;
import com.shop.dto.response.LoginResponse;
import com.shop.entity.LoginLog;
import com.shop.entity.RefreshToken;
import com.shop.entity.SmsCode;
import com.shop.entity.User;
import com.shop.exception.BusinessException;
import com.shop.repository.LoginLogRepository;
import com.shop.repository.RefreshTokenRepository;
import com.shop.repository.SmsCodeRepository;
import com.shop.repository.UserRepository;
import com.shop.security.JwtUtils;
import com.shop.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.LocalDateTime;
import java.util.Random;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;
    private final RefreshTokenRepository refreshTokenRepository;
    private final LoginLogRepository loginLogRepository;
    private final SmsCodeRepository smsCodeRepository;

    private static final Random RANDOM = new Random();

    @Override
    public void register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new BusinessException(400, "用户名已存在");
        }
        User user = User.builder()
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .email(request.getEmail())
                .phone(request.getPhone())
                .role(Constants.ROLE_USER)
                .status(Constants.USER_STATUS_ACTIVE)
                .build();
        userRepository.save(user);
    }

    @Override
    public LoginResponse login(LoginRequest request) {
        String ip = getClientIp();
        String ua = getClientUserAgent();
        String username = request.getUsername();

        LocalDateTime lockSince = LocalDateTime.now().minusMinutes(Constants.LOGIN_LOCK_MINUTES);
        long failCount = loginLogRepository.countFailedByIpSince(ip, lockSince);
        if (failCount >= Constants.LOGIN_MAX_FAIL_COUNT) {
            saveLoginLog(null, username, ip, ua, Constants.LOGIN_LOG_FAIL_LOCKED, "IP锁定：15分钟内失败" + failCount + "次");
            throw new BusinessException(400, "请15分钟后再试");
        }

        User user = userRepository.findByUsername(username).orElse(null);
        if (user == null) {
            saveLoginLog(null, username, ip, ua, Constants.LOGIN_LOG_FAIL_NOT_FOUND, "用户名不存在");
            throw new BusinessException(400, "用户名或密码错误");
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            saveLoginLog(user.getId(), username, ip, ua, Constants.LOGIN_LOG_FAIL_WRONG_PWD, "密码错误");
            throw new BusinessException(400, "用户名或密码错误");
        }

        if (Constants.USER_STATUS_DISABLED.equals(user.getStatus())) {
            saveLoginLog(user.getId(), username, ip, ua, Constants.LOGIN_LOG_FAIL_DISABLED, "账号已被禁用");
            throw new BusinessException(400, "账号已被禁用");
        }

        saveLoginLog(user.getId(), username, ip, ua, Constants.LOGIN_LOG_SUCCESS, null);
        return buildTokensAndRespond(user);
    }

    @Override
    @Transactional
    public LoginResponse refresh(String refreshTokenStr) {
        RefreshToken stored = refreshTokenRepository.findByToken(refreshTokenStr)
                .orElseThrow(() -> new BusinessException(401, "Refresh Token无效"));
        if (stored.getRevoked()) {
            throw new BusinessException(401, "Refresh Token已被撤销");
        }
        if (stored.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new BusinessException(401, "Refresh Token已过期，请重新登录");
        }
        stored.setRevoked(true);
        refreshTokenRepository.save(stored);

        User user = userRepository.findById(stored.getUserId())
                .orElseThrow(() -> new BusinessException(401, "用户不存在"));
        if (Constants.USER_STATUS_DISABLED.equals(user.getStatus())) {
            throw new BusinessException(401, "账号已被禁用");
        }
        return buildTokensAndRespond(user);
    }

    @Override
    @Transactional
    public void logout(String refreshTokenStr) {
        refreshTokenRepository.findByToken(refreshTokenStr).ifPresent(rt -> {
            rt.setRevoked(true);
            refreshTokenRepository.save(rt);
        });
    }

    // ==================== SMS ====================

    @Override
    @Transactional
    public String sendCode(SendCodeRequest request) {
        String phone = request.getPhone();
        String type = request.getType();

        // Rate limit: 60s interval
        LocalDateTime oneMinAgo = LocalDateTime.now().minusSeconds(60);
        smsCodeRepository.findTopByPhoneAndTypeAndUsedFalseAndExpiresAtAfterOrderByCreateTimeDesc(
                        phone, type, LocalDateTime.now())
                .ifPresent(last -> {
                    if (last.getCreateTime().isAfter(oneMinAgo)) {
                        throw new BusinessException(400, "发送频率过快，请60秒后再试");
                    }
                });

        // Daily limit: 5 per phone per type
        LocalDateTime todayStart = LocalDateTime.now().toLocalDate().atStartOfDay();
        long dailyCount = smsCodeRepository.countByPhoneAndTypeSince(phone, type, todayStart);
        if (dailyCount >= 5) {
            throw new BusinessException(400, "今日发送次数已达上限");
        }

        // Generate 6-digit code
        String code = String.format("%06d", RANDOM.nextInt(1000000));
        SmsCode smsCode = SmsCode.builder()
                .phone(phone)
                .code(code)
                .type(type)
                .expiresAt(LocalDateTime.now().plusMinutes(5))
                .used(false)
                .build();
        smsCodeRepository.save(smsCode);

        log.info("=== SMS CODE === phone={} type={} code={}", phone, type, code);
        return code;
    }

    @Override
    @Transactional
    public boolean verifyCode(VerifyCodeRequest request) {
        SmsCode smsCode = smsCodeRepository
                .findTopByPhoneAndTypeAndUsedFalseAndExpiresAtAfterOrderByCreateTimeDesc(
                        request.getPhone(), request.getType(), LocalDateTime.now())
                .orElseThrow(() -> new BusinessException(400, "验证码不存在或已过期"));
        if (!smsCode.getCode().equals(request.getCode())) {
            throw new BusinessException(400, "验证码错误");
        }
        smsCode.setUsed(true);
        smsCodeRepository.save(smsCode);
        return true;
    }

    @Override
    @Transactional
    public LoginResponse loginBySms(SmsLoginRequest request) {
        // Verify code first
        VerifyCodeRequest vcReq = new VerifyCodeRequest();
        vcReq.setPhone(request.getPhone());
        vcReq.setCode(request.getCode());
        vcReq.setType(Constants.SMS_TYPE_LOGIN);
        verifyCode(vcReq);

        // Find user by phone
        User user = userRepository.findByPhone(request.getPhone())
                .orElse(null);
        if (user == null) {
            // Auto register
            user = User.builder()
                    .username("u" + request.getPhone())
                    .password(passwordEncoder.encode(request.getPhone()))
                    .phone(request.getPhone())
                    .role(Constants.ROLE_USER)
                    .status(Constants.USER_STATUS_ACTIVE)
                    .build();
            userRepository.save(user);
        }
        if (Constants.USER_STATUS_DISABLED.equals(user.getStatus())) {
            throw new BusinessException(400, "账号已被禁用");
        }

        saveLoginLog(user.getId(), user.getUsername(), getClientIp(), getClientUserAgent(),
                Constants.LOGIN_LOG_SUCCESS, null);
        return buildTokensAndRespond(user);
    }

    @Override
    @Transactional
    public void registerBySms(SmsRegisterRequest request) {
        // Verify code first
        VerifyCodeRequest vcReq = new VerifyCodeRequest();
        vcReq.setPhone(request.getPhone());
        vcReq.setCode(request.getCode());
        vcReq.setType(Constants.SMS_TYPE_REGISTER);
        verifyCode(vcReq);

        if (userRepository.findByPhone(request.getPhone()).isPresent()) {
            throw new BusinessException(400, "该手机号已注册");
        }

        User user = User.builder()
                .username("u" + request.getPhone())
                .password(passwordEncoder.encode(request.getPassword()))
                .phone(request.getPhone())
                .role(Constants.ROLE_USER)
                .status(Constants.USER_STATUS_ACTIVE)
                .build();
        userRepository.save(user);
    }

    // ==================== Internal helpers ====================

    private LoginResponse buildTokensAndRespond(User user) {
        String accessToken = jwtUtils.generateAccessToken(user.getId(), user.getUsername(), user.getRole());
        String refreshTokenStr = jwtUtils.generateRefreshToken();
        RefreshToken rt = RefreshToken.builder()
                .userId(user.getId())
                .token(refreshTokenStr)
                .expiresAt(LocalDateTime.now().plusSeconds(jwtUtils.getRefreshExpirationMs() / 1000))
                .revoked(false)
                .build();
        refreshTokenRepository.save(rt);
        return new LoginResponse(accessToken, refreshTokenStr,
                jwtUtils.getAccessExpirationMs() / 1000,
                user.getId(), user.getUsername(), user.getRole());
    }

    private void saveLoginLog(Long userId, String username, String ip, String userAgent, String status, String failReason) {
        LoginLog log = LoginLog.builder()
                .userId(userId)
                .username(username)
                .ip(ip)
                .userAgent(userAgent)
                .status(status)
                .failReason(failReason)
                .build();
        loginLogRepository.save(log);
    }

    private String getClientIp() {
        ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attrs == null) return "unknown";
        HttpServletRequest req = attrs.getRequest();
        String ip = req.getHeader("X-Forwarded-For");
        if (ip != null && !ip.isBlank()) {
            return ip.split(",")[0].trim();
        }
        ip = req.getHeader("X-Real-IP");
        if (ip != null && !ip.isBlank()) return ip.trim();
        return req.getRemoteAddr();
    }

    private String getClientUserAgent() {
        ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attrs == null) return "unknown";
        String ua = attrs.getRequest().getHeader("User-Agent");
        return ua != null ? ua : "unknown";
    }
}
