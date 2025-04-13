import { z } from "zod";

export const CreateCashboxSchema = z.object({
    name: z.string().min(3).max(20),
    currency: z.string(),
    // balance: z.number().min(0, "Balance must be non-negative"),
});

export type CreateCashboxSchemaType = z.infer<
    typeof CreateCashboxSchema
>;