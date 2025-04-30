import React, { useState, useEffect } from 'react';
import { motion } from 'framer-motion';
import apiClient, { getApiUrl } from '../../../utils/apiConfig';

function BusinessDetailsContent() {
  const [formData, setFormData] = useState({
    businessName: '',
    yearsOfExperience: '',
    paymentMethod: '',
    status: '',
  });
  
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [provider, setProvider] = useState(null);
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [successMessage, setSuccessMessage] = useState('');
  const [serviceCategories, setServiceCategories] = useState([]);
  const [providerServices, setProviderServices] = useState([]);
  
  const token = localStorage.getItem('authToken') || sessionStorage.getItem('authToken');
  const userId = localStorage.getItem('userId') || sessionStorage.getItem('userId');

  // Fetch provider details and their services on component load
  useEffect(() => {
    const fetchProviderDetails = async () => {
      if (!userId || !token) {
        setError('You must be logged in to view this page');
        setLoading(false);
        return;
      }
      
      try {
        // Get all service providers
        const providersResponse = await apiClient.get(getApiUrl('/service-providers/getAll'));
        
        // Find the provider that matches the logged-in user's ID
        const matchingProvider = providersResponse.data.find(provider => 
          provider.userAuth && provider.userAuth.userId === Number(userId)
        );
        
        if (matchingProvider) {
          setProvider(matchingProvider);
          
          // Update form data with the provider's details
          setFormData({
            businessName: matchingProvider.businessName || '',
            yearsOfExperience: matchingProvider.yearsOfExperience || '',
            paymentMethod: matchingProvider.paymentMethod || '',
            status: matchingProvider.status || '',
          });

          // Fetch all services to find this provider's services
          const servicesResponse = await apiClient.get(getApiUrl('/services/getAll'));
          
          // Filter services for this provider
          const services = servicesResponse.data.filter(
            service => service.provider && service.provider.providerId === matchingProvider.providerId
          );
          
          setProviderServices(services);
          
          // Get unique categories from the provider's services
          const categoryIds = [...new Set(services.map(service => 
            service.category?.categoryId
          ))].filter(id => id != null);
          
          // Fetch all service categories
          const categoriesResponse = await apiClient.get(getApiUrl('/service-categories/getAll'));
          
          // Filter categories to only include those that the provider has services in
          const relevantCategories = categoriesResponse.data.filter(
            category => categoryIds.includes(category.categoryId)
          );
          
          setServiceCategories(relevantCategories);
        } else {
          setError('No service provider profile found for your account');
        }
      } catch (err) {
        console.error('Error fetching service provider details:', err);
        setError('Failed to load your business profile');
      } finally {
        setLoading(false);
      }
    };
    
    fetchProviderDetails();
  }, [userId, token]);

  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData(prevState => ({
      ...prevState,
      [name]: value
    }));
  };
  
  const handleSubmit = async (e) => {
    e.preventDefault();
    
    if (!provider) {
      setError('Provider profile not found');
      return;
    }
    
    setIsSubmitting(true);
    setError(null);
    setSuccessMessage('');
    
    try {
      // Prepare the updated provider object
      const updatedProvider = {
        ...provider,
        businessName: formData.businessName,
        yearsOfExperience: Number(formData.yearsOfExperience),
        paymentMethod: formData.paymentMethod,
        status: formData.status,
      };
      
      // Send update request
      await apiClient.put(getApiUrl(`/service-providers/update/${provider.providerId}`), updatedProvider);
      
      setSuccessMessage('Business details updated successfully!');
      
      // Refresh provider data
      setProvider(updatedProvider);
    } catch (err) {
      console.error('Error updating service provider details:', err);
      setError('Failed to update business profile');
    } finally {
      setIsSubmitting(false);
    }
  };

  const containerVariants = {
    hidden: { opacity: 0 },
    visible: {
      opacity: 1,
      transition: {
        staggerChildren: 0.1
      }
    }
  };

  const itemVariants = {
    hidden: { opacity: 0, y: 20 },
    visible: {
      opacity: 1, y: 0,
      transition: { type: "spring", stiffness: 100 }
    }
  };

  if (loading) {
    return (
      <div className="flex justify-center items-center min-h-[500px]">
        <motion.div
          initial={{ opacity: 0, scale: 0.8 }}
          animate={{ opacity: 1, scale: 1 }}
          transition={{ duration: 0.5 }}
          className="flex flex-col items-center"
        >
          <div className="w-16 h-16 border-4 border-[#F4CE14] border-t-transparent rounded-full animate-spin"></div>
          <p className="mt-4 text-gray-600 font-medium">Loading your business profile...</p>
        </motion.div>
      </div>
    );
  }

  return (
    <motion.div 
      className="bg-white rounded-lg shadow-lg overflow-hidden"
      initial={{ opacity: 0 }}
      animate={{ opacity: 1 }}
      transition={{ duration: 0.5 }}
    >
      {/* Header Section */}
      <motion.div 
        className="bg-gradient-to-r from-[#495E57] to-[#3e4f49] px-8 py-6"
        initial={{ opacity: 0, y: -20 }}
        animate={{ opacity: 1, y: 0 }}
        transition={{ duration: 0.5, delay: 0.1 }}
      >
        <h1 className="text-3xl font-bold text-white">Business Details</h1>
        <p className="text-gray-200 mt-2">
          Complete your business profile to attract more customers
        </p>
      </motion.div>

      {/* Form Content */}
      <div className="p-8">
        {error && (
          <motion.div 
            className="bg-red-100 border-l-4 border-red-500 text-red-700 p-4 rounded mb-6" 
            role="alert"
            initial={{ opacity: 0, x: -10 }}
            animate={{ opacity: 1, x: 0 }}
            transition={{ duration: 0.3 }}
          >
            <p className="font-medium">Error</p>
            <p>{error}</p>
          </motion.div>
        )}
        
        {successMessage && (
          <motion.div 
            className="bg-green-100 border-l-4 border-green-500 text-green-700 p-4 rounded mb-6" 
            role="alert"
            initial={{ opacity: 0, scale: 0.9 }}
            animate={{ opacity: 1, scale: 1 }}
            transition={{ duration: 0.3 }}
          >
            <p className="font-medium">Success!</p>
            <p>{successMessage}</p>
          </motion.div>
        )}
        
        <form className="space-y-8" onSubmit={handleSubmit}>
          <motion.div
            variants={containerVariants}
            initial="hidden"
            animate="visible"
          >
            {/* Main Business Info Section */}
            <motion.div variants={itemVariants} className="mb-8">
              <h2 className="text-xl font-semibold text-[#495E57] border-b border-gray-200 pb-2 mb-6">
                Basic Information
              </h2>
              
              <div className="space-y-6">
                {/* Business Name */}
                <div>
                  <label htmlFor="businessName" className="block text-sm font-medium text-gray-700 mb-1">
                    Business Name
                  </label>
                  <input
                    type="text"
                    id="businessName"
                    name="businessName"
                    value={formData.businessName}
                    onChange={handleChange}
                    className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-[#F4CE14] focus:border-[#F4CE14] shadow-sm"
                    placeholder="Enter your business name"
                  />
                  <p className="mt-1 text-sm text-gray-500">This name will be displayed to customers on your profile and services</p>
                </div>

                {/* Service Categories Display */}
                <div>
                  <h3 className="text-md font-medium text-gray-700 mb-3">Your Service Categories</h3>
                  
                  {serviceCategories.length === 0 ? (
                    <p className="text-gray-500 italic">You haven't added any services yet. Add services to display categories here.</p>
                  ) : (
                    <div className="flex flex-wrap gap-2">
                      {serviceCategories.map(category => (
                        <motion.span 
                          key={category.categoryId}
                          className="inline-block bg-[#495E57]/10 text-[#495E57] px-3 py-1 rounded-full text-sm font-medium"
                          whileHover={{ scale: 1.05 }}
                          whileTap={{ scale: 0.95 }}
                        >
                          {category.categoryName}
                        </motion.span>
                      ))}
                    </div>
                  )}
                  <p className="mt-2 text-sm text-gray-500">These categories are based on your current services</p>
                </div>
              </div>
            </motion.div>

            {/* Additional Information Section */}
            <motion.div variants={itemVariants}>
              <h2 className="text-xl font-semibold text-[#495E57] border-b border-gray-200 pb-2 mb-6">
                Additional Information
              </h2>
              
              <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                {/* Years of Experience */}
                <div>
                  <label htmlFor="yearsOfExperience" className="block text-sm font-medium text-gray-700 mb-1">
                    Years of Experience
                  </label>
                  <input
                    type="number"
                    id="yearsOfExperience"
                    name="yearsOfExperience"
                    value={formData.yearsOfExperience}
                    onChange={handleChange}
                    className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-[#F4CE14] focus:border-[#F4CE14] shadow-sm"
                    placeholder="Number of years"
                  />
                </div>

                {/* Payment Method */}
                <div>
                  <label htmlFor="paymentMethod" className="block text-sm font-medium text-gray-700 mb-1">
                    Payment Method
                  </label>
                  <select
                    id="paymentMethod"
                    name="paymentMethod"
                    value={formData.paymentMethod}
                    onChange={handleChange}
                    className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-[#F4CE14] focus:border-[#F4CE14] shadow-sm"
                  >
                    <option value="">Select payment method</option>
                    <option value="Cash">Cash</option>
                    <option value="Credit Card">Credit Card</option>
                    <option value="Online Payment">Online Payment</option>
                    <option value="Multiple Methods">Multiple Methods</option>
                  </select>
                </div>

                {/* Status */}
                <div>
                  <label htmlFor="status" className="block text-sm font-medium text-gray-700 mb-1">
                    Status
                  </label>
                  <select
                    id="status"
                    name="status"
                    value={formData.status}
                    onChange={handleChange}
                    className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-[#F4CE14] focus:border-[#F4CE14] shadow-sm"
                  >
                    <option value="">Select status</option>
                    <option value="Active">Active</option>
                    <option value="Away">Away</option>
                    <option value="Busy">Busy</option>
                    <option value="On Vacation">On Vacation</option>
                  </select>
                </div>
              </div>
            </motion.div>

            {/* Submit Button */}
            <motion.div 
              className="flex justify-center pt-8"
              variants={itemVariants}
            >
              <motion.button
                type="submit"
                disabled={isSubmitting}
                whileHover={{ scale: 1.02, boxShadow: "0 5px 10px rgba(0,0,0,0.1)" }}
                whileTap={{ scale: 0.98 }}
                className={`px-8 py-3 bg-[#F4CE14] text-[#495E57] font-semibold rounded-lg shadow hover:bg-yellow-400 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-yellow-500 transition-colors ${
                  isSubmitting ? 'opacity-70 cursor-not-allowed' : ''
                }`}
              >
                {isSubmitting ? (
                  <span className="flex items-center">
                    <svg className="animate-spin -ml-1 mr-3 h-5 w-5 text-[#495E57]" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24">
                      <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4"></circle>
                      <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
                    </svg>
                    Saving...
                  </span>
                ) : 'Save Changes'}
              </motion.button>
            </motion.div>
          </motion.div>
        </form>
      </div>
    </motion.div>
  );
}

export default BusinessDetailsContent;