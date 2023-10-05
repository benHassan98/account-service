package com.odinbook.accountservice.service;

import com.azure.storage.blob.BlobServiceClientBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Service
public class ImageServiceImpl implements ImageService {
    @Value("${spring.cloud.azure.storage.connection-string}")
    private String connectStr;
    @Override
    public void createBlob(String blobName, MultipartFile image) throws IOException {
        new BlobServiceClientBuilder()
                .connectionString(connectStr)
                .buildClient()
                .getBlobContainerClient("images")
                .getBlobClient(blobName)
                .upload(image.getInputStream());
    }

    @Override
    public byte[] findBlob(String blobName) {

        return new BlobServiceClientBuilder()
                .connectionString(connectStr)
                .buildClient()
                .getBlobContainerClient("images")
                .getBlobClient(blobName)
                .downloadContent().toBytes();
    }
}
