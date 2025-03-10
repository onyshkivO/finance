package com.onyshkiv.finance.service;

import com.onyshkiv.finance.model.entity.Currency;

public interface UserService {
    void changeUserBaseCurrency(Currency currencyToConvert);
}
