package id.ac.ui.cs.advprog.jsoninventoryservice.service;

import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;

public interface StorageService {
    String uploadFile(MultipartFile file) throws IOException;
}