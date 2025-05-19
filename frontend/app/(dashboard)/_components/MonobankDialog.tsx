"use client";

import { Dialog, DialogTrigger, DialogContent, DialogHeader, DialogTitle } from "@/components/ui/dialog";
import { useQuery, UseQueryOptions, useMutation, useQueryClient } from "@tanstack/react-query";
import { ReactNode, useState } from "react";
import { getAccessUrl, getMonobankCards, activateMonobankCard, deactivateMonobankCard } from "@/data/services/monobank-service";
import { MononbakAuth, MonobankAccount } from "@/lib/types";
import { QRCodeSVG } from "qrcode.react";
import { Button } from "@/components/ui/button";
import { toast } from "sonner";
import { RefreshCw, Power } from "lucide-react";
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from "@/components/ui/table";

interface Props {
    trigger: ReactNode;
}

function MonobankDialog({ trigger }: Props) {
    const [open, setOpen] = useState(false);
    const [togglingCardId, setTogglingCardId] = useState<string | null>(null);
    const queryClient = useQueryClient();

    const queryOptions: UseQueryOptions<MononbakAuth, Error> = {
        queryKey: ['monobank-auth'],
        queryFn: () => getAccessUrl(),
        enabled: open,
        retry: false,
    };

    const { data: authData, isLoading, error, refetch: refetchAuth } = useQuery(queryOptions);

    const cardsQueryOptions: UseQueryOptions<MonobankAccount[], Error> = {
        queryKey: ['monobank-cards'],
        queryFn: () => getMonobankCards(),
        enabled: open && authData?.isAccepted === true,
        retry: false,
    };

    const { data: cardsData, isLoading: isLoadingCards, refetch: refetchCards } = useQuery(cardsQueryOptions);

    const activateMutation = useMutation({
        mutationFn: activateMonobankCard,
        onSuccess: () => {
            toast.success("Card monitoring activated");
            queryClient.invalidateQueries({ queryKey: ['monobank-cards'] });
        },
        onError: (error) => {
            toast.error("Failed to activate card monitoring. Please try again later");
            console.error(error);
        },
        onSettled: () => {
            setTogglingCardId(null);
        }
    });

    const deactivateMutation = useMutation({
        mutationFn: deactivateMonobankCard,
        onSuccess: () => {
            toast.success("Card monitoring deactivated");
            queryClient.invalidateQueries({ queryKey: ['monobank-cards'] });
        },
        onError: (error) => {
            toast.error("Failed to deactivate card monitoring. Please try again later");
            console.error(error);
        },
        onSettled: () => {
            setTogglingCardId(null);
        }
    });

    if (error) {
        toast.error("Failed to get Monobank access URL");
        console.error(error);
    }

    const handleReload = () => {
        refetchAuth();
        if (authData?.isAccepted) {
            refetchCards();
        }
    };

    const handleToggleMonitoring = (cardId: string, isMonitoring: boolean) => {
        setTogglingCardId(cardId);
        if (isMonitoring) {
            deactivateMutation.mutate(cardId);
        } else {
            activateMutation.mutate(cardId);
        }
    };

    return (
        <Dialog open={open} onOpenChange={setOpen}>
            <DialogTrigger asChild>{trigger}</DialogTrigger>
            <DialogContent>
                <DialogHeader>
                    <DialogTitle className="flex items-center justify-between">
                        <span>
                            {authData?.isAccepted === false
                                ? "Connect Monobank Account"
                                : "Your Monobank Cards"}
                        </span>
                        <Button
                            variant="ghost"
                            size="icon"
                            onClick={handleReload}
                            disabled={isLoading || isLoadingCards}
                        >
                            <RefreshCw className={`h-4 w-4 ${isLoading || isLoadingCards ? 'animate-spin' : ''}`} />
                        </Button>
                    </DialogTitle>
                </DialogHeader>
                {isLoading ? (
                    <div className="flex justify-center items-center p-4">
                        <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-gray-900"></div>
                    </div>
                ) : error ? (
                    <div className="text-center text-red-500">
                        Failed to load Monobank authorization data
                    </div>
                ) : authData && !authData.isAccepted ? (
                    <div className="space-y-4">
                        <div className="text-center">
                            <p className="mb-4">Please scan the QR code or click the link below to connect your Monobank account:</p>
                            <div className="flex justify-center mb-4">
                                <div className="bg-white p-4 rounded-md shadow-md">
                                    <QRCodeSVG value={authData.confirmUrl} size={200} />
                                </div>
                            </div>
                            <Button
                                variant="outline"
                                onClick={() => window.open(authData.confirmUrl, '_blank')}
                                className="w-full"
                            >
                                Open Monobank
                            </Button>
                        </div>
                    </div>
                ) : isLoadingCards ? (
                    <div className="flex justify-center items-center p-4">
                        <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-gray-900"></div>
                    </div>
                ) : cardsData ? (
                    <div className="space-y-4">
                        <div className="rounded-md border">
                            <Table>
                                <TableHeader>
                                    <TableRow>
                                        <TableHead>Type</TableHead>
                                        <TableHead>Currency</TableHead>
                                        <TableHead>Card Number</TableHead>
                                        <TableHead className="text-right">Monitoring</TableHead>
                                    </TableRow>
                                </TableHeader>
                                <TableBody>
                                    {cardsData.map((card) => (
                                        <TableRow key={card.id}>
                                            <TableCell>{card.type}</TableCell>
                                            <TableCell>{card.currencyCode}</TableCell>
                                            <TableCell>{card.maskedPan}</TableCell>
                                            <TableCell className="text-right">
                                                <Button
                                                    variant="ghost"
                                                    size="icon"
                                                    onClick={() => handleToggleMonitoring(card.id, card.isMonitoring)}
                                                    disabled={togglingCardId === card.id}
                                                    className={card.isMonitoring ? "text-green-500 hover:text-green-600" : "text-red-500 hover:text-red-600"}
                                                >
                                                    <Power className={`h-4 w-4 ${togglingCardId === card.id ? 'animate-spin' : ''}`} />
                                                </Button>
                                            </TableCell>
                                        </TableRow>
                                    ))}
                                </TableBody>
                            </Table>
                        </div>
                    </div>
                ) : null}
            </DialogContent>
        </Dialog>
    );
}

export default MonobankDialog;
