package com.onyshkiv.finance.service;

import com.onyshkiv.finance.model.entity.Currency;

import java.math.BigDecimal;
import java.time.LocalDate;

public interface CurrencyService {
    BigDecimal convert(BigDecimal amount, Currency currencyFrom, Currency currencyTo, LocalDate dateOfTransaction);

    BigDecimal getExchangeRate(Currency currencyFrom, Currency currencyTo, LocalDate dateOfTransaction);

}
