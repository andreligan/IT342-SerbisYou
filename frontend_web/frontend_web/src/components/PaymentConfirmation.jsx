import React from "react";

const PaymentConfirmation = ({
  serviceData,
  payMongoFee,
  appFee,
  totalPrice,
  debugInfo,
  isProcessingPayment,
  handleBack,
  handleSubmit
}) => {
  return (
    <div className="bg-white rounded-2xl shadow-xl overflow-hidden">
      {/* Header with gradient background */}
      <div className="relative bg-gradient-to-r from-[#445954] to-[#495E57] text-white p-6">
        <div className="absolute top-0 right-0 w-64 h-64 bg-[#F4CE14]/10 rounded-full -translate-x-16 -translate-y-32 blur-3xl"></div>
        <div className="absolute bottom-0 left-1/4 w-40 h-40 bg-[#F4CE14]/10 rounded-full -translate-y-10 blur-2xl"></div>
        
        <div className="relative z-10">
          <div className="flex items-center mb-4">
            <div className="h-10 w-10 bg-[#F4CE14]/20 rounded-full flex items-center justify-center mr-3">
              <svg xmlns="http://www.w3.org/2000/svg" className="h-5 w-5 text-[#F4CE14]" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M17 9V7a2 2 0 00-2-2H5a2 2 0 00-2 2v6a2 2 0 002 2h2m2 4h10a2 2 0 002-2v-6a2 2 0 00-2-2H9a2 2 0 00-2 2v6a2 2 0 002 2zm7-5a2 2 0 11-4 0 2 2 0 014 0z" />
              </svg>
            </div>
            <div>
              <div className="text-[#F4CE14] text-sm font-medium">Step 3 of 3</div>
              <h2 className="text-2xl font-bold">Complete Payment</h2>
            </div>
          </div>
          <p className="text-sm text-white/80 ml-13 pl-[52px]">
            Review your payment details and confirm to complete your booking
          </p>
        </div>
      </div>

      <div className="p-6 md:p-8">
        {/* Service Summary Card */}
        <div className="bg-gray-50 rounded-xl border border-gray-200 overflow-hidden mb-6">
          <div className="bg-[#495E57]/5 px-4 py-3 border-b border-gray-100">
            <h3 className="font-semibold text-[#495E57] flex items-center">
              <svg xmlns="http://www.w3.org/2000/svg" className="h-5 w-5 mr-2" viewBox="0 0 20 20" fill="currentColor">
                <path fillRule="evenodd" d="M6 6V5a3 3 0 013-3h2a3 3 0 013 3v1h2a2 2 0 012 2v3.57A22.952 22.952 0 0110 13a22.95 22.95 0 01-8-1.43V8a2 2 0 012-2h2zm2-1a1 1 0 011-1h2a1 1 0 011 1v1H8V5zm1 5a1 1 0 011-1h.01a1 1 0 110 2H10a1 1 0 01-1-1z" clipRule="evenodd" />
                <path d="M2 13.692V16a2 2 0 002 2h12a2 2 0 002-2v-2.308A24.974 24.974 0 0110 15c-2.796 0-5.487-.46-8-1.308z" />
              </svg>
              Service Summary
            </h3>
          </div>
          
          <div className="px-5 py-4">
            <div className="flex items-center justify-between mb-3">
              <span className="text-gray-700 font-medium">{serviceData.serviceName}</span>
              <span className="text-gray-900 font-bold">₱{parseFloat(serviceData.price).toLocaleString('en-PH', {minimumFractionDigits: 2})}</span>
            </div>
            
            <div className="text-sm text-gray-500 mb-2">
              <div className="mb-1">
                {serviceData.provider?.firstName} {serviceData.provider?.lastName}
              </div>
              {serviceData.categoryName && (
                <span className="inline-block bg-gray-100 text-gray-600 rounded-full px-2.5 py-0.5 text-xs">
                  {serviceData.categoryName}
                </span>
              )}
            </div>
          </div>
        </div>
        
        {/* Payment Details Card */}
        <div className="bg-white rounded-xl shadow-sm border border-gray-200 overflow-hidden mb-6">
          <div className="bg-[#495E57]/5 px-4 py-3 border-b border-gray-100">
            <h3 className="font-semibold text-[#495E57] flex items-center">
              <svg xmlns="http://www.w3.org/2000/svg" className="h-5 w-5 mr-2" viewBox="0 0 20 20" fill="currentColor">
                <path d="M4 4a2 2 0 00-2 2v1h16V6a2 2 0 00-2-2H4z" />
                <path fillRule="evenodd" d="M18 9H2v5a2 2 0 002 2h12a2 2 0 002-2V9zM4 13a1 1 0 011-1h1a1 1 0 110 2H5a1 1 0 01-1-1zm5-1a1 1 0 100 2h1a1 1 0 100-2H9z" clipRule="evenodd" />
              </svg>
              Payment Details
            </h3>
          </div>
          
          <div className="p-5">
            {/* Payment breakdown */}
            <div className="space-y-3 mb-5">
              <div className="flex justify-between items-center">
                <div className="flex items-center">
                  <span className="text-gray-600">Service Base Price</span>
                  <div className="group relative ml-2 cursor-help">
                    <svg xmlns="http://www.w3.org/2000/svg" className="h-4 w-4 text-gray-400" viewBox="0 0 20 20" fill="currentColor">
                      <path fillRule="evenodd" d="M18 10a8 8 0 11-16 0 8 8 0 0116 0zm-8-3a1 1 0 00-.867.5 1 1 0 11-1.731-1A3 3 0 0113 8a3.001 3.001 0 01-2 2.83V11a1 1 0 11-2 0v-1a1 1 0 011-1 1 1 0 100-2zm0 8a1 1 0 100-2 1 1 0 000 2z" clipRule="evenodd" />
                    </svg>
                    <div className="opacity-0 group-hover:opacity-100 transition-opacity absolute bottom-full left-1/2 transform -translate-x-1/2 mb-2 w-48 p-2 bg-gray-800 text-white text-xs rounded-lg pointer-events-none z-10">
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
                      <path fillRule="evenodd" d="M18 10a8 8 0 11-16 0 8 8 0 0116 0zm-8-3a1 1 0 00-.867.5 1 1 0 11-1.731-1A3 3 0 0113 8a3.001 3.001 0 01-2 2.83V11a1 1 0 11-2 0v-1a1 1 0 011-1 1 1 0 100-2zm0 8a1 1 0 100-2 1 1 0 000 2z" clipRule="evenodd" />
                    </svg>
                    <div className="opacity-0 group-hover:opacity-100 transition-opacity absolute bottom-full left-1/2 transform -translate-x-1/2 mb-2 w-48 p-2 bg-gray-800 text-white text-xs rounded-lg pointer-events-none z-10">
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
                      <path fillRule="evenodd" d="M18 10a8 8 0 11-16 0 8 8 0 0116 0zm-8-3a1 1 0 00-.867.5 1 1 0 11-1.731-1A3 3 0 0113 8a3.001 3.001 0 01-2 2.83V11a1 1 0 11-2 0v-1a1 1 0 011-1 1 1 0 100-2zm0 8a1 1 0 100-2 1 1 0 000 2z" clipRule="evenodd" />
                    </svg>
                    <div className="opacity-0 group-hover:opacity-100 transition-opacity absolute bottom-full left-1/2 transform -translate-x-1/2 mb-2 w-48 p-2 bg-gray-800 text-white text-xs rounded-lg pointer-events-none z-10">
                      Fee to maintain our platform and services
                    </div>
                  </div>
                </div>
                <span className="font-medium text-gray-800">₱{appFee.toLocaleString('en-PH', {minimumFractionDigits: 2})}</span>
              </div>
            </div>
            
            {/* Total with highlight */}
            <div className="bg-gradient-to-r from-[#F4CE14]/10 to-transparent p-4 rounded-lg">
              <div className="flex justify-between items-center">
                <span className="font-bold text-gray-800">Total Amount Due</span>
                <span className="text-xl font-bold text-[#495E57]">₱{totalPrice.toLocaleString('en-PH', {minimumFractionDigits: 2})}</span>
              </div>
            </div>
          </div>
        </div>
        
        {/* Payment Security Information */}
        <div className="bg-blue-50 border-l-4 border-blue-400 p-4 rounded-r-lg mb-6 flex items-start">
          <svg xmlns="http://www.w3.org/2000/svg" className="h-6 w-6 text-blue-500 mt-0.5 mr-3 flex-shrink-0" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M13 16h-1v-4h-1m1-4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
          </svg>
          <div>
            <p className="text-sm text-blue-700 font-medium mb-1">What happens next?</p>
            <p className="text-sm text-blue-600">
              After confirming your booking, you'll receive a confirmation email with all the details.
              The service provider will contact you to coordinate the service delivery.
            </p>
          </div>
        </div>
        
        {/* Security Badges */}
        <div className="flex flex-wrap items-center justify-center gap-3 mb-6 text-gray-400 text-xs">
          <div className="flex items-center">
            <svg xmlns="http://www.w3.org/2000/svg" className="h-5 w-5 mr-1" viewBox="0 0 20 20" fill="currentColor">
              <path fillRule="evenodd" d="M2.166 4.999A11.954 11.954 0 0010 1.944 11.954 11.954 0 0017.834 5c.11.65.166 1.32.166 2.001 0 5.225-3.34 9.67-8 11.317C5.34 16.67 2 12.225 2 7c0-.682.057-1.35.166-2.001zm11.541 3.708a1 1 0 00-1.414-1.414L9 10.586 7.707 9.293a1 1 0 00-1.414 1.414l2 2a1 1 0 001.414 0l4-4z" clipRule="evenodd" />
            </svg>
            SECURE PAYMENT
          </div>
          <div className="flex items-center">
            <svg xmlns="http://www.w3.org/2000/svg" className="h-5 w-5 mr-1" viewBox="0 0 20 20" fill="currentColor">
              <path fillRule="evenodd" d="M5 9V7a5 5 0 0110 0v2a2 2 0 012 2v5a2 2 0 01-2 2H5a2 2 0 01-2-2v-5a2 2 0 012-2zm8-2v2H7V7a3 3 0 016 0z" clipRule="evenodd" />
            </svg>
            ENCRYPTED DATA
          </div>
          <div className="flex items-center">
            <svg xmlns="http://www.w3.org/2000/svg" className="h-5 w-5 mr-1" viewBox="0 0 20 20" fill="currentColor">
              <path d="M13 6a3 3 0 11-6 0 3 3 0 016 0zM18 8a2 2 0 11-4 0 2 2 0 014 0zM14 15a4 4 0 00-8 0v3h8v-3zM6 8a2 2 0 11-4 0 2 2 0 014 0zM16 18v-3a5.972 5.972 0 00-.75-2.906A3.005 3.005 0 0119 15v3h-3zM4.75 12.094A5.973 5.973 0 004 15v3H1v-3a3 3 0 013.75-2.906z" />
            </svg>
            TRUSTED PROVIDERS
          </div>
        </div>
        
        {/* Debug information section - only show in development */}
        {process.env.NODE_ENV !== 'production' && debugInfo && (
          <div className="mt-4 p-4 bg-gray-50 border border-gray-300 rounded-lg overflow-auto max-h-60">
            <h3 className="font-medium text-gray-700 mb-2 text-xs uppercase tracking-wide">Debug Information:</h3>
            <pre className="text-xs text-gray-600 whitespace-pre-wrap">
              {JSON.stringify(debugInfo, null, 2)}
            </pre>
          </div>
        )}
        
        {/* Navigation buttons */}
        <div className="mt-8 flex justify-between">
          <button
            onClick={handleBack}
            className="px-6 py-3 bg-gray-100 text-gray-700 rounded-lg font-medium hover:bg-gray-200 transition-colors flex items-center"
          >
            <svg xmlns="http://www.w3.org/2000/svg" className="h-5 w-5 mr-2" viewBox="0 0 20 20" fill="currentColor">
              <path fillRule="evenodd" d="M9.707 14.707a1 1 0 01-1.414 0l-4-4a1 1 0 010-1.414l4-4a1 1 0 011.414 1.414L7.414 9H15a1 1 0 110 2H7.414l2.293 2.293a1 1 0 010 1.414z" clipRule="evenodd" />
            </svg>
            Back
          </button>
          <button
            onClick={handleSubmit}
            disabled={isProcessingPayment}
            className={`px-6 py-3 bg-[#F4CE14] text-[#495E57] rounded-lg font-medium shadow-md hover:bg-yellow-300 active:bg-yellow-400 transition-all duration-200 transform hover:-translate-y-0.5 flex items-center
              ${isProcessingPayment ? 'opacity-50 cursor-not-allowed hover:transform-none' : ''}
            `}
          >
            {isProcessingPayment ? (
              <>
                <svg className="animate-spin -ml-1 mr-2 h-5 w-5 text-[#495E57]" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24">
                  <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4"></circle>
                  <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
                </svg>
                Processing...
              </>
            ) : (
              <>
                Complete Booking
                <svg xmlns="http://www.w3.org/2000/svg" className="h-5 w-5 ml-2" viewBox="0 0 20 20" fill="currentColor">
                  <path fillRule="evenodd" d="M10.293 5.293a1 1 0 011.414 0l4 4a1 1 0 010 1.414l-4 4a1 1 0 01-1.414-1.414L12.586 11H5a1 1 0 110-2h7.586l-2.293-2.293a1 1 0 010-1.414z" clipRule="evenodd" />
                </svg>
              </>
            )}
          </button>
        </div>
      </div>
    </div>
  );
};

export default PaymentConfirmation;
