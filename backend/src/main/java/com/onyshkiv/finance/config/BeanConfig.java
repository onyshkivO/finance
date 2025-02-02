package com.onyshkiv.finance.config;

import jakarta.annotation.PostConstruct;
import okhttp3.OkHttpClient;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.security.Security;
import java.util.concurrent.TimeUnit;

@Configuration
public class BeanConfig {
    @Bean
    public OkHttpClient okHttpClient() {
        return new OkHttpClient.Builder()
                .readTimeout(30, TimeUnit.SECONDS)
                .build();
    }
    @PostConstruct
    public void init() {
        Security.addProvider(new BouncyCastleProvider());
    }
}
