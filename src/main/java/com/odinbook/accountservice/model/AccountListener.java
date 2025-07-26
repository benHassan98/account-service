package com.odinbook.accountservice.model;

import java.io.InputStream;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.minio.GetObjectArgs;
import io.minio.MinioClient;
import jakarta.persistence.PostLoad;

@Component
class AccountListener {

  @Value("${minio.bucket}")
  private String BUCKET;

  private final MinioClient minioClient;

  public AccountListener(MinioClient minioClient) {
    this.minioClient = minioClient;
  }

  @PostLoad
  void postLoad(Account account) {
    var getObjectArgs = GetObjectArgs.builder()
        .bucket(BUCKET)
        .object(account.getPictureId())
        .build();
    try (InputStream inputStream = this.minioClient.getObject(getObjectArgs)) {
      account.setPictureBytes(inputStream.readAllBytes());
    } catch (Exception exception) {
      exception.printStackTrace();
    }
  }
}
