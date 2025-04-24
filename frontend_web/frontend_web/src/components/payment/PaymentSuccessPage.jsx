import React, { useEffect, useState, useRef } from 'react';
import { useLocation, useNavigate } from 'react-router-dom';
import axios from 'axios';
import confetti from 'canvas-confetti';

const PaymentSuccessPage = () => {
  const navigate = useNavigate();
  const location = useLocation();
  const [isLoading, setIsLoading] = useState(true);
  const [message, setMessage] = useState('Payment successful!');
  const [error, setError] = useState(null);
  const [bookingDetails, setBookingDetails] = useState(null);
  const confettiCanvasRef = useRef(null);
  
  // Launch confetti when payment success is confirmed
  useEffect(() => {
    if (!isLoading && !error) {
      const duration = 3 * 1000;
      const end = Date.now() + duration;
      
      const runConfetti = () => {
        confetti({
          particleCount: 2,
          angle: 60,
          spread: 55,
          origin: { x: 0 },
          colors: ['#F4CE14', '#495E57']
        });
        
        confetti({
          particleCount: 2,
          angle: 120,
          spread: 55,
          origin: { x: 1 },
          colors: ['#F4CE14', '#495E57']
        });

        if (Date.now() < end) {
          requestAnimationFrame(runConfetti);
        }
      };
      
      runConfetti();
    }
  }, [isLoading, error]);
  
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
            setBookingDetails(location.state.bookingData);
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
        setBookingDetails(response.data);
        
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
  
  // Format date function
  const formatDate = (dateString) => {
    if (!dateString) return 'Not available';
    const date = new Date(dateString);
    return new Intl.DateTimeFormat('en-US', {
      weekday: 'long',
      year: 'numeric',
      month: 'long',
      day: 'numeric'
    }).format(date);
  };
  
  // Format time function
  const formatTime = (timeString) => {
    if (!timeString) return 'Not available';
    // Handle common time formats
    const timeParts = timeString.split(':');
    if (timeParts.length < 2) return timeString;
    
    const hour = parseInt(timeParts[0], 10);
    const minutes = timeParts[1];
    const period = hour >= 12 ? 'PM' : 'AM';
    const formattedHour = hour % 12 || 12;
    
    return `${formattedHour}:${minutes} ${period}`;
  };
  
  return (
    <div className="min-h-screen bg-gray-50 py-12">
      <div className="max-w-4xl mx-auto px-4">
        {isLoading ? (
          <div className="bg-white rounded-2xl shadow-xl p-8 flex flex-col items-center justify-center">
            <div className="relative">
              <div className="w-24 h-24 border-t-4 border-b-4 border-[#F4CE14] rounded-full animate-spin"></div>
              <div className="absolute inset-0 flex items-center justify-center">
                <div className="w-16 h-16 bg-white rounded-full"></div>
              </div>
              <div className="absolute inset-0 flex items-center justify-center">
                <svg xmlns="http://www.w3.org/2000/svg" className="h-8 w-8 text-[#495E57]" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z" />
                </svg>
              </div>
            </div>
            <p className="mt-6 text-xl text-gray-700 font-medium">Processing your payment...</p>
            <p className="mt-2 text-gray-500">This will only take a moment.</p>
          </div>
        ) : error ? (
          <div className="bg-white rounded-2xl shadow-xl overflow-hidden">
            <div className="bg-red-50 p-6 border-b border-red-100">
              <div className="flex flex-col sm:flex-row items-center">
                <div className="flex items-center justify-center h-16 w-16 rounded-full bg-red-100 flex-shrink-0 mb-4 sm:mb-0">
                  <svg className="h-8 w-8 text-red-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M12 8v4m0 4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z"></path>
                  </svg>
                </div>
                <div className="sm:ml-6 text-center sm:text-left">
                  <h2 className="text-2xl font-bold text-red-800">Payment Issue</h2>
                  <p className="mt-1 text-red-700">We encountered a problem with your booking</p>
                </div>
              </div>
            </div>
            
            <div className="p-6">
              <div className="bg-red-50 border-l-4 border-red-500 p-4 rounded-md mb-6">
                <p className="text-red-700">{error}</p>
              </div>
              
              <p className="text-gray-600 mb-6">
                Your payment was processed successfully, but we couldn't complete your booking. 
                Please contact our support team with the following information:
              </p>
              
              <ul className="list-disc list-inside mb-8 text-gray-600">
                <li>Payment Time: {new Date().toLocaleString()}</li>
                <li>Error Type: Booking Creation Failed</li>
                <li>Support Email: support@serbisyo.com</li>
              </ul>
              
              <div className="flex flex-col sm:flex-row gap-4 justify-center sm:justify-start">
                <button 
                  onClick={() => navigate('/customerProfile/bookingHistory')}
                  className="bg-gray-100 hover:bg-gray-200 text-gray-800 px-6 py-3 rounded-lg transition-colors font-medium flex items-center justify-center"
                >
                  <svg xmlns="http://www.w3.org/2000/svg" className="h-5 w-5 mr-2" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M19 9l-7 7-7-7" />
                  </svg>
                  View Bookings
                </button>
                <button 
                  onClick={() => navigate('/customerHomePage')}
                  className="bg-[#495E57] hover:bg-[#3a4a43] text-white px-6 py-3 rounded-lg transition-colors font-medium flex items-center justify-center"
                >
                  <svg xmlns="http://www.w3.org/2000/svg" className="h-5 w-5 mr-2" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M3 12l2-2m0 0l7-7 7 7m-7-7v14" />
                  </svg>
                  Home
                </button>
              </div>
            </div>
          </div>
        ) : (
          <div className="bg-white rounded-2xl shadow-xl overflow-hidden">
            {/* Celebratory Header */}
            <div className="relative bg-gradient-to-r from-[#495E57] to-[#37423C] p-8 text-white overflow-hidden">
              <div className="absolute top-0 right-0 w-64 h-64 bg-[#F4CE14]/10 rounded-full -translate-x-24 -translate-y-24 blur-3xl"></div>
              <div className="absolute bottom-0 left-0 w-64 h-64 bg-[#F4CE14]/5 rounded-full translate-x-8 translate-y-16 blur-2xl"></div>
              
              <div className="relative flex flex-col items-center sm:items-start sm:flex-row">
                <div className="bg-white/10 backdrop-blur-sm w-24 h-24 rounded-full border-2 border-[#F4CE14] flex items-center justify-center mb-4 sm:mb-0">
                  <svg xmlns="http://www.w3.org/2000/svg" className="h-12 w-12 text-[#F4CE14]" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z" />
                  </svg>
                </div>
                <div className="sm:ml-6 text-center sm:text-left">
                  <div className="inline-block px-3 py-1 rounded-full bg-[#F4CE14]/20 mb-3 backdrop-blur-sm">
                    <span className="text-[#F4CE14] text-sm font-medium">Payment Successful</span>
                  </div>
                  <h1 className="text-3xl font-bold mb-2">Thank You for Your Booking!</h1>
                  <p className="text-white/80">{message}</p>
                </div>
              </div>
            </div>
            
            {/* Booking Details Section */}
            <div className="p-6 sm:p-8">
              {bookingDetails && (
                <div className="mb-8">
                  <h2 className="text-xl font-semibold text-[#495E57] mb-4 flex items-center">
                    <svg xmlns="http://www.w3.org/2000/svg" className="h-5 w-5 mr-2" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                      <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 5H7a2 2 0 00-2 2v12a2 2 0 002 2h10a2 2 0 002-2V7a2 2 0 00-2-2h-2M9 5a2 2 0 002 2h2a2 2 0 002-2M9 5a2 2 0 012-2h2a2 2 0 012 2" />
                    </svg>
                    Booking Details
                  </h2>
                  
                  <div className="bg-gray-50 rounded-xl p-6 border border-gray-100">
                    <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                      <div>
                        <div className="mb-4">
                          <p className="text-sm font-medium text-gray-500 mb-1">Service</p>
                          <p className="text-gray-800 font-semibold text-lg">
                            {bookingDetails.service?.serviceName || "Selected Service"}
                          </p>
                        </div>
                        
                        <div className="mb-4">
                          <p className="text-sm font-medium text-gray-500 mb-1">Date</p>
                          <p className="text-gray-800">
                            {formatDate(bookingDetails.bookingDate)}
                          </p>
                        </div>
                        
                        <div>
                          <p className="text-sm font-medium text-gray-500 mb-1">Time</p>
                          <p className="text-gray-800">
                            {formatTime(bookingDetails.bookingTime)}
                          </p>
                        </div>
                      </div>
                      
                      <div>
                        <div className="mb-4">
                          <p className="text-sm font-medium text-gray-500 mb-1">Service Provider</p>
                          <p className="text-gray-800">
                            {bookingDetails.service?.provider?.firstName} {bookingDetails.service?.provider?.lastName}
                          </p>
                        </div>
                        
                        <div className="mb-4">
                          <p className="text-sm font-medium text-gray-500 mb-1">Payment Method</p>
                          <p className="text-gray-800 capitalize">
                            {bookingDetails.paymentMethod || "Not specified"}
                          </p>
                        </div>
                        
                        <div>
                          <p className="text-sm font-medium text-gray-500 mb-1">Total Amount</p>
                          <p className="text-[#495E57] font-bold">
                            ₱{Number(bookingDetails.totalCost).toLocaleString('en-PH', {
                              minimumFractionDigits: 2,
                              maximumFractionDigits: 2
                            })}
                          </p>
                        </div>
                      </div>
                    </div>
                  </div>
                </div>
              )}
              
              {/* What's Next Section */}
              <div className="mb-8">
                <h2 className="text-xl font-semibold text-[#495E57] mb-4 flex items-center">
                  <svg xmlns="http://www.w3.org/2000/svg" className="h-5 w-5 mr-2" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M13 7l5 5m0 0l-5 5m5-5H6" />
                  </svg>
                  What's Next?
                </h2>
                
                <div className="space-y-4">
                  <div className="flex">
                    <div className="flex-shrink-0 h-10 w-10 rounded-full bg-[#F4CE14]/20 flex items-center justify-center mr-4">
                      <span className="text-[#495E57] font-bold">1</span>
                    </div>
                    <div>
                      <h3 className="font-medium text-gray-800">Check Your Email</h3>
                      <p className="text-gray-600 mt-1">We've sent you a booking confirmation email with all the details.</p>
                    </div>
                  </div>
                  
                  <div className="flex">
                    <div className="flex-shrink-0 h-10 w-10 rounded-full bg-[#F4CE14]/20 flex items-center justify-center mr-4">
                      <span className="text-[#495E57] font-bold">2</span>
                    </div>
                    <div>
                      <h3 className="font-medium text-gray-800">Provider Confirmation</h3>
                      <p className="text-gray-600 mt-1">Your service provider will contact you to confirm the details.</p>
                    </div>
                  </div>
                  
                  <div className="flex">
                    <div className="flex-shrink-0 h-10 w-10 rounded-full bg-[#F4CE14]/20 flex items-center justify-center mr-4">
                      <span className="text-[#495E57] font-bold">3</span>
                    </div>
                    <div>
                      <h3 className="font-medium text-gray-800">Track Your Booking</h3>
                      <p className="text-gray-600 mt-1">You can view your booking status in your Booking History.</p>
                    </div>
                  </div>
                </div>
              </div>
              
              {/* Action Buttons */}
              <div className="flex flex-col sm:flex-row gap-4">
                <button 
                  onClick={() => navigate('/customerProfile/bookingHistory')}
                  className="flex-1 bg-[#495E57] hover:bg-[#3a4a43] text-white px-6 py-3 rounded-lg transition-colors font-medium flex items-center justify-center"
                >
                  <svg xmlns="http://www.w3.org/2000/svg" className="h-5 w-5 mr-2" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 5H7a2 2 0 00-2 2v12a2 2 0 002 2h10a2 2 0 002-2V7a2 2 0 00-2-2h-2M9 5a2 2 0 002 2h2a2 2 0 002-2M9 5a2 2 0 012-2h2a2 2 0 012 2" />
                  </svg>
                  View Booking History
                </button>
                <button 
                  onClick={() => navigate('/browseServices')}
                  className="flex-1 bg-[#F4CE14] hover:bg-yellow-400 text-[#495E57] px-6 py-3 rounded-lg transition-colors font-medium flex items-center justify-center"
                >
                  <svg xmlns="http://www.w3.org/2000/svg" className="h-5 w-5 mr-2" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M4 6h16M4 12h16M4 18h7" />
                  </svg>
                  Browse More Services
                </button>
              </div>
            </div>
          </div>
        )}
        
        {/* Customer Support Section */}
        <div className="mt-8 p-6 bg-white rounded-xl shadow-md border border-gray-100">
          <div className="flex flex-col sm:flex-row items-center">
            <div className="bg-[#495E57]/10 p-4 rounded-full mb-4 sm:mb-0">
              <svg xmlns="http://www.w3.org/2000/svg" className="h-8 w-8 text-[#495E57]" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M18.364 5.636l-3.536 3.536m0 5.656l3.536 3.536M9.172 9.172L5.636 5.636m3.536 9.192l-3.536 3.536M21 12a9 9 0 11-18 0 9 9 0 0118 0zm-5 0a4 4 0 11-8 0 4 4 0 018 0z" />
              </svg>
            </div>
            <div className="sm:ml-6 text-center sm:text-left">
              <h3 className="font-semibold text-gray-800 mb-1">Need Help?</h3>
              <p className="text-gray-600">If you have any questions about your booking, please contact our support team.</p>
              <div className="mt-3">
                <a href="mailto:support@serbisyo.com" className="text-[#495E57] hover:text-[#F4CE14] font-medium transition-colors">
                  support@serbisyo.com
                </a>
                <span className="mx-2 text-gray-400">|</span>
                <a href="tel:+639123456789" className="text-[#495E57] hover:text-[#F4CE14] font-medium transition-colors">
                  +63 912 345 6789
                </a>
              </div>
            </div>
          </div>
        </div>
        
        <canvas 
          ref={confettiCanvasRef}
          className="fixed pointer-events-none inset-0 z-50"
          style={{ width: '100vw', height: '100vh' }}
        ></canvas>
      </div>
    </div>
  );
};

export default PaymentSuccessPage;
