"use client";

import React from "react";
import { useUser } from "@/context/UserContext";
import Link from "next/link";
import { buttonVariants } from "@/components/ui/button";
import { cn } from "@/lib/utils";
import { DropdownMenu, DropdownMenuContent, DropdownMenuItem, DropdownMenuTrigger } from "@/components/ui/dropdown-menu";
import { Avatar, AvatarFallback } from "@/components/ui/avatar";
import { logoutAction } from "@/data/actions/auth-actions";

function UserIcon() {
    const { user } = useUser();

    return (
        <div className="flex items-center">
            {user  ? (
                <DropdownMenu>
                    <DropdownMenuTrigger className="flex items-center gap-2 cursor-pointer">
                        <Avatar>
                            <AvatarFallback>{user.login.charAt(0).toUpperCase()}</AvatarFallback>
                        </Avatar>
                        <span className="text-lg font-medium text-muted-foreground">{user.login}</span>
                    </DropdownMenuTrigger>
                    <DropdownMenuContent align="end">
                        <DropdownMenuItem asChild className="cursor-pointer">
                            <Link href="/profile">Profile</Link>
                        </DropdownMenuItem>
                        <DropdownMenuItem onClick={logoutAction} className="cursor-pointer">
                            Logout
                        </DropdownMenuItem>
                    </DropdownMenuContent>
                </DropdownMenu>
            ) : (
                <Link
                    href="/signin"
                    className={cn(buttonVariants({ variant: "outline" }))}
                >
                    Sign In
                </Link>
            )}
        </div>
    );
}

export default UserIcon;
