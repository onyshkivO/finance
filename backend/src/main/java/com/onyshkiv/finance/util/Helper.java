package com.onyshkiv.finance.util;

import com.onyshkiv.finance.model.entity.Currency;

public abstract class Helper {
    public static  Currency convertCurrencyCodeToCurrency(Integer currencyCode) {
        return switch (currencyCode) {
            case 978 -> Currency.EUR;
            case 840 -> Currency.USD;
            case 980 -> Currency.UAH;
            default ->
                    throw new UnsupportedOperationException("Cannot convert currencyCode " + currencyCode + " to currency");
        };
    }
}
