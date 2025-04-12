"use client";

import { UserData } from "@/lib/types";
import React, { createContext, useContext, useEffect, useState } from "react";

const UserContext = createContext<{
    user: UserData | null;
    setUser: (user: UserData | null) => void;
    isLoading: boolean;
}>({
    user: null,
    setUser: () => {},
    isLoading: true,
});

export function UserProvider({ children }: { children: React.ReactNode }) {
    const [user, setUser] = useState<UserData | null>(null);
    const [isLoading, setIsLoading] = useState(true);

    useEffect(() => {
        function getCookie(name: string) {
            const match = document.cookie.match(new RegExp("(^| )" + name + "=([^;]+)"));
            return match ? decodeURIComponent(match[2]) : null;
        }

        const storedUserData = getCookie("userData");
        if (storedUserData) {
            try {
                const parsedUser: UserData = JSON.parse(storedUserData);
                setUser(parsedUser);
            } catch (error) {
                console.error("Failed to parse userData:", error);
            }
        }
        setIsLoading(false); // done loading
    }, []);

    return (
        <UserContext.Provider value={{ user, setUser, isLoading }}>
            {children}
        </UserContext.Provider>
    );
}

export function useUser() {
    return useContext(UserContext);
}
