"use client";

import { Button } from "@/components/ui/button";
// import { Command, CommandInput, CommandList, CommandEmpty, CommandGroup, CommandItem } from "@/components/ui/command";
import { Popover, PopoverContent, PopoverTrigger } from "@/components/ui/popover";
import { TransactionType } from "@/lib/types";
import { useQuery } from "@tanstack/react-query";
import React from "react";

interface Props {
    type: TransactionType;
}
interface Category {
    id: string; 
    name: string; 
    type: string; 
    icon: string;
    mccCodes: Set<number>;
}

function CategoryPicker({ type }: Props) {
    const [open, setOpen] = React.useState(false);
    const [value, setValue] = React.useState("");

    const categoriesQuery = useQuery({
        queryKey: ["categories", type],
        queryFn: () =>
            fetch(`/category/type/${type}`).then((res) =>
                res.json()),
    });

    const selectedCategory = categoriesQuery.data?.find(
        (category: Category) => category.name === value
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
                    {selectedCategory ? (
                        <CategoryRow category={selectedCategory} />
                    ) : (
                        "Select category"
                    )}
                </Button>
            </PopoverTrigger>
            <PopoverContent className="w-[200px] p-0">
                {/* <Command>
                    <CommandInput placeholder="Search category..." />
                    <CommandList>
                        <CommandEmpty>No results found.</CommandEmpty>
                        <CommandGroup heading="Categories">
                            {categoriesQuery.data?.map((category: Category) => (
                                <CommandItem
                                    key={category.id}
                                    onSelect={() => {
                                        setValue(category.name);
                                        setOpen(false);
                                    }}
                                >
                                    <CategoryRow category={category} />
                                </CommandItem>
                            ))}
                        </CommandGroup>
                    </CommandList>
                </Command> */}
            </PopoverContent>
        </Popover>
    );
}

export default CategoryPicker;

function CategoryRow({ category }: { category: Category }) {
    return (
        <div className="flex items-center gap-2">
            <span role="img">{category.icon}</span>
            <span>{category.name}</span>
        </div>
    );
}
