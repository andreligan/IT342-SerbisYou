import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { motion } from 'framer-motion';
import BaseModal from './shared/BaseModal';
// Replace API import with apiClient and getApiUrl from apiConfig
import apiClient, { getApiUrl } from '../utils/apiConfig';

const LoginPopup = ({ open, onClose }) => {
  const [userName, setUserName] = useState('');
  const [password, setPassword] = useState('');
  const [rememberMe, setRememberMe] = useState(false);
  const [errorMessage, setErrorMessage] = useState('');
  const navigate = useNavigate();

  const handleSubmit = async (e) => {
    e.preventDefault();
    setErrorMessage("");
    
    try {
      console.log("Attempting login for user:", userName);
      
      // Format the login data in the exact format expected by the backend
      const loginData = {
        userName: userName,
        password: password
      };
      
      console.log("Sending login request to:", getApiUrl('user-auth/login'));
      
      try {
        // First try with apiClient
        const response = await apiClient.post(getApiUrl('user-auth/login'), loginData);
        console.log("Login successful with apiClient, received response:", response.status);
        
        // Extract the data
        const { token, role, userId } = response.data;
        handleSuccessfulLogin(token, role, userId);
      } catch (axiosError) {
        console.error("apiClient login failed, trying direct fetch:", axiosError);
        
        // If apiClient fails, try with direct fetch as a fallback
        const fetchResponse = await fetch(getApiUrl('user-auth/login'), {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json',
            'Accept': 'application/json'
          },
          body: JSON.stringify(loginData),
          credentials: 'include'
        });
        
        if (!fetchResponse.ok) {
          throw new Error(`Fetch login failed with status: ${fetchResponse.status} ${fetchResponse.statusText}`);
        }
        
        const data = await fetchResponse.json();
        console.log("Login successful with fetch, received data:", data);
        
        const { token, role, userId } = data;
        handleSuccessfulLogin(token, role, userId);
      }
    } catch (error) {
      // Handle login error
      console.error("Login failed:", error);
      
      if (error.response) {
        // The request was made and the server responded with a status code
        // that falls out of the range of 2xx
        if (error.response.status === 403) {
          setErrorMessage("Access forbidden. Your account may be inactive or you have entered incorrect credentials.");
        } else if (error.response.status === 401) {
          setErrorMessage("Invalid username or password. Please try again.");
        } else if (error.response.data && error.response.data.message) {
          setErrorMessage(error.response.data.message);
        } else {
          setErrorMessage(`Server error (${error.response.status}). Please try again later.`);
        }
      } else if (error.request) {
        // The request was made but no response was received
        setErrorMessage("No response from server. Please check your internet connection and try again.");
      } else {
        // Something happened in setting up the request that triggered an Error
        setErrorMessage("An error occurred while trying to log in. Please try again.");
      }
    }
  };
  
  const handleSuccessfulLogin = (token, role, userId) => {
    // Store in localStorage/sessionStorage
    if (rememberMe) {
      localStorage.setItem('authToken', token);
      localStorage.setItem('userRole', role);
      localStorage.setItem('userId', userId);
      localStorage.setItem('isAuthenticated', 'true');
      localStorage.setItem('username', userName);
    } else {
      sessionStorage.setItem('authToken', token);
      sessionStorage.setItem('userRole', role);
      sessionStorage.setItem('userId', userId);
      sessionStorage.setItem('isAuthenticated', 'true');
      sessionStorage.setItem('username', userName);
    }

    // Close the login popup
    onClose();

    // Redirect based on role WITH HISTORY REPLACEMENT
    if (role.toLowerCase() === "customer") {
      navigate('/customerHomePage', { replace: true });
    } else if (role.toLowerCase() === "service provider") {
      navigate('/serviceProviderHomePage', { replace: true });
    } else if (role.toLowerCase() === "admin") {
      navigate('/adminHomePage', { replace: true });
    } else {
      // Handle other roles or unexpected cases
      console.error("Unknown user role:", role);
      navigate('/');
    }
  };

  const handleGoogleLogin = () => {
    try {
      console.log("Redirecting to Google OAuth2...");
      // The OAuth redirect URI must match what's configured in the backend and Google Cloud Console
      const googleAuthUrl = `${API_BASE_URL}/oauth2/authorization/google`;
      
      console.log("Google Auth URL:", googleAuthUrl);
      
      // Open in the same window, not a popup, to avoid popup blockers
      // Using window.location.href ensures this is treated as a full navigation,
      // not a React Router navigation
      window.location.href = googleAuthUrl;
    } catch (error) {
      console.error("Error redirecting to Google login:", error);
      setErrorMessage("Failed to initialize Google login. Please try again.");
    }
  };

  return (
    <BaseModal 
      isOpen={open} 
      onClose={onClose}
      maxWidth="max-w-md"
    >
      <div className="bg-white rounded-lg overflow-hidden">
        {/* Dialog Header */}
        <div className="relative pt-6 px-6 pb-2 text-center">
          <button 
            onClick={onClose}
            className="absolute right-4 top-4 text-yellow-500 hover:text-yellow-700"
            aria-label="close"
          >
            <svg xmlns="http://www.w3.org/2000/svg" className="h-6 w-6" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M6 18L18 6M6 6l12 12" />
            </svg>
          </button>
          <motion.h2 
            className="text-3xl font-bold text-[#495E57]"
            initial={{ opacity: 0, y: -10 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ delay: 0.1 }}
          >
            Welcome Back!
          </motion.h2>
          <motion.p 
            className="mt-1 text-gray-500"
            initial={{ opacity: 0 }}
            animate={{ opacity: 1 }}
            transition={{ delay: 0.2 }}
          >
            Login to your account
          </motion.p>
        </div>
        
        {/* Dialog Content */}
        <div className="px-6 py-4">
          <form onSubmit={handleSubmit}>
            {errorMessage && (
              <motion.div 
                className="mb-4 text-red-500 p-3 bg-red-50 rounded-lg"
                initial={{ opacity: 0, height: 0 }}
                animate={{ opacity: 1, height: 'auto' }}
                exit={{ opacity: 0, height: 0 }}
              >
                {errorMessage}
              </motion.div>
            )}
            
            <motion.div 
              className="mb-4"
              initial={{ opacity: 0, y: 10 }}
              animate={{ opacity: 1, y: 0 }}
              transition={{ delay: 0.3 }}
            >
              <label htmlFor="userName" className="block text-sm font-medium text-gray-700 mb-1">
                Username
              </label>
              <input
                id="userName"
                type="text"
                value={userName}
                onChange={(e) => setUserName(e.target.value)}
                required
                className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-yellow-500 focus:border-yellow-500"
              />
            </motion.div>
            
            <motion.div 
              className="mb-2"
              initial={{ opacity: 0, y: 10 }}
              animate={{ opacity: 1, y: 0 }}
              transition={{ delay: 0.4 }}
            >
              <label htmlFor="password" className="block text-sm font-medium text-gray-700 mb-1">
                Password
              </label>
              <input
                id="password"
                type="password"
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                required
                className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-yellow-500 focus:border-yellow-500"
              />
            </motion.div>
            
            <motion.div 
              className="flex justify-between items-center mb-6 mt-6"
              initial={{ opacity: 0, y: 10 }}
              animate={{ opacity: 1, y: 0 }}
              transition={{ delay: 0.5 }}
            >
              <div className="flex items-center">
                <input
                  id="rememberMe"
                  type="checkbox"
                  checked={rememberMe}
                  onChange={() => setRememberMe(!rememberMe)}
                  className="h-4 w-4 text-yellow-500 accent-yellow-500 focus:ring-yellow-400 border-gray-300 rounded"
                />
                <label htmlFor="rememberMe" className="ml-2 block text-sm text-gray-700">
                  Remember Me
                </label>
              </div>
              <a href="#forgot-password" className="text-sm text-yellow-600 hover:text-yellow-800">
                Forgot Password?
              </a>
            </motion.div>
            
            <motion.button
              type="submit"
              className="w-full bg-[#F4CE14] hover:bg-[#e0bd13] text-gray-700 py-3 rounded-md font-medium focus:outline-none focus:ring-2 focus:ring-yellow-600 focus:ring-opacity-50"
              initial={{ opacity: 0, y: 10 }}
              animate={{ opacity: 1, y: 0 }}
              transition={{ delay: 0.6 }}
              whileHover={{ scale: 1.02 }}
              whileTap={{ scale: 0.98 }}
            >
              Log In
            </motion.button>
            
            <motion.div 
              className="text-center mt-4"
              initial={{ opacity: 0 }}
              animate={{ opacity: 1 }}
              transition={{ delay: 0.7 }}
            >
              <p className="text-gray-700">
                No account yet? 
                <a href="#signup" className="ml-1 text-yellow-600 hover:text-yellow-800 font-medium">
                  Sign up here
                </a>
              </p>
            </motion.div>
            
            <div className="relative flex py-4 items-center my-4">
              <div className="flex-grow border-t border-gray-300"></div>
              <span className="flex-shrink mx-4 text-gray-400">or</span>
              <div className="flex-grow border-t border-gray-300"></div>
            </div>
            
            <motion.button
              type="button"
              onClick={handleGoogleLogin}
              className="h-12 w-full flex items-center justify-center border border-gray-300 hover:bg-gray-50 text-gray-700 py-2 px-4 rounded-md"
              initial={{ opacity: 0, y: 10 }}
              animate={{ opacity: 1, y: 0 }}
              transition={{ delay: 0.8 }}
              whileHover={{ scale: 1.02, backgroundColor: "rgba(0, 0, 0, 0.05)" }}
              whileTap={{ scale: 0.98 }}
            >
              <svg className="w-5 h-5 mr-2" viewBox="0 0 24 24">
                <path fill="#4285F4" d="M22.56 12.25c0-.78-.07-1.53-.2-2.25H12v4.26h5.92c-.26 1.37-1.04 2.53-2.21 3.31v2.77h3.57c2.08-1.92 3.28-4.74 3.28-8.09z" />
                <path fill="#34A853" d="M12 23c2.97 0 5.46-.98 7.28-2.66l-3.57-2.77c-.98.66-2.23 1.06-3.71 1.06-2.86 0-5.29-1.93-6.16-4.53H2.18v2.84C3.99 20.53 7.7 23 12 23z" />
                <path fill="#FBBC05" d="M5.84 14.09c-.22-.66-.35-1.36-.35-2.09s.13-1.43.35-2.09V7.07H2.18C1.43 8.55 1 10.22 1 12s.43 3.45 1.18 4.93l2.85-2.22.81-.62z" />
                <path fill="#EA4335" d="M12 5.38c1.62 0 3.06.56 4.21 1.64l3.15-3.15C17.45 2.09 14.97 1 12 1 7.7 1 3.99 3.47 2.18 7.07l3.66 2.84c.87-2.6 3.3-4.53 6.16-4.53z" />
              </svg>
              Log in with Google
            </motion.button>
          </form>
        </div>
      </div>
    </BaseModal>
  );
};

export default LoginPopup;