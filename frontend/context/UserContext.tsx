"use client";

import { UserData } from "@/lib/types";
import React, { createContext, useContext, useEffect, useState } from "react";

// Create Context
const UserContext = createContext<{
    user: UserData | null;
    setUser: (user: UserData | null) => void;
}>({
    user: null,
    setUser: () => {},
});

// Create Provider Component
export function UserProvider({ children }: { children: React.ReactNode }) {
    const [user, setUser] = useState<UserData | null>(null);

    // Load user data from cookies/localStorage on mount
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
    }, []);

    return <UserContext.Provider value={{ user, setUser }}>{children}</UserContext.Provider>;
}

// Custom Hook for Using User Context
export function useUser() {
    return useContext(UserContext);
}