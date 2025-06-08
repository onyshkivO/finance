
import clientApi from "@/data/services/client-api";
import { MonobankAccount, MononbakAuth } from "@/lib/types";

export async function getAccessUrl(
    config?: {
        onSuccess?: (data: MononbakAuth) => void;
        onError?: (error: unknown) => void;
    }
): Promise<MononbakAuth> {
    try {
        const response = await clientApi.post<MononbakAuth>(`/mono/request`, {
        });

        config?.onSuccess?.(response.data);
        return response.data;
    } catch (error) {
        console.error("Failed to fetch mono/request:", error);
        config?.onError?.(error);
        throw error;
    }
}

export async function getMonobankCards(
    config?: {
        onSuccess?: (data: MonobankAccount[]) => void;
        onError?: (error: unknown) => void;
    }
): Promise<MonobankAccount[]> {
    try {
        const response = await clientApi.get<MonobankAccount[]>(`/mono/account`, {
        });

        config?.onSuccess?.(response.data);
        return response.data;
    } catch (error) {
        console.error("Failed to fetch >(`/mono/account:", error);
        config?.onError?.(error);
        throw error;
    }
}

export async function activateMonobankCard(cardId: string) {

    try {
         const response = await clientApi.put(`/mono/account/monitor/${cardId}`)
         console.log(response);
        } catch (error) {
        console.error("Error creating category:", error);
        throw new Error("Internal server error");
    }
}

export async function deactivateMonobankCard(cardId: string) {

    try {
         const response = await clientApi.put(`/mono/account/unmonitor/${cardId}`)
         console.log(response);
        } catch (error) {
        console.error("Error creating category:", error);
        throw new Error("Internal server error");
    }
}