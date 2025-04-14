import axios from 'axios';

// Create an axios instance
const API = axios.create();

// Add a response interceptor
API.interceptors.response.use(
  (response) => {
    return response;
  },
  async (error) => {
    const originalRequest = error.config;
    
    // If the error is 401 Unauthorized and we haven't already tried to refresh
    if (error.response?.status === 401 && !originalRequest._retry) {
      originalRequest._retry = true;
      
      // Clear tokens from storage
      localStorage.removeItem('authToken');
      localStorage.removeItem('userRole');
      localStorage.removeItem('userId');
      localStorage.removeItem('isAuthenticated');
      
      sessionStorage.removeItem('authToken');
      sessionStorage.removeItem('userRole');
      sessionStorage.removeItem('userId');
      sessionStorage.removeItem('isAuthenticated');
      
      // Force redirect to homepage/login
      window.location.href = '/';
      
      return Promise.reject(error);
    }
    
    return Promise.reject(error);
  }
);

export default API;