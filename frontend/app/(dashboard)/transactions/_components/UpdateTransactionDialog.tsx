"use client";

import { Dialog, DialogContent, DialogHeader, DialogTitle, DialogFooter, DialogClose } from "@/components/ui/dialog";
import { CURRENCIES, Transaction, TransactionType, UserData } from "@/lib/types";
import { cn } from "@/lib/utils";
import { CreateTransactionSchema, CreateTransactionSchemaType } from "@/schema/transaction";
import {useCallback, useEffect } from "react";
import { zodResolver } from "@hookform/resolvers/zod"
import React from "react";
import { useForm } from "react-hook-form";
import { Form, FormControl, FormDescription, FormField, FormItem, FormLabel, FormMessage } from "@/components/ui/form";
import { Input } from "@/components/ui/input";
import CategoryPicker from "@/app/(dashboard)/_components/CategoryPicker";
import { Popover, PopoverContent, PopoverTrigger } from "@/components/ui/popover";
import { Button } from "@/components/ui/button";
import { CalendarIcon, Loader2 } from "lucide-react";
import { Calendar } from "@/components/ui/calendar";
import { format } from "date-fns";
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select";
import { useMutation, useQueryClient } from "@tanstack/react-query";
import { UpdateTransaction } from "@/data/services/transaction-service";
import { toast } from "sonner";
import Cookies from "js-cookie";

interface Props {
    transaction: Transaction;
    open: boolean;
    setOpen: (open: boolean) => void;
}

function UpdateTransactionDialog({ transaction, open, setOpen }: Props) {
    const queryClient = useQueryClient();
    const [userCurrency, setUserCurrency] = React.useState<string>("");

    useEffect(() => {
        const userDataString = Cookies.get("userData");
        if (userDataString) {
            try {
                const userData: UserData = JSON.parse(userDataString);
                setUserCurrency(userData.currency.code);
            } catch (error) {
                console.error("Failed to parse userData cookie:", error);
            }
        }
    }, []);

    const form = useForm<CreateTransactionSchemaType>({
        resolver: zodResolver(CreateTransactionSchema),
        defaultValues: {
            type: transaction.type.toLowerCase() as TransactionType,
            date: new Date(transaction.transactionDate.split('-').reverse().join('-')),
            currency: userCurrency,
            amount: transaction.amount,
            description: transaction.description || '',
            category: transaction.category.id,
        },
    });

    useEffect(() => {
        if (userCurrency) {
            form.setValue("currency", userCurrency);
        }
    }, [userCurrency, form]);

    const handleCategoryChange = useCallback(
        (value: string) => {
            form.setValue("category", value);
        },
        [form]
    );

    const { mutate, isPending } = useMutation({
        mutationFn: (values: CreateTransactionSchemaType) => UpdateTransaction(transaction.id, values),
        onSuccess: () => {
            toast.success("Transaction updated successfully ⭐", {
                id: "update-transaction",
            });

            // Invalidate queries to refetch data
            queryClient.invalidateQueries({
                queryKey: ["transactions", "history"],
            });
            queryClient.invalidateQueries({
                queryKey: ["overview"],
            });
            
            setOpen(false);
        },
        onError: (error) => {
            console.error("Transaction update failed:", error);
            toast.error("Failed to update transaction. Please try again.", {
                id: "update-transaction",
            });
        },
    });

    const onSubmit = useCallback(
        (values: CreateTransactionSchemaType) => {
            toast.loading("Updating transaction...", {
                id: "update-transaction"
            });
            mutate(values);
        },
        [mutate]
    );

    return (
        <Dialog open={open} onOpenChange={setOpen}>
            <DialogContent>
                <DialogHeader>
                    <DialogTitle>
                        Update transaction
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
                                        <Input {...field} />
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
                                    <FormItem className="flex flex-col">
                                        <FormLabel>Amount</FormLabel>
                                        <FormControl>
                                            <Input type="number" {...field} />
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
                                            <CategoryPicker 
                                                type={form.getValues("type")} 
                                                onChange={handleCategoryChange} 
                                                defaultValue={transaction.category.id}
                                            />
                                        </FormControl>
                                        <FormDescription>
                                            Select a category for this transaction
                                        </FormDescription>
                                    </FormItem>
                                )}
                            />
                            <FormField
                                control={form.control}
                                name="date"
                                render={({ field }) => (
                                    <FormItem className="flex flex-col">
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
                                                    onSelect={(value) => {
                                                        if (!value) return;
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

export default UpdateTransactionDialog; 