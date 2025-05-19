"use client";
import { Button } from "@/components/ui/button";
import { Dialog, DialogClose, DialogContent, DialogDescription, DialogFooter, DialogHeader, DialogTitle } from "@/components/ui/dialog";
import { Form, FormControl, FormDescription, FormField, FormItem, FormLabel } from "@/components/ui/form";
import { Input } from "@/components/ui/input";
import { Popover, PopoverContent, PopoverTrigger } from "@/components/ui/popover";
import { Category, TransactionType } from "@/lib/types";
import { cn } from "@/lib/utils";
import {
    CreateCategorySchema,
    CreateCategorySchemaType,
} from "@/schema/categories";
import { zodResolver } from "@hookform/resolvers/zod";
import { CircleOff, Loader2, X } from "lucide-react";
import React, { useCallback, useState } from "react";
import { useForm } from "react-hook-form";
import Picker from "@emoji-mart/react";
import data from "@emoji-mart/data";
import { useMutation, useQueryClient } from "@tanstack/react-query";
import { toast } from "sonner";
import { UpdateCategory } from "@/data/services/category-service";
import { useTheme } from "next-themes";

interface Props {
    category: Category;
    open: boolean;
    setOpen: (open: boolean) => void;
}
function UpdateCategoryDialog({ category, open, setOpen }: Props) {
    const [mccCodes, setMccCodes] = useState<number[]>(Array.from(category.mccCodes || []));
    const [mccInput, setMccInput] = useState("");
    const form = useForm<CreateCategorySchemaType>({
        resolver: zodResolver(CreateCategorySchema),
        defaultValues: {
          type: category.type.toLowerCase() as TransactionType,
          name: category.name,
          icon: category.icon ?? null,
          mccCodes: Array.from(category.mccCodes || []),
        },
      });

    const queryClient = useQueryClient();
    const theme = useTheme();

    const { mutate, isPending } = useMutation({
        mutationFn: (values: CreateCategorySchemaType) => {
            console.log('Updating category with values:', values);
            console.log('Category ID:', category.id);
            return UpdateCategory(category.id, values);
        },
        onSuccess: async () => {
            form.reset();
            toast.success(`Category ${category.name} updated successfully`, {
                id: "update-category",
            });
            await queryClient.invalidateQueries({
                queryKey: ["categories"],
            });
            setOpen(false);
        },
        onError: (error) => {
            toast.error("Failed to update cashbox. " + error.message, {
                id: "update-category",
            });
        },
    });

    const onSubmit = useCallback(
        (values: CreateCategorySchemaType) => {
            toast.loading("Updating category...", { id: "update-category" });
            console.log(values);
            console.log(mccCodes);
            console.log("values.icon"+values.icon);
            mutate({ ...values, mccCodes });
        },
        [mutate, mccCodes]
    );

    const [error, setError] = useState<string | null>(null);

    const handleAddMccCode = () => {
        setError(null);

        if (!mccInput.trim()) {
            setError("Please enter at least one MCC code");
            return;
        }

        const newCodes: number[] = [];
        const invalidEntries: string[] = [];

        mccInput.split(",").forEach((entry) => {
            const trimmed = entry.trim();
            if (!trimmed) return;

            const num = parseInt(trimmed, 10);
            if (isNaN(num) || num <= 0 || !/^\d+$/.test(trimmed)) {
                invalidEntries.push(trimmed);
                return;
            }

            if (!mccCodes.includes(num)) {
                newCodes.push(num);
            }
        });

        if (invalidEntries.length > 0) {
            setError(`Invalid MCC codes: ${invalidEntries.join(", ")}. Must be positive integers.`);
        }

        if (newCodes.length > 0) {
            setMccCodes([...mccCodes, ...newCodes]);
            setMccInput("");
        }
    };

    const handleRemoveMccCode = (code: number) => {
        setMccCodes(mccCodes.filter((mcc) => mcc !== code));
    };

    return (
        <Dialog open={open} onOpenChange={setOpen}>
            <DialogContent>
                <DialogHeader>
                    <DialogTitle>
                        Update
                        <span
                            className={cn(
                                "m-1",
                                category.type === "INCOME" ? "text-emerald-500" : "text-red-500"
                            )}
                        >
                            {category.type.toLowerCase()}
                        </span>
                        category
                    </DialogTitle>
                    <DialogDescription>
                        Update your category details
                    </DialogDescription>
                </DialogHeader>
                <Form {...form}>
                    <form onSubmit={form.handleSubmit(onSubmit)} className="space-y-8">
                        <FormField
                            control={form.control}
                            name="name"
                            render={({ field }) => (
                                <FormItem>
                                    <FormLabel>Name</FormLabel>
                                    <FormControl>
                                        <Input {...field} />
                                    </FormControl>
                                    <FormDescription>
                                        Category name (required)
                                    </FormDescription>
                                </FormItem>
                            )}
                        />
                        <FormField
                            control={form.control}
                            name="icon"
                            render={({ field }) => (
                                <FormItem>
                                    <FormLabel>Icon</FormLabel>
                                    <FormControl>
                                        <Popover>
                                            <PopoverTrigger asChild>
                                                <Button
                                                    variant={"outline"}
                                                    className="h-[100px] w-full"
                                                >
                                                    {form.watch("icon") ? (
                                                        <div className="flex flex-col items-center gap-2">
                                                            <span className="text-5xl" role="img">
                                                                {field.value}
                                                            </span>
                                                            <p className="text-xs text-muted-foreground">
                                                                Click to change
                                                            </p>
                                                        </div>
                                                    ) : (
                                                        <div className="flex flex-col items-center gap-2">
                                                            <CircleOff className="text-5xl h-[48px] w-[48px]" />
                                                            <p className="text-xs text-muted-foreground">
                                                                Click to select
                                                            </p>
                                                        </div>
                                                    )}
                                                </Button>
                                            </PopoverTrigger>
                                            <PopoverContent className="w-full">
                                                <Picker
                                                    data={data}
                                                    theme={theme.resolvedTheme}
                                                    onEmojiSelect={(emoji: { native: string }) => {
                                                        field.onChange(emoji.native);
                                                    }}
                                                />
                                            </PopoverContent>
                                        </Popover>
                                    </FormControl>
                                    <FormDescription>
                                        Category icon (optional)
                                    </FormDescription>
                                </FormItem>
                            )}
                        />
                        <div>
                            <FormLabel>MCC Codes (optional)</FormLabel>
                            <div className="flex items-center space-x-2">
                                <Input
                                    type="text"
                                    value={mccInput}
                                    onChange={(e) => setMccInput(e.target.value)}
                                    onKeyDown={(e) => {
                                        if (e.key === "Enter") {
                                            e.preventDefault();
                                            handleAddMccCode();
                                        }
                                    }}
                                    placeholder="Enter MCC codes (e.g., 1234, 5678)"
                                />
                                <Button type="button" onClick={handleAddMccCode}>
                                    Add
                                </Button>
                            </div>

                            {error && (
                                <p className="mt-1 text-sm text-destructive">{error}</p>
                            )}

                            {mccCodes.length > 0 && (
                                <div className="mt-2 max-h-[200px] overflow-y-auto">
                                    <div className="flex flex-wrap gap-2">
                                        {mccCodes.map((code) => (
                                            <div
                                                key={code}
                                                className="flex items-center gap-2 bg-secondary px-3 py-1 rounded-md text-secondary-foreground"
                                            >
                                                {code}
                                                <button
                                                    onClick={() => handleRemoveMccCode(code)}
                                                    className="text-red-500 hover:text-red-600 dark:hover:text-red-400"
                                                >
                                                    <X size={14} />
                                                </button>
                                            </div>
                                        ))}
                                    </div>
                                </div>
                            )}
                        </div>
                    </form>
                </Form>
                <DialogFooter>
                    <DialogClose asChild>
                        <Button
                            className="cursor-pointer"
                            type="button"
                            variant={"secondary"}
                            onClick={() => {
                                form.reset();
                            }}
                        >
                            Cancel
                        </Button>
                    </DialogClose>
                    <Button
                        onClick={form.handleSubmit(onSubmit)}
                        disabled={isPending}
                        className="cursor-pointer"
                    >
                        {isPending ? (
                            <>
                                Updating <Loader2 className="animate-spin h-4 w-4 ml-2" />
                            </>
                        ) : (
                            "Update"
                        )}
                    </Button>
                </DialogFooter>
            </DialogContent>
        </Dialog>
    );
}

export default UpdateCategoryDialog; 