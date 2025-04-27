"use client";

import { Button } from "@/components/ui/button";
import { Command, CommandEmpty, CommandGroup, CommandInput, CommandItem, CommandList } from "@/components/ui/command";
import { Popover, PopoverContent, PopoverTrigger } from "@/components/ui/popover";
import { useQuery } from "@tanstack/react-query";
import React, { useCallback, useEffect } from "react";
import CreateCashboxDialog from "./CreateCashboxDialog";

import { Check, ChevronsUpDown } from "lucide-react";
import { cn } from "@/lib/utils";
import { Cashbox, UserData } from "@/lib/types";
import { fetchUserCashBoxes } from "@/data/services/cashbox-service";

interface Props {
  user: UserData;
  onChange: (value: Cashbox) => void;
  defaultValue?: string;
}

function CashboxPicker({ onChange, defaultValue, user }: Props) {
  const [open, setOpen] = React.useState(false);
  const [value, setValue] = React.useState(defaultValue || "");


  useEffect(() => {
    if (defaultValue) {
      setValue(defaultValue);
    }
  }, [defaultValue]);

  const { data: cashboxes = [] } = useQuery<Cashbox[]>({
    queryKey: ["cashboxes"],
    queryFn: () => fetchUserCashBoxes(),
  });

  useEffect(() => {
    if (!value) return;
    const selected = cashboxes.find((cb) => cb.id === value);
    if (selected) onChange(selected);
  }, [onChange, value, cashboxes]);

  const selectedCashbox = cashboxes.find(
    (cashbox: Cashbox) => cashbox.id === value
  );

  const successCallback = useCallback((cashbox: Cashbox) => {
    setValue(cashbox.id)
    setOpen((prev) => !prev)
  },
    [setValue, setOpen]
  );

  return (
    <Popover open={open} onOpenChange={setOpen}>
      <PopoverTrigger asChild>
        <Button
          variant="outline"
          role="combobox"
          aria-expanded={open}
          className="w-[200px] justify-between"
        >
          {selectedCashbox ? (
            <CashboxRow cashbox={selectedCashbox} />
          ) : (
            "Select cashbox"
          )}
          <ChevronsUpDown className="ml-2 h-4 w-4 shrink-0 opacity-50" />
        </Button>
      </PopoverTrigger>
      <PopoverContent className="w-[200px] p-0">
        <Command onSubmit={(e) => e.preventDefault()}>
          <CommandInput placeholder="Search cashbox..." />
          <CreateCashboxDialog
            defaultCurrency={user?.currency.code}
            successCallback={successCallback}
          />
          <CommandEmpty>
            <p>Category not found</p>
            <p className="text-xs text-muted-foreground">
              Tip: Create a new category
            </p>
          </CommandEmpty>
          <CommandGroup>
            <CommandList>
              {cashboxes.map((cashbox) => (
                <CommandItem
                  key={cashbox.id}
                  onSelect={() => {
                    setValue(cashbox.id);
                    setOpen((prev) => !prev);
                    onChange(cashbox); // 👈 pass full object
                  }}
                >
                  <CashboxRow cashbox={cashbox} />
                  <Check
                    className={cn(
                      "mr-2 w-4 h-4 opacity-0",
                      value === cashbox.id && "opacity-100"
                    )}
                  />
                </CommandItem>
              ))}
            </CommandList>
          </CommandGroup>
        </Command>
      </PopoverContent>
    </Popover>
  );
}

export default CashboxPicker;

function CashboxRow({ cashbox }: { cashbox: Cashbox }) {
  return (
    <div className="flex items-center gap-2">
      <span>{cashbox.name}</span>
      <span className="text-muted-foreground">({cashbox.currency})</span>
    </div>
  );
}
