import React, { useState } from 'react';
import axios from 'axios';

const SignupStepWizard = () => {
  const [currentStep, setCurrentStep] = useState(0);
  const [formData, setFormData] = useState({
    accountType: '',
    lastName: '',
    firstName: '',
    phoneNumber: '',
    address: '',
    businessName: '',
    yearsOfExperience: '',
    userName: '',
    email: '',
    password: '',
    confirmPassword: '',
  });
  const [errorMessage, setErrorMessage] = useState('');

  const steps = ['Type', 'Details', 'Credentials', 'Complete'];

  const handleSelection = (type) => {
    setFormData({ ...formData, accountType: type });
    handleNext();
  };

  const handleNext = () => {
    setCurrentStep((prevStep) => prevStep + 1);
  };

  const handlePrevious = () => {
    setCurrentStep((prevStep) => prevStep - 1);
  };

  const handleChange = (event) => {
    setFormData({ ...formData, [event.target.name]: event.target.value });
  };

  const handleSubmit = async () => {
    if (formData.password !== formData.confirmPassword) {
      setErrorMessage('Passwords do not match.');
      return;
    }
  
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
              address: {
                addressId: 1, // Replace with actual address ID or logic
              },
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
              address: {
                addressId: 1, // Replace with actual address ID or logic
              },
            }
          : null;
  
      // Send both UserAuthEntity and either CustomerEntity or ServiceProviderEntity in a single request
      const requestBody = { userAuth, customer, serviceProvider };
      const response = await axios.post('/api/user-auth/register', requestBody);
  
      alert(response.data); // Show success message
      setCurrentStep((prevStep) => prevStep + 1); // Move to the next step
    } catch (error) {
      const errorMsg = error.response?.data?.message || 'An error occurred during registration.';
      setErrorMessage(errorMsg);
    }
  };

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
          {currentStep === 0 && (
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
          )}

          {currentStep === 1 && (
            <div>
              <h2 className="text-2xl font-semibold mb-8 text-center text-[#495E57]">Enter Your Details</h2>
              <div className="space-y-4">
                <div>
                  <label className="block text-gray-700 mb-1">Last Name</label>
                  <input 
                    type="text" 
                    name="lastName" 
                    value={formData.lastName} 
                    onChange={handleChange}
                    className="w-full p-3 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-[#F4CE14]" 
                  />
                </div>
                <div>
                  <label className="block text-gray-700 mb-1">First Name</label>
                  <input 
                    type="text" 
                    name="firstName" 
                    value={formData.firstName} 
                    onChange={handleChange}
                    className="w-full p-3 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-[#F4CE14]" 
                  />
                </div>
                <div>
                  <label className="block text-gray-700 mb-1">Phone Number</label>
                  <input 
                    type="tel" 
                    name="phoneNumber" 
                    value={formData.phoneNumber} 
                    onChange={handleChange}
                    className="w-full p-3 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-[#F4CE14]" 
                  />
                </div>
                <div>
                  <label className="block text-gray-700 mb-1">Address</label>
                  <input 
                    type="text" 
                    name="address" 
                    value={formData.address} 
                    onChange={handleChange}
                    className="w-full p-3 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-[#F4CE14]" 
                  />
                </div>

                {formData.accountType === 'Service Provider' && (
                  <>
                    <div>
                      <label className="block text-gray-700 mb-1">Business Name</label>
                      <input 
                        type="text" 
                        name="businessName" 
                        value={formData.businessName} 
                        onChange={handleChange}
                        className="w-full p-3 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-[#F4CE14]" 
                      />
                    </div>
                    <div>
                      <label className="block text-gray-700 mb-1">Years of Experience</label>
                      <input 
                        type="number" 
                        name="yearsOfExperience" 
                        value={formData.yearsOfExperience} 
                        onChange={handleChange}
                        className="w-full p-3 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-[#F4CE14]" 
                      />
                    </div>
                  </>
                )}
              </div>
            </div>
          )}

          {currentStep === 2 && (
            <div>
              <h2 className="text-2xl font-semibold mb-8 text-center text-[#495E57]">Create Your Account</h2>
              <div className="space-y-4">
                <div>
                  <label className="block text-gray-700 mb-1">Username</label>
                  <input 
                    type="text" 
                    name="userName" 
                    value={formData.userName} 
                    onChange={handleChange}
                    className="w-full p-3 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-[#F4CE14]" 
                  />
                </div>
                <div>
                  <label className="block text-gray-700 mb-1">Email</label>
                  <input 
                    type="email" 
                    name="email" 
                    value={formData.email} 
                    onChange={handleChange}
                    className="w-full p-3 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-[#F4CE14]" 
                  />
                </div>
                <div>
                  <label className="block text-gray-700 mb-1">Password</label>
                  <input 
                    type="password" 
                    name="password" 
                    value={formData.password} 
                    onChange={handleChange}
                    className="w-full p-3 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-[#F4CE14]" 
                  />
                </div>
                <div>
                  <label className="block text-gray-700 mb-1">Confirm Password</label>
                  <input 
                    type="password" 
                    name="confirmPassword" 
                    value={formData.confirmPassword} 
                    onChange={handleChange}
                    className="w-full p-3 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-[#F4CE14]" 
                  />
                </div>
              </div>
            </div>
          )}

          {currentStep === 3 && (
            <div className="text-center py-8">
              <div className="w-20 h-20 bg-green-100 rounded-full flex items-center justify-center mx-auto mb-6">
                <svg xmlns="http://www.w3.org/2000/svg" className="h-12 w-12 text-green-600" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M5 13l4 4L19 7" />
                </svg>
              </div>
              <h2 className="text-3xl font-bold text-gray-800 mb-4">Registration Complete!</h2>
              <p className="text-gray-600 mb-8">Thank you for signing up. You can now access your account.</p>
              <button className="bg-[#495E57] text-white font-semibold py-3 px-8 rounded-lg hover:bg-opacity-90 transition-all">
                Go to Dashboard
              </button>
            </div>
          )}
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