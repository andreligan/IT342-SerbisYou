import React, { useState, useEffect } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import axios from 'axios';

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

  return (
    <div className="max-w-7xl px-4 sm:px-6 lg:px-8 py-8">
      <div className="mb-8">
        <h1 className="text-3xl font-bold text-gray-800">My Services</h1>
        <p className="text-gray-600 mt-2">
          Manage the services you offer to your clients
        </p>
      </div>

      {error && (
        <div className="mb-6 p-4 bg-red-50 border-l-4 border-red-500 rounded-md text-red-700 shadow">
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
        </div>
      )}

      {success && (
        <div className="mb-6 p-4 bg-green-50 border-l-4 border-green-500 rounded-md text-green-700 shadow">
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
        </div>
      )}

      {/* Add Service Button */}
      <div className="flex justify-end mb-6">
        <button 
          className="flex items-center px-4 py-2 bg-[#F4CE14] text-[#495E57] font-medium rounded-md hover:bg-yellow-300 shadow-md transition-colors"
          onClick={handleAddClick}
        >
          <svg className="w-5 h-5 mr-2" fill="none" stroke="currentColor" viewBox="0 0 24 24" xmlns="http://www.w3.org/2000/svg">
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M12 6v6m0 0v6m0-6h6m-6 0H6"></path>
          </svg>
          Add New Service
        </button>
      </div>

      {/* Loading State */}
      {loading && !Object.keys(servicesByCategory).length ? (
        <div className="flex justify-center items-center py-16 bg-gray-50 rounded-lg shadow">
          <div className="animate-spin rounded-full h-12 w-12 border-t-2 border-b-2 border-[#495E57]"></div>
        </div>
      ) : !Object.keys(servicesByCategory).length ? (
        <div className="flex flex-col items-center justify-center py-16 px-4 bg-gray-50 rounded-lg shadow">
          <svg className="w-16 h-16 mb-4 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24" xmlns="http://www.w3.org/2000/svg">
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z"></path>
          </svg>
          <p className="text-gray-600 text-lg font-medium mb-2">
            No services added yet
          </p>
          <p className="text-gray-500 text-center mb-6">
            Start by adding your first service to showcase to clients
          </p>
          <button 
            className="flex items-center px-4 py-2 bg-[#F4CE14] text-[#495E57] font-medium rounded-md hover:bg-yellow-300 shadow transition-colors"
            onClick={handleAddClick}
          >
            <svg className="w-5 h-5 mr-2" fill="none" stroke="currentColor" viewBox="0 0 24 24" xmlns="http://www.w3.org/2000/svg">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M12 6v6m0 0v6m0-6h6m-6 0H6"></path>
            </svg>
            Add New Service
          </button>
        </div>
      ) : (
        <div className="bg-white rounded-lg shadow overflow-hidden">
          {/* Horizontal Tabs */}
          <div className="border-b border-gray-200">
            <nav className="flex overflow-x-auto">
              {Object.entries(servicesByCategory).map(([categoryId, category]) => (
                <button
                  key={categoryId}
                  onClick={() => setActiveTab(categoryId)}
                  className={`
                    whitespace-nowrap py-4 px-6 border-b-2 font-medium text-sm flex items-center
                    ${activeTab === categoryId 
                      ? 'border-[#F4CE14] text-[#495E57]' 
                      : 'border-transparent text-gray-500 hover:text-gray-700 hover:border-gray-300'}
                  `}
                >
                  {category.categoryName}
                  <span className={`ml-2 py-0.5 px-2 rounded-full text-xs font-medium 
                    ${activeTab === categoryId ? 'bg-[#F4CE14] text-[#495E57]' : 'bg-gray-100 text-gray-600'}`}>
                    {category.services.length}
                  </span>
                </button>
              ))}
            </nav>
          </div>

          {/* Services Container */}
          <div className="p-6 bg-gray-50">
            {activeTab && servicesByCategory[activeTab] && (
              <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
                {servicesByCategory[activeTab].services.map((service) => (
                  <Link
                  key={service.serviceId}
                  to={`/service/${service.serviceId}`}
                  className="block"
                >
                  <div key={service.serviceId} className="bg-white rounded-lg overflow-hidden shadow hover:shadow-md transition-shadow border border-gray-100">
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
                        <span className="font-medium mr-1">Price:</span> {service.price}
                      </div>
                      <p className="text-gray-700 text-sm line-clamp-3">{service.serviceDescription}</p>
                    </div>
                    <div className="flex justify-end p-3 bg-gray-50 border-t border-gray-100">
                      <button
                        className="p-2 mr-2 text-blue-600 hover:bg-blue-50 rounded-full transition-colors flex items-center"
                        onClick={() => handleEditClick(service)}
                      >
                        <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24" xmlns="http://www.w3.org/2000/svg">
                          <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M11 5H6a2 2 0 00-2 2v11a2 2 0 002 2h11a2 2 0 002-2v-5m-1.414-9.414a2 2 0 112.828 2.828L11.828 15H9v-2.828l8.586-8.586z"></path>
                        </svg>
                      </button>
                      <button
                        className="p-2 text-red-600 hover:bg-red-50 rounded-full transition-colors flex items-center"
                        onClick={() => handleDeleteClick(service)}
                      >
                        <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24" xmlns="http://www.w3.org/2000/svg">
                          <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16"></path>
                        </svg>
                      </button>
                    </div>
                  </div>
                  </Link>
                ))}
              </div>
            )}
          </div>
        </div>
      )}

      {/* Edit Service Dialog */}
      {openEditDialog && (
        <div className="fixed inset-0 z-50 overflow-y-auto" aria-labelledby="modal-title" role="dialog" aria-modal="true">
          <div className="flex items-end justify-center min-h-screen pt-4 px-4 pb-20 text-center sm:block sm:p-0">
            {/* Background overlay */}
            <div className="fixed inset-0 bg-gray-500 bg-opacity-75 transition-opacity" onClick={() => setOpenEditDialog(false)}></div>

            {/* Modal panel */}
            <div className="inline-block align-bottom bg-white rounded-lg text-left overflow-hidden shadow-xl transform transition-all sm:my-8 sm:align-middle sm:max-w-lg sm:w-full">
              <div className="bg-[#495E57] px-4 py-3 flex justify-between items-center">
                <h3 className="text-lg leading-6 font-medium text-white" id="modal-title">
                  Edit Service
                </h3>
                <button
                  onClick={() => setOpenEditDialog(false)}
                  className="text-white hover:text-gray-200"
                >
                  <svg className="h-6 w-6" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M6 18L18 6M6 6l12 12" />
                  </svg>
                </button>
              </div>
              <div className="bg-white px-4 pt-5 pb-4 sm:p-6">
                <div className="space-y-4">
                  <div>
                    <label htmlFor="categoryId" className="block text-sm font-medium text-gray-700">Service Category</label>
                    <select
                      id="categoryId"
                      name="categoryId"
                      value={currentService.categoryId}
                      onChange={handleInputChange}
                      className="mt-1 block w-full pl-3 pr-10 py-2 text-base border border-gray-300 focus:outline-none focus:ring-[#F4CE14] focus:border-[#F4CE14] sm:text-sm rounded-md"
                      required
                    >
                      <option value="">Select a category</option>
                      {categories.map((category) => (
                        <option key={category.categoryId} value={category.categoryId}>
                          {category.categoryName}
                        </option>
                      ))}
                    </select>
                  </div>
                  
                  <div>
                    <label htmlFor="serviceName" className="block text-sm font-medium text-gray-700">Service Name</label>
                    <input
                      type="text"
                      id="serviceName"
                      name="serviceName"
                      value={currentService.serviceName}
                      onChange={handleInputChange}
                      className="mt-1 block w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:ring-[#F4CE14] focus:border-[#F4CE14] sm:text-sm"
                      required
                    />
                  </div>
                  
                  <div>
                    <label htmlFor="serviceDescription" className="block text-sm font-medium text-gray-700">Service Description</label>
                    <textarea
                      id="serviceDescription"
                      name="serviceDescription"
                      rows="3"
                      value={currentService.serviceDescription}
                      onChange={handleInputChange}
                      className="mt-1 block w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:ring-[#F4CE14] focus:border-[#F4CE14] sm:text-sm"
                      required
                    ></textarea>
                  </div>
                  
                  <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
                    <div>
                      <label htmlFor="price" className="block text-sm font-medium text-gray-700">Price Range</label>
                      <input
                        type="text"
                        id="price"
                        name="price"
                        value={currentService.price}
                        onChange={handleInputChange}
                        className="mt-1 block w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:ring-[#F4CE14] focus:border-[#F4CE14] sm:text-sm"
                        required
                      />
                    </div>
                    
                    <div>
                      <label htmlFor="durationEstimate" className="block text-sm font-medium text-gray-700">Duration Estimate</label>
                      <input
                        type="text"
                        id="durationEstimate"
                        name="durationEstimate"
                        value={currentService.durationEstimate}
                        onChange={handleInputChange}
                        className="mt-1 block w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:ring-[#F4CE14] focus:border-[#F4CE14] sm:text-sm"
                        required
                      />
                    </div>
                  </div>
                </div>
              </div>
              <div className="bg-gray-50 px-4 py-3 sm:px-6 sm:flex sm:flex-row-reverse">
                <button
                  type="button"
                  className="w-full inline-flex justify-center rounded-md border border-transparent shadow-sm px-4 py-2 bg-[#F4CE14] text-base font-medium text-[#495E57] hover:bg-yellow-300 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-yellow-500 sm:ml-3 sm:w-auto sm:text-sm"
                  onClick={handleUpdateService}
                  disabled={loading || !currentService.categoryId || !currentService.serviceName}
                >
                  {loading ? (
                    <>
                      <svg className="animate-spin -ml-1 mr-2 h-4 w-4 text-[#495E57]" fill="none" viewBox="0 0 24 24">
                        <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4"></circle>
                        <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
                      </svg>
                      Updating...
                    </>
                  ) : 'Update Service'}
                </button>
                <button
                  type="button"
                  className="mt-3 w-full inline-flex justify-center rounded-md border border-gray-300 shadow-sm px-4 py-2 bg-white text-base font-medium text-gray-700 hover:bg-gray-50 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-[#495E57] sm:mt-0 sm:ml-3 sm:w-auto sm:text-sm"
                  onClick={() => setOpenEditDialog(false)}
                >
                  Cancel
                </button>
              </div>
            </div>
          </div>
        </div>
      )}

      {/* Delete Confirmation Dialog */}
      {openDeleteDialog && (
        <div className="fixed inset-0 z-50 overflow-y-auto" aria-labelledby="modal-title" role="dialog" aria-modal="true">
          <div className="flex items-end justify-center min-h-screen pt-4 px-4 pb-20 text-center sm:block sm:p-0">
            {/* Background overlay */}
            <div className="fixed inset-0 bg-gray-500 bg-opacity-75 transition-opacity" onClick={() => setOpenDeleteDialog(false)}></div>

            {/* Modal panel */}
            <div className="inline-block align-bottom bg-white rounded-lg text-left overflow-hidden shadow-xl transform transition-all sm:my-8 sm:align-middle sm:max-w-lg sm:w-full">
              <div className="bg-red-600 px-4 py-3">
                <h3 className="text-lg leading-6 font-medium text-white" id="modal-title">
                  Confirm Deletion
                </h3>
              </div>
              <div className="bg-white px-4 pt-5 pb-4 sm:p-6">
                <div className="sm:flex sm:items-start">
                  <div className="mx-auto flex-shrink-0 flex items-center justify-center h-12 w-12 rounded-full bg-red-100 sm:mx-0 sm:h-10 sm:w-10">
                    <svg className="h-6 w-6 text-red-600" fill="none" stroke="currentColor" viewBox="0 0 24 24" xmlns="http://www.w3.org/2000/svg">
                      <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z"></path>
                    </svg>
                  </div>
                  <div className="mt-3 text-center sm:mt-0 sm:ml-4 sm:text-left">
                    <h3 className="text-lg leading-6 font-medium text-gray-900" id="modal-headline">
                      Delete Service
                    </h3>
                    <div className="mt-2">
                      <p className="text-sm text-gray-500">
                        Are you sure you want to delete the service "{currentService.serviceName}"? This action cannot be undone.
                      </p>
                    </div>
                  </div>
                </div>
              </div>
              <div className="bg-gray-50 px-4 py-3 sm:px-6 sm:flex sm:flex-row-reverse">
                <button 
                  type="button" 
                  className="w-full inline-flex justify-center rounded-md border border-transparent shadow-sm px-4 py-2 bg-red-600 text-base font-medium text-white hover:bg-red-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-red-500 sm:ml-3 sm:w-auto sm:text-sm"
                  onClick={handleDeleteService}
                  disabled={loading}
                >
                  {loading ? (
                    <svg className="animate-spin -ml-1 mr-2 h-4 w-4 text-white" fill="none" viewBox="0 0 24 24">
                      <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4"></circle>
                      <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
                    </svg>
                  ) : ''}
                  Delete
                </button>
                <button 
                  type="button" 
                  className="mt-3 w-full inline-flex justify-center rounded-md border border-gray-300 shadow-sm px-4 py-2 bg-white text-base font-medium text-gray-700 hover:bg-gray-50 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-[#495E57] sm:mt-0 sm:ml-3 sm:w-auto sm:text-sm"
                  onClick={() => setOpenDeleteDialog(false)}
                >
                  Cancel
                </button>
              </div>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}

export default MyServicesContent;