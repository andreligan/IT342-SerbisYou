import React, { useState, useEffect } from 'react';
import axios from 'axios';
import { useNavigate, Routes, Route, Navigate, useLocation } from 'react-router-dom';

const SignupStepWizard = () => {
  const navigate = useNavigate();
  const location = useLocation();
  const [formData, setFormData] = useState({
    accountType: '',
    lastName: '',
    firstName: '',
    phoneNumber: '',
    businessName: '',
    yearsOfExperience: '',
    userName: '',
    email: '',
    password: '',
    confirmPassword: '',
  });
  const [formDataLoaded, setFormDataLoaded] = useState(false);
  const [errorMessage, setErrorMessage] = useState('');
  const [validationErrors, setValidationErrors] = useState({});
  
  const steps = ['Type', 'Details', 'Credentials', 'Complete'];
  
  // Get current step based on the path
  const getCurrentStep = () => {
    const path = location.pathname;
    if (path.includes('/signup/details')) return 1;
    if (path.includes('/signup/credentials')) return 2;
    if (path.includes('/signup/complete')) return 3;
    return 0; // Default to type selection
  };
  
  const currentStep = getCurrentStep();

  // Load saved form data on component mount
  useEffect(() => {
    const savedData = sessionStorage.getItem('signupFormData');
    if (savedData) {
      setFormData(JSON.parse(savedData));
    }
    setFormDataLoaded(true); // Mark that we've loaded data from session storage
  }, []);

  // Save form data to session storage on change
  useEffect(() => {
    if (formDataLoaded) { // Only save after initial load completes
      sessionStorage.setItem('signupFormData', JSON.stringify(formData));
    }
  }, [formData, formDataLoaded]);
  
  // Navigation guards - Only run after data is loaded from sessionStorage
  useEffect(() => {
    if (!formDataLoaded) return; // Skip if data hasn't loaded yet
    
    // Redirect if trying to access a step without completing previous steps
    if (currentStep === 1 && !formData.accountType) {
      navigate('/signup/type');
    } else if (currentStep === 2 && (!formData.firstName || !formData.lastName)) {
      navigate('/signup/details');
    }
  }, [currentStep, formData, navigate, formDataLoaded]);

  // Validate details step fields
  const validateDetailsStep = () => {
    const errors = {};
    
    if (!formData.lastName.trim()) errors.lastName = 'Last name is required';
    if (!formData.firstName.trim()) errors.firstName = 'First name is required';
    if (!formData.phoneNumber.trim()) errors.phoneNumber = 'Phone number is required';
    
    if (formData.accountType === 'Service Provider') {
      if (!formData.businessName.trim()) errors.businessName = 'Business name is required';
      if (!formData.yearsOfExperience) errors.yearsOfExperience = 'Years of experience is required';
    }
    
    setValidationErrors(errors);
    return Object.keys(errors).length === 0;
  };

  // Validate credentials step fields
  const validateCredentialsStep = () => {
    const errors = {};
    
    if (!formData.userName.trim()) errors.userName = 'Username is required';
    if (!formData.email.trim()) errors.email = 'Email is required';
    if (!formData.password) errors.password = 'Password is required';
    if (!formData.confirmPassword) errors.confirmPassword = 'Confirm password is required';
    
    if (formData.password !== formData.confirmPassword) {
      errors.confirmPassword = 'Passwords do not match';
    }
    
    setValidationErrors(errors);
    return Object.keys(errors).length === 0;
  };

  const handleSelection = (type) => {
    setFormData({ ...formData, accountType: type });
    navigate('/signup/details');
  };

  const handleNext = () => {
    if (currentStep === 1) {
      // Validate details step before proceeding
      if (!validateDetailsStep()) return;
    }
    
    const nextRoutes = ['/signup/type', '/signup/details', '/signup/credentials', '/signup/complete'];
    navigate(nextRoutes[currentStep + 1]);
  };

  const handlePrevious = () => {
    // Clear validation errors when going back
    setValidationErrors({});
    
    const prevRoutes = ['/signup/type', '/signup/details', '/signup/credentials', '/signup/complete'];
    navigate(prevRoutes[currentStep - 1]);
  };

  const handleChange = (event) => {
    // Clear specific validation error when field changes
    if (validationErrors[event.target.name]) {
      setValidationErrors({
        ...validationErrors,
        [event.target.name]: ''
      });
    }
    
    setFormData({ ...formData, [event.target.name]: event.target.value });
  };

  const handleSubmit = async () => {
    // Validate credentials step before submission
    if (!validateCredentialsStep()) return;
    
    setErrorMessage(''); // Clear any previous error messages
    
    try {
      // Prepare UserAuthEntity
      const userAuth = {
        userName: formData.userName,
        email: formData.email,
        password: formData.password,
        role: formData.accountType,
      };
  
      // Prepare CustomerEntity or ServiceProviderEntity based on account type
      const customer =
        formData.accountType === 'Customer'
          ? {
              firstName: formData.firstName,
              lastName: formData.lastName,
              phoneNumber: formData.phoneNumber,
            }
          : null;
  
      const serviceProvider =
        formData.accountType === 'Service Provider'
          ? {
              firstName: formData.firstName,
              lastName: formData.lastName,
              phoneNumber: formData.phoneNumber,
              businessName: formData.businessName,
              yearsOfExperience: parseInt(formData.yearsOfExperience, 10),
            }
          : null;
  
      // Send both UserAuthEntity and either CustomerEntity or ServiceProviderEntity in a single request
      const requestBody = { userAuth, customer, serviceProvider };
      const response = await axios.post('/api/user-auth/register', requestBody);
  
      alert(response.data); // Show success message
      navigate('/signup/complete');
    } catch (error) {
      const errorMsg = error.response?.data?.message || 'An error occurred during registration.';
      setErrorMessage(errorMsg);
    }
  };

  // Render step content based on current route
  const renderStepContent = () => (
    <Routes>
      <Route path="type" element={
        <div className="text-center">
          <h2 className="text-2xl font-semibold mb-8 text-[#495E57]">I am a</h2>
          <div className="flex flex-col md:flex-row justify-center gap-6 mt-8">
            <button 
              onClick={() => handleSelection('Customer')}
              className="w-45 bg-[#495E57] hover:bg-opacity-90 text-white py-4 px-8 rounded shadow-md transition-all transform hover:scale-105"
            >
              Customer
            </button>
            <button 
              onClick={() => handleSelection('Service Provider')}
              className="w-45 bg-[#F4CE14] hover:bg-opacity-90 text-[#495E57] py-4 px-8 rounded shadow-md transition-all transform hover:scale-105"
            >
              Service Provider
            </button>
          </div>
        </div>
      } />
      
      <Route path="details" element={
        <div>
          <h2 className="text-2xl font-semibold mb-8 text-center text-[#495E57]">Enter Your Details</h2>
          <div className="space-y-4">
            <div>
              <label className="block text-gray-700 mb-1">
                Last Name <span className="text-red-500">*</span>
              </label>
              <input 
                type="text" 
                name="lastName" 
                value={formData.lastName} 
                onChange={handleChange}
                required
                className={`w-full p-3 border rounded-lg focus:outline-none focus:ring-2 
                  ${validationErrors.lastName ? 'border-red-500 focus:ring-red-200' : 'border-gray-300 focus:ring-[#F4CE14]'}`}
              />
              {validationErrors.lastName && (
                <p className="text-red-500 text-sm mt-1">{validationErrors.lastName}</p>
              )}
            </div>
            
            <div>
              <label className="block text-gray-700 mb-1">
                First Name <span className="text-red-500">*</span>
              </label>
              <input 
                type="text" 
                name="firstName" 
                value={formData.firstName} 
                onChange={handleChange}
                required
                className={`w-full p-3 border rounded-lg focus:outline-none focus:ring-2 
                  ${validationErrors.firstName ? 'border-red-500 focus:ring-red-200' : 'border-gray-300 focus:ring-[#F4CE14]'}`}
              />
              {validationErrors.firstName && (
                <p className="text-red-500 text-sm mt-1">{validationErrors.firstName}</p>
              )}
            </div>
            
            <div>
              <label className="block text-gray-700 mb-1">
                Phone Number <span className="text-red-500">*</span>
              </label>
              <input 
                type="tel" 
                name="phoneNumber" 
                value={formData.phoneNumber} 
                onChange={handleChange}
                required
                className={`w-full p-3 border rounded-lg focus:outline-none focus:ring-2 
                  ${validationErrors.phoneNumber ? 'border-red-500 focus:ring-red-200' : 'border-gray-300 focus:ring-[#F4CE14]'}`}
              />
              {validationErrors.phoneNumber && (
                <p className="text-red-500 text-sm mt-1">{validationErrors.phoneNumber}</p>
              )}
            </div>

            {formData.accountType === 'Service Provider' && (
              <>
                <div>
                  <label className="block text-gray-700 mb-1">
                    Business Name <span className="text-red-500">*</span>
                  </label>
                  <input 
                    type="text" 
                    name="businessName" 
                    value={formData.businessName} 
                    onChange={handleChange}
                    required
                    className={`w-full p-3 border rounded-lg focus:outline-none focus:ring-2 
                      ${validationErrors.businessName ? 'border-red-500 focus:ring-red-200' : 'border-gray-300 focus:ring-[#F4CE14]'}`}
                  />
                  {validationErrors.businessName && (
                    <p className="text-red-500 text-sm mt-1">{validationErrors.businessName}</p>
                  )}
                </div>
                
                <div>
                  <label className="block text-gray-700 mb-1">
                    Years of Experience <span className="text-red-500">*</span>
                  </label>
                  <input 
                    type="number" 
                    name="yearsOfExperience" 
                    value={formData.yearsOfExperience} 
                    onChange={handleChange}
                    required
                    className={`w-full p-3 border rounded-lg focus:outline-none focus:ring-2 
                      ${validationErrors.yearsOfExperience ? 'border-red-500 focus:ring-red-200' : 'border-gray-300 focus:ring-[#F4CE14]'}`}
                  />
                  {validationErrors.yearsOfExperience && (
                    <p className="text-red-500 text-sm mt-1">{validationErrors.yearsOfExperience}</p>
                  )}
                </div>
              </>
            )}
          </div>
        </div>
      } />
      
      <Route path="credentials" element={
        <div>
          <h2 className="text-2xl font-semibold mb-8 text-center text-[#495E57]">Create Your Account</h2>
          <div className="space-y-4">
            <div>
              <label className="block text-gray-700 mb-1">
                Username <span className="text-red-500">*</span>
              </label>
              <input 
                type="text" 
                name="userName" 
                value={formData.userName} 
                onChange={handleChange}
                required
                className={`w-full p-3 border rounded-lg focus:outline-none focus:ring-2 
                  ${validationErrors.userName ? 'border-red-500 focus:ring-red-200' : 'border-gray-300 focus:ring-[#F4CE14]'}`}
              />
              {validationErrors.userName && (
                <p className="text-red-500 text-sm mt-1">{validationErrors.userName}</p>
              )}
            </div>
            
            <div>
              <label className="block text-gray-700 mb-1">
                Email <span className="text-red-500">*</span>
              </label>
              <input 
                type="email" 
                name="email" 
                value={formData.email} 
                onChange={handleChange}
                required
                className={`w-full p-3 border rounded-lg focus:outline-none focus:ring-2 
                  ${validationErrors.email ? 'border-red-500 focus:ring-red-200' : 'border-gray-300 focus:ring-[#F4CE14]'}`}
              />
              {validationErrors.email && (
                <p className="text-red-500 text-sm mt-1">{validationErrors.email}</p>
              )}
            </div>
            
            <div>
              <label className="block text-gray-700 mb-1">
                Password <span className="text-red-500">*</span>
              </label>
              <input 
                type="password" 
                name="password" 
                value={formData.password} 
                onChange={handleChange}
                required
                className={`w-full p-3 border rounded-lg focus:outline-none focus:ring-2 
                  ${validationErrors.password ? 'border-red-500 focus:ring-red-200' : 'border-gray-300 focus:ring-[#F4CE14]'}`}
              />
              {validationErrors.password && (
                <p className="text-red-500 text-sm mt-1">{validationErrors.password}</p>
              )}
            </div>
            
            <div>
              <label className="block text-gray-700 mb-1">
                Confirm Password <span className="text-red-500">*</span>
              </label>
              <input 
                type="password" 
                name="confirmPassword" 
                value={formData.confirmPassword} 
                onChange={handleChange}
                required
                className={`w-full p-3 border rounded-lg focus:outline-none focus:ring-2 
                  ${validationErrors.confirmPassword ? 'border-red-500 focus:ring-red-200' : 'border-gray-300 focus:ring-[#F4CE14]'}`}
              />
              {validationErrors.confirmPassword && (
                <p className="text-red-500 text-sm mt-1">{validationErrors.confirmPassword}</p>
              )}
            </div>
          </div>
        </div>
      } />
      
      <Route path="complete" element={
        <div className="text-center py-8">
          <div className="w-20 h-20 bg-green-100 rounded-full flex items-center justify-center mx-auto mb-6">
            <svg xmlns="http://www.w3.org/2000/svg" className="h-12 w-12 text-green-600" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M5 13l4 4L19 7" />
            </svg>
          </div>
          <h2 className="text-3xl font-bold text-gray-800 mb-4">Registration Complete!</h2>
          <p className="text-gray-600 mb-8">Thank you for signing up. You can now access your account.</p>
          <button 
            onClick={() => {
              sessionStorage.removeItem('signupFormData'); // Clear stored form data
              navigate('/'); // Redirect to landing page
            }}
            className="bg-[#495E57] text-white font-semibold py-3 px-8 rounded-lg hover:bg-opacity-90 transition-all"
          >
            Go back to Landing Page
          </button>
        </div>
      } />
      
      {/* Default redirect to first step */}
      <Route path="*" element={<Navigate to="/signup/type" replace />} />
    </Routes>
  );

  return (
    <div className="container mx-auto px-4 py-8">
      <div className="bg-white shadow-xl rounded p-8 md:p-10 max-w-4xl mx-auto">
        <h1 className="text-4xl font-bold text-center mb-8 text-gray-800">Get Started</h1>

        {/* Fixed Stepper with properly aligned connectors */}
        <div className="mb-12 relative">
          <div className="flex justify-between">
            {steps.map((label, index) => (
              <div key={index} className="flex flex-col items-center z-10">
                <div className={`w-20 h-20 rounded-full flex items-center justify-center text-2xl font-semibold ${
                  currentStep >= index 
                    ? 'bg-[#F4CE14] text-[#495E57]' 
                    : 'bg-gray-100 text-gray-300'
                }`}>
                  {index + 1}
                </div>
                <div className="text-sm mt-2 text-center">{label}</div>
              </div>
            ))}
          </div>
          
          {/* Connector lines positioned in the middle of the circles */}
          <div className="absolute top-5 left-0 right-0 flex justify-center gap-8 w-4/5 mx-auto z-0">
            {steps.map((_, index) => (
              index < steps.length - 1 && (
                <div key={`connector-${index}`} className="w-[50%] mt-5">
                  <div className={`h-1 ${
                    currentStep > index ? 'bg-[#F4CE14]' : 'bg-gray-200'
                  }`} />
                </div>
              )
            ))}
          </div>
        </div>

        <div className="mt-8">
          {renderStepContent()}
        </div>

        {errorMessage && (
          <div className="mt-4 p-3 bg-red-100 text-red-700 rounded-lg text-center">
            {errorMessage}
          </div>
        )}

        <div className="mt-10 flex justify-between">
          {currentStep > 0 && currentStep < 3 && (
            <button 
              onClick={handlePrevious}
              className="px-6 py-2 border border-gray-300 rounded-lg text-gray-700 hover:bg-gray-50"
            >
              Previous
            </button>
          )}
          <div className="flex-1"></div>
          {currentStep < 2 && currentStep > 0 && (
            <button 
              onClick={handleNext}
              className="px-6 py-2 bg-[#F4CE14] text-[#495E57] font-semibold rounded-lg hover:bg-opacity-90"
            >
              Next
            </button>
          )}
          {currentStep === 2 && (
            <button 
              onClick={handleSubmit}
              className="px-6 py-2 bg-[#F4CE14] text-[#495E57] font-semibold rounded-lg hover:bg-opacity-90"
            >
              Submit
            </button>
          )}
        </div>
      </div>
    </div>
  );
};

export default SignupStepWizard;