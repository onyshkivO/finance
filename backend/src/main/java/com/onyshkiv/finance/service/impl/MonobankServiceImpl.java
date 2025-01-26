package com.onyshkiv.finance.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.onyshkiv.finance.exception.ExternalServiceException;
import com.onyshkiv.finance.exception.NotFoundException;
import com.onyshkiv.finance.model.dto.MonobankAuthDto;
import com.onyshkiv.finance.model.dto.monobank.MonobankClientDto;
import com.onyshkiv.finance.model.dto.monobank.StatementItemDetailsDto;
import com.onyshkiv.finance.model.dto.monobank.StatementItemDto;
import com.onyshkiv.finance.model.entity.MonobankAccount;
import com.onyshkiv.finance.model.entity.MonobankAuth;
import com.onyshkiv.finance.model.entity.Transaction;
import com.onyshkiv.finance.model.entity.TransactionType;
import com.onyshkiv.finance.repository.MonobankAccountRepository;
import com.onyshkiv.finance.repository.MonobankAuthRepository;
import com.onyshkiv.finance.repository.TransactionRepository;
import com.onyshkiv.finance.security.SecurityContextHelper;
import com.onyshkiv.finance.service.MonobankService;
import com.onyshkiv.finance.util.ApplicationMapper;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.PrivateKey;
import java.security.Signature;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
public class MonobankServiceImpl implements MonobankService {
    private static final String MONOBANK_AUTH_URL = "https://api.monobank.ua";
    private static final String REQUEST_ACCESS = "/personal/auth/request";
    private static final String SET_WEBHOOK = "/personal/corp/webhook";
    private static final String CLIENT_INFO = "/personal/client-info";

    private static final String BASIC_URI = "http://localhost:8080";
    private static final String CONFIRM_WEBHOOK_URL = BASIC_URI + "/mono/confirm";
    private static final String TRANSACTION_WEBHOOK_URL = BASIC_URI + "/mono/transaction";

    @Value("${monobank.x-key-id}")
    private String xKeyId;
    @Value("${monobank.private-key-file-path}")
    private String privateKeyPath;

    private final OkHttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final MonobankAuthRepository monobankAuthRepository;
    private final MonobankAccountRepository monobankAccountRepository;
    private final SecurityContextHelper securityContextHelper;
    private final ApplicationMapper applicationMapper;
    private final TransactionRepository transactionRepository;


    @Autowired
    public MonobankServiceImpl(ObjectMapper objectMapper,
                               MonobankAuthRepository monobankAuthRepository,
                               OkHttpClient httpClient,
                               MonobankAccountRepository monobankAccountRepository,
                               SecurityContextHelper securityContextHelper,
                               ApplicationMapper applicationMapper,
                               TransactionRepository transactionRepository) {
        this.objectMapper = objectMapper;
        this.monobankAuthRepository = monobankAuthRepository;
        this.httpClient = httpClient;
        this.monobankAccountRepository = monobankAccountRepository;
        this.securityContextHelper = securityContextHelper;
        this.applicationMapper = applicationMapper;
        this.transactionRepository = transactionRepository;
    }

    @Transactional
    public void confirmAccess(String requestId) {
        MonobankAuth monobankAuth = monobankAuthRepository.findByRequestId(requestId)
                .orElseThrow(() -> new NotFoundException("Monobank not found with requestId " + requestId));
        monobankAuth.setActivated(true);
        saveClientAccounts(requestId);
        setWebhook(requestId);
    }

    @Transactional
    public MonobankAuthDto requestAccessAndStore() {
        securityContextHelper.validateLoggedInUser();
        try {
            String responseJson = requestAccess();

            MonobankAuthDto response = objectMapper.readValue(responseJson, MonobankAuthDto.class);
            MonobankAuth auth = MonobankAuth.builder()
                    .userId(securityContextHelper.getLoggedInUser().getId())
                    .activated(false)
                    .requestId(response.getTokenRequestId())
                    .acceptUrl(response.getAcceptUrl())
                    .build();
            monobankAuthRepository.save(auth);
            return response;
        } catch (IOException | ExternalServiceException e) {
            throw new ExternalServiceException("Error during request to Monobank api: " + e.getMessage(), e);
        }
    }

    private void setWebhook(String requestId) {
        try {
            String url = MONOBANK_AUTH_URL + SET_WEBHOOK;
            String xTime = String.valueOf(System.currentTimeMillis() / 1000);
            String xSign = generateSignature(xTime, SET_WEBHOOK, privateKeyPath);
            RequestBody formBody = new FormBody.Builder()
                    .add("webHookUrl", TRANSACTION_WEBHOOK_URL)
                    .build();

            Request request = new Request.Builder()
                    .url(url)
                    .header("X-Key-Id", xKeyId)
                    .header("X-Request-Id", requestId)
                    .header("X-Time", xTime)
                    .header("X-Sign", xSign)
                    .header("Content-Type", "application/json")
                    .post(formBody)
                    .build();

            sendRequestToMonobankApi(request);
        } catch (IOException | ExternalServiceException e) {
            throw new ExternalServiceException("Error during request to Monobank api: " + e.getMessage(), e);
        }
    }

    @Transactional
    public void parseAndSaveTransactionWebhook(StatementItemDto statementItemDto) {
        UUID userId = monobankAuthRepository.getUserIdByAccountId(statementItemDto.getAccount())
                .orElseThrow(() -> new NotFoundException("User not found for monobank account id: " + statementItemDto.getAccount()));
        StatementItemDetailsDto transactionDetails = statementItemDto.getStatementItem();
        Transaction transaction = Transaction.builder()
                .id(UUID.randomUUID())
                .userId(userId)
                .category(null)//todo  logic for category
                .type(transactionDetails.getAmount().compareTo(BigDecimal.ZERO) > 0 ? TransactionType.INCOME : TransactionType.EXPENSE)
                .amount(transactionDetails.getAmount().divide(BigDecimal.valueOf(100L)))
                .description(transactionDetails.getDescription())
                .transactionDate(transactionDetails.getTime().toLocalDate())
                .build();
        transactionRepository.save(transaction);

    }

    private void saveClientAccounts(String requestId) {
        try {
            String responseJson = getClientInfo(requestId);
            MonobankClientDto monobankClientDto = objectMapper.readValue(responseJson, MonobankClientDto.class);
            List<MonobankAccount> monobankAccounts = applicationMapper.monobankClientDtoToMonobankAccountList(monobankClientDto, requestId);
            monobankAccountRepository.saveAll(monobankAccounts);
        } catch (IOException | ExternalServiceException e) {
            throw new ExternalServiceException("Error during request to Monobank api: " + e.getMessage(), e);
        }
    }

    private String getClientInfo(String requestId) {
        try {
            String url = MONOBANK_AUTH_URL + REQUEST_ACCESS;
            String xTime = String.valueOf(System.currentTimeMillis() / 1000);
            String xSign = generateSignature(xTime, REQUEST_ACCESS, privateKeyPath);

            Request request = new Request.Builder()
                    .url(url)
                    .header("X-Key-Id", xKeyId)
                    .header("X-Time", xTime)
                    .header("X-Sign", xSign)
                    .header("X-Request-Id", requestId)
                    .header("Content-Type", "application/json")
                    .get()
                    .build();

            return sendRequestToMonobankApi(request);
        } catch (IOException | ExternalServiceException e) {
            throw new ExternalServiceException("Error during request to Monobank api: " + e.getMessage(), e);
        }
    }

    private String requestAccess() {
        try {
            String url = MONOBANK_AUTH_URL + CLIENT_INFO;
            String xTime = String.valueOf(System.currentTimeMillis() / 1000);
            String xSign = generateSignature(xTime, REQUEST_ACCESS, privateKeyPath);

            Request request = new Request.Builder()
                    .url(url)
                    .header("X-Key-Id", xKeyId)
                    .header("X-Time", xTime)
                    .header("X-Sign", xSign)
                    .header("X-Callback", CONFIRM_WEBHOOK_URL)
                    .header("Content-Type", "application/json")
                    .post(RequestBody.create(null, new byte[0]))
                    .build();

            return sendRequestToMonobankApi(request);
        } catch (IOException | ExternalServiceException e) {
            throw new ExternalServiceException("Error during request to Monobank api: " + e.getMessage(), e);
        }
    }

    private String sendRequestToMonobankApi(Request request) throws IOException {
        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new ExternalServiceException("Unexpected response from monobank api: " + response);
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
