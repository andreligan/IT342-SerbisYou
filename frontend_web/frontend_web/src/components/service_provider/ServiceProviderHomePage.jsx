import React, { useState, useEffect } from "react";
import { useNavigate, Link } from "react-router-dom";
import Footer from "../Footer";
import axios from "axios";
import serviceImage1 from "../../assets/appliance repair.jpg";
import serviceImage2 from "../../assets/carpentry.jpg";
import serviceImage3 from "../../assets/cleaning.jpg";
import electrical from "../../assets/electrical.jpg";
import plumbing from "../../assets/plumbing.jpg";
import pestControl from "../../assets/pest control.jpg";
import homePainting from "../../assets/home painting.jpg";
import lawnCare from "../../assets/lawn care.jpg";
import { motion } from "framer-motion";

function ServiceProviderHomePage() {
  const navigate = useNavigate();
  const [services, setServices] = useState([]);
  const [categoryGroups, setCategoryGroups] = useState({});
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [providerName, setProviderName] = useState("Service Provider");
  
  // Image mapping for categories
  const categoryImageMap = {
    "Plumbing": plumbing,
    "Electrical": electrical,
    "Cleaning": serviceImage3,
    "Pest Control": pestControl,
    "Appliance Repair": serviceImage1,
    "Home Painting": homePainting,
    "Carpentry": serviceImage2,
    "Lawn Care": lawnCare
  };

  // Default images if category doesn't match
  const defaultImages = [serviceImage1, serviceImage2, serviceImage3, electrical, plumbing];

  // Get appropriate image for a service
  const getServiceImage = (categoryName, index) => {
    if (categoryImageMap[categoryName]) {
      return categoryImageMap[categoryName];
    }
    // Fallback to default images with rotation
    return defaultImages[index % defaultImages.length];
  };

  useEffect(() => {
    const fetchProviderServices = async () => {
      try {
        setLoading(true);
        const token = localStorage.getItem('authToken') || sessionStorage.getItem('authToken');
        const userId = localStorage.getItem('userId') || sessionStorage.getItem('userId');
        
        if (!token || !userId) {
          setError("Authentication information not found. Please login again.");
          setLoading(false);
          return;
        }

        // Try to get provider directly using getByAuthId endpoint with correct parameter name
        try {
          console.log("Attempting to fetch provider with authId:", userId);
          const providerResponse = await axios.get(`/api/service-providers/getByAuthId?authId=${userId}`, {
            headers: { 'Authorization': `Bearer ${token}` }
          });
          
          console.log("Provider response data:", providerResponse.data);
          const provider = providerResponse.data;
          
          // Handle the case where the response is a string error message
          if (typeof provider === 'string') {
            console.error("Received string response instead of provider object:", provider);
            throw new Error(provider || "Invalid provider data returned");
          }
          
          if (!provider || provider.providerId === undefined) {
            console.error("Invalid provider data returned:", provider);
            throw new Error("Invalid provider data returned");
          }
          
          // Set provider name from fetched data
          setProviderName(`${provider.firstName || ''} ${provider.lastName || ''}`.trim() || "Service Provider");
          
          // Step 2: Get all services
          const servicesResponse = await axios.get("/api/services/getAll", {
            headers: { 'Authorization': `Bearer ${token}` }
          });
          
          // Step 3: Filter to get only this provider's services
          const providerServices = servicesResponse.data.filter(
            service => service.provider && service.provider.providerId === provider.providerId
          );
          
          // Step 4: Get all categories for display
          const categoriesResponse = await axios.get("/api/service-categories/getAll", {
            headers: { 'Authorization': `Bearer ${token}` }
          });
          
          // Create a map of category IDs to names
          const categoryMap = categoriesResponse.data.reduce((map, category) => {
            map[category.categoryId] = category.categoryName;
            return map;
          }, {});
          
          // Enrich services with category names and images
          const enhancedServices = providerServices.map((service, index) => ({
            id: service.serviceId,
            title: service.serviceName,
            subtitle: service.serviceDescription,
            priceRange: service.priceRange,
            durationEstimate: service.durationEstimate,
            categoryId: service.category?.categoryId,
            categoryName: categoryMap[service.category?.categoryId] || "Uncategorized",
            image: getServiceImage(categoryMap[service.category?.categoryId], index)
          }));
          
          setServices(enhancedServices);
          
          // Group services by category
          const groupedServices = enhancedServices.reduce((groups, service) => {
            const categoryName = service.categoryName;
            if (!groups[categoryName]) {
              groups[categoryName] = [];
            }
            groups[categoryName].push(service);
            return groups;
          }, {});
          
          setCategoryGroups(groupedServices);
          setLoading(false);
          
        } catch (directError) {
          console.error("Direct API call failed, falling back to alternative method:", directError);
          
          // Fallback: Get service provider ID by matching userId from all providers
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
          
          // Set provider name from fetched data
          setProviderName(`${provider.firstName || ''} ${provider.lastName || ''}`.trim() || "Service Provider");
          
          // Step 2: Get all services
          const servicesResponse = await axios.get("/api/services/getAll", {
            headers: { 'Authorization': `Bearer ${token}` }
          });
          
          // Step 3: Filter to get only this provider's services
          const providerServices = servicesResponse.data.filter(
            service => service.provider && service.provider.providerId === provider.providerId
          );
          
          // Step 4: Get all categories for display
          const categoriesResponse = await axios.get("/api/service-categories/getAll", {
            headers: { 'Authorization': `Bearer ${token}` }
          });
          
          // Create a map of category IDs to names
          const categoryMap = categoriesResponse.data.reduce((map, category) => {
            map[category.categoryId] = category.categoryName;
            return map;
          }, {});
          
          // Enrich services with category names and images
          const enhancedServices = providerServices.map((service, index) => ({
            id: service.serviceId,
            title: service.serviceName,
            subtitle: service.serviceDescription,
            priceRange: service.priceRange,
            durationEstimate: service.durationEstimate,
            categoryId: service.category?.categoryId,
            categoryName: categoryMap[service.category?.categoryId] || "Uncategorized",
            image: getServiceImage(categoryMap[service.category?.categoryId], index)
          }));
          
          setServices(enhancedServices);
          
          // Group services by category
          const groupedServices = enhancedServices.reduce((groups, service) => {
            const categoryName = service.categoryName;
            if (!groups[categoryName]) {
              groups[categoryName] = [];
            }
            groups[categoryName].push(service);
            return groups;
          }, {});
          
          setCategoryGroups(groupedServices);
          setLoading(false);
        }
      } catch (error) {
        console.error("Error fetching service provider data:", error);
        setError("Failed to load services. Please try again later.");
        setLoading(false);
      }
    };
    
    fetchProviderServices();
  }, []);

  return (
    <>
      {/* Hero Section */}
      <div className="flex relative h-63">
        {/* Left Content */}
        <div className="flex-1 bg-[#495E57] flex flex-col justify-center items-center p-10 z-10">
          <p className="font-bold text-4xl text-[#F4CE14] mb-2">
            Welcome,{" "}
            <span 
              onClick={() => navigate("/serviceProviderProfile")}
              className="cursor-pointer hover:underline transition-colors active:text-[#E9C412]"
            >
              {providerName}
            </span>
            !
          </p>
          <p className="text-white mb-4 text-center text-1xl">
            Add your services and start connecting with customers today.
          </p>
        </div>
        
        {/* Right Content - Slideshow */}
        <div className="flex-1 relative overflow-hidden">
          <img 
            src={serviceImage1} 
            alt="Service 1" 
            className="w-full h-full object-cover absolute animate-fade"
          />
          <img 
            src={serviceImage2} 
            alt="Service 2" 
            className="w-full h-full object-cover absolute animate-fade-delayed-1"
            style={{ animationDelay: "3.3s" }}
          />
          <img 
            src={serviceImage3} 
            alt="Service 3" 
            className="w-full h-full object-cover absolute animate-fade-delayed-2"
            style={{ animationDelay: "6.6s" }}
          />
        </div>
      </div>

      {/* Quick Actions Section */}
      <div className="bg-gradient-to-br from-white to-gray-100 p-8 rounded-lg shadow-xl mb-8 max-w-7xl mx-auto">
        <motion.h2 
          className="text-2xl font-bold text-[#495E57] mb-6 text-center"
          initial={{ opacity: 0, y: -20 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ duration: 0.5 }}
        >
          Quick Actions
        </motion.h2>
        <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
          <motion.div
            whileHover={{ scale: 1.03 }}
            whileTap={{ scale: 0.97 }}
            initial={{ opacity: 0, y: 20 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ duration: 0.5, delay: 0.1 }}
          >
            <Link 
              to="/addService" 
              className="block h-full bg-gradient-to-r from-[#F4CE14] to-[#f5d44a] text-[#333] rounded-xl overflow-hidden shadow-md hover:shadow-xl transition duration-300"
            >
              <div className="p-6 flex flex-col h-full">
                <div className="bg-white/30 rounded-full w-16 h-16 flex items-center justify-center mb-4 mx-auto">
                  <svg xmlns="http://www.w3.org/2000/svg" className="h-8 w-8" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 4v16m8-8H4" />
                  </svg>
                </div>
                <h3 className="text-xl font-bold text-center mb-2">Add New Service</h3>
                <p className="text-center text-sm opacity-80 flex-grow">Create a new service offering for your customers</p>
                <div className="flex justify-center mt-4">
                  <span className="inline-flex items-center text-sm font-medium">
                    Get Started
                    <svg className="w-5 h-5 ml-1" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                      <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M14 5l7 7m0 0l-7 7m7-7H3" />
                    </svg>
                  </span>
                </div>
              </div>
            </Link>
          </motion.div>

          <motion.div
            whileHover={{ scale: 1.03 }}
            whileTap={{ scale: 0.97 }}
            initial={{ opacity: 0, y: 20 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ duration: 0.5, delay: 0.2 }}
          >
            <Link 
              to="/serviceProviderBookings"
              className="block h-full bg-white text-[#495E57] rounded-xl overflow-hidden shadow-md hover:shadow-xl transition duration-300 border border-gray-100"
            >
              <div className="p-6 flex flex-col h-full">
                <div className="bg-[#495E57]/10 rounded-full w-16 h-16 flex items-center justify-center mb-4 mx-auto">
                  <svg className="w-8 h-8 text-[#495E57]" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 5H7a2 2 0 00-2 2v12a2 2 0 002 2h10a2 2 0 002-2V7a2 2 0 00-2-2h-2M9 5a2 2 0 002 2h2a2 2 0 002-2M9 5a2 2 0 012-2h2a2 2 0 012 2m-3 7h3m-3 4h3m-6-4h.01M9 16h.01" />
                  </svg>
                </div>
                <h3 className="text-xl font-bold text-center mb-2">Customer Bookings</h3>
                <p className="text-center text-sm text-gray-600 flex-grow">Manage service requests and track appointments</p>
                <div className="flex justify-center mt-4">
                  <span className="inline-flex items-center text-sm font-medium text-[#495E57]">
                    View Bookings
                    <svg className="w-5 h-5 ml-1" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                      <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M14 5l7 7m0 0l-7 7m7-7H3" />
                    </svg>
                  </span>
                </div>
              </div>
            </Link>
          </motion.div>

          <motion.div
            whileHover={{ scale: 1.03 }}
            whileTap={{ scale: 0.97 }}
            initial={{ opacity: 0, y: 20 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ duration: 0.5, delay: 0.3 }}
          >
            <Link 
              to="/serviceProviderProfile/schedule"
              className="block h-full bg-white text-[#495E57] rounded-xl overflow-hidden shadow-md hover:shadow-xl transition duration-300 border border-gray-100"
            >
              <div className="p-6 flex flex-col h-full">
                <div className="bg-[#495E57]/10 rounded-full w-16 h-16 flex items-center justify-center mb-4 mx-auto">
                  <svg className="w-8 h-8 text-[#495E57]" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z" />
                  </svg>
                </div>
                <h3 className="text-xl font-bold text-center mb-2">Manage Schedule</h3>
                <p className="text-center text-sm text-gray-600 flex-grow">Set your availability and working hours</p>
                <div className="flex justify-center mt-4">
                  <span className="inline-flex items-center text-sm font-medium text-[#495E57]">
                    Update Schedule
                    <svg className="w-5 h-5 ml-1" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                      <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M14 5l7 7m0 0l-7 7m7-7H3" />
                    </svg>
                  </span>
                </div>
              </div>
            </Link>
          </motion.div>
        </div>
      </div>

      {/* Services Offered Section */}
      <div className="py-12 px-8 bg-gradient-to-b from-gray-50 to-white">
        <motion.div
          initial={{ opacity: 0, y: 20 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ duration: 0.6 }}
          className="max-w-7xl mx-auto"
        >
          <motion.h5 
            className="font-bold text-3xl text-[#495E57] mb-6 text-center"
            initial={{ opacity: 0, y: -10 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ duration: 0.5, delay: 0.2 }}
          >
            Your Services
          </motion.h5>
          
          {loading ? (
            <motion.div 
              className="flex flex-col items-center justify-center my-12"
              initial={{ opacity: 0 }}
              animate={{ opacity: 1 }}
              transition={{ duration: 0.5 }}
            >
              <motion.div 
                className="w-16 h-16 border-4 border-[#F4CE14] border-t-transparent rounded-full"
                animate={{ rotate: 360 }}
                transition={{ repeat: Infinity, duration: 1, ease: "linear" }}
              />
              <p className="text-gray-500 mt-4">Loading your services...</p>
            </motion.div>
          ) : error ? (
            <motion.div 
              className="max-w-xl mx-auto my-8 p-6 bg-red-50 border border-red-200 text-red-700 rounded-lg shadow-sm"
              initial={{ opacity: 0, scale: 0.95 }}
              animate={{ opacity: 1, scale: 1 }}
              transition={{ duration: 0.4 }}
            >
              <div className="flex items-center mb-3">
                <svg className="w-6 h-6 mr-2 text-red-500" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 8v4m0 4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
                </svg>
                <h6 className="font-semibold text-lg">Error</h6>
              </div>
              <p>{error}</p>
            </motion.div>
          ) : services.length === 0 ? (
            <motion.div 
              className="text-center my-12 max-w-md mx-auto bg-white p-8 rounded-xl shadow-md"
              initial={{ opacity: 0, y: 20 }}
              animate={{ opacity: 1, y: 0 }}
              transition={{ duration: 0.5 }}
            >
              <div className="bg-gray-100 w-20 h-20 rounded-full flex items-center justify-center mx-auto mb-6">
                <svg className="w-10 h-10 text-gray-400" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 9v3m0 0v3m0-3h3m-3 0H9m12 0a9 9 0 11-18 0 9 9 0 0118 0z" />
                </svg>
              </div>
              <h6 className="text-xl font-semibold text-gray-700 mb-3">
                No Services Yet
              </h6>
              <p className="text-gray-600 mb-6">
                You haven't added any services to your profile. Start by adding your first service to begin receiving bookings.
              </p>
              <motion.button 
                whileHover={{ scale: 1.05 }}
                whileTap={{ scale: 0.95 }}
                className="bg-gradient-to-r from-[#F4CE14] to-[#e5c118] text-[#333] px-6 py-3 rounded-lg font-medium shadow-lg hover:shadow-xl transition-all"
                onClick={() => navigate("/addService")}
              >
                Add Your First Service
              </motion.button>
            </motion.div>
          ) : (
            <>
              <div className="mb-8">
                <motion.div 
                  className="overflow-x-auto pb-4 scrollbar-thin scrollbar-thumb-[#495E57]/20 scrollbar-thumb-rounded scrollbar-track-gray-100 -mx-2 px-2"
                  initial={{ opacity: 0 }}
                  animate={{ opacity: 1 }}
                  transition={{ duration: 0.5, delay: 0.3 }}
                >
                  <div className="flex flex-row justify-center flex-nowrap mt-1 pb-1 w-max min-w-full space-x-6">
                    {Object.keys(categoryGroups).map((categoryName, index) => (
                      <motion.div 
                        className="w-64 flex-none"
                        key={categoryName}
                        initial={{ opacity: 0, y: 30 }}
                        animate={{ opacity: 1, y: 0 }}
                        transition={{ duration: 0.5, delay: 0.1 * index }}
                        whileHover={{ y: -5 }}
                      >
                        <div className="bg-white border border-gray-200 rounded-2xl p-5 shadow-lg hover:shadow-xl transition-shadow duration-300 overflow-hidden">
                          <div className="relative overflow-hidden rounded-xl mb-4 h-48">
                            <img 
                              src={categoryImageMap[categoryName] || defaultImages[index % defaultImages.length]} 
                              alt={categoryName}
                              className="w-full h-full object-cover transition-transform duration-300 hover:scale-110" 
                            />
                            <div className="absolute inset-0 bg-gradient-to-t from-black/70 to-transparent flex items-end">
                              <h6 className="text-xl font-bold text-white p-4">{categoryName}</h6>
                            </div>
                          </div>
                          <div className="flex items-center justify-between">
                            <p className="text-sm font-medium text-gray-800">
                              <span className="text-lg font-bold text-[#495E57] mr-1">{categoryGroups[categoryName].length}</span> 
                              service{categoryGroups[categoryName].length !== 1 ? 's' : ''}
                            </p>
                            <motion.button
                              whileHover={{ scale: 1.05 }}
                              whileTap={{ scale: 0.95 }}
                              className="text-sm text-[#495E57] hover:text-[#F4CE14] flex items-center font-medium"
                              onClick={() => navigate(`/serviceProviderProfile/services?category=${categoryName}`)}
                            >
                              View
                              <svg className="w-4 h-4 ml-1" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 5l7 7-7 7" />
                              </svg>
                            </motion.button>
                          </div>
                        </div>
                      </motion.div>
                    ))}
                  </div>
                </motion.div>
              </div>
              
              <motion.div 
                className="flex justify-center gap-5 mt-8"
                initial={{ opacity: 0, y: 20 }}
                animate={{ opacity: 1, y: 0 }}
                transition={{ duration: 0.5, delay: 0.6 }}
              >
                <motion.button
                  whileHover={{ scale: 1.05 }}
                  whileTap={{ scale: 0.95 }}
                  className="px-6 py-3 bg-[#495E57] hover:bg-[#3a4a45] text-white rounded-lg transition-colors shadow-md hover:shadow-lg flex items-center"
                  onClick={() => navigate("/serviceProviderProfile/services")}
                >
                  <svg className="w-5 h-5 mr-2" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 6v6m0 0v6m0-6h6m-6 0H6" />
                  </svg>
                  Manage All Services
                </motion.button>
              </motion.div>
            </>
          )}
        </motion.div>
      </div>

      {/* Footer Section */}
      <Footer />

      {/* Add these animation classes to your global CSS or tailwind config */}
      <style jsx>{`
        @keyframes fade {
          0% { opacity: 1; }
          33% { opacity: 0; }
          66% { opacity: 1; }
          100% { opacity: 0; }
        }
        .animate-fade {
          animation: fade 10s infinite;
        }
        .animate-fade-delayed-1 {
          animation: fade 10s infinite;
          animation-delay: 3.3s;
        }
        .animate-fade-delayed-2 {
          animation: fade 10s infinite;
          animation-delay: 6.6s;
        }
      `}</style>
    </>
  );
}

export default ServiceProviderHomePage;