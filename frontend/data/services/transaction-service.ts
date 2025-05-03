import clientApi from "@/data/services/client-api";
import { normalizeTransaction } from "@/lib/helpers";
import { Transaction, TransactionDtoBackend, UserData } from "@/lib/types";
import { CreateTransactionSchema, CreateTransactionSchemaType } from "@/schema/transaction";
import { format } from "date-fns";
import Cookies from "js-cookie";

export async function CreateTransaction(form: CreateTransactionSchemaType) {
    console.log("form", form);
    const parsedBody = CreateTransactionSchema.safeParse(form);
    if (!parsedBody.success) {
        throw new Error(parsedBody.error.message);
    }

    const { amount, category, date, description, type, currency, cashbox, coefficient } = parsedBody.data;
    
    try {
        const response = await clientApi.post("/transaction", {
            category: {
                id: category
            },
            type: type.toUpperCase(),
            amount: amount,
            description: description || null,
            currency: currency,
            transactionDate: format(date, 'dd-MM-yyyy'),
            coefficient: coefficient,
            cashbox:{ 
                id: cashbox
            }
        });

        return response.data;
    } catch (error) {
        console.error("Error creating екфтіфсешщт:", error);
        throw new Error("Internal server error");
    }
}

export async function fetchUserTransactionsByDateRange(from: Date, to: Date): Promise<Transaction[]> {
    const formattedFrom = format(from, "yyyy-MM-dd");
    const formattedTo = format(to, "yyyy-MM-dd");
  
    try {
      const response = await clientApi.get<TransactionDtoBackend[]>("/transaction", {
        params: {
          from: formattedFrom,
          to: formattedTo
        }
      });

      const userDataString = Cookies.get("userData");
      if (!userDataString) {
        throw new Error("User data not found");
      }

      const userData: UserData = JSON.parse(userDataString);
      const userCurrency = userData.currency.code;

      const normalized = response.data.map(tx => normalizeTransaction(tx, userCurrency));
      
      return normalized;
    } catch (error) {
      console.error("Error fetching transactions:", error);
      throw new Error("Failed to fetch transactions");
    }
  }

  export async function DeleteTransaction(id: string) {

    try {
        const response = await clientApi.delete(`/transaction/${id}`, {
        });

        return response.data;
    } catch (error) {
        console.error("Error deleting transaction:", error);
        throw new Error("Internal server error");
    }
}

export async function UpdateTransaction(id: string, form: CreateTransactionSchemaType) {
    const parsedBody = CreateTransactionSchema.safeParse(form);
    if (!parsedBody.success) {
        throw new Error(parsedBody.error.message);
    }
    console.log("form", form);
    console.log("parsedBody", parsedBody);
    const { amount, category, date, description, type, currency, cashbox, coefficient } = parsedBody.data;
    console.log("cashbox", cashbox);
    try {
        const response = await clientApi.put(`/transaction/${id}`, {
            category: {
                id: category
            },
            type: type.toUpperCase(),
            amount: amount,
            description: description || null,
            currency: currency,
            coefficient: coefficient,
            cashbox:{ 
                id: cashbox
            },
            transactionDate: format(date, 'dd-MM-yyyy')
        });

        return response.data;
    } catch (error) {
        console.error("Error updating transaction:", error);
        throw new Error("Internal server error");
    }
}