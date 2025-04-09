import React, { useState, useEffect } from 'react';
import {
  Box,
  Button,
  Grid,
  TextField,
  Typography,
  CircularProgress,
  Alert,
  IconButton,
  Card,
  CardContent,
  Dialog,
  DialogActions,
  DialogContent,
  DialogContentText,
  DialogTitle,
} from '@mui/material';
import axios from 'axios';
import DeleteIcon from '@mui/icons-material/Delete';
import EditIcon from '@mui/icons-material/Edit';
import AddIcon from '@mui/icons-material/Add';

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
// In your handleSubmit function, update this section:
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
    <>
      <Box>
        <Typography variant="h4" gutterBottom>My Address</Typography>
        <Typography variant="body1" color="text.secondary" sx={{ mb: 4 }}>
          {editMode ? 'Edit your address' : 'Add a new address'}
        </Typography>
      </Box>

      {error && (
        <Alert severity="error" sx={{ mb: 2 }}>{error}</Alert>
      )}

      {success && (
        <Alert severity="success" sx={{ mb: 2 }}>{success}</Alert>
      )}

      <form onSubmit={handleSubmit}>
        <Grid container spacing={2}>
          <Grid item xs={12}>
            <TextField
              fullWidth
              name="barangay"
              label="Barangay"
              variant="outlined"
              value={addressForm.barangay}
              onChange={handleChange}
              required
            />
          </Grid>
          <Grid item xs={12} md={6}>
            <TextField
              fullWidth
              name="city"
              label="City"
              variant="outlined"
              value={addressForm.city}
              onChange={handleChange}
              required
            />
          </Grid>
          <Grid item xs={12} md={6}>
            <TextField
              fullWidth
              name="province"
              label="Province"
              variant="outlined"
              value={addressForm.province}
              onChange={handleChange}
              required
            />
          </Grid>
          <Grid item xs={12} md={6}>
            <TextField
              fullWidth
              name="streetName"
              label="Street Name"
              variant="outlined"
              value={addressForm.streetName}
              onChange={handleChange}
              required
            />
          </Grid>
          <Grid item xs={12} md={6}>
            <TextField
              fullWidth
              name="zipCode"
              label="Zip Code"
              variant="outlined"
              value={addressForm.zipCode}
              onChange={handleChange}
              required
            />
          </Grid>
        </Grid>

        <Box sx={{ display: 'flex', justifyContent: 'center', mt: 4, gap: 2 }}>
          <Button 
            type="submit" 
            variant="contained" 
            sx={{ width: 150 }}
            disabled={loading}
            startIcon={editMode ? <EditIcon /> : <AddIcon />}
          >
            {loading ? <CircularProgress size={24} /> : (editMode ? 'Update' : 'Save')}
          </Button>
          {editMode && (
            <Button 
              variant="outlined" 
              sx={{ width: 150 }}
              onClick={handleCancel}
            >
              Cancel
            </Button>
          )}
        </Box>
      </form>

      {/* List of Addresses */}
      <Box sx={{ mt: 6 }}>
        <Typography variant="h5" gutterBottom>My Addresses</Typography>
        
        {loading && !addresses.length ? (
          <Box sx={{ display: 'flex', justifyContent: 'center', p: 4 }}>
            <CircularProgress />
          </Box>
        ) : !addresses.length ? (
          <Typography variant="body1" color="text.secondary" sx={{ my: 2 }}>
            No addresses added yet.
          </Typography>
        ) : (
          <Grid container spacing={2} sx={{ mt: 2 }}>
            {addresses.map((address) => (
              <Grid item xs={12} key={address.addressId}>
                <Card sx={{
                  border: address.addressId === providerAddressId ? '2px solid #4caf50' : 'none',
                  boxShadow: address.addressId === providerAddressId ? '0 0 8px rgba(76, 175, 80, 0.5)' : undefined
                }}>
                  <CardContent>
                    <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start' }}>
                      <Box>
                        <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                          <Typography variant="h6">
                            {address.streetName}, {address.barangay}
                          </Typography>
                          {address.addressId === providerAddressId && (
                            <Alert severity="success" sx={{ py: 0, px: 1, height: 24 }}>Main</Alert>
                          )}
                        </Box>
                        <Typography variant="body1">
                          {address.city}, {address.province} {address.zipCode}
                        </Typography>
                      </Box>
                      <Box>
                        {address.addressId !== providerAddressId && (
                          <Button 
                            variant="outlined" 
                            size="small" 
                            sx={{ mr: 1 }}
                            onClick={() => setAsMainAddress(address.addressId)}
                          >
                            Set as Main
                          </Button>
                        )}
                        <IconButton onClick={() => handleEdit(address)} color="primary">
                          <EditIcon />
                        </IconButton>
                        <IconButton 
                          onClick={() => openDeleteDialog(address.addressId)} 
                          color="error"
                          disabled={address.addressId === providerAddressId}
                        >
                          <DeleteIcon />
                        </IconButton>
                      </Box>
                    </Box>
                  </CardContent>
                </Card>
              </Grid>
            ))}
          </Grid>
        )}
      </Box>

      {/* Delete Confirmation Dialog */}
      <Dialog
        open={deleteDialogOpen}
        onClose={() => setDeleteDialogOpen(false)}
      >
        <DialogTitle>Confirm Deletion</DialogTitle>
        <DialogContent>
          <DialogContentText>
            Are you sure you want to delete this address? This action cannot be undone.
          </DialogContentText>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setDeleteDialogOpen(false)}>Cancel</Button>
          <Button 
            onClick={() => handleDelete(addressToDelete)} 
            color="error" 
            autoFocus
          >
            Delete
          </Button>
        </DialogActions>
      </Dialog>
    </>
  );
}

export default AddressContent;