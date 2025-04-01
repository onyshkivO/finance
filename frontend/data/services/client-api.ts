// src/data/services/client-api.ts
import axios from "axios";
import Cookies from "js-cookie";

const clientApi = axios.create({
  baseURL: process.env.NEXT_PUBLIC_API_BASE_URL || "http://localhost:80",
  withCredentials: true,
});

clientApi.interceptors.request.use(
  (config) => {
    if (typeof window !== "undefined") {
      const userData = Cookies.get("userData");
      
      if (userData) {
        try {
          const parsedUserData = JSON.parse(userData);
          const token = parsedUserData?.jwtToken;
          if (token) {
            config.headers.Authorization = `Bearer ${token}`;
          }
        } catch (e) {
          console.error("Error parsing userData cookie", e);
        }
      }
    }
    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

clientApi.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      // Only run on client side
      if (typeof window !== "undefined") {
        Cookies.remove("userData");
        window.location.href = "/signin";
      }
    }
    return Promise.reject(error);
  }
);

export default clientApi;