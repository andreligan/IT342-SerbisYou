import React, { useState, useEffect } from 'react';
import axios from 'axios';

function ProfileContent({ selectedImage, setSelectedImage }) {
  // Form state - separate for userAuth and provider entities
  const [formData, setFormData] = useState({
    // UserAuth fields
    userName: '',
    email: '',
    
    // ServiceProvider fields
    firstName: '',
    lastName: '',
    phoneNumber: '',
    businessName: '',
    yearsOfExperience: 0,
    availabilitySchedule: '',
    status: '',
    paymentMethod: '',
    verified: false
  });
  
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [success, setSuccess] = useState(false);
  const [providerId, setProviderId] = useState(null);
  const [userAuthId, setUserAuthId] = useState(null);

  // Get userId and token from localStorage or sessionStorage
  const userId = localStorage.getItem('userId') || sessionStorage.getItem('userId');
  const token = localStorage.getItem('authToken') || sessionStorage.getItem('authToken');

  // Fetch provider details on component mount
  useEffect(() => {
    const findProviderByUserId = async () => {
      try {
        setLoading(true);
        
        // Step 1: Get service provider ID by matching userId
        const providersResponse = await axios.get("/api/service-providers/getAll", {
          headers: { 'Authorization': `Bearer ${token}` }
        });
        
        const provider = providersResponse.data.find(
          p => p.userAuth && p.userAuth.userId == userId
        );
        
        if (!provider) {
          setError("No service provider profile found for this account.");
          setLoading(false);
          return;
        }

        // Store the providerId and userAuthId for later use
        console.log('Provider data:', provider);
        console.log('Provider ID:', provider.providerId);
        console.log('UserAuth data:', provider.userAuth);
        console.log('UserAuth ID:', provider.userAuth.userId);
        
        setProviderId(provider.providerId);
        setUserAuthId(provider.userAuth.userId);
        
        // Now fetch detailed information using the providerId
        const detailsResponse = await axios.get(`/api/service-providers/getById/${provider.providerId}`, {
          headers: {
            'Authorization': `Bearer ${token}`
          }
        });
        
        // Get userAuth details
        const userAuthResponse = await axios.get('/api/user-auth/getAll', {
          headers: {
            'Authorization': `Bearer ${token}`
          }
        });
        
        const userAuth = userAuthResponse.data.find(
          auth => auth.userId == userId
        );
        
        if (detailsResponse.data && userAuth) {
          // Update form data with provider and userAuth details
          setFormData({
            // UserAuth fields
            userName: userAuth.userName || '',
            email: userAuth.email || '',
            
            // ServiceProvider fields
            firstName: detailsResponse.data.firstName || '',
            lastName: detailsResponse.data.lastName || '',
            phoneNumber: detailsResponse.data.phoneNumber || '',
            businessName: detailsResponse.data.businessName || '',
            yearsOfExperience: detailsResponse.data.yearsOfExperience || 0,
            availabilitySchedule: detailsResponse.data.availabilitySchedule || '',
            status: detailsResponse.data.status || '',
            paymentMethod: detailsResponse.data.paymentMethod || '',
            verified: detailsResponse.data.verified || false
          });
          
          // If provider has a profile image  
          if (detailsResponse.data.profileImage) {
            setSelectedImage(detailsResponse.data.profileImage);
          }
        }
        
        setLoading(false);
      } catch (err) {
        console.error('Error fetching provider details:', err);
        setError('Failed to load provider details. Please try again later.');
        setLoading(false);
      }
    };

    if (userId && token) {
      findProviderByUserId();
      console.log('Retrieved userId:', userId);
      console.log('Retrieved token:', token ? 'Token exists' : 'Token is missing');
      console.log('hello');
    } else {
      setError('Authentication required. Please log in again.');
      setLoading(false);
    }
  }, [userId, token, setSelectedImage]);

  // Handle input changes
  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData((prev) => ({
      ...prev,
      [name]: value,
    }));
  };

  // Handle switch change for boolean values
  const handleSwitchChange = (e) => {
    const { name, checked } = e.target;
    setFormData((prev) => ({
      ...prev,
      [name]: checked,
    }));
  };

  // Handle form submission
  const handleSubmit = async (e) => {
    e.preventDefault();
    
    if (!providerId || !userAuthId) {
      setError('Provider ID or User Auth ID not found. Cannot update profile.');
      return;
    }
    
    try {
      setLoading(true);
      
      // Define data objects from formData
      const serviceProviderData = {
        firstName: formData.firstName,
        lastName: formData.lastName,
        phoneNumber: formData.phoneNumber,
        businessName: formData.businessName,
        yearsOfExperience: formData.yearsOfExperience,
        availabilitySchedule: formData.availabilitySchedule,
        status: formData.status,
        paymentMethod: formData.paymentMethod,
        verified: formData.verified
      };
      
      // Update service provider data
      const providerResponse = await axios.put(`/api/service-providers/update/${providerId}`, serviceProviderData, {
        headers: {
          'Authorization': `Bearer ${token}`
        }
      });
      console.log('Provider update response:', providerResponse.data);
      
      // Try to update userAuth data but handle potential permission error
      try {
        const userAuthData = {
          userName: formData.userName,
          email: formData.email
        };
        
        const userAuthResponse = await axios.put(`/api/user-auth/update/${userAuthId}`, userAuthData, {
          headers: {
            'Authorization': `Bearer ${token}`
          }
        });
        console.log('UserAuth update response:', userAuthResponse.data);
      } catch (authErr) {
        console.warn('Unable to update authentication data:', authErr.message);
        // Don't treat this as a critical error, just show a warning
        setError('Profile updated, but username/email changes require admin approval.');
      }
      
      setSuccess(true);
      setTimeout(() => setSuccess(false), 3000);
      setLoading(false);
    } catch (err) {
      console.error('Error updating provider details:', err);
      // Show detailed error information
      console.error('Error response:', err.response?.data);
      console.error('Error status:', err.response?.status);
      console.error('Error headers:', err.response?.headers);
      
      setError(err.response?.data?.message || err.message || 'Failed to update provider details. Please try again.');
      setLoading(false);
    }
  };

  // Handle image upload
  const handleImageUpload = async (e) => {
    const file = e.target.files[0];
    if (!file || !providerId) return;
    
    const reader = new FileReader();
    reader.onload = () => {
      setSelectedImage(reader.result);
      
      // Upload the image to the server
      const uploadImage = async () => {
        try {
          const formData = new FormData();
          formData.append('image', file);
          
          await axios.post(`/api/service-providers/upload-image/${providerId}`, formData, {
            headers: {
              'Authorization': `Bearer ${token}`,
              'Content-Type': 'multipart/form-data'
            }
          });
        } catch (err) {
          console.error('Error uploading image:', err);
          setError('Failed to upload profile image. Please try again.');
        }
      };
      
      // Uncomment to enable image upload
      // uploadImage();
    };
    reader.readAsDataURL(file);
  };

  if (loading && !formData.userName) {
    return (
      <div className="flex justify-center p-8">
        <div className="w-12 h-12 border-4 border-t-4 border-t-green-500 rounded-full animate-spin border-gray-200"></div>
      </div>
    );
  }

  return (
    <div className="bg-white rounded-lg shadow-md">
      {/* Header */}
      <div className="bg-[#495E57] text-white p-6 rounded-t-lg">
        <h1 className="text-3xl font-bold">My Profile</h1>
        <p className="text-gray-200 mt-2">
          Manage and protect your account
        </p>
      </div>

      {/* Alerts */}
      {error && (
        <div className="bg-red-50 border-l-4 border-red-500 p-4 m-4 text-red-700">
          {error}
        </div>
      )}

      {success && (
        <div className="bg-green-50 border-l-4 border-green-500 p-4 m-4 text-green-700">
          Profile updated successfully!
        </div>
      )}

      <form onSubmit={handleSubmit} className="p-6">
        <div className="flex flex-col lg:flex-row gap-8">
          {/* Left Column - Profile Image */}
          <div className="lg:w-1/3 flex flex-col items-center gap-4">
            <div className="relative">
              <div className="w-48 h-48 rounded-full overflow-hidden bg-gray-100 border-4 border-[#F4CE14]">
                {selectedImage ? (
                  <img 
                    src={selectedImage} 
                    alt="Profile" 
                    className="w-full h-full object-cover"
                  />
                ) : (
                  <div className="w-full h-full flex items-center justify-center bg-gray-200 text-gray-400">
                    No Image
                  </div>
                )}
              </div>
              <label 
                htmlFor="profile-image-upload" 
                className="absolute bottom-0 right-0 bg-[#F4CE14] rounded-full p-2 cursor-pointer shadow-md hover:bg-yellow-400 transition"
              >
                <svg xmlns="http://www.w3.org/2000/svg" className="h-6 w-6 text-[#495E57]" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 6v6m0 0v6m0-6h6m-6 0H6" />
                </svg>
                <input
                  id="profile-image-upload"
                  type="file"
                  accept="image/*"
                  className="hidden"
                  onChange={handleImageUpload}
                />
              </label>
            </div>
            
            <div className="text-center">
              <h2 className="text-xl font-semibold text-gray-800">{formData.firstName} {formData.lastName}</h2>
              <p className="text-gray-600">{formData.businessName || 'Service Provider'}</p>
            </div>
            
            <button 
              type="submit" 
              className="mt-4 w-45 bg-[#F4CE14] text-[#495E57] font-bold py-3 px-6 rounded-lg hover:bg-yellow-400 transition shadow-md disabled:opacity-50 disabled:cursor-not-allowed"
              disabled={loading}
            >
              {loading ? (
                <div className="flex justify-center items-center">
                  <div className="w-5 h-5 border-2 border-t-2 border-[#495E57] rounded-full animate-spin"></div>
                </div>
              ) : (
                'Save Changes'
              )}
            </button>
          </div>

          {/* Right Column - Form Fields */}
          <div className="lg:w-2/3">
            {/* Account Information */}
            <div className="mb-8">
              <h2 className="text-xl font-semibold text-[#495E57] mb-4 pb-2 border-b border-gray-200">
                Account Information
              </h2>
              <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                <div>
                  <label className="block text-gray-700 text-sm font-medium mb-2">Username</label>
                  <input
                    type="text"
                    name="userName"
                    value={formData.userName}
                    onChange={handleChange}
                    className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-[#F4CE14]"
                  />
                </div>
                <div>
                  <label className="block text-gray-700 text-sm font-medium mb-2">Email</label>
                  <input
                    type="email"
                    name="email"
                    value={formData.email}
                    onChange={handleChange}
                    className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-[#F4CE14]"
                  />
                </div>
              </div>
            </div>

            {/* Personal Information */}
            <div className="mb-8">
              <h2 className="text-xl font-semibold text-[#495E57] mb-4 pb-2 border-b border-gray-200">
                Personal Information
              </h2>
              <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                <div>
                  <label className="block text-gray-700 text-sm font-medium mb-2">First Name</label>
                  <input
                    type="text"
                    name="firstName"
                    value={formData.firstName}
                    onChange={handleChange}
                    className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-[#F4CE14]"
                  />
                </div>
                <div>
                  <label className="block text-gray-700 text-sm font-medium mb-2">Last Name</label>
                  <input
                    type="text"
                    name="lastName"
                    value={formData.lastName}
                    onChange={handleChange}
                    className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-[#F4CE14]"
                  />
                </div>
                <div>
                  <label className="block text-gray-700 text-sm font-medium mb-2">Phone Number</label>
                  <input
                    type="tel"
                    name="phoneNumber"
                    value={formData.phoneNumber}
                    onChange={handleChange}
                    className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-[#F4CE14]"
                  />
                </div>
                <div>
                  <label className="block text-gray-700 text-sm font-medium mb-2">Years of Experience</label>
                  <input
                    type="number"
                    name="yearsOfExperience"
                    value={formData.yearsOfExperience}
                    onChange={handleChange}
                    className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-[#F4CE14]"
                  />
                </div>
              </div>
            </div>

            {/* Business Information - Uncomment if needed */}
            {/* <div>
              <h2 className="text-xl font-semibold text-[#495E57] mb-4 pb-2 border-b border-gray-200">
                Business Information
              </h2>
              <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                <div>
                  <label className="block text-gray-700 text-sm font-medium mb-2">Business Name</label>
                  <input
                    type="text"
                    name="businessName"
                    value={formData.businessName}
                    onChange={handleChange}
                    className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-[#F4CE14]"
                  />
                </div>
                <div>
                  <label className="block text-gray-700 text-sm font-medium mb-2">Years of Experience</label>
                  <input
                    type="number"
                    name="yearsOfExperience"
                    value={formData.yearsOfExperience}
                    onChange={handleChange}
                    className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-[#F4CE14]"
                  />
                </div>
                <div>
                  <label className="block text-gray-700 text-sm font-medium mb-2">Availability Schedule</label>
                  <input
                    type="text"
                    name="availabilitySchedule"
                    value={formData.availabilitySchedule}
                    onChange={handleChange}
                    className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-[#F4CE14]"
                  />
                </div>
                <div>
                  <label className="block text-gray-700 text-sm font-medium mb-2">Payment Method</label>
                  <input
                    type="text"
                    name="paymentMethod"
                    value={formData.paymentMethod}
                    onChange={handleChange}
                    className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-[#F4CE14]"
                  />
                </div>
                <div>
                  <label className="block text-gray-700 text-sm font-medium mb-2">Status</label>
                  <input
                    type="text"
                    name="status"
                    value={formData.status}
                    onChange={handleChange}
                    className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-[#F4CE14]"
                  />
                </div>
                <div className="flex items-center mt-4">
                  <label className="inline-flex items-center">
                    <input
                      type="checkbox"
                      name="verified"
                      checked={formData.verified}
                      onChange={handleSwitchChange}
                      disabled={true}
                      className="form-checkbox h-5 w-5 text-[#F4CE14]"
                    />
                    <span className="ml-2 text-gray-700">Verified</span>
                  </label>
                </div>
              </div>
            </div> */}
          </div>
        </div>
      </form>
    </div>
  );
}

export default ProfileContent;