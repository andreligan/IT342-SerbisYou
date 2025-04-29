import axios from 'axios';

// Get the API URL from environment variables
const apiUrl = import.meta.env.VITE_API_URL || 'http://localhost:8080';

// Make sure the API URL has a trailing slash
const baseURL = apiUrl.endsWith('/') ? apiUrl : `${apiUrl}/`;

console.log('API baseURL:', baseURL); // Debugging line

const API = axios.create({
  baseURL,
  timeout: 15000, // Increase timeout a bit
  withCredentials: true, // Needed for cookies/sessions
  headers: {
    'Content-Type': 'application/json',
  }
});

// Add request interceptor to add auth token
API.interceptors.request.use(
  config => {
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
    console.error('API Error:', error.response || error.message);
    return Promise.reject(error);
  }
);

export default API;