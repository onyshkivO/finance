"use client";

import { fetchUserTransactionsByDateRange } from "@/data/services/transaction-service";
import { Transaction } from "@/lib/types";
import { useQuery } from "@tanstack/react-query";
import {
    ColumnDef,
    ColumnFiltersState,
    flexRender,
    getCoreRowModel,
    getFilteredRowModel,
    getPaginationRowModel,
    getSortedRowModel,
    SortingState,
    useReactTable,
  } from "@tanstack/react-table"
   
  import {
    Table,
    TableBody,
    TableCell,
    TableHead,
    TableHeader,
    TableRow,
  } from "@/components/ui/table"
import SkeletonWrapper from "@/components/custom/SkeletonWrapper";
import { DataTableColumnHeader } from "@/components/datatable/CoulumnHeader";
import { useMemo, useState } from "react";
import { cn } from "@/lib/utils";
import { DataTableFacetedFilter } from "@/components/datatable/FacetedFilters";
import { DataTableViewOptions } from "@/components/datatable/ColumnToggle";
import { Button } from "@/components/ui/button";
import {download, generateCsv, mkConfig} from "export-to-csv";
import { DownloadIcon, MoreHorizontal, TrashIcon, PencilIcon } from "lucide-react";
import { DropdownMenu, DropdownMenuContent, DropdownMenuItem, DropdownMenuLabel, DropdownMenuSeparator, DropdownMenuTrigger } from "@/components/ui/dropdown-menu";
import DeleteTransactionDialog from "./DeleteTransactionDialog";
import UpdateTransactionDialog from "./UpdateTransactionDialog";


interface Props {
    from: Date;
    to: Date;
}

/* eslint-disable */
const emptyData: any[] = [];

const columns: ColumnDef<Transaction>[] = [
    {
        accessorKey: "category",
        header: ({column})=>(
            <DataTableColumnHeader
            column={column}
            title="Category"
            />
        ),
        filterFn: (row, columnId, filterValue) => {
            if (!filterValue.length) return true;
            return filterValue.includes(row.original.category.id);
        },
        cell: ({ row }) => (
            <div className="flex gap-2 capitalize">
                {row.original.category.icon}
                <div className="capitalize">{row.original.category.name}</div>
            </div>
        ),
        sortingFn: (rowA, rowB) => {
            const nameA = rowA.original.category.name.toLowerCase();
            const nameB = rowB.original.category.name.toLowerCase();
            return nameA.localeCompare(nameB);
        }
    },
    {
        accessorKey: "description",
        header: ({column})=>(
            <DataTableColumnHeader
            column={column}
            title="Description"
            />
        ),
        cell: ({ row }) => (
            <div className="capitalize">
                {row.original.description}
            </div>
        ),
    },
    {
        accessorKey: "date",
        header: ({ column }) => (
            <DataTableColumnHeader column={column} title="Date" />
        ),
        cell: ({ row }) => {
            const dateStr = row.original.transactionDate;
            try {
                const [day, month, year] = dateStr.split('-');
                const date = new Date(`${year}-${month}-${day}`);
                const formattedDate = date.toLocaleDateString("en-GB", {
                    timeZone: "UTC",
                    day: "2-digit",
                    month: "2-digit",
                    year: "numeric"
                });
                return <div className="text-muted-foreground">{formattedDate}</div>;
            } catch (error) {
                return <div className="text-muted-foreground">{dateStr}</div>;
            }
        },
        sortingFn: (rowA, rowB) => {
            const dateA = new Date(rowA.original.transactionDate.split('-').reverse().join('-'));
            const dateB = new Date(rowB.original.transactionDate.split('-').reverse().join('-'));
            return dateA.getTime() - dateB.getTime();
        }
    },
    {
        accessorKey: "type",
        filterFn: (row, columnId, filterValue) => {
          return filterValue.includes(row.getValue(columnId));
        },
        header: ({ column }) => (
            <DataTableColumnHeader column={column} title="Type" />
        ),
        cell: ({ row }) => (
    <div
        className={cn(
            "capitalize rounded-lg text-center p-2",
            row.original.type === "income" && 
                "bg-emerald-400/10 text-emerald-500",
            row.original.type === "expense" && 
                "bg-red-400/10 text-red-500"
        )}>
        {row.original.type}
    </div>
    ),
    },
    {accessorKey: "amount",
        header: ({ column }) => (
            <DataTableColumnHeader column={column} title="Amount" />
        ),
        cell: ({ row }) => (
            <p className="text-md rounded-lg bg-gray-400/5 p-2 text-center font-medium">
                {row.original.formatedAmount}
            </p>
        ),
    },
    {
      id: "actions",
      enableHiding: false,
      cell: ({ row }) => {
        return <RowActions transaction={row.original} />;
      }
    }
]

const csvConfig = mkConfig({
    fieldSeparator: ',',
    decimalSeparator: '.',
    useKeysAsHeaders: true,
    showColumnHeaders: true,
    useBom: true,
    useTextFile: false as const,
    filename: 'transactions_' + new Date().toISOString().split('T')[0],
    quoteStrings: true
});


function TransactionsTable({ from, to }: Props) {
    const [sorting, setSorting] = useState<SortingState>([]);
    const [columnFilters, setColumnFilters] = useState<ColumnFiltersState>([]);
    const history = useQuery({
        queryKey: ["transactions","history", from, to],
        queryFn: () => fetchUserTransactionsByDateRange(from, to)
    });

    const handleExportCsv = (exportData: any[]) => {
        const csv = generateCsv(csvConfig)(exportData);
        download(csvConfig)(csv);
    }

    const table = useReactTable({
        data: history.data || emptyData,
        columns,
        getCoreRowModel: getCoreRowModel(),
        state:{
            sorting,
            columnFilters
        },
        onColumnFiltersChange: setColumnFilters,
        getFilteredRowModel: getFilteredRowModel(),
        onSortingChange: setSorting,
        getSortedRowModel: getSortedRowModel(),
        getPaginationRowModel: getPaginationRowModel(),
    });
    
    const categoriesOptions = useMemo(() => {
      const categoriesMap = new Map();
      history.data?.forEach((transaction) => {
          categoriesMap.set(transaction.category.id, {
              value: transaction.category.id,
              label: `${transaction.category.icon} ${transaction.category.name}`,
          });
      });
      const uniqueCategories = new Set(categoriesMap.values());
      return Array.from(uniqueCategories);
  }, [history.data]);

    return (
        <div className="w-full">
            <div className="flex flex-wrap items-end justify-between gap-2 py-4">
              <div className="flex gap-2">
              {table.getColumn("category") && (
                <DataTableFacetedFilter 
                title="Category" 
                column={table.getColumn("category")} 
                options={categoriesOptions}
              />
            )}
            {table.getColumn("type") && (
                <DataTableFacetedFilter 
                title="Type" 
                column={table.getColumn("type")} 
                options={[
                  {label: "Income", value: "income"},
                  {label: "Expense", value: "expense"},
                ]}
              />
            )}
              </div>
              <div className="flex flex-wrap gap-2">
              <Button
                variant={"outline"}
                size={"sm"}
                className="ml-auto h-8 lg:flex"
                onClick={() => {
                    const data = table.getFilteredRowModel().rows.map(row => ({
                        Category: row.original.category.name || '',
                        'Category Icon': row.original.category.icon || '',
                        Description: row.original.description || '',
                        Type: row.original.type || '',
                        Amount: row.original.amount?.toString() || '',
                        'Formatted Amount': row.original.formatedAmount || '',
                        Date: row.original.transactionDate || ''
                    }));
                    handleExportCsv(data);
                }}
              >
               <DownloadIcon className="mr-2 w-4 h-4"/>
               Export CSV
              </Button>
                <DataTableViewOptions table={table} />
              </div>
            </div>
            <SkeletonWrapper isLoading={history.isLoading}>
                <div className="rounded-md border">
                    <Table>
                        <TableHeader>
                            {table.getHeaderGroups().map((headerGroup) => (
                                <TableRow key={headerGroup.id}>
                                    {headerGroup.headers.map((header) => (
                                        <TableHead key={header.id} className="text-center">
                                            {header.isPlaceholder
                                                ? null
                                                : flexRender(
                                                    header.column.columnDef.header,
                                                    header.getContext()
                                                )}
                                        </TableHead>
                                    ))}
                                </TableRow>
                            ))}
                        </TableHeader>
                        <TableBody>
                            {table.getRowModel().rows?.length ? (
                                table.getRowModel().rows.map((row) => (
                                    <TableRow
                                        key={row.id}
                                        data-state={row.getIsSelected() && "selected"}
                                    >
                                        {row.getVisibleCells().map((cell) => (
                                            <TableCell key={cell.id} className="text-center">
                                                {flexRender(cell.column.columnDef.cell, cell.getContext())}
                                            </TableCell>
                                        ))}
                                    </TableRow>
                                ))
                            ) : (
                                <TableRow>
                                    <TableCell colSpan={columns.length} className="h-24 text-center">
                                        No results.
                                    </TableCell>
                                </TableRow>
                            )}
                        </TableBody>
                    </Table>
                </div>
                <div className="flex items-center justify-end space-x-2 py-4">
                <Button
                  variant="outline"
                  size="sm"
                  onClick={() => table.previousPage()}
                  disabled={!table.getCanPreviousPage()}
                >
                  Previous
                </Button>
                <Button
                  variant="outline"
                  size="sm"
                  onClick={() => table.nextPage()}
                  disabled={!table.getCanNextPage()}
                >
                  Next
                </Button>
              </div>
            </SkeletonWrapper>
        </div>
    );
}

export default TransactionsTable;

function RowActions({ transaction }: { transaction: Transaction }) {
  const [showDeleteDialog, setShowDeleteDialog] = useState(false);
  const [showUpdateDialog, setShowUpdateDialog] = useState(false);

  return (
      <>
          <DeleteTransactionDialog 
              open={showDeleteDialog} 
              setOpen={setShowDeleteDialog} 
              transactionId={transaction.id} 
          />
          <UpdateTransactionDialog 
              transaction={transaction}
              open={showUpdateDialog}
              setOpen={setShowUpdateDialog}
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
                          setShowUpdateDialog((prev) => !prev);
                      }}
                  >
                      <PencilIcon className="h-4 w-4 text-muted-foreground" />
                      Update
                  </DropdownMenuItem>
                  <DropdownMenuItem
                      className="flex items-center gap-2"
                      onSelect={() => {
                          setShowDeleteDialog((prev) => !prev);
                      }}
                  >
                      <TrashIcon className="h-4 w-4 text-muted-foreground" />
                      Delete
                  </DropdownMenuItem>
              </DropdownMenuContent>
          </DropdownMenu>
      </>
  );
}