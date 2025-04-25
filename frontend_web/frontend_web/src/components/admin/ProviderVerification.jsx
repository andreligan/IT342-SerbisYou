import React, { useState, useEffect } from 'react';
import axios from 'axios';
import { Link } from 'react-router-dom';

const ProviderVerification = () => {
  const [providers, setProviders] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [selectedProvider, setSelectedProvider] = useState(null);
  const [filter, setFilter] = useState('pending'); // 'pending', 'verified', 'all'

  useEffect(() => {
    fetchProviders();
  }, []);

  const fetchProviders = async () => {
    try {
      setLoading(true);
      const token = localStorage.getItem('authToken') || sessionStorage.getItem('authToken');
      
      if (!token) {
        throw new Error('Authentication token not found');
      }
      
      const headers = { 'Authorization': `Bearer ${token}` };
      
      const response = await axios.get('/api/service-providers/getAll', { headers });
      setProviders(response.data || []);
      setLoading(false);
    } catch (err) {
      setError('Failed to load service providers');
      setLoading(false);
      console.error('Error fetching service providers:', err);
    }
  };

  const handleApprove = async (providerId) => {
    try {
      const token = localStorage.getItem('authToken') || sessionStorage.getItem('authToken');
      const headers = { 'Authorization': `Bearer ${token}` };
      
      // Update provider verification status
      const updateData = { verified: true };
      await axios.put(`/api/service-providers/update/${providerId}`, updateData, { headers });
      
      // Update local state
      setProviders(providers.map(provider => 
        provider.providerId === providerId 
          ? { ...provider, verified: true } 
          : provider
      ));
      
      if (selectedProvider?.providerId === providerId) {
        setSelectedProvider({ ...selectedProvider, verified: true });
      }
      
    } catch (err) {
      console.error('Error approving provider:', err);
      alert('Failed to approve service provider');
    }
  };

  const handleReject = async (providerId) => {
    if (window.confirm('Are you sure you want to reject this service provider?')) {
      try {
        const token = localStorage.getItem('authToken') || sessionStorage.getItem('authToken');
        const headers = { 'Authorization': `Bearer ${token}` };
        
        // Delete the verification request or update status as rejected
        // This depends on your backend implementation
        await axios.delete(`/api/verification/reject/${providerId}`, { headers });
        
        // Update UI
        fetchProviders();
        
      } catch (err) {
        console.error('Error rejecting provider:', err);
        alert('Failed to reject service provider');
      }
    }
  };

  const filteredProviders = providers.filter(provider => {
    if (filter === 'pending') return !provider.verified;
    if (filter === 'verified') return provider.verified;
    return true; // 'all'
  });

  if (loading && providers.length === 0) return <div className="text-center p-8">Loading service providers...</div>;
  if (error) return <div className="text-center p-8 text-red-500">{error}</div>;

  return (
    <div className="min-h-screen bg-gray-100 p-4">
      <div className="container mx-auto">
        <div className="bg-white p-6 rounded-lg shadow-md mb-6">
          <div className="flex justify-between items-center mb-6">
            <h1 className="text-2xl font-bold text-[#495E57]">Provider Verification</h1>
            <Link to="/adminHomePage" className="text-[#495E57] hover:underline flex items-center">
              <svg xmlns="http://www.w3.org/2000/svg" className="h-5 w-5 mr-1" viewBox="0 0 20 20" fill="currentColor">
                <path fillRule="evenodd" d="M9.707 14.707a1 1 0 01-1.414 0l-4-4a1 1 0 010-1.414l4-4a1 1 0 011.414 1.414L7.414 9H15a1 1 0 110 2H7.414l2.293 2.293a1 1 0 010 1.414z" clipRule="evenodd" />
              </svg>
              Back to Dashboard
            </Link>
          </div>

          <div className="flex mb-4">
            <button 
              className={`px-4 py-2 rounded-l ${filter === 'pending' ? 'bg-[#495E57] text-white' : 'bg-gray-200'}`}
              onClick={() => setFilter('pending')}
            >
              Pending
            </button>
            <button 
              className={`px-4 py-2 ${filter === 'verified' ? 'bg-[#495E57] text-white' : 'bg-gray-200'}`}
              onClick={() => setFilter('verified')}
            >
              Verified
            </button>
            <button 
              className={`px-4 py-2 rounded-r ${filter === 'all' ? 'bg-[#495E57] text-white' : 'bg-gray-200'}`}
              onClick={() => setFilter('all')}
            >
              All
            </button>
          </div>

          <div className="grid md:grid-cols-3 gap-4">
            <div className="md:col-span-1 overflow-y-auto max-h-[70vh]">
              <h2 className="font-medium mb-2">Service Providers</h2>
              {filteredProviders.length > 0 ? (
                filteredProviders.map((provider) => (
                  <div 
                    key={provider.providerId}
                    className={`p-3 mb-2 border rounded cursor-pointer ${selectedProvider?.providerId === provider.providerId ? 'border-[#495E57] bg-gray-50' : 'border-gray-200 hover:bg-gray-50'}`}
                    onClick={() => setSelectedProvider(provider)}
                  >
                    <div className="flex justify-between items-center">
                      <span className="font-medium">{provider.firstName} {provider.lastName}</span>
                      <span className={`px-2 py-1 text-xs rounded-full ${provider.verified ? 'bg-green-100 text-green-800' : 'bg-yellow-100 text-yellow-800'}`}>
                        {provider.verified ? 'Verified' : 'Pending'}
                      </span>
                    </div>
                    <p className="text-sm text-gray-600">{provider.businessName}</p>
                  </div>
                ))
              ) : (
                <div className="p-3 text-center text-gray-500">
                  No service providers {filter !== 'all' && `with status: ${filter}`}
                </div>
              )}
            </div>

            <div className="md:col-span-2 bg-gray-50 p-4 rounded">
              {selectedProvider ? (
                <div>
                  <div className="flex justify-between items-start mb-4">
                    <h2 className="text-xl font-bold">{selectedProvider.firstName} {selectedProvider.lastName}</h2>
                    <span className={`px-3 py-1 rounded-full text-sm ${selectedProvider.verified ? 'bg-green-100 text-green-800' : 'bg-yellow-100 text-yellow-800'}`}>
                      {selectedProvider.verified ? 'Verified' : 'Pending Verification'}
                    </span>
                  </div>
                  
                  <div className="grid grid-cols-1 md:grid-cols-2 gap-4 mb-4">
                    <div>
                      <p className="text-sm text-gray-500">Business Name</p>
                      <p className="font-medium">{selectedProvider.businessName || 'N/A'}</p>
                    </div>
                    <div>
                      <p className="text-sm text-gray-500">Phone Number</p>
                      <p className="font-medium">{selectedProvider.phoneNumber || 'N/A'}</p>
                    </div>
                    <div>
                      <p className="text-sm text-gray-500">Email</p>
                      <p className="font-medium">{selectedProvider.userAuth?.email || 'N/A'}</p>
                    </div>
                    <div>
                      <p className="text-sm text-gray-500">Experience</p>
                      <p className="font-medium">{selectedProvider.yearsOfExperience} years</p>
                    </div>
                  </div>
                  
                  {selectedProvider.verification && (
                    <div className="mb-4">
                      <h3 className="font-medium mb-2">Verification Documents</h3>
                      <div className="bg-white p-3 rounded border">
                        <p className="mb-2">ID Type: {selectedProvider.verification.idType}</p>
                        <p className="mb-2">ID Number: {selectedProvider.verification.idNumber}</p>
                        {/* Add document preview if available */}
                      </div>
                    </div>
                  )}
                  
                  {!selectedProvider.verified && (
                    <div className="flex mt-4">
                      <button
                        className="bg-green-500 text-white px-4 py-2 rounded mr-2 hover:bg-green-600"
                        onClick={() => handleApprove(selectedProvider.providerId)}
                      >
                        Approve
                      </button>
                      <button
                        className="bg-red-500 text-white px-4 py-2 rounded hover:bg-red-600"
                        onClick={() => handleReject(selectedProvider.providerId)}
                      >
                        Reject
                      </button>
                    </div>
                  )}
                </div>
              ) : (
                <div className="flex justify-center items-center h-64 text-gray-500">
                  Select a service provider to view details
                </div>
              )}
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

export default ProviderVerification;
