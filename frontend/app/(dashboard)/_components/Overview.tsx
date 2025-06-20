"use client";

import { DateRangePicker } from '@/components/ui/date-range-picker';
import { differenceInDays, startOfMonth } from 'date-fns';
import React, { useState } from 'react'
import { toast } from 'sonner';
import StatsCards from './StatsCards';
import { Currency } from '@/lib/types';
import { MAX_DATE_RANGE_DAYS } from '@/lib/constants';
import CategoryStats from './CategoryStats';
import CashboxStats from './CashboxStats';
function Overview({ userCurrency }: { userCurrency: Currency }) {
    const [dateRange, setDateRange] = useState<{ from: Date; to: Date }>({
        from: startOfMonth(new Date()),
        to: new Date(),
    });

    return (
        <>
            <div className="container mx-auto flex flex-wrap items-end justify-between gap-2 py-6">
                <h2 className="text-3xl font-bold">Overview</h2>
                <div className="flex items-center gap-3">
                    <DateRangePicker
                        initialDateFrom={dateRange.from}
                        initialDateTo={dateRange.to}
                        showCompare={false}
                        onUpdate={(values) => {
                            const { from, to } = values.range;
                            // We update the date range only if both dates are set

                            if (!from || !to) return;
                            if (differenceInDays(to, from) > MAX_DATE_RANGE_DAYS) {
                                toast.error(
                                    `The selected date range is too big. Max allowed range is ${MAX_DATE_RANGE_DAYS} days!`
                                );
                                return;
                            }
                            setDateRange({ from, to });
                        }}
                    />
                </div>
            </div>
            <div className='container mx-auto flex flex-wrap items-end justify-between gap-2 py-6'>
                <StatsCards
                    userCurrency={userCurrency}
                    from={dateRange.from}
                    to={dateRange.to} />

                <CategoryStats
                    userCurrency={userCurrency}
                    from={dateRange.from}
                    to={dateRange.to} />
                    
                <CashboxStats
                    userCurrency={userCurrency}
                    from={dateRange.from}
                    to={dateRange.to} />
            </div>
        </>
    )
}

export default Overview