// src/data/services/server-api.ts
import axios from "axios";
import { cookies } from "next/headers";
import { redirect } from "next/navigation";

const serverApi = axios.create({
  baseURL: process.env.NEXT_PUBLIC_API_BASE_URL || "https://back.fintracker.click",
  withCredentials: true,
});

export async function authenticatedServerApiRequest(config: {
  method: "get" | "post" | "put" | "delete" | "patch";
  url: string;
  data?: any;
  headers?: Record<string, string>;
}) {
  const cookieStore = await cookies();
  const userData = cookieStore.get("userData")?.value;

  if (!userData) {
    redirect("/signin");
  }

  try {
    const parsedUserData = JSON.parse(userData);
    const token = parsedUserData?.jwtToken;

    if (!token) {
      cookieStore.delete("userData");
      redirect("/signin");
    }

    const response = await serverApi.request({
      ...config,
      headers: {
        ...config.headers,
        Authorization: `Bearer ${token}`,
      },
    });

    return response.data;
  } catch (error) {
    if (axios.isAxiosError(error) && error.response?.status === 401) {
      cookieStore.delete("userData");
      redirect("/signin");
    }
    console.error("API request failed:", error);
    throw error;
  }
}

export default serverApi;