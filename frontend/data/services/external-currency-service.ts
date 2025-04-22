export async function fetchCurrencyRate(fromCurrency: string, toCurrency: string) {
    const response = await fetch(
        `https://cdn.jsdelivr.net/npm/@fawazahmed0/currency-api@latest/v1/currencies/${fromCurrency}.json`
    );
    const data = await response.json();
    return data[fromCurrency]?.[toCurrency];
};