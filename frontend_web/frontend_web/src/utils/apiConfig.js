// API configuration utility

// Determine if we're in development or production
const isDevelopment = import.meta.env.DEV;

// Set the base API URL based on environment
export const API_BASE_URL = isDevelopment 
  ? '' // Empty for local development (will use proxy)
  : 'https://serbisyo-backend.onrender.com';

// Helper function to build API URLs
export const getApiUrl = (endpoint) => {
  // Make sure endpoint has a leading slash if needed
  const formattedEndpoint = endpoint.startsWith('/') ? endpoint : `/${endpoint}`;
  return `${API_BASE_URL}/api${formattedEndpoint}`;
};

// Helper function specifically for image URLs 
export const getImageUrl = (imagePath) => {
  if (!imagePath) return null;
  
  // Direct URL handling
  if (imagePath.startsWith('http')) return imagePath;
  
  // Extract filename only, regardless of path format
  let filename = imagePath;
  if (imagePath.includes('/')) {
    filename = imagePath.split('/').pop();
  } else if (imagePath.includes('\\')) {
    filename = imagePath.split('\\').pop();
  }
  
  // Create proper URL to image resource
  return `${API_BASE_URL}/uploads/${filename}`;
};

// Export axios with default headers and configuration
import axios from 'axios';

const apiClient = axios.create();

// Request interceptor to add auth token to requests
apiClient.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('authToken') || sessionStorage.getItem('authToken');
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => Promise.reject(error)
);

export default apiClient;
