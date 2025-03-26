import { z } from "zod";

export const CreateCategorySchema = z.object({
  name: z.string().min(3).max(20),
  icon: z.string().max(20).optional(),
  type: z.enum(['income', 'expense']),
  mccCodes: z
    .array(z.number().int().positive()) // Ensure it's an array of positive integers
    .optional() // Allows it to be omitted
    .default([]), // Defaults to an empty array if not provided
});

export type CreateCategorySchemaType = z.infer<typeof CreateCategorySchema>;