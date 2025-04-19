import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import axios from 'axios';

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
      // Send login request to the backend
      const response = await axios.post('/api/user-auth/login', {
        userName,
        password,
      });
    
      // Extract the data
      const { token, role, userId } = response.data;
      
      // Store in localStorage/sessionStorage
      if (rememberMe) {
        localStorage.setItem('authToken', token);
        localStorage.setItem('userRole', role);
        localStorage.setItem('userId', userId);
        localStorage.setItem('isAuthenticated', 'true');
      } else {
        sessionStorage.setItem('authToken', token);
        sessionStorage.setItem('userRole', role);
        sessionStorage.setItem('userId', userId);
        sessionStorage.setItem('isAuthenticated', 'true');
      }
  
      // Close the login popup
      onClose();
  
      // Redirect based on role WITH HISTORY REPLACEMENT
      if (role.toLowerCase() === "customer") {
        navigate('/customerHomePage', { replace: true });
      } else if (role.toLowerCase() === "service provider") {
        navigate('/serviceProviderHomePage', { replace: true });
      } else {
        // Handle other roles or unexpected cases
        console.error("Unknown user role:", role);
        navigate('/');
      }
    } catch (error) {
      // Handle login error
      console.error("Login failed:", error);
      setErrorMessage(error.response?.data?.message || "Login failed. Please check your credentials.");
    }
  };

  const handleGoogleLogin = () => {
    window.location.href = 'http://localhost:8080/oauth2/authorization/google';
  };

  if (!open) return null;

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-opacity-90 backdrop-blur-sm">
      <div className="bg-white rounded-lg shadow-xl w-1/3 mx-4 overflow-hidden p-4">
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
          <h2 className="text-3xl font-bold text-[#495E57]">Welcome Back!</h2>
          <p className="mt-1 text-gray-500">Login to your account</p>
        </div>
        
        {/* Dialog Content */}
        <div className="px-6 py-4">
          <form onSubmit={handleSubmit}>
            {errorMessage && (
              <div className="mb-4 text-red-500">
                {errorMessage}
              </div>
            )}
            
            <div className="mb-4">
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
            </div>
            
            <div className="mb-2">
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
            </div>
            
            <div className="flex justify-between items-center mb-6 mt-6">
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
            </div>
            
            <button
              type="submit"
              className="w-full bg-[#F4CE14] hover:bg-[#e0bd13] text-gray-700 py-3 rounded-md font-medium focus:outline-none focus:ring-2 focus:ring-yellow-600 focus:ring-opacity-50"
            >
              Log In
            </button>
            
            <div className="text-center mt-4">
              <p className="text-gray-700">
                No account yet? 
                <a href="#signup" className="ml-1 text-yellow-600 hover:text-yellow-800 font-medium">
                  Sign up here
                </a>
              </p>
            </div>
            
            <div className="relative flex py-4 items-center my-4">
              <div className="flex-grow border-t border-gray-300"></div>
              <span className="flex-shrink mx-4 text-gray-400">or</span>
              <div className="flex-grow border-t border-gray-300"></div>
            </div>
            
            <button
              type="button"
              onClick={handleGoogleLogin}
              className="h-12 w-full flex items-center justify-center border border-gray-300 hover:bg-gray-50 text-gray-700 py-2 px-4 rounded-md"
            >
              <svg className="w-5 h-5 mr-2" viewBox="0 0 24 24">
                <path fill="#4285F4" d="M22.56 12.25c0-.78-.07-1.53-.2-2.25H12v4.26h5.92c-.26 1.37-1.04 2.53-2.21 3.31v2.77h3.57c2.08-1.92 3.28-4.74 3.28-8.09z" />
                <path fill="#34A853" d="M12 23c2.97 0 5.46-.98 7.28-2.66l-3.57-2.77c-.98.66-2.23 1.06-3.71 1.06-2.86 0-5.29-1.93-6.16-4.53H2.18v2.84C3.99 20.53 7.7 23 12 23z" />
                <path fill="#FBBC05" d="M5.84 14.09c-.22-.66-.35-1.36-.35-2.09s.13-1.43.35-2.09V7.07H2.18C1.43 8.55 1 10.22 1 12s.43 3.45 1.18 4.93l2.85-2.22.81-.62z" />
                <path fill="#EA4335" d="M12 5.38c1.62 0 3.06.56 4.21 1.64l3.15-3.15C17.45 2.09 14.97 1 12 1 7.7 1 3.99 3.47 2.18 7.07l3.66 2.84c.87-2.6 3.3-4.53 6.16-4.53z" />
              </svg>
              Log in with Google
            </button>
          </form>
        </div>
      </div>
    </div>
  );
};

export default LoginPopup;