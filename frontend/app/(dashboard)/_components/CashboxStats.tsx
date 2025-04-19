"use client";

import SkeletonWrapper from "@/components/custom/SkeletonWrapper";
import { Card, CardHeader, CardTitle } from "@/components/ui/card";
import { Progress } from "@/components/ui/progress";
import { ScrollArea } from "@/components/ui/scroll-area";
import { getCashboxStats } from "@/data/services/stats-service";
import { GetFormatterForCurrency } from "@/lib/helpers";
import { CashboxStatsType, Currency, TransactionType } from "@/lib/types";
import { useQuery } from "@tanstack/react-query";
import { useMemo } from "react";

interface Props {
    from: Date;
    to: Date;
    userCurrency: Currency;
}

function CashboxStats({ userCurrency, from, to }: Props) {
    const statsQuery = useQuery({
        queryKey: ["overview", "stats", "cashbox", from, to],
        queryFn: () => getCashboxStats(from, to),
    });

    const formatter = useMemo(() => {
        return GetFormatterForCurrency(userCurrency.code);
    }, [userCurrency.code])

    return (
        <div className="flex w-full flex-wrap gap-2 md:flex-nowrap">
            <SkeletonWrapper isLoading={statsQuery.isFetching}>
                <CashboxCard
                    formatter={formatter}
                    type="income"
                    data={statsQuery.data || []}
                />
            </SkeletonWrapper>

            <SkeletonWrapper isLoading={statsQuery.isFetching}>
                <CashboxCard
                    formatter={formatter}
                    type="expense"
                    data={statsQuery.data || []}
                />
            </SkeletonWrapper>
        </div>
    );
}

export default CashboxStats;

function CashboxCard({
    data,
    type,
    formatter,
}: {
    type: TransactionType;
    formatter: Intl.NumberFormat;
    data: CashboxStatsType[];
}) {
    const filteredData = data.filter((el) => el.type === type);
    const total = filteredData.reduce(
        (acc, el) => acc + (el.amount || 0),
        0
    );

    return (
        <Card className="h-80 w-full col-span-6">
            <CardHeader>
                <CardTitle className="grid grid-flow-row justify-between gap-2 text-muted-foreground md:grid-flow-col">
                    {type === "income" ? "Incomes" : "Expenses"} by cashbox
                </CardTitle>
            </CardHeader>

            <div className="flex items-center justify-between gap-2">
                {filteredData.length == 0 && (
                    <div className="flex h-60 w-full flex-col items-center justify-center">
                        No data for the selected period
                        <p className="text-sm text-muted-foreground">
                            Try selecting a different period or try adding new{" "}
                            {type == "income" ? "incomes" : "expenses"}
                        </p>
                    </div>
                )}
                
                {filteredData.length > 0 && (
                <ScrollArea className="h-60 w-full px-4">
                    <div className="flex w-full flex-col gap-4 p-4">
                        {filteredData.map((item) => {
                            const amount = item.amount || 0;
                            const percentage = (amount * 100) / (total || amount);

                            return (
                                <div key={item.cashbox} className="flex flex-col gap-2">
                                    <div className="flex items-center justify-between">
                                        <span className="flex items-center text-gray-400">
                                            {item.cashbox}
                                            <span className="ml-2 text-xs text-muted-foreground">
                                                ({percentage.toFixed(0)}%)
                                            </span>
                                        </span>
                                        <span className="text-sm text-gray-400">
                                            {formatter.format(amount)}
                                        </span>
                                    </div>
                                    <Progress
                                    value= {percentage}
                                    indicator={
                                        type ==="income"?
                                        "bg-emerald-500":
                                        "bg-red-500"
                                    }/>
                                </div>
                            );
                        })}
                    </div>
                </ScrollArea>
                )}
            </div>
        </Card>
    );
}