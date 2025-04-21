"use client";

import SkeletonWrapper from "@/components/custom/SkeletonWrapper";
import { Card, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { ScrollArea } from "@/components/ui/scroll-area";
import { GetFormatterForCurrency } from "@/lib/helpers";
import { useQuery } from "@tanstack/react-query";
import { fetchUserCashBoxes } from "@/data/services/cashbox-service";
import { DropdownMenu, DropdownMenuContent, DropdownMenuItem, DropdownMenuLabel, DropdownMenuSeparator, DropdownMenuTrigger } from "@/components/ui/dropdown-menu";
import { Button } from "@/components/ui/button";
import { MoreHorizontal, ArrowRightLeft, PencilIcon, PlusSquare } from "lucide-react";
import { Cashbox, UserData } from "@/lib/types";
import CreateTransferDialog from "./CreateTransferDialog";
import { useState } from "react";
import { Separator } from "@/components/ui/separator";
import UpdateCashboxDialog from "./UpdateCashboxDialog";
import CreateCashboxDialog from "./CreateCashboxDialog";

function CashboxOverviewCard({ user }: { user: UserData }) {
    const cashboxesQuery = useQuery({
        queryKey: ["cashboxesOb"],
        queryFn: fetchUserCashBoxes,
    });

    return (
        <SkeletonWrapper isLoading={cashboxesQuery.isFetching}>

            <Card className="h-80 w-full col-span-6 bg-card text-card-foreground flex flex-col gap-6 rounded-xl border py-6 shadow-sm flex-1">
                <CardHeader>
                    <CardTitle className="flex items-center justify-between gap-2">
                        <div className="flex items-center gap-2">
                            <div>
                                <div className="text-sm text-muted-foreground">
                                    Cashboxes
                                </div>
                            </div>
                        </div>

                        <CreateCashboxDialog
                            successCallback={() => cashboxesQuery.refetch()}
                            trigger={
                                <Button className="gap-2 text-sm">
                                    <PlusSquare className="h-4 w-4" />
                                    Create cashbox
                                </Button>
                            }
                            defaultCurrency={user.currency.code}
                        />
                    </CardTitle>
                    <CardDescription>
                            Manage your cashboxes and their balances
                    </CardDescription>
                </CardHeader>
                <Separator />

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
                                            <div className="grid grid-cols-[1fr_auto_auto] items-center gap-4">
                                                <span className="flex items-center text-gray-400 truncate">
                                                    {cashbox.name}
                                                    <span className="ml-2 text-xs text-muted-foreground">
                                                        ({cashbox.currency})
                                                    </span>
                                                </span>
                                                <span className="text-sm text-gray-400 text-right whitespace-nowrap">
                                                    {formatter.format(cashbox.balance)}
                                                </span>
                                                <div className="flex justify-end">
                                                    <RowActions cashbox={cashbox} user={user} cashboxesQuery={cashboxesQuery} />
                                                </div>
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
// eslint-disable-next-line @typescript-eslint/no-explicit-any
function RowActions({ cashbox, user, cashboxesQuery }: { cashbox: Cashbox, user: UserData, cashboxesQuery: any }) {
    const [showCreateTransferDialog, setShowCreateTransferDialog] = useState(false);
    const [showUpdateCashboxDialog, setShowUpdateCashboxDialog] = useState(false);

    return (
        <>
            <CreateTransferDialog
                successCallback={() => cashboxesQuery.refetch()}
                cashbox={cashbox}
                user={user}
                open={showCreateTransferDialog}
                setOpen={setShowCreateTransferDialog}
            />
            <UpdateCashboxDialog
                successCallback={() => cashboxesQuery.refetch()}
                cashbox={cashbox}
                open={showUpdateCashboxDialog}
                setOpen={setShowUpdateCashboxDialog}
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
                    <DropdownMenuItem
                        className="flex items-center gap-2"
                        onSelect={() => {
                            setShowUpdateCashboxDialog((prev) => !prev);
                        }}
                    >
                        <PencilIcon className="h-4 w-4 text-muted-foreground" />
                        Update
                    </DropdownMenuItem>

                </DropdownMenuContent>
            </DropdownMenu>
        </>
    );
}

export default CashboxOverviewCard;