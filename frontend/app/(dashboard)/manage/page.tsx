"use client";

import SkeletonWrapper from "@/components/custom/SkeletonWrapper";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { getUserCategoriesByType } from "@/data/services/category-service";
import { Category, TransactionType } from "@/lib/types";
import { useQuery, useQueryClient } from "@tanstack/react-query";
import { CircleOff, PlusSquare, TrashIcon, TrendingDown, TrendingUp, PencilIcon } from "lucide-react";
import React, { useEffect, useState } from "react";
import CreateCategoryDialog from "../_components/CreateCategoryDialog";
import { Button } from "@/components/ui/button";
import { Separator } from "@/components/ui/separator";
import { cn } from "@/lib/utils";
import DeleteCategoryDialog from "../_components/DeleteCategoryDialog";
import { CurrencyComboBox } from "@/components/custom/CurrencyComboBox";
import { useUser } from "@/context/UserContext";
import { redirect } from "next/navigation";
import Cookies from "js-cookie";
import UpdateCategoryDialog from "../_components/UpdateCategoryDialog";
import CashboxOverviewCard from "../_components/CashboxOverviewCard";

function Page() {
    const { user, isLoading } = useUser();
    const [hasCheckedCookie, setHasCheckedCookie] = useState(false);

    useEffect(() => {
        if (!isLoading) {
            const userDataString = Cookies.get("userData");
            if (!userDataString) {
                redirect("/signin");
            }
            setHasCheckedCookie(true);
        }
    }, [isLoading]);

    if (isLoading || !hasCheckedCookie) {
        return (
            <div className="flex h-screen w-full items-center justify-center">
                <div className="text-center">
                    <p className="text-lg font-semibold">Loading...</p>
                    <p className="text-sm text-muted-foreground">Please wait while we load your data</p>
                </div>
            </div>
        );
    }

    if (!user) {
        redirect("/signin");
    }
    console.log("user"+user.currency.code);
    
    return (
        <>
            {/* HEADER */}
            <div className="border-b bg-card">
                <div className="container mx-auto flex flex-wrap items-center justify-between gap-6 py-8">
                    <div>
                        <p className="text-3xl font-bold">Manage</p>
                        <p className="text-muted-foreground">
                            Manage your account settings and categories
                        </p>
                    </div>
                </div>
            </div>
            {/* END HEADER */}
            <div className="container mx-auto flex flex-col gap-4 p-4">
                <div className="flex flex-col md:flex-row gap-4">
                    <Card className="bg-card text-card-foreground flex flex-col gap-6 rounded-xl border py-6 shadow-sm flex-1">
                        <CardHeader>
                            <CardTitle>Currency</CardTitle>
                            <CardDescription>
                                Set your default currency for transactions
                            </CardDescription>
                        </CardHeader>
                        <CardContent>
                            <CurrencyComboBox baseCurrency={user.currency} />
                        </CardContent>
                    </Card>
                    <CashboxOverviewCard user={user} />
                    {/* <Card className="flex-1">
                        <CardHeader>
                            <CardTitle>Cashboxes</CardTitle>
                            <CardDescription>
                                Manage your cashboxes and their balances
                            </CardDescription>
                        </CardHeader>
                        <CardContent>
                            <CashboxOverviewCard />
                        </CardContent>
                    </Card> */}
                </div>
                <CategoryList type="income" />
                <CategoryList type="expense" />
            </div>
        </>
    );
}

export default Page;


function CategoryList({ type }: { type: TransactionType }) {
    const queryClient = useQueryClient();

    const categoriesQuery = useQuery({
        queryKey: ["categories", type],
        queryFn: () => getUserCategoriesByType(type),
    });
    const dataAvailable = categoriesQuery.data && categoriesQuery.data.length > 0;

    const onDelete = (id: string) => {
        queryClient.setQueryData<Category[]>(["categories", type], (oldData) =>
            oldData?.filter((cat) => cat.id !== id) ?? []
        );
    };

    return (
        <SkeletonWrapper isLoading={categoriesQuery.isLoading}>
            <Card>
                <CardHeader>
                    <CardTitle className="flex items-center justify-between gap-2">
                        <div className="flex items-center gap-2">
                            {type === "expense" ? (
                                <TrendingDown className="h-12 w-12 items-center rounded-lg bg-red-400/10 p-2 text-red-500" />
                            ) : (
                                <TrendingUp className="h-12 w-12 items-center rounded-lg bg-emerald-400/10 p-2 text-emerald-500" />
                            )}
                            <div>
                                {type === "income" ? "Incomes" : "Expenses"}
                                <div className="text-sm text-muted-foreground">
                                    Sorted by name
                                </div>
                            </div>
                        </div>

                        <CreateCategoryDialog type={type}
                            successCallback={() => categoriesQuery.refetch()}
                            trigger={
                                <Button className="gap-2 text-sm">
                                    <PlusSquare className="h-4 w-4" />
                                    Create category
                                </Button>
                            }
                        />
                    </CardTitle>
                </CardHeader>
                <Separator />

                {!dataAvailable && (
                    <div className="flex h-40 w-full flex-col items-center justify-center">
                        <p>
                            No
                            <span
                                className={cn(
                                    "ml-1",
                                    type === "income" ? "text-emerald-500" : "text-red-500"
                                )}
                            >
                                {type}
                            </span>
                            categories yet
                        </p>

                        <p className="text-sm text-muted-foreground">
                            Create one to get started
                        </p>
                    </div>
                )}
                {dataAvailable && (
                    <div className="grid grid-flow-row gap-2 p-2 sm:grid-flow-row sm:grid-cols-2 md:grid-cols-3 lg:grid-cols-4">
                        {categoriesQuery.data.map((category: Category) => (
                            <CategoryCard category={category} key={category.name}
                            onDelete={onDelete}
                            
                            />
                            
                        ))}
                    </div>
                )}
            </Card>
        </SkeletonWrapper>
    );
}

function CategoryCard({ category, onDelete }: { category: Category, onDelete: (id: string) => void }) {
    const [showUpdateDialog, setShowUpdateDialog] = useState(false);
    const [showDeleteDialog, setShowDeleteDialog] = useState(false);

    return (
        <div className="flex flex-col justify-between rounded-md border shadow-md shadow-black/10 dark:shadow-white/10">
            <div className="flex flex-col items-center gap-2 p-4">
                {category.icon ? (
                    <span className="text-3xl" role="img">
                        {category.icon}
                    </span>
                ) : (
                    <CircleOff className="text-5xl h-[48px] w-[48px]" />
                )}
                <span>{category.name}</span>
            </div>
            <div className="flex border-t">
                <Button
                    className="flex-1 items-center gap-2 rounded-none text-muted-foreground hover:bg-blue-500/20"
                    variant={"secondary"}
                    onClick={() => setShowUpdateDialog(true)}
                >
                    <PencilIcon className="h-4 w-4" />
                    Update
                </Button>
                <Button
                    className="flex-1 items-center gap-2 rounded-none text-muted-foreground hover:bg-red-500/20"
                    variant={"secondary"}
                    onClick={() => setShowDeleteDialog(true)}
                >
                    <TrashIcon className="h-4 w-4" />
                    Delete
                </Button>
            </div>
            <UpdateCategoryDialog 
                category={category}
                open={showUpdateDialog}
                setOpen={setShowUpdateDialog}
            />
            <DeleteCategoryDialog 
                category={category}
                open={showDeleteDialog}
                setOpen={setShowDeleteDialog}
                successCallback={() => onDelete(category.id)}
            />
        </div>
    );
}



