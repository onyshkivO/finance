import axios from "axios";
import Cookies from "js-cookie";

const api = axios.create({
  baseURL: process.env.NEXT_PUBLIC_API_BASE_URL || "http://13.60.241.29:80",
  withCredentials: true,
});

api.interceptors.request.use(
  (config) => {
    // Get cookie from client-side storage
    const userData = Cookies.get("userData");
      console.log("userData: "+userData)

    if (userData) {
      const parsedUserData = JSON.parse(userData);
      const token = parsedUserData?.jwtToken;
      if (token) {
        config.headers.Authorization = `Bearer ${token}`;
      }
    }
    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

api.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      // Remove cookie on client-side
      Cookies.remove("userData");

      if (typeof window !== "undefined") {
        window.location.href = "/signin";
      }
    }
    return Promise.reject(error);
  }
);

export default api;
