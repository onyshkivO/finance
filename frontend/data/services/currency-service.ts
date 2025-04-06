import { UpdateUserCurrencySchema } from "@/schema/currency";
import clientApi from "./client-api";

export async function UpdateUserCurrency(currencyCode: string) {
    console.log(currencyCode);
    
    const parsedBody = UpdateUserCurrencySchema.safeParse({ currencyCode });
    if  (!parsedBody.success){
        throw parsedBody.error;
    }
    try {
        const response = await clientApi.put(`/user/changeCurrency/${currencyCode}`);

        return currencyCode;
    } catch (error) {
        console.error("Error creating category:", error);
        throw new Error("Internal server error");
    }
}