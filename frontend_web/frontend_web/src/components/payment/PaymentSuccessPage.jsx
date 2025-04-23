import React, { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';

const PaymentSuccessPage = () => {
  const navigate = useNavigate();
  const [isProcessing, setIsProcessing] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    const processSuccessfulPayment = async () => {
      try {
        // Retrieve booking data from localStorage
        const pendingBookingString = localStorage.getItem('pendingBooking');
        if (!pendingBookingString) {
          setError("No pending booking found.");
          setIsProcessing(false);
          return;
        }

        // Simply mark payment as successful without making another booking
        console.log("Payment processed successfully");
        
        // Clean up the localStorage
        localStorage.removeItem('pendingBooking');
        
        setIsProcessing(false);
        
        // After 2 seconds, redirect to customer home page
        setTimeout(() => {
          navigate('/customerHomePage');
        }, 2000);
        
      } catch (error) {
        console.error("Error processing payment:", error);
        setError("Failed to process payment. Please contact support.");
        setIsProcessing(false);
      }
    };

    processSuccessfulPayment();
  }, [navigate]);

  return (
    <div className="min-h-screen flex items-center justify-center bg-gray-50">
      <div className="bg-white p-8 rounded-lg shadow-lg max-w-md w-full">
        {isProcessing ? (
          <div className="text-center">
            <div className="animate-spin rounded-full h-12 w-12 border-t-2 border-b-2 border-[#495E57] mx-auto"></div>
            <p className="mt-4 text-gray-600">Processing your payment...</p>
          </div>
        ) : error ? (
          <div className="text-center">
            <div className="text-red-500 text-5xl mb-4">
              <i className="fas fa-exclamation-circle"></i>
            </div>
            <h2 className="text-2xl font-bold text-gray-800 mb-2">Payment Error</h2>
            <p className="text-gray-600 mb-6">{error}</p>
            <button 
              onClick={() => navigate('/customerHomePage')} 
              className="bg-[#495E57] text-white py-2 px-6 rounded-lg hover:bg-[#3A4A47] transition"
            >
              Go to Home
            </button>
          </div>
        ) : (
          <div className="text-center">
            <div className="text-green-500 text-5xl mb-4">
              <i className="fas fa-check-circle"></i>
            </div>
            <h2 className="text-2xl font-bold text-[#495E57] mb-2">Payment Successful!</h2>
            <p className="text-gray-600 mb-6">Your booking has been confirmed.</p>
            <button 
              onClick={() => navigate('/customerHomePage')} 
              className="bg-[#495E57] text-white py-2 px-6 rounded-lg hover:bg-[#3A4A47] transition"
            >
              Go to Home
            </button>
          </div>
        )}
      </div>
    </div>
  );
};

export default PaymentSuccessPage;
