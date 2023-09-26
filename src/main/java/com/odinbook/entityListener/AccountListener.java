package com.odinbook.entityListener;

import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import com.nimbusds.jose.crypto.impl.PasswordBasedCryptoProvider;
import com.nimbusds.jose.util.Base64;
import com.odinbook.model.Account;
import com.odinbook.service.ImageService;
import jakarta.persistence.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Objects;

@Component
public class AccountListener {

    private final ImageService imageService;

    @Autowired
    public AccountListener(ImageService imageService) {
        this.imageService = imageService;
    }
    @PreUpdate
    @PrePersist
    public void prePersistOrPreUpdate(Account account) {
        try{
            String blobName = Objects.nonNull(account.getPicture())?
                    account.getPicture():Objects.isNull(account.getImage())?
                    "defaultPicture":
                    Base64.encode(account.getEmail())+"/"+account.getImage().getName();

            account.setPicture(blobName);
            imageService.createBlob(account.getPicture(),account.getImage());

        }
        catch (IOException exception){
            exception.printStackTrace();
        }
    }

    @PostLoad
    public void postLoad(Account account) {

        String blob = Base64.encode(imageService.findBlob(account.getPicture())).toString();
        account.setPicture(blob);
    }




}
