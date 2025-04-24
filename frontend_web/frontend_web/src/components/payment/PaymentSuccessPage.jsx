import React, { useEffect, useState } from 'react';
import { useLocation, useNavigate } from 'react-router-dom';
import axios from 'axios';

const PaymentSuccessPage = () => {
  const navigate = useNavigate();
  const location = useLocation();
  const [isLoading, setIsLoading] = useState(true);
  const [message, setMessage] = useState('Payment successful!');
  const [error, setError] = useState(null);
  
  useEffect(() => {
    // This function will only run once per page load
    const processPaymentOnce = async () => {
      try {
        // Check if we've already processed this payment
        const isProcessed = sessionStorage.getItem('paymentProcessed');
        if (isProcessed) {
          setMessage('Your booking has already been processed.');
          setIsLoading(false);
          return;
        }

        // Check if there's a pending booking from GCash flow
        const pendingBooking = sessionStorage.getItem('pendingBooking');
        if (!pendingBooking) {
          // If there's no pending booking but we have booking data in location state
          // (from cash payment flow), just show success
          if (location.state?.bookingData) {
            setMessage('Your booking has been confirmed!');
          } else {
            setMessage('Payment successful!');
          }
          setIsLoading(false);
          return;
        }

        // Mark as being processed immediately to prevent duplicate processing
        sessionStorage.setItem('paymentProcessed', 'true');
        
        setMessage('Processing your booking...');
        
        // Parse the booking request
        const bookingRequest = JSON.parse(pendingBooking);
        
        // Add an idempotency key based on timestamp and service to help prevent duplicates
        const idempotencyKey = `booking_${bookingRequest.customer.customerId}_${bookingRequest.service.serviceId}_${Date.now()}`;
        bookingRequest.idempotencyKey = idempotencyKey;
        
        // Submit the booking to the backend
        const token = localStorage.getItem("authToken") || sessionStorage.getItem("authToken");
        const response = await axios.post('/api/bookings/postBooking', bookingRequest, {
          headers: { 
            Authorization: `Bearer ${token}`,
            'X-Idempotency-Key': idempotencyKey // Add idempotency header
          }
        });
        
        console.log('Booking created after successful payment:', response.data);
        
        // Clear the pending booking from session storage
        sessionStorage.removeItem('pendingBooking');
        
        setMessage('Your payment was successful and your booking has been confirmed!');
      } catch (error) {
        console.error('Error processing booking after payment:', error);
        setError('We received your payment, but there was an issue creating your booking. Please contact support.');
      } finally {
        setIsLoading(false);
      }
    };
    
    processPaymentOnce();
    
    // Cleanup function to clear the processed flag when navigating away
    return () => {
      // Only clear if we're actually navigating away, not on initial render
      setTimeout(() => {
        sessionStorage.removeItem('paymentProcessed');
      }, 1000);
    };
  }, []); // Empty dependency array means this effect runs only once when component mounts
  
  return (
    <div className="min-h-screen bg-gray-50 flex flex-col items-center justify-center p-4">
      <div className="bg-white rounded-lg shadow-md p-8 max-w-lg w-full text-center">
        {isLoading ? (
          <div className="flex flex-col items-center">
            <div className="animate-spin rounded-full h-16 w-16 border-t-2 border-b-2 border-[#495E57]"></div>
            <p className="mt-4 text-lg text-gray-600">Processing your payment...</p>
          </div>
        ) : error ? (
          <div>
            <div className="mx-auto flex items-center justify-center h-20 w-20 rounded-full bg-red-100 mb-6">
              <svg className="h-10 w-10 text-red-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M6 18L18 6M6 6l12 12"></path>
              </svg>
            </div>
            <h2 className="text-2xl font-bold text-red-600 mb-4">Payment Issue</h2>
            <p className="text-gray-600 mb-6">{error}</p>
            <button 
              onClick={() => navigate('/dashboard')}
              className="bg-[#495E57] text-white px-6 py-2 rounded-md hover:bg-[#3a4a43] transition-colors"
            >
              Return to Dashboard
            </button>
          </div>
        ) : (
          <div>
            <div className="mx-auto flex items-center justify-center h-20 w-20 rounded-full bg-green-100 mb-6">
              <svg className="h-10 w-10 text-green-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M5 13l4 4L19 7"></path>
              </svg>
            </div>
            <h2 className="text-2xl font-bold text-[#495E57] mb-4">Payment Successful</h2>
            <p className="text-gray-600 mb-6">{message}</p>
            <div className="flex flex-col sm:flex-row justify-center gap-4">
              <button 
                onClick={() => navigate('/customerProfile/bookingHistory')}
                className="bg-[#495E57] text-white px-6 py-2 rounded-md hover:bg-[#3a4a43] transition-colors"
              >
                Booking History
              </button>
              <button 
                onClick={() => navigate('/browseServices')}
                className="bg-[#F4CE14] text-[#495E57] px-6 py-2 rounded-md hover:bg-yellow-300 transition-colors"
              >
                Browse More Services
              </button>
            </div>
          </div>
        )}
      </div>
    </div>
  );
};

export default PaymentSuccessPage;
