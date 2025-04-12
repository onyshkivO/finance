"use client"

import * as React from "react"
import { useMediaQuery } from "@/hooks/use-media-query"
import { Button } from "@/components/ui/button"
import {
    Command,
    CommandEmpty,
    CommandGroup,
    CommandInput,
    CommandItem,
    CommandList,
} from "@/components/ui/command"
import {
    Drawer,
    DrawerContent,
    DrawerTrigger,
} from "@/components/ui/drawer"
import {
    Popover,
    PopoverContent,
    PopoverTrigger,
} from "@/components/ui/popover"
import { CURRENCIES, Currency, UserData } from "@/lib/types"
import { useMutation } from "@tanstack/react-query"
import { UpdateUserCurrency } from "@/data/services/currency-service"
import { toast } from "sonner"
import Cookies from "js-cookie"
import { useUser } from "@/context/UserContext"

const config = {
    expires: 7, // 7 days
    path: "/",
    secure: process.env.NODE_ENV === "production",
    sameSite: "lax" as const
};

interface CurrencyComboBoxProps {
    baseCurrency: Currency
}

export function CurrencyComboBox({ baseCurrency }: CurrencyComboBoxProps) {
    const [open, setOpen] = React.useState(false)
    const isDesktop = useMediaQuery("(min-width: 768px)")
    const [selectedStatus, setSelectedOption] = React.useState<Currency | null>(null)
    const { setUser } = useUser()

    React.useEffect(() => {
        console.log("baseCurrency"+baseCurrency.code);
        
        const userCurrency = CURRENCIES.find(
            (currency) => currency.code === baseCurrency.code
        );
        console.log("userCurrency"+userCurrency?.code);
        
        if (userCurrency) setSelectedOption(userCurrency);
    }, [baseCurrency]);

    const mutation = useMutation({
        mutationFn: UpdateUserCurrency,
        onSuccess: (currencyCode: string) => {
            toast.success('Currency updated successfully', {
                id: "update-currency",
            });

            const updatedCurrency = CURRENCIES.find((c) => c.code === currencyCode);

            if (!updatedCurrency) return;

            const userDataString = Cookies.get("userData");
            
            if (userDataString) {
                try {
                    const userData: UserData = JSON.parse(userDataString);
                    const updatedUserData = {
                        ...userData,
                        currency: updatedCurrency,
                    };
                    Cookies.set("userData", JSON.stringify(updatedUserData), config);
                    // Update the user context
                    setUser(updatedUserData);
                } catch (error) {
                    console.error("Failed to parse userData cookie:", error);
                }
            }
        },
        onError: (e) => {
            console.error(e);
            toast.error("Failed to update currency", {
                id: "update-currency",
            });
        },
    });

    const updateOption = React.useCallback(
        (currency: Currency | null) => {
            if (!currency) {
                toast.error("Please select a currency");
                return;
            }

            toast.loading("Updating currency...", {
                id: "update-currency",
            });
            mutation.mutate(currency.code);
        },
        [mutation]
    );

    return (
        <div>
            {/* Currency Selection for Desktop */}
            {isDesktop ? (
                <Popover open={open} onOpenChange={setOpen}>
                    <PopoverTrigger asChild>
                        <Button variant="outline" className="w-full justify-start" disabled={mutation.isPending}>
                            {selectedStatus ? <>{selectedStatus.name}</> : <>+ Set currency</>}
                        </Button>
                    </PopoverTrigger>
                    <PopoverContent className="w-[200px] p-0" align="start">
                        <OptionList setOpen={setOpen} setSelectedOption={setSelectedOption} />
                    </PopoverContent>
                </Popover>
            ) : (
                <Drawer open={open} onOpenChange={setOpen}>
                    <DrawerTrigger asChild>
                        <Button variant="outline" className="w-full justify-start" disabled={mutation.isPending}>
                            {selectedStatus ? <>{selectedStatus.name}</> : <>+ Set currency</>}
                        </Button>
                    </DrawerTrigger>
                    <DrawerContent>
                        <div className="mt-4 border-t">
                            <OptionList setOpen={setOpen} setSelectedOption={setSelectedOption} />
                        </div>
                    </DrawerContent>
                </Drawer>
            )}

            {/* Update Currency Button outside of Popover */}
            <div className="p-4">
                <Button
                    className="w-full"
                    onClick={() => updateOption(selectedStatus)}
                    disabled={!selectedStatus || mutation.isPending}
                >
                    Update Currency
                </Button>
            </div>
        </div>
    )
}

function OptionList({
    setOpen,
    setSelectedOption,
}: {
    setOpen: (open: boolean) => void
    setSelectedOption: (status: Currency | null) => void
}) {
    return (
        <Command>
            <CommandInput placeholder="Filter currency..." />
            <CommandList>
                <CommandEmpty>No results found.</CommandEmpty>
                <CommandGroup>
                    {CURRENCIES.map((currency: Currency) => (
                        <CommandItem
                            key={currency.name}
                            value={currency.name}
                            onSelect={(name) => {
                                console.log("CURRENCIES.find((currency) => currency.name === name) || null"+CURRENCIES.find((currency) => currency.name === name) || null)
                                setSelectedOption(
                                    CURRENCIES.find((currency) => currency.name === name) || null
                                )
                                setOpen(false)
                            }}
                        >
                            {currency.name}
                        </CommandItem>
                    ))}
                </CommandGroup>
            </CommandList>
        </Command>
    )
}