package com.onyshkiv.finance.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.onyshkiv.finance.exception.ExternalServiceException;
import com.onyshkiv.finance.exception.NotFoundException;
import com.onyshkiv.finance.model.dto.MonobankAuthDto;
import com.onyshkiv.finance.model.dto.monobank.MonobankCardResponse;
import com.onyshkiv.finance.model.dto.monobank.MonobankClientDto;
import com.onyshkiv.finance.model.dto.monobank.StatementItemDetailsDto;
import com.onyshkiv.finance.model.dto.monobank.StatementItemDto;
import com.onyshkiv.finance.model.entity.Cashbox;
import com.onyshkiv.finance.model.entity.Category;
import com.onyshkiv.finance.model.entity.Currency;
import com.onyshkiv.finance.model.entity.MonobankAccount;
import com.onyshkiv.finance.model.entity.MonobankAuth;
import com.onyshkiv.finance.model.entity.Transaction;
import com.onyshkiv.finance.model.entity.TransactionType;
import com.onyshkiv.finance.model.entity.User;
import com.onyshkiv.finance.repository.CashboxRepository;
import com.onyshkiv.finance.repository.CategoryMccRepository;
import com.onyshkiv.finance.repository.MonobankAccountRepository;
import com.onyshkiv.finance.repository.MonobankAuthRepository;
import com.onyshkiv.finance.repository.TransactionRepository;
import com.onyshkiv.finance.repository.UserRepository;
import com.onyshkiv.finance.security.SecurityContextHelper;
import com.onyshkiv.finance.service.MonobankService;
import com.onyshkiv.finance.service.TransactionService;
import com.onyshkiv.finance.util.ApplicationMapper;
import lombok.extern.slf4j.Slf4j;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.bouncycastle.asn1.pkcs.PrivateKeyInfo;
import org.bouncycastle.openssl.PEMKeyPair;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.Signature;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.Base64;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static com.onyshkiv.finance.model.entity.TransactionType.EXPENSE;
import static com.onyshkiv.finance.util.Helper.convertCurrencyCodeToCurrency;

@Service
@Slf4j
public class MonobankServiceImpl implements MonobankService {
    private static final String MONOBANK_AUTH_URL = "https://api.monobank.ua";
    private static final String REQUEST_ACCESS = "/personal/auth/request";
    private static final String SET_WEBHOOK = "/personal/corp/webhook";
    private static final String CLIENT_INFO = "/personal/client-info";

    private static final String BASIC_URI = "https://61cd-178-212-97-140.ngrok-free.app";
    private static final String CONFIRM_WEBHOOK_URL = BASIC_URI + "/mono/confirm";
    private static final String TRANSACTION_WEBHOOK_URL = BASIC_URI + "/mono/transaction";
    private final UserRepository userRepository;

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
    private final CategoryMccRepository categoryMccRepository;
    private final TransactionService transactionService;
    private final CashboxRepository cashboxRepository;


    @Autowired
    public MonobankServiceImpl(ObjectMapper objectMapper,
                               MonobankAuthRepository monobankAuthRepository,
                               OkHttpClient httpClient,
                               MonobankAccountRepository monobankAccountRepository,
                               SecurityContextHelper securityContextHelper,
                               ApplicationMapper applicationMapper,
                               TransactionRepository transactionRepository,
                               CategoryMccRepository categoryMccRepository,
                               UserRepository userRepository,
                               TransactionService transactionService, CashboxRepository cashboxRepository) {
        this.objectMapper = objectMapper;
        this.monobankAuthRepository = monobankAuthRepository;
        this.httpClient = httpClient;
        this.monobankAccountRepository = monobankAccountRepository;
        this.securityContextHelper = securityContextHelper;
        this.applicationMapper = applicationMapper;
        this.transactionRepository = transactionRepository;
        this.categoryMccRepository = categoryMccRepository;
        this.userRepository = userRepository;
        this.transactionService = transactionService;
        this.cashboxRepository = cashboxRepository;
    }

    @Transactional
    public void confirmAccess(String requestId) {
        MonobankAuth monobankAuth = monobankAuthRepository.findByRequestId(requestId)
                .orElseThrow(() -> new NotFoundException("Monobank not found with requestId " + requestId));
        monobankAuth.setActivated(true);
        saveClientAccounts(requestId, monobankAuth.getUserId());
        setWebhook(requestId);
    }

    @Transactional
    public MonobankAuthDto requestAccessAndStore() {
        UUID loggedInUserId = securityContextHelper.getLoggedInUser().getId();
        Optional<MonobankAuth> monobankAuthFromDb = monobankAuthRepository.findByUserId(loggedInUserId);
        if  (monobankAuthFromDb.isPresent()){
            return MonobankAuthDto.builder()
                    .isConnected(monobankAuthFromDb.get().isActivated())
                    .acceptUrl(monobankAuthFromDb.get().getAcceptUrl())
                    .build();
        }
        try {
            String responseJson = requestAccess();

            MonobankAuthDto response = objectMapper.readValue(responseJson, MonobankAuthDto.class);
            response.setIsConnected(false);
            MonobankAuth auth = MonobankAuth.builder()
                    .userId(loggedInUserId)
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
            String xTime = String.valueOf(Instant.now().getEpochSecond());
            String xSign = generateSignature(xTime + SET_WEBHOOK, SET_WEBHOOK, privateKeyPath);

            String jsonBody = "{\"webHookUrl\": \"" + TRANSACTION_WEBHOOK_URL + "\"}";
            MediaType JSON = MediaType.get("application/json; charset=utf-8");
            RequestBody requestBody = RequestBody.create(jsonBody, JSON);
            Request request = new Request.Builder()
                    .url(url)
                    .header("X-Key-Id", xKeyId)
                    .header("X-Request-Id", requestId)
                    .header("X-Time", xTime)
                    .header("X-Sign", xSign)
                    .header("Content-Type", "application/json")
                    .post(requestBody)
                    .build();

            sendRequestToMonobankApi(request);
        } catch (IOException | ExternalServiceException e) {
            throw new ExternalServiceException("Error during request to Monobank api: " + e.getMessage(), e);
        } catch (RuntimeException e) {
            log.error("setWebhook :: Unexpected exception ", e);
            throw e;
        }
    }

    @Transactional
    public void parseAndSaveTransactionWebhook(StatementItemDto statementItemDto) {
        MonobankAccount monobankAccount = monobankAccountRepository.findByAccountId(statementItemDto.getAccount())
                .orElseThrow(() -> new NotFoundException("Monobank account not found for monobank account id: " + statementItemDto.getAccount()));
        if (!monobankAccount.getMonitor()) {
            log.info("user do not monitor account with id {}, skipping transaction", statementItemDto.getAccount());
            return;
        }
        UUID userId = monobankAccount.getUserId();
        StatementItemDetailsDto transactionDetails = statementItemDto.getStatementItem();

        TransactionType type = transactionDetails.getAmount().compareTo(BigInteger.ZERO) > 0 ? TransactionType.INCOME : EXPENSE;
        BigDecimal amount = new BigDecimal(transactionDetails.getAmount()).divide(BigDecimal.valueOf(100)).abs();
        Optional<UUID> categoryIdOptional = categoryMccRepository.getCategoryIdByMccAndUserIdAndType(transactionDetails.getMcc(), userId, type);
//        Currency transactionCurrency = convertCurrencyCodeToCurrency(transactionDetails.getCurrencyCode());
        Currency transactionCurrency = convertCurrencyCodeToCurrency(monobankAccount.getCurrencyCode());
        User user = userRepository.findById(userId).orElseThrow(() -> new NotFoundException("User not found with id " + userId));

        Transaction transaction = Transaction.builder()
                .id(UUID.randomUUID())
                .userId(userId)
                .category(categoryIdOptional.map(Category::new).orElse(null))
                .type(type)
                .cashbox(monobankAccount.getCashbox())
                .description(transactionDetails.getDescription())
                .transactionDate(transactionDetails.getTransactionDate())
                .build();
        transactionService.setTransactionAmountInternal(amount, transactionCurrency, user.getCurrency(), transaction);
        Cashbox cashbox = monobankAccount.getCashbox();
        BigDecimal cashboxBalance = cashbox.getBalance();
        cashbox.setBalance(EXPENSE.equals(transaction.getType()) ? cashboxBalance.subtract(transaction.getBaseAmount()) : cashboxBalance.add(transaction.getBaseAmount()));
        transactionRepository.save(transaction);

    }

    @Override
    @Transactional
    public void monitorAccount(String accountId) {
        MonobankAccount monobankAccount = monobankAccountRepository.findByAccountId(accountId)
                .orElseThrow(() -> new NotFoundException("Monobank account not found for monobank account id: " + accountId));
        monobankAccount.setMonitor(true);
        monobankAccount.getCashbox().setDeletedAt(null);
    }

    @Override
    @Transactional
    public void unmonitorAccount(String accountId) {
        MonobankAccount monobankAccount = monobankAccountRepository.findByAccountId(accountId)
                .orElseThrow(() -> new NotFoundException("Monobank account not found for monobank account id: " + accountId));
        monobankAccount.setMonitor(false);
    }

    @Override
    public List<MonobankCardResponse> getUserMonobankAccounts() {
        UUID userId = securityContextHelper.getLoggedInUser().getId();
        List<MonobankAccount> userMonobankAccounts = monobankAccountRepository.findAllByUserId(userId);
        return userMonobankAccounts.stream()
                .map(applicationMapper::monobankAccountToMonobankCardResponse)
                .toList();
    }

    private void saveClientAccounts(String requestId, UUID userId) {
        try {
            String responseJson = getClientInfo(requestId);
            MonobankClientDto monobankClientDto = objectMapper.readValue(responseJson, MonobankClientDto.class);
            List<MonobankAccount> monobankAccounts = applicationMapper.monobankClientDtoToMonobankAccountList(monobankClientDto, userId);

            monobankAccounts.forEach(account -> {
                Currency accountCurrency = convertCurrencyCodeToCurrency(account.getCurrencyCode());
                Cashbox cashbox = Cashbox.builder()
                        .id(UUID.randomUUID())
                        .userId(userId)
                        .currency(accountCurrency)
                        .name(account.getType().name().toLowerCase() + " " + accountCurrency.name() + " monobank card")
                        .balance(account.getBalance())
                        .deletedAt(OffsetDateTime.now())
                        .build();
                cashboxRepository.save(cashbox);
                account.setCashbox(cashbox);
                monobankAccountRepository.upsertMonobankAccount(account);
            });
        } catch (IOException | ExternalServiceException e) {
            throw new ExternalServiceException("Error during request to Monobank api: " + e.getMessage(), e);
        }
    }

    private String getClientInfo(String requestId) {
        try {
            String url = MONOBANK_AUTH_URL + CLIENT_INFO;
            String xTime = String.valueOf(Instant.now().getEpochSecond());
            String xSign = generateSignature(xTime + requestId + CLIENT_INFO, CLIENT_INFO, privateKeyPath);

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
            String url = MONOBANK_AUTH_URL + REQUEST_ACCESS;
            String xTime = String.valueOf(Instant.now().getEpochSecond());
            String xSign = generateSignature(xTime + REQUEST_ACCESS, REQUEST_ACCESS, privateKeyPath);

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
                throw new ExternalServiceException("Not success response from monobank api: " + response + " body: " + (response.body() != null ? response.body().string() : null));
            }
            return response.body() != null ? response.body().string() : null;
        }
    }

    public String generateSignature(String dataToSign, String resource, String privateKeyPath) {
        try {
            PrivateKey privateKey = loadECPrivateKey(privateKeyPath);
            byte[] data = dataToSign.getBytes(StandardCharsets.UTF_8);

            Signature signer = Signature.getInstance("SHA256withECDSA", "BC");
            signer.initSign(privateKey);
            signer.update(data);
            byte[] signatureBytes = signer.sign();
            return Base64.getEncoder().encodeToString(signatureBytes);
        } catch (Exception e) {
            log.error("Error generation X-Sign for request to {}", resource, e);
            throw new RuntimeException("Error generation X-Sign", e);
        }
    }

    private PrivateKey loadECPrivateKey(String filename) throws Exception {
        try (Reader reader = new FileReader(filename);
             PEMParser pemParser = new PEMParser(reader)) {

            Object object;
            JcaPEMKeyConverter converter = new JcaPEMKeyConverter().setProvider("BC");
            while ((object = pemParser.readObject()) != null) {
                if (object instanceof PEMKeyPair) {
                    PEMKeyPair pemKeyPair = (PEMKeyPair) object;
                    KeyPair keyPair = converter.getKeyPair(pemKeyPair);
                    return keyPair.getPrivate();
                }
                if (object instanceof PrivateKeyInfo) {
                    PrivateKeyInfo privateKeyInfo = (PrivateKeyInfo) object;
                    return converter.getPrivateKey(privateKeyInfo);
                }
            }
            throw new ExternalServiceException("No EC KeyPair found in PEM file");
        }
    }

}
