import React, { useState, useEffect, useMemo, useCallback } from "react";
import { useNavigate } from "react-router-dom";
import axios from "axios";
import ServiceFilters from "./filters/ServiceFilters";
import ServiceDetailsModal from "./modals/ServiceDetailsModal";

const BASE_URL = "http://localhost:8080"; // Define the base URL for the backend

const BrowseServicesPage = () => {
  const [services, setServices] = useState([]);
  const [filteredServices, setFilteredServices] = useState([]);
  const [selectedService, setSelectedService] = useState(null);
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [isLoading, setIsLoading] = useState(true);
  const [activeFilters, setActiveFilters] = useState({
    categories: [],
    priceRange: [0, 10000],
    rating: 0,
    verifiedOnly: false,
    sortBy: 'recommended',
    experience: 0
  });
  const [serviceRatings, setServiceRatings] = useState({});
  const [clickPosition, setClickPosition] = useState(null);
  const navigate = useNavigate();

  useEffect(() => {
    const fetchServicesAndRatings = async () => {
      try {
        const token = localStorage.getItem("authToken") || sessionStorage.getItem("authToken");
        if (!token) {
          console.error("No authentication token found");
          setIsLoading(false);
          return;
        }

        const servicesResponse = await axios.get(`${BASE_URL}/api/services/getAll`, {
          headers: {
            Authorization: `Bearer ${token}`,
          },
        });

        const services = servicesResponse.data;

        const ratingsMap = {};
        const servicesWithImages = await Promise.all(
          services.map(async (service) => {
            try {
              // Fetch the service image path
              const imageResponse = await axios.get(`${BASE_URL}/api/services/getServiceImage/${service.serviceId}`, {
                headers: {
                  Authorization: `Bearer ${token}`,
                },
              });
              service.serviceImage = imageResponse.data; // Add the image path to the service object
              
              // Fetch provider profile image if provider exists
              if (service.provider && service.provider.providerId) {
                try {
                  const providerImageResponse = await axios.get(
                    `${BASE_URL}/api/service-providers/getServiceProviderImage/${service.provider.providerId}`,
                    {
                      headers: {
                        Authorization: `Bearer ${token}`,
                      },
                    }
                  );
                  service.provider.profileImage = providerImageResponse.data;
                  console.log(`Provider ${service.provider.providerId} image path: ${service.provider.profileImage}`);
                } catch (error) {
                  console.warn(`Could not fetch provider image for provider ${service.provider.providerId}:`, error);
                  service.provider.profileImage = null;
                }
              }
            } catch (error) {
              console.error(`Error fetching image for service ${service.serviceId}:`, error);
              service.serviceImage = null; // Default to null if the image fetch fails
            }

            try {
              // Fetch the service rating
              const ratingResponse = await axios.get(`${BASE_URL}/api/reviews/getServiceRating/${service.serviceId}`, {
                headers: {
                  Authorization: `Bearer ${token}`,
                },
              });
              ratingsMap[service.serviceId] = ratingResponse.data;
            } catch (error) {
              console.error(`Error fetching rating for service ${service.serviceId}:`, error);
              ratingsMap[service.serviceId] = { averageRating: 0, reviewCount: 0 };
            }

            return service;
          })
        );

        const servicesWithCategories = servicesWithImages.map((service) => ({
          ...service,
          categoryName: service.category?.categoryName || "Uncategorized",
        }));

        setServices(servicesWithCategories);
        setServiceRatings(ratingsMap);
        setIsLoading(false);
      } catch (error) {
        console.error("Error fetching services:", error);
        setIsLoading(false);
      }
    };

    fetchServicesAndRatings();
  }, []);

  const applyFilters = useCallback((services, filters) => {
    if (!services) return [];
    
    let result = services.filter(service => {
      if (filters.categories.length > 0 && !filters.categories.includes(service.categoryName)) {
        return false;
      }
      
      const price = parseFloat(service.price) || 0;
      if (price < filters.priceRange[0] || price > filters.priceRange[1]) {
        return false;
      }
      
      const rating = serviceRatings[service.serviceId]?.averageRating || 0;
      if (rating < filters.rating) {
        return false;
      }
      
      if (filters.verifiedOnly && !service.provider?.verified) {
        return false;
      }
      
      const experience = service.provider?.yearsOfExperience || 0;
      if (experience < filters.experience) {
        return false;
      }
      
      return true;
    });
    
    switch(filters.sortBy) {
      case 'price_low':
        result.sort((a, b) => (parseFloat(a.price) || 0) - (parseFloat(b.price) || 0));
        break;
      case 'price_high':
        result.sort((a, b) => (parseFloat(b.price) || 0) - (parseFloat(a.price) || 0));
        break;
      case 'rating':
        result.sort((a, b) => (serviceRatings[b.serviceId]?.averageRating || 0) - 
                             (serviceRatings[a.serviceId]?.averageRating || 0));
        break;
      case 'experience':
        result.sort((a, b) => (b.provider?.yearsOfExperience || 0) - (a.provider?.yearsOfExperience || 0));
        break;
      case 'recommended':
      default:
        result.sort((a, b) => {
          const scoreA = (serviceRatings[a.serviceId]?.averageRating || 0) * 2 + 
                       (a.provider?.verified ? 3 : 0);
          const scoreB = (serviceRatings[b.serviceId]?.averageRating || 0) * 2 + 
                       (b.provider?.verified ? 3 : 0);
          return scoreB - scoreA;
        });
    }
    
    return result;
  }, [serviceRatings]);
  
  useEffect(() => {
    setFilteredServices(applyFilters(services, activeFilters));
  }, [services, activeFilters, applyFilters]);
  
  const handleFilterChange = useCallback((newFilters) => {
    setActiveFilters(newFilters);
  }, []);

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

  const serviceCards = useMemo(() => {
    return filteredServices.map((service) => (
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
            className="w-full h-56 object-cover"
            loading="lazy"
          />
          <div className="absolute top-3 left-3 bg-[#495E57]/80 text-white text-xs font-semibold px-3 py-1.5 rounded-full backdrop-blur-sm z-20 shadow-sm">
            {service.categoryName}
          </div>
          {service.provider?.verified && (
            <div className="absolute top-3 right-3 bg-[#F4CE14]/90 text-[#495E57] text-xs font-bold px-3 py-1.5 rounded-full flex items-center gap-1.5 backdrop-blur-sm z-20 shadow-sm">
              <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="currentColor" className="w-4 h-4">
                <path fillRule="evenodd" d="M8.603 3.799A4.49 4.49 0 0112 2.25c1.357 0 2.573.6 3.397 1.549a4.49 4.49 0 013.498 1.307 4.491 4.491 0 011.307 3.497A4.49 4.49 0 0121.75 12a4.49 4.49 0 01-1.549 3.397 4.491 4.491 0 01-1.307 3.497 4.491 4.491 0 01-3.497 1.307A4.49 4.49 0 0112 21.75a4.49 4.49 0 01-3.397-1.549 4.49 4.49 0 01-3.498-1.306 4.491 4.491 0 01-1.307-3.498A4.49 4.49 0 012.25 12c0-1.357.6-2.573 1.549-3.397a4.49 4.49 0 011.307-3.497 4.49 4.49 0 013.497-1.307zm7.007 6.387a.75.75 0 10-1.22-.872l-3.236 4.53L9.53 12.22a.75.75 0 00-1.06 1.06l2.25 2.25a.75.75 0 001.14-.094l3.75-5.25z" clipRule="evenodd" />
              </svg>
              Verified
            </div>
          )}
          <div className="absolute bottom-0 left-0 right-0 p-4 z-10">
            <h2 className="text-xl font-bold text-white drop-shadow-md">
              {service.serviceName}
            </h2>
            <div className="flex items-center mt-1.5">
              {serviceRatings[service.serviceId]?.averageRating > 0 ? (
                <>
                  <div className="flex items-center gap-0.5 text-[#F4CE14] drop-shadow-sm">
                    {renderStars(serviceRatings[service.serviceId]?.averageRating || 0)}
                  </div>
                  <span className="ml-2 text-sm text-white font-medium">
                    {serviceRatings[service.serviceId]?.averageRating.toFixed(1)}
                  </span>
                  <span className="ml-1 text-xs text-gray-200">
                    ({serviceRatings[service.serviceId]?.reviewCount})
                  </span>
                </>
              ) : (
                <span className="text-sm text-gray-200 italic">No reviews yet</span>
              )}
            </div>
          </div>
        </div>

        <div className="flex flex-col justify-between p-5 h-full">
          <p className="text-sm text-gray-600 line-clamp-2 mb-4">
            {service.serviceDescription}
          </p>
          
          <div className="mt-auto space-y-3">
            <div className="flex items-center justify-between text-sm">
              <div className="flex items-center gap-2 text-gray-600">
                <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" strokeWidth={1.5} stroke="currentColor" className="w-5 h-5">
                  <path strokeLinecap="round" strokeLinejoin="round" d="M12 6v6h4.5m4.5 0a9 9 0 11-18 0 9 9 0 0118 0z" />
                </svg>
                <span>{service.durationEstimate || "Not specified"}</span>
              </div>
              <div className="px-3 py-1 rounded-full bg-[#F4CE14]/10 text-[#495E57] font-bold text-lg">
                ₱{service.price}
              </div>
            </div>
            
            <div className="flex items-center border-t border-gray-100 pt-3">
              {service.provider?.profileImage ? (
                <img 
                  src={`${BASE_URL}${service.provider.profileImage}`} 
                  alt="Provider"
                  className="w-8 h-8 rounded-full object-cover border border-gray-200"
                />
              ) : (
                <div className="w-8 h-8 rounded-full bg-[#495E57]/10 flex items-center justify-center text-[#495E57]">
                  <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" strokeWidth={1.5} stroke="currentColor" className="w-4 h-4">
                    <path strokeLinecap="round" strokeLinejoin="round" d="M15.75 6a3.75 3.75 0 11-7.5 0 3.75 3.75 0 017.5 0zM4.501 20.118a7.5 7.5 0 0114.998 0A17.933 17.933 0 0112 21.75c-2.676 0-5.216-.584-7.499-1.632z" />
                  </svg>
                </div>
              )}
              <span className="ml-2 text-sm font-medium text-[#495E57]">
                {service.provider?.firstName && service.provider?.lastName
                  ? `${service.provider?.firstName} ${service.provider?.lastName}`
                  : "Unknown"}
              </span>
              
              {service.provider?.yearsOfExperience > 0 && (
                <span className="ml-auto text-xs text-gray-500">
                  {service.provider.yearsOfExperience} {service.provider.yearsOfExperience === 1 ? 'year' : 'years'} exp.
                </span>
              )}
            </div>
          </div>
        </div>
      </div>
    ));
  }, [filteredServices, handleOpenModal, renderStars, serviceRatings]);

  return (
    <div className="bg-gray-50 min-h-screen">
      <div className="bg-gradient-to-r from-[#495E57] to-[#3A4A45] text-white py-12">
        <div className="container mx-auto px-4">
          <h1 className="text-4xl font-bold text-center mb-2">Browse Services</h1>
          <p className="text-center text-gray-200 max-w-xl mx-auto">Find skilled professionals and quality services to meet all your household and personal needs</p>
        </div>
      </div>
      
      <div className="container mx-auto px-4 py-8">
        <div className="flex flex-col md:flex-row">
          <div className="md:w-72 lg:w-80 flex-shrink-0 md:pr-6 md:mr-4 md:border-r md:border-gray-200 md:sticky md:top-4 md:self-start" 
               style={{ maxHeight: 'calc(100vh - 2rem)', overflowY: 'auto' }}>
            <ServiceFilters 
              services={services}
              onFilterChange={handleFilterChange}
              className="w-full mb-6 md:mb-0"
            />
          </div>
          
          <div className="flex-1 md:pl-2">
            {isLoading ? (
              <div className="flex justify-center items-center h-64">
                <div className="flex flex-col items-center">
                  <div className="animate-spin rounded-full h-12 w-12 border-t-2 border-b-2 border-[#F4CE14]"></div>
                  <p className="mt-4 text-gray-500">Loading services...</p>
                </div>
              </div>
            ) : filteredServices.length === 0 ? (
              <div className="bg-white rounded-xl shadow-md p-12 text-center">
                <div className="w-20 h-20 bg-gray-100 rounded-full flex items-center justify-center mx-auto mb-6">
                  <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" strokeWidth={1} stroke="currentColor" className="w-10 h-10 text-gray-400">
                    <path strokeLinecap="round" strokeLinejoin="round" d="M15.75 15.75l-2.489-2.489m0 0a3.375 3.375 0 10-4.773-4.773 3.375 3.375 0 004.774 4.774zM21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
                  </svg>
                </div>
                <h3 className="text-xl font-semibold text-gray-800 mb-2">No services found</h3>
                <p className="text-gray-600 mb-6">We couldn't find any services matching your current filters.</p>
                <button 
                  onClick={() => setActiveFilters({
                    categories: [],
                    priceRange: [0, 10000],
                    rating: 0,
                    verifiedOnly: false,
                    sortBy: 'recommended',
                    experience: 0
                  })}
                  className="px-6 py-2 bg-[#F4CE14] text-[#495E57] font-medium rounded-full hover:bg-[#e5c119] transition-colors duration-300"
                >
                  Reset all filters
                </button>
              </div>
            ) : (
              <>
                <div className="flex justify-between items-center mb-8 bg-white p-3 px-5 rounded-full shadow-sm">
                  <p className="text-gray-600">
                    <span className="font-semibold text-[#495E57]">{filteredServices.length}</span> services found
                  </p>
                  <div className="flex items-center">
                    <label className="text-sm text-gray-500 mr-2">Sort by:</label>
                    <select 
                      className="bg-gray-50 border border-gray-200 rounded-full px-4 py-1.5 text-sm focus:outline-none focus:ring-2 focus:ring-[#F4CE14]/50 cursor-pointer"
                      value={activeFilters.sortBy}
                      onChange={(e) => setActiveFilters({...activeFilters, sortBy: e.target.value})}
                    >
                      <option value="recommended">Recommended</option>
                      <option value="price_low">Price: Low to High</option>
                      <option value="price_high">Price: High to Low</option>
                      <option value="rating">Highest Rating</option>
                      <option value="experience">Most Experienced</option>
                    </select>
                  </div>
                </div>
                
                <div className="grid grid-cols-1 md:grid-cols-2 xl:grid-cols-3 gap-6">
                  {serviceCards}
                </div>
              </>
            )}
          </div>
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

export default React.memo(BrowseServicesPage);