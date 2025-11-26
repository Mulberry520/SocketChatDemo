package com.mulberry.WebChat.config;

import com.aliyun.oss.common.auth.CredentialsProvider;
import com.aliyun.oss.common.auth.DefaultCredentialProvider;
import com.aliyun.oss.common.comm.SignVersion;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import com.aliyun.oss.*;

@Configuration
public class OssConfig {
    @Value("${custom.oss.access-key-id}")
    private String accessKeyId;
    @Value("${custom.oss.access-key-secret}")
    private String accessKeySecret;
    @Value("${custom.oss.endpoint}")
    private String endpoint;
    @Value("${custom.oss.region}")
    private String region;

    @Bean
    public OSS ossClient() {
        CredentialsProvider credentialsProvider = new DefaultCredentialProvider(accessKeyId, accessKeySecret);
        ClientBuilderConfiguration clientBuilderConfiguration = new ClientBuilderConfiguration();
        clientBuilderConfiguration.setSignatureVersion(SignVersion.V4);
        OSS ossClient = OSSClientBuilder.create()
                .endpoint(endpoint)
                .credentialsProvider(credentialsProvider)
                .clientConfiguration(clientBuilderConfiguration)
                .region(region)
                .build();
        return ossClient;
    }
}
