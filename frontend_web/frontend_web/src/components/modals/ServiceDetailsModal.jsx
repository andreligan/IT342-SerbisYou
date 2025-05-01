import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { motion } from 'framer-motion';
import apiClient, { getApiUrl, API_BASE_URL } from '../../utils/apiConfig';
import BaseModal from '../shared/BaseModal';

const ServiceDetailsModal = ({ isOpen, onClose, serviceId, position = { top: 0, left: 0 } }) => {
  const [service, setService] = useState(null);
  const [provider, setProvider] = useState(null);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState(null);
  const [serviceImage, setServiceImage] = useState(null);
  const [showFullDescription, setShowFullDescription] = useState(false);
  const navigate = useNavigate();

  useEffect(() => {
    const fetchServiceDetails = async () => {
      if (!serviceId) return;

      try {
        setIsLoading(true);
        setError(null);

        // Get service details
        const serviceResponse = await apiClient.get(getApiUrl(`/services/getById/${serviceId}`));
        const serviceData = serviceResponse.data;

        setService(serviceData);

        // If service has provider data, fetch provider details
        if (serviceData?.provider?.providerId) {
          const providerResponse = await apiClient.get(
            getApiUrl(`/service-providers/getById/${serviceData.provider.providerId}`)
          );
          setProvider(providerResponse.data);
        }

        // Fetch service image
        try {
          const imageResponse = await apiClient.get(getApiUrl(`/services/getServiceImage/${serviceId}`));
          if (imageResponse.data) {
            // Use API_BASE_URL to construct complete image URL
            setServiceImage(`${API_BASE_URL}${imageResponse.data}`);
          }
        } catch (err) {
          console.warn("Could not load service image:", err);
          // Continue without image
        }

        setIsLoading(false);
      } catch (err) {
        console.error('Error fetching service details:', err);
        setError('Failed to load service details');
        setIsLoading(false);
      }
    };

    if (isOpen && serviceId) {
      fetchServiceDetails();
    } else {
      // Reset state when modal is closed
      setService(null);
      setProvider(null);
      setServiceImage(null);
      setError(null);
    }
  }, [isOpen, serviceId]);

  const handleBookNow = () => {
    // Ensure serviceId is passed correctly
    navigate(`/book-service/${serviceId}`);
    onClose();
  };

  // Format currency
  const formatPrice = (price) => {
    return new Intl.NumberFormat('en-PH', {
      style: 'currency',
      currency: 'PHP',
    }).format(price);
  };

  const truncateDescription = (text, maxLength = 150) => {
    if (!text) return '';
    if (text.length <= maxLength) return text;
    return text.slice(0, maxLength) + '...';
  };

  return (
    <BaseModal isOpen={isOpen} onClose={onClose} maxWidth="max-w-4xl" clickPosition={position}>
      <motion.div
        className="bg-white rounded-lg shadow-lg overflow-hidden"
        initial={{ opacity: 0, y: 10 }}
        animate={{ opacity: 1, y: 0 }}
        exit={{ opacity: 0, y: 10 }}
        transition={{ duration: 0.3 }}
      >
        {isLoading ? (
          <div className="p-8 flex justify-center items-center h-64">
            <div className="w-10 h-10 border-4 border-[#F4CE14] border-t-transparent rounded-full animate-spin"></div>
          </div>
        ) : error ? (
          <div className="p-8 text-center h-64 flex flex-col justify-center items-center">
            <svg className="w-12 h-12 text-red-500 mb-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 8v4m0 4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
            </svg>
            <h3 className="text-lg font-semibold text-gray-800 mb-2">Error Loading Service Details</h3>
            <p className="text-gray-600">{error}</p>
          </div>
        ) : service ? (
          <>
            <div className="relative">
              <div className="h-56 bg-gray-200">
                {serviceImage ? (
                  <motion.img
                    src={serviceImage}
                    alt={service.serviceName}
                    className="w-full h-full object-cover"
                    initial={{ opacity: 0 }}
                    animate={{ opacity: 1 }}
                    transition={{ duration: 0.5 }}
                  />
                ) : (
                  <div className="w-full h-full flex items-center justify-center bg-gradient-to-r from-gray-300 to-gray-200">
                    <svg className="w-16 h-16 text-gray-400" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                      <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M4 16l4.586-4.586a2 2 0 012.828 0L16 16m-2-2l1.586-1.586a2 2 0 012.828 0L20 14m-6-6h.01M6 20h12a2 2 0 002-2V6a2 2 0 00-2-2H6a2 2 0 00-2 2v12a2 2 0 002 2z" />
                    </svg>
                  </div>
                )}

                <button
                  onClick={onClose}
                  className="absolute top-4 right-4 p-1 bg-black/50 text-white rounded-full hover:bg-black/70 transition-colors"
                >
                  <svg xmlns="http://www.w3.org/2000/svg" className="h-6 w-6" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M6 18L18 6M6 6l12 12" />
                  </svg>
                </button>
              </div>

              <div className="p-6">
                <div className="flex justify-between items-start mb-4">
                  <div>
                    <h2 className="text-2xl font-bold text-gray-800">{service.serviceName}</h2>
                    <div className="flex items-center mt-1">
                      <span className="text-[#F4CE14] font-semibold text-xl">
                        {service.price && formatPrice(service.price)}
                      </span>
                      {service.durationEstimate && (
                        <span className="ml-3 bg-gray-100 text-gray-700 px-3 py-1 rounded-full text-sm">
                          {service.durationEstimate}
                        </span>
                      )}
                    </div>
                  </div>

                  <motion.button
                    whileHover={{ scale: 1.03 }}
                    whileTap={{ scale: 0.97 }}
                    className="px-6 py-2 bg-[#F4CE14] text-[#495E57] rounded-lg font-semibold shadow-md hover:bg-yellow-400 transition-colors"
                    onClick={handleBookNow}
                  >
                    Book this Service
                  </motion.button>
                </div>

                {/* Service Provider */}
                {provider && (
                  <div className="mb-4 p-4 bg-gray-50 rounded-lg">
                    <h3 className="text-lg font-semibold text-gray-800 mb-2">Service Provider</h3>
                    <div className="flex items-center">
                      <div className="h-12 w-12 rounded-full bg-[#495E57] text-white flex items-center justify-center font-bold text-lg">
                        {provider.firstName?.[0] || provider.businessName?.[0] || 'SP'}
                      </div>
                      <div className="ml-3">
                        <p className="font-semibold">{provider.businessName || `${provider.firstName} ${provider.lastName}`}</p>
                        <div className="flex items-center text-sm text-gray-600">
                          <svg xmlns="http://www.w3.org/2000/svg" className="h-4 w-4 mr-1" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z" />
                          </svg>
                          {provider.yearsOfExperience} {provider.yearsOfExperience === 1 ? 'year' : 'years'} of experience
                        </div>
                      </div>
                    </div>
                  </div>
                )}

                {/* Description */}
                <div className="mb-6">
                  <h3 className="text-lg font-semibold text-gray-800 mb-2">Description</h3>
                  <div className="text-gray-600">
                    {showFullDescription ? (
                      service.serviceDescription
                    ) : (
                      truncateDescription(service.serviceDescription)
                    )}
                    {service.serviceDescription?.length > 150 && (
                      <button
                        onClick={() => setShowFullDescription(!showFullDescription)}
                        className="text-blue-600 hover:text-blue-800 font-medium ml-1"
                      >
                        {showFullDescription ? 'Show less' : 'Read more'}
                      </button>
                    )}
                  </div>
                </div>

                {/* Service Category */}
                {service.category && (
                  <div className="mb-4">
                    <h3 className="text-lg font-semibold text-gray-800 mb-2">Category</h3>
                    <span className="bg-[#495E57]/10 text-[#495E57] px-3 py-1 rounded-full">
                      {service.category.categoryName}
                    </span>
                  </div>
                )}
              </div>
            </div>
          </>
        ) : (
          <div className="p-8 text-center h-64 flex flex-col justify-center items-center">
            <p>No service data available</p>
          </div>
        )}
      </motion.div>
    </BaseModal>
  );
};

export default ServiceDetailsModal;
