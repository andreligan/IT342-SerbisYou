import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { motion, AnimatePresence } from 'framer-motion';
import apiClient, { getApiUrl, API_BASE_URL } from '../../utils/apiConfig';

const ProviderVerification = () => {
  const [providers, setProviders] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [selectedProvider, setSelectedProvider] = useState(null);
  const [filter, setFilter] = useState('pending'); // 'pending', 'verified', 'all'
  const [searchTerm, setSearchTerm] = useState('');
  const [confirmAction, setConfirmAction] = useState(null);
  const [toast, setToast] = useState({ show: false, message: '', type: '' });
  const [isLoading, setIsLoading] = useState(false);
  const [providerImages, setProviderImages] = useState({});

  useEffect(() => {
    fetchProviders();
  }, []);

  const fetchProviders = async () => {
    try {
      setLoading(true);
      
      const response = await apiClient.get(getApiUrl('service-providers/getAll'));
      console.log('Providers API response:', response.data);
      
      // Verify that the response contains an array
      if (!Array.isArray(response.data)) {
        console.error('Expected an array of providers but received:', response.data);
        throw new Error('Invalid data format received from server');
      }
      
      const providersData = response.data || [];
      setProviders(providersData);
      
      const imagePromises = providersData.map(provider => 
        apiClient.get(getApiUrl(`service-providers/getServiceProviderImage/${provider.providerId}`))
          .then(res => ({
            providerId: provider.providerId,
            imageUrl: res.data ? `${API_BASE_URL}${res.data}` : null
          }))
          .catch((err) => {
            console.error(`Error fetching image for provider ${provider.providerId}:`, err);
            return { providerId: provider.providerId, imageUrl: null };
          })
      );
      
      const images = await Promise.all(imagePromises);
      const imageMap = images.reduce((acc, img) => {
        acc[img.providerId] = img.imageUrl;
        return acc;
      }, {});
      
      setProviderImages(imageMap);
      setLoading(false);
    } catch (err) {
      setError('Failed to load service providers');
      setLoading(false);
      console.error('Error fetching service providers:', err);
      console.error('Error details:', err.response?.data || err.message);
    }
  };

  const handleApprove = async (providerId) => {
    try {
      setIsLoading(true);
      
      const updateData = { verified: true };
      await apiClient.put(getApiUrl(`service-providers/update/${providerId}`), updateData);
      
      setProviders(providers.map(provider => 
        provider.providerId === providerId 
          ? { ...provider, verified: true } 
          : provider
      ));
      
      if (selectedProvider?.providerId === providerId) {
        setSelectedProvider({ ...selectedProvider, verified: true });
      }
      
      setToast({
        show: true,
        message: 'Provider successfully verified!',
        type: 'success'
      });
      
      setTimeout(() => setToast({ show: false, message: '', type: '' }), 3000);
      setIsLoading(false);
    } catch (err) {
      console.error('Error approving provider:', err);
      console.error('Error details:', err.response?.data || err.message);
      setToast({
        show: true,
        message: 'Failed to verify provider',
        type: 'error'
      });
      setTimeout(() => setToast({ show: false, message: '', type: '' }), 3000);
      setIsLoading(false);
    }
  };

  const handleReject = async (providerId) => {
    try {
      setIsLoading(true);
      
      const updateData = { verified: false };
      await apiClient.put(getApiUrl(`service-providers/update/${providerId}`), updateData);
      
      setConfirmAction(null);
      fetchProviders();
      
      setToast({
        show: true,
        message: 'Provider verification rejected',
        type: 'info'
      });
      
      setTimeout(() => setToast({ show: false, message: '', type: '' }), 3000);
      setIsLoading(false);
    } catch (err) {
      console.error('Error rejecting provider:', err);
      console.error('Error details:', err.response?.data || err.message);
      setToast({
        show: true,
        message: 'Failed to reject provider',
        type: 'error'
      });
      setTimeout(() => setToast({ show: false, message: '', type: '' }), 3000);
      setIsLoading(false);
    }
  };

  const filteredProviders = providers.filter(provider => {
    const matchesFilter = 
      filter === 'all' || 
      (filter === 'pending' && !provider.verified) ||
      (filter === 'verified' && provider.verified);
    
    const searchFields = [
      provider.firstName || '',
      provider.lastName || '',
      provider.businessName || '',
      provider.phoneNumber || '',
      provider.userAuth?.email || ''
    ].map(field => field.toLowerCase());
    
    const matchesSearch = searchTerm === '' || 
      searchFields.some(field => field.includes(searchTerm.toLowerCase()));
    
    return matchesFilter && matchesSearch;
  });

  if (loading && providers.length === 0) {
    return (
      <div className="min-h-screen bg-gray-100 flex justify-center items-center p-4">
        <div className="animate-pulse flex flex-col items-center">
          <div className="h-32 w-32 rounded-full bg-[#F4CE14] opacity-70"></div>
          <div className="mt-4 h-8 w-56 bg-[#495E57] rounded opacity-70"></div>
          <div className="mt-2 h-6 w-32 bg-[#495E57] rounded opacity-50"></div>
        </div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="min-h-screen bg-gray-100 flex justify-center items-center p-4">
        <div className="max-w-md p-8 bg-white rounded-lg shadow-lg text-center">
          <div className="inline-flex items-center justify-center h-16 w-16 rounded-full bg-red-100 mb-6">
            <svg className="h-8 w-8 text-red-500" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z" />
            </svg>
          </div>
          <h3 className="text-xl font-medium text-gray-800">{error}</h3>
          <p className="text-gray-500 mt-2">Please try again later or contact support.</p>
          <button 
            onClick={() => window.location.reload()} 
            className="mt-6 px-4 py-2 bg-[#495E57] text-white rounded-md hover:bg-[#3a4a45]"
          >
            Retry
          </button>
        </div>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-gray-100 p-4">
      <div className="container mx-auto px-4 py-8">
        <AnimatePresence>
          {toast.show && (
            <motion.div
              initial={{ opacity: 0, y: -50 }}
              animate={{ opacity: 1, y: 0 }}
              exit={{ opacity: 0, y: -50 }}
              className={`fixed top-5 right-5 z-50 p-4 rounded-lg shadow-lg ${
                toast.type === 'success' ? 'bg-green-100 text-green-800 border-l-4 border-green-500' : 
                toast.type === 'error' ? 'bg-red-100 text-red-800 border-l-4 border-red-500' : 
                'bg-blue-100 text-blue-800 border-l-4 border-blue-500'
              } flex items-center`}
            >
              {toast.type === 'success' ? (
                <svg className="w-5 h-5 mr-2" fill="currentColor" viewBox="0 0 20 20">
                  <path fillRule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zm3.707-9.293a1 1 0 00-1.414-1.414L9 10.586 7.707 9.293a1 1 0 00-1.414 1.414l2 2a1 1 0 001.414 0l4-4z" clipRule="evenodd" />
                </svg>
              ) : toast.type === 'error' ? (
                <svg className="w-5 h-5 mr-2" fill="currentColor" viewBox="0 0 20 20">
                  <path fillRule="evenodd" d="M8.257 3.099c.765-1.36 2.722-1.36 3.486 0l5.58 9.92c.75 1.334-.213 2.98-1.742 2.98H4.42c-1.53 0-2.493-1.646-1.743-2.98l5.58-9.92zM11 13a1 1 0 11-2 0 1 1 0 012 0zm-1-8a1 1 0 00-1 1v3a1 1 0 002 0V6a1 1 0 00-1-1z" clipRule="evenodd" />
                </svg>
              ) : (
                <svg className="w-5 h-5 mr-2" fill="currentColor" viewBox="0 0 20 20">
                  <path fillRule="evenodd" d="M18 10a8 8 0 11-16 0 8 8 0 0116 0zm-7-4a1 1 0 11-2 0 1 1 0 012 0zM9 9a1 1 0 000 2v3a1 1 0 001 1h1a1 1 0 100-2v-3a1 1 0 00-1-1H9z" clipRule="evenodd" />
                </svg>
              )}
              <span>{toast.message}</span>
            </motion.div>
          )}
        </AnimatePresence>

        <div>
          <Link 
            to="/adminHomePage" 
            className="inline-flex items-center px-4 py-2 bg-white text-[#495E57] font-medium rounded-lg border border-gray-200 hover:bg-gray-50 transition-all duration-200 shadow-sm group"
          >
            <svg xmlns="http://www.w3.org/2000/svg" className="h-5 w-5 mr-2 group-hover:-translate-x-1 transition-transform" viewBox="0 0 20 20" fill="currentColor">
              <path fillRule="evenodd" d="M9.707 14.707a1 1 0 01-1.414 0l-4-4a1 1 0 010-1.414l4-4a1 1 0 011.414 1.414L7.414 9H15a1 1 0 110 2H7.414l2.293 2.293a1 1 0 010 1.414z" clipRule="evenodd" />
            </svg>
            Back to Dashboard
          </Link>
        </div>
        <motion.div 
          initial={{ opacity: 0, y: 20 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ duration: 0.5 }}
          className="rounded-lg mb-6 overflow-hidden"
        >
          <div className="flex flex-col md:flex-row">
            <div className="p-6 md:w-1/2 relative overflow-hidden">
              <h1 className="text-3xl text-[#495E57] font-bold relative z-10 mb-2">Provider Verification</h1>
              <p className="text-[#495E57] mb-6 relative z-10">Manage and approve service provider verification requests</p>
            </div>
            
            <div className="p-6 md:w-1/2 flex flex-col justify-center">
              <div className="grid grid-cols-2 gap-6">
                <motion.div 
                  whileHover={{ scale: 1.03 }}
                  transition={{ type: "spring", stiffness: 300 }}
                  className="bg-gradient-to-br from-yellow-50 to-yellow-100 p-6 rounded-xl border border-yellow-200"
                >
                  <h3 className="text-gray-500 text-sm font-semibold uppercase tracking-wider mb-1">Pending</h3>
                  <div className="flex items-end gap-2">
                    <span className="text-4xl font-bold text-amber-600">
                      {providers.filter(provider => !provider.verified).length}
                    </span>
                    <span className="text-amber-600/70 text-sm mb-1">requests</span>
                  </div>
                </motion.div>
                
                <motion.div 
                  whileHover={{ scale: 1.03 }}
                  transition={{ type: "spring", stiffness: 300 }}
                  className="bg-gradient-to-br from-green-50 to-green-100 p-6 rounded-xl border border-green-200"
                >
                  <h3 className="text-gray-500 text-sm font-semibold uppercase tracking-wider mb-1">Verified</h3>
                  <div className="flex items-end gap-2">
                    <span className="text-4xl font-bold text-green-600">
                      {providers.filter(provider => provider.verified).length}
                    </span>
                    <span className="text-green-600/70 text-sm mb-1">providers</span>
                  </div>
                </motion.div>
              </div>
            </div>
          </div>
        </motion.div>
        
        <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
          <div className="bg-white rounded-lg shadow-md p-4">
            <div className="mb-4">
              <div className="flex space-x-2 mb-4">
                <button 
                  className={`px-3 py-1 rounded-full text-xs font-medium transition-colors
                    ${filter === 'pending' 
                      ? 'bg-[#F4CE14] text-[#495E57]' 
                      : 'bg-gray-200 text-gray-700 hover:bg-gray-300'}`}
                  onClick={() => setFilter('pending')}
                >
                  Pending
                </button>
                <button 
                  className={`px-3 py-1 rounded-full text-xs font-medium transition-colors
                    ${filter === 'verified' 
                      ? 'bg-[#F4CE14] text-[#495E57]' 
                      : 'bg-gray-200 text-gray-700 hover:bg-gray-300'}`}
                  onClick={() => setFilter('verified')}
                >
                  Verified
                </button>
                <button 
                  className={`px-3 py-1 rounded-full text-xs font-medium transition-colors
                    ${filter === 'all' 
                      ? 'bg-[#F4CE14] text-[#495E57]' 
                      : 'bg-gray-200 text-gray-700 hover:bg-gray-300'}`}
                  onClick={() => setFilter('all')}
                >
                  All
                </button>
              </div>
              
              <div className="relative mb-4">
                <input
                  type="text"
                  className="w-full p-2 pl-10 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-[#495E57]"
                  placeholder="Search providers..."
                  value={searchTerm}
                  onChange={(e) => setSearchTerm(e.target.value)}
                />
                <div className="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
                  <svg xmlns="http://www.w3.org/2000/svg" className="h-5 w-5 text-gray-400" viewBox="0 0 20 20" fill="currentColor">
                    <path fillRule="evenodd" d="M8 4a4 4 0 100 8 4 4 0 000-8zM2 8a6 6 0 1110.89 3.476l4.817 4.817a1 1 0 01-1.414 1.414l-4.816-4.816A6 6 0 012 8z" clipRule="evenodd" />
                  </svg>
                </div>
              </div>
            </div>
            
            <div className="overflow-y-auto max-h-[calc(100vh-300px)] pr-1 space-y-2 pb-4">
              {filteredProviders.length > 0 ? (
                filteredProviders.map((provider) => (
                  <motion.div
                    key={provider.providerId}
                    initial={{ opacity: 0, y: 10 }}
                    animate={{ opacity: 1, y: 0 }}
                    transition={{ duration: 0.2 }}
                    whileHover={{ scale: 1.02 }}
                    className={`p-3 rounded-lg cursor-pointer transition-all duration-200 ${
                      selectedProvider?.providerId === provider.providerId 
                        ? 'bg-[#495E57] text-white shadow-md' 
                        : 'bg-gray-50 hover:bg-gray-100 border border-gray-200'
                    }`}
                    onClick={() => setSelectedProvider(provider)}
                  >
                    <div className="flex items-center space-x-3">
                      <div className="h-12 w-12 rounded-full overflow-hidden bg-gray-200 flex-shrink-0">
                        {providerImages[provider.providerId] ? (
                          <img 
                            src={providerImages[provider.providerId]} 
                            alt={`${provider.firstName} ${provider.lastName}`}
                            className="h-full w-full object-cover"
                            onError={(e) => {
                              e.target.onerror = null;
                              e.target.src = `https://ui-avatars.com/api/?name=${provider.firstName}+${provider.lastName}&background=random`;
                            }}
                          />
                        ) : (
                          <div className="h-full w-full flex items-center justify-center bg-[#F4CE14] text-[#495E57] font-bold text-xl">
                            {provider.firstName ? provider.firstName.charAt(0) : '?'}{provider.lastName ? provider.lastName.charAt(0) : ''}
                          </div>
                        )}
                      </div>
                      <div className="flex-grow">
                        <div className="flex justify-between items-center">
                          <h3 className={`font-medium ${selectedProvider?.providerId === provider.providerId ? 'text-white' : 'text-gray-800'}`}>
                            {provider.firstName} {provider.lastName}
                          </h3>
                          <span className={`px-2 py-1 text-xs rounded-full ${
                            provider.verified 
                              ? 'bg-green-100 text-green-800' 
                              : 'bg-amber-100 text-amber-800'
                          }`}>
                            {provider.verified ? 'Verified' : 'Pending'}
                          </span>
                        </div>
                        <p className={`text-sm ${selectedProvider?.providerId === provider.providerId ? 'text-gray-100' : 'text-gray-600'}`}>
                          {provider.businessName || 'Individual Provider'}
                        </p>
                      </div>
                    </div>
                  </motion.div>
                ))
              ) : (
                <div className="flex flex-col items-center justify-center py-10">
                  <div className="w-16 h-16 bg-gray-200 rounded-full flex items-center justify-center mb-4">
                    <svg xmlns="http://www.w3.org/2000/svg" className="h-8 w-8 text-gray-400" viewBox="0 0 20 20" fill="currentColor">
                      <path fillRule="evenodd" d="M18 10a8 8 0 11-16 0 8 8 0 0116 0zm-7-4a1 1 0 11-2 0 1 1 0 012 0zM9 9a1 1 0 000 2v3a1 1 0 001 1h1a1 1 0 100-2v-3a1 1 0 00-1-1H9z" clipRule="evenodd" />
                    </svg>
                  </div>
                  <p className="text-gray-500 text-center">
                    No service providers {filter !== 'all' && `with status: ${filter}`}
                    {searchTerm && ` matching "${searchTerm}"`}
                  </p>
                </div>
              )}
            </div>
          </div>

          <div className="md:col-span-2 bg-white rounded-lg shadow-md overflow-hidden">
            {selectedProvider ? (
              <div className="h-full flex flex-col">
                <div className={`p-6 ${selectedProvider.verified ? 'bg-green-50' : 'bg-amber-50'}`}>
                  <div className="flex justify-between items-start mb-4">
                    <div className="flex items-center">
                      <div className="h-16 w-16 rounded-full overflow-hidden bg-gray-200 mr-4">
                        {providerImages[selectedProvider.providerId] ? (
                          <img 
                            src={providerImages[selectedProvider.providerId]} 
                            alt={`${selectedProvider.firstName} ${selectedProvider.lastName}`}
                            className="h-full w-full object-cover"
                          />
                        ) : (
                          <div className="h-full w-full flex items-center justify-center bg-[#F4CE14] text-[#495E57] font-bold text-xl">
                            {selectedProvider.firstName ? selectedProvider.firstName.charAt(0) : '?'}{selectedProvider.lastName ? selectedProvider.lastName.charAt(0) : ''}
                          </div>
                        )}
                      </div>
                      <div>
                        <h2 className="text-2xl font-bold text-gray-800">{selectedProvider.firstName} {selectedProvider.lastName}</h2>
                        <div className="flex items-center space-x-2 mt-1">
                          <span className={`inline-flex items-center px-3 py-1 rounded-full text-sm ${
                            selectedProvider.verified 
                              ? 'bg-green-100 text-green-800' 
                              : 'bg-amber-100 text-amber-800'
                          }`}>
                            {selectedProvider.verified ? (
                              <>
                                <svg xmlns="http://www.w3.org/2000/svg" className="h-4 w-4 mr-1" viewBox="0 0 20 20" fill="currentColor">
                                  <path fillRule="evenodd" d="M6.267 3.455a3.066 3.066 0 001.745-.723 3.066 3.066 0 013.976 0 3.066 3.066 0 001.745.723 3.066 3.066 0 012.812 2.812c.051.643.304 1.254.723 1.745a3.066 3.066 0 010 3.976 3.066 3.066 0 00-.723 1.745 3.066 3.066 0 01-2.812 2.812 3.066 3.066 0 00-1.745.723 3.066 3.066 0 01-3.976 0 3.066 3.066 0 00-1.745-.723 3.066 3.066 0 01-2.812-2.812 3.066 3.066 0 00-.723-1.745 3.066 3.066 0 010-3.976 3.066 3.066 0 00.723-1.745 3.066 3.066 0 012.812-2.812zm7.44 5.252a1 1 0 00-1.414-1.414L9 10.586 7.707 9.293a1 1 0 00-1.414 1.414l2 2a1 1 0 001.414 0l4-4z" clipRule="evenodd" />
                                </svg>
                                Verified Provider
                              </>
                            ) : 'Pending Verification'}
                          </span>
                          {selectedProvider.businessName && (
                            <span className="bg-gray-100 text-gray-800 text-sm px-3 py-1 rounded-full">Business</span>
                          )}
                        </div>
                      </div>
                    </div>
                    
                    {!selectedProvider.verified && (
                      <div className="flex space-x-2">
                        <button
                          className="bg-green-500 text-white px-4 py-2 rounded-lg hover:bg-green-600 transition-colors flex items-center"
                          onClick={() => handleApprove(selectedProvider.providerId)}
                          disabled={isLoading}
                        >
                          {isLoading ? (
                            <svg className="animate-spin -ml-1 mr-2 h-4 w-4 text-white" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24">
                              <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4"></circle>
                              <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
                            </svg>
                          ) : (
                            <svg xmlns="http://www.w3.org/2000/svg" className="h-5 w-5 mr-1" viewBox="0 0 20 20" fill="currentColor">
                              <path fillRule="evenodd" d="M16.707 5.293a1 1 0 010 1.414l-8 8a1 1 0 01-1.414 0l-4-4a1 1 0 011.414-1.414L8 12.586l7.293-7.293a1 1 0 011.414 0z" clipRule="evenodd" />
                            </svg>
                          )}
                          Approve
                        </button>
                        <button
                          className="bg-gray-200 text-gray-800 px-4 py-2 rounded-lg hover:bg-gray-300 transition-colors"
                          onClick={() => setConfirmAction({ type: 'reject', providerId: selectedProvider.providerId })}
                          disabled={isLoading}
                        >
                          Reject
                        </button>
                      </div>
                    )}
                  </div>
                </div>
                
                <div className="p-6 flex-1 overflow-auto">
                  <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                    <div className="bg-gray-50 p-4 rounded-lg border border-gray-200">
                      <h3 className="font-medium text-lg mb-3 text-[#495E57]">Business Information</h3>
                      <div className="space-y-3">
                        <div>
                          <p className="text-sm text-gray-500">Business Name</p>
                          <p className="font-medium">{selectedProvider.businessName || 'Not provided'}</p>
                        </div>
                        <div>
                          <p className="text-sm text-gray-500">Years of Experience</p>
                          <p className="font-medium">{selectedProvider.yearsOfExperience || 0} years</p>
                        </div>
                        <div>
                          <p className="text-sm text-gray-500">Status</p>
                          <p className="font-medium">{selectedProvider.status || 'N/A'}</p>
                        </div>
                      </div>
                    </div>
                    
                    <div className="bg-gray-50 p-4 rounded-lg border border-gray-200">
                      <h3 className="font-medium text-lg mb-3 text-[#495E57]">Contact Information</h3>
                      <div className="space-y-3">
                        <div>
                          <p className="text-sm text-gray-500">Email</p>
                          <p className="font-medium">{selectedProvider.userAuth?.email || 'Not provided'}</p>
                        </div>
                        <div>
                          <p className="text-sm text-gray-500">Phone Number</p>
                          <p className="font-medium">{selectedProvider.phoneNumber || 'Not provided'}</p>
                        </div>
                      </div>
                    </div>
                    
                    {selectedProvider.verification && (
                      <div className="bg-gray-50 p-4 rounded-lg border border-gray-200 md:col-span-2">
                        <h3 className="font-medium text-lg mb-3 text-[#495E57]">Verification Documents</h3>
                        <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                          <div>
                            <p className="text-sm text-gray-500">ID Type</p>
                            <p className="font-medium">{selectedProvider.verification.idType || 'Not specified'}</p>
                          </div>
                          <div>
                            <p className="text-sm text-gray-500">ID Number</p>
                            <p className="font-medium">{selectedProvider.verification.idNumber || 'Not provided'}</p>
                          </div>
                          <div className="md:col-span-2 grid grid-cols-1 md:grid-cols-2 gap-4 mt-2">
                            <div className="bg-gray-100 p-3 rounded border border-dashed border-gray-300 text-center">
                              <svg xmlns="http://www.w3.org/2000/svg" className="h-10 w-10 mx-auto text-gray-400" viewBox="0 0 20 20" fill="currentColor">
                                <path fillRule="evenodd" d="M4 4a2 2 0 012-2h4.586A2 2 0 0112 2.586L15.414 6A2 2 0 0116 7.414V16a2 2 0 01-2 2H6a2 2 0 01-2-2V4zm2 6a1 1 0 011-1h6a1 1 0 110 2H7a1 1 0 01-1-1zm1 3a1 1 0 100 2h6a1 1 0 100-2H7z" clipRule="evenodd" />
                              </svg>
                              <p className="mt-2 text-sm text-gray-600">ID Document</p>
                            </div>
                            <div className="bg-gray-100 p-3 rounded border border-dashed border-gray-300 text-center">
                              <svg xmlns="http://www.w3.org/2000/svg" className="h-10 w-10 mx-auto text-gray-400" viewBox="0 0 20 20" fill="currentColor">
                                <path fillRule="evenodd" d="M4 4a2 2 0 012-2h4.586A2 2 0 0112 2.586L15.414 6A2 2 0 0116 7.414V16a2 2 0 01-2 2H6a2 2 0 01-2-2V4zm2 6a1 1 0 011-1h6a1 1 0 110 2H7a1 1 0 01-1-1zm1 3a1 1 0 100 2h6a1 1 0 100-2H7z" clipRule="evenodd" />
                              </svg>
                              <p className="mt-2 text-sm text-gray-600">Business License</p>
                            </div>
                          </div>
                        </div>
                      </div>
                    )}
                    
                    {selectedProvider.services && selectedProvider.services.length > 0 && (
                      <div className="bg-gray-50 p-4 rounded-lg border border-gray-200 md:col-span-2">
                        <h3 className="font-medium text-lg mb-3 text-[#495E57]">Services Offered</h3>
                        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-3">
                          {selectedProvider.services.map(service => (
                            <div key={service.serviceId} className="bg-white p-3 rounded-lg border border-gray-200">
                              <p className="font-medium">{service.serviceName}</p>
                              <p className="text-sm text-gray-500 mt-1">{service.category?.categoryName}</p>
                            </div>
                          ))}
                        </div>
                      </div>
                    )}
                  </div>
                </div>
              </div>
            ) : (
              <div className="flex flex-col items-center justify-center h-full p-10 bg-gray-50">
                <div className="w-24 h-24 bg-gray-200 rounded-full flex items-center justify-center mb-4">
                  <svg xmlns="http://www.w3.org/2000/svg" className="h-10 w-10 text-gray-400" viewBox="0 0 20 20" fill="currentColor">
                    <path fillRule="evenodd" d="M18 10a8 8 0 11-16 0 8 8 0 0116 0zm-8-3a1 1 0 00-.867.5 1 1 0 11-1.731-1A3 3 0 0113 8a3.001 3.001 0 01-2 2.83V11a1 1 0 11-2 0v-1a1 1 0 011-1 1 1 0 100-2zm0 8a1 1 0 100-2 1 1 0 000 2z" clipRule="evenodd" />
                  </svg>
                </div>
                <h3 className="text-xl font-medium text-gray-800 mb-2">No Provider Selected</h3>
                <p className="text-gray-500 text-center max-w-md">
                  Select a service provider from the list to view their details and manage their verification status.
                </p>
              </div>
            )}
          </div>
        </div>

        {confirmAction && (
          <div className="fixed inset-0 z-50 flex items-center justify-center bg-black bg-opacity-50">
            <motion.div
              initial={{ scale: 0.9, opacity: 0 }}
              animate={{ scale: 1, opacity: 1 }}
              className="bg-white rounded-xl max-w-md w-full p-0 shadow-xl m-4 overflow-hidden"
            >
              {/* Modal Header */}
              <div className="bg-gray-50 p-6 border-b border-gray-100">
                <h3 className="text-xl font-semibold text-gray-800">Confirm Action</h3>
              </div>
              
              {/* Modal Body with increased spacing */}
              <div className="p-8">
                <div className="flex items-start space-x-4">
                  <div className="flex-shrink-0 mt-1">
                    <div className="w-10 h-10 rounded-full bg-amber-100 flex items-center justify-center">
                      <svg xmlns="http://www.w3.org/2000/svg" className="h-5 w-5 text-amber-600" viewBox="0 0 20 20" fill="currentColor">
                        <path fillRule="evenodd" d="M8.257 3.099c.765-1.36 2.722-1.36 3.486 0l5.58 9.92c.75 1.334-.213 2.98-1.742 2.98H4.42c-1.53 0-2.493-1.646-1.743-2.98l5.58-9.92zM11 13a1 1 0 11-2 0 1 1 0 012 0zm-1-8a1 1 0 00-1 1v3a1 1 0 002 0V6a1 1 0 00-1-1z" clipRule="evenodd" />
                      </svg>
                    </div>
                  </div>
                  <p className="text-gray-600 leading-relaxed">
                    {confirmAction.type === 'reject' 
                      ? 'Are you sure you want to reject this provider verification request? This action cannot be undone.'
                      : 'Are you sure you want to proceed with this action?'}
                  </p>
                </div>
              </div>
              
              {/* Modal Footer with button spacing */}
              <div className="px-6 py-4 bg-gray-50 border-t border-gray-100 flex justify-end space-x-4">
                <button
                  className="px-5 py-2.5 bg-white text-gray-700 rounded-lg border border-gray-300 hover:bg-gray-50 transition-colors font-medium"
                  onClick={() => setConfirmAction(null)}
                  disabled={isLoading}
                >
                  Cancel
                </button>
                <button
                  className="px-5 py-2.5 bg-red-500 text-white rounded-lg hover:bg-red-600 transition-colors font-medium flex items-center"
                  onClick={() => {
                    if (confirmAction.type === 'reject') {
                      handleReject(confirmAction.providerId);
                    }
                  }}
                  disabled={isLoading}
                >
                  {isLoading ? (
                    <>
                      <svg className="animate-spin -ml-1 mr-3 h-4 w-4 text-white" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24">
                        <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4"></circle>
                        <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
                      </svg>
                      Processing...
                    </>
                  ) : 'Confirm Rejection'}
                </button>
              </div>
            </motion.div>
          </div>
        )}
      </div>
    </div>
  );
};

export default ProviderVerification;
