import React from "react";

const ReviewBookingDetails = ({
  serviceData,
  bookingDate,
  bookingTime,
  address,
  note,
  setNote,
  paymentMethod,
  setPaymentMethod,
  payMongoFee,
  appFee,
  totalPrice,
  handleBack,
  handleNext,
  formatDateForDisplay,
  formatTimeWithAMPM,
  navigate
}) => {
  return (
    <div className="bg-white rounded-lg shadow-lg overflow-hidden">
      <div className="p-5 bg-[#495E57] text-white">
        <h2 className="text-xl font-semibold">Review Your Booking</h2>
        <p className="text-sm opacity-90">Please review your booking details before proceeding</p>
      </div>

      <div className="p-6">
        {/* Service Details */}
        <div className="mb-8">
          <h3 className="text-lg font-medium text-[#495E57] mb-4 pb-2 border-b">Service Details</h3>
          <div className="flex flex-col md:flex-row gap-4">
            <div className="md:w-1/4">
              {serviceData.imageUrl ? (
                <img 
                  src={serviceData.imageUrl} 
                  alt={serviceData.serviceName} 
                  className="w-full h-32 object-cover rounded-lg shadow-sm" 
                />
              ) : (
                <div className="w-full h-32 bg-gray-200 rounded-lg flex items-center justify-center">
                  <svg className="w-12 h-12 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M4 16l4.586-4.586a2 2 0 012.828 0L16 16m-2-2l1.586-1.586a2 2 0 012.828 0L20 14m-6-6h.01M6 20h12a2 2 0 002-2V6a2 2 0 00-2-2H6a2 2 0 00-2 2v12a2 2 0 002 2z" />
                  </svg>
                </div>
              )}
            </div>
            <div className="md:w-3/4">
              <h4 className="text-xl font-semibold">{serviceData.serviceName}</h4>
              <p className="text-gray-600 mt-1">{serviceData.serviceDescription || "No description provided."}</p>
              
              <div className="mt-3 flex flex-wrap gap-2">
                {serviceData.category && (
                  <span className="px-2 py-1 bg-gray-100 text-gray-600 text-xs rounded-full">
                    {serviceData.category.categoryName}
                  </span>
                )}
                {serviceData.duration && (
                  <span className="px-2 py-1 bg-gray-100 text-gray-600 text-xs rounded-full">
                    Est. duration: {serviceData.duration}
                  </span>
                )}
              </div>
              
              <div className="mt-3">
                <p className="font-medium">Provider: <span className="font-normal">{serviceData.provider?.firstName || ""} {serviceData.provider?.lastName || ""}</span></p>
              </div>
            </div>
          </div>
        </div>

        {/* Booking Details */}
        <div className="mb-8">
          <h3 className="text-lg font-medium text-[#495E57] mb-4 pb-2 border-b">Booking Details</h3>
          
          <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
            <div>
              <div className="mb-4">
                <h4 className="font-medium text-gray-700 mb-1">Date</h4>
                <div className="p-3 bg-gray-50 border border-gray-200 rounded-lg flex items-center">
                  <svg className="w-5 h-5 text-[#495E57] mr-3" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z" />
                  </svg>
                  <span>{formatDateForDisplay(bookingDate)}</span>
                </div>
              </div>
              
              <div className="mb-4">
                <h4 className="font-medium text-gray-700 mb-1">Time</h4>
                <div className="p-3 bg-gray-50 border border-gray-200 rounded-lg flex items-center">
                  <svg className="w-5 h-5 text-[#495E57] mr-3" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z" />
                  </svg>
                  <span>
                    {bookingTime ? 
                      formatTimeWithAMPM(bookingTime.split('-')[0]) + ' - ' + formatTimeWithAMPM(bookingTime.split('-')[1]) 
                      : 'No time selected'}
                  </span>
                </div>
              </div>
            </div>
            
            <div>
              <h4 className="font-medium text-gray-700 mb-1">Service Location</h4>
              <div className="p-3 bg-gray-50 border border-gray-200 rounded-lg">
                <div className="flex items-start">
                  <svg className="w-5 h-5 text-[#495E57] mr-3 mt-1" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M17.657 16.657L13.414 20.9a1.998 1.998 0 01-2.827 0l-4.244-4.243a8 8 0 1111.314 0z" />
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M15 11a3 3 0 11-6 0 3 3 0 016 0z" />
                  </svg>
                  <div>
                    <p className="text-gray-700">{address || "No address available"}</p>
                    <button
                      onClick={() => navigate('/customerProfile/address')}
                      className="text-sm text-[#495E57] hover:underline mt-1"
                    >
                      Change address
                    </button>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
        
        {/* Special Instructions */}
        <div className="mb-8">
          <h3 className="text-lg font-medium text-[#495E57] mb-4 pb-2 border-b">Special Instructions</h3>
          <textarea
            value={note}
            onChange={(e) => setNote(e.target.value)}
            placeholder="Add any special instructions or notes for the service provider..."
            className="w-full p-3 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-[#495E57] min-h-[100px]"
          />
        </div>

        {/* Payment Method Selection */}
        <div className="mb-8">
          <h3 className="text-lg font-medium text-[#495E57] mb-4 pb-2 border-b">Payment Method</h3>
          <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
            {/* Cash Option */}
            <div 
              className={`border ${
                paymentMethod === 'cash' 
                  ? 'border-[#F4CE14] bg-yellow-50' 
                  : 'border-gray-200 bg-white'
              } rounded-lg p-4 cursor-pointer transition-all hover:shadow-md`}
              onClick={() => setPaymentMethod('cash')}
            >
              <div className="flex items-center">
                <div className={`w-6 h-6 rounded-full border-2 mr-3 flex items-center justify-center ${
                  paymentMethod === 'cash' ? 'border-[#495E57]' : 'border-gray-300'
                }`}>
                  {paymentMethod === 'cash' && (
                    <div className="w-3 h-3 bg-[#495E57] rounded-full"></div>
                  )}
                </div>
                <div className="flex items-center justify-between flex-1">
                  <div className="flex items-center">
                    <div className="h-10 w-10 bg-[#495E57] bg-opacity-10 rounded-full flex items-center justify-center mr-3">
                      <svg className="w-5 h-5 text-[#495E57]" fill="none" stroke="currentColor" viewBox="0 0 24 24" xmlns="http://www.w3.org/2000/svg">
                        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M17 9V7a2 2 0 00-2-2H5a2 2 0 00-2 2v6a2 2 0 002 2h2m2 4h10a2 2 0 002-2v-6a2 2 0 00-2-2H9a2 2 0 00-2 2v6a2 2 0 002 2zm7-5a2 2 0 11-4 0 2 2 0 014 0z"></path>
                      </svg>
                    </div>
                    <div>
                      <p className="font-medium text-gray-800">Cash</p>
                      <p className="text-xs text-gray-500">Pay with cash when service is complete</p>
                    </div>
                  </div>
                </div>
              </div>
            </div>
            
            {/* GCash Option */}
            <div 
              className={`border ${
                paymentMethod === 'gcash' 
                  ? 'border-[#F4CE14] bg-blue-50' 
                  : 'border-gray-200 bg-white'
              } rounded-lg p-4 cursor-pointer transition-all hover:shadow-md`}
              onClick={() => setPaymentMethod('gcash')}
            >
              <div className="flex items-center">
                <div className={`w-6 h-6 rounded-full border-2 mr-3 flex items-center justify-center ${
                  paymentMethod === 'gcash' ? 'border-[#495E57]' : 'border-gray-300'
                }`}>
                  {paymentMethod === 'gcash' && (
                    <div className="w-3 h-3 bg-[#495E57] rounded-full"></div>
                  )}
                </div>
                <div className="flex items-center justify-between flex-1">
                  <div className="flex items-center">
                    <div className="h-10 w-10 bg-blue-500 bg-opacity-10 rounded-full flex items-center justify-center mr-3">
                      <svg className="w-5 h-5 text-blue-500" fill="none" stroke="currentColor" viewBox="0 0 24 24" xmlns="http://www.w3.org/2000/svg">
                        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M12 18h.01M8 21h8a2 2 0 002-2V5a2 2 0 00-2-2H8a2 2 0 00-2 2v14a2 2 0 002 2z"></path>
                      </svg>
                    </div>
                    <div>
                      <p className="font-medium text-gray-800">GCash</p>
                      <p className="text-xs text-gray-500">Pay using GCash mobile payment</p>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>

        {/* Payment Summary */}
        <div className="mb-8">
          <h3 className="text-lg font-medium text-[#495E57] mb-4 pb-2 border-b">Payment Summary</h3>
          <div className="bg-gray-50 p-4 rounded-lg border border-gray-200">
            <div className="flex justify-between mb-2">
              <span className="text-gray-600">{serviceData.serviceName} Price:</span>
              <span className="font-medium">₱{serviceData.price.toLocaleString()}</span>
            </div>
            <div className="flex justify-between mb-2">
              <span className="text-gray-600">PayMongo Fee (2.5%):</span>
              <span className="font-medium">₱{payMongoFee.toLocaleString()}</span>
            </div>
            <div className="flex justify-between mb-2">
              <span className="text-gray-600">App Fee (2.5%):</span>
              <span className="font-medium">₱{appFee.toLocaleString()}</span>
            </div>
            <div className="flex justify-between text-lg font-bold mt-2 pt-2 border-t">
              <span className="text-gray-800">Total:</span>
              <span className="text-[#495E57]">₱{totalPrice.toLocaleString()}</span>
            </div>
          </div>
        </div>

        {/* Navigation buttons */}
        <div className="mt-8 flex justify-between">
          <button
            onClick={handleBack}
            className="px-6 py-3 bg-gray-100 text-gray-700 rounded-lg font-medium hover:bg-gray-200 transition-colors"
          >
            Back
          </button>
          <button
            onClick={handleNext}
            className="px-6 py-3 bg-[#F4CE14] text-[#495E57] rounded-lg font-medium shadow-sm hover:bg-yellow-300 transition-colors"
          >
            Proceed to Payment
          </button>
        </div>
      </div>
    </div>
  );
};

export default ReviewBookingDetails;
