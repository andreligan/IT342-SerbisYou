import React, { useState, useEffect } from 'react';
import {
  Box,
  Button,
  Typography,
  CircularProgress,
  Alert,
  Card,
  CardContent,
  CardActions,
  Grid,
  TextField,
  Dialog,
  DialogActions,
  DialogContent,
  DialogContentText,
  DialogTitle,
  FormControl,
  InputLabel,
  Select,
  MenuItem,
  Divider,
  Chip,
  IconButton,
  Accordion,
  AccordionSummary,
  AccordionDetails
} from '@mui/material';
import axios from 'axios';
import AddIcon from '@mui/icons-material/Add';
import EditIcon from '@mui/icons-material/Edit';
import DeleteIcon from '@mui/icons-material/Delete';
import ExpandMoreIcon from '@mui/icons-material/ExpandMore';

function MyServicesContent() {
  // States for data
  const [providerId, setProviderId] = useState(null);
  const [services, setServices] = useState([]);
  const [categories, setCategories] = useState([]);
  const [servicesByCategory, setServicesByCategory] = useState({});
  
  // States for UI
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);
  const [success, setSuccess] = useState(null);
  
  // States for dialogs
  const [openAddDialog, setOpenAddDialog] = useState(false);
  const [openEditDialog, setOpenEditDialog] = useState(false);
  const [openDeleteDialog, setOpenDeleteDialog] = useState(false);
  
  // State for current service
  const [currentService, setCurrentService] = useState({
    serviceId: null,
    serviceName: '',
    serviceDescription: '',
    priceRange: '',
    durationEstimate: '',
    categoryId: ''
  });
  
  // Get userId and token from localStorage or sessionStorage
  const userId = localStorage.getItem('userId') || sessionStorage.getItem('userId');
  const token = localStorage.getItem('authToken') || sessionStorage.getItem('authToken');
  
  // Step 1: Fetch the service provider first to get their providerId
  useEffect(() => {
    const getProviderId = async () => {
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
      getProviderId();
    }
  }, [userId, token]);
  
  // Step 2: Fetch all service categories for dropdowns
  useEffect(() => {
    const fetchCategories = async () => {
      try {
        const response = await axios.get('/api/service-categories/getAll', {
          headers: { 'Authorization': `Bearer ${token}` }
        });
        setCategories(response.data);
      } catch (err) {
        console.error('Error fetching categories:', err);
        setError('Failed to load service categories. Please try again later.');
      }
    };
    
    fetchCategories();
  }, [token]);
  
  // Step 3: Once we have provider ID, fetch their services
  useEffect(() => {
    const fetchServices = async () => {
      if (!providerId) return;
      
      try {
        setLoading(true);
        setError(null);
        
        const response = await axios.get('/api/services/getAll', {
          headers: { 'Authorization': `Bearer ${token}` }
        });
        
        // Filter services by providerId
        const providerServices = response.data.filter(
          service => service.provider && service.provider.providerId === providerId
        );
        
        setServices(providerServices);
        
        // Group services by category
        const grouped = {};
        providerServices.forEach(service => {
          const categoryId = service.category.categoryId;
          const categoryName = service.category.categoryName;
          
          if (!grouped[categoryId]) {
            grouped[categoryId] = {
              categoryName: categoryName,
              services: []
            };
          }
          
          grouped[categoryId].services.push(service);
        });
        
        setServicesByCategory(grouped);
        setLoading(false);
      } catch (err) {
        console.error('Error fetching services:', err);
        setError('Failed to load services. Please try again later.');
        setLoading(false);
      }
    };
    
    fetchServices();
  }, [providerId, token]);
  
  // Handle opening the add dialog
  const handleAddClick = () => {
    setCurrentService({
      serviceId: null,
      serviceName: '',
      serviceDescription: '',
      priceRange: '',
      durationEstimate: '',
      categoryId: ''
    });
    setOpenAddDialog(true);
  };
  
  // Handle opening the edit dialog
  const handleEditClick = (service) => {
    setCurrentService({
      serviceId: service.serviceId,
      serviceName: service.serviceName,
      serviceDescription: service.serviceDescription,
      priceRange: service.priceRange,
      durationEstimate: service.durationEstimate,
      categoryId: service.category.categoryId
    });
    setOpenEditDialog(true);
  };
  
  // Handle opening the delete dialog
  const handleDeleteClick = (service) => {
    setCurrentService(service);
    setOpenDeleteDialog(true);
  };
  
  // Handle input changes
  const handleInputChange = (e) => {
    const { name, value } = e.target;
    setCurrentService(prev => ({
      ...prev,
      [name]: value
    }));
  };
  
  // Add a new service
  const handleAddService = async () => {
    try {
      setLoading(true);
      setError(null);
      
      // Create service payload
      const servicePayload = {
        serviceName: currentService.serviceName,
        serviceDescription: currentService.serviceDescription,
        priceRange: currentService.priceRange,
        durationEstimate: currentService.durationEstimate,
        // Use providerId and categoryId to establish relationships
        provider: { providerId: providerId },
        category: { categoryId: currentService.categoryId }
      };
      
      const response = await axios.post('/api/services/postService', servicePayload, {
        headers: { 'Authorization': `Bearer ${token}` }
      });
      
      // Add new service to the list
      setServices(prev => [...prev, response.data]);
      
      // Update services by category
      setServicesByCategory(prev => {
        const categoryId = response.data.category.categoryId;
        const categoryName = response.data.category.categoryName;
        
        const newServicesByCategory = {...prev};
        
        if (!newServicesByCategory[categoryId]) {
          newServicesByCategory[categoryId] = {
            categoryName: categoryName,
            services: []
          };
        }
        
        newServicesByCategory[categoryId].services.push(response.data);
        
        return newServicesByCategory;
      });
      
      setSuccess('Service added successfully!');
      setOpenAddDialog(false);
      setLoading(false);
      
      // Auto-hide success message
      setTimeout(() => setSuccess(null), 3000);
    } catch (err) {
      console.error('Error adding service:', err);
      setError('Failed to add service. Please try again.');
      setLoading(false);
    }
  };
  
  // Update an existing service
  const handleUpdateService = async () => {
    try {
      setLoading(true);
      setError(null);
      
      // Create service payload with just the service details
      const servicePayload = {
        serviceName: currentService.serviceName,
        serviceDescription: currentService.serviceDescription,
        priceRange: currentService.priceRange,
        durationEstimate: currentService.durationEstimate
        // Don't include provider and category here since they're in the URL path
      };
      
      // Use the correct URL format with path variables
      const response = await axios.put(
        `/api/services/updateService/${currentService.serviceId}/${providerId}/${currentService.categoryId}`, 
        servicePayload,
        {
          headers: { 'Authorization': `Bearer ${token}` }
        }
      );
      
      // Update the service in our list
      setServices(prev => 
        prev.map(service => 
          service.serviceId === currentService.serviceId ? response.data : service
        )
      );
      
      // Update services by category
      setServicesByCategory(prev => {
        const updatedServicesByCategory = {...prev};
        
        // Remove the service from its previous category
        Object.keys(updatedServicesByCategory).forEach(categoryId => {
          updatedServicesByCategory[categoryId].services = updatedServicesByCategory[categoryId].services.filter(
            service => service.serviceId !== currentService.serviceId
          );
        });
        
        // Add the updated service to its new category
        const categoryId = response.data.category.categoryId;
        const categoryName = response.data.category.categoryName;
        
        if (!updatedServicesByCategory[categoryId]) {
          updatedServicesByCategory[categoryId] = {
            categoryName: categoryName,
            services: []
          };
        }
        
        updatedServicesByCategory[categoryId].services.push(response.data);
        
        // Remove any empty categories
        Object.keys(updatedServicesByCategory).forEach(categoryId => {
          if (updatedServicesByCategory[categoryId].services.length === 0) {
            delete updatedServicesByCategory[categoryId];
          }
        });
        
        return updatedServicesByCategory;
      });
      
      setSuccess('Service updated successfully!');
      setOpenEditDialog(false);
      setLoading(false);
      
      // Auto-hide success message
      setTimeout(() => setSuccess(null), 3000);
    } catch (err) {
      console.error('Error updating service:', err);
      setError('Failed to update service. Please try again.');
      setLoading(false);
    }
  };
  
  // Delete a service
  const handleDeleteService = async () => {
    try {
      setLoading(true);
      setError(null);
      
      await axios.delete(`/api/services/delete/${currentService.serviceId}`, {
        headers: { 'Authorization': `Bearer ${token}` }
      });
      
      // Remove the service from our list
      setServices(prev => 
        prev.filter(service => service.serviceId !== currentService.serviceId)
      );
      
      // Update services by category
      setServicesByCategory(prev => {
        const updatedServicesByCategory = {...prev};
        
        // Remove the service from its category
        Object.keys(updatedServicesByCategory).forEach(categoryId => {
          updatedServicesByCategory[categoryId].services = updatedServicesByCategory[categoryId].services.filter(
            service => service.serviceId !== currentService.serviceId
          );
          
          // Remove any empty categories
          if (updatedServicesByCategory[categoryId].services.length === 0) {
            delete updatedServicesByCategory[categoryId];
          }
        });
        
        return updatedServicesByCategory;
      });
      
      setSuccess('Service deleted successfully!');
      setOpenDeleteDialog(false);
      setLoading(false);
      
      // Auto-hide success message
      setTimeout(() => setSuccess(null), 3000);
    } catch (err) {
      console.error('Error deleting service:', err);
      setError('Failed to delete service. Please try again.');
      setLoading(false);
    }
  };

  return (
    <>
      <Box>
        <Typography variant="h4" gutterBottom>My Services</Typography>
        <Typography variant="body1" color="text.secondary" sx={{ mb: 4 }}>
          Manage the services you offer
        </Typography>
      </Box>

      {error && (
        <Alert severity="error" sx={{ mb: 2 }}>{error}</Alert>
      )}

      {success && (
        <Alert severity="success" sx={{ mb: 2 }}>{success}</Alert>
      )}

      {/* Add Service Button */}
      <Box sx={{ display: 'flex', justifyContent: 'flex-end', mb: 2 }}>
        <Button 
          variant="contained" 
          color="primary" 
          startIcon={<AddIcon />}
          onClick={handleAddClick}
        >
          Add New Service
        </Button>
      </Box>

      {/* Services List by Category */}
      {loading && !Object.keys(servicesByCategory).length ? (
        <Box sx={{ display: 'flex', justifyContent: 'center', p: 4 }}>
          <CircularProgress />
        </Box>
      ) : !Object.keys(servicesByCategory).length ? (
        <Typography variant="body1" sx={{ textAlign: 'center', py: 4 }}>
          No services added yet. Click "Add New Service" to get started.
        </Typography>
      ) : (
        <Box sx={{ mt: 2 }}>
          {Object.entries(servicesByCategory).map(([categoryId, category]) => (
            <Accordion key={categoryId} defaultExpanded sx={{ mb: 2 }}>
              <AccordionSummary
                expandIcon={<ExpandMoreIcon />}
                aria-controls={`category-${categoryId}-content`}
                id={`category-${categoryId}-header`}
              >
                <Typography variant="h6">{category.categoryName}</Typography>
                <Chip 
                  label={`${category.services.length} service${category.services.length !== 1 ? 's' : ''}`} 
                  size="small" 
                  sx={{ ml: 2 }}
                />
              </AccordionSummary>
              <AccordionDetails>
                <Grid container spacing={2}>
                  {category.services.map((service) => (
                    <Grid item xs={12} md={6} lg={4} key={service.serviceId}>
                      <Card variant="outlined">
                        <CardContent>
                          <Typography variant="h6" component="div">
                            {service.serviceName}
                          </Typography>
                          <Typography sx={{ mb: 1.5 }} color="text.secondary">
                            Price: {service.priceRange}
                          </Typography>
                          <Typography sx={{ mb: 1.5 }} color="text.secondary">
                            Duration: {service.durationEstimate}
                          </Typography>
                          <Typography variant="body2">
                            {service.serviceDescription}
                          </Typography>
                        </CardContent>
                        <CardActions>
                          <IconButton
                            size="small"
                            color="primary"
                            onClick={() => handleEditClick(service)}
                          >
                            <EditIcon />
                          </IconButton>
                          <IconButton
                            size="small"
                            color="error"
                            onClick={() => handleDeleteClick(service)}
                          >
                            <DeleteIcon />
                          </IconButton>
                        </CardActions>
                      </Card>
                    </Grid>
                  ))}
                </Grid>
              </AccordionDetails>
            </Accordion>
          ))}
        </Box>
      )}

      {/* Add Service Dialog */}
      <Dialog open={openAddDialog} onClose={() => setOpenAddDialog(false)} maxWidth="sm" fullWidth>
        <DialogTitle>Add New Service</DialogTitle>
        <DialogContent>
          <Box component="form" sx={{ mt: 2 }}>
            <Grid container spacing={2}>
              <Grid item xs={12}>
                <FormControl fullWidth required>
                  <InputLabel id="category-label">Service Category</InputLabel>
                  <Select
                    labelId="category-label"
                    id="categoryId"
                    name="categoryId"
                    value={currentService.categoryId}
                    onChange={handleInputChange}
                    label="Service Category"
                  >
                    {categories.map((category) => (
                      <MenuItem key={category.categoryId} value={category.categoryId}>
                        {category.categoryName}
                      </MenuItem>
                    ))}
                  </Select>
                </FormControl>
              </Grid>
              <Grid item xs={12}>
                <TextField
                  required
                  fullWidth
                  id="serviceName"
                  name="serviceName"
                  label="Service Name"
                  value={currentService.serviceName}
                  onChange={handleInputChange}
                />
              </Grid>
              <Grid item xs={12}>
                <TextField
                  required
                  fullWidth
                  id="serviceDescription"
                  name="serviceDescription"
                  label="Service Description"
                  value={currentService.serviceDescription}
                  onChange={handleInputChange}
                  multiline
                  rows={3}
                />
              </Grid>
              <Grid item xs={12} sm={6}>
                <TextField
                  required
                  fullWidth
                  id="priceRange"
                  name="priceRange"
                  label="Price Range"
                  value={currentService.priceRange}
                  onChange={handleInputChange}
                  placeholder="e.g., ₱500 - ₱1000"
                />
              </Grid>
              <Grid item xs={12} sm={6}>
                <TextField
                  required
                  fullWidth
                  id="durationEstimate"
                  name="durationEstimate"
                  label="Duration Estimate"
                  value={currentService.durationEstimate}
                  onChange={handleInputChange}
                  placeholder="e.g., 1-2 hours"
                />
              </Grid>
            </Grid>
          </Box>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setOpenAddDialog(false)}>Cancel</Button>
          <Button 
            onClick={handleAddService} 
            variant="contained" 
            color="primary"
            disabled={loading || !currentService.categoryId || !currentService.serviceName}
          >
            {loading ? <CircularProgress size={24} /> : 'Add Service'}
          </Button>
        </DialogActions>
      </Dialog>

      {/* Edit Service Dialog */}
      <Dialog open={openEditDialog} onClose={() => setOpenEditDialog(false)} maxWidth="sm" fullWidth>
        <DialogTitle>Edit Service</DialogTitle>
        <DialogContent>
          <Box component="form" sx={{ mt: 2 }}>
            <Grid container spacing={2}>
              <Grid item xs={12}>
                <FormControl fullWidth required>
                  <InputLabel id="category-edit-label">Service Category</InputLabel>
                  <Select
                    labelId="category-edit-label"
                    id="categoryId-edit"
                    name="categoryId"
                    value={currentService.categoryId}
                    onChange={handleInputChange}
                    label="Service Category"
                  >
                    {categories.map((category) => (
                      <MenuItem key={category.categoryId} value={category.categoryId}>
                        {category.categoryName}
                      </MenuItem>
                    ))}
                  </Select>
                </FormControl>
              </Grid>
              <Grid item xs={12}>
                <TextField
                  required
                  fullWidth
                  id="serviceName-edit"
                  name="serviceName"
                  label="Service Name"
                  value={currentService.serviceName}
                  onChange={handleInputChange}
                />
              </Grid>
              <Grid item xs={12}>
                <TextField
                  required
                  fullWidth
                  id="serviceDescription-edit"
                  name="serviceDescription"
                  label="Service Description"
                  value={currentService.serviceDescription}
                  onChange={handleInputChange}
                  multiline
                  rows={3}
                />
              </Grid>
              <Grid item xs={12} sm={6}>
                <TextField
                  required
                  fullWidth
                  id="priceRange-edit"
                  name="priceRange"
                  label="Price Range"
                  value={currentService.priceRange}
                  onChange={handleInputChange}
                />
              </Grid>
              <Grid item xs={12} sm={6}>
                <TextField
                  required
                  fullWidth
                  id="durationEstimate-edit"
                  name="durationEstimate"
                  label="Duration Estimate"
                  value={currentService.durationEstimate}
                  onChange={handleInputChange}
                />
              </Grid>
            </Grid>
          </Box>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setOpenEditDialog(false)}>Cancel</Button>
          <Button 
            onClick={handleUpdateService} 
            variant="contained" 
            color="primary"
            disabled={loading || !currentService.categoryId || !currentService.serviceName}
          >
            {loading ? <CircularProgress size={24} /> : 'Update Service'}
          </Button>
        </DialogActions>
      </Dialog>

      {/* Delete Confirmation Dialog */}
      <Dialog open={openDeleteDialog} onClose={() => setOpenDeleteDialog(false)}>
        <DialogTitle>Confirm Deletion</DialogTitle>
        <DialogContent>
          <DialogContentText>
            Are you sure you want to delete the service "{currentService.serviceName}"? This action cannot be undone.
          </DialogContentText>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setOpenDeleteDialog(false)}>Cancel</Button>
          <Button 
            onClick={handleDeleteService} 
            color="error" 
            variant="contained"
            disabled={loading}
          >
            {loading ? <CircularProgress size={24} /> : 'Delete'}
          </Button>
        </DialogActions>
      </Dialog>
    </>
  );
}

export default MyServicesContent;