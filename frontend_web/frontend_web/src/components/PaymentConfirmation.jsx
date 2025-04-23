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
    <div className="bg-white rounded-lg shadow-lg p-6">
      <h2 className="text-xl font-semibold text-[#495E57] mb-4">Payment</h2>
      
      {/* Add detailed payment summary here as well */}
      <div className="bg-gray-50 p-4 rounded-lg border border-gray-200 mb-6">
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
          <span className="text-gray-800">Total Amount:</span>
          <span className="text-[#495E57]">₱{totalPrice.toLocaleString()}</span>
        </div>
      </div>
      
      {/* Debug information section - only show in development */}
      {process.env.NODE_ENV !== 'production' && debugInfo && (
        <div className="mt-4 p-4 bg-gray-50 border border-gray-300 rounded-lg overflow-auto max-h-60">
          <h3 className="font-medium text-gray-700 mb-2">Debug Information:</h3>
          <pre className="text-xs text-gray-600 whitespace-pre-wrap">
            {JSON.stringify(debugInfo, null, 2)}
          </pre>
        </div>
      )}
      
      {/* Navigation buttons for step 3 */}
      <div className="mt-8 flex justify-between">
        <button
          onClick={handleBack}
          className="px-6 py-3 bg-gray-100 text-gray-700 rounded-lg font-medium hover:bg-gray-200 transition-colors"
        >
          Back
        </button>
        <button
          onClick={handleSubmit}
          disabled={isProcessingPayment}
          className={`px-6 py-3 bg-[#F4CE14] text-[#495E57] rounded-lg font-medium shadow-sm hover:bg-yellow-300 transition-colors
            ${isProcessingPayment ? 'opacity-50 cursor-not-allowed' : ''}
          `}
        >
          {isProcessingPayment ? 'Processing...' : 'Complete Booking'}
        </button>
      </div>
    </div>
  );
};

export default PaymentConfirmation;
