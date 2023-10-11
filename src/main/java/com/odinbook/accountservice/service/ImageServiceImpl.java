package com.odinbook.accountservice.service;

import com.azure.storage.blob.BlobServiceClientBuilder;
import jakarta.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Objects;


@Service
public class ImageServiceImpl implements ImageService {
    @Value("${spring.cloud.azure.storage.connection-string}")
    private String blobStorageConnectStr;
    @Override
    public void createBlob(String blobName, MultipartFile image) throws IOException {
        if(Objects.isNull(image))
            return;

        new BlobServiceClientBuilder()
                .connectionString(blobStorageConnectStr)
                .buildClient()
                .getBlobContainerClient("images")
                .getBlobClient(blobName)
                .upload(image.getInputStream());


    }
}
