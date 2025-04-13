"use client";

import { Dialog, DialogTrigger, DialogContent, DialogHeader, DialogTitle, DialogFooter, DialogClose } from "@/components/ui/dialog";
import { CURRENCIES, TransactionType, UserData } from "@/lib/types";
import { cn } from "@/lib/utils";
import { CreateTransactionSchema, CreateTransactionSchemaType } from "@/schema/transaction";
import { ReactNode, useCallback, useState } from "react";
import { zodResolver } from "@hookform/resolvers/zod"
import React from "react";
import { useForm } from "react-hook-form";
import { Form, FormControl, FormDescription, FormField, FormItem, FormLabel, FormMessage } from "@/components/ui/form";
import { Input } from "@/components/ui/input";
import CategoryPicker from "./CategoryPicker";
import CashboxPicker from "./CashboxPicker";
import { Popover, PopoverContent, PopoverTrigger } from "@/components/ui/popover";
import { Button } from "@/components/ui/button";
import { CalendarIcon, Loader2 } from "lucide-react";
import { Calendar } from "@/components/ui/calendar";
import { format } from "date-fns";
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select";
import { useMutation, useQueryClient } from "@tanstack/react-query";
import { CreateTransaction } from "@/data/services/transaction-service";
import { toast } from "sonner";


interface Props {
    trigger: ReactNode;
    type: TransactionType;
    user: UserData | null;
}


function CreateTransactionDialog({ trigger, type, user }: Props) {
    const form = useForm<CreateTransactionSchemaType>({
        resolver: zodResolver(CreateTransactionSchema),
        defaultValues: {
            type: type,
            date: new Date(),
            currency: user?.currency.code,
        },
    });
    const [open, setOpen] = useState(false);

    const handleCategoryChange = useCallback(
        (value: string) => {
            form.setValue("category", value);
        },
        [form]
    );

    const handleCashboxChange = useCallback(
        (value: string) => {
            form.setValue("cashbox", value);
        },
        [form]
    );

    const queryClient = useQueryClient();

    const { mutate, isPending } = useMutation({
        mutationFn: CreateTransaction,
        onSuccess: () => {
            toast.success("Transaction created successfully ⭐", {
                id: "create-transaction",
            });

            // Reset form fields
            form.reset({
                type,
                description: "",
                amount: 0,
                date: new Date(),
                category: undefined,
                cashbox: undefined,
                currency: user?.currency.code, // Reset to user's default currency
            });

            // Invalidate overview query to refetch data
            queryClient.invalidateQueries({
                queryKey: ["overview"],
            });
            setOpen((prev) => !prev);
        },
        onError: (error) => {
            console.error("Transaction creation failed:", error);
            toast.error("Failed to create transaction. Please try again.", {
                id: "create-transaction",
            });
        },
    });

    const onSubmit = useCallback(
        (values: CreateTransactionSchemaType) => {
            toast.loading("Creating transaction...", {
                id: "create-transaction"
            });

            mutate({
                ...values
            });
        },
        [mutate]
    );
    return (
        <Dialog open={open} onOpenChange={setOpen}>
            <DialogTrigger asChild>{trigger}</DialogTrigger>
            <DialogContent>
                <DialogHeader>
                    <DialogTitle>
                        Create a new
                        <span
                            className={cn(
                                "m-1",
                                type === "income" ? "text-emerald-500" : "text-red-500"
                            )}
                        >
                            {type}
                        </span>
                        transaction
                    </DialogTitle>
                </DialogHeader>
                <Form {...form}>
                    <form className="space-y-4" onSubmit={form.handleSubmit(onSubmit)}>
                        <FormField
                            control={form.control}
                            name="description"
                            render={({ field }) => (
                                <FormItem>
                                    <FormLabel>Description</FormLabel>
                                    <FormControl>
                                        <Input defaultValue={""} {...field} />
                                    </FormControl>
                                    <FormDescription>
                                        Transaction description (optional)
                                    </FormDescription>
                                </FormItem>
                            )}
                        />
                        <div className="flex items-center justify-between gap-2">

                            <FormField
                                control={form.control}
                                name="amount"
                                render={({ field }) => (
                                    <FormItem  className="flex flex-col">
                                        <FormLabel>Amount</FormLabel>
                                        <FormControl>
                                            <Input defaultValue={0} type="number" {...field} />
                                        </FormControl>
                                        <FormDescription>
                                            Transaction amount (required)
                                        </FormDescription>
                                    </FormItem>
                                )}
                            />
                            <FormField
                                control={form.control}
                                name="currency"
                                render={({ field }) => (
                                    <FormItem className="w-[200px] flex flex-col">
                                        <FormLabel>Currency</FormLabel>
                                        <Select onValueChange={field.onChange} defaultValue={field.value}>
                                            <FormControl>
                                                <SelectTrigger className="w-[200px]">
                                                    <SelectValue placeholder="Select currency" />
                                                </SelectTrigger>
                                            </FormControl>
                                            <SelectContent className="w-[200px]">
                                                <div className="max-h-[300px] overflow-y-auto">
                                                    {CURRENCIES.map((currency) => (
                                                        <SelectItem key={currency.code} value={currency.code}>
                                                            {currency.code} - {currency.name}
                                                        </SelectItem>
                                                    ))}
                                                </div>
                                            </SelectContent>
                                        </Select>
                                        <FormDescription>
                                            Transaction currency (required)
                                        </FormDescription>
                                    </FormItem>
                                )}
                            />
                        </div>
                        <div className="flex items-center justify-between gap-2">
                            <FormField
                                control={form.control}
                                name="category"
                                /* eslint-disable @typescript-eslint/no-unused-vars */
                                render={({ field }) => (
                                    <FormItem className="flex flex-col">
                                        <FormLabel>Category</FormLabel>
                                        <FormControl>
                                            <CategoryPicker type={type} onChange={handleCategoryChange} />
                                        </FormControl>
                                        <FormDescription>
                                            Select a category for this transaction
                                        </FormDescription>
                                    </FormItem>
                                )}
                            />
                            <FormField
                                control={form.control}
                                name="cashbox"
                                /* eslint-disable @typescript-eslint/no-unused-vars */
                                render={({ field }) => (
                                    <FormItem className="flex flex-col">
                                        <FormLabel>Cashbox</FormLabel>
                                        <FormControl>
                                            <CashboxPicker onChange={handleCashboxChange} user={user as UserData} />
                                        </FormControl>
                                        <FormDescription>
                                            Select a cashbox for this transaction
                                        </FormDescription>
                                    </FormItem>
                                )}
                            />
                        </div>
                        <div className="flex items-center justify-between gap-2">
                            <FormField
                                control={form.control}
                                name="date"
                                render={({ field }) => (
                                    <FormItem  className="flex flex-col">
                                        <FormLabel>Transaction Date</FormLabel>
                                        <Popover>
                                            <PopoverTrigger asChild>
                                                <FormControl>
                                                    <Button
                                                        variant={"outline"}
                                                        className={cn(
                                                            "w-[200px] pl-3 text-left font-normal",
                                                            !field.value && "text-muted-foreground"
                                                        )}
                                                    >
                                                        {field.value ? (
                                                            format(field.value, 'PPP')
                                                        ) : (
                                                            <span>Pick a date</span>
                                                        )}
                                                        <CalendarIcon className="ml-auto h-4 w-4 opacity-50" />
                                                    </Button>
                                                </FormControl>
                                            </PopoverTrigger>
                                            <PopoverContent className="w-auto p-0">
                                                <Calendar
                                                    mode="single"
                                                    selected={field.value}
                                                    onSelect={(value)=>{
                                                        if(!value) return;
                                                        field.onChange(value);
                                                    }}
                                                    initialFocus
                                                    disabled={(date) => date > new Date()}
                                                />
                                            </PopoverContent>
                                        </Popover>
                                        <FormDescription>
                                            Select a date for this transaction
                                        </FormDescription>
                                        <FormMessage />
                                    </FormItem>
                                )}
                            />
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

export default CreateTransactionDialog;

// function onSuccess(data: any, variables: { type: "income" | "expense"; amount: number; date: Date; category: string; currency: string; description?: string | undefined; }, context: unknown): unknown {
//     throw new Error("Function not implemented.");
// }
