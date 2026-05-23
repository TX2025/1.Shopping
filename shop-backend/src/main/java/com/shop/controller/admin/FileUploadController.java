package com.shop.controller.admin;

import com.shop.common.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.nio.file.*;
import java.util.*;

@Slf4j
@RestController
@RequestMapping("/api/admin")
public class FileUploadController {

    @Value("${file.upload.dir:uploads}")
    private String uploadDir;

    private Path uploadPath;

    @PostConstruct
    void init() {
        Path dir = Paths.get(uploadDir);
        if (!dir.isAbsolute()) {
            dir = Paths.get(System.getProperty("user.dir")).resolve(uploadDir);
        }
        this.uploadPath = dir.toAbsolutePath().normalize();
        try {
            Files.createDirectories(this.uploadPath);
        } catch (IOException e) {
            log.error("Failed to create upload dir: {}", this.uploadPath, e);
        }
        log.info("Upload directory: {}", this.uploadPath);
    }

    private static final Set<String> IMAGE_EXTENSIONS = Set.of("jpg", "jpeg", "png", "gif");
    private static final Set<String> VIDEO_EXTENSIONS = Set.of("mp4");
    private static final Set<String> ALLOWED_EXTENSIONS;

    static {
        ALLOWED_EXTENSIONS = new HashSet<>();
        ALLOWED_EXTENSIONS.addAll(IMAGE_EXTENSIONS);
        ALLOWED_EXTENSIONS.addAll(VIDEO_EXTENSIONS);
    }

    @PostMapping("/upload")
    public ApiResponse<Map<String, Object>> upload(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return ApiResponse.error("文件为空");
        }

        String originalFilename = file.getOriginalFilename();
        String ext = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            ext = originalFilename.substring(originalFilename.lastIndexOf(".") + 1).toLowerCase();
        }

        if (!ALLOWED_EXTENSIONS.contains(ext)) {
            return ApiResponse.error("不支持的文件格式: " + ext + "，仅支持 jpg/jpeg/png/gif/mp4");
        }

        try {
            String newFilename = UUID.randomUUID().toString() + "." + ext;
            Path filePath = uploadPath.resolve(newFilename);
            file.transferTo(filePath.toFile());

            String url = "/uploads/" + newFilename;
            log.info("File uploaded: {} -> {}", originalFilename, url);

            Map<String, Object> data = new LinkedHashMap<>();
            data.put("url", url);
            data.put("filename", newFilename);
            data.put("originalName", originalFilename);
            data.put("size", file.getSize());
            return ApiResponse.success(data);
        } catch (IOException e) {
            log.error("Upload failed", e);
            return ApiResponse.error("上传失败: " + e.getMessage());
        }
    }

    @GetMapping("/upload/list")
    public ApiResponse<List<Map<String, Object>>> listFiles() {
        List<Map<String, Object>> files = new ArrayList<>();
        if (!Files.exists(uploadPath)) {
            return ApiResponse.success(files);
        }
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(uploadPath)) {
            for (Path p : stream) {
                Map<String, Object> info = new LinkedHashMap<>();
                info.put("filename", p.getFileName().toString());
                info.put("url", "/uploads/" + p.getFileName().toString());
                info.put("size", Files.size(p));
                files.add(info);
            }
        } catch (IOException e) {
            log.error("List files failed", e);
        }
        return ApiResponse.success(files);
    }
}
