import axios from 'axios';

// Hard-code the production API URL to ensure consistency
// Make sure this points directly to the backend API
const baseURL = 'https://serbisyo-backend.onrender.com/';

console.log('API baseURL:', baseURL); // Debugging line

// Helper function to format API paths correctly
const formatPath = (path) => {
  // Don't add 'api/' prefix in production since the backend may be configured differently
  // Just return the path as is to avoid routing issues
  console.log(`Formatted API path: ${path}`);
  return path;
};

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
    
    // Format the URL to ensure proper API path
    config.url = formatPath(config.url);
    
    console.log(`Making request to: ${config.baseURL}${config.url}`);  // Log full URL for debugging
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