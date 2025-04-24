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

        // Step 1: Get service provider ID by matching userId
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
          <button
          className="h-12 bg-[#F4CE14] hover:bg-[#e0b813] text-[#495E57] px-4 py-2 rounded transition-colors text-center shadow-md hover:shadow-lg"
          onClick={() => navigate("/addService")}
          >
            Add a Service
          </button>
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
      <div className="bg-white p-6 rounded-lg shadow-lg mb-6">
        <h2 className="text-xl font-bold text-[#495E57] mb-4">Quick Actions</h2>
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
          <Link 
            to="/addService" 
            className="flex items-center justify-center p-4 bg-[#F4CE14] text-black rounded-lg hover:bg-yellow-500 transition"
          >
            <svg xmlns="http://www.w3.org/2000/svg" className="h-6 w-6 mr-2" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 4v16m8-8H4" />
            </svg>
            Add New Service
          </Link>
        </div>
      </div>

      {/* Services Offered Section */}
      <div className="p-10 bg-gray-50">
        <h5 className="font-bold text-2xl text-[#495E57] mb-4 text-center">
          Your Services
        </h5>
        
        {loading ? (
          <div className="flex justify-center my-8">
            <div className="w-8 h-8 border-4 border-green-500 border-t-transparent rounded-full animate-spin"></div>
          </div>
        ) : error ? (
          <div className="max-w-xl mx-auto my-4 p-4 bg-red-100 border border-red-400 text-red-700 rounded">
            {error}
          </div>
        ) : services.length === 0 ? (
          <div className="text-center my-6">
            <h6 className="text-lg text-gray-600">
              You haven't added any services yet.
            </h6>
            <button 
              className="mt-2 bg-green-600 hover:bg-green-700 text-white px-4 py-2 rounded transition-colors shadow-md hover:shadow-lg"
              onClick={() => navigate("/addService")}
            >
              Add Your First Service
            </button>
          </div>
        ) : (
          <>
            <div className="overflow-x-auto pb-2 scrollbar-thin scrollbar-thumb-gray-300 scrollbar-thumb-rounded">
              <div className="flex flex-row justify-center flex-nowrap mt-1 pb-1 w-max min-w-full">
                {Object.keys(categoryGroups).map((categoryName, index) => (
                  <div className="w-56 flex-none mx-2" key={categoryName}>
                    <div className="border border-gray-300 rounded-lg p-5 bg-white shadow-md hover:scale-105 transition-transform duration-300">
                      <img 
                        src={categoryImageMap[categoryName] || defaultImages[index % defaultImages.length]} 
                        alt={categoryName}
                        className="w-full h-40 object-cover rounded-lg" 
                      />
                      <h6 className="text-lg font-bold text-[#495E57] mt-2 text-center">{categoryName}</h6>
                      <p className="text-sm text-gray-600 text-center mt-1">
                        {categoryGroups[categoryName].length} service{categoryGroups[categoryName].length !== 1 ? 's' : ''}
                      </p>
                    </div>
                  </div>
                ))}
              </div>
            </div>
            
            <div className="flex justify-center gap-5 mt-4">
              <button
                className="w-40 h-12 bg-[#F4CE14] hover:bg-[#e0b813] text-[#495E57] px-4 py-2 rounded transition-colors text-center shadow-md hover:shadow-lg"
                onClick={() => navigate("/addService")}
              >
                Add New Service
              </button>
                
              <button
                className="w-40 bg-[#495E57] hover:bg-[#3a4a45] text-white px-4 py-2 rounded transition-colors text-center shadow-md hover:shadow-lg"
                onClick={() => navigate("/myServices")}
              >
                Manage Services
              </button>
            </div>
          </>
        )}
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