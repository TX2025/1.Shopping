package com.shop.controller.admin;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
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
    public ResponseEntity<Map<String, Object>> upload(@RequestParam("file") MultipartFile file) {
        Map<String, Object> result = new HashMap<>();

        if (file.isEmpty()) {
            result.put("success", false);
            result.put("message", "文件为空");
            return ResponseEntity.badRequest().body(result);
        }

        String originalFilename = file.getOriginalFilename();
        String ext = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            ext = originalFilename.substring(originalFilename.lastIndexOf(".") + 1).toLowerCase();
        }

        if (!ALLOWED_EXTENSIONS.contains(ext)) {
            result.put("success", false);
            result.put("message", "不支持的文件格式: " + ext + "，仅支持 jpg/jpeg/png/gif/mp4");
            return ResponseEntity.badRequest().body(result);
        }

        try {
            String newFilename = UUID.randomUUID().toString() + "." + ext;
            Path filePath = uploadPath.resolve(newFilename);
            file.transferTo(filePath.toFile());

            String url = "/uploads/" + newFilename;
            log.info("File uploaded: {} -> {}", originalFilename, url);

            result.put("success", true);
            result.put("url", url);
            result.put("filename", newFilename);
            result.put("originalName", originalFilename);
            result.put("size", file.getSize());
            return ResponseEntity.ok(result);
        } catch (IOException e) {
            log.error("Upload failed", e);
            result.put("success", false);
            result.put("message", "上传失败: " + e.getMessage());
            return ResponseEntity.internalServerError().body(result);
        }
    }

    @GetMapping("/upload/list")
    public ResponseEntity<List<Map<String, Object>>> listFiles() {
        List<Map<String, Object>> files = new ArrayList<>();
        if (!Files.exists(uploadPath)) {
            return ResponseEntity.ok(files);
        }
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(uploadPath)) {
            for (Path p : stream) {
                Map<String, Object> info = new HashMap<>();
                info.put("filename", p.getFileName().toString());
                info.put("url", "/uploads/" + p.getFileName().toString());
                info.put("size", Files.size(p));
                files.add(info);
            }
        } catch (IOException e) {
            log.error("List files failed", e);
        }
        return ResponseEntity.ok(files);
    }
}
