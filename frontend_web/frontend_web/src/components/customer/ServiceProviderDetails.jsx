import React, { useState, useEffect, useCallback } from "react";
import { useParams, useNavigate } from "react-router-dom";
import axios from "axios";
import ServiceDetailsModal from "../modals/ServiceDetailsModal";
import Footer from "../Footer";

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
    <div className="bg-gray-50 min-h-screen">
      {/* Hero section with provider details */}
      <div className="relative bg-[#495E57] text-white">
        {/* Background decorative elements */}
        <div className="absolute inset-0 overflow-hidden">
          <div className="absolute top-0 right-0 w-1/3 h-64 bg-[#F4CE14]/10 blur-3xl rounded-full"></div>
          <div className="absolute bottom-0 left-1/4 w-48 h-48 bg-[#F4CE14]/5 blur-xl rounded-full"></div>
        </div>
        
        <div className="container mx-auto px-4 py-8 relative z-10">
          <button
            onClick={() => navigate(-1)}
            className="mb-4 flex items-center gap-2 text-white hover:text-[#F4CE14] transition-colors"
          >
            <svg xmlns="http://www.w3.org/2000/svg" className="h-5 w-5" viewBox="0 0 20 20" fill="currentColor">
              <path fillRule="evenodd" d="M9.707 14.707a1 1 0 01-1.414 0l-4-4a1 1 0 010-1.414l4-4a1 1 0 011.414 1.414L7.414 9H15a1 1 0 110 2H7.414l2.293 2.293a1 1 0 010 1.414z" clipRule="evenodd" />
            </svg>
            Back
          </button>
          
          <div className="flex flex-col md:flex-row items-center gap-8 py-6">
            <div className="relative">
              <div className="w-40 h-40 md:w-48 md:h-48 rounded-full overflow-hidden border-4 border-white shadow-xl">
                <img
                  src={getImageUrl(profileImagePath)}
                  alt={`${provider.firstName} ${provider.lastName}`}
                  className="w-full h-full object-cover"
                  onError={handleImageError}
                />
                {imageFailed && (
                  <div className="absolute inset-0 flex items-center justify-center text-sm text-gray-500 bg-gray-100 rounded-full">
                    <svg xmlns="http://www.w3.org/2000/svg" className="h-12 w-12 text-gray-400" viewBox="0 0 20 20" fill="currentColor">
                      <path fillRule="evenodd" d="M10 9a3 3 0 100-6 3 3 0 000 6zm-7 9a7 7 0 1114 0H3z" clipRule="evenodd" />
                    </svg>
                  </div>
                )}
              </div>
              
              {provider.verified && (
                <div className="absolute bottom-0 right-0 bg-[#F4CE14] text-[#495E57] p-2 rounded-full shadow-md">
                  <svg xmlns="http://www.w3.org/2000/svg" className="h-6 w-6" viewBox="0 0 20 20" fill="currentColor">
                    <path fillRule="evenodd" d="M6.267 3.455a3.066 3.066 0 001.745-.723 3.066 3.066 0 013.976 0 3.066 3.066 0 001.745.723 3.066 3.066 0 012.812 2.812c.051.643.304 1.254.723 1.745a3.066 3.066 0 010 3.976 3.066 3.066 0 00-.723 1.745 3.066 3.066 0 01-2.812 2.812 3.066 3.066 0 00-1.745.723 3.066 3.066 0 01-3.976 0 3.066 3.066 0 00-1.745-.723 3.066 3.066 0 01-2.812-2.812 3.066 3.066 0 00-.723-1.745 3.066 3.066 0 010-3.976 3.066 3.066 0 00.723-1.745 3.066 3.066 0 012.812-2.812zm7.44 5.252a1 1 0 00-1.414-1.414L9 10.586 7.707 9.293a1 1 0 00-1.414 1.414l2 2a1 1 0 001.414 0l4-4z" clipRule="evenodd" />
                  </svg>
                </div>
              )}
            </div>
            
            <div className="text-center md:text-left md:flex-1">
              <h1 className="text-3xl md:text-4xl font-bold mb-2">{provider.firstName} {provider.lastName}</h1>
              
              {provider.businessName && (
                <p className="text-xl text-[#F4CE14] font-medium mb-4">
                  {provider.businessName}
                </p>
              )}
              
              <div className="flex flex-wrap gap-3 justify-center md:justify-start mt-3">
                <div className="bg-white/10 backdrop-blur-sm px-4 py-1 rounded-full flex items-center">
                  <svg xmlns="http://www.w3.org/2000/svg" className="h-4 w-4 mr-2" viewBox="0 0 20 20" fill="currentColor">
                    <path d="M9.049 2.927c.3-.921 1.603-.921 1.902 0l1.07 3.292a1 1 0 00.95.69h3.462c.969 0 1.371 1.24.588 1.81l-2.8 2.034a1 1 0 00-.364 1.118l1.07 3.292c.3.921-.755 1.688-1.54 1.118l-2.8-2.034a1 1 0 00-1.175 0l-2.8 2.034c-.784.57-1.838-.197-1.539-1.118l1.07-3.292a1 1 0 00-.364-1.118L2.98 8.72c-.783-.57-.38-1.81.588-1.81h3.461a1 1 0 00.951-.69l1.07-3.292z" />
                  </svg>
                  <span>{provider.averageRating?.toFixed(1) || "No ratings"}</span>
                </div>
                
                <div className="bg-white/10 backdrop-blur-sm px-4 py-1 rounded-full flex items-center">
                  <svg xmlns="http://www.w3.org/2000/svg" className="h-4 w-4 mr-2" viewBox="0 0 20 20" fill="currentColor">
                    <path fillRule="evenodd" d="M6 2a1 1 0 00-1 1v1H4a2 2 0 00-2 2v10a2 2 0 002 2h12a2 2 0 002-2V6a2 2 0 00-2-2h-1V3a1 1 0 10-2 0v1H7V3a1 1 0 00-1-1zm0 5a1 1 0 000 2h8a1 1 0 100-2H6z" clipRule="evenodd" />
                  </svg>
                  <span>{provider.yearsOfExperience ? `${provider.yearsOfExperience} years experience` : "Experience not specified"}</span>
                </div>
              </div>
            </div>
            
            <div className="flex-shrink-0 hidden md:block">
              <button
                onClick={() => navigate('/browseServices')}
                className="bg-[#F4CE14] text-[#495E57] px-6 py-3 rounded-full font-bold hover:bg-yellow-400 transition-colors shadow-lg flex items-center gap-2"
              >
                <svg xmlns="http://www.w3.org/2000/svg" className="h-5 w-5" viewBox="0 0 20 20" fill="currentColor">
                  <path fillRule="evenodd" d="M6 2a1 1 0 00-1 1v1H4a2 2 0 00-2 2v10a2 2 0 002 2h12a2 2 0 002-2V6a2 2 0 00-2-2h-1V3a1 1 0 10-2 0v1H7V3a1 1 0 00-1-1zm0 5a1 1 0 000 2h8a1 1 0 100-2H6z" clipRule="evenodd" />
                </svg>
                Browse More Services
              </button>
            </div>
          </div>
        </div>
        
        {/* Curved bottom edge */}
        <div className="absolute bottom-0 left-0 right-0 h-16 bg-gray-50 rounded-t-[50%] transform translate-y-8"></div>
      </div>
      
      <div className="container mx-auto px-4 mt-12 relative z-10">
        {/* Quick info cards */}
        <div className="grid grid-cols-1 md:grid-cols-3 gap-6 -mt-20 mb-12">
          <div className="bg-white rounded-xl shadow-md p-6 hover:shadow-lg transition-shadow">
            <div className="flex items-start">
              <div className="p-3 bg-[#F4CE14]/20 rounded-full mr-4">
                <svg xmlns="http://www.w3.org/2000/svg" className="h-6 w-6 text-[#495E57]" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M3 8l7.89 5.26a2 2 0 002.22 0L21 8M5 19h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v10a2 2 0 002 2z" />
                </svg>
              </div>
              <div>
                <h3 className="font-semibold text-gray-700 mb-1">Email</h3>
                <p className="text-gray-600">{provider.email || "Not available"}</p>
              </div>
            </div>
          </div>
          
          <div className="bg-white rounded-xl shadow-md p-6 hover:shadow-lg transition-shadow">
            <div className="flex items-start">
              <div className="p-3 bg-[#F4CE14]/20 rounded-full mr-4">
                <svg xmlns="http://www.w3.org/2000/svg" className="h-6 w-6 text-[#495E57]" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M3 5a2 2 0 012-2h3.28a1 1 0 01.948.684l1.498 4.493a1 1 0 01-.502 1.21l-2.257 1.13a11.042 11.042 0 005.516 5.516l1.13-2.257a1 1 0 011.21-.502l4.493 1.498a1 1 0 01.684.949V19a2 2 0 01-2 2h-1C9.716 21 3 14.284 3 6V5z" />
                </svg>
              </div>
              <div>
                <h3 className="font-semibold text-gray-700 mb-1">Phone</h3>
                <p className="text-gray-600">{provider.phoneNumber || "Not available"}</p>
              </div>
            </div>
          </div>
          
          <div className="bg-white rounded-xl shadow-md p-6 hover:shadow-lg transition-shadow">
            <div className="flex items-start">
              <div className="p-3 bg-[#F4CE14]/20 rounded-full mr-4">
                <svg xmlns="http://www.w3.org/2000/svg" className="h-6 w-6 text-[#495E57]" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z" />
                </svg>
              </div>
              <div>
                <h3 className="font-semibold text-gray-700 mb-1">Availability</h3>
                <p className="text-gray-600">{provider.availabilitySchedule || "Contact for details"}</p>
              </div>
            </div>
          </div>
        </div>
        
        {/* About section if description exists */}
        {provider.description && (
          <div className="bg-white rounded-xl shadow-md p-6 mb-10">
            <h2 className="text-2xl font-bold text-[#495E57] mb-4 flex items-center">
              <svg xmlns="http://www.w3.org/2000/svg" className="h-6 w-6 mr-2" viewBox="0 0 20 20" fill="currentColor">
                <path fillRule="evenodd" d="M18 10a8 8 0 11-16 0 8 8 0 0116 0zm-7-4a1 1 0 11-2 0 1 1 0 012 0zM9 9a1 1 0 000 2v3a1 1 0 001 1h1a1 1 0 100-2v-3a1 1 0 00-1-1H9z" clipRule="evenodd" />
              </svg>
              About
            </h2>
            <p className="text-gray-700 leading-relaxed">
              {provider.description}
            </p>
          </div>
        )}
        
        {/* Services Section */}
        <div className="mb-10">
          <div className="flex justify-between items-center mb-6">
            <h2 className="text-2xl font-bold text-[#495E57] flex items-center">
              <svg xmlns="http://www.w3.org/2000/svg" className="h-6 w-6 mr-2" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M21 13.255A23.931 23.931 0 0112 15c-3.183 0-6.22-.62-9-1.745M16 6V4a2 2 0 00-2-2h-4a2 2 0 00-2 2v2m4 6h.01M5 20h14a2 2 0 002-2V8a2 2 0 00-2-2H5a2 2 0 00-2 2v10a2 2 0 002 2z" />
              </svg>
              Services Offered
            </h2>
            
            <div className="flex items-center bg-white rounded-full shadow-sm p-1 pr-3">
              <span className="text-sm text-gray-600 mr-2">Sort by:</span>
              <select 
                className="bg-transparent border-none focus:outline-none text-[#495E57] text-sm font-medium cursor-pointer"
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
          
          {servicesLoading ? (
            <div className="flex justify-center items-center h-64 bg-white rounded-xl shadow-md">
              <div className="flex flex-col items-center">
                <div className="animate-spin rounded-full h-12 w-12 border-t-2 border-b-2 border-[#F4CE14]"></div>
                <p className="mt-4 text-gray-500">Loading services...</p>
              </div>
            </div>
          ) : providerServices.length > 0 ? (
            <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-4">
              {sortServices(providerServices).map(service => (
                <div
                  key={service.serviceId}
                  onClick={(e) => handleOpenModal(service, e)}
                  className="bg-white rounded-xl shadow-md hover:shadow-xl transition-all duration-300 cursor-pointer flex flex-col h-full relative overflow-hidden border border-gray-100 transform hover:-translate-y-1"
                >
                  <div className="relative">
                    <div className="absolute inset-0 bg-gradient-to-t from-black/60 to-transparent z-10"></div>
                    <img
                      src={service.serviceImage ? `${BASE_URL}${service.serviceImage}` : "/default-service.jpg"}
                      alt={service.serviceName}
                      className="w-full h-48 object-cover"
                      loading="lazy"
                      onError={(e) => {
                        e.target.onerror = null;
                        e.target.src = "/default-service.jpg";
                      }}
                    />
                    <div className="absolute top-3 left-3 bg-[#495E57]/80 text-white text-xs font-semibold px-3 py-1.5 rounded-full backdrop-blur-sm z-20 shadow-sm">
                      {service.categoryName || 'Uncategorized'}
                    </div>
                    <div className="absolute bottom-0 left-0 right-0 p-4 z-10">
                      <h3 className="text-xl font-bold text-white drop-shadow-md">
                        {service.serviceName}
                      </h3>
                    </div>
                  </div>

                  <div className="flex flex-col justify-between p-4 flex-grow">
                    <p className="text-sm text-gray-600 line-clamp-2 mb-4">
                      {service.serviceDescription}
                    </p>
                    
                    <div className="mt-auto space-y-3">
                      <div className="flex items-center justify-between">
                        <div className="flex items-center">
                          <svg xmlns="http://www.w3.org/2000/svg" className="h-5 w-5 text-[#495E57]" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={1.5} d="M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z" />
                          </svg>
                          <span className="ml-2 text-sm text-gray-600">{service.durationEstimate || "Not specified"}</span>
                        </div>
                        <div className="bg-[#F4CE14]/10 text-[#495E57] font-bold px-3 py-1 rounded-full text-sm">
                          ₱{service.price}
                        </div>
                      </div>
                      
                      <div>
                        <div className="flex items-center mb-1">
                          {serviceRatings[service.serviceId]?.averageRating > 0 ? (
                            <>
                              <div className="flex">
                                {renderStars(serviceRatings[service.serviceId]?.averageRating || 0)}
                              </div>
                              <span className="ml-1 text-sm text-gray-600">
                                {serviceRatings[service.serviceId]?.averageRating.toFixed(1)}
                              </span>
                              <span className="ml-1 text-xs text-gray-500">
                                ({serviceRatings[service.serviceId]?.reviewCount})
                              </span>
                            </>
                          ) : (
                            <span className="text-sm text-gray-400 italic">No reviews yet</span>
                          )}
                        </div>
                        <button 
                          className="w-full bg-[#495E57] hover:bg-[#3e4f49] text-white py-2 rounded-lg transition-colors flex items-center justify-center gap-2"
                          onClick={(e) => {
                            e.stopPropagation();
                            navigate('/bookService', { state: { service } });
                          }}
                        >
                          <svg xmlns="http://www.w3.org/2000/svg" className="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v10a2 2 0 002 2z" />
                          </svg>
                          Book Now
                        </button>
                      </div>
                    </div>
                  </div>
                </div>
              ))}
            </div>
          ) : (
            <div className="bg-white rounded-xl shadow-md p-12 text-center">
              <div className="w-20 h-20 bg-gray-100 rounded-full flex items-center justify-center mx-auto mb-6">
                <svg xmlns="http://www.w3.org/2000/svg" className="h-10 w-10 text-gray-400" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={1} d="M9.75 17L9 20l-1 1h8l-1-1-.75-3M3 13h18M5 17h14a2 2 0 002-2V5a2 2 0 00-2-2H5a2 2 0 00-2 2v10a2 2 0 002 2z" />
                </svg>
              </div>
              <h3 className="text-xl font-semibold text-gray-800 mb-2">No Services Listed</h3>
              <p className="text-gray-600 mb-6">This provider hasn't added any services yet.</p>
              <button 
                onClick={() => navigate('/browseServices')}
                className="px-6 py-2 bg-[#F4CE14] text-[#495E57] font-medium rounded-full hover:bg-[#e5c119] transition-colors duration-300"
              >
                Browse All Services
              </button>
            </div>
          )}
        </div>
      </div>

      <Footer/>

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
