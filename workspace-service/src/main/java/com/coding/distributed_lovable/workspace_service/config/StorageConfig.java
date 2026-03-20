package com.coding.distributed_lovable.workspace_service.config;

import io.minio.MinioClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class StorageConfig {

  @Value("${minio.url}")
  private String endPointUrl;

  @Value("${minio.access-key}")
  private String username;

  @Value("${minio.secret-key}")
  private String password;

  @Bean
  public MinioClient minioClient() {
    return MinioClient.builder().endpoint(endPointUrl).credentials(username, password).build();
  }
}
