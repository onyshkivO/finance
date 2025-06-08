"use client";
import { Button } from "@/components/ui/button";
import { Dialog, DialogClose, DialogContent, DialogDescription, DialogFooter, DialogHeader, DialogTitle, DialogTrigger } from "@/components/ui/dialog";
import { Form, FormControl, FormDescription, FormField, FormItem, FormLabel } from "@/components/ui/form";
import { Input } from "@/components/ui/input";
import {
    CreateCashboxSchema,
    CreateCashboxSchemaType,
} from "@/schema/cashbox";
import { zodResolver } from "@hookform/resolvers/zod";
import { Loader2, PlusSquare } from "lucide-react";
import React, { useCallback } from "react";
import { useForm } from "react-hook-form";
import { useMutation, useQueryClient } from "@tanstack/react-query";
import { toast } from "sonner";
import { Cashbox, CURRENCIES } from "@/lib/types";
import { CreateCashbox } from "@/data/services/cashbox-service";
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select";

interface Props {
    defaultCurrency: string;
    successCallback: (cashbox: Cashbox) => void;
    trigger?: React.ReactNode;
}

function CreateCashboxDialog({ successCallback, trigger, defaultCurrency }: Props) {
    const [open, setOpen] = React.useState(false);
    const form = useForm<CreateCashboxSchemaType>({
        resolver: zodResolver(CreateCashboxSchema),
        defaultValues: {
            name: "",
            currency: defaultCurrency,
            balance: 0,
        },
    });

    const queryClient = useQueryClient();
    const { mutate, isPending } = useMutation({
        mutationFn: CreateCashbox,
        onSuccess: async (data: Cashbox) => {
            form.reset({
                name: "",
                currency: defaultCurrency,
            });
            toast.success(`Cashbox ${data.name} created successfully`, {
                id: "create-cashbox",
            });
            successCallback(data);
            await queryClient.invalidateQueries({
                queryKey: ["cashboxes"],
            });
            setOpen((prev) => !prev);
        },
        onError: (error) => {
            toast.error("Failed to create cashbox. " + error.message, {
                id: "create-cashbox",
            });
        },
    });

    const onSubmit = useCallback(
        (values: CreateCashboxSchemaType) => {
            toast.loading("Creating cashbox...", { id: "create-cashbox" });
            mutate(values);
        },
        [mutate]
    );
    

    return (
        <Dialog open={open} onOpenChange={setOpen}>
            <DialogTrigger asChild>
                {trigger ? (
                    trigger
                ) : (
                    <Button
                        variant="ghost"
                        className="flex border-separate items-center justify-start rounded-none border-b px-3 py-3 text-muted-foreground"
                    >
                        <PlusSquare className="mr-2 h-4 w-4" />
                        Create new
                    </Button>
                )}
            </DialogTrigger>
            <DialogContent>
                <DialogHeader>
                    <DialogTitle>Create new cashbox</DialogTitle>
                    <DialogDescription>
                        Add a new cashbox to manage your finances
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
                                        Give your cashbox a descriptive name
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
                                            Cashbox currency (required)
                                        </FormDescription>
                                    </FormItem>
                                )}
                            />
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
                                        Set the initial balance for this cashbox
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
                            "Create"
                        )}
                    </Button>
                </DialogFooter>
            </DialogContent>
        </Dialog>
    );
}

export default CreateCashboxDialog;