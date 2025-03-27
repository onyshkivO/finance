import clientApi from "@/data/services/client-api";
import { CreateTransactionSchema, CreateTransactionSchemaType } from "@/schema/transaction";
import { format } from "date-fns";

export async function CreateTransaction(form: CreateTransactionSchemaType) {
    const parsedBody = CreateTransactionSchema.safeParse(form);
    if (!parsedBody.success) {
        throw new Error(parsedBody.error.message);
    }

    const { amount, category, date, description, type, currency } = parsedBody.data;

    try {
        const response = await clientApi.post("/transaction", {
            category: {
                id: category
            },
            type: type.toUpperCase(),
            amount: amount,
            description: description || null,
            currency: currency,
            transactionDate: format(date, 'dd-MM-yyyy') // Format date to match DTO pattern
        });

        return response.data;
    } catch (error) {
        console.error("Error creating екфтіфсешщт:", error);
        throw new Error("Internal server error");
    }
}
