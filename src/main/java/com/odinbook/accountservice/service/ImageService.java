package com.odinbook.accountservice.service;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface ImageService {
    public void createBlob(String blobName, MultipartFile image) throws IOException;
    public byte[] findBlob(String blobName);
}
