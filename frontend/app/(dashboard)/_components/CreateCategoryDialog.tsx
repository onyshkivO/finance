"use client";
import { Button } from "@/components/ui/button";
import { Dialog, DialogClose, DialogContent, DialogDescription, DialogFooter, DialogHeader, DialogTitle, DialogTrigger } from "@/components/ui/dialog";
import { Form, FormControl, FormDescription, FormField, FormItem, FormLabel } from "@/components/ui/form";
import { Input } from "@/components/ui/input";
import { Popover, PopoverContent, PopoverTrigger } from "@/components/ui/popover";
import { TransactionType, Category } from "@/lib/types";
import { cn } from "@/lib/utils";
import {
    CreateCategorySchema,
    CreateCategorySchemaType,
} from "@/schema/categories";
import { zodResolver } from "@hookform/resolvers/zod";
import { CircleOff, Loader2, PlusSquare, X } from "lucide-react";
import React, { ReactNode, useCallback, useState } from "react";
import { useForm } from "react-hook-form";
import Picker from "@emoji-mart/react";
import data from "@emoji-mart/data";
import { useMutation, useQueryClient } from "@tanstack/react-query";
import { toast } from "sonner";
import { CreateCategory } from "@/data/services/category-service";
import { useTheme } from "next-themes";

interface Props {
    type: TransactionType;
    successCallback: (category: Category) => void;
    trigger?: ReactNode;
}

function CreateCategoryDialog({ type, successCallback, trigger }: Props) {
    const [open, setOpen] = useState(false);
    const [mccCodes, setMccCodes] = useState<number[]>([]);
    const [mccInput, setMccInput] = useState("");
    const form = useForm<CreateCategorySchemaType>({
        resolver: zodResolver(CreateCategorySchema),
        defaultValues: {
            type: type,
        },
    });

    const queryClient = useQueryClient();
    const theme = useTheme();

    const { mutate, isPending } = useMutation({
        mutationFn: CreateCategory,
        onSuccess: async (data: Category) => {
            form.reset({
                type,
                name: "",
                icon: "",
                mccCodes: []
            });
            toast.success(`Category ${data.name} created successfully`, {
                id: "create-category",
            });
            successCallback(data)
            await queryClient.invalidateQueries({
                queryKey: ["categories"],
            });
            setOpen((prev) => !prev);
        },
        onError: (error) => {
            toast.error("Failed to create category. " + error.message, {
                id: "create-category",
            });
        },
    });

    const onSubmit = useCallback(
        (values: CreateCategorySchemaType) => {
            toast.loading("Creating category...", { id: "create-category" });
            mutate({ ...values, mccCodes });
        },
        [mutate, mccCodes]
    );

    const [error, setError] = useState<string | null>(null);

    const handleAddMccCode = () => {
        setError(null); // Reset error

        if (!mccInput.trim()) {
            setError("Please enter at least one MCC code");
            return;
        }

        const newCodes: number[] = [];
        const invalidEntries: string[] = [];

        mccInput.split(",").forEach((entry) => {
            const trimmed = entry.trim();
            if (!trimmed) return; // Skip empty entries

            // Validate: Must be positive integer
            const num = parseInt(trimmed, 10);
            if (isNaN(num) || num <= 0 || !/^\d+$/.test(trimmed)) {
                invalidEntries.push(trimmed);
                return;
            }

            // Check for duplicates (both existing and in current batch)
            if (!mccCodes.includes(num)) {
                newCodes.push(num);
            }
        });

        // Show errors if any invalid entries
        if (invalidEntries.length > 0) {
            setError(`Invalid MCC codes: ${invalidEntries.join(", ")}. Must be positive integers.`);
        }

        // Add valid codes
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
            <DialogTrigger asChild>
                {trigger ? trigger : <Button
                    variant="ghost"
                    className="flex border-separate items-center justify-start rounded-none border-b px-3 py-3 text-muted-foreground"
                >
                    <PlusSquare className="mr-2 h-4 w-4" />
                    Create new
                </Button>}
            </DialogTrigger>
            <DialogContent>
                <DialogHeader>
                    <DialogTitle>
                        Create
                        <span
                            className={cn(
                                "m-1",
                                type === "income" ? "text-emerald-500" : "text-red-500"
                            )}
                        >
                            {type}
                        </span>
                        category
                    </DialogTitle>
                    <DialogDescription>
                        Categories are used to group transactions
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
                                        <Input defaultValue={""} {...field} />
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

                            {/* Error message */}
                            {error && (
                                <p className="mt-1 text-sm text-destructive">{error}</p>
                            )}

                            {/* MCC Tags (with scroll) */}
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
                                Creating <Loader2 className="animate-spin h-4 w-4 ml-2" />
                            </>
                        ) : (
                            "Create"
                        )}
                    </Button>
                </DialogFooter>
            </DialogContent>
        </Dialog>
    );
}

export default CreateCategoryDialog;