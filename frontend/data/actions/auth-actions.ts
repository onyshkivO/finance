"use server";
import { z } from "zod";
import { cookies } from "next/headers";
import { registerUserService, loginUserService } from "@/data/services/auth-service";
import { redirect } from "next/navigation";

const config = {
    maxAge: 60 * 60 * 24 * 7,
    path: "/",
    domain: process.env.HOST ?? "localhost",
    httpOnly: true,
    secure: process.env.NODE_ENV === "development",
};

const schemaRegister = z.object({
    login: z
        .string()
        .min(4, { message: "User login should be between 4 and 20 symbols" })
        .max(20, { message: "User login should be between 4 and 20 symbols" })
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

    const cookieStore = await cookies();
    cookieStore.set("jwtToken", response.data.token, config);

    redirect("/");
}

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

    const cookieStore = await cookies();
    cookieStore.set("jwtToken", response.data.token, config);

    redirect("/");
}
