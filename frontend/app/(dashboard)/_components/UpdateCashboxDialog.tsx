"use client";
import { Button } from "@/components/ui/button";
import { Dialog, DialogClose, DialogContent, DialogFooter, DialogHeader, DialogTitle } from "@/components/ui/dialog";
import { Form, FormControl, FormDescription, FormField, FormItem, FormLabel } from "@/components/ui/form";
import { Input } from "@/components/ui/input";
import {
    CreateCashboxSchema,
    CreateCashboxSchemaType,
} from "@/schema/cashbox";
import { zodResolver } from "@hookform/resolvers/zod";
import { Loader2 } from "lucide-react";
import React, { useCallback } from "react";
import { useForm } from "react-hook-form";
import { useMutation, useQueryClient } from "@tanstack/react-query";
import { toast } from "sonner";
import { Cashbox } from "@/lib/types";
import { UpdateCashbox } from "@/data/services/cashbox-service";

interface Props {
    open: boolean;
    setOpen: (open: boolean) => void;
    successCallback: () => void;
    cashbox: Cashbox;
}

function UpdateCashboxDialog({ cashbox, open, setOpen, successCallback }: Props) {
    const form = useForm<CreateCashboxSchemaType>({
        resolver: zodResolver(CreateCashboxSchema),
        defaultValues: {
            name: cashbox.name,
            currency: cashbox.currency,
            balance: cashbox.balance,
        },
    });

    const queryClient = useQueryClient();
    const { mutate, isPending } = useMutation({
        mutationFn: (values: CreateCashboxSchemaType) => {
                    console.log('Updating Cashbox with values:', values);
                    console.log('Cashbox ID:', cashbox.id);
                    return UpdateCashbox(values, cashbox.id);
                },
        onSuccess: async (data: Cashbox) => {
            form.reset();
            toast.success(`Cashbox ${data.name} updated successfully`, {
                id: "update-cashbox",
            });
            successCallback()
            await queryClient.invalidateQueries({
                queryKey: ["cashboxes"],
            });
            setOpen(false);
        },
        onError: (error) => {
            
            toast.error("Failed to update cashbox. " + error.message, {
                id: "update-cashbox",
            });
        },
    });

    const onSubmit = useCallback(
        (values: CreateCashboxSchemaType) => {
            toast.loading("Updating cashbox...", { id: "update-cashbox" });
            mutate(values);
        },
        [mutate]
    );
    

    return (
        <Dialog open={open} onOpenChange={setOpen}>
            <DialogContent>
                <DialogHeader>
                    <DialogTitle>Update cashbox</DialogTitle>
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
                                        Give your cashbox a descriptive name
                                    </FormDescription>
                                </FormItem>
                            )}
                        />
                        {/* <FormField
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
                                            Cashbox currency (required)
                                        </FormDescription>
                                    </FormItem>
                                )}
                            /> */}
                        <FormField
                            control={form.control}
                            name="balance"
                            render={({ field }) => (
                                <FormItem>
                                    <FormLabel>Initial Balance</FormLabel>
                                    <FormControl>
                                        <Input
                                            type="number"
                                            placeholder="Enter initial balance"
                                            {...field}
                                            onChange={(e) => field.onChange(Number(e.target.value))}
                                        />
                                    </FormControl>
                                    <FormDescription>
                                        Set the balance for this cashbox
                                    </FormDescription>
                                </FormItem>
                            )}
                        />
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
                            "Update"
                        )}
                    </Button>
                </DialogFooter>
            </DialogContent>
        </Dialog>
    );
}

export default UpdateCashboxDialog;