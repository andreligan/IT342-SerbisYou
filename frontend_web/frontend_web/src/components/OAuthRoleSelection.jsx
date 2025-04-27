import React, { useState } from 'react';
import { useNavigate, useLocation } from 'react-router-dom';
import axios from 'axios';

function OAuthRoleSelection() {
  const navigate = useNavigate();
  const location = useLocation();
  const searchParams = new URLSearchParams(location.search);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);
  
  // Get user info from query params
  const email = searchParams.get('email') || '';
  const name = searchParams.get('name') || '';
  const picture = searchParams.get('picture') || '';
  
  // Helper to extract first & last name from full name
  const extractNames = (fullName) => {
    const parts = fullName.split(' ');
    if (parts.length === 1) return { firstName: parts[0], lastName: '' };
    return {
      firstName: parts[0],
      lastName: parts.slice(1).join(' ')
    };
  };
  
  const { firstName, lastName } = extractNames(name);

  // Generate a more personalized username (firstName + lastName or first part of email)
  const generateUsername = () => {
    if (firstName && lastName) {
      return `${firstName.toLowerCase()}${lastName.toLowerCase()}`.substring(0, 15);
    } else {
      // If name isn't available, use the part before @ in email
      return email.split('@')[0];
    }
  };
  
  const handleRoleSelection = async (role) => {
    setLoading(true);
    setError(null);
    
    try {
      // Create base data structure
      const userData = {
        userAuth: {
          userName: generateUsername(),
          email: email,
          role: role
        }
      };
      
      if (role === 'Customer') {
        userData.customer = {
          firstName: firstName,
          lastName: lastName,
          phoneNumber: '' // Will be updated later in profile
        };
        userData.address = { // Basic placeholder address
          streetName: '',
          barangay: '',
          city: '',
          province: '',
          zipCode: ''
        };
      } else if (role === 'Service Provider') {
        userData.serviceProvider = {
          firstName: firstName,
          lastName: lastName,
          phoneNumber: '', // Will be updated later in profile
          businessName: `${firstName}'s Services`, // Default business name
          yearsOfExperience: 0
        };
        userData.address = { // Basic placeholder address
          streetName: '',
          barangay: '',
          city: '',
          province: '',
          zipCode: ''
        };
      }
      
      // Register user with Google info
      const response = await axios.post('/api/oauth/register', userData);
      
      // Store authentication data
      localStorage.setItem('authToken', response.data.token);
      localStorage.setItem('userId', response.data.userId);
      localStorage.setItem('userRole', response.data.role);
      localStorage.setItem('isAuthenticated', 'true');
      
      // Redirect based on role
      if (role === 'Customer') {
        navigate('/customerHomePage');
      } else {
        navigate('/serviceProviderHomePage');
      }
      
    } catch (err) {
      console.error('Registration error:', err);
      setError(err.response?.data?.message || 'Failed to complete registration');
    } finally {
      setLoading(false);
    }
  };

  if (!email) {
    return (
      <div className="container mx-auto px-4 py-16 text-center">
        <div className="bg-red-100 p-6 rounded-lg">
          <h2 className="text-xl font-semibold text-red-700">Missing Information</h2>
          <p className="mt-2">Unable to continue with Google signup. Please try again.</p>
          <button 
            onClick={() => navigate('/')}
            className="mt-4 px-4 py-2 bg-[#495E57] text-white rounded"
          >
            Return to Home
          </button>
        </div>
      </div>
    );
  }

  return (
    <div className="container mx-auto px-4 py-8">
      <div className="bg-white shadow-xl rounded p-8 max-w-4xl mx-auto">
        <div className="text-center mb-8">
          <img 
            src={picture || 'https://via.placeholder.com/100'} 
            alt={name}
            className="w-24 h-24 rounded-full mx-auto mb-4 border-4 border-[#F4CE14]"
          />
          <h1 className="text-3xl font-bold">Welcome, {name}!</h1>
          <p className="text-gray-600 mt-2">Complete your registration by selecting your role</p>
        </div>
        
        {error && (
          <div className="bg-red-100 text-red-700 p-4 rounded mb-6">
            {error}
          </div>
        )}
        
        <div className="grid grid-cols-1 md:grid-cols-2 gap-6 mt-8">
          <button
            onClick={() => handleRoleSelection('Customer')}
            disabled={loading}
            className="bg-white border-2 border-[#495E57] hover:bg-[#495E57] hover:text-white text-[#495E57] p-6 rounded-lg text-center transition-colors flex flex-col items-center"
          >
            <svg xmlns="http://www.w3.org/2000/svg" className="h-16 w-16 mb-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z" />
            </svg>
            <h3 className="text-xl font-bold">Customer</h3>
            <p className="mt-2 text-sm">Browse and book services</p>
          </button>
          
          <button
            onClick={() => handleRoleSelection('Service Provider')}
            disabled={loading}
            className="bg-white border-2 border-[#F4CE14] hover:bg-[#F4CE14] hover:text-[#495E57] text-[#F4CE14] p-6 rounded-lg text-center transition-colors flex flex-col items-center"
          >
            <svg xmlns="http://www.w3.org/2000/svg" className="h-16 w-16 mb-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M21 13.255A23.931 23.931 0 0112 15c-3.183 0-6.22-.62-9-1.745M16 6V4a2 2 0 00-2-2h-4a2 2 0 00-2 2v2m4 6h.01M5 20h14a2 2 0 002-2V8a2 2 0 00-2-2H5a2 2 0 00-2 2v10a2 2 0 002 2z" />
            </svg>
            <h3 className="text-xl font-bold">Service Provider</h3>
            <p className="mt-2 text-sm">Offer and manage services</p>
          </button>
        </div>
        
        {loading && (
          <div className="flex justify-center mt-6">
            <div className="w-8 h-8 border-4 border-[#F4CE14] border-t-transparent rounded-full animate-spin"></div>
          </div>
        )}
      </div>
    </div>
  );
}

export default OAuthRoleSelection;