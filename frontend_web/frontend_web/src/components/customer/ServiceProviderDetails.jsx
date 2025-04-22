import React, { useState, useEffect } from "react";
import { useParams, useNavigate } from "react-router-dom";
import axios from "axios";

const BASE_URL = "http://localhost:8080";

const ServiceProviderDetails = () => {
  const { providerId } = useParams();
  const [provider, setProvider] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const navigate = useNavigate();

  useEffect(() => {
    const fetchProviderDetails = async () => {
      try {
        console.log("Fetching provider details for ID:", providerId);
        const token = localStorage.getItem("authToken") || sessionStorage.getItem("authToken");
        
        // Try all possible API endpoints until one works
        try {
          // First try the correct endpoint based on your backend controller
          const response = await axios.get(`${BASE_URL}/api/service-providers/getById/${providerId}`, {
            headers: { Authorization: `Bearer ${token}` }
          });
          
          console.log("Provider data received:", response.data);
          setProvider(response.data);
          setLoading(false);
        } catch (err) {
          console.log("First endpoint failed, trying alternative...");
          
          // Try second alternative
          try {
            const altResponse = await axios.get(`${BASE_URL}/api/serviceProvider/${providerId}`, {
              headers: { Authorization: `Bearer ${token}` }
            });
            
            console.log("Provider data received from alt endpoint:", altResponse.data);
            setProvider(altResponse.data);
            setLoading(false);
          } catch (err2) {
            // Try third alternative
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

  const renderStars = (rating) => {
    const stars = [];
    const fullStars = Math.floor(rating);
    const halfStar = rating % 1 >= 0.5;
    
    for (let i = 0; i < fullStars; i++) {
      stars.push(<i key={`full-${i}`} className="fas fa-star text-yellow-400"></i>);
    }
    
    if (halfStar) {
      stars.push(<i key="half" className="fas fa-star-half-alt text-yellow-400"></i>);
    }
    
    const emptyStars = 5 - stars.length;
    for (let i = 0; i < emptyStars; i++) {
      stars.push(<i key={`empty-${i}`} className="far fa-star text-yellow-400"></i>);
    }
    
    return stars;
  };

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

  return (
    <div className="container mx-auto p-4 max-w-6xl">
      <button
        onClick={() => navigate(-1)}
        className="mb-6 flex items-center gap-2 text-[#495E57] hover:text-[#F4CE14] transition-colors"
      >
        <i className="fas fa-arrow-left"></i> Back
      </button>
      
      {/* Provider header card */}
      <div className="bg-white rounded-lg shadow-md overflow-hidden mb-8">
        <div className="bg-[#495E57] p-6 text-white relative">
          <h1 className="text-3xl font-bold">Service Provider Details</h1>
        </div>
        
        <div className="p-6">
          <div className="flex flex-col md:flex-row items-start gap-8">
            {/* Provider image */}
            <div className="md:w-1/4 flex flex-col items-center">
              <img
                src={provider.profileImage 
                  ? `${BASE_URL}${provider.profileImage}` 
                  : "/default-profile.jpg"}
                alt={`${provider.firstName} ${provider.lastName}`}
                className="w-48 h-48 rounded-full border-4 border-[#F4CE14] shadow-lg object-cover"
                onError={(e) => {
                  e.target.onerror = null;
                  e.target.src = "/default-profile.jpg";
                }}
              />
              {provider.verified && (
                <div className="mt-4 bg-green-100 text-green-800 px-4 py-2 rounded-full flex items-center gap-2">
                  <i className="fas fa-check-circle"></i>
                  <span>Verified Provider</span>
                </div>
              )}
            </div>
            
            {/* Provider information */}
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
              
              {/* Availability */}
              <div className="mt-6 bg-gray-50 p-4 rounded-lg">
                <h3 className="font-semibold text-[#495E57] mb-2">Availability</h3>
                <p className="text-gray-700">
                  {provider.availabilitySchedule || "Contact provider for availability details"}
                </p>
              </div>
              
              {/* Bio/Description */}
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
    </div>
  );
};

export default ServiceProviderDetails;
