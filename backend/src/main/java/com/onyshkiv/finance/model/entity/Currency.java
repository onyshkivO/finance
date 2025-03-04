package com.onyshkiv.finance.model.entity;

import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public enum Currency {
    AED("Emirati Dirham"),
    AFN("Afghan Afghani"),
    ALL("Albanian Lek"),
    AMD("Armenian Dram"),
    ANG("Dutch Guilder"),
    AOA("Angolan Kwanza"),
    ARS("Argentine Peso"),
    AUD("Australian Dollar"),
    AZN("Azerbaijan Manat"),
    BAM("Bosnian Convertible Mark"),
    BDT("Bangladeshi Taka"),
    BGN("Bulgarian Lev"),
    BHD("Bahraini Dinar"),
    BIF("Burundian Franc"),
    BMD("Bermudian Dollar"),
    BND("Bruneian Dollar"),
    BOB("Bolivian Bolíviano"),
    BRL("Brazilian Real"),
    BSD("Bahamian Dollar"),
    BTN("Bhutanese Ngultrum"),
    BWP("Botswana Pula"),
    BYN("Belarusian Ruble"),
    CAD("Canadian Dollar"),
    CHF("Swiss Franc"),
    CLP("Chilean Peso"),
    CNY("Chinese Yuan Renminbi"),
    COP("Colombian Peso"),
    CRC("Costa Rican Colon"),
    CZK("Czech Koruna"),
    DKK("Danish Krone"),
    DOP("Dominican Peso"),
    DZD("Algerian Dinar"),
    EGP("Egyptian Pound"),
    EUR("Euro"),
    GBP("British Pound"),
    GEL("Georgian Lari"),
    GHS("Ghanaian Cedi"),
    HKD("Hong Kong Dollar"),
    HRK("Croatian Kuna"),
    HUF("Hungarian Forint"),
    IDR("Indonesian Rupiah"),
    ILS("Israeli Shekel"),
    INR("Indian Rupee"),
    IQD("Iraqi Dinar"),
    IRR("Iranian Rial"),
    ISK("Icelandic Krona"),
    JMD("Jamaican Dollar"),
    JOD("Jordanian Dinar"),
    JPY("Japanese Yen"),
    KES("Kenyan Shilling"),
    KGS("Kyrgyzstani Som"),
    KHR("Cambodian Riel"),
    KRW("South Korean Won"),
    KWD("Kuwaiti Dinar"),
    KZT("Kazakhstani Tenge"),
    LAK("Lao Kip"),
    LBP("Lebanese Pound"),
    LKR("Sri Lankan Rupee"),
    LYD("Libyan Dinar"),
    MAD("Moroccan Dirham"),
    MDL("Moldovan Leu"),
    MKD("Macedonian Denar"),
    MMK("Burmese Kyat"),
    MNT("Mongolian Tughrik"),
    MXN("Mexican Peso"),
    MYR("Malaysian Ringgit"),
    MZN("Mozambican Metical"),
    NAD("Namibian Dollar"),
    NGN("Nigerian Naira"),
    NZD("New Zealand Dollar"),
    OMR("Omani Rial"),
    PEN("Peruvian Sol"),
    PHP("Philippine Peso"),
    PKR("Pakistani Rupee"),
    PLN("Polish Zloty"),
    QAR("Qatari Riyal"),
    RON("Romanian Leu"),
    RSD("Serbian Dinar"),
    SAR("Saudi Riyal"),
    SEK("Swedish Krona"),
    SGD("Singapore Dollar"),
    THB("Thai Baht"),
    TRY("Turkish Lira"),
    UAH("Ukrainian Hryvnia"),
    USD("US Dollar"),
    VND("Vietnamese Dong"),
    ZAR("South African Rand");

    private final String description;

    Currency(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public static Currency fromCode(String code) {
        try {
            return Currency.valueOf(code.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Unknown currency code: " + code);
        }
    }

    public static Map<String, String> getCurrencyMap() {
        return Stream.of(Currency.values())
                .collect(Collectors.toMap(Enum::name, Currency::getDescription));
    }
}
