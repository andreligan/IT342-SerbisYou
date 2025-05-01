import React, { useState, useEffect } from "react";
import { API_BASE_URL } from "../utils/apiConfig";

const ReviewBookingDetails = ({
  serviceData,
  bookingDate,
  bookingTime,
  address,
  note,
  setNote,
  paymentMethod,
  setPaymentMethod,
  isFullPayment,
  setIsFullPayment,
  payMongoFee,
  appFee,
  totalPrice,
  handleBack,
  handleNext,
  formatDateForDisplay,
  formatTimeWithAMPM,
  navigate
}) => {
  // Helper function to safely get the start and end time with AM/PM
  const getFormattedTimeRange = () => {
    if (!bookingTime) return 'No time selected';
    const [start, end] = bookingTime.split('-');
    return `${formatTimeWithAMPM(start)} - ${formatTimeWithAMPM(end)}`;
  };

  // Calculate downpayment amount
  const downPaymentAmount = totalPrice * 0.5;

  // Show payment options only for GCash
  const showPaymentOptions = paymentMethod === 'gcash';

  return (
    <div className="bg-white rounded-2xl shadow-xl overflow-hidden border border-gray-100">
      {/* Enhanced Header with Service Info */}
      <div className="relative bg-gradient-to-r from-[#445954] to-[#495E57] text-white">
        <div className="absolute top-0 right-0 w-64 h-64 bg-[#F4CE14]/10 rounded-full -translate-x-16 -translate-y-32 blur-3xl"></div>
        <div className="absolute bottom-0 left-1/4 w-40 h-40 bg-[#F4CE14]/10 rounded-full -translate-y-10 blur-2xl"></div>
        
        <div className="relative p-6 sm:p-8 z-10">
          <div className="flex flex-col md:flex-row md:items-center gap-4">
            <div className="flex-1">
              <div className="text-[#F4CE14] text-sm font-medium mb-1">Step 2 of 3</div>
              <h2 className="text-2xl font-bold">Review Your Booking</h2>
              <p className="text-sm text-white/80 mt-1">
                Please review your booking details before proceeding to payment
              </p>
            </div>
          </div>
        </div>
      </div>

      <div className="p-6 sm:p-8">
        {/* Service Details Card - Enhanced Design */}
        <div className="mb-8 bg-white rounded-xl shadow-sm border border-gray-100 overflow-hidden transition-all duration-300 hover:shadow-md">
          <div className="bg-[#495E57]/5 px-5 py-3 border-b border-gray-100">
            <h3 className="font-semibold text-[#495E57] flex items-center">
              <svg xmlns="http://www.w3.org/2000/svg" className="h-5 w-5 mr-2" viewBox="0 0 20 20" fill="currentColor">
                <path fillRule="evenodd" d="M6 6V5a3 3 0 013-3h2a3 3 0 013 3v1h2a2 2 0 012 2v3.57A22.952 22.952 0 0110 13a22.95 22.95 0 01-8-1.43V8a2 2 0 012-2h2zm2-1a1 1 0 011-1h2a1 1 0 011 1v1H8V5zm1 5a1 1 0 011-1h.01a1 1 0 110 2H10a1 1 0 01-1-1z" clipRule="evenodd" />
                <path d="M2 13.692V16a2 2 0 002 2h12a2 2 0 002-2v-2.308A24.974 24.974 0 0110 15c-2.796 0-5.487-.46-8-1.308z" />
              </svg>
              Service Details
            </h3>
          </div>
          
          <div className="p-5">
            <div className="flex flex-col md:flex-row gap-6">
              <div className="md:w-1/4">
                {serviceData.serviceImage ? (
                  <img 
                    src={`${API_BASE_URL}${serviceData.serviceImage}`} 
                    alt={serviceData.serviceName} 
                    className="w-full h-40 object-cover rounded-lg shadow-sm"
                    onError={(e) => {
                      e.target.onerror = null;
                      e.target.src = "/default-service.jpg";
                    }}
                  />
                ) : (
                  <div className="w-full h-40 bg-gray-100 rounded-lg flex items-center justify-center">
                    <svg className="w-12 h-12 text-gray-300" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M4 16l4.586-4.586a2 2 0 012.828 0L16 16m-2-2l1.586-1.586a2 2 0 012.828 0L20 14m-6-6h.01M6 20h12a2 2 0 002-2V6a2 2 0 00-2-2H6a2 2 0 00-2 2v12a2 2 0 002 2z" />
                    </svg>
                  </div>
                )}
              </div>
              
              <div className="md:w-3/4">
                <h4 className="text-xl font-semibold text-[#495E57] mb-2">{serviceData.serviceName}</h4>
                <p className="text-gray-600 mb-4">{serviceData.serviceDescription || "No description provided."}</p>
                
                <div className="flex flex-wrap gap-2 mb-4">
                  {serviceData.categoryName && (
                    <span className="px-3 py-1 bg-[#495E57]/10 text-[#495E57] text-xs font-medium rounded-full">
                      {serviceData.categoryName}
                    </span>
                  )}
                  {serviceData.durationEstimate && (
                    <span className="px-3 py-1 bg-gray-100 text-gray-600 text-xs font-medium rounded-full">
                      Est. duration: {serviceData.durationEstimate}
                    </span>
                  )}
                  <span className="px-3 py-1 bg-[#F4CE14]/10 text-amber-700 text-xs font-medium rounded-full">
                    ₱{serviceData.price.toLocaleString()}
                  </span>
                </div>
                
                <div className="flex items-center gap-3">
                  {serviceData.provider?.profileImage ? (
                    <img 
                      src={`${API_BASE_URL}${serviceData.provider.profileImage}`} 
                      alt="Provider" 
                      className="w-8 h-8 rounded-full object-cover border-2 border-[#F4CE14]"
                      onError={(e) => {
                        e.target.onerror = null;
                        e.target.src = "/default-profile.jpg";
                      }}
                    />
                  ) : (
                    <div className="w-8 h-8 rounded-full bg-[#495E57]/10 flex items-center justify-center">
                      <svg xmlns="http://www.w3.org/2000/svg" className="h-4 w-4 text-[#495E57]" viewBox="0 0 20 20" fill="currentColor">
                        <path fillRule="evenodd" d="M10 9a3 3 0 100-6 3 3 0 000 6zm-7 9a7 7 0 1114 0H3z" clipRule="evenodd" />
                      </svg>
                    </div>
                  )}
                  <div className="flex flex-col">
                    <span className="text-sm font-medium text-gray-700">
                      {serviceData.provider?.firstName || ""} {serviceData.provider?.lastName || ""}
                    </span>
                    <span className="text-xs text-gray-500">Service Provider</span>
                  </div>
                  {serviceData.provider?.verified && (
                    <span className="bg-green-50 text-green-600 text-xs px-2 py-0.5 rounded-full border border-green-100 flex items-center">
                      <svg xmlns="http://www.w3.org/2000/svg" className="h-3 w-3 mr-1" viewBox="0 0 20 20" fill="currentColor">
                        <path fillRule="evenodd" d="M6.267 3.455a3.066 3.066 0 001.745-.723 3.066 3.066 0 013.976 0 3.066 3.066 0 001.745.723 3.066 3.066 0 012.812 2.812c.051.643.304 1.254.723 1.745a3.066 3.066 0 010 3.976 3.066 3.066 0 00-.723 1.745 3.066 3.066 0 01-2.812 2.812 3.066 3.066 0 00-1.745.723 3.066 3.066 0 01-3.976 0 3.066 3.066 0 00-1.745-.723 3.066 3.066 0 01-2.812-2.812 3.066 3.066 0 00-.723-1.745 3.066 3.066 0 010-3.976 3.066 3.066 0 00.723-1.745 3.066 3.066 0 012.812-2.812zm7.44 5.252a1 1 0 00-1.414-1.414L9 10.586 7.707 9.293a1 1 0 00-1.414 1.414l2 2a1 1 0 001.414 0l4-4z" clipRule="evenodd" />
                      </svg>
                      Verified
                    </span>
                  )}
                </div>
              </div>
            </div>
          </div>
        </div>

        {/* Booking Details Grid - Completely Redesigned */}
        <div className="grid grid-cols-1 md:grid-cols-2 gap-6 mb-8">
          {/* Left Column - Date and Time */}
          <div className="bg-white rounded-xl shadow-sm border border-gray-100 overflow-hidden hover:shadow-md transition-all duration-300">
            <div className="bg-[#495E57]/5 px-5 py-3 border-b border-gray-100">
              <h3 className="font-semibold text-[#495E57] flex items-center">
                <svg xmlns="http://www.w3.org/2000/svg" className="h-5 w-5 mr-2" viewBox="0 0 20 20" fill="currentColor">
                  <path fillRule="evenodd" d="M6 2a1 1 0 00-1 1v1H4a2 2 0 00-2 2v10a2 2 0 002 2h12a2 2 0 002-2V6a2 2 0 00-2-2h-1V3a1 1 0 10-2 0v1H7V3a1 1 0 00-1-1zm0 5a1 1 0 000 2h8a1 1 0 100-2H6z" clipRule="evenodd" />
                </svg>
                Schedule Details
              </h3>
            </div>
            <div className="p-5">
              <div className="flex items-center gap-4 mb-6 pb-6 border-b border-dashed border-gray-100">
                <div className="h-14 w-14 rounded-lg bg-[#495E57]/10 flex items-center justify-center flex-shrink-0">
                  <svg className="w-7 h-7 text-[#495E57]" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={1.5} d="M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z" />
                  </svg>
                </div>
                <div>
                  <p className="text-sm text-gray-500">Booking Date</p>
                  <p className="font-medium text-gray-800 mt-1">{formatDateForDisplay(bookingDate)}</p>
                </div>
              </div>
              
              <div className="flex items-center gap-4">
                <div className="h-14 w-14 rounded-lg bg-[#495E57]/10 flex items-center justify-center flex-shrink-0">
                  <svg className="w-7 h-7 text-[#495E57]" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={1.5} d="M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z" />
                  </svg>
                </div>
                <div>
                  <p className="text-sm text-gray-500">Time Slot</p>
                  <p className="font-medium text-gray-800 mt-1">{getFormattedTimeRange()}</p>
                </div>
              </div>
            </div>
          </div>
          
          {/* Right Column - Service Location */}
          <div className="bg-white rounded-xl shadow-sm border border-gray-100 overflow-hidden hover:shadow-md transition-all duration-300">
            <div className="bg-[#495E57]/5 px-5 py-3 border-b border-gray-100">
              <h3 className="font-semibold text-[#495E57] flex items-center">
                <svg xmlns="http://www.w3.org/2000/svg" className="h-5 w-5 mr-2" viewBox="0 0 20 20" fill="currentColor">
                  <path fillRule="evenodd" d="M5.05 4.05a7 7 0 119.9 9.9L10 18.9l-4.95-4.95a7 7 0 010-9.9zM10 11a2 2 0 100-4 2 2 0 000 4z" clipRule="evenodd" />
                </svg>
                Service Location
              </h3>
            </div>
            <div className="p-5">
              <div className="flex items-start">
                <div className="h-14 w-14 rounded-lg bg-[#495E57]/10 flex items-center justify-center flex-shrink-0">
                  <svg className="w-7 h-7 text-[#495E57]" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={1.5} d="M17.657 16.657L13.414 20.9a1.998 1.998 0 01-2.827 0l-4.244-4.243a8 8 0 1111.314 0z" />
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={1.5} d="M15 11a3 3 0 11-6 0 3 3 0 016 0z" />
                  </svg>
                </div>
                <div className="ml-4 flex-1">
                  <p className="text-sm text-gray-500">Delivery Address</p>
                  {address ? (
                    <>
                      <p className="font-medium text-gray-800 mt-1">{address}</p>
                      <button
                        onClick={() => navigate('/customerProfile/address')}
                        className="mt-2 text-sm text-[#495E57] hover:text-[#F4CE14] transition-colors flex items-center"
                      >
                        <svg xmlns="http://www.w3.org/2000/svg" className="h-3.5 w-3.5 mr-1" viewBox="0 0 20 20" fill="currentColor">
                          <path d="M13.586 3.586a2 2 0 112.828 2.828l-.793.793-2.828-2.828.793-.793zM11.379 5.793L3 14.172V17h2.828l8.38-8.379-2.83-2.828z" />
                        </svg>
                        Change address
                      </button>
                    </>
                  ) : (
                    <div className="mt-1 flex flex-col">
                      <p className="text-red-500 text-sm">No address available</p>
                      <button
                        onClick={() => navigate('/customerProfile/address')}
                        className="mt-2 px-4 py-2 bg-[#F4CE14] text-[#495E57] text-sm font-medium rounded-lg flex items-center w-max"
                      >
                        <svg xmlns="http://www.w3.org/2000/svg" className="h-4 w-4 mr-1.5" viewBox="0 0 20 20" fill="currentColor">
                          <path fillRule="evenodd" d="M10 5a1 1 0 011 1v3h3a1 1 0 110 2h-3v3a1 1 0 11-2 0v-3H6a1 1 0 110-2h3V6a1 1 0 011-1z" clipRule="evenodd" />
                        </svg>
                        Add Address
                      </button>
                    </div>
                  )}
                </div>
              </div>
            </div>
          </div>
        </div>
        
        {/* Special Instructions Card */}
        <div className="bg-white rounded-xl shadow-sm border border-gray-100 overflow-hidden mb-8 hover:shadow-md transition-all duration-300">
          <div className="bg-[#495E57]/5 px-5 py-3 border-b border-gray-100">
            <h3 className="font-semibold text-[#495E57] flex items-center">
              <svg xmlns="http://www.w3.org/2000/svg" className="h-5 w-5 mr-2" viewBox="0 0 20 20" fill="currentColor">
                <path fillRule="evenodd" d="M18 13V5a2 2 0 00-2-2H4a2 2 0 00-2 2v8a2 2 0 002 2h3l3 3 3-3h3a2 2 0 002-2zM5 7a1 1 0 011-1h8a1 1 0 110 2H6a1 1 0 01-1-1zm1 3a1 1 0 100 2h3a1 1 0 100-2H6z" clipRule="evenodd" />
              </svg>
              Special Instructions
            </h3>
          </div>
          <div className="p-5">
            <textarea
              value={note}
              onChange={(e) => setNote(e.target.value)}
              placeholder="Add any special instructions or notes for the service provider..."
              className="w-full p-4 border border-gray-200 rounded-xl focus:outline-none focus:ring-2 focus:ring-[#F4CE14] focus:border-transparent min-h-[120px] text-gray-700 placeholder-gray-400"
            />
            <p className="text-xs text-gray-500 mt-2">
              *You can enter specific needs, access instructions, or preferences here
            </p>
          </div>
        </div>

        {/* Payment Method Selection - Redesigned with Modern Cards */}
        <div className="bg-white rounded-xl shadow-sm border border-gray-100 overflow-hidden mb-8 hover:shadow-md transition-all duration-300">
          <div className="bg-[#495E57]/5 px-5 py-3 border-b border-gray-100">
            <h3 className="font-semibold text-[#495E57] flex items-center">
              <svg xmlns="http://www.w3.org/2000/svg" className="h-5 w-5 mr-2" viewBox="0 0 20 20" fill="currentColor">
                <path d="M4 4a2 2 0 00-2 2v1h16V6a2 2 0 00-2-2H4z" />
                <path fillRule="evenodd" d="M18 9H2v5a2 2 0 002 2h12a2 2 0 002-2V9zM4 13a1 1 0 011-1h1a1 1 0 110 2H5a1 1 0 01-1-1zm5-1a1 1 0 100 2h1a1 1 0 100-2H9z" clipRule="evenodd" />
              </svg>
              Payment Method
            </h3>
          </div>
          <div className="p-5">
            <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
              {/* Cash Payment Option */}
              <div 
                className={`relative rounded-xl overflow-hidden cursor-pointer transition-all duration-300 transform ${
                  paymentMethod === 'cash' 
                    ? 'ring-2 ring-[#F4CE14] scale-[1.02]' 
                    : 'hover:scale-[1.01] hover:shadow-md'
                }`}
                onClick={() => {
                  setPaymentMethod('cash');
                  setIsFullPayment(false); // Cash payment is always after service
                }}
              >
                <div className={`absolute inset-0 ${
                  paymentMethod === 'cash' 
                    ? 'bg-gradient-to-br from-[#F4CE14]/20 to-[#F4CE14]/5' 
                    : 'bg-white'
                }`}></div>
                
                {paymentMethod === 'cash' && (
                  <div className="absolute top-3 right-3 h-6 w-6 bg-[#F4CE14] rounded-full flex items-center justify-center">
                    <svg xmlns="http://www.w3.org/2000/svg" className="h-4 w-4 text-white" viewBox="0 0 20 20" fill="currentColor">
                      <path fillRule="evenodd" d="M16.707 5.293a1 1 0 010 1.414l-8 8a1 1 0 01-1.414 0l-4-4a1 1 0 011.414-1.414L8 12.586l7.293-7.293a1 1 0 011.414 0z" clipRule="evenodd" />
                    </svg>
                  </div>
                )}
                
                <div className="relative p-5 border border-gray-200 rounded-xl">
                  <div className="flex items-center gap-4">
                    <div className="h-12 w-12 bg-[#F4CE14]/10 rounded-full flex items-center justify-center">
                      <svg xmlns="http://www.w3.org/2000/svg" className="h-6 w-6 text-[#F4CE14]" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M17 9V7a2 2 0 00-2-2H5a2 2 0 00-2 2v6a2 2 0 002 2h2m2 4h10a2 2 0 002-2v-6a2 2 0 00-2-2H9a2 2 0 00-2 2v6a2 2 0 002 2zm7-5a2 2 0 11-4 0 2 2 0 014 0z" />
                      </svg>
                    </div>
                    <div>
                      <h4 className="font-medium text-gray-900">Cash Payment</h4>
                      <p className="text-sm text-gray-500 mt-1">Pay with cash upon completion of service</p>
                    </div>
                  </div>
                </div>
              </div>
              
              {/* GCash Payment Option */}
              <div 
                className={`relative rounded-xl overflow-hidden cursor-pointer transition-all duration-300 transform ${
                  paymentMethod === 'gcash' 
                    ? 'ring-2 ring-blue-500 scale-[1.02]' 
                    : 'hover:scale-[1.01] hover:shadow-md'
                }`}
                onClick={() => setPaymentMethod('gcash')}
              >
                <div className={`absolute inset-0 ${
                  paymentMethod === 'gcash' 
                    ? 'bg-gradient-to-br from-blue-500/20 to-blue-500/5' 
                    : 'bg-white'
                }`}></div>
                
                {paymentMethod === 'gcash' && (
                  <div className="absolute top-3 right-3 h-6 w-6 bg-blue-500 rounded-full flex items-center justify-center">
                    <svg xmlns="http://www.w3.org/2000/svg" className="h-4 w-4 text-white" viewBox="0 0 20 20" fill="currentColor">
                      <path fillRule="evenodd" d="M16.707 5.293a1 1 0 010 1.414l-8 8a1 1 0 01-1.414 0l-4-4a1 1 0 011.414-1.414L8 12.586l7.293-7.293a1 1 0 011.414 0z" clipRule="evenodd" />
                    </svg>
                  </div>
                )}
                
                <div className="relative p-5 border border-gray-200 rounded-xl">
                  <div className="flex items-center gap-4">
                    <div className="h-12 w-12 bg-blue-500/10 rounded-full flex items-center justify-center">
                      <svg xmlns="http://www.w3.org/2000/svg" className="h-6 w-6 text-blue-500" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 18h.01M8 21h8a2 2 0 002-2V5a2 2 0 00-2-2H8a2 2 0 00-2 2v14a2 2 0 002 2z" />
                      </svg>
                    </div>
                    <div>
                      <h4 className="font-medium text-gray-900">GCash</h4>
                      <p className="text-sm text-gray-500 mt-1">Pay securely online with GCash mobile wallet</p>
                    </div>
                  </div>
                </div>
              </div>
            </div>
            
            {/* Add GCash Payment Options */}
            {showPaymentOptions && (
              <div className="mt-5 border-t border-gray-100 pt-5">
                <h4 className="text-gray-700 font-medium mb-3">Payment Options</h4>
                <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                  {/* Full Payment Option */}
                  <div 
                    className={`relative rounded-xl overflow-hidden cursor-pointer transition-all duration-300 transform 
                      ${isFullPayment ? 'ring-2 ring-blue-500 bg-blue-50' : 'hover:bg-gray-50'}`}
                    onClick={() => setIsFullPayment(true)}
                  >
                    <div className="p-4 border border-gray-200 rounded-xl">
                      <div className="flex items-center">
                        <div className={`h-5 w-5 rounded-full border ${isFullPayment ? 'border-blue-500 bg-blue-500' : 'border-gray-300'} flex-shrink-0 mr-3`}>
                          {isFullPayment && (
                            <svg xmlns="http://www.w3.org/2000/svg" className="h-5 w-5 text-white" viewBox="0 0 20 20" fill="currentColor">
                              <path fillRule="evenodd" d="M16.707 5.293a1 1 0 010 1.414l-8 8a1 1 0 01-1.414 0l-4-4a1 1 0 011.414-1.414L8 12.586l7.293-7.293a1 1 0 011.414 0z" clipRule="evenodd" />
                            </svg>
                          )}
                        </div>
                        <div>
                          <h4 className="font-medium text-gray-900">Full Payment</h4>
                          <p className="text-sm text-gray-600 mt-1">Pay the entire amount now (₱{totalPrice.toLocaleString('en-PH', {minimumFractionDigits: 2})})</p>
                        </div>
                      </div>
                    </div>
                  </div>
                  
                  {/* Downpayment Option */}
                  <div 
                    className={`relative rounded-xl overflow-hidden cursor-pointer transition-all duration-300 transform 
                      ${!isFullPayment ? 'ring-2 ring-blue-500 bg-blue-50' : 'hover:bg-gray-50'}`}
                    onClick={() => setIsFullPayment(false)}
                  >
                    <div className="p-4 border border-gray-200 rounded-xl">
                      <div className="flex items-center">
                        <div className={`h-5 w-5 rounded-full border ${!isFullPayment ? 'border-blue-500 bg-blue-500' : 'border-gray-300'} flex-shrink-0 mr-3`}>
                          {!isFullPayment && (
                            <svg xmlns="http://www.w3.org/2000/svg" className="h-5 w-5 text-white" viewBox="0 0 20 20" fill="currentColor">
                              <path fillRule="evenodd" d="M16.707 5.293a1 1 0 010 1.414l-8 8a1 1 0 01-1.414 0l-4-4a1 1 0 011.414-1.414L8 12.586l7.293-7.293a1 1 0 011.414 0z" clipRule="evenodd" />
                            </svg>
                          )}
                        </div>
                        <div>
                          <h4 className="font-medium text-gray-900">Downpayment (50%)</h4>
                          <p className="text-sm text-gray-600 mt-1">Pay ₱{downPaymentAmount.toLocaleString('en-PH', {minimumFractionDigits: 2})} now, rest after service</p>
                        </div>
                      </div>
                    </div>
                  </div>
                </div>
                <div className="mt-3 text-xs text-gray-500 bg-gray-50 p-2 rounded-lg">
                  <p className="flex items-center">
                    <svg xmlns="http://www.w3.org/2000/svg" className="h-4 w-4 mr-1 text-blue-500" viewBox="0 0 20 20" fill="currentColor">
                      <path fillRule="evenodd" d="M18 10a8 8 0 11-16 0 8 8 0 0116 0zm-7-4a1 1 0 11-2 0 1 1 0 012 0zM9 9a1 1 0 000 2v3a1 1 0 001 1h1a1 1 0 100-2v-3a1 1 0 00-1-1H9z" clipRule="evenodd" />
                    </svg>
                    {isFullPayment ? 
                      "Your payment will be processed immediately and your booking will be confirmed." : 
                      "You'll pay 50% now to secure your booking, and the remaining balance after the service is completed."
                    }
                  </p>
                </div>
              </div>
            )}

            {/* Cash Payment Options - Show deposit option only for larger services */}
            {paymentMethod === 'cash' && totalPrice > 1000 && (
              <div className="mt-5 border-t border-gray-100 pt-5">
                <h4 className="text-gray-700 font-medium mb-3">Cash Payment Options</h4>
                <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                  {/* Full Payment Option */}
                  <div 
                    className={`relative rounded-xl overflow-hidden cursor-pointer transition-all duration-300 transform 
                      ${isFullPayment ? 'ring-2 ring-[#F4CE14] bg-[#F4CE14]/10' : 'hover:bg-gray-50'}`}
                    onClick={() => setIsFullPayment(true)}
                  >
                    <div className="p-4 border border-gray-200 rounded-xl">
                      <div className="flex items-center">
                        <div className={`h-5 w-5 rounded-full border ${isFullPayment ? 'border-[#F4CE14] bg-[#F4CE14]' : 'border-gray-300'} flex-shrink-0 mr-3`}>
                          {isFullPayment && (
                            <svg xmlns="http://www.w3.org/2000/svg" className="h-5 w-5 text-white" viewBox="0 0 20 20" fill="currentColor">
                              <path fillRule="evenodd" d="M16.707 5.293a1 1 0 010 1.414l-8 8a1 1 0 01-1.414 0l-4-4a1 1 0 011.414-1.414L8 12.586l7.293-7.293a1 1 0 011.414 0z" clipRule="evenodd" />
                            </svg>
                          )}
                        </div>
                        <div>
                          <h4 className="font-medium text-gray-900">Pay Full Amount</h4>
                          <p className="text-sm text-gray-600 mt-1">Pay ₱{totalPrice.toLocaleString('en-PH', {minimumFractionDigits: 2})} after service</p>
                        </div>
                      </div>
                    </div>
                  </div>
                  
                  {/* Deposit Option */}
                  <div 
                    className={`relative rounded-xl overflow-hidden cursor-pointer transition-all duration-300 transform 
                      ${!isFullPayment ? 'ring-2 ring-[#F4CE14] bg-[#F4CE14]/10' : 'hover:bg-gray-50'}`}
                    onClick={() => setIsFullPayment(false)}
                  >
                    <div className="p-4 border border-gray-200 rounded-xl">
                      <div className="flex items-center">
                        <div className={`h-5 w-5 rounded-full border ${!isFullPayment ? 'border-[#F4CE14] bg-[#F4CE14]' : 'border-gray-300'} flex-shrink-0 mr-3`}>
                          {!isFullPayment && (
                            <svg xmlns="http://www.w3.org/2000/svg" className="h-5 w-5 text-white" viewBox="0 0 20 20" fill="currentColor">
                              <path fillRule="evenodd" d="M16.707 5.293a1 1 0 010 1.414l-8 8a1 1 0 01-1.414 0l-4-4a1 1 0 011.414-1.414L8 12.586l7.293-7.293a1 1 0 011.414 0z" clipRule="evenodd" />
                            </svg>
                          )}
                        </div>
                        <div>
                          <h4 className="font-medium text-gray-900">Pay Deposit (₱500)</h4>
                          <p className="text-sm text-gray-600 mt-1">Pay ₱500 deposit now + ₱{(totalPrice - 500).toLocaleString('en-PH', {minimumFractionDigits: 2})} after service</p>
                        </div>
                      </div>
                    </div>
                  </div>
                </div>
                <div className="mt-3 text-xs text-gray-500 bg-gray-50 p-2 rounded-lg">
                  <p className="flex items-center">
                    <svg xmlns="http://www.w3.org/2000/svg" className="h-4 w-4 mr-1 text-amber-500" viewBox="0 0 20 20" fill="currentColor">
                      <path fillRule="evenodd" d="M18 10a8 8 0 11-16 0 8 8 0 0116 0zm-7-4a1 1 0 11-2 0 1 1 0 012 0zM9 9a1 1 0 000 2v3a1 1 0 001 1h1a1 1 0 100-2v-3a1 1 0 00-1-1H9z" clipRule="evenodd" />
                    </svg>
                    For services over ₱1,000, we recommend paying a deposit to secure your booking.
                  </p>
                </div>
              </div>
            )}
          </div>
        </div>

        {/* Payment Summary Card - Enhanced with Visual Elements */}
        <div className="bg-gradient-to-tr from-[#495E57]/5 to-white rounded-xl border border-gray-100 overflow-hidden mb-8 hover:shadow-md transition-all duration-300">
          <div className="bg-[#495E57]/10 px-5 py-3 border-b border-gray-100">
            <h3 className="font-semibold text-[#495E57] flex items-center">
              <svg xmlns="http://www.w3.org/2000/svg" className="h-5 w-5 mr-2" viewBox="0 0 20 20" fill="currentColor">
                <path fillRule="evenodd" d="M4 4a2 2 0 00-2 2v4a2 2 0 002 2V6h10a2 2 0 00-2-2H4zm2 6a2 2 0 012-2h8a2 2 0 012 2v4a2 2 0 01-2 2H8a2 2 0 01-2-2v-4zm6 4a2 2 0 100-4 2 2 0 000 4z" clipRule="evenodd" />
              </svg>
              Payment Summary
            </h3>
          </div>
          <div className="p-5">
            <div className="space-y-3">
              <div className="flex justify-between items-center">
                <div className="flex items-center">
                  <span className="text-gray-600">Service Base Price</span>
                  <div className="group relative ml-2 cursor-help">
                    <svg xmlns="http://www.w3.org/2000/svg" className="h-4 w-4 text-gray-400" viewBox="0 0 20 20" fill="currentColor">
                      <path fillRule="evenodd" d="M18 10a8 8 0 11-16 0 8 8 0 0116 0zm-8-3a1 1 0 00-.867.5 1 1 0 11-1.731-1A3 3 0 0113 8a3.001 3.001 0 01-2 2.83V11a1 1 0 11-2 0v-1a1 1 0 011-1 1 1 0 100-2zm0 8a1 1 0 100-2 1 1 0 000 2z" clipRule="evenodd" />
                    </svg>
                    <div className="opacity-0 group-hover:opacity-100 transition-opacity absolute bottom-full left-1/2 transform -translate-x-1/2 mb-2 w-48 p-2 bg-gray-800 text-white text-xs rounded-lg pointer-events-none">
                      Base price charged by the service provider
                    </div>
                  </div>
                </div>
                <span className="font-medium text-gray-800">₱{serviceData.price.toLocaleString('en-PH', {minimumFractionDigits: 2})}</span>
              </div>
              <div className="flex justify-between items-center">
                <div className="flex items-center">
                  <span className="text-gray-600">Payment Processing Fee (2.5%)</span>
                  <div className="group relative ml-2 cursor-help">
                    <svg xmlns="http://www.w3.org/2000/svg" className="h-4 w-4 text-gray-400" viewBox="0 0 20 20" fill="currentColor">
                      <path fillRule="evenodd" d="M18 10a8 8 0 11-16 0 8 8 0 0116 0zm-8-3a1 1 0 00-.867.5 1 1 0 11-1.731-1A3 3 0 0113 8a3.001 3.001 0 01-2 2.83V11a1 1 0 11-2 0v-1a1 1 0 011-1a1 1 0 100-2zm0 8a1 1 0 100-2 1 1 0 000 2z" clipRule="evenodd" />
                    </svg>
                    <div className="opacity-0 group-hover:opacity-100 transition-opacity absolute bottom-full left-1/2 transform -translate-x-1/2 mb-2 w-48 p-2 bg-gray-800 text-white text-xs rounded-lg pointer-events-none">
                      Fee charged by our payment processor
                    </div>
                  </div>
                </div>
                <span className="font-medium text-gray-800">₱{payMongoFee.toLocaleString('en-PH', {minimumFractionDigits: 2})}</span>
              </div>
              <div className="flex justify-between items-center">
                <div className="flex items-center">
                  <span className="text-gray-600">Platform Fee (2.5%)</span>
                  <div className="group relative ml-2 cursor-help">
                    <svg xmlns="http://www.w3.org/2000/svg" className="h-4 w-4 text-gray-400" viewBox="0 0 20 20" fill="currentColor">
                      <path fillRule="evenodd" d="M18 10a8 8 0 11-16 0 8 8 0 0116 0zm-8-3a1 1 0 00-.867.5 1 1 0 11-1.731-1A3 3 0 0113 8a3.001 3.001 0 01-2 2.83V11a1 1 0 11-2 0v-1a1 1 0 011-1a1 1 0 100-2zm0 8a1 1 0 100-2 1 1 0 000 2z" clipRule="evenodd" />
                    </svg>
                    <div className="opacity-0 group-hover:opacity-100 transition-opacity absolute bottom-full left-1/2 transform -translate-x-1/2 mb-2 w-48 p-2 bg-gray-800 text-white text-xs rounded-lg pointer-events-none">
                      Fee to maintain our platform and services
                    </div>
                  </div>
                </div>
                <span className="font-medium text-gray-800">₱{appFee.toLocaleString('en-PH', {minimumFractionDigits: 2})}</span>
              </div>
              <div className="pt-3 mt-3 border-t border-dashed border-gray-200">
                <div className="flex justify-between items-center">
                  <span className="font-semibold text-gray-800">Total Amount Due</span>
                  <span className="font-bold text-xl text-[#495E57]">₱{totalPrice.toLocaleString('en-PH', {minimumFractionDigits: 2})}</span>
                </div>
                <p className="text-xs text-gray-500 mt-2">
                  *Payment due {paymentMethod === 'cash' ? 'upon completion of service' : 'before service through GCash'}
                </p>
              </div>
            </div>
          </div>
        </div>

        {/* Navigation buttons - Enhanced Design */}
        <div className="mt-8 flex justify-between">
          <button
            onClick={handleBack}
            className="px-6 py-3 bg-gray-100 text-gray-700 rounded-lg font-medium hover:bg-gray-200 transition-all duration-200 flex items-center shadow-sm"
          >
            <svg xmlns="http://www.w3.org/2000/svg" className="h-5 w-5 mr-2" viewBox="0 0 20 20" fill="currentColor">
              <path fillRule="evenodd" d="M9.707 14.707a1 1 0 01-1.414 0l-4-4a1 1 0 010-1.414l4-4a1 1 0 011.414 1.414L7.414 9H15a1 1 0 110 2H7.414l2.293 2.293a1 1 0 010 1.414z" clipRule="evenodd" />
            </svg>
            Back to Schedule
          </button>
          <button
            onClick={handleNext}
            className="px-6 py-3 bg-[#F4CE14] text-[#495E57] rounded-lg font-medium shadow-md hover:bg-[#f3d028] active:bg-[#e5bc12] transition-all duration-200 transform hover:-translate-y-0.5 flex items-center"
          >
            Proceed to Payment
            <svg xmlns="http://www.w3.org/2000/svg" className="h-5 w-5 ml-2" viewBox="0 0 20 20" fill="currentColor">
              <path fillRule="evenodd" d="M10.293 5.293a1 1 0 011.414 0l4 4a1 1 0 010 1.414l-4 4a1 1 0 01-1.414-1.414L12.586 11H5a1 1 0 110-2h7.586l-2.293-2.293a1 1 0 010-1.414z" clipRule="evenodd" />
            </svg>
          </button>
        </div>
      </div>
    </div>
  );
};

export default ReviewBookingDetails;
