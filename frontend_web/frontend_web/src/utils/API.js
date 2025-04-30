import axios from 'axios';

// Hard-code the production API URL to ensure consistency
const baseURL = 'https://serbisyo-backend.onrender.com/api/';

console.log('API baseURL:', baseURL); // Debugging line

const API = axios.create({
  baseURL,
  timeout: 30000, // Increase timeout for slow server responses
  withCredentials: true, // Needed for cookies/sessions
  headers: {
    'Content-Type': 'application/json',
    'Accept': 'application/json',
  }
});

// Add request interceptor to add auth token
API.interceptors.request.use(
  config => {
    // Get token from storage
    const token = localStorage.getItem('authToken') || sessionStorage.getItem('authToken');
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    
    return config;
  },
  error => Promise.reject(error)
);

// Add response interceptor for error logging
API.interceptors.response.use(
  response => response,
  error => {
    if (error.response) {
      console.error('API Error:', {
        status: error.response.status,
        statusText: error.response.statusText,
        url: error.response.config.url,
        data: error.response.data
      });
      
      // Handle specific HTTP error codes
      if (error.response.status === 403) {
        console.warn('403 Forbidden error - this could be a CORS or authentication issue');
        
        // Check if this is a login attempt
        if (error.response.config.url.includes('login')) {
          console.warn('Login attempt failed with 403 error - likely incorrect credentials');
        }
      } else if (error.response.status === 401) {
        console.warn('401 Unauthorized - clearing stored authentication data');
        localStorage.removeItem('authToken');
        sessionStorage.removeItem('authToken');
      }
    } else if (error.request) {
      console.error('API Error: No response received', error.request);
    } else {
      console.error('API Error:', error.message);
    }
    
    return Promise.reject(error);
  }
);

export default API;