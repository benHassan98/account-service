package com.odinbook.accountservice.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.minio.MinioClient;

@Configuration
class MinioConfig {

  @Bean
  MinioClient minioClient(
      @Value("${minio.url}") String url,
      @Value("${minio.accessKey}") String accessKey,
      @Value("${minio.secretKey}") String secretKey) {
    return MinioClient.builder()
        .endpoint(url)
        .credentials(accessKey, secretKey)
        .build();
  }

}
