import api from "./api";

interface RegisterUserProps {
    login: string;
    password: string;
    email: string;
}

interface LoginUserProps {
    login: string;
    password: string;
}

interface ResetPasswordProps {
    token: string;
    newPassword: string;
}

export async function registerUserService(userData: RegisterUserProps) {
    try {
        const response = await api.post("/auth/signup", userData);

        return {
            status: response.status,
            data: response.data,
            error: null,
        };
    } catch (error: any) {
        console.error("Registration Service Error:", error);
        return {
            status: error.response?.status || 500,
            data: null,
            error: error.response?.data || { message: "Something went wrong. Please try again later." },
        };
    }
}

export async function loginUserService(userData: LoginUserProps) {
    try {
        const response = await api.post("/auth/signin", userData);
        
        return {
            status: response.status,
            data: response.data,
            error: null,
        };
    } catch (error: any) {
        console.error("Login Service Error:", error);
        return {
            status: error.response?.status || 500,
            data: null,
            error: error.response?.data || { message: "Something went wrong. Please try again later." },
        };
    }
}

export async function sendForgotPasswordEmail(email: string) : Promise<any> {
    try {
        const response = await api.post("/auth/forgot-password", {
            email: email
    });
        
    return response.data;

    } catch (error: any) {
        console.error("Error creating cashbox:", error);
        throw new Error("Error requesting reset email");
    }
}

export async function resetPassword({ token, newPassword }: ResetPasswordProps) {
    try {
        const response = await api.post("/auth/reset-password", {
            token,
            newPassword
        });
        
        return {
            status: response.status,
            data: response.data,
            error: null,
        };
    } catch (error: any) {
        console.error("Reset Password Service Error:", error);
        return {
            status: error.response?.status || 500,
            data: null,
            error: error.response?.data || { message: "Something went wrong. Please try again later." },
        };
    }
}