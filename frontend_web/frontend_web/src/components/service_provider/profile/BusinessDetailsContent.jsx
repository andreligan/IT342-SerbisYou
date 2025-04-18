import React, { useState, useEffect } from 'react';
import axios from 'axios';

function BusinessDetailsContent() {
  const [formData, setFormData] = useState({
    businessName: '',
    businessDescription: '',
    businessCategory: '',
    yearEstablished: '',
    yearsOfExperience: '',
    availabilitySchedule: '',
    paymentMethod: '',
    status: '',
    // verified: false
  });
  
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [provider, setProvider] = useState(null);
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [successMessage, setSuccessMessage] = useState('');
  
  const token = localStorage.getItem('authToken') || sessionStorage.getItem('authToken');
  const userId = localStorage.getItem('userId') || sessionStorage.getItem('userId');

  // Fetch provider details on component load
  useEffect(() => {
    const fetchProviderDetails = async () => {
      if (!userId || !token) {
        setError('You must be logged in to view this page');
        setLoading(false);
        return;
      }
      
      try {
        // Get all service providers
        const response = await axios.get('/api/service-providers/getAll', {
          headers: { Authorization: `Bearer ${token}` },
        });
        
        // Find the provider that matches the logged-in user's ID
        const matchingProvider = response.data.find(provider => 
          provider.userAuth && provider.userAuth.userId === Number(userId)
        );
        
        if (matchingProvider) {
          setProvider(matchingProvider);
          
          // Update form data with the provider's details
          setFormData({
            businessName: matchingProvider.businessName || '',
            businessDescription: '', // Not directly available in entity
            businessCategory: '', // Not directly available in entity
            yearEstablished: '', // Not directly available in entity
            yearsOfExperience: matchingProvider.yearsOfExperience || '',
            availabilitySchedule: matchingProvider.availabilitySchedule || '',
            paymentMethod: matchingProvider.paymentMethod || '',
            status: matchingProvider.status || '',
            // verified: matchingProvider.verified || false
          });
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

  const handleSwitchChange = (e) => {
    const { name, checked } = e.target;
    setFormData(prevState => ({
      ...prevState,
      [name]: checked
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
        availabilitySchedule: formData.availabilitySchedule,
        paymentMethod: formData.paymentMethod,
        status: formData.status,
        // Note: verified field should not be updated by the user
      };
      
      // Send update request
      await axios.put(`/api/service-providers/update/${provider.providerId}`, updatedProvider, {
        headers: { Authorization: `Bearer ${token}` },
      });
      
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

  if (loading) {
    return <div className="flex justify-center items-center min-h-[500px]">Loading...</div>;
  }

  return (
      <div className="bg-white rounded-lg shadow-lg overflow-hidden">
        {/* Header Section */}
        <div className="bg-[#495E57] px-8 py-6">
          <h1 className="text-3xl font-bold text-white">Business Details</h1>
          <p className="text-gray-200 mt-2">
            Complete your business profile to attract more customers
          </p>
        </div>

        {/* Form Content */}
        <div className="p-8">
          {error && (
            <div className="bg-red-100 border border-red-400 text-red-700 px-4 py-3 rounded relative mb-6" role="alert">
              <span className="block sm:inline">{error}</span>
            </div>
          )}
          
          {successMessage && (
            <div className="bg-green-100 border border-green-400 text-green-700 px-4 py-3 rounded relative mb-6" role="alert">
              <span className="block sm:inline">{successMessage}</span>
            </div>
          )}
          
          <form className="space-y-8" onSubmit={handleSubmit}>
            {/* Main Business Info Section */}
            <div>
              <h2 className="text-lg font-semibold text-gray-700 border-b border-gray-200 pb-2 mb-4">
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
                    className="w-full px-4 py-2 border border-gray-300 rounded-md focus:ring-[#F4CE14] focus:border-[#F4CE14]"
                    placeholder="Enter your business name"
                  />
                </div>

                {/* Business Description */}
                <div>
                  <label htmlFor="businessDescription" className="block text-sm font-medium text-gray-700 mb-1">
                    Business Description
                  </label>
                  <textarea
                    id="businessDescription"
                    name="businessDescription"
                    value={formData.businessDescription}
                    onChange={handleChange}
                    rows={4}
                    className="w-full px-4 py-2 border border-gray-300 rounded-md focus:ring-[#F4CE14] focus:border-[#F4CE14]"
                    placeholder="Describe your business and services in detail"
                  />
                </div>

                {/* Two-column layout */}
                <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                  {/* Business Category */}
                  <div>
                    <label htmlFor="businessCategory" className="block text-sm font-medium text-gray-700 mb-1">
                      Business Category
                    </label>
                    <input
                      type="text"
                      id="businessCategory"
                      name="businessCategory"
                      value={formData.businessCategory}
                      onChange={handleChange}
                      className="w-full px-4 py-2 border border-gray-300 rounded-md focus:ring-[#F4CE14] focus:border-[#F4CE14]"
                      placeholder="e.g. Plumbing, Electrical, etc."
                    />
                  </div>

                  {/* Year Established */}
                  <div>
                    <label htmlFor="yearEstablished" className="block text-sm font-medium text-gray-700 mb-1">
                      Year Established
                    </label>
                    <input
                      type="text"
                      id="yearEstablished"
                      name="yearEstablished"
                      value={formData.yearEstablished}
                      onChange={handleChange}
                      className="w-full px-4 py-2 border border-gray-300 rounded-md focus:ring-[#F4CE14] focus:border-[#F4CE14]"
                      placeholder="e.g. 2018"
                    />
                  </div>
                </div>
              </div>
            </div>

            {/* Additional Information Section */}
            <div>
              <h2 className="text-lg font-semibold text-gray-700 border-b border-gray-200 pb-2 mb-4">
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
                    className="w-full px-4 py-2 border border-gray-300 rounded-md focus:ring-[#F4CE14] focus:border-[#F4CE14]"
                    placeholder="Number of years"
                  />
                </div>

                {/* Availability Schedule */}
                <div>
                  <label htmlFor="availabilitySchedule" className="block text-sm font-medium text-gray-700 mb-1">
                    Availability Schedule
                  </label>
                  <input
                    type="text"
                    id="availabilitySchedule"
                    name="availabilitySchedule"
                    value={formData.availabilitySchedule}
                    onChange={handleChange}
                    className="w-full px-4 py-2 border border-gray-300 rounded-md focus:ring-[#F4CE14] focus:border-[#F4CE14]"
                    placeholder="e.g. Mon-Fri, 9AM-5PM"
                  />
                </div>

                {/* Payment Method */}
                <div>
                  <label htmlFor="paymentMethod" className="block text-sm font-medium text-gray-700 mb-1">
                    Payment Method
                  </label>
                  <input
                    type="text"
                    id="paymentMethod"
                    name="paymentMethod"
                    value={formData.paymentMethod}
                    onChange={handleChange}
                    className="w-full px-4 py-2 border border-gray-300 rounded-md focus:ring-[#F4CE14] focus:border-[#F4CE14]"
                    placeholder="e.g. Cash, Credit Card, PayPal"
                  />
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
                    className="w-full px-4 py-2 border border-gray-300 rounded-md focus:ring-[#F4CE14] focus:border-[#F4CE14]"
                  >
                    <option value="">Select status</option>
                    <option value="Active">Active</option>
                    <option value="Away">Away</option>
                    <option value="Busy">Busy</option>
                    <option value="On Vacation">On Vacation</option>
                  </select>
                </div>
              </div>

              {/* Verification Switch
              <div className="mt-6">
                <label className="flex items-center space-x-3 cursor-not-allowed opacity-70">
                  <input
                    type="checkbox"
                    name="verified"
                    checked={formData.verified}
                    onChange={handleSwitchChange}
                    disabled
                    className="form-checkbox h-5 w-5 text-[#495E57] rounded border-gray-300 focus:ring-[#F4CE14]"
                  />
                  <span className="text-sm font-medium text-gray-700">Verified Business</span>
                  <span className="text-xs text-gray-500">(Managed by administrators)</span>
                </label>
              </div> */}
            </div>

            {/* Submit Button */}
            <div className="flex justify-center pt-6">
              <button
                type="submit"
                disabled={isSubmitting}
                className={`px-8 py-3 bg-[#F4CE14] text-[#495E57] font-semibold rounded-md shadow hover:bg-yellow-400 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-yellow-500 transition-colors ${isSubmitting ? 'opacity-70 cursor-not-allowed' : ''}`}
              >
                {isSubmitting ? 'Saving...' : 'Save Changes'}
              </button>
            </div>
          </form>
        </div>
      </div>
  );
}

export default BusinessDetailsContent;