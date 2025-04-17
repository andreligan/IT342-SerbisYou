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
  
  // PSGC API data states
  const [provinces, setProvinces] = useState([]);
  const [cities, setCities] = useState([]);
  const [barangays, setBarangays] = useState([]);
  const [selectedProvinceCode, setSelectedProvinceCode] = useState('');
  const [selectedCityCode, setSelectedCityCode] = useState('');
  const [selectedBarangayCode, setSelectedBarangayCode] = useState('');
  
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
  
  // Extended loading state for address data
  const [isLoading, setIsLoading] = useState({
    provinces: false,
    cities: false,
    barangays: false
  });
  
  // Store the complete provider data to avoid refetching
  const [providerData, setProviderData] = useState(null);
  
  // Get userId and token from localStorage or sessionStorage
  const userId = localStorage.getItem('userId') || sessionStorage.getItem('userId');
  const token = localStorage.getItem('authToken') || sessionStorage.getItem('authToken');
  
  // First fetch provinces on component mount
  useEffect(() => {
    const fetchProvinces = async () => {
      setIsLoading(prev => ({ ...prev, provinces: true }));
      try {
        const response = await axios.get('https://psgc.gitlab.io/api/provinces');
        setProvinces(response.data);
      } catch (error) {
        console.error("Failed to fetch provinces:", error);
        setError("Failed to fetch provinces. Please try again later.");
      } finally {
        setIsLoading(prev => ({ ...prev, provinces: false }));
      }
    };
    
    fetchProvinces();
  }, []);
  
  // Modified approach for fetching and displaying addresses
  useEffect(() => {
    const fetchProviderAddresses = async () => {
      try {
        setLoading(true);
        setError(null);
        
        // Step 1: Get userId from storage
        const userId = localStorage.getItem('userId') || sessionStorage.getItem('userId');
        const token = localStorage.getItem('authToken') || sessionStorage.getItem('authToken');
        
        if (!userId || !token) {
          setError("Authentication information missing. Please login again.");
          setLoading(false);
          return;
        }
        
        // Step 2: Get user role (optional - can be skipped if all users of this component are service providers)
        // If you have role stored in localStorage/sessionStorage:
        const userRole = localStorage.getItem('userRole') || sessionStorage.getItem('userRole');
        
        if (userRole !== 'Service Provider') {
          setError("Only service providers can manage addresses");
          setLoading(false);
          return;
        }
        
        // Step 3: Get all service providers and find match by userId
        const providersResponse = await axios.get("/api/service-providers/getAll", {
          headers: { 'Authorization': `Bearer ${token}` }
        });
        
        const provider = providersResponse.data.find(
          p => p.userAuth && p.userAuth.userId == userId
        );

        console.log("Provider data:", provider);
        
        if (!provider) {
          setError("No service provider profile found for this account.");
          setLoading(false);
          return;
        }
        
        // Step 4: Store the provider ID and complete provider data
        setProviderId(provider.providerId);
        setProviderData(provider);
        
        // If provider has a main address, store its ID
        if (provider.address) {
          setProviderAddressId(provider.address.addressId);
        }
        
        // Step 5: Get all addresses
        const addressesResponse = await axios.get('/api/addresses/getAll', {
          headers: { 'Authorization': `Bearer ${token}` }
        });

        console.log("All addresses:", addressesResponse.data);
        
        // Step 6: Filter addresses by providerId
        const providerAddresses = addressesResponse.data.filter(
          address => address.serviceProvider?.providerId === provider.providerId || address.providerId === provider.providerId
        );

        console.log("Filtered provider addresses:", providerAddresses);
        
        // Step 7: Set the addresses to display
        setAddresses(providerAddresses);
        
        setLoading(false);
        
      } catch (err) {
        console.error('Error fetching provider addresses:', err);
        setError('Failed to load addresses. Please try again later.');
        setLoading(false);
      }
    };
    
    fetchProviderAddresses();
  }, [token]); // Re-run when token changes

  // Add this new useEffect that depends on both provinces and addresses
  useEffect(() => {
    const initializeFormData = async () => {
      // Only proceed when both data sets are available
      if (provinces.length > 0 && addresses.length > 0) {
        // Find the main address or use the first one
        const mainAddress = addresses.find(addr => 
          addr.addressId === providerAddressId
        ) || addresses[0];
        
        // Try to populate the form with this address data
        await populateAddressForm(mainAddress);
      }
    };
    
    initializeFormData();
  }, [provinces, addresses, providerAddressId]);
  
  // New function to populate form without entering edit mode
  const populateAddressForm = async (address) => {
    setAddressForm({
      barangay: address.barangay || '',
      city: address.city || '',
      province: address.province || '',
      streetName: address.streetName || '',
      zipCode: address.zipCode || ''
    });
    
    // Find and select the province code that matches the province name
    setIsLoading(prev => ({ ...prev, provinces: true }));
    try {
      // Find the province code matching the province name in the database
      const matchedProvince = provinces.find(p => p.name === address.province);
      
      if (matchedProvince) {
        setSelectedProvinceCode(matchedProvince.code);
        
        // Load cities for this province
        setIsLoading(prev => ({ ...prev, cities: true }));
        const citiesResponse = await axios.get(`https://psgc.gitlab.io/api/provinces/${matchedProvince.code}/cities-municipalities`);
        setCities(citiesResponse.data);
        setIsLoading(prev => ({ ...prev, cities: false }));
        
        // Find and select the city code matching the city name
        const matchedCity = citiesResponse.data.find(c => c.name === address.city);
        
        if (matchedCity) {
          setSelectedCityCode(matchedCity.code);
          
          // Load barangays for this city
          setIsLoading(prev => ({ ...prev, barangays: true }));
          const barangaysResponse = await axios.get(`https://psgc.gitlab.io/api/cities-municipalities/${matchedCity.code}/barangays`);
          setBarangays(barangaysResponse.data);
          
          // Find and select the barangay that matches the address
          const matchedBarangay = barangaysResponse.data.find(b => b.name === address.barangay);
          if (matchedBarangay) {
            setSelectedBarangayCode(matchedBarangay.code);
          }
          
          setIsLoading(prev => ({ ...prev, barangays: false }));
        }
      }
    } catch (error) {
      console.error("Error loading address location data:", error);
    } finally {
      setIsLoading(prev => ({ ...prev, provinces: false }));
    }
  };
  
  // Handle province change - fetch cities
  const handleProvinceChange = async (e) => {
    const provinceCode = e.target.value;
    const provinceName = e.target.options[e.target.selectedIndex].text;
    
    setSelectedProvinceCode(provinceCode);
    setAddressForm(prev => ({ 
      ...prev, 
      province: provinceName,
      city: '',
      barangay: '' 
    }));
    
    setCities([]);
    setBarangays([]);
    
    if (!provinceCode) return;
    
    setIsLoading(prev => ({ ...prev, cities: true }));
    try {
      const response = await axios.get(`https://psgc.gitlab.io/api/provinces/${provinceCode}/cities-municipalities`);
      setCities(response.data);
    } catch (error) {
      console.error("Failed to fetch cities:", error);
      setError("Failed to load cities for the selected province.");
    } finally {
      setIsLoading(prev => ({ ...prev, cities: false }));
    }
  };
  
  // Handle city change - fetch barangays
  const handleCityChange = async (e) => {
    const cityCode = e.target.value;
    const cityName = e.target.options[e.target.selectedIndex].text;
    
    setSelectedCityCode(cityCode);
    setAddressForm(prev => ({ 
      ...prev, 
      city: cityName,
      barangay: '' 
    }));
    
    setBarangays([]);
    
    if (!cityCode) return;
    
    setIsLoading(prev => ({ ...prev, barangays: true }));
    try {
      const response = await axios.get(`https://psgc.gitlab.io/api/cities-municipalities/${cityCode}/barangays`);
      setBarangays(response.data);
    } catch (error) {
      console.error("Failed to fetch barangays:", error);
      setError("Failed to load barangays for the selected city.");
    } finally {
      setIsLoading(prev => ({ ...prev, barangays: false }));
    }
  };
  
  // Handle barangay selection
  const handleBarangayChange = (e) => {
    const barangayCode = e.target.value;
    setSelectedBarangayCode(barangayCode);
    const barangayName = e.target.options[e.target.selectedIndex].text;
    setAddressForm(prev => ({ ...prev, barangay: barangayName }));
  };
  
  // Handle other input changes
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
        // Find the current address to get its main status
        const currentAddress = addresses.find(addr => addr.addressId === currentAddressId);
        
        const addressPayload = {
          ...addressForm,
          providerId: providerId,
          serviceProviderId: providerId,
          // Preserve the main status when editing
          main: currentAddress?.main || false
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
          
        } catch (updateErr) {
          console.error('Error updating address:', updateErr);
          console.error('Error response:', updateErr.response?.data);
          throw updateErr;
        }
      } else {
        // Create a new address with main explicitly set to false
        const response = await axios.post(
          '/api/addresses/postAddress', 
          {
            ...addressForm,
            main: false, // Always set main to false for new addresses
            serviceProvider: {
              providerId: providerId
            }
          },
          {
            headers: { 'Authorization': `Bearer ${token}` }
          }
        );
        
        const newAddressId = response.data.addressId;
        
        // If this is the first address, set it as the provider's main address
        if (addresses.length === 0) {
          // Get the full address object from response
          const newAddress = response.data;
          
          // Create complete object with main=true
          const mainAddress = {
            ...newAddress,
            main: true
          };
          
          await axios.put(
            `/api/addresses/updateAddress/${newAddressId}`, 
            mainAddress,
            { headers: { 'Authorization': `Bearer ${token}` }}
          );
          
          await axios.put(
            `/api/service-providers/update/${providerId}`, 
            { addressId: newAddressId },
            { headers: { 'Authorization': `Bearer ${token}` }}
          );
          
          setProviderAddressId(newAddressId);
          
          // Update the response data to reflect main status
          response.data.main = true;
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
      setSelectedProvinceCode('');
      setSelectedCityCode('');
      setSelectedBarangayCode('');
      setCities([]);
      setBarangays([]);
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
      
      // Find the address to check if it's main
      const addressToDelete = addresses.find(addr => addr.addressId === addressId);
      
      // Check if this is a main address
      if (addressToDelete && addressToDelete.main) {
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
      
      // CORRECT APPROACH: Get all providers and find by userId
      const userId = localStorage.getItem('userId') || sessionStorage.getItem('userId');
      
      // Get all service providers
      const providersResponse = await axios.get(
        "/api/service-providers/getAll", 
        { headers: { 'Authorization': `Bearer ${token}` }}
      );
      
      // Find the one that matches our userId
      const completeProvider = providersResponse.data.find(
        p => p.userAuth && p.userAuth.userId == userId
      );
      
      if (!completeProvider) {
        throw new Error("Provider not found");
      }
      
      console.log("Complete provider data:", completeProvider);
      
      // Find the address to update
      const addressToUpdate = addresses.find(addr => addr.addressId === addressId);
      
      if (!addressToUpdate) {
        throw new Error("Address not found");
      }
      
      console.log("Setting as main address:", addressToUpdate);
      
      // Send complete address object with main set to true AND include complete provider
      const updatedMainAddress = {
        ...addressToUpdate,
        main: true,
        // Include the COMPLETE service provider to maintain all attributes
        serviceProvider: {
          providerId: providerId,
          businessName: completeProvider.businessName,
          firstName: completeProvider.firstName,
          lastName: completeProvider.lastName,
          paymentMethod: completeProvider.paymentMethod,
          phoneNumber: completeProvider.phoneNumber,
          status: completeProvider.status,
        }
      };
      
      console.log("Complete provider data:", completeProvider);
      
      // Update the address to set main to true
      const updateResponse = await axios.put(
        `/api/addresses/updateAddress/${addressId}`, 
        updatedMainAddress,
        { headers: { 'Authorization': `Bearer ${token}` }}
      );
      
      console.log("Update response:", updateResponse.data);
      console.log("Complete provider data:", completeProvider);
      
      // Set all other addresses to main: false (also preserving full serviceProvider)
      for (const addr of addresses) {
        if (addr.addressId !== addressId && addr.main) {
          const otherAddress = {
            ...addr,
            main: false,
            // Include the COMPLETE service provider to maintain all attributes
            serviceProvider: {
              providerId: providerId,
              businessName: completeProvider.businessName,
              firstName: completeProvider.firstName,
              lastName: completeProvider.lastName,
              paymentMethod: completeProvider.paymentMethod,
              phoneNumber: completeProvider.phoneNumber,
              status: completeProvider.status,
            }
          };
          
          await axios.put(
            `/api/addresses/updateAddress/${addr.addressId}`, 
            otherAddress,
            { headers: { 'Authorization': `Bearer ${token}` }}
          );
        }
      }
    
      // Update the provider's primary address in the provider entity
      await axios.put(
        `/api/service-providers/update/${completeProvider.providerId}`, 
        {
          addressId: addressId,
          firstName: completeProvider.firstName,
          lastName: completeProvider.lastName,
          phoneNumber: completeProvider.phoneNumber,
          businessName: completeProvider.businessName,
          yearsOfExperience: completeProvider.yearsOfExperience,
          status: completeProvider.status
        },
        { headers: { 'Authorization': `Bearer ${token}` }}
      );
      
      setProviderAddressId(addressId);
      
      // Update addresses in local state
      setAddresses(prev => prev.map(addr => ({
        ...addr,
        main: addr.addressId === addressId
      })));
      
      setSuccess('Main address updated successfully!');
      setLoading(false);
      
      setTimeout(() => setSuccess(null), 3000);
      
    } catch (err) {
      console.error('Error updating main address:', err);
      setError('Failed to update main address. Please try again.');
      setLoading(false);
    }
  };
  
  // Update handleEdit to use the common populateAddressForm function
  const handleEdit = async (address) => {
    await populateAddressForm(address);
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
    setSelectedProvinceCode('');
    setSelectedCityCode('');
    setSelectedBarangayCode('');
    setCities([]);
    setBarangays([]);
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
              {/* Province Dropdown - Using PSGC API */}
              <div>
                <label className="block text-gray-700 text-sm font-medium mb-2">Province *</label>
                <select
                  name="province"
                  value={selectedProvinceCode}
                  onChange={handleProvinceChange}
                  required
                  className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-[#F4CE14]"
                >
                  <option value="">Select Province</option>
                  {provinces.map(province => (
                    <option key={province.code} value={province.code}>
                      {province.name}
                    </option>
                  ))}
                </select>
                {isLoading.provinces && <span className="text-sm text-gray-500">Loading provinces...</span>}
                {editMode && !selectedProvinceCode && addressForm.province && (
                  <p className="text-sm text-amber-600 mt-1">
                    Current value: {addressForm.province}. Select from dropdown to change.
                  </p>
                )}
              </div>
              
              {/* City Dropdown - Using PSGC API */}
              <div>
                <label className="block text-gray-700 text-sm font-medium mb-2">City/Municipality *</label>
                <select
                  name="city"
                  value={selectedCityCode}
                  onChange={handleCityChange}
                  disabled={!selectedProvinceCode && !editMode}
                  required
                  className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-[#F4CE14]"
                >
                  <option value="">Select City/Municipality</option>
                  {cities.map(city => (
                    <option key={city.code} value={city.code}>
                      {city.name}
                    </option>
                  ))}
                </select>
                {isLoading.cities && <span className="text-sm text-gray-500">Loading cities...</span>}
                {editMode && !selectedCityCode && addressForm.city && (
                  <p className="text-sm text-amber-600 mt-1">
                    Current value: {addressForm.city}. Select province first to update.
                  </p>
                )}
              </div>
              
              {/* Barangay Dropdown - Using PSGC API */}
              <div>
                <label className="block text-gray-700 text-sm font-medium mb-2">Barangay *</label>
                <select
                  name="barangay"
                  value={selectedBarangayCode}
                  onChange={handleBarangayChange}
                  disabled={!selectedCityCode && !editMode}
                  required
                  className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-[#F4CE14]"
                >
                  <option value="">Select Barangay</option>
                  {barangays.map(barangay => (
                    <option key={barangay.code} value={barangay.code}>
                      {barangay.name}
                    </option>
                  ))}
                </select>
                {isLoading.barangays && <span className="text-sm text-gray-500">Loading barangays...</span>}
                {editMode && !barangays.length && addressForm.barangay && (
                  <p className="text-sm text-amber-600 mt-1">
                    Current value: {addressForm.barangay}. Select city first to update.
                  </p>
                )}
              </div>
              
              {/* Street Name Input */}
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
              
              {/* Zip Code Input */}
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
      {/* Rest of the component remains the same */}
      <div className="px-6 pb-6">
        {/* Address list code remains unchanged */}
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
                className={`bg-white rounded-lg border ${address.main ? 'border-[#F4CE14] shadow-md' : 'border-gray-200'}`}
              >
                <div className="p-4">
                  <div className="flex flex-col md:flex-row justify-between">
                    <div className="mb-4 md:mb-0">
                      <div className="flex items-center mb-1">
                        <h3 className="text-lg font-medium">
                          {address.streetName}, {address.barangay}
                        </h3>
                        {address.main && (
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
                      {!address.main && (
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
                        className={`p-2 rounded ${address.main 
                          ? 'bg-gray-100 text-gray-400 cursor-not-allowed' 
                          : 'bg-gray-100 text-red-500 hover:bg-gray-200 transition'}`}
                        disabled={address.main}
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