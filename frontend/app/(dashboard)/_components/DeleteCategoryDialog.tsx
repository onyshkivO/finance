"use client";

import { Dialog, DialogContent, DialogDescription, DialogFooter, DialogHeader, DialogTitle } from "@/components/ui/dialog";
import { DeleteCategory } from "@/data/services/category-service";
import { Category } from "@/lib/types";
import { useMutation, useQueryClient } from "@tanstack/react-query";
import React from "react";
import { toast } from "sonner";
import { Button } from "@/components/ui/button";
import { Loader2 } from "lucide-react";
import { DialogClose } from "@/components/ui/dialog";

interface Props {
    category: Category;
    open: boolean;
    setOpen: (open: boolean) => void;
    successCallback: () => void;
}

function DeleteCategoryDialog({ category, open, setOpen, successCallback }: Props) {
    const queryClient = useQueryClient();

    const { mutate, isPending } = useMutation({
        mutationFn: DeleteCategory,
        onSuccess: async () => {
            toast.success(`Category ${category.name} deleted successfully`, {
                id: "delete-category",
            });
            await queryClient.invalidateQueries({
                queryKey: ["categories"],
            });
            successCallback();
            setOpen(false);
        },
        onError: (error) => {
            toast.error("Failed to delete cashbox. " + error.message, {
                id: "delete-category",
            });
        },
    });

    const onSubmit = () => {
        toast.loading("Deleting category...", { id: "delete-category" });
        mutate(category.id);
    };

    return (
        <Dialog open={open} onOpenChange={setOpen}>
            <DialogContent>
                <DialogHeader>
                    <DialogTitle>Are you sure?</DialogTitle>
                    <DialogDescription>
                        This action cannot be undone. This will permanently delete the{" "}
                        <span className="font-medium">{category.name}</span> category.
                    </DialogDescription>
                </DialogHeader>
                <DialogFooter>
                    <DialogClose asChild>
                        <Button
                            className="cursor-pointer"
                            type="button"
                            variant={"secondary"}
                        >
                            Cancel
                        </Button>
                    </DialogClose>
                    <Button
                        onClick={onSubmit}
                        disabled={isPending}
                        className="cursor-pointer"
                    >
                        {isPending ? (
                            <>
                                Deleting <Loader2 className="animate-spin h-4 w-4 ml-2" />
                            </>
                        ) : (
                            "Delete"
                        )}
                    </Button>
                </DialogFooter>
            </DialogContent>
        </Dialog>
    );
}

export default DeleteCategoryDialog;