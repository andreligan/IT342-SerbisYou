import React, { useState, useEffect } from 'react';
import axios from 'axios';

function AddressContent() {
  // Form state
  const [addressForm, setAddressForm] = useState({
    barangay: '',
    city: '',
    province: '',
    streetName: '',
    zipCode: ''
  });
  
  // State for addresses list, error handling, loading states
  const [addresses, setAddresses] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);
  const [success, setSuccess] = useState(null);
  const [editMode, setEditMode] = useState(false);
  const [currentAddressId, setCurrentAddressId] = useState(null);
  const [deleteDialogOpen, setDeleteDialogOpen] = useState(false);
  const [addressToDelete, setAddressToDelete] = useState(null);
  const [providerId, setProviderId] = useState(null);
  const [providerAddressId, setProviderAddressId] = useState(null);
  
  // Get userId and token from localStorage or sessionStorage
  const userId = localStorage.getItem('userId') || sessionStorage.getItem('userId');
  const token = localStorage.getItem('authToken') || sessionStorage.getItem('authToken');
  
  // First fetch the service provider to get their addressId
  useEffect(() => {
    const getServiceProviderDetails = async () => {
      try {
        setLoading(true);
        
        // Get all service providers
        const providersResponse = await axios.get("/api/service-providers/getAll", {
          headers: { 'Authorization': `Bearer ${token}` }
        });
        
        // Find the provider that matches the current user
        const provider = providersResponse.data.find(
          p => p.userAuth && p.userAuth.userId == userId
        );
        
        if (provider) {
          console.log('Found provider:', provider);
          setProviderId(provider.providerId);
          
          // Check if provider has a nested address object
          if (provider.address) {
            console.log('Provider address:', provider.address);
            setProviderAddressId(provider.address.addressId);
            
            // Directly populate the form with provider's address data
            setAddressForm({
              barangay: provider.address.barangay || '',
              city: provider.address.city || '',
              province: provider.address.province || '',
              streetName: provider.address.streetName || '',
              zipCode: provider.address.zipCode || ''
            });
            
            // Add this address to addresses list
            setAddresses([provider.address]);
            setEditMode(true);
            setCurrentAddressId(provider.address.addressId);
          } else if (provider.addressId) {
            // If address is not nested but referenced by ID
            setProviderAddressId(provider.addressId);
            // Will fetch address details in the next useEffect
          } else {
            console.log('No address found for this provider');
          }
        } else {
          setError("No service provider profile found for this account.");
        }
        
        setLoading(false);
      } catch (err) {
        console.error('Error fetching provider details:', err);
        setError('Failed to load provider details. Please try again later.');
        setLoading(false);
      }
    };
    
    if (userId && token) {
      getServiceProviderDetails();
    }
  }, [userId, token]);
  
  // Then fetch addresses once we have the provider addressId
  useEffect(() => {
    if (providerAddressId) {
      fetchAddresses();
    }
  }, [providerAddressId]);
  
  // Fetch all addresses
  const fetchAddresses = async () => {
    try {
      setLoading(true);
      setError(null);
      
      const response = await axios.get('/api/addresses/getAll', {
        headers: { 'Authorization': `Bearer ${token}` }
      });
      
      // Filter addresses by addressId that match the provider's addressId
      const providerAddresses = response.data.filter(address => 
        // Include the provider's address and any addresses specifically linked to this provider
        address.addressId === providerAddressId || address.providerId === providerId
      );
      
      setAddresses(providerAddresses);
      setLoading(false);
      
      // If there's a matched address and we're not in edit mode, populate the form
      if (providerAddresses.length > 0 && !editMode) {
        const mainAddress = providerAddresses.find(addr => addr.addressId === providerAddressId) || providerAddresses[0];
        handleEdit(mainAddress);
      }
      
    } catch (err) {
      console.error('Error fetching addresses:', err);
      setError('Failed to load addresses. Please try again.');
      setLoading(false);
    }
  };
  
  // Handle input changes
  const handleChange = (e) => {
    const { name, value } = e.target;
    setAddressForm(prev => ({
      ...prev,
      [name]: value
    }));
  };
  
  // Handle form submission
  const handleSubmit = async (e) => {
    e.preventDefault();
    
    try {
      setLoading(true);
      setError(null);
      
      // If in edit mode, update the address
      if (editMode && currentAddressId) {
        const addressPayload = {
          ...addressForm,
          providerId: providerId,
          // Include any other required fields for authorization
          serviceProviderId: providerId  // Some backends use this to verify relationship
        };
        
        console.log("Sending address update with payload:", addressPayload);
        
        try {
          const response = await axios.put(
            `/api/addresses/updateAddress/${currentAddressId}`, 
            addressPayload,
            {
              headers: { 
                'Authorization': `Bearer ${token}`,
                'Content-Type': 'application/json'
              }
            }
          );
          
          console.log("Address update response:", response.data);
          
          setSuccess('Address updated successfully!');
          
          // Update the addresses list
          setAddresses(prev => 
            prev.map(addr => addr.addressId === currentAddressId ? response.data : addr)
          );
          
          // If this is the provider's main address, we also need to update the provider
          if (currentAddressId === providerAddressId) {
            await axios.put(`/api/service-providers/update/${providerId}`, 
              { addressId: currentAddressId },
              { headers: { 'Authorization': `Bearer ${token}` }}
            );
          }
        } catch (updateErr) {
          console.error('Error updating address:', updateErr);
          console.error('Error response:', updateErr.response?.data);
          throw updateErr; // Rethrow to be caught by the outer catch block
        }
      } else {
        // Create a new address
        const response = await axios.post(
          '/api/addresses/postAddress', 
          {
            ...addressForm,
            providerId: providerId // Link address to provider instead of user
          },
          {
            headers: { 'Authorization': `Bearer ${token}` }
          }
        );
        
        const newAddressId = response.data.addressId;
        
        // If this is the first address, set it as the provider's main address
        if (!providerAddressId) {
          await axios.put(`/api/service-providers/update/${providerId}`, 
            { addressId: newAddressId },
            { headers: { 'Authorization': `Bearer ${token}` }}
          );
          
          setProviderAddressId(newAddressId);
        }
        
        setSuccess('New address added successfully!');
        
        // Add the new address to the list
        setAddresses(prev => [...prev, response.data]);
      }
      
      // Reset form and state
      setAddressForm({
        barangay: '',
        city: '',
        province: '',
        streetName: '',
        zipCode: ''
      });
      setEditMode(false);
      setCurrentAddressId(null);
      setLoading(false);
      
      // Auto-hide success message after 3 seconds
      setTimeout(() => setSuccess(null), 3000);
      
    } catch (err) {
      console.error('Error saving address:', err);
      setError(err.response?.data?.message || 'Failed to save address. Please try again.');
      setLoading(false);
    }
  };
  
  // Handle address deletion
  const handleDelete = async (addressId) => {
    try {
      setLoading(true);
      setError(null);
      
      // Check if this is the provider's main address
      if (addressId === providerAddressId) {
        setError("Cannot delete your main address. Please set another address as primary first.");
        setLoading(false);
        setDeleteDialogOpen(false);
        return;
      }
      
      await axios.delete(`/api/addresses/delete/${addressId}`, {
        headers: { 'Authorization': `Bearer ${token}` }
      });
      
      setSuccess('Address deleted successfully!');
      
      // Remove the deleted address from the list
      setAddresses(prev => prev.filter(addr => addr.addressId !== addressId));
      
      setLoading(false);
      setDeleteDialogOpen(false);
      
      // Auto-hide success message after 3 seconds
      setTimeout(() => setSuccess(null), 3000);
      
    } catch (err) {
      console.error('Error deleting address:', err);
      setError(err.response?.data?.message || 'Failed to delete address. Please try again.');
      setLoading(false);
      setDeleteDialogOpen(false);
    }
  };
  
  // Set an address as the provider's main address
  const setAsMainAddress = async (addressId) => {
    try {
      setLoading(true);
      
      await axios.put(`/api/service-providers/update/${providerId}`, 
        { addressId: addressId },
        { headers: { 'Authorization': `Bearer ${token}` }}
      );
      
      setProviderAddressId(addressId);
      setSuccess('Main address updated successfully!');
      setLoading(false);
      
      setTimeout(() => setSuccess(null), 3000);
      
    } catch (err) {
      console.error('Error updating main address:', err);
      setError('Failed to update main address. Please try again.');
      setLoading(false);
    }
  };
  
  // Load address data for editing
  const handleEdit = (address) => {
    setAddressForm({
      barangay: address.barangay || '',
      city: address.city || '',
      province: address.province || '',
      streetName: address.streetName || '',
      zipCode: address.zipCode || ''
    });
    setEditMode(true);
    setCurrentAddressId(address.addressId);
    
    // Scroll to form
    window.scrollTo({ top: 0, behavior: 'smooth' });
  };
  
  // Cancel editing
  const handleCancel = () => {
    setAddressForm({
      barangay: '',
      city: '',
      province: '',
      streetName: '',
      zipCode: ''
    });
    setEditMode(false);
    setCurrentAddressId(null);
  };
  
  // Open delete confirmation dialog
  const openDeleteDialog = (addressId) => {
    setAddressToDelete(addressId);
    setDeleteDialogOpen(true);
  };

  return (
    <div className="bg-white rounded-lg shadow-md">
      {/* Header */}
      <div className="bg-[#495E57] text-white p-6 rounded-t-lg">
        <h1 className="text-3xl font-bold">My Addresses</h1>
        <p className="text-gray-200 mt-2">
          {editMode ? 'Edit your address details' : 'Add a new address to your profile'}
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
          {success}
        </div>
      )}

      {/* Address Form */}
      <div className="p-6">
        <form onSubmit={handleSubmit} className="max-w-4xl mx-auto">
          <div className="bg-gray-50 p-6 rounded-lg border border-gray-200 mb-6">
            <h2 className="text-xl font-semibold text-[#495E57] mb-4 pb-2 border-b border-gray-200">
              {editMode ? 'Edit Address' : 'Add New Address'}
            </h2>
            
            <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
              <div>
                <label className="block text-gray-700 text-sm font-medium mb-2">Street Name</label>
                <input
                  type="text"
                  name="streetName"
                  value={addressForm.streetName}
                  onChange={handleChange}
                  required
                  className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-[#F4CE14]"
                  placeholder="123 Main Street"
                />
              </div>
              
              <div>
                <label className="block text-gray-700 text-sm font-medium mb-2">Barangay</label>
                <input
                  type="text"
                  name="barangay"
                  value={addressForm.barangay}
                  onChange={handleChange}
                  required
                  className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-[#F4CE14]"
                  placeholder="Barangay name"
                />
              </div>
              
              <div>
                <label className="block text-gray-700 text-sm font-medium mb-2">City</label>
                <input
                  type="text"
                  name="city"
                  value={addressForm.city}
                  onChange={handleChange}
                  required
                  className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-[#F4CE14]"
                  placeholder="City"
                />
              </div>
              
              <div>
                <label className="block text-gray-700 text-sm font-medium mb-2">Province</label>
                <input
                  type="text"
                  name="province"
                  value={addressForm.province}
                  onChange={handleChange}
                  required
                  className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-[#F4CE14]"
                  placeholder="Province"
                />
              </div>
              
              <div>
                <label className="block text-gray-700 text-sm font-medium mb-2">Zip Code</label>
                <input
                  type="text"
                  name="zipCode"
                  value={addressForm.zipCode}
                  onChange={handleChange}
                  required
                  className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-[#F4CE14]"
                  placeholder="Zip Code"
                />
              </div>
            </div>
            
            <div className="flex justify-center mt-8 space-x-4">
              <button 
                type="submit" 
                disabled={loading}
                className="bg-[#F4CE14] text-[#495E57] font-bold py-3 px-8 rounded-lg hover:bg-yellow-400 transition shadow-md flex items-center disabled:opacity-50 disabled:cursor-not-allowed"
              >
                {loading ? (
                  <div className="w-5 h-5 border-2 border-t-2 border-[#495E57] rounded-full animate-spin mr-2"></div>
                ) : editMode ? (
                  <svg xmlns="http://www.w3.org/2000/svg" className="h-5 w-5 mr-2" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M11 5H6a2 2 0 00-2 2v11a2 2 0 002 2h11a2 2 0 002-2v-5m-1.414-9.414a2 2 0 112.828 2.828L11.828 15H9v-2.828l8.586-8.586z" />
                  </svg>
                ) : (
                  <svg xmlns="http://www.w3.org/2000/svg" className="h-5 w-5 mr-2" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 6v6m0 0v6m0-6h6m-6 0H6" />
                  </svg>
                )}
                {editMode ? 'Update Address' : 'Save Address'}
              </button>
              
              {editMode && (
                <button 
                  type="button" 
                  onClick={handleCancel}
                  className="border border-gray-300 text-gray-700 font-medium py-3 px-8 rounded-lg hover:bg-gray-50 transition"
                >
                  Cancel
                </button>
              )}
            </div>
          </div>
        </form>
      </div>

      {/* Address List */}
      <div className="px-6 pb-6">
        <h2 className="text-2xl font-semibold text-[#495E57] mb-4">Saved Addresses</h2>
        
        {loading && !addresses.length ? (
          <div className="flex justify-center p-8">
            <div className="w-12 h-12 border-4 border-t-4 border-t-[#F4CE14] rounded-full animate-spin border-gray-200"></div>
          </div>
        ) : !addresses.length ? (
          <div className="bg-gray-50 p-8 text-center rounded-lg border border-gray-200">
            <p className="text-gray-500">You haven't added any addresses yet.</p>
          </div>
        ) : (
          <div className="space-y-4">
            {addresses.map((address) => (
              <div 
                key={address.addressId} 
                className={`bg-white rounded-lg border ${address.addressId === providerAddressId ? 'border-[#F4CE14] shadow-md' : 'border-gray-200'}`}
              >
                <div className="p-4">
                  <div className="flex flex-col md:flex-row justify-between">
                    <div className="mb-4 md:mb-0">
                      <div className="flex items-center mb-1">
                        <h3 className="text-lg font-medium">
                          {address.streetName}, {address.barangay}
                        </h3>
                        {address.addressId === providerAddressId && (
                          <span className="ml-2 bg-[#F4CE14] text-[#495E57] text-xs font-bold px-2 py-1 rounded">
                            Main Address
                          </span>
                        )}
                      </div>
                      <p className="text-gray-600">
                        {address.city}, {address.province} {address.zipCode}
                      </p>
                    </div>
                    
                    <div className="flex items-center space-x-2">
                      {address.addressId !== providerAddressId && (
                        <button 
                          onClick={() => setAsMainAddress(address.addressId)}
                          className="text-[#495E57] border border-[#495E57] text-sm font-medium px-3 py-1 rounded hover:bg-[#495E57] hover:text-white transition"
                        >
                          Set as Main
                        </button>
                      )}
                      
                      <button 
                        onClick={() => handleEdit(address)}
                        className="bg-gray-100 text-gray-700 p-2 rounded hover:bg-gray-200 transition"
                      >
                        <svg xmlns="http://www.w3.org/2000/svg" className="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                          <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M11 5H6a2 2 0 00-2 2v11a2 2 0 002 2h11a2 2 0 002-2v-5m-1.414-9.414a2 2 0 112.828 2.828L11.828 15H9v-2.828l8.586-8.586z" />
                        </svg>
                      </button>
                      
                      <button 
                        onClick={() => openDeleteDialog(address.addressId)}
                        className={`p-2 rounded ${address.addressId === providerAddressId 
                          ? 'bg-gray-100 text-gray-400 cursor-not-allowed' 
                          : 'bg-gray-100 text-red-500 hover:bg-gray-200 transition'}`}
                        disabled={address.addressId === providerAddressId}
                      >
                        <svg xmlns="http://www.w3.org/2000/svg" className="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                          <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16" />
                        </svg>
                      </button>
                    </div>
                  </div>
                </div>
              </div>
            ))}
          </div>
        )}
      </div>

      {/* Delete Confirmation Dialog */}
      {deleteDialogOpen && (
        <div className="fixed inset-0 z-50 overflow-y-auto bg-black bg-opacity-50 flex items-center justify-center">
          <div className="bg-white rounded-lg max-w-md w-full p-6 shadow-xl">
            <h3 className="text-lg font-medium text-gray-900 mb-4">Confirm Deletion</h3>
            <p className="text-gray-600 mb-6">
              Are you sure you want to delete this address? This action cannot be undone.
            </p>
            <div className="flex justify-end space-x-3">
              <button
                onClick={() => setDeleteDialogOpen(false)}
                className="border border-gray-300 text-gray-700 px-4 py-2 rounded hover:bg-gray-50 transition"
              >
                Cancel
              </button>
              <button
                onClick={() => handleDelete(addressToDelete)}
                className="bg-red-600 text-white px-4 py-2 rounded hover:bg-red-700 transition"
              >
                Delete
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}

export default AddressContent;