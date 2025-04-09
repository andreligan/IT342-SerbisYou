import React, { useState, useEffect } from 'react';
import {
  Box,
  Button,
  Grid,
  TextField,
  Typography,
  Avatar,
  CircularProgress,
  Alert,
  Switch,
  FormControlLabel,
} from '@mui/material';
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
        
        // Store the providerId and userAuthId for later use
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
    
// Inside handleSubmit function
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
      <Box sx={{ display: 'flex', justifyContent: 'center', p: 4 }}>
        <CircularProgress />
      </Box>
    );
  }

  return (
    <>
      <Box>
        <Typography variant="h4" gutterBottom>My Profile</Typography>
        <Typography variant="body1" color="text.secondary" sx={{ mb: 4 }}>
          Manage and protect your account
        </Typography>
      </Box>

      {error && (
        <Alert severity="error" sx={{ mb: 2 }}>{error}</Alert>
      )}

      {success && (
        <Alert severity="success" sx={{ mb: 2 }}>Profile updated successfully!</Alert>
      )}

      <form onSubmit={handleSubmit}>
        <Box sx={{ display: 'flex', flexDirection: 'row', gap: 5, paddingLeft: 5 }}>
          <Grid container spacing={2} direction="column">
            <Typography variant="h6" gutterBottom>Account Information</Typography>
            <Grid item xs={12}>
              <TextField
                fullWidth
                name="userName"
                label="userName"
                variant="outlined"
                value={formData.userName}
                onChange={handleChange}
          sx={{ minWidth: '500px' }}
              />
            </Grid>
            <Grid item xs={12}>
              <TextField
                fullWidth
                name="email"
                label="Email"
                type="email"
                variant="outlined"
                value={formData.email}
                onChange={handleChange}
              />
            </Grid>
            
            <Typography variant="h6" gutterBottom sx={{ mt: 3 }}>Personal Information</Typography>
            <Grid item xs={12}>
              <TextField
                fullWidth
                name="firstName"
                label="firstName"
                variant="outlined"
                value={formData.firstName}
                onChange={handleChange}
              />
            </Grid>
            <Grid item xs={12}>
              <TextField
                fullWidth
                name="lastName"
                label="Last Name"
                variant="outlined"
                value={formData.lastName}
                onChange={handleChange}
              />
            </Grid>
            <Grid item xs={12}>
              <TextField
                fullWidth
                name="phoneNumber"
                label="Phone Number"
                type="tel"
                variant="outlined"
                value={formData.phoneNumber}
                onChange={handleChange}
              />
            </Grid>
            
            <Typography variant="h6" gutterBottom sx={{ mt: 3 }}>Business Information</Typography>
            <Grid item xs={12}>
              <TextField
                fullWidth
                name="businessName"
                label="Business Name"
                variant="outlined"
                value={formData.businessName}
                onChange={handleChange}
              />
            </Grid>
            <Grid item xs={12}>
              <TextField
                fullWidth
                name="yearsOfExperience"
                label="Years of Experience"
                type="number"
                variant="outlined"
                value={formData.yearsOfExperience}
                onChange={handleChange}
              />
            </Grid>
            <Grid item xs={12}>
              <TextField
                fullWidth
                name="availabilitySchedule"
                label="Availability Schedule"
                variant="outlined"
                value={formData.availabilitySchedule}
                onChange={handleChange}
              />
            </Grid>
            <Grid item xs={12}>
              <TextField
                fullWidth
                name="paymentMethod"
                label="Payment Method"
                variant="outlined"
                value={formData.paymentMethod}
                onChange={handleChange}
              />
            </Grid>
            <Grid item xs={12}>
              <TextField
                fullWidth
                name="status"
                label="Status"
                variant="outlined"
                value={formData.status}
                onChange={handleChange}
              />
            </Grid>
            <Grid item xs={12}>
              <FormControlLabel
                control={
                  <Switch
                    name="verified"
                    checked={formData.verified}
                    onChange={handleSwitchChange}
                    disabled={true} // Typically verification is handled by admins
                  />
                }
                label="Verified"
              />
            </Grid>
          </Grid>
          
          <Grid item xs={12} sx={{ display: 'flex', justifyContent: 'center', alignItems: 'flex-start'}}>
            <Box sx={{ display: 'flex', flexDirection:'column', alignItems: 'center', gap: 2 }}>
              <Box>
                <Avatar
                  src={selectedImage}
                  sx={{ width: 200, height: 200 }}
                />
              </Box>
              <input
                accept="image/*"
                style={{ display: 'none' }}
                id="profile-image-upload"
                type="file"
                onChange={handleImageUpload}
              />
              <label htmlFor="profile-image-upload">
                <Button
                  variant="outlined"
                  component="span"
                  sx={{ width: 150, fontSize: 30 }}
                >
                  +
                </Button>
              </label>
            </Box>
          </Grid>
        </Box>

        <Box sx={{ display: 'flex', justifyContent: 'center', mt: 4}}>
          <Button 
            type="submit" 
            variant="contained" 
            sx={{ width: 150 }}
            disabled={loading}
          >
            {loading ? <CircularProgress size={24} /> : 'Save'}
          </Button>
        </Box>
      </form>
    </>
  );
}

export default ProfileContent;