import React, { useState, useEffect, useRef } from "react";
import axios from "axios";

const BASE_URL = "http://localhost:8080";

const ServiceDetailsModal = ({ 
  isOpen, 
  onClose, 
  service, 
  serviceRatings, 
  onBookService,
  renderStars,
  clickPosition
}) => {
  const [animationState, setAnimationState] = useState('closed');
  const [providerImage, setProviderImage] = useState(null);
  const modalRef = useRef(null);
  
  useEffect(() => {
    if (isOpen && service?.provider?.providerId) {
      // Fetch the provider's profile image
      const fetchProviderImage = async () => {
        try {
          const response = await axios.get(
            `${BASE_URL}/api/serviceProviders/getServiceProviderImage/${service.provider.providerId}`,
            { withCredentials: true } // Include cookies or authentication tokens
          );
          setProviderImage(`${BASE_URL}${response.data}`);
        } catch (error) {
          console.error("Error fetching provider image:", error);
          setProviderImage("/default-profile.jpg"); // Fallback to default profile image
        }
      };
      fetchProviderImage();
    }
  }, [isOpen, service]);

  useEffect(() => {
    if (isOpen) {
      // Start opening animation
      setAnimationState('opening');
      // After a brief delay, set to fully open
      const timer = setTimeout(() => {
        setAnimationState('open');
      }, 10);
      return () => clearTimeout(timer);
    } else {
      setAnimationState('closed');
    }
  }, [isOpen]);
  
  if (!isOpen || !service) return null;
  
  // Animation styles based on state
  let overlayStyle = {
    opacity: 0,
    transition: 'opacity 0.3s ease-out'
  };
  
  let modalStyle = {
    transform: 'scale(0.8)',
    opacity: 0,
    transition: 'transform 0.4s ease-out, opacity 0.3s ease-out',
  };
  
  // Backdrop blur animation style
  let backdropStyle = {
    backdropFilter: 'blur(0px)',
    backgroundColor: 'rgba(0, 0, 0, 0)',
    transition: 'backdrop-filter 0.4s ease-out, background-color 0.3s ease-out',
  }
  
  if (animationState === 'opening') {
    overlayStyle.opacity = 0;
    modalStyle = {
      ...modalStyle,
      transform: clickPosition ? `translate(${clickPosition.x}px, ${clickPosition.y}px) scale(0.5)` : 'scale(0.8)',
      opacity: 0
    };
    backdropStyle = {
      ...backdropStyle,
      backdropFilter: 'blur(0px)',
      backgroundColor: 'rgba(0, 0, 0, 0)'
    };
  } else if (animationState === 'open') {
    overlayStyle.opacity = 1;
    modalStyle = {
      ...modalStyle,
      transform: 'scale(1) translate(0, 0)',
      opacity: 1
    };
    backdropStyle = {
      ...backdropStyle,
      backdropFilter: 'blur(10px)',
      backgroundColor: 'rgba(0, 0, 0, 0.7)'
    };
  }

  return (
    <div 
      className="fixed inset-0 flex items-center justify-center z-50 p-4 overflow-auto"
      style={overlayStyle}
      onClick={onClose}
    >
      <div 
        className="absolute inset-0"
        style={backdropStyle}
        onClick={onClose}
      ></div>
      
      <div
        ref={modalRef}
        className="bg-white bg-opacity-90 rounded-xl shadow-xl w-full max-w-5xl overflow-hidden relative"
        style={modalStyle}
        onClick={(e) => e.stopPropagation()}
      >
        {/* Modal Header */}
        <div className="bg-[#495E57] p-5 text-white relative">
          <h2 className="text-2xl md:text-3xl font-bold text-center">{service.serviceName}</h2>
          <button
            onClick={onClose}
            className="absolute right-5 top-5 text-white hover:text-[#F4CE14] transition-colors text-xl"
          >
            <i className="fas fa-times"></i>
          </button>
        </div>

        <div className="p-6 md:p-8">
          {/* Provider Details - Enhanced Layout */}
          <div className="flex flex-col md:flex-row items-start gap-6 mb-8">
            <div className="flex items-center gap-5 md:w-1/2">
              <img
                src={providerImage || "/default-profile.jpg"}
                alt="Provider Profile"
                className="w-28 h-28 rounded-full border-2 border-[#F4CE14] shadow-md"
              />
              <div className="flex flex-col">
                <div className="mb-3">
                  <h3 className="text-xl font-bold text-[#495E57] flex items-center gap-2">
                    <i className="fas fa-user text-[#495E57]"></i>
                    {service.provider?.firstName || "Unknown"} {service.provider?.lastName || ""}
                    {service.provider?.verified && (
                      <i className="fas fa-check-circle text-[#F4CE14]" title="Verified Provider"></i>
                    )}
                  </h3>
                  <p className="text-sm text-gray-600 flex items-center gap-2 mt-1">
                    <i className="fas fa-briefcase text-[#495E57]"></i>
                    {service.provider?.businessName || "Independent Provider"}
                  </p>
                </div>
                
                <div className="flex items-center gap-2 mb-2">
                  {renderStars(service.provider?.averageRating || 0)}
                  <span className="ml-1 text-sm text-gray-600">
                    ({service.provider?.averageRating?.toFixed(1) || "No ratings"})
                  </span>
                </div>
                <p className="text-sm text-gray-600 flex items-center gap-2">
                  <i className="fas fa-calendar-alt text-[#495E57]"></i>
                  {service.provider?.yearsOfExperience ? `${service.provider?.yearsOfExperience} years experience` : "Experience not specified"}
                </p>
              </div>
            </div>

            <div className="flex-1 md:text-right md:border-l md:pl-6 border-gray-200">
              <p className="text-sm text-gray-700 font-medium mb-2">Contact Information:</p>
              <div className="flex flex-col md:items-end gap-3">
                <p className="flex items-center md:justify-end gap-2 text-sm">
                  <i className="fas fa-phone text-[#495E57] mr-1"></i>
                  <span className="bg-gray-100 px-3 py-1.5 rounded-full font-medium">
                    {service.provider?.phoneNumber || "Not available"}
                  </span>
                </p>
                <p className="flex items-center md:justify-end gap-2 text-sm">
                  <i className="fas fa-clock text-[#495E57] mr-1"></i>
                  <span className="bg-gray-50 px-3 py-1.5 rounded-full">
                    {service.provider?.availabilitySchedule || "Contact for details"}
                  </span>
                </p>
              </div>
            </div>
          </div>

          <hr className="border-t border-gray-200 my-6" />

          {/* Service Details - Reorganized for better use of space */}
          <div className="flex flex-col md:flex-row gap-8">
            <div className="md:w-2/5">
              <img
                src={`${BASE_URL}${service.serviceImage}`}
                alt={service.serviceName}
                className="w-full h-64 object-cover rounded-lg shadow-md"
              />
              <div className="mt-4 bg-[#F4CE14] text-[#495E57] text-xl font-bold text-center px-6 py-3 rounded-md shadow-sm">
                ₱{service.price}.00
              </div>

              {/* Service Categories and Duration */}
              <div className="flex flex-wrap items-center gap-2 mt-4">
                <span className="bg-[#495E57] text-white px-4 py-2 rounded-full text-sm">
                  {service.categoryName}
                </span>
                <span className="bg-gray-100 text-gray-700 px-4 py-2 rounded-full text-sm">
                  {service.durationEstimate || "Duration not specified"}
                </span>
              </div>
            </div>

            <div className="flex-1">
              {/* Service Description */}
              <div>
                <h3 className="text-xl font-bold text-[#495E57] mb-3">Description</h3>
                <p className="text-gray-700 mb-6 leading-relaxed">
                  {service.serviceDescription}
                </p>
              </div>
              
              {/* Service Rating */}
              <div className="bg-gray-50 p-4 rounded-lg mb-6">
                <h4 className="text-lg font-semibold text-[#495E57] mb-3">Service Rating</h4>
                {serviceRatings[service.serviceId]?.reviewCount > 0 ? (
                  <div className="flex items-center gap-3">
                    <div className="flex items-center text-lg">
                      {renderStars(serviceRatings[service.serviceId]?.averageRating || 0)}
                      <span className="ml-2 text-gray-700 font-medium">
                        {serviceRatings[service.serviceId]?.averageRating?.toFixed(1)}
                      </span>
                    </div>
                    <span className="text-sm text-gray-500">
                      based on {serviceRatings[service.serviceId]?.reviewCount} reviews
                    </span>
                  </div>
                ) : (
                  <span className="text-sm text-gray-400 italic">No reviews yet</span>
                )}
              </div>
              
              {/* Book Service Button */}
              <button
                onClick={onBookService}
                className="w-full bg-[#495E57] hover:bg-[#3e4f49] text-white font-bold py-4 px-6 rounded-md transition-colors flex items-center justify-center gap-2 text-lg shadow-sm"
              >
                <i className="fas fa-calendar-check"></i> Book This Service
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

export default React.memo(ServiceDetailsModal);
