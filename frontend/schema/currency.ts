
import { CURRENCIES } from "@/lib/types";
import { z } from "zod";

export const UpdateUserCurrencySchema = z.object({
    currencyCode: z.custom((value) => {
        const found = CURRENCIES.some((c) => c.code === value);
        if (!found) {
            throw new Error(`invalid currency: ${value}`);
        }
        return value;
    })
});
