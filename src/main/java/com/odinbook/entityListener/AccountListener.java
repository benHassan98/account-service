package com.odinbook.entityListener;

import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import com.nimbusds.jose.crypto.impl.PasswordBasedCryptoProvider;
import com.nimbusds.jose.util.Base64;
import com.odinbook.model.Account;
import jakarta.persistence.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Objects;

public class AccountListener {
    @Value("${spring.cloud.azure.storage.connection-string}")
    private String connectStr;

    public String getBlobName (Account account) throws IOException {
        if(Objects.nonNull(account.getPicture())){
            return account.getPicture();
        }
        if(Objects.isNull(account.getImage())){
            return "defaultPicture";
        }

        String blobName = Base64.encode(account.getEmail())+"/"+account.getImage().getName();

        new BlobServiceClientBuilder()
                .connectionString(connectStr)
                .buildClient()
                .getBlobContainerClient("images")
                .getBlobClient(blobName)
                .upload(account.getImage().getInputStream());

        account.setPicture(blobName);

        return account.getPicture();
    }
    public String getBlob (String blobName){

        byte[] blobContent = new BlobServiceClientBuilder()
                .connectionString(connectStr)
                .buildClient()
                .getBlobContainerClient("images")
                .getBlobClient(blobName)
                .downloadContent().toBytes();


        return Base64.encode(blobContent).toString();
    }


    @PreUpdate
    @PrePersist
    public void prePersistOrPreUpdate(Account account) {
        try{
            String picturePath = getBlobName(account);
            account.setPicture(picturePath);
        }
        catch (IOException exception){
            exception.printStackTrace();
        }
    }

    @PostLoad
    public void postLoad(Account account) {
        String blob = getBlob(account.getPicture());
        account.setPicture(blob);
    }




}
