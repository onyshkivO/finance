package com.onyshkiv.finance.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.onyshkiv.finance.exception.ExternalServiceException;
import com.onyshkiv.finance.exception.NotFoundException;
import com.onyshkiv.finance.model.entity.Currency;
import com.onyshkiv.finance.service.CurrencyService;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Service
@Slf4j
public class Fawazahmed0CurrencyServiceImpl implements CurrencyService {

    private final String BASE_URL = "https://cdn.jsdelivr.net/npm/@fawazahmed0/currency-api@latest/v1/currencies/";
    private final String FALLBACK_URL = "https://latest.currency-api.pages.dev/v1/currencies/";
    private DateTimeFormatter frmt = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private final OkHttpClient httpClient;
    private final ObjectMapper objectMapper;

    public Fawazahmed0CurrencyServiceImpl(OkHttpClient httpClient, ObjectMapper objectMapper) {
        this.httpClient = httpClient;
        this.objectMapper = objectMapper;
    }

    @Override
    public BigDecimal convert(BigDecimal amount, Currency currencyFrom, Currency currencyTo, LocalDate dateOfTransaction) {
        String currencyFromString = currencyFrom.name().toLowerCase();
        String currencyToString = currencyTo.name().toLowerCase();

        double exchangeRate;
        try {
            exchangeRate = getExchangeRate(dateOfTransaction, currencyFromString, currencyToString, BASE_URL);
            return amount.multiply(BigDecimal.valueOf(exchangeRate)).setScale(2, RoundingMode.HALF_UP);
        } catch (ExternalServiceException e) {
            log.warn("Error while getting exchange rate for currencies from : {} to : {}, from base url, retrying with fallback url",
                    currencyFromString, currencyToString);
        }
        exchangeRate = getExchangeRate(dateOfTransaction, currencyFromString, currencyToString, FALLBACK_URL);
        return amount.multiply(BigDecimal.valueOf(exchangeRate)).setScale(2, RoundingMode.HALF_UP);
    }

    private double getExchangeRate(LocalDate dateOfTransaction, String currencyFromString, String currencyToString, String url) {
        Request baseUrlRequest = new Request.Builder()
                .url(url.replace("latest", dateOfTransaction.format(frmt)) + currencyFromString + ".json")
                .header("Content-Type", "application/json")
                .get()
                .build();

        JsonNode response = sendRequestHttpRequest(baseUrlRequest);
        JsonNode currencyNode = response.path(currencyFromString).path(currencyToString);
        if (currencyNode == null || currencyNode.isMissingNode()) {
            log.error("Not supported currency to convert {}", currencyToString);
            throw new NotFoundException("Not supported currency " + currencyToString);
        }
        return currencyNode.asDouble();
    }

    private JsonNode sendRequestHttpRequest(Request request) {
        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                log.error("Not success response from external api: {} body: {}", response, (response.body() != null ? response.body().string() : null));
                throw new ExternalServiceException("Exchange rate service is temporarily unavailable. Please try again later.");
            }
            if (response.body() == null) {
                log.error("Not response body present from external api: {}  ", response);
                throw new ExternalServiceException("Exchange rate service is temporarily unavailable. Please try again later.");
            }
            return objectMapper.readTree(response.body().string());
        } catch (Exception e) {
            log.error("Unexpected error occurs while sending request to {}", request.url(), e);
            throw new ExternalServiceException("Exchange rate service is temporarily unavailable. Please try again later.");
        }
    }
}