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
// export async function getUserCategoriesByType(type: string) {
//     try {
//         const response = await clientApi.get<Category[]>(`/category/type/${type.toUpperCase()}`);
//         console.log("Fetched categories:", response.data); // Log the categories
//
//         const filteredData = response.data.map((category: any) => ({
//             id: category.id,
//             name: category.name,
//             type: category.type,
//             icon: category.icon,
//             mccCodes: category.mccCodes
//         }));
//
//         return {
//             status: response.status,
//             data: filteredData,
//             error: null,
//         };
//     } catch (error: any) {
//         console.error("Error retrieving user categories:", error);
//         return {
//             status: error.response?.status || 500,
//             data: null,
//             error: error.response?.data || { message: "Something went wrong. Please try again later." },
//         };
//     }
// }

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
