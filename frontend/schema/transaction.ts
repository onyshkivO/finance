import { z } from "zod";

export const CreateTransactionSchema = z.object({
    amount: z.coerce.number().positive().multipleOf(0.01),
    description: z.string().optional(),
    date: z.coerce.date(),
    category: z.string(),
    cashbox: z.string(),
    currency: z.string(),
    coefficient: z.coerce.number().positive().multipleOf(0.01),
    type: z.union([z.literal("income"), z.literal("expense")]),
});

export type CreateTransactionSchemaType = z.infer<
    typeof CreateTransactionSchema
>;