import React, { useState, useEffect, useMemo, useCallback } from "react";
import { useNavigate } from "react-router-dom";
import axios from "axios";
import ServiceFilters from "./filters/ServiceFilters";

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
  const navigate = useNavigate();

  useEffect(() => {
    const fetchServices = async () => {
      try {
        const token = localStorage.getItem("authToken") || sessionStorage.getItem("authToken");
        if (!token) {
          console.error("No authentication token found");
          setIsLoading(false);
          return;
        }

        const servicesResponse = await axios.get("/api/services/getAll", {
          headers: {
            Authorization: `Bearer ${token}`,
          },
        });

        const services = servicesResponse.data;

        console.log("Services:", services);

        const servicesWithCategories = services.map((service) => ({
          ...service,
          categoryName: service.category?.categoryName || "Uncategorized",
        }));

        setServices(servicesWithCategories);
        setIsLoading(false);
      } catch (error) {
        console.error("Error fetching services:", error);
        setIsLoading(false);
      }
    };

    fetchServices();
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
      
      const rating = service.provider?.averageRating || 0;
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
        result.sort((a, b) => (b.provider?.averageRating || 0) - (a.provider?.averageRating || 0));
        break;
      case 'experience':
        result.sort((a, b) => (b.provider?.yearsOfExperience || 0) - (a.provider?.yearsOfExperience || 0));
        break;
      case 'recommended':
      default:
        result.sort((a, b) => {
          const scoreA = (a.provider?.averageRating || 0) * 2 + (a.provider?.verified ? 3 : 0);
          const scoreB = (b.provider?.averageRating || 0) * 2 + (b.provider?.verified ? 3 : 0);
          return scoreB - scoreA;
        });
    }
    
    return result;
  }, []);
  
  useEffect(() => {
    setFilteredServices(applyFilters(services, activeFilters));
  }, [services, activeFilters, applyFilters]);
  
  const handleFilterChange = useCallback((newFilters) => {
    setActiveFilters(newFilters);
  }, []);

  const handleOpenModal = useCallback((service) => {
    console.log("Selected Service:", service);
    setSelectedService(service);
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
        onClick={() => handleOpenModal(service)}
        className="bg-white rounded-lg shadow-lg hover:shadow-xl transition-transform transform hover:scale-105 cursor-pointer flex flex-col h-full relative"
      >
        <img
          src={service.image || "/default-service.jpg"}
          alt={service.serviceName}
          className="w-full h-48 object-cover rounded-t-lg"
          loading="lazy"
        />

        <div className="flex flex-col justify-between p-4 h-full">
          <div>
            <h2 className="text-lg font-bold text-gray-800 text-center">{service.serviceName}</h2>
            <p className="text-sm text-gray-600 text-center mt-2 line-clamp-3">
              {service.serviceDescription}
            </p>
          </div>
          <div className="mt-4">
            <p className="text-sm font-semibold text-gray-800 text-center mt-2">
              Category: {service.categoryName}
            </p>
            <p className="text-sm text-gray-600 text-center">
              Duration: {service.durationEstimate || "Not specified"}
            </p>
            <p className="text-sm italic text-gray-600 text-center mt-2">
              Provider: {service.provider?.firstName && service.provider?.lastName
                ? `${service.provider?.firstName} ${service.provider?.lastName}`
                : "Unknown"}
            </p>
          </div>
        </div>

        <div className="absolute bottom-0 right-0 bg-yellow-400 text-black text-sm font-semibold px-4 py-2 rounded-tl-lg">
          {"PHP " + service.price}
        </div>
      </div>
    ));
  }, [filteredServices, handleOpenModal]);

  return (
    <div className="p-6 md:p-10">
      <h1 className="text-3xl font-bold text-gray-800 text-center mb-8">Browse Services</h1>
      
      <div className="flex flex-col md:flex-row gap-6">
        <ServiceFilters 
          services={services}
          onFilterChange={handleFilterChange}
          className="w-full md:w-64 flex-shrink-0"
        />
        
        <div className="flex-1">
          {isLoading ? (
            <p className="text-center text-gray-600">Loading services...</p>
          ) : filteredServices.length === 0 ? (
            <div className="bg-white rounded-lg shadow p-6 text-center">
              <p className="text-gray-600 mb-2">No services match your filters.</p>
              <button 
                onClick={() => setActiveFilters({
                  categories: [],
                  priceRange: [0, 10000],
                  rating: 0,
                  verifiedOnly: false,
                  sortBy: 'recommended',
                  experience: 0
                })}
                className="text-blue-500 hover:underline"
              >
                Clear filters
              </button>
            </div>
          ) : (
            <>
              <p className="mb-4 text-gray-600">{filteredServices.length} services found</p>
              <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-6">
                {serviceCards}
              </div>
            </>
          )}
        </div>
      </div>

      {isModalOpen && selectedService && (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
          <div className="bg-white rounded-lg shadow-lg w-full max-w-3xl p-6">
            <h2 className="text-2xl font-bold text-gray-800 text-center mb-6">Service Details</h2>

            {/* Service Provider Details */}
            <div className="flex flex-col md:flex-row items-center md:items-start gap-6 mb-6">
              <div className="flex items-center gap-4">
                <img
                  src={selectedService.provider?.profilePicture || "/default-profile.jpg"}
                  alt="Provider Profile"
                  className="w-24 h-24 rounded-full"
                />
                <div className="flex flex-col">
                  <div className="mb-4">
                    <h3 className="text-lg font-bold text-gray-800 flex items-center gap-2">
                      <i className="fas fa-user text-blue-500"></i>
                      {selectedService.provider?.firstName || "Unknown"} {selectedService.provider?.lastName || ""}
                    </h3>
                    <p className="text-sm text-gray-600 flex items-center gap-2">
                      <i className="fas fa-briefcase text-green-500"></i>
                      {selectedService.provider?.businessName || "No Business Name"}
                    </p>
                  </div>
                  
                  <div className="flex items-center gap-2 mb-2">
                    <i className="fas fa-thumbs-up text-blue-400"></i>
                    {renderStars(selectedService.provider?.averageRating || 0)}
                  </div>
                  <p className="text-sm text-gray-600 flex items-center gap-2">
                    <i className="fas fa-calendar-alt text-purple-500"></i>
                    Years of Experience: {selectedService.provider?.yearsOfExperience || "Not specified"}
                  </p>
                </div>
              </div>

              <div className="flex-1 text-right">
                <p className="text-sm text-gray-600 flex items-center justify-end gap-2">
                  <i className="fas fa-check-circle text-green-500"></i>
                  <strong>Verification:</strong> {selectedService.provider?.verified ? "Verified" : "Not Verified"}
                </p>
                <p className="text-sm text-gray-600 mt-2 flex items-center justify-end gap-2">
                  <i className="fas fa-phone text-blue-500"></i>
                  <strong>Contact:</strong> {selectedService.provider?.phoneNumber || "Not available"}
                </p>
                <p className="text-sm text-gray-600 mt-2 flex items-center justify-end gap-2">
                  <i className="fas fa-clock text-orange-500"></i>
                  <strong>Availability:</strong> {selectedService.provider?.availabilitySchedule || "Not specified"}
                </p>
              </div>
            </div>

            {/* Divider */}
            <hr className="border-t border-gray-300 my-6" />

            {/* Service Details */}
            <div className="flex flex-col md:flex-row items-center md:items-start gap-6">
              <div className="flex items-center gap-4">
                <img
                  src={selectedService.image || "/default-service.jpg"}
                  alt="Service"
                  className="w-32 h-32 object-cover rounded-lg"
                />
                <div className="flex flex-col justify-center">
                  <h3 className="text-lg font-bold text-gray-800 flex items-center gap-2">
                    <i className="fas fa-concierge-bell text-blue-500"></i>
                    {selectedService.serviceName}
                  </h3>
                  <p className="text-sm text-gray-600 mt-2 flex items-center gap-2">
                    <i className="fas fa-info-circle text-gray-500"></i>
                    {selectedService.serviceDescription}
                  </p>
                </div>
              </div>

              <div className="flex-1 text-right">
                <p className="text-sm text-gray-600 flex items-center justify-end gap-2">
                  <i className="fas fa-tags text-purple-500"></i>
                  <strong>Category:</strong> {selectedService.categoryName}
                </p>
                <p className="text-sm text-gray-600 mt-2 flex items-center justify-end gap-2">
                  <i className="fas fa-clock text-orange-500"></i>
                  <strong>Duration:</strong> {selectedService.durationEstimate || "Not specified"}
                </p>
                <div className="mt-2 flex items-center justify-end gap-2">
                  <p className="text-sm font-bold bg-yellow-400 text-black px-4 py-2 rounded-lg">
                    {"PHP " + selectedService.price}
                  </p>
                </div>
              </div>
            </div>

            <div className="flex justify-end gap-4 mt-6">
              <button
                onClick={handleBookService}
                className="px-4 py-2 bg-green-500 text-white rounded hover:bg-green-600"
              >
                Book Service
              </button>
              <button
                onClick={handleCloseModal}
                className="px-4 py-2 bg-gray-300 text-gray-800 rounded hover:bg-gray-400"
              >
                Close
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

export default React.memo(BrowseServicesPage);