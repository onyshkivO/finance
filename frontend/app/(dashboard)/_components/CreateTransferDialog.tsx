"use client";

import {
    Dialog,
    DialogContent,
    DialogFooter,
    DialogHeader,
    DialogTitle,
} from "@/components/ui/dialog";
import { Cashbox, UserData } from "@/lib/types";
import { zodResolver } from "@hookform/resolvers/zod";
import { useForm } from "react-hook-form";
import {
    Form,
    FormControl,
    FormDescription,
    FormField,
    FormItem,
    FormLabel,
    FormMessage,
} from "@/components/ui/form";
import { Input } from "@/components/ui/input";
import { Button } from "@/components/ui/button";
import { CalendarIcon, Loader2 } from "lucide-react";
import { Calendar } from "@/components/ui/calendar";
import { format } from "date-fns";
import {
    Popover,
    PopoverContent,
    PopoverTrigger,
} from "@/components/ui/popover";
import { useMutation, useQueryClient } from "@tanstack/react-query";
import { CreateTransfer } from "@/data/services/cashbox-service";
import { toast } from "sonner";
import { cn } from "@/lib/utils";
import {
    CreateTransferSchema,
    CreateTransferSchemaType,
} from "@/schema/cashbox";
import { useCallback, useState } from "react";
import CashboxPicker from "./CashboxPicker";

interface Props {
    open: boolean;
    setOpen: (open: boolean) => void;
    successCallback: () => void;
    cashbox: Cashbox;
    user: UserData | null;
}

function CreateTransferDialog({
    cashbox,
    user,
    open,
    setOpen,
    successCallback,
}: Props) {
    const [customCoefficient, setCustomCoefficient] = useState(false);
    const [isSameCurrency, setIsSameCurrency] = useState(false);
    const [isCashboxToSelected, setIsCashboxToSelected] = useState(false);


    const form = useForm<CreateTransferSchemaType>({
        resolver: zodResolver(CreateTransferSchema),
        defaultValues: {
            amount: 0,
            description: "",
            cashboxFrom: cashbox.id,
            date: new Date(),
            coefficient: 1,
        },
    });


    const handleCashboxChange = useCallback(
        (targetCashbox: Cashbox) => {
            form.setValue("cashboxTo", targetCashbox.id);
            setIsCashboxToSelected(true);
    
            if (!cashbox.currency || !targetCashbox.currency) {
                setIsSameCurrency(false);
                form.setValue("coefficient", 0); // or use "-" if you're displaying as string
                return;
            }
    
            const fromCurrency = cashbox.currency.toLowerCase();
            const toCurrency = targetCashbox.currency.toLowerCase();
    
            if (fromCurrency === toCurrency) {
                setIsSameCurrency(true);
                setCustomCoefficient(false);
                form.setValue("coefficient", 1);
            } else {
                setIsSameCurrency(false);
                if (customCoefficient) return;
    
                    
                fetch(`https://cdn.jsdelivr.net/npm/@fawazahmed0/currency-api@latest/v1/currencies/${fromCurrency}.json`)
                    .then((res) => res.json())
                    .then((data) => {
                        const rate = data[fromCurrency]?.[toCurrency];
                        if (rate) {
                            const roundedRate = Math.round(rate * 100) / 100;
                            form.setValue("coefficient", roundedRate);
                        }
                    });
            }
        },
        [cashbox.currency, customCoefficient, form]
    );

    const queryClient = useQueryClient();

    const { mutate, isPending } = useMutation({
        mutationFn: CreateTransfer,
        onSuccess: () => {
            toast.success("Transfer created successfully", {
                id: "create-transfer",
            });
            form.reset({
                description: "",
                amount: 0,
                date: new Date(),
                cashboxFrom: cashbox.id,
                coefficient: 1,
            });
            setCustomCoefficient(false);
            setOpen(false);
            successCallback();
            queryClient.invalidateQueries({ queryKey: ["cashboxesOb"] });
        },
        onError: (error) => {
            console.error("Transfer creation failed:", error);
            toast.error("Failed to create transfer. " + error.message, {
                id: "create-transfer",
            });
        },
    });

    const onSubmit = useCallback(
        (values: CreateTransferSchemaType) => {
            toast.loading("Creating transfer...", {
                id: "create-transfer",
            });
            mutate(values);
        },
        [mutate]
    );

    return (
        <Dialog open={open} onOpenChange={setOpen}>
            <DialogContent>
                <DialogHeader>
                    <DialogTitle>Transfer from {cashbox.name}</DialogTitle>
                </DialogHeader>
                <Form {...form}>
                    <form onSubmit={form.handleSubmit(onSubmit)} className="space-y-4">

                        {/* First row: Amount + Date */}
                        <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                            <FormField
                                control={form.control}
                                name="amount"
                                render={({ field }) => (
                                    <FormItem>
                                        <FormLabel>Amount</FormLabel>
                                        <FormControl>
                                            <Input type="number" {...field} />
                                        </FormControl>
                                        <FormDescription>Transaction amount (required)</FormDescription>
                                    </FormItem>
                                )}
                            />

                            <FormField
                                control={form.control}
                                name="date"
                                render={({ field }) => (
                                    <FormItem className="flex flex-col">
                                        <FormLabel>Transfer Date</FormLabel>
                                        <Popover>
                                            <PopoverTrigger asChild>
                                                <FormControl>
                                                    <Button
                                                        variant="outline"
                                                        className={cn(
                                                            "w-full pl-3 text-left font-normal",
                                                            !field.value && "text-muted-foreground"
                                                        )}
                                                    >
                                                        {field.value ? format(field.value, "PPP") : "Pick a date"}
                                                        <CalendarIcon className="ml-auto h-4 w-4 opacity-50" />
                                                    </Button>
                                                </FormControl>
                                            </PopoverTrigger>
                                            <PopoverContent className="w-auto p-0">
                                                <Calendar
                                                    mode="single"
                                                    selected={field.value}
                                                    onSelect={(value) => value && field.onChange(value)}
                                                    initialFocus
                                                    disabled={(date) => date > new Date()}
                                                />
                                            </PopoverContent>
                                        </Popover>
                                        <FormDescription>
                                            Select a date for this transfer
                                        </FormDescription>
                                        <FormMessage />
                                    </FormItem>
                                )}
                            />
                        </div>

                        {/* Second row: Cashbox + Coefficient */}
                        <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                            <FormField
                                control={form.control}
                                name="cashboxTo"
                                render={() => (
                                    <FormItem className="flex flex-col">
                                        <FormLabel>Cashbox</FormLabel>
                                        <FormControl>
                                            <CashboxPicker
                                                onChange={handleCashboxChange}
                                                user={user as UserData}
                                            />
                                        </FormControl>
                                        <FormDescription>
                                            Select a cashbox for this transaction
                                        </FormDescription>
                                        <FormMessage />
                                    </FormItem>
                                )}
                            />

                            <FormField
                                control={form.control}
                                name="coefficient"
                                render={({ field }) => (
                                    <FormItem>
                                        <FormLabel>Coefficient</FormLabel>
                                        <FormControl>
                                            <Input
                                                type="number"
                                                step="0.0001"
                                                {...field}
                                                disabled={!isCashboxToSelected || isSameCurrency}
                                                value={
                                                    !isCashboxToSelected
                                                        ? "-"
                                                        : isSameCurrency
                                                            ? 1
                                                            : field.value ?? 0
                                                }
                                                onChange={(e) => {
                                                    setCustomCoefficient(true);
                                                    const value = parseFloat(e.target.value);
                                                    field.onChange(value);
                                                }}
                                            />
                                        </FormControl>
                                        <FormDescription>
                                            {isSameCurrency
                                                ? "Same currency — coefficient is 1"
                                                : !isCashboxToSelected
                                                    ? "Please select target cashbox"
                                                    : "Currency exchange coefficient. Auto-filled, but you can override."}
                                        </FormDescription>
                                        <FormMessage />
                                    </FormItem>
                                )}
                            />
                        </div>

                        {/* Third row: Description */}
                        <FormField
                            control={form.control}
                            name="description"
                            render={({ field }) => (
                                <FormItem>
                                    <FormLabel>Description</FormLabel>
                                    <FormControl>
                                        <Input {...field} />
                                    </FormControl>
                                    <FormDescription>Transfer description (optional)</FormDescription>
                                </FormItem>
                            )}
                        />

                        <DialogFooter>
                            <Button
                                type="button"
                                variant="outline"
                                onClick={() => setOpen(false)}
                            >
                                Cancel
                            </Button>
                            <Button
                                type="submit"
                                disabled={isPending}
                                className="cursor-pointer"
                            >
                                {isPending ? (
                                    <>
                                        <Loader2 className="mr-2 h-4 w-4 animate-spin" />
                                        Creating...
                                    </>
                                ) : (
                                    "Create Transfer"
                                )}
                            </Button>
                        </DialogFooter>
                    </form>
                </Form>
            </DialogContent>
        </Dialog>

    );
}

export default CreateTransferDialog;
