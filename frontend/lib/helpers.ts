import { BackendCategoryStats, CategoryStatsType, CURRENCIES } from "./types";

export function GetFormatterForCurrency(currency: string) {

    const locale = CURRENCIES.find((c) => c.code === currency)?.locale;

    return new Intl.NumberFormat(locale, {
        style: "currency",
        currency,
    });
}

export function normalizeCategoryStats(data: BackendCategoryStats[]): CategoryStatsType[] {
    return data.map(item => ({
        ...item,
        type: item.type.toLowerCase() as 'income' | 'expense'
    }));
  }