package id.ac.ui.cs.advprog.jsoninventoryservice.controller;

import id.ac.ui.cs.advprog.jsoninventoryservice.exception.ImageUploadException;
import id.ac.ui.cs.advprog.jsoninventoryservice.service.StorageService;
import id.ac.ui.cs.advprog.jsoninventoryservice.utils.ApiResponse;
import id.ac.ui.cs.advprog.jsoninventoryservice.utils.ResponseUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@CrossOrigin(originPatterns = "*", allowedHeaders = "*")
@RestController
@RequestMapping("/api/products/images")
@RequiredArgsConstructor
public class ImageUploadController {

    private final StorageService storageService;

    @PostMapping("/upload")
    public ResponseEntity<ApiResponse<Map<String, String>>> uploadImage(@RequestParam("file") MultipartFile file) {
        try {
            String imageUrl = storageService.uploadFile(file);
            return ResponseUtil.success(Map.of("image_url", imageUrl), "Image uploaded successfully");
        } catch (IOException e) {
            throw new ImageUploadException("Failed to upload image to S3", e);
        }
    }
}