"use server";
import { z } from "zod";
import { cookies } from "next/headers";
import { registerUserService, loginUserService } from "@/data/services/auth-service";
import { redirect } from "next/navigation";
import { CURRENCIES } from "@/lib/types";

const config = {
    maxAge: 60 * 60 * 24 * 7,
    path: "/",
    // domain: "localhost",
    secure: process.env.NODE_ENV === "development",
};

const schemaRegister = z.object({
    login: z
        .string()
        .min(4, { message: "User login should be between 5 and 20 symbols" })
        .max(20, { message: "User login should be between 5 and 20 symbols" })
        .regex(/^(?=.{4,20}$)(?![_.])(?!.*[_.]{2})[a-zA-Z0-9._]+(?<![_.])$/, {
            message: "Bad user login",
        }),
    email: z
        .string()
        .email({ message: "Email should be in email format" })
        .min(5, { message: "User email should be between 4 and 50 symbols" })
        .max(50, { message: "User email should be between 4 and 50 symbols" })
        .nonempty({ message: "Email should exist" }),
    password: z
        .string()
        .min(6, { message: "User password should be between 6 and 50 symbols" })
        .max(50, { message: "User password should be between 6 and 50 symbols" })
        .regex(/^[$\/A-Za-z0-9_-]{6,60}$/, { message: "Bad password format" })
        .nonempty({ message: "Password should exist" }),
});

const schemaLogin = z.object({
    login: z
        .string()
        .min(4, { message: "User login should be between 5 and 20 symbols" })
        .max(20, { message: "User login should be between 5 and 20 symbols" })
        .regex(/^(?=.{4,20}$)(?![_.])(?!.*[_.]{2})[a-zA-Z0-9._]+(?<![_.])$/, {
            message: "Bad user login",
        }),
    password: z
        .string()
        .min(6, { message: "User password should be between 6 and 50 symbols" })
        .max(50, { message: "User password should be between 6 and 50 symbols" })
        .regex(/^[$\/A-Za-z0-9_-]{6,60}$/, { message: "Bad password format" })
});
// eslint-disable-next-line @typescript-eslint/no-explicit-any
export async function registerUserAction(prevState: any, formData: FormData) {
    const enteredData = {
        login: formData.get("login") as string,
        password: formData.get("password") as string,
        email: formData.get("email") as string,
    };

    const validation = schemaRegister.safeParse(enteredData);

    if (!validation.success) {
        return {
            ...prevState,
            zodErrors: validation.error.flatten().fieldErrors,
            authErrors: null,
            message: "Missing Fields. Failed to Register.",
            inputs: enteredData,
        };
    }

    const response = await registerUserService(validation.data);

    if (!response || response.error) {
        return {
            ...prevState,
            authErrors: response?.error || null,
            zodErrors: null,
            message: response?.error?.message || "Ops! Something went wrong. Please try again.",
            inputs: enteredData,
        };
    }
    const userCurrency = CURRENCIES.find(c => c.code === response.data.currency) || { code: response.data.currency, name: "Unknown Currency" };

    const userData = {
        jwtToken: response.data.token,
        login: response.data.login,
        currency: userCurrency,
        id: response.data.id
    };
    const cookieStore = await cookies();
    cookieStore.set("userData", JSON.stringify(userData), config);

    return {
        success: true,
        data: response.data
      };
    // redirect("/");
}
// eslint-disable-next-line @typescript-eslint/no-explicit-any
export async function loginUserAction(prevState: any, formData: FormData) {
    const enteredData = {
        login: formData.get("login") as string,
        password: formData.get("password") as string,
    };

    const validation = schemaLogin.safeParse(enteredData);

    if (!validation.success) {
        return {
            ...prevState,
            zodErrors: validation.error.flatten().fieldErrors,
            authErrors: null,
            message: "Missing Fields. Failed to Login.",
            inputs: enteredData,
        };
    }

    const response = await loginUserService(validation.data);
    if (!response || response.error) {
        return {
            ...prevState,
            authErrors: response?.error || null,
            zodErrors: null,
            message: response?.error?.message || "Invalid login credentials. Please try again.",
            inputs: enteredData,
        };
    }

    const userCurrency = CURRENCIES.find(c => c.code === response.data.currency) || { code: response.data.currency, name: "Unknown Currency" };

    const userData = {
        jwtToken: response.data.token,
        login: response.data.login,
        currency: userCurrency,
        id: response.data.id
    };
    console.log("userData", userData);
    
    const cookieStore = await cookies();
    cookieStore.set("userData", JSON.stringify(userData), config);
    console.log("cookieStore.get(userData)",cookieStore.get("userData"));
    
    return {
        success: true,
        data: response.data
      };
    // redirect("/");
}

export async function logoutAction() {
    const cookieStore = await cookies();
    cookieStore.set("userData", "", { ...config, maxAge: 0 });
    redirect("/");
}
