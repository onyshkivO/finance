export type TransactionType = "income" | "expense"

export type Timeframe = "month" | "year";
export type Period = {year: number, month: number};
export interface UserData {
  token: string;
  login: string;
  currency: Currency;
  id: string;
}
export interface Category {
  id: string;
  name: string;
  icon: string;
  type: string;
  mccCodes: Set<number>;
}

export interface BalanceStats {
  expense: number;
  income: number;
}

export interface Currency {
  code: string;
  name: string;
  symbol: string;
  locale: string;
}

export type BackendCategoryStats = {
  type: 'INCOME' | 'EXPENSE'; // Backend uses uppercase
  category: string;
  icon: string;
  amount: number;
};

export interface Transaction {
  id: string;
  category: Category;
  type: "income" | "expense";
  amount: number;
  formatedAmount: string;
  description?: string | null;
  currency: Currency;
  transactionDate: string;
  cashbox: Cashbox;
}

export interface TransactionDtoBackend {
  id: string; 
  category: CategoryDtoBackend;
  type: "INCOME" | "EXPENSE"; 
  amount: number;
  description?: string | null;
  currency: string;
  transactionDate: string; 
  cashbox: Cashbox;
}

export interface CategoryDtoBackend {
  id: string; 
  name: string;
  type: "INCOME" | "EXPENSE"; 
  icon?: string;
  mccCodes?: number[];
}

export type CategoryStatsType = {
  type: TransactionType;
  category: string;
  icon: string;
  amount: number;
};

export type BackendCashboxStats = {
  type: 'INCOME' | 'EXPENSE';
  cashbox: string;
  amount: number;
};

export type CashboxStatsType = {
  type: TransactionType;
  cashbox: string;
  amount: number;
};

export type HistoryStatsType = {
  expense: number;
  income: number;
  year: number;
  month: number;
};

export type Cashbox = {
  id: string;
  userId: string;
  name: string;
  currency: string;
  balance: number;
};

export const CURRENCIES = [
  { code: "AED", name: "Emirati Dirham", symbol: "د.إ", locale: "ar-AE" },
  { code: "AFN", name: "Afghan Afghani", symbol: "؋", locale: "fa-AF" },
  { code: "ALL", name: "Albanian Lek", symbol: "L", locale: "sq-AL" },
  { code: "AMD", name: "Armenian Dram", symbol: "֏", locale: "hy-AM" },
  { code: "ANG", name: "Dutch Guilder", symbol: "ƒ", locale: "nl-CW" },
  { code: "AOA", name: "Angolan Kwanza", symbol: "Kz", locale: "pt-AO" },
  { code: "ARS", name: "Argentine Peso", symbol: "$", locale: "es-AR" },
  { code: "AUD", name: "Australian Dollar", symbol: "$", locale: "en-AU" },
  { code: "AZN", name: "Azerbaijan Manat", symbol: "₼", locale: "az-AZ" },
  { code: "BAM", name: "Bosnian Convertible Mark", symbol: "KM", locale: "bs-BA" },
  { code: "BDT", name: "Bangladeshi Taka", symbol: "৳", locale: "bn-BD" },
  { code: "BGN", name: "Bulgarian Lev", symbol: "лв", locale: "bg-BG" },
  { code: "BHD", name: "Bahraini Dinar", symbol: ".د.ب", locale: "ar-BH" },
  { code: "BIF", name: "Burundian Franc", symbol: "FBu", locale: "fr-BI" },
  { code: "BMD", name: "Bermudian Dollar", symbol: "$", locale: "en-BM" },
  { code: "BND", name: "Bruneian Dollar", symbol: "$", locale: "ms-BN" },
  { code: "BOB", name: "Bolivian Bolíviano", symbol: "Bs.", locale: "es-BO" },
  { code: "BRL", name: "Brazilian Real", symbol: "R$", locale: "pt-BR" },
  { code: "BSD", name: "Bahamian Dollar", symbol: "$", locale: "en-BS" },
  { code: "BTN", name: "Bhutanese Ngultrum", symbol: "Nu.", locale: "dz-BT" },
  { code: "BWP", name: "Botswana Pula", symbol: "P", locale: "en-BW" },
  { code: "BYN", name: "Belarusian Ruble", symbol: "Br", locale: "be-BY" },
  { code: "CAD", name: "Canadian Dollar", symbol: "$", locale: "en-CA" },
  { code: "CHF", name: "Swiss Franc", symbol: "CHF", locale: "de-CH" },
  { code: "CLP", name: "Chilean Peso", symbol: "$", locale: "es-CL" },
  { code: "CNY", name: "Chinese Yuan Renminbi", symbol: "¥", locale: "zh-CN" },
  { code: "COP", name: "Colombian Peso", symbol: "$", locale: "es-CO" },
  { code: "CRC", name: "Costa Rican Colon", symbol: "₡", locale: "es-CR" },
  { code: "CZK", name: "Czech Koruna", symbol: "Kč", locale: "cs-CZ" },
  { code: "DKK", name: "Danish Krone", symbol: "kr", locale: "da-DK" },
  { code: "DOP", name: "Dominican Peso", symbol: "RD$", locale: "es-DO" },
  { code: "DZD", name: "Algerian Dinar", symbol: "دج", locale: "ar-DZ" },
  { code: "EGP", name: "Egyptian Pound", symbol: "£", locale: "ar-EG" },
  { code: "EUR", name: "Euro", symbol: "€", locale: "de-DE" }, // Germany as default Eurozone country
  { code: "GBP", name: "British Pound", symbol: "£", locale: "en-GB" },
  { code: "GEL", name: "Georgian Lari", symbol: "₾", locale: "ka-GE" },
  { code: "GHS", name: "Ghanaian Cedi", symbol: "₵", locale: "en-GH" },
  { code: "HKD", name: "Hong Kong Dollar", symbol: "$", locale: "zh-HK" },
  { code: "HRK", name: "Croatian Kuna", symbol: "kn", locale: "hr-HR" },
  { code: "HUF", name: "Hungarian Forint", symbol: "Ft", locale: "hu-HU" },
  { code: "IDR", name: "Indonesian Rupiah", symbol: "Rp", locale: "id-ID" },
  { code: "ILS", name: "Israeli Shekel", symbol: "₪", locale: "he-IL" },
  { code: "INR", name: "Indian Rupee", symbol: "₹", locale: "en-IN" },
  { code: "IQD", name: "Iraqi Dinar", symbol: "ع.د", locale: "ar-IQ" },
  { code: "IRR", name: "Iranian Rial", symbol: "﷼", locale: "fa-IR" },
  { code: "ISK", name: "Icelandic Krona", symbol: "kr", locale: "is-IS" },
  { code: "JMD", name: "Jamaican Dollar", symbol: "J$", locale: "en-JM" },
  { code: "JOD", name: "Jordanian Dinar", symbol: "د.ا", locale: "ar-JO" },
  { code: "JPY", name: "Japanese Yen", symbol: "¥", locale: "ja-JP" },
  { code: "KES", name: "Kenyan Shilling", symbol: "KSh", locale: "sw-KE" },
  { code: "KGS", name: "Kyrgyzstani Som", symbol: "с", locale: "ky-KG" },
  { code: "KHR", name: "Cambodian Riel", symbol: "៛", locale: "km-KH" },
  { code: "KRW", name: "South Korean Won", symbol: "₩", locale: "ko-KR" },
  { code: "KWD", name: "Kuwaiti Dinar", symbol: "د.ك", locale: "ar-KW" },
  { code: "KZT", name: "Kazakhstani Tenge", symbol: "₸", locale: "kk-KZ" },
  { code: "LAK", name: "Lao Kip", symbol: "₭", locale: "lo-LA" },
  { code: "LBP", name: "Lebanese Pound", symbol: "ل.ل", locale: "ar-LB" },
  { code: "LKR", name: "Sri Lankan Rupee", symbol: "Rs", locale: "si-LK" },
  { code: "LYD", name: "Libyan Dinar", symbol: "ل.د", locale: "ar-LY" },
  { code: "MAD", name: "Moroccan Dirham", symbol: "د.م.", locale: "ar-MA" },
  { code: "MDL", name: "Moldovan Leu", symbol: "L", locale: "ro-MD" },
  { code: "MKD", name: "Macedonian Denar", symbol: "ден", locale: "mk-MK" },
  { code: "MMK", name: "Burmese Kyat", symbol: "K", locale: "my-MM" },
  { code: "MNT", name: "Mongolian Tughrik", symbol: "₮", locale: "mn-MN" },
  { code: "MXN", name: "Mexican Peso", symbol: "$", locale: "es-MX" },
  { code: "MYR", name: "Malaysian Ringgit", symbol: "RM", locale: "ms-MY" },
  { code: "MZN", name: "Mozambican Metical", symbol: "MT", locale: "pt-MZ" },
  { code: "NAD", name: "Namibian Dollar", symbol: "$", locale: "en-NA" },
  { code: "NGN", name: "Nigerian Naira", symbol: "₦", locale: "en-NG" },
  { code: "NZD", name: "New Zealand Dollar", symbol: "$", locale: "en-NZ" },
  { code: "OMR", name: "Omani Rial", symbol: "ر.ع.", locale: "ar-OM" },
  { code: "PEN", name: "Peruvian Sol", symbol: "S/.", locale: "es-PE" },
  { code: "PHP", name: "Philippine Peso", symbol: "₱", locale: "en-PH" },
  { code: "PKR", name: "Pakistani Rupee", symbol: "Rs", locale: "ur-PK" },
  { code: "PLN", name: "Polish Zloty", symbol: "zł", locale: "pl-PL" },
  { code: "QAR", name: "Qatari Riyal", symbol: "ر.ق", locale: "ar-QA" },
  { code: "RON", name: "Romanian Leu", symbol: "lei", locale: "ro-RO" },
  { code: "RSD", name: "Serbian Dinar", symbol: "дин", locale: "sr-RS" },
  { code: "SAR", name: "Saudi Riyal", symbol: "﷼", locale: "ar-SA" },
  { code: "SEK", name: "Swedish Krona", symbol: "kr", locale: "sv-SE" },
  { code: "SGD", name: "Singapore Dollar", symbol: "$", locale: "en-SG" },
  { code: "THB", name: "Thai Baht", symbol: "฿", locale: "th-TH" },
  { code: "TRY", name: "Turkish Lira", symbol: "₺", locale: "tr-TR" },
  { code: "UAH", name: "Ukrainian Hryvnia", symbol: "₴", locale: "uk-UA" },
  { code: "USD", name: "US Dollar", symbol: "$", locale: "en-US" },
  { code: "VND", name: "Vietnamese Dong", symbol: "₫", locale: "vi-VN" },
  { code: "ZAR", name: "South African Rand", symbol: "R", locale: "en-ZA" },
];

