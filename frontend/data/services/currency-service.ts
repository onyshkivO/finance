import { UpdateUserCurrencySchema } from "@/schema/currency";
import clientApi from "./client-api";

function extractErrorMessage(error: any): string {
    if (error?.response?.data?.message) {
      return error.response.data.message;
    }
    return "Internal server error";
  }

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
        throw new Error(extractErrorMessage(error));
    }
}