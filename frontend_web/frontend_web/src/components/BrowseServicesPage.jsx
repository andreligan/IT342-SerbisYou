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
        className="bg-white rounded-lg shadow-md hover:shadow-lg transition-all duration-300 cursor-pointer flex flex-col h-full relative overflow-hidden border border-gray-100"
      >
        <div className="relative">
          <img
            src={service.serviceImage ? `${BASE_URL}${service.serviceImage}` : "/default-service.jpg"}
            alt={service.serviceName}
            className="w-full h-52 object-cover"
            loading="lazy"
          />
          <div className="absolute top-0 left-0 bg-[#495E57] bg-opacity-75 text-white text-xs font-semibold px-2 py-1 rounded-br-md">
            {service.categoryName}
          </div>
          {service.provider?.verified && (
            <div className="absolute top-0 right-0 bg-[#F4CE14] text-[#495E57] text-xs font-bold px-2 py-1 rounded-bl-md flex items-center gap-1">
              <i className="fas fa-check-circle"></i> Verified
            </div>
          )}
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
            <div className="flex items-center justify-between text-sm">
              <span className="text-gray-600">Provider:</span>
              <span className="font-medium text-[#495E57]">
                {service.provider?.firstName && service.provider?.lastName
                  ? `${service.provider?.firstName} ${service.provider?.lastName}`
                  : "Unknown"}
              </span>
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
          </div>
        </div>

        <div className="absolute bottom-0 right-0 bg-[#F4CE14] text-[#495E57] font-bold px-3 py-1.5 rounded-lg shadow-sm">
          ₱ {service.price}.00
        </div>
      </div>
    ));
  }, [filteredServices, handleOpenModal, renderStars, serviceRatings]);

  return (
    <div className="bg-gray-50 min-h-screen">
      <div className="bg-[#495E57] text-white py-8">
        <h1 className="text-3xl font-bold text-center">Browse Services</h1>
        <p className="text-center text-gray-200 mt-2">Find the perfect service for your needs</p>
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
                <div className="animate-spin rounded-full h-12 w-12 border-t-2 border-b-2 border-[#495E57]"></div>
              </div>
            ) : filteredServices.length === 0 ? (
              <div className="bg-white rounded-lg shadow-md p-8 text-center">
                <div className="text-5xl text-gray-300 mb-4">
                  <i className="fas fa-search"></i>
                </div>
                <p className="text-gray-600 mb-4">No services match your filters.</p>
                <button 
                  onClick={() => setActiveFilters({
                    categories: [],
                    priceRange: [0, 10000],
                    rating: 0,
                    verifiedOnly: false,
                    sortBy: 'recommended',
                    experience: 0
                  })}
                  className="text-[#495E57] hover:text-[#F4CE14] font-medium transition-colors"
                >
                  Clear all filters
                </button>
              </div>
            ) : (
              <>
                <div className="flex justify-between items-center mb-6">
                  <p className="text-gray-600">
                    <span className="font-semibold text-[#495E57]">{filteredServices.length}</span> services found
                  </p>
                  <div className="text-sm text-gray-500">
                    <span>Sort by: </span>
                    <select 
                      className="bg-transparent border-b border-gray-300 focus:outline-none focus:border-[#495E57] cursor-pointer ml-1"
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
                <div className="grid grid-cols-1 sm:grid-cols-1 md:grid-cols-2 xl:grid-cols-3 gap-6">
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