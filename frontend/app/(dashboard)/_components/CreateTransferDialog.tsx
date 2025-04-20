"use client";

import { Dialog, DialogContent, DialogFooter, DialogHeader, DialogTitle } from "@/components/ui/dialog";
import { Cashbox, UserData } from "@/lib/types";
import { zodResolver } from "@hookform/resolvers/zod";
import { useForm } from "react-hook-form";
import { Form, FormControl, FormDescription, FormField, FormItem, FormLabel, FormMessage } from "@/components/ui/form";
import { Input } from "@/components/ui/input";
import { Button } from "@/components/ui/button";
import { CalendarIcon } from "lucide-react";
import { Calendar } from "@/components/ui/calendar";
import { format } from "date-fns";
import { Popover, PopoverContent, PopoverTrigger } from "@/components/ui/popover";
import { useMutation, useQueryClient } from "@tanstack/react-query";
import { CreateTransfer } from "@/data/services/cashbox-service";
import { toast } from "sonner";
import { cn } from "@/lib/utils";
import { CreateTransferSchema, CreateTransferSchemaType } from "@/schema/cashbox";
import { useCallback } from "react";
import CashboxPicker from "./CashboxPicker";
import { Loader2 } from "lucide-react";


interface Props {
    open: boolean;
    setOpen: (open: boolean) => void;
    successCallback: () => void;
    cashbox: Cashbox;
    user: UserData | null;
}

/* eslint-disable @typescript-eslint/no-unused-vars */
function CreateTransferDialog({ cashbox, user, open, setOpen, successCallback }: Props) {
    const form = useForm<CreateTransferSchemaType>({
        resolver: zodResolver(CreateTransferSchema),
        defaultValues: {
            amount: 0,
            description: "",
            cashboxFrom: cashbox.id,
            date: new Date(),
        },
    });

    const handleCashboxChange = useCallback(
        (value: string) => {
            form.setValue("cashboxTo", value);
        },
        [form]
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
                date: new Date()
            });
            setOpen(false);
            successCallback()
            queryClient.invalidateQueries({ queryKey: ["cashboxesOb"] });
        },
        onError: (error) => {
            console.error("Transfer creation failed:", error);
            toast.error("Failed to create transfer. "+error.message, {
                id: "create-transfer",
            });
        },
    });

    const onSubmit = useCallback(
        (values: CreateTransferSchemaType) => {
           console.log(values);
            toast.loading("Creating transfer...", {
                id: "create-transfer",
            });
            mutate({
                ...values
                // cashboxFrom: cashbox.id
            });
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
                        <FormField
                            control={form.control}
                            name="amount"
                            render={({ field }) => (
                                <FormItem>
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
                                name="cashboxTo"
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
                                        <FormMessage /> 
                                    </FormItem>
                                )}
                            />

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
                                        Transfer description (optional)
                                    </FormDescription>
                                </FormItem>
                            )}
                        />

                            <FormField
                                control={form.control}
                                name="date"
                                render={({ field }) => (
                                    <FormItem  className="flex flex-col">
                                        <FormLabel>Transfer Date</FormLabel>
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
                                            Select a date for this transfer
                                        </FormDescription>
                                        <FormMessage />
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
                        onClick={form.handleSubmit(onSubmit)}
                        // onClick={form.handleSubmit(onSubmit)}
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
