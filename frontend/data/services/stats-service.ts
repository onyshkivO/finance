
import clientApi from "@/data/services/client-api";
import { normalizeCashboxStats, normalizeCategoryStats } from "@/lib/helpers";
import { BackendCashboxStats, BackendCategoryStats, BalanceStats, CashboxStatsType, CategoryStatsType, HistoryStatsType, Period, Timeframe } from "@/lib/types";

export async function getBalanceStats(
    from: Date,
    to: Date,
    config?: {
        onSuccess?: (data: BalanceStats) => void;
        onError?: (error: unknown) => void;
    }
): Promise<BalanceStats> {
    try {
        const response = await clientApi.get<BalanceStats>(`/stats/balance`, {
            params: { 
                from: from.toISOString().split("T")[0], 
                to: to.toISOString().split("T")[0]
            }
        });

        config?.onSuccess?.(response.data);
        return response.data;
    } catch (error) {
        console.error("Failed to fetch balance stats:", error);
        config?.onError?.(error);
        throw error;
    }
}

export async function getCategoryStats(
    from: Date,
    to: Date,
    config?: {
        onSuccess?: (data: CategoryStatsType[]) => void;
        onError?: (error: unknown) => void;
    }
): Promise<CategoryStatsType[]> {
    try {
        const response = await clientApi.get<BackendCategoryStats[]>(`/stats/category`, {
            params: { 
                from: from.toISOString().split("T")[0], 
                to: to.toISOString().split("T")[0]
            }
        });

        const normalizedData = normalizeCategoryStats(response.data);

        config?.onSuccess?.(normalizedData);
        return normalizedData;
    } catch (error) {
        console.error("Failed to fetch balance stats:", error);
        config?.onError?.(error);
        throw error;
    }
}


export async function getCashboxStats(
    from: Date,
    to: Date,
    config?: {
        onSuccess?: (data: CashboxStatsType[]) => void;
        onError?: (error: unknown) => void;
    }
): Promise<CashboxStatsType[]> {
    try {
        const response = await clientApi.get<BackendCashboxStats[]>(`/stats/cashbox`, {
            params: { 
                from: from.toISOString().split("T")[0], 
                to: to.toISOString().split("T")[0]
            }
        });

        const normalizedData = normalizeCashboxStats(response.data);

        config?.onSuccess?.(normalizedData);
        return normalizedData;
    } catch (error) {
        console.error("Failed to fetch balance stats:", error);
        config?.onError?.(error);
        throw error;
    }
}

export async function getHistoryPeriods(
    config?: {
        onSuccess?: (data: number[]) => void;
        onError?: (error: unknown) => void;
    }
): Promise<number[]> {
    try {
        const response = await clientApi.get<number[]>(`/stats/history/periods`);

        config?.onSuccess?.(response.data);
        return response.data;
    } catch (error) {
        console.error("Failed to fetch history periods:", error);
        config?.onError?.(error);
        throw error;
    }
}

export async function getHistoryData(
    timeframe: Timeframe,
    period: Period,
    config?: {
        onSuccess?: (data: HistoryStatsType[]) => void;
        onError?: (error: unknown) => void;
    }
): Promise<HistoryStatsType[]> {
    try {
        const response = await clientApi.get<HistoryStatsType[]>(`/stats/${timeframe}`, {
            params: { 
                year: period.year, 
                month: period.month
            }
        });

        config?.onSuccess?.(response.data);
        return response.data;
    } catch (error) {
        console.error("Failed to fetch history stats:", error);
        config?.onError?.(error);
        throw error;
    }
}