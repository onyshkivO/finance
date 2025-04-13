import clientApi from "@/data/services/client-api";
import { Cashbox } from "@/lib/types";
import { CreateCashboxSchema, CreateCashboxSchemaType } from "@/schema/cashbox";

import { format } from "date-fns";

export async function CreateCashbox(form: CreateCashboxSchemaType) {
    const parsedBody = CreateCashboxSchema.safeParse(form);
    if (!parsedBody.success) {
        throw new Error(parsedBody.error.message);
    }

    const { name, currency } = parsedBody.data;

    try {
        const response = await clientApi.post("/cashbox", {
            name: name,
            currency: currency
        });

        return response.data;
    } catch (error) {
        console.error("Error creating cashbox:", error);
        throw new Error("Internal server error");
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

//   export async function DeleteTransaction(id: string) {

//     try {
//         const response = await clientApi.delete(`/transaction/${id}`, {
//         });

//         return response.data;
//     } catch (error) {
//         console.error("Error deleting transaction:", error);
//         throw new Error("Internal server error");
//     }
// }

// export async function UpdateTransaction(id: string, form: CreateTransactionSchemaType) {
//     const parsedBody = CreateTransactionSchema.safeParse(form);
//     if (!parsedBody.success) {
//         throw new Error(parsedBody.error.message);
//     }

//     const { amount, category, date, description, type, currency } = parsedBody.data;

//     try {
//         const response = await clientApi.put(`/transaction/${id}`, {
//             category: {
//                 id: category
//             },
//             type: type.toUpperCase(),
//             amount: amount,
//             description: description || null,
//             currency: currency,
//             transactionDate: format(date, 'dd-MM-yyyy')
//         });

//         return response.data;
//     } catch (error) {
//         console.error("Error updating transaction:", error);
//         throw new Error("Internal server error");
//     }
// }