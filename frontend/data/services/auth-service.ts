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
