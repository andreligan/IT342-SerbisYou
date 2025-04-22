import React, { useState, useEffect, useCallback } from "react";
import { useParams, useNavigate } from "react-router-dom";
import axios from "axios";
import ServiceDetailsModal from "../modals/ServiceDetailsModal";

const BASE_URL = "http://localhost:8080";

const ServiceProviderDetails = () => {
  const { providerId } = useParams();
  const [provider, setProvider] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [imageFailed, setImageFailed] = useState(false);
  const [providerServices, setProviderServices] = useState([]);
  const [servicesLoading, setServicesLoading] = useState(true);
  const [serviceRatings, setServiceRatings] = useState({});
  const [sortBy, setSortBy] = useState('recommended');
  const [selectedService, setSelectedService] = useState(null);
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [clickPosition, setClickPosition] = useState(null);
  const navigate = useNavigate();

  useEffect(() => {
    const fetchProviderDetails = async () => {
      try {
        console.log("Fetching provider details for ID:", providerId);
        const token = localStorage.getItem("authToken") || sessionStorage.getItem("authToken");
        
        try {
          const response = await axios.get(`${BASE_URL}/api/service-providers/getById/${providerId}`, {
            headers: { Authorization: `Bearer ${token}` }
          });
          
          console.log("Provider data received:", response.data);
          setProvider(response.data);
          setLoading(false);
        } catch (err) {
          console.log("First endpoint failed, trying alternative...");
          
          try {
            const altResponse = await axios.get(`${BASE_URL}/api/serviceProvider/${providerId}`, {
              headers: { Authorization: `Bearer ${token}` }
            });
            
            console.log("Provider data received from alt endpoint:", altResponse.data);
            setProvider(altResponse.data);
            setLoading(false);
          } catch (err2) {
            console.log("Second endpoint failed, trying final alternative...");
            const altResponse2 = await axios.get(`${BASE_URL}/api/providers/${providerId}`, {
              headers: { Authorization: `Bearer ${token}` }
            });
            
            console.log("Provider data received from final alt endpoint:", altResponse2.data);
            setProvider(altResponse2.data);
            setLoading(false);
          }
        }
      } catch (err) {
        console.error("Error fetching provider details:", err);
        setError("Failed to load provider details. Please try again later.");
        setLoading(false);
      }
    };

    if (providerId) {
      fetchProviderDetails();
    }
  }, [providerId]);

  useEffect(() => {
    if (providerId && !loading && provider) {
      fetchProviderServices();
    }
  }, [providerId, loading, provider]);

  const fetchProviderServices = async () => {
    try {
      setServicesLoading(true);
      const token = localStorage.getItem("authToken") || sessionStorage.getItem("authToken");
      
      try {
        const servicesResponse = await axios.get(`${BASE_URL}/api/services/getAll`, {
          headers: {
            Authorization: `Bearer ${token}`,
          },
        });
        
        const providerIdNum = parseInt(providerId);
        let services = servicesResponse.data.filter(service => 
          service.provider && 
          (service.provider.providerId === providerIdNum || 
           service.provider.userId === providerIdNum || 
           service.provider.id === providerIdNum)
        );

        console.log(`Filtered ${services.length} services for provider ID ${providerId}`);
        
        // Add the provider's profile image to each service object
        services = services.map(service => {
          if (service.provider) {
            service.provider.profileImage = provider.profileImage || provider.serviceProviderImage;
          }
          return service;
        });
        
        const ratingsMap = {};
        const servicesWithImages = await Promise.all(
          services.map(async (service) => {
            try {
              const imageResponse = await axios.get(`${BASE_URL}/api/services/getServiceImage/${service.serviceId}`, {
                headers: { Authorization: `Bearer ${token}` },
              });
              service.serviceImage = imageResponse.data;
              
              try {
                const ratingResponse = await axios.get(`${BASE_URL}/api/reviews/getServiceRating/${service.serviceId}`, {
                  headers: { Authorization: `Bearer ${token}` },
                });
                ratingsMap[service.serviceId] = ratingResponse.data;
              } catch (error) {
                console.error(`Error fetching rating for service ${service.serviceId}:`, error);
                ratingsMap[service.serviceId] = { averageRating: 0, reviewCount: 0 };
              }
              
              if (!service.categoryName && service.category) {
                service.categoryName = service.category.categoryName || "Uncategorized";
              }
              
              return service;
            } catch (error) {
              console.error(`Error processing service ${service.serviceId}:`, error);
              return service;
            }
          })
        );
        
        setProviderServices(servicesWithImages);
        setServiceRatings(ratingsMap);
      } catch (err) {
        console.error("Error fetching services:", err);
        
        try {
          const altResponse = await axios.get(`${BASE_URL}/api/services/getByProviderId/${providerId}`, {
            headers: { Authorization: `Bearer ${token}` }
          });
          
          console.log("Provider services received from direct endpoint:", altResponse.data);
          
          const servicesData = altResponse.data.map(service => {
            if (service.provider) {
              service.provider.profileImage = provider.profileImage || provider.serviceProviderImage;
            }
            return service;
          });
          
          const ratingsMap = {};
          
          await Promise.all(
            servicesData.map(async (service) => {
              try {
                const ratingResponse = await axios.get(`${BASE_URL}/api/reviews/getServiceRating/${service.serviceId}`, {
                  headers: { Authorization: `Bearer ${token}` },
                });
                ratingsMap[service.serviceId] = ratingResponse.data;
              } catch (e) {
                console.warn(`Couldn't fetch rating for service ${service.serviceId}:`, e);
                ratingsMap[service.serviceId] = { averageRating: 0, reviewCount: 0 };
              }
            })
          );
          
          setProviderServices(servicesData);
          setServiceRatings(ratingsMap);
        } catch (altErr) {
          console.error("All attempts to fetch services failed:", altErr);
          setProviderServices([]);
        }
      }
      
      setServicesLoading(false);
    } catch (err) {
      console.error("Error in service fetching process:", err);
      setProviderServices([]);
      setServicesLoading(false);
    }
  };

  const renderStars = useCallback((rating) => {
    const stars = [];
    for (let i = 1; i <= 5; i++) {
      stars.push(
        i <= rating ? (
          <span key={i} className="text-yellow-400">&#9733;</span>
        ) : (
          <span key={i} className="text-yellow-400">&#9734;</span>
        )
      );
    }
    return stars;
  }, []);

  const sortServices = (services) => {
    if (!services || !services.length) return [];
    
    const clonedServices = [...services];
    
    switch(sortBy) {
      case 'price_low':
        return clonedServices.sort((a, b) => (parseFloat(a.price) || 0) - (parseFloat(b.price) || 0));
      case 'price_high':
        return clonedServices.sort((a, b) => (parseFloat(b.price) || 0) - (parseFloat(a.price) || 0));
      case 'rating':
        return clonedServices.sort((a, b) => 
          (serviceRatings[b.serviceId]?.averageRating || 0) - (serviceRatings[a.serviceId]?.averageRating || 0)
        );
      case 'recommended':
      default:
        return clonedServices.sort((a, b) => {
          const scoreA = (serviceRatings[a.serviceId]?.averageRating || 0) * 2 + (a.featured ? 3 : 0);
          const scoreB = (serviceRatings[b.serviceId]?.averageRating || 0) * 2 + (b.featured ? 3 : 0);
          return scoreB - scoreA;
        });
    }
  };

  const getImageUrl = (imagePath) => {
    if (!imagePath) return "/default-profile.jpg";
    
    if (imagePath.startsWith('http')) {
      return imagePath;
    }
    
    if (imagePath.startsWith('/')) {
      return `${BASE_URL}${imagePath}`;
    }
    
    return `${BASE_URL}/${imagePath}`;
  };

  const handleImageError = (e) => {
    console.error("Image failed to load:", e.target.src);
    
    if (!imageFailed) {
      setImageFailed(true);
      e.target.src = "/default-profile.jpg";
    }
  };

  const handleOpenModal = useCallback((service, event) => {
    console.log("Selected Service:", service);
    setSelectedService(service);
    
    if (event && event.currentTarget) {
      const rect = event.currentTarget.getBoundingClientRect();
      setClickPosition({
        x: rect.left + (rect.width / 2) - (window.innerWidth / 2), 
        y: rect.top + (rect.height / 2) - (window.innerHeight / 2)
      });
    } else {
      setClickPosition(null);
    }
    
    setIsModalOpen(true);
  }, []);

  const handleCloseModal = useCallback(() => {
    setSelectedService(null);
    setIsModalOpen(false);
  }, []);

  const handleBookService = useCallback(() => {
    if (selectedService) {
      navigate("/bookService", { state: { service: selectedService } });
    }
  }, [selectedService, navigate]);

  if (loading) {
    return (
      <div className="flex justify-center items-center min-h-screen">
        <div className="animate-spin rounded-full h-16 w-16 border-t-2 border-b-2 border-[#495E57]"></div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="flex flex-col items-center justify-center min-h-screen p-4">
        <div className="bg-red-100 border border-red-400 text-red-700 px-4 py-3 rounded mb-4">
          <p>{error}</p>
        </div>
        <button
          onClick={() => navigate(-1)}
          className="px-4 py-2 bg-[#495E57] text-white rounded hover:bg-[#3e4f49]"
        >
          Go Back
        </button>
      </div>
    );
  }

  if (!provider) {
    return (
      <div className="flex flex-col items-center justify-center min-h-screen p-4">
        <div className="bg-yellow-100 border border-yellow-400 text-yellow-700 px-4 py-3 rounded mb-4">
          <p>Provider not found</p>
        </div>
        <button
          onClick={() => navigate(-1)}
          className="px-4 py-2 bg-[#495E57] text-white rounded hover:bg-[#3e4f49]"
        >
          Go Back
        </button>
      </div>
    );
  }

  const profileImagePath = provider.profileImage || provider.serviceProviderImage || null;
  console.log("Provider image data:", { 
    rawPath: profileImagePath,
    processedUrl: getImageUrl(profileImagePath),
    provider: provider
  });

  return (
    <div className="container mx-auto p-4 max-w-6xl">
      <button
        onClick={() => navigate(-1)}
        className="mb-6 flex items-center gap-2 text-[#495E57] hover:text-[#F4CE14] transition-colors"
      >
        <i className="fas fa-arrow-left"></i> Back
      </button>
      
      <div className="bg-white rounded-lg shadow-md overflow-hidden mb-8">
        <div className="bg-[#495E57] p-6 text-white relative">
          <h1 className="text-3xl font-bold">Service Provider Details</h1>
        </div>
        
        <div className="p-6">
          <div className="flex flex-col md:flex-row items-start gap-8">
            <div className="md:w-1/4 flex flex-col items-center">
              <div className="relative w-48 h-48">
                <img
                  src={getImageUrl(profileImagePath)}
                  alt={`${provider.firstName} ${provider.lastName}`}
                  className="w-48 h-48 rounded-full border-4 border-[#F4CE14] shadow-lg object-cover"
                  onError={handleImageError}
                />
                {imageFailed && (
                  <div className="absolute inset-0 flex items-center justify-center text-sm text-gray-500 bg-gray-100 rounded-full">
                    No Image Available
                  </div>
                )}
              </div>
              {provider.verified && (
                <div className="mt-4 bg-green-100 text-green-800 px-4 py-2 rounded-full flex items-center gap-2">
                  <i className="fas fa-check-circle"></i>
                  <span>Verified Provider</span>
                </div>
              )}
            </div>
            
            <div className="flex-1">
              <h2 className="text-2xl font-bold text-[#495E57] mb-2">
                {provider.firstName} {provider.lastName}
              </h2>
              
              {provider.businessName && (
                <p className="text-lg text-gray-700 mb-4">
                  <i className="fas fa-briefcase text-[#495E57] mr-2"></i>
                  {provider.businessName}
                </p>
              )}
              
              <div className="grid grid-cols-1 md:grid-cols-2 gap-6 mt-6">
                <div className="bg-gray-50 p-4 rounded-lg">
                  <h3 className="font-semibold text-[#495E57] mb-2">Contact Information</h3>
                  <p className="flex items-center gap-2 mb-2">
                    <i className="fas fa-envelope text-[#495E57]"></i>
                    {provider.email}
                  </p>
                  <p className="flex items-center gap-2">
                    <i className="fas fa-phone text-[#495E57]"></i>
                    {provider.phoneNumber || "No phone number provided"}
                  </p>
                </div>
                
                <div className="bg-gray-50 p-4 rounded-lg">
                  <h3 className="font-semibold text-[#495E57] mb-2">Experience & Rating</h3>
                  <p className="flex items-center gap-2 mb-2">
                    <i className="fas fa-calendar-alt text-[#495E57]"></i>
                    {provider.yearsOfExperience ? `${provider.yearsOfExperience} years experience` : "Experience not specified"}
                  </p>
                  <div className="flex items-center gap-2">
                    <div className="flex">
                      {renderStars(provider.averageRating || 0)}
                    </div>
                    <span className="text-gray-700">
                      ({provider.averageRating?.toFixed(1) || "No ratings"})
                    </span>
                  </div>
                </div>
              </div>
              
              <div className="mt-6 bg-gray-50 p-4 rounded-lg">
                <h3 className="font-semibold text-[#495E57] mb-2">Availability</h3>
                <p className="text-gray-700">
                  {provider.availabilitySchedule || "Contact provider for availability details"}
                </p>
              </div>
              
              {provider.description && (
                <div className="mt-6">
                  <h3 className="font-semibold text-[#495E57] mb-2">About</h3>
                  <p className="text-gray-700 leading-relaxed">
                    {provider.description}
                  </p>
                </div>
              )}
            </div>
          </div>
        </div>
      </div>
      
      <div className="bg-white rounded-lg shadow-md overflow-hidden mb-8">
        <div className="bg-[#495E57] p-6 text-white relative">
          <div className="flex justify-between items-center">
            <h2 className="text-2xl font-bold">Services Offered</h2>
            <div className="text-sm">
              <span>Sort by: </span>
              <select 
                className="bg-transparent border-b border-white focus:outline-none text-white cursor-pointer ml-1"
                value={sortBy}
                onChange={(e) => setSortBy(e.target.value)}
              >
                <option value="recommended">Recommended</option>
                <option value="price_low">Price: Low to High</option>
                <option value="price_high">Price: High to Low</option>
                <option value="rating">Highest Rating</option>
              </select>
            </div>
          </div>
        </div>
        
        <div className="p-6">
          {servicesLoading ? (
            <div className="flex justify-center items-center py-8">
              <div className="animate-spin rounded-full h-10 w-10 border-t-2 border-b-2 border-[#495E57]"></div>
            </div>
          ) : providerServices.length > 0 ? (
            <div className="grid grid-cols-1 sm:grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
              {sortServices(providerServices).map(service => (
                <div
                  key={service.serviceId}
                  className="bg-white rounded-lg shadow-md hover:shadow-lg transition-all duration-300 cursor-pointer flex flex-col h-full relative overflow-hidden border border-gray-100"
                  onClick={(e) => handleOpenModal(service, e)}
                >
                  <div className="relative">
                    <img
                      src={service.serviceImage ? `${BASE_URL}${service.serviceImage}` : "/default-service.jpg"}
                      alt={service.serviceName}
                      className="w-full h-52 object-cover"
                      loading="lazy"
                      onError={(e) => {
                        e.target.onerror = null;
                        e.target.src = "/default-service.jpg";
                      }}
                    />
                    <div className="absolute top-0 left-0 bg-[#495E57] bg-opacity-75 text-white text-xs font-semibold px-2 py-1 rounded-br-md">
                      {service.categoryName || 'Uncategorized'}
                    </div>
                  </div>

                  <div className="flex flex-col justify-between p-4 h-full">
                    <div>
                      <h2 className="text-lg font-bold text-[#495E57] text-center">{service.serviceName}</h2>
                      <p className="text-sm text-gray-600 text-center mt-2 line-clamp-2">
                        {service.serviceDescription}
                      </p>
                    </div>
                    <div className="mt-4 border-t border-gray-100 pt-3">
                      <div className="flex items-center justify-between text-sm text-gray-600 mb-1">
                        <span>Duration:</span>
                        <span className="font-medium">{service.durationEstimate || "Not specified"}</span>
                      </div>
                      <div className="flex items-center justify-left mt-2">
                        {serviceRatings[service.serviceId]?.averageRating > 0 ? (
                          <>
                            {renderStars(serviceRatings[service.serviceId]?.averageRating || 0)}
                            <span className="ml-1 text-sm text-gray-600">
                              ({serviceRatings[service.serviceId]?.averageRating.toFixed(1)})
                            </span>
                            <span className="ml-1 text-xs text-gray-500">
                              {serviceRatings[service.serviceId]?.reviewCount} review/s
                            </span>
                          </>
                        ) : (
                          <span className="text-sm text-gray-400 italic">No reviews yet</span>
                        )}
                      </div>
                      <button 
                        className="mt-3 bg-[#495E57] hover:bg-[#3e4f49] text-white w-full py-2 rounded transition-colors flex items-center justify-center gap-2"
                        onClick={(e) => {
                          e.stopPropagation();
                          navigate('/bookService', { state: { service } });
                        }}
                      >
                        <i className="fas fa-calendar-check"></i> Book Now
                      </button>
                    </div>
                  </div>

                  <div className="absolute bottom-0 right-0 bg-[#F4CE14] text-[#495E57] font-bold px-3 py-1.5 rounded-lg shadow-sm">
                    ₱ {service.price}.00
                  </div>
                </div>
              ))}
            </div>
          ) : (
            <div className="text-center py-12 text-gray-500">
              <i className="fas fa-tools text-5xl mb-4 text-gray-300"></i>
              <h3 className="text-xl font-semibold mb-2">No Services Listed</h3>
              <p>This provider hasn't added any services yet.</p>
            </div>
          )}
        </div>
      </div>

      <ServiceDetailsModal
        isOpen={isModalOpen}
        onClose={handleCloseModal}
        service={selectedService}
        serviceRatings={serviceRatings}
        onBookService={handleBookService}
        renderStars={renderStars}
        clickPosition={clickPosition}
      />
    </div>
  );
};

export default ServiceProviderDetails;
