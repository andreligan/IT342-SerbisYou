import { useState, useEffect } from 'react';
import { fetchUserAddresses, updateAddressMain, createAddress, updateAddress, deleteAddress } from '../utils/addressApi';

/**
 * Custom hook for managing addresses
 * @param {string} userType - 'Service Provider' or 'Customer'
 */
export const useAddressManager = (userType) => {
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
  const [userId, setUserId] = useState(null);
  const [userInfo, setUserInfo] = useState(null);
  const [userAddressId, setUserAddressId] = useState(null);
  
  // Extended loading state for address data
  const [isLoading, setIsLoading] = useState({
    provinces: false,
    cities: false,
    barangays: false
  });

  // Get userId and token
  useEffect(() => {
    const storedUserId = localStorage.getItem('userId') || sessionStorage.getItem('userId');
    const token = localStorage.getItem('authToken') || sessionStorage.getItem('authToken');
    
    if (storedUserId && token) {
      setUserId(storedUserId);
      
      // Fetch user addresses based on user type
      fetchAddresses(storedUserId, token, userType);
    }
  }, [userType]);

  // Fetch user addresses
  const fetchAddresses = async (userId, token, userType) => {
    try {
      setLoading(true);
      setError(null);
      
      const { addresses, userInfo, mainAddressId } = await fetchUserAddresses(userId, token, userType);
      
      setAddresses(addresses);
      setUserInfo(userInfo);
      setUserAddressId(mainAddressId);
      
      setLoading(false);
    } catch (err) {
      console.error(`Error fetching ${userType.toLowerCase()} addresses:`, err);
      setError(`Failed to load addresses. Please try again later.`);
      setLoading(false);
    }
  };

  // Handle form submission
  const handleSubmit = async (e) => {
    e.preventDefault();
    
    try {
      setLoading(true);
      setError(null);
      
      const token = localStorage.getItem('authToken') || sessionStorage.getItem('authToken');
      
      // Fix: Extract the correct ID field based on user type
      let entityId;
      if (!userInfo) {
        throw new Error(`User information not loaded. Please refresh the page.`);
      }
      
      if (userType === 'Service Provider') {
        entityId = userInfo.providerId;
        console.log("Using provider ID:", entityId);
        
        if (!entityId) {
          throw new Error("Provider ID not found in user data. Please refresh the page.");
        }
      } else { // Customer
        entityId = userInfo.customerId;
        console.log("Using customer ID:", entityId);
        
        if (!entityId) {
          throw new Error("Customer ID not found in user data. Please refresh the page.");
        }
      }
      
      // If in edit mode, update the address
      if (editMode && currentAddressId) {
        const currentAddress = addresses.find(addr => addr.addressId === currentAddressId);
        
        const updatedAddress = await updateAddress(
          token,
          currentAddressId,
          addressForm,
          entityId, // Now we're sure this is defined
          currentAddress?.main || false,
          userType
        );
        
        setSuccess('Address updated successfully!');
        
        // Update the addresses list
        setAddresses(prev => 
          prev.map(addr => addr.addressId === currentAddressId ? updatedAddress : addr)
        );
        
      } else {
        // Create a new address
        const newAddress = await createAddress(
          token,
          addressForm,
          entityId, // Now we're sure this is defined
          addresses.length === 0,
          userType
        );
        
        setSuccess('New address added successfully!');
        
        // Add the new address to the list
        setAddresses(prev => [...prev, newAddress]);
        
        // If this was the first address, set it as main
        if (addresses.length === 0) {
          setUserAddressId(newAddress.addressId);
        }
      }
      
      // Reset form and state
      resetForm();
      setLoading(false);
      
      // Auto-hide success message after 3 seconds
      setTimeout(() => setSuccess(null), 3000);
      
    } catch (err) {
      console.error('Error saving address:', err);
      setError(err.message || 'Failed to save address. Please try again.');
      setLoading(false);
    }
  };

  // Handle address deletion
  const handleDelete = async (addressId) => {
    try {
      setLoading(true);
      setError(null);
      
      const token = localStorage.getItem('authToken') || sessionStorage.getItem('authToken');
      
      // Find the address to check if it's main
      const addressToDelete = addresses.find(addr => addr.addressId === addressId);
      
      // Check if this is a main address
      if (addressToDelete && addressToDelete.main) {
        setError("Cannot delete your main address. Please set another address as primary first.");
        setLoading(false);
        setDeleteDialogOpen(false);
        return;
      }
      
      await deleteAddress(token, addressId);
      
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

  // Set an address as the user's main address
  const setAsMainAddress = async (addressId) => {
    try {
      setLoading(true);
      
      const token = localStorage.getItem('authToken') || sessionStorage.getItem('authToken');
      
      await updateAddressMain(token, addressId, addresses, userInfo, userType);
      
      setUserAddressId(addressId);
      
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
  
  // Reset the form
  const resetForm = () => {
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

  return {
    // State
    addressForm,
    setAddressForm,
    provinces,
    setProvinces,
    cities,
    setCities,
    barangays,
    setBarangays,
    selectedProvinceCode,
    setSelectedProvinceCode,
    selectedCityCode,
    setSelectedCityCode,
    selectedBarangayCode,
    setSelectedBarangayCode,
    addresses,
    loading,
    error,
    success,
    editMode,
    setEditMode,
    currentAddressId,
    setCurrentAddressId,
    deleteDialogOpen,
    setDeleteDialogOpen,
    addressToDelete,
    setAddressToDelete,
    isLoading,
    setIsLoading,
    
    // Methods
    handleSubmit,
    handleDelete,
    setAsMainAddress,
    resetForm
  };
};

export default useAddressManager;
