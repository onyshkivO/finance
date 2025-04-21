import { z } from "zod";

export const CreateCashboxSchema = z.object({
    name: z.string().min(3).max(20),
    currency: z.string(),
    balance: z.number().min(0, "Balance must be non-negative"),
});

export type CreateCashboxSchemaType = z.infer<
    typeof CreateCashboxSchema
>;

export const CreateTransferSchema = z.object({
    amount: z.coerce.number().positive().multipleOf(0.01),
    coefficient: z.number().positive(),
    description: z.string().optional(),
    date: z.coerce.date(),
    cashboxFrom: z.string(),
    cashboxTo: z.string(),
}).refine((data) => data.cashboxFrom !== data.cashboxTo, {
    message: "Cannot transfer to the same cashbox",
    path: ["cashboxTo"], // Show the error message near the target cashbox field
  });

export type CreateTransferSchemaType = z.infer<
    typeof CreateTransferSchema
>;