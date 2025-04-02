"use client";


import { AlertDialog, AlertDialogAction, AlertDialogCancel, AlertDialogContent, AlertDialogDescription, AlertDialogFooter, AlertDialogHeader, AlertDialogTitle, AlertDialogTrigger } from "@/components/ui/alert-dialog";
import { DeleteCategory } from "@/data/services/category-service";
import { Category } from "@/lib/types";
import { useMutation } from "@tanstack/react-query";
import React, { ReactNode } from "react";
import { toast } from "sonner";

interface Props {
    trigger: ReactNode;
    category: Category;
}

function DeleteCategoryDialog({ category, trigger }: Props) {
    const categoryIdentifier = `${category.name}-${category.type}`;
    const deleteMutation = useMutation({
        mutationFn: DeleteCategory,
        onSuccess: () => {
            toast.success("Category deleted successfully", {
                id: categoryIdentifier,
            });
        },
    });

    return (
        <AlertDialog>
            <AlertDialogTrigger asChild>
                {trigger}
            </AlertDialogTrigger>
            <AlertDialogContent>
                <AlertDialogHeader>
                    <AlertDialogTitle>Are you absolutely sure?</AlertDialogTitle>
                    <AlertDialogDescription>
                        This action cannot be undone. This will permanently delete your
                        category
                    </AlertDialogDescription>
                </AlertDialogHeader>
                <AlertDialogFooter>
                    <AlertDialogCancel>Cancel</AlertDialogCancel>
                    <AlertDialogAction
                        onClick={() => {
                            toast.loading("Deleting category...", {
                                id: categoryIdentifier,
                            });
                            deleteMutation.mutate(
                                category.id
                            );
                        }}
                    >
                        Continue
                    </AlertDialogAction>
                </AlertDialogFooter>
            </AlertDialogContent>
        </AlertDialog>
    );
}

export default DeleteCategoryDialog;