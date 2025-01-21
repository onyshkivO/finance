package com.onyshkiv.finance.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.onyshkiv.finance.model.dto.MonobankAuthDto;
import com.onyshkiv.finance.model.entity.MonobankAuth;
import com.onyshkiv.finance.repository.MonobankAuthRepository;
import com.onyshkiv.finance.service.MonobankService;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.PrivateKey;
import java.security.Signature;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class MonobankServiceImpl implements MonobankService {
    private static final String MONOBANK_AUTH_URL = "https://api.monobank.ua";
    private static final String REQUEST_ACCESS = "/personal/auth/request";
    @Value("${monobank.x-key-id}")
    private String xKeyId;
    @Value("${monobank.private-key-file-path}")
    private String privateKeyPath;
    private final OkHttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final MonobankAuthRepository monobankAuthRepository;

    public MonobankServiceImpl(ObjectMapper objectMapper, MonobankAuthRepository monobankAuthRepository) {
        this.objectMapper = objectMapper;
        this.monobankAuthRepository = monobankAuthRepository;
        this.httpClient = new OkHttpClient.Builder()
                .readTimeout(30, TimeUnit.SECONDS)
                .build();
    }

    @Transactional
    public MonobankAuth requestAccessAndStore() throws IOException {
        String responseJson = requestAccess();

        // Parse JSON response
        MonobankAuthDto response = objectMapper.readValue(responseJson, MonobankAuthDto.class);

        // Save response to the database
        MonobankAuth auth = new MonobankAuth();
        auth.setTokenRequestId(response.getTokenRequestId());
        auth.setAcceptUrl(response.getAcceptUrl());
        auth.setActivated(false);

        return monobankAuthRepository.save(auth);
    }


    public String requestAccess() throws IOException {
        String url = MONOBANK_AUTH_URL + REQUEST_ACCESS;
        String xTime = String.valueOf(System.currentTimeMillis() / 1000);
        String xSign = generateSignature(xTime, REQUEST_ACCESS, privateKeyPath);

        Request request = new Request.Builder()
                .url(url)
                .header("X-Time", xTime)
                .header("X-Sign", xSign)
                .header("Content-Type", "application/json")
                .post(RequestBody.create(null, new byte[0]))
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Unexpected response: " + response);
            }

            return response.body() != null ? response.body().string() : null;
        }
    }

    private String generateSignature(String xTime, String resource, String privateKeyPath) {
        try {
            String data = xTime + resource;
            byte[] privateKeyBytes = Files.readAllBytes(Paths.get(privateKeyPath));
            PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(privateKeyBytes);
            PrivateKey privateKey = java.security.KeyFactory.getInstance("EC", "BC").generatePrivate(keySpec);

            Signature signature = Signature.getInstance("SHA256withECDSA", "BC");
            signature.initSign(privateKey);
            signature.update(data.getBytes(StandardCharsets.UTF_8));

            byte[] signedData = signature.sign();
            return Base64.getEncoder().encodeToString(signedData);
        } catch (Exception e) {
            throw new RuntimeException("Error generating X-Sign signature", e);
        }
    }

}
