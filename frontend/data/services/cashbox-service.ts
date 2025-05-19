import clientApi from "@/data/services/client-api";
import { Cashbox } from "@/lib/types";
import { CreateCashboxSchema, CreateCashboxSchemaType, CreateTransferSchema, CreateTransferSchemaType } from "@/schema/cashbox";

import { format } from "date-fns";

function extractErrorMessage(error: any): string {
  if (error?.response?.data?.message) {
    return error.response.data.message;
  }
  return "Internal server error";
}

export async function CreateCashbox(form: CreateCashboxSchemaType) {
    const parsedBody = CreateCashboxSchema.safeParse(form);
    if (!parsedBody.success) {
        throw new Error(parsedBody.error.message);
    }

    const { name, currency, balance } = parsedBody.data;

    try {
        const response = await clientApi.post("/cashbox", {
            name: name,
            currency: currency,
            balance: balance
        });

        return response.data;
    } catch (error) {
        console.error("Error creating cashbox:", error);
        throw new Error(extractErrorMessage(error));
    }
}

export async function fetchUserCashBoxes(): Promise<Cashbox[]> {
  
    try {
      const response = await clientApi.get<Cashbox[]>("/cashbox");

      return response.data;
    } catch (error) {
      console.error("Error fetching cashboxes:", error);
      throw new Error("Failed to fetch cashboxes");
    }
  }


export async function CreateTransfer(form: CreateTransferSchemaType) {
    console.log("form", form);
    const parsedBody = CreateTransferSchema.safeParse(form);
    if (!parsedBody.success) {
        throw new Error(parsedBody.error.message);
    }

    const { amount, date, description, cashboxFrom, cashboxTo, coefficient} = parsedBody.data;
    
    try {
        const response = await clientApi.put("/cashbox/transfer", {
            amount: amount,
            description: description || null,
            date: format(date, 'dd-MM-yyyy'),
            cashboxFromId: cashboxFrom,
            cashboxToId: cashboxTo,
            currencyCoefficient: coefficient
        });

        return response.data;
    } catch (error) {
        console.error("Error creating cashbox:", error);
        throw new Error(extractErrorMessage(error));
    }
}

export async function UpdateCashbox(form: CreateCashboxSchemaType, id: string) {
    const parsedBody = CreateCashboxSchema.safeParse(form);
    if (!parsedBody.success) {
        throw new Error(parsedBody.error.message);
    }

    const { name, currency, balance } = parsedBody.data;

    try {
        const response = await clientApi.put(`/cashbox/${id}`, {
            name: name,
            currency: currency,
            balance: balance
        });

        return response.data;
    } catch (error) {
        console.error("Error creating cashbox:", error);
        throw new Error(extractErrorMessage(error));
    }
}