import { BackendCashboxStats, BackendCategoryStats, CashboxStatsType, Category, CategoryDtoBackend, CategoryStatsType, CURRENCIES, Transaction, TransactionDtoBackend } from "./types";

export function GetFormatterForCurrency(currency: string) {

    const locale = CURRENCIES.find((c) => c.code === currency)?.locale;

    return new Intl.NumberFormat(locale, {
        style: "currency",
        currency,
    });
}

export function normalizeCategory(category: CategoryDtoBackend): Category {
    return {
      ...category,
      type: category.type.toLowerCase(),
      mccCodes: new Set(category.mccCodes || []),
      icon: category.icon || ""
    };
  }

export function normalizeCategoryStats(data: BackendCategoryStats[]): CategoryStatsType[] {
    return data.map(item => ({
        ...item,
        type: item.type.toLowerCase() as 'income' | 'expense'
    }));
  }

export function normalizeCashboxStats(data: BackendCashboxStats[]): CashboxStatsType[] {
    return data.map(item => ({
        ...item,
        type: item.type.toLowerCase() as 'income' | 'expense'
    }));
}

export function normalizeTransaction(tx: TransactionDtoBackend,currency : string): Transaction {
    const normalizedCurrency = CURRENCIES.find((c) => c.code === tx.currency);
    if (!normalizedCurrency) {
      throw new Error(`Unsupported currency code: ${tx.currency}`);
    }
    const formatter = GetFormatterForCurrency(currency);
    return {
      ...tx,
      type: tx.type.toLowerCase() as "income" | "expense", // assuming these two only
      currency: normalizedCurrency,
      category: normalizeCategory(tx.category),
      formatedAmount: formatter.format(tx.amount)
    };
  }