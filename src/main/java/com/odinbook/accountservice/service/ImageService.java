package com.odinbook.accountservice.service;

import jakarta.validation.constraints.NotNull;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface ImageService {
    public void createBlob(String blobName, MultipartFile image) throws IOException;
}
