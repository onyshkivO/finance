"use client";

import SkeletonWrapper from "@/components/custom/SkeletonWrapper";
import { Card, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { ScrollArea } from "@/components/ui/scroll-area";
import { GetFormatterForCurrency } from "@/lib/helpers";
import { useQuery } from "@tanstack/react-query";
import { fetchUserCashBoxes } from "@/data/services/cashbox-service";
import { DropdownMenu, DropdownMenuContent, DropdownMenuItem, DropdownMenuLabel, DropdownMenuSeparator, DropdownMenuTrigger } from "@/components/ui/dropdown-menu";
import { Button } from "@/components/ui/button";
import { MoreHorizontal, ArrowRightLeft } from "lucide-react";
import { Cashbox, UserData } from "@/lib/types";
import CreateTransferDialog from "./CreateTransferDialog";
import { useState } from "react";

function CashboxOverviewCard({ user }: { user: UserData }) {
    const cashboxesQuery = useQuery({
        queryKey: ["cashboxesOb"],
        queryFn: fetchUserCashBoxes,
    });

    return (
        <SkeletonWrapper isLoading={cashboxesQuery.isFetching}>

        <Card className="h-80 w-full col-span-6 bg-card text-card-foreground flex flex-col gap-6 rounded-xl border py-6 shadow-sm flex-1">
            <CardHeader>
                <CardTitle>Cashboxes</CardTitle>
                <CardDescription>
                    Manage your cashboxes and their balances
                </CardDescription>
            </CardHeader>

                <div className="flex items-center justify-between gap-2">
                    {cashboxesQuery.data?.length === 0 && (
                        <div className="flex h-60 w-full flex-col items-center justify-center">
                            No cashboxes found
                        </div>
                    )}
                    
                    {cashboxesQuery.data && cashboxesQuery.data.length > 0 && (
                        <ScrollArea className="h-60 w-full px-4">
                            <div className="flex w-full flex-col gap-4 p-4">
                                {cashboxesQuery.data.map((cashbox) => {
                                    const formatter = GetFormatterForCurrency(cashbox.currency);
                                    return (
                                        <div key={cashbox.id} className="flex flex-col gap-2">
                                            <div className="flex items-center justify-between">
                                                <span className="flex items-center text-gray-400">
                                                    {cashbox.name}
                                                    <span className="ml-2 text-xs text-muted-foreground">
                                                        ({cashbox.currency})
                                                    </span>
                                                </span>
                                                <span className="text-sm text-gray-400">
                                                    {formatter.format(cashbox.balance)}
                                                </span>
                                                <RowActions cashbox={cashbox} user={user} />
                                            </div>
                                        </div>
                                    );
                                })}
                            </div>
                        </ScrollArea>
                    )}
                </div>
            </Card>
        </SkeletonWrapper>
    );
}

function RowActions({ cashbox, user }: { cashbox: Cashbox, user: UserData }) {
    const [showCreateTransferDialog, setShowCreateTransferDialog] = useState(false);

    return (
        <>
        <CreateTransferDialog 
                    cashbox={cashbox}
                    user={user}
                    open={showCreateTransferDialog}
                    setOpen={setShowCreateTransferDialog}
                />
        <DropdownMenu>
            <DropdownMenuTrigger asChild>
                <Button variant="ghost" className="h-8 w-8 p-0">
                    <span className="sr-only">Open menu</span>
                    <MoreHorizontal className="h-4 w-4" />
                </Button>
            </DropdownMenuTrigger>
            <DropdownMenuContent align="end">
                <DropdownMenuLabel>Actions</DropdownMenuLabel>
                <DropdownMenuSeparator />
                <DropdownMenuItem
                      className="flex items-center gap-2"
                      onSelect={() => {
                        setShowCreateTransferDialog((prev) => !prev);
                      }}
                  >
                      <ArrowRightLeft className="h-4 w-4 text-muted-foreground" />
                      Transfer
                  </DropdownMenuItem>
                
            </DropdownMenuContent>
        </DropdownMenu>
        </>
    );
}

export default CashboxOverviewCard;