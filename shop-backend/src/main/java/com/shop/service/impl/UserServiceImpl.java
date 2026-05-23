package com.shop.service.impl;

import com.shop.common.PageResult;
import com.shop.entity.User;
import com.shop.entity.UserAddress;
import com.shop.exception.BusinessException;
import com.shop.repository.UserAddressRepository;
import com.shop.repository.UserRepository;
import com.shop.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserAddressRepository userAddressRepository;

    @Override
    public User getProfile(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(404, "用户不存在"));
    }

    @Override
    public User updateProfile(Long userId, User user) {
        User existing = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(404, "用户不存在"));
        existing.setEmail(user.getEmail());
        existing.setPhone(user.getPhone());
        existing.setAvatar(user.getAvatar());
        return userRepository.save(existing);
    }

    @Override
    public List<UserAddress> getAddresses(Long userId) {
        return userAddressRepository.findByUserIdOrderByIsDefaultDesc(userId);
    }

    @Override
    public UserAddress addAddress(Long userId, UserAddress address) {
        address.setUserId(userId);
        if (Boolean.TRUE.equals(address.getIsDefault())) {
            userAddressRepository.findByUserIdOrderByIsDefaultDesc(userId)
                    .forEach(a -> { a.setIsDefault(false); userAddressRepository.save(a); });
        }
        return userAddressRepository.save(address);
    }

    @Override
    public PageResult<User> adminList(int page, int size) {
        PageRequest pr = PageRequest.of(page - 1, size, Sort.by("createTime").descending());
        Page<User> result = userRepository.findAll(pr);
        result.forEach(u -> u.setPassword(null));
        return PageResult.of(result.getContent(), result.getTotalElements(), page, size);
    }

    @Override
    public void updateUserStatus(Long userId, String status) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(404, "用户不存在"));
        user.setStatus(status);
        userRepository.save(user);
    }
}
