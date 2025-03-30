"use client";

import SkeletonWrapper from "@/components/custom/SkeletonWrapper";
import { Card } from "@/components/ui/card";
import { getBalanceStats } from "@/data/services/stats-service";
import { GetFormatterForCurrency } from "@/lib/helpers";
import { Currency } from "@/lib/types";
import { useQuery } from "@tanstack/react-query";
import { TrendingDown, TrendingUp, Wallet } from "lucide-react";
import React, { ReactNode, useCallback, useMemo } from "react"
import CountUp from "react-countup"

interface Props {
    from: Date;
    to: Date;
    userCurrency: Currency;
}

function StatsCards({ userCurrency, from, to }: Props) {
    const statsQuery = useQuery({
        queryKey: ["overview", "stats", from, to],
        queryFn: () => getBalanceStats(from, to),
    });

    const formatter = useMemo(()=>{
        return GetFormatterForCurrency(userCurrency.code);
    }, [userCurrency.code])

    const income = statsQuery.data?.income || 0;
    const expense = statsQuery.data?.expense || 0;
    const balance = income - expense;
    return (

        <div className="relative flex w-full flex-wrap gap-2 md:flex-nowrap">
            <SkeletonWrapper isLoading={statsQuery.isFetching}>
                <StatCard
                    formatter={formatter}
                    value={income}
                    title="Income"
                    icon={
                        <TrendingUp className="h-12 w-12 items-center rounded-lg p-2 flex text-emerald-500 bg-emerald-400/10" />
                    }
                />
            </SkeletonWrapper>

            <SkeletonWrapper isLoading={statsQuery.isFetching}>
                <StatCard
                    formatter={formatter}
                    value={expense}
                    title="Expense"
                    icon={
                        <TrendingDown className="h-12 w-12 items-center rounded-lg p-2 flex text-red-500 bg-red-400/10" />
                    }
                />
            </SkeletonWrapper>

            <SkeletonWrapper isLoading={statsQuery.isFetching}>
                <StatCard
                    formatter={formatter}
                    value={balance}
                    title="Balance"
                    icon={
                        <Wallet className="h-12 w-12 items-center rounded-lg p-2 flex text-violet-500 bg-violet-400/10" />
                    }
                />
            </SkeletonWrapper>
        </div>
    )
}

export default StatsCards;

function StatCard({ icon, title, value, formatter }: {
    formatter: Intl.NumberFormat,
    icon: ReactNode,
    title: string,
    value: number
}) {
    const formatFn = useCallback(
        (value: number) => {
            return formatter.format(value);
        },
        [formatter]
    );

    return (
        <Card className="flex flex-row h-24 w-full items-center gap-2 p-4">
            {icon}
            <div className="flex flex-col items-start gap-0">
                <p className="text-muted-foreground">{title}</p>
                <CountUp
                    preserveValue
                    redraw={false}
                    end={value}
                    decimals={2}
                    formattingFn={formatFn}
                    className="text-2xl"
                />
            </div>
        </Card>
    );
}