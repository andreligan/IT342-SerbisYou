import React, { useState, useEffect } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import axios from 'axios';
import { motion } from 'framer-motion';
import BaseModal from '../../shared/BaseModal';

// Base URL for the backend server
const BASE_URL = "http://localhost:8080";

function MyServicesContent() {
  // Add navigate for routing
  const navigate = useNavigate();
  
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
  const [openEditDialog, setOpenEditDialog] = useState(false);
  const [openDeleteDialog, setOpenDeleteDialog] = useState(false);

  // State for active tab
  const [activeTab, setActiveTab] = useState(null);
  
  // State for current service - only for edit/delete now
  const [currentService, setCurrentService] = useState({
    serviceId: null,
    serviceName: '',
    serviceDescription: '',
    price: '',
    durationEstimate: '',
    categoryId: ''
  });
  
  // State for service images
  const [serviceImages, setServiceImages] = useState({});

  // State for expanded image modal
  const [expandedImage, setExpandedImage] = useState(null);
  const [isImageModalOpen, setIsImageModalOpen] = useState(false);

  // Animation variants
  const containerVariants = {
    hidden: { opacity: 0 },
    visible: {
      opacity: 1,
      transition: {
        when: "beforeChildren",
        staggerChildren: 0.1
      }
    }
  };

  const itemVariants = {
    hidden: { y: 20, opacity: 0 },
    visible: {
      y: 0,
      opacity: 1,
      transition: { type: "spring", stiffness: 100 }
    }
  };

  const cardVariants = {
    hidden: { scale: 0.9, opacity: 0 },
    visible: { 
      scale: 1, 
      opacity: 1,
      transition: { type: "spring", damping: 12 }
    },
    hover: { 
      y: -5,
      boxShadow: "0 10px 25px -5px rgba(0, 0, 0, 0.1), 0 8px 10px -6px rgba(0, 0, 0, 0.1)",
      transition: { duration: 0.2 }
    },
    tap: { scale: 0.98 }
  };

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
        
        // Set the first category as active if there are any
        const categoryIds = Object.keys(grouped);
        if (categoryIds.length > 0 && !activeTab) {
          setActiveTab(categoryIds[0]);
        }
        
        setLoading(false);
      } catch (err) {
        console.error('Error fetching services:', err);
        setError('Failed to load services. Please try again later.');
        setLoading(false);
      }
    };
    
    fetchServices();
  }, [providerId, token]);

  // Fetch service images after services are loaded
  useEffect(() => {
    const fetchServiceImages = async () => {
      try {
        const images = {};
        for (const service of services) {
          try {
            const response = await axios.get(`/api/services/getServiceImage/${service.serviceId}`, {
              headers: { Authorization: `Bearer ${token}` },
            });
            // Prepend the base URL to the relative path
            images[service.serviceId] = `${BASE_URL}${response.data}`;
          } catch (err) {
            console.error(`Error fetching image for service ${service.serviceId}:`, err);
            images[service.serviceId] = null; // Handle missing images gracefully
          }
        }
        setServiceImages(images);
      } catch (err) {
        console.error('Error fetching service images:', err);
      }
    };

    if (services.length > 0) {
      fetchServiceImages();
    }
  }, [services, token]);
  
  // Navigate to add service page instead of opening dialog
  const handleAddClick = () => {
    navigate('/addService');
  };
  
  // Handle opening the edit dialog
  const handleEditClick = (service) => {
    setCurrentService({
      serviceId: service.serviceId,
      serviceName: service.serviceName,
      serviceDescription: service.serviceDescription,
      price: service.price,
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
  
  // Update an existing service
  const handleUpdateService = async () => {
    try {
      setLoading(true);
      setError(null);
      
      // Create service payload with just the service details
      const servicePayload = {
        serviceName: currentService.serviceName,
        serviceDescription: currentService.serviceDescription,
        price: currentService.price,
        durationEstimate: currentService.durationEstimate
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
        
        // If the active tab was deleted, set a new active tab
        if (Object.keys(updatedServicesByCategory).length > 0 && 
            !updatedServicesByCategory[activeTab]) {
          setActiveTab(Object.keys(updatedServicesByCategory)[0]);
        }
        
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

  // Handle image upload
  const handleImageUpload = async (serviceId, file) => {
    try {
      const formData = new FormData();
      formData.append('image', file);

      const response = await axios.post(`/api/services/uploadServiceImage/${serviceId}`, formData, {
        headers: {
          Authorization: `Bearer ${token}`,
          'Content-Type': 'multipart/form-data',
        },
      });

      console.log(response.data);

      // Update the service image state with the new image path
      setServiceImages((prev) => ({
        ...prev,
        [serviceId]: `${BASE_URL}/uploads/${serviceId}_${file.name}`,
      }));
    } catch (err) {
      console.error('Error uploading service image:', err);
    }
  };

  // Handle image click to expand
  const handleImageClick = (imageUrl) => {
    if (imageUrl) {
      setExpandedImage(imageUrl);
      setIsImageModalOpen(true);
    }
  };

  return (
    <motion.div 
      className="max-w-7xl px-4 sm:px-6 lg:px-8 py-8"
      initial={{ opacity: 0 }}
      animate={{ opacity: 1 }}
      exit={{ opacity: 0 }}
      transition={{ duration: 0.5 }}
    >
      <motion.div 
        className="mb-8"
        initial={{ opacity: 0, y: -20 }}
        animate={{ opacity: 1, y: 0 }}
        transition={{ delay: 0.2, duration: 0.5 }}
      >
        <h1 className="text-3xl font-bold text-gray-800">My Services</h1>
        <p className="text-gray-600 mt-2">
          Manage the services you offer to your clients
        </p>
      </motion.div>

      {error && (
        <motion.div 
          className="mb-6 p-4 bg-red-50 border-l-4 border-red-500 rounded-md text-red-700 shadow"
          initial={{ opacity: 0, x: -20 }}
          animate={{ opacity: 1, x: 0 }}
          transition={{ duration: 0.3 }}
        >
          <div className="flex">
            <div className="flex-shrink-0">
              <svg className="h-5 w-5 text-red-400" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 20 20" fill="currentColor">
                <path fillRule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zM8.707 7.293a1 1 0 00-1.414 1.414L8.586 10l-1.293 1.293a1 1 0 101.414 1.414L10 11.414l1.293 1.293a1 1 0 001.414-1.414L11.414 10l1.293-1.293a1 1 0 00-1.414-1.414L10 8.586 8.707 7.293z" clipRule="evenodd" />
              </svg>
            </div>
            <div className="ml-3">
              <p className="text-sm font-medium">{error}</p>
            </div>
          </div>
        </motion.div>
      )}

      {success && (
        <motion.div 
          className="mb-6 p-4 bg-green-50 border-l-4 border-green-500 rounded-md text-green-700 shadow"
          initial={{ opacity: 0, scale: 0.9 }}
          animate={{ opacity: 1, scale: 1 }}
          transition={{ duration: 0.3 }}
        >
          <div className="flex">
            <div className="flex-shrink-0">
              <svg className="h-5 w-5 text-green-400" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 20 20" fill="currentColor">
                <path fillRule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zm3.707-9.293a1 1 0 00-1.414-1.414L9 10.586 7.707 9.293a1 1 0 00-1.414 1.414l2 2a1 1 0 001.414 0l4-4z" clipRule="evenodd" />
              </svg>
            </div>
            <div className="ml-3">
              <p className="text-sm font-medium">{success}</p>
            </div>
          </div>
        </motion.div>
      )}

      {/* Add Service Button */}
      <motion.div 
        className="flex justify-end mb-6"
        initial={{ opacity: 0, y: 10 }}
        animate={{ opacity: 1, y: 0 }}
        transition={{ delay: 0.3, duration: 0.4 }}
      >
        <motion.button 
          className="flex items-center px-4 py-2 bg-[#F4CE14] text-[#495E57] font-medium rounded-md hover:bg-yellow-300 shadow-md transition-colors"
          onClick={handleAddClick}
          whileHover={{ scale: 1.05, boxShadow: "0 10px 15px -3px rgba(0, 0, 0, 0.1), 0 4px 6px -2px rgba(0, 0, 0, 0.05)" }}
          whileTap={{ scale: 0.95 }}
        >
          <svg className="w-5 h-5 mr-2" fill="none" stroke="currentColor" viewBox="0 0 24 24" xmlns="http://www.w3.org/2000/svg">
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M12 6v6m0 0v6m0-6h6m-6 0H6"></path>
          </svg>
          Add New Service
        </motion.button>
      </motion.div>

      {/* Loading State */}
      {loading && !Object.keys(servicesByCategory).length ? (
        <motion.div 
          className="flex justify-center items-center py-16 bg-gray-50 rounded-lg shadow"
          initial={{ opacity: 0 }}
          animate={{ opacity: 1 }}
          transition={{ duration: 0.5 }}
        >
          <div className="animate-spin rounded-full h-12 w-12 border-t-2 border-b-2 border-[#495E57]"></div>
        </motion.div>
      ) : !Object.keys(servicesByCategory).length ? (
        <motion.div 
          className="flex flex-col items-center justify-center py-16 px-4 bg-gray-50 rounded-lg shadow"
          variants={containerVariants}
          initial="hidden"
          animate="visible"
        >
          <motion.div variants={itemVariants}>
            <svg className="w-16 h-16 mb-4 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24" xmlns="http://www.w3.org/2000/svg">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z"></path>
            </svg>
          </motion.div>
          <motion.p className="text-gray-600 text-lg font-medium mb-2" variants={itemVariants}>
            No services added yet
          </motion.p>
          <motion.p className="text-gray-500 text-center mb-6" variants={itemVariants}>
            Start by adding your first service to showcase to clients
          </motion.p>
          <motion.button 
            className="flex items-center px-4 py-2 bg-[#F4CE14] text-[#495E57] font-medium rounded-md hover:bg-yellow-300 shadow transition-colors"
            onClick={handleAddClick}
            variants={itemVariants}
            whileHover={{ scale: 1.05, boxShadow: "0 10px 15px -3px rgba(0, 0, 0, 0.1), 0 4px 6px -2px rgba(0, 0, 0, 0.05)" }}
            whileTap={{ scale: 0.95 }}
          >
            <svg className="w-5 h-5 mr-2" fill="none" stroke="currentColor" viewBox="0 0 24 24" xmlns="http://www.w3.org/2000/svg">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M12 6v6m0 0v6m0-6h6m-6 0H6"></path>
            </svg>
            Add New Service
          </motion.button>
        </motion.div>
      ) : (
        <motion.div 
          className="bg-white rounded-lg shadow-lg overflow-hidden"
          initial={{ opacity: 0, y: 20 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ duration: 0.5 }}
        >
          {/* Horizontal Tabs */}
          <div className="border-b border-gray-200 overflow-hidden">
            <motion.nav 
              className="flex overflow-x-auto"
              initial={{ opacity: 0 }}
              animate={{ opacity: 1 }}
              transition={{ duration: 0.5, delay: 0.2 }}
            >
              {Object.entries(servicesByCategory).map(([categoryId, category], index) => (
                <motion.button
                  key={categoryId}
                  onClick={() => setActiveTab(categoryId)}
                  className={`
                    whitespace-nowrap py-4 px-6 border-b-2 font-medium text-sm flex items-center
                    ${activeTab === categoryId 
                      ? 'border-[#F4CE14] text-[#495E57]' 
                      : 'border-transparent text-gray-500 hover:text-gray-700 hover:border-gray-300'}
                  `}
                  initial={{ opacity: 0, y: -10 }}
                  animate={{ opacity: 1, y: 0 }}
                  transition={{ delay: 0.1 * index, duration: 0.4 }}
                  whileHover={{ y: -2 }}
                  whileTap={{ scale: 0.95 }}
                >
                  {category.categoryName}
                  <span className={`ml-2 py-0.5 px-2 rounded-full text-xs font-medium 
                    ${activeTab === categoryId ? 'bg-[#F4CE14] text-[#495E57]' : 'bg-gray-100 text-gray-600'}`}>
                    {category.services.length}
                  </span>
                </motion.button>
              ))}
            </motion.nav>
          </div>

          {/* Services Container */}
          <div className="p-6 bg-gray-50 min-h-[400px]">
            {activeTab && servicesByCategory[activeTab] && (
              <motion.div 
                className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6"
                variants={containerVariants}
                initial="hidden"
                animate="visible"
              >
                {servicesByCategory[activeTab].services.map((service, index) => (
                  <motion.div 
                    key={service.serviceId} 
                    className="bg-white rounded-lg overflow-hidden shadow border border-gray-100 flex flex-col h-full"
                    variants={cardVariants}
                    whileHover="hover"
                    whileTap="tap"
                    custom={index}
                    transition={{ delay: index * 0.05 }}
                  >
                    {/* Service Image */}
                    <div className="relative">
                      <div className="w-full h-48 bg-gray-100 flex items-center justify-center overflow-hidden">
                        {serviceImages[service.serviceId] ? (
                          <motion.img
                            src={serviceImages[service.serviceId]}
                            alt={service.serviceName}
                            className="w-full h-full object-cover cursor-pointer"
                            onClick={(e) => {
                              e.preventDefault(); // Prevent navigation when clicking the image
                              e.stopPropagation();
                              handleImageClick(serviceImages[service.serviceId]);
                            }}
                            initial={{ scale: 1 }}
                            whileHover={{ scale: 1.05 }}
                            transition={{ duration: 0.3 }}
                          />
                        ) : (
                          <div className="flex flex-col items-center justify-center w-full h-full bg-gray-100">
                            <svg className="w-12 h-12 text-gray-300" fill="none" stroke="currentColor" viewBox="0 0 24 24" xmlns="http://www.w3.org/2000/svg">
                              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="1.5" d="M4 16l4.586-4.586a2 2 0 012.828 0L16 16m-2-2l1.586-1.586a2 2 0 012.828 0L20 14m-6-6h.01M6 20h12a2 2 0 002-2V6a2 2 0 00-2-2H6a2 2 0 00-2 2z"></path>
                            </svg>
                            <span className="text-gray-400 text-sm mt-2">No Image</span>
                          </div>
                        )}
                      </div>
                      <motion.label
                        htmlFor={`upload-image-${service.serviceId}`}
                        className="absolute bottom-2 right-2 bg-[#F4CE14] text-[#495E57] px-3 py-1 rounded-md text-sm cursor-pointer hover:bg-yellow-300"
                        whileHover={{ scale: 1.05 }}
                        whileTap={{ scale: 0.95 }}
                      >
                        {serviceImages[service.serviceId] ? 'Change Image' : 'Add Image'}
                        <input
                          id={`upload-image-${service.serviceId}`}
                          type="file"
                          accept="image/*"
                          className="hidden"
                          onChange={(e) => handleImageUpload(service.serviceId, e.target.files[0])}
                        />
                      </motion.label>
                    </div>

                    {/* Clickable area for service details - make this flex-grow to push buttons to bottom */}
                    <Link
                      to={`/service/${service.serviceId}`}
                      className="block flex-grow"
                    >
                      <div className="p-5">
                        <h3 className="text-lg font-semibold text-[#495E57] mb-2">{service.serviceName}</h3>
                        <div className="flex items-center text-sm text-gray-600 mb-1">
                          <svg className="w-4 h-4 mr-1 text-[#F4CE14]" fill="currentColor" viewBox="0 0 20 20" xmlns="http://www.w3.org/2000/svg">
                            <path fillRule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zm1-14a1 1 0 10-2 0v4a1 1 0 00.293.707l2.828 2.829a1 1 0 101.415-1.415L11 9.586V4z" clipRule="evenodd"></path>
                          </svg>
                          <span className="font-medium mr-1">Duration:</span> {service.durationEstimate}
                        </div>
                        <div className="flex items-center text-sm text-gray-600 mb-3">
                          <svg className="w-4 h-4 mr-1 text-[#F4CE14]" fill="currentColor" viewBox="0 0 20 20" xmlns="http://www.w3.org/2000/svg">
                            <path fillRule="evenodd" d="M4 4a2 2 0 00-2 2v4a2 2 0 002 2V6h10a2 2 0 00-2-2H4zm2 6a2 2 0 012-2h8a2 2 0 012 2v4a2 2 0 01-2 2H8a2 2 0 01-2-2v-4zm6 4a2 2 0 100-4 2 2 0 000 4z" clipRule="evenodd"></path>
                          </svg>
                          <span className="font-medium mr-1">Price:</span> ₱{service.price}
                        </div>
                        <p className="text-gray-700 text-sm line-clamp-3">{service.serviceDescription}</p>
                      </div>
                    </Link>
                    
                    {/* Buttons at bottom */}
                    <div className="flex justify-end p-3 bg-red-50 border-t border-gray-100 mt-auto">
                      <motion.button
                        className="p-2 mr-2 text-blue-600 hover:bg-blue-50 rounded-full transition-colors flex items-center"
                        onClick={(e) => {
                          e.stopPropagation();
                          handleEditClick(service);
                        }}
                        whileHover={{ scale: 1.1, backgroundColor: "rgba(59, 130, 246, 0.1)" }}
                        whileTap={{ scale: 0.9 }}
                      >
                        <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24" xmlns="http://www.w3.org/2000/svg">
                          <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M11 5H6a2 2 0 00-2 2v11a2 2 0 002 2h11a2 2 0 002-2v-5m-1.414-9.414a2 2 0 112.828 2.828L11.828 15H9v-2.828l8.586-8.586z"></path>
                        </svg>
                      </motion.button>
                      <motion.button
                        className="p-2 text-red-600 hover:bg-red-50 rounded-full transition-colors flex items-center"
                        onClick={(e) => {
                          e.stopPropagation();
                          handleDeleteClick(service);
                        }}
                        whileHover={{ scale: 1.1, backgroundColor: "rgba(239, 68, 68, 0.1)" }}
                        whileTap={{ scale: 0.9 }}
                      >
                        <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24" xmlns="http://www.w3.org/2000/svg">
                          <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16"></path>
                        </svg>
                      </motion.button>
                    </div>
                  </motion.div>
                ))}
              </motion.div>
            )}
          </div>
        </motion.div>
      )}

      {/* Edit Service Dialog */}
      <BaseModal 
        isOpen={openEditDialog}
        onClose={() => setOpenEditDialog(false)}
        maxWidth="max-w-lg"
      >
        <div className="bg-white rounded-xl text-left overflow-hidden shadow-xl border border-gray-100">
          <motion.div 
            className="bg-gradient-to-r from-[#495E57] to-[#3e4f49] px-6 py-4 flex justify-between items-center"
            initial={{ opacity: 0, x: -20 }}
            animate={{ opacity: 1, x: 0 }}
            transition={{ delay: 0.1, duration: 0.3 }}
          >
            <h3 className="text-lg font-medium text-white flex items-center" id="modal-title">
              <svg xmlns="http://www.w3.org/2000/svg" className="h-5 w-5 mr-2" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M11 5H6a2 2 0 00-2 2v11a2 2 0 002 2h11a2 2 0 002-2v-5m-1.414-9.414a2 2 0 112.828 2.828L11.828 15H9v-2.828l8.586-8.586z" />
              </svg>
              Edit Service
            </h3>
            <motion.button
              onClick={() => setOpenEditDialog(false)}
              className="text-white hover:text-gray-200 focus:outline-none"
              whileHover={{ scale: 1.1 }}
              whileTap={{ scale: 0.9 }}
            >
              <svg className="h-6 w-6" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M6 18L18 6M6 6l12 12" />
              </svg>
            </motion.button>
          </motion.div>

          <div className="bg-white px-6 pt-5 pb-6">
            <div className="space-y-5">
              {/* Service Category */}
              <motion.div
                initial={{ opacity: 0, y: 10 }}
                animate={{ opacity: 1, y: 0 }}
                transition={{ delay: 0.2, duration: 0.3 }}
              >
                <label htmlFor="categoryId" className="block text-sm font-medium text-gray-700 mb-1">Service Category</label>
                <select
                  id="categoryId"
                  name="categoryId"
                  value={currentService.categoryId}
                  onChange={handleInputChange}
                  className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-[#F4CE14] focus:border-[#F4CE14] shadow-sm transition-all"
                  required
                >
                  <option value="">Select a category</option>
                  {categories.map((category) => (
                    <option key={category.categoryId} value={category.categoryId}>
                      {category.categoryName}
                    </option>
                  ))}
                </select>
              </motion.div>
              
              {/* Service Name */}
              <motion.div
                initial={{ opacity: 0, y: 10 }}
                animate={{ opacity: 1, y: 0 }}
                transition={{ delay: 0.3, duration: 0.3 }}
              >
                <label htmlFor="serviceName" className="block text-sm font-medium text-gray-700 mb-1">Service Name</label>
                <input
                  type="text"
                  id="serviceName"
                  name="serviceName"
                  value={currentService.serviceName}
                  onChange={handleInputChange}
                  className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-[#F4CE14] focus:border-[#F4CE14] shadow-sm transition-all"
                  required
                />
              </motion.div>
              
              {/* Service Description */}
              <motion.div
                initial={{ opacity: 0, y: 10 }}
                animate={{ opacity: 1, y: 0 }}
                transition={{ delay: 0.4, duration: 0.3 }}
              >
                <label htmlFor="serviceDescription" className="block text-sm font-medium text-gray-700 mb-1">Service Description</label>
                <textarea
                  id="serviceDescription"
                  name="serviceDescription"
                  rows="4"
                  value={currentService.serviceDescription}
                  onChange={handleInputChange}
                  className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-[#F4CE14] focus:border-[#F4CE14] shadow-sm transition-all resize-none"
                  required
                ></textarea>
              </motion.div>
              
              {/* Price and Duration */}
              <motion.div 
                className="grid grid-cols-1 sm:grid-cols-2 gap-4"
                initial={{ opacity: 0, y: 10 }}
                animate={{ opacity: 1, y: 0 }}
                transition={{ delay: 0.5, duration: 0.3 }}
              >
                <div>
                  <label htmlFor="price" className="block text-sm font-medium text-gray-700 mb-1">Price (₱)</label>
                  <input
                    type="text"
                    id="price"
                    name="price"
                    value={currentService.price}
                    onChange={handleInputChange}
                    className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-[#F4CE14] focus:border-[#F4CE14] shadow-sm transition-all"
                    required
                  />
                </div>
                
                <div>
                  <label htmlFor="durationEstimate" className="block text-sm font-medium text-gray-700 mb-1">Duration Estimate</label>
                  <input
                    type="text"
                    id="durationEstimate"
                    name="durationEstimate"
                    value={currentService.durationEstimate}
                    onChange={handleInputChange}
                    className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-[#F4CE14] focus:border-[#F4CE14] shadow-sm transition-all"
                    required
                  />
                </div>
              </motion.div>
            </div>
          </div>

          {/* Action buttons */}
          <motion.div 
            className="bg-gray-50 px-6 py-4 sm:flex sm:flex-row-reverse gap-3"
            initial={{ opacity: 0, y: 10 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ delay: 0.6, duration: 0.3 }}
          >
            <motion.button
              type="button"
              className="w-full sm:w-auto flex justify-center items-center bg-[#F4CE14] text-[#495E57] px-6 py-3 font-medium rounded-lg hover:bg-yellow-400 focus:outline-none focus:ring-2 focus:ring-yellow-400 transition-colors"
              onClick={handleUpdateService}
              disabled={loading || !currentService.categoryId || !currentService.serviceName}
              whileHover={{ scale: 1.03 }}
              whileTap={{ scale: 0.97 }}
            >
              {loading ? (
                <>
                  <svg className="animate-spin -ml-1 mr-2 h-5 w-5 text-[#495E57]" fill="none" viewBox="0 0 24 24">
                    <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4"></circle>
                    <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
                  </svg>
                  Updating...
                </>
              ) : (
                <>
                  <svg xmlns="http://www.w3.org/2000/svg" className="h-5 w-5 mr-2" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M5 13l4 4L19 7" />
                  </svg>
                  Update Service
                </>
              )}
            </motion.button>
            <motion.button
              type="button"
              className="w-full sm:w-auto mt-3 sm:mt-0 flex justify-center items-center text-gray-700 bg-gray-100 px-6 py-3 rounded-lg hover:bg-gray-200 focus:outline-none focus:ring-2 focus:ring-gray-300 transition-colors font-medium"
              onClick={() => setOpenEditDialog(false)}
              whileHover={{ scale: 1.03 }}
              whileTap={{ scale: 0.97 }}
            >
              <svg xmlns="http://www.w3.org/2000/svg" className="h-5 w-5 mr-2" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M6 18L18 6M6 6l12 12" />
              </svg>
              Cancel
            </motion.button>
          </motion.div>
        </div>
      </BaseModal>

      {/* Delete Confirmation Dialog */}
      <BaseModal 
        isOpen={openDeleteDialog}
        onClose={() => setOpenDeleteDialog(false)}
        maxWidth="max-w-lg"
      >
        <div className="bg-white rounded-xl text-left overflow-hidden shadow-xl border border-gray-100">
          <motion.div 
            className="bg-gradient-to-r from-red-600 to-red-500 px-6 py-4"
            initial={{ opacity: 0, x: -20 }}
            animate={{ opacity: 1, x: 0 }}
            transition={{ delay: 0.1, duration: 0.3 }}
          >
            <h3 className="text-lg font-medium text-white flex items-center" id="modal-title">
              <svg xmlns="http://www.w3.org/2000/svg" className="h-5 w-5 mr-2" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16" />
              </svg>
              Confirm Deletion
            </h3>
          </motion.div>
          
          <motion.div 
            className="bg-white px-6 pt-5 pb-6"
            initial={{ opacity: 0, y: 10 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ delay: 0.2, duration: 0.3 }}
          >
            <div className="flex items-start space-x-4">
              <div className="flex-shrink-0 w-12 h-12 rounded-full bg-red-100 flex items-center justify-center">
                <svg className="h-6 w-6 text-red-600" fill="none" stroke="currentColor" viewBox="0 0 24 24" xmlns="http://www.w3.org/2000/svg">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z"></path>
                </svg>
              </div>
              <div>
                <h3 className="text-lg font-medium text-gray-900 mb-2">
                  Delete Service
                </h3>
                <p className="text-gray-600">
                  Are you sure you want to delete the service "<span className="font-semibold text-gray-900">{currentService.serviceName}</span>"? This action cannot be undone.
                </p>
              </div>
            </div>
          </motion.div>
          
          <motion.div 
            className="bg-gray-50 px-6 py-4 sm:flex sm:flex-row-reverse gap-3"
            initial={{ opacity: 0, y: 10 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ delay: 0.3, duration: 0.3 }}
          >
            <motion.button 
              type="button" 
              className="w-full sm:w-auto flex justify-center items-center bg-red-600 text-white px-6 py-3 font-medium rounded-lg hover:bg-red-700 focus:outline-none focus:ring-2 focus:ring-red-400 transition-colors"
              onClick={handleDeleteService}
              disabled={loading}
              whileHover={{ scale: 1.03 }}
              whileTap={{ scale: 0.97 }}
            >
              {loading ? (
                <span className="flex items-center">
                  <svg className="animate-spin -ml-1 mr-2 h-5 w-5 text-white" fill="none" viewBox="0 0 24 24">
                    <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4"></circle>
                    <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
                  </svg>
                  Deleting...
                </span>
              ) : (
                <>
                  <svg className="w-5 h-5 mr-2" fill="none" stroke="currentColor" viewBox="0 0 24 24" xmlns="http://www.w3.org/2000/svg">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16"></path>
                  </svg>
                  Delete Service
                </>
              )}
            </motion.button>
            <motion.button 
              type="button" 
              className="w-full sm:w-auto mt-3 sm:mt-0 flex justify-center items-center text-gray-700 bg-gray-100 px-6 py-3 rounded-lg hover:bg-gray-200 focus:outline-none focus:ring-2 focus:ring-gray-300 transition-colors font-medium"
              onClick={() => setOpenDeleteDialog(false)}
              whileHover={{ scale: 1.03 }}
              whileTap={{ scale: 0.97 }}
            >
              <svg xmlns="http://www.w3.org/2000/svg" className="h-5 w-5 mr-2" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M6 18L18 6M6 6l12 12" />
              </svg>
              Cancel
            </motion.button>
          </motion.div>
        </div>
      </BaseModal>

      {/* Image Modal */}
      <BaseModal
        isOpen={isImageModalOpen}
        onClose={() => setIsImageModalOpen(false)}
        maxWidth="max-w-4xl"
      >
        <div className="relative bg-black rounded-lg overflow-hidden">
          {/* Close button */}
          <motion.button
            className="absolute top-4 right-4 bg-black bg-opacity-50 text-white p-2 rounded-full z-10"
            onClick={() => setIsImageModalOpen(false)}
            whileHover={{ scale: 1.1, backgroundColor: "rgba(0,0,0,0.7)" }}
            whileTap={{ scale: 0.9 }}
          >
            <svg xmlns="http://www.w3.org/2000/svg" className="h-6 w-6" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M6 18L18 6M6 6l12 12" />
            </svg>
          </motion.button>
          
          {/* Image */}
          <motion.div
            initial={{ opacity: 0 }}
            animate={{ opacity: 1 }}
            className="flex items-center justify-center"
          >
            <img 
              src={expandedImage} 
              alt="Service Preview" 
              className="max-w-full max-h-[80vh] object-contain"
            />
          </motion.div>
        </div>
      </BaseModal>
    </motion.div>
  );
}

export default MyServicesContent;