import { CreateCategorySchema, CreateCategorySchemaType } from "@/schema/categories";
import clientApi from "@/data/services/client-api";
import {Category} from "@/lib/types";
// category-service.ts
export async function getUserCategoriesByType(
    type: string,
    config?: {
        onSuccess?: (data: Category[]) => void;
        onError?: (error: unknown) => void;
    }
): Promise<Category[]> {
    try {
        const response = await clientApi.get<Category[]>(`/category/type/${type.toUpperCase()}`);
        config?.onSuccess?.(response.data);
        return response.data;
    } catch (error) {
        console.error("Failed to fetch categories:", error);
        config?.onError?.(error);
        throw error;
    }
 }

export async function CreateCategory(form: CreateCategorySchemaType) {
    const parsedBody = CreateCategorySchema.safeParse(form);
    if (!parsedBody.success) {
        throw new Error("bad request");
    }

    const { name, icon, type, mccCodes } = parsedBody.data;

    try {
        const response = await clientApi.post("/category", {
            name,
            icon,
            type: type.toUpperCase(),
            mccCodes
        });

        return response.data;
    } catch (error) {
        console.error("Error creating category:", error);
        throw new Error("Internal server error");
    }
}


export async function DeleteCategory(id: string) {

    try {
        const response = await clientApi.delete(`/category/${id}`, {
        });

        return response.data;
    } catch (error) {
        console.error("Error creating category:", error);
        throw new Error("Internal server error");
    }
}
