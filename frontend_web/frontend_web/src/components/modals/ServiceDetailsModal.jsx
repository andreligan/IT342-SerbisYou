import React from "react";

const ServiceDetailsModal = ({ 
  isOpen, 
  onClose, 
  service, 
  serviceRatings, 
  onBookService,
  renderStars
}) => {
  if (!isOpen || !service) return null;

  return (
    <div className="fixed inset-0 bg-opacity-70 flex items-center justify-center z-50 p-4 backdrop-blur-sm">
      <div className="bg-white rounded-lg shadow-xl w-full max-w-3xl overflow-hidden">
        {/* Modal Header */}
        <div className="bg-[#495E57] p-4 text-white relative">
          <h2 className="text-2xl font-bold text-center">{service.serviceName}</h2>
          <button
            onClick={onClose}
            className="absolute right-4 top-4 text-white hover:text-[#F4CE14]"
          >
            <i className="fas fa-times"></i>
          </button>
        </div>

        <div className="p-6">
          {/* Provider Details */}
          <div className="flex flex-col md:flex-row items-center md:items-start gap-6 mb-6">
            <div className="flex items-center gap-4">
              <img
                src={service.provider?.profilePicture || "/default-profile.jpg"}
                alt="Provider Profile"
                className="w-24 h-24 rounded-full border-2 border-[#F4CE14] shadow-md"
              />
              <div className="flex flex-col">
                <div className="mb-2">
                  <h3 className="text-lg font-bold text-[#495E57] flex items-center gap-2">
                    <i className="fas fa-user text-[#495E57]"></i>
                    {service.provider?.firstName || "Unknown"} {service.provider?.lastName || ""}
                    {service.provider?.verified && (
                      <i className="fas fa-check-circle text-[#F4CE14]" title="Verified Provider"></i>
                    )}
                  </h3>
                  <p className="text-sm text-gray-600 flex items-center gap-2">
                    <i className="fas fa-briefcase text-[#495E57]"></i>
                    {service.provider?.businessName || "Independent Provider"}
                  </p>
                </div>
                
                <div className="flex items-center gap-2 mb-2">
                  {renderStars(service.provider?.averageRating || 0)}
                  <span className="text-sm text-gray-600">
                    ({service.provider?.averageRating?.toFixed(1) || "No ratings"})
                  </span>
                </div>
                <p className="text-sm text-gray-600 flex items-center gap-2">
                  <i className="fas fa-calendar-alt text-[#495E57]"></i>
                  {service.provider?.yearsOfExperience ? `${service.provider?.yearsOfExperience} years experience` : "Experience not specified"}
                </p>
              </div>
            </div>

            <div className="flex-1 text-right">
              <p className="text-sm text-gray-600 flex items-center justify-end gap-2">
                <strong>Contact:</strong>
                <span className="bg-gray-100 px-3 py-1 rounded-full">
                  {service.provider?.phoneNumber || "Not available"}
                </span>
              </p>
              <p className="text-sm text-gray-600 mt-2 flex items-center justify-end gap-2">
                <strong>Availability:</strong> 
                <span>{service.provider?.availabilitySchedule || "Contact for details"}</span>
              </p>
            </div>
          </div>

          <hr className="border-t border-gray-200 my-6" />

          {/* Service Details */}
          <div className="flex flex-col md:flex-row items-center md:items-start gap-6">
            <div className="w-full md:w-1/3">
              <img
                src={service.image || "/default-service.jpg"}
                alt="Service"
                className="w-full h-48 object-cover rounded-lg shadow-md"
              />
              <div className="mt-4 bg-[#F4CE14] text-[#495E57] text-lg font-bold text-center px-4 py-2 rounded-md">
                ₱{service.price}
              </div>
            </div>

            <div className="flex-1">
              <div className="flex items-center gap-2 mb-3">
                <span className="bg-[#495E57] text-white px-3 py-1 rounded-full text-sm">
                  {service.categoryName}
                </span>
                <span className="bg-gray-100 text-gray-700 px-3 py-1 rounded-full text-sm">
                  {service.durationEstimate || "Duration not specified"}
                </span>
              </div>
              
              <h3 className="text-lg font-bold text-[#495E57] mb-2">Description</h3>
              <p className="text-gray-600 mb-6 leading-relaxed">
                {service.serviceDescription}
              </p>
              
              {/* Service Rating */}
              <div className="flex items-center gap-2 mb-6">
                <span className="text-sm text-gray-700">Service Rating:</span>
                {serviceRatings[service.serviceId]?.reviewCount > 0 ? (
                  <>
                    <div className="flex items-center">
                      {renderStars(serviceRatings[service.serviceId]?.averageRating || 0)}
                      <span className="ml-1 text-sm text-gray-600">
                        ({serviceRatings[service.serviceId]?.averageRating?.toFixed(1)})
                      </span>
                    </div>
                    <span className="ml-1 text-xs text-gray-500">
                      based on {serviceRatings[service.serviceId]?.reviewCount} reviews
                    </span>
                  </>
                ) : (
                  <span className="text-sm text-gray-400 italic">No reviews yet</span>
                )}
              </div>
              
              {/* Book Service Button */}
              <button
                onClick={onBookService}
                className="w-full bg-[#495E57] hover:bg-[#3e4f49] text-white font-bold py-3 px-6 rounded-md transition-colors flex items-center justify-center gap-2"
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
