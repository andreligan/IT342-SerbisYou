import React, { useEffect, useState, useRef } from 'react';
import { useLocation, useNavigate } from 'react-router-dom';
import confetti from 'canvas-confetti';
import NotificationService from '../../services/NotificationService';
import apiClient, { getApiUrl, API_BASE_URL } from '../../utils/apiConfig';

const PaymentSuccessPage = () => {
  const navigate = useNavigate();
  const location = useLocation();
  const [isLoading, setIsLoading] = useState(true);
  const [message, setMessage] = useState('Payment successful!');
  const [error, setError] = useState(null);
  const [bookingDetails, setBookingDetails] = useState(null);
  const confettiCanvasRef = useRef(null);
  
  // Check if this is a cash payment
  const isCashPayment = bookingDetails?.paymentMethod?.toUpperCase() === 'CASH';
  
  // Generate a payment code for cash payments
  const [paymentCode, setPaymentCode] = useState('');
  
  // Set up payment code for cash payments
  useEffect(() => {
    if (isCashPayment && bookingDetails) {
      // Generate a unique payment code based on booking ID and timestamp
      const code = `SY-${bookingDetails.bookingId}-${Date.now().toString().slice(-6)}`;
      setPaymentCode(code);
    }
  }, [isCashPayment, bookingDetails]);
  
  // Calculate downpayment/remaining balance if applicable
  const isDownpayment = bookingDetails?.paymentMethod?.toUpperCase() === 'GCASH' && bookingDetails?.fullPayment === false;
  const paidAmount = isDownpayment ? (bookingDetails?.totalCost * 0.5) : 
                    (isCashPayment ? 0 : bookingDetails?.totalCost);
  const remainingBalance = isDownpayment ? (bookingDetails?.totalCost * 0.5) : 
                          (isCashPayment ? bookingDetails?.totalCost : 0);

  // Save receipt as image for cash payment
  const receiptRef = useRef(null);
  const saveReceipt = async () => {
    if (!receiptRef.current) return;
    
    try {
      // Since html2canvas may not be imported, use browser screenshot capability
      // or just show an alert for now
      alert('Please take a screenshot of this page to save your payment reference.');
    } catch (err) {
      console.error('Error saving receipt:', err);
    }
  };

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
        
        // Store the service information
        const serviceId = bookingRequest.service?.serviceId;
        console.log("Service ID from pending booking:", serviceId);
        
        // Add an idempotency key based on timestamp and service to help prevent duplicates
        // *** IMPORTANT CHANGE: Include idempotency key in the body instead of as a header ***
        const idempotencyKey = `booking_${bookingRequest.customer.customerId}_${bookingRequest.service.serviceId}_${Date.now()}`;
        bookingRequest.idempotencyKey = idempotencyKey;
        
        // Submit the booking to the backend
        const response = await apiClient.post(getApiUrl('/bookings/postBooking'), bookingRequest);
        
        console.log('Booking created after successful payment:', response.data);
        setBookingDetails(response.data);
        
        // Create notification for service provider (for GCash payment)
        if (response.data && response.data.service && response.data.service.provider) {
          try {
            // Format date and time for notification message
            const formattedDate = formatDate(response.data.bookingDate);
            const formattedTime = formatTime(response.data.bookingTime);
            
            // Get the service name from response data or use a generic name
            const serviceName = response.data.service?.serviceName || "Service";
            console.log("Using service name for notification:", serviceName);
            
            // Get provider user ID, with fallback options
            let providerUserId;
            
            // Option 1: Try to get from userAuth object if available
            if (response.data.service.provider.userAuth && response.data.service.provider.userAuth.userId) {
              providerUserId = response.data.service.provider.userAuth.userId;
              console.log("Provider user ID from userAuth:", providerUserId);
            }
            // Option 2: If provider has a direct userId property (might be set in some implementations)
            else if (response.data.service.provider.userId) {
              providerUserId = response.data.service.provider.userId;
              console.log("Provider user ID from provider:", providerUserId);
            }
            // Option 3: If all else fails, fetch the provider details
            else {
              try {
                const providerId = response.data.service.provider.providerId;
                console.log("Fetching provider details for provider ID:", providerId);
                
                const providerResponse = await apiClient.get(getApiUrl(`/service-providers/getById/${providerId}`));
                
                if (providerResponse.data && providerResponse.data.userAuth) {
                  providerUserId = providerResponse.data.userAuth.userId;
                  console.log("Provider user ID from API call:", providerUserId);
                } else {
                  throw new Error("Could not get provider user ID from API");
                }
              } catch (error) {
                console.error("Error fetching provider details:", error);
                throw new Error("Failed to retrieve provider user ID");
              }
            }
            
            if (!providerUserId) {
              throw new Error("Could not determine provider user ID");
            }
            
            // Create notification data with the resolved userId
            const notificationData = {
              user: { userId: providerUserId }, 
              type: "booking",
              message: `New booking request: ${serviceName} on ${formattedDate} at ${formattedTime}`,
              isRead: false,
              createdAt: new Date().toISOString(),
              referenceId: response.data.bookingId,
              referenceType: "Booking"
            };
            
            await NotificationService.createNotification(notificationData);
            console.log("GCash payment booking notification created successfully");
          } catch (notifError) {
            console.error("Error creating GCash payment booking notification:", notifError);
            // Continue with the booking flow even if notification creation fails
          }
        }
        
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
      setTimeout(() => {
        sessionStorage.removeItem('paymentProcessed');
      }, 1000);
    };
  }, []); 
  
  // Enhanced format date function to handle both string and array formats
  const formatDate = (dateInput) => {
    if (!dateInput) return 'Not available';
    
    let date;
    
    if (Array.isArray(dateInput)) {
      // If date is in array format [year, month, day]
      const [year, month, day] = dateInput;
      date = new Date(year, month - 1, day); // Month in JS is 0-indexed
    } else if (typeof dateInput === 'string') {
      // If date is in string format
      date = new Date(dateInput);
    } else {
      return 'Invalid date format';
    }
    
    if (isNaN(date.getTime())) {
      return 'Invalid date';
    }
    
    return new Intl.DateTimeFormat('en-US', {
      weekday: 'long',
      year: 'numeric',
      month: 'long',
      day: 'numeric'
    }).format(date);
  };
  
  // Enhanced format time function to handle both string and array formats
  const formatTime = (timeInput) => {
    if (!timeInput) return 'Not available';
    
    let hour, minutes;
    
    if (Array.isArray(timeInput)) {
      // If time is in array format [hour, minute]
      [hour, minutes] = timeInput;
      minutes = minutes || 0; // Default to 0 if minutes aren't provided
    } else if (typeof timeInput === 'string') {
      // If time is in string format like "HH:MM:SS"
      const timeParts = timeInput.split(':');
      if (timeParts.length < 2) return timeInput;
      hour = parseInt(timeParts[0], 10);
      minutes = timeParts[1];
    } else {
      return 'Invalid time format';
    }
    
    // Format with padding for minutes
    const paddedMinutes = String(minutes).padStart(2, '0');
    const period = hour >= 12 ? 'PM' : 'AM';
    const formattedHour = hour % 12 || 12;
    
    return `${formattedHour}:${paddedMinutes} ${period}`;
  };
  
  return (
    <div className="min-h-screen bg-gray-50 py-12">
      <div className="max-w-4xl mx-auto px-4">
        {/* The rest of the component remains the same */}
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
            {/* Error display - same as before */}
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
            {/* Celebratory Header - same as before */}
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
                  
                  {/* Main booking details panel */}
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
                            {(bookingDetails.paymentMethod || "Not specified").toLowerCase()}
                            {isDownpayment && " (50% Downpayment)"}
                          </p>
                        </div>
                        
                        <div>
                          <p className="text-sm font-medium text-gray-500 mb-1">Payment Status</p>
                          <div className="flex flex-col">
                            {isCashPayment ? (
                              // Cash payment status display
                              <div>
                                <p className="text-orange-600 font-bold flex items-center">
                                  <svg xmlns="http://www.w3.org/2000/svg" className="h-4 w-4 mr-1" viewBox="0 0 20 20" fill="currentColor">
                                    <path fillRule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zm1-12a1 1 0 10-2 0v4a1 1 0 00.293.707l2.828 2.829a1 1 0 101.415-1.415L11 9.586V6z" clipRule="evenodd" />
                                  </svg>
                                  Payment Pending (Cash)
                                </p>
                                <div className="mt-2 bg-orange-50 p-2 rounded-lg border border-orange-100">
                                  <p className="text-sm flex items-center text-orange-800">
                                    <svg xmlns="http://www.w3.org/2000/svg" className="h-4 w-4 mr-1" viewBox="0 0 20 20" fill="currentColor">
                                      <path fillRule="evenodd" d="M18 10a8 8 0 11-16 0 8 8 0 0116 0zm-7-4a1 1 0 11-2 0 1 1 0 012 0zM9 9a1 1 0 000 2v3a1 1 0 001 1h1a1 1 0 100-2v-3a1 1 0 00-1-1H9z" clipRule="evenodd" />
                                    </svg>
                                    Payment will be collected after service completion
                                  </p>
                                  {!bookingDetails.fullPayment && bookingDetails.totalCost > 1000 && (
                                    <p className="text-sm mt-1 text-orange-700">
                                      ₱500 deposit payment required at the start of service.
                                    </p>
                                  )}
                                </div>
                              </div>
                            ) : (
                              // GCash payment status display
                              <React.Fragment>
                                <p className="text-[#495E57] font-bold">
                                  {isDownpayment ? "Partial Payment" : "Full Payment"}
                                </p>
                                {isDownpayment && (
                                  <div className="mt-2 bg-blue-50 p-2 rounded-lg border border-blue-100">
                                    <p className="text-sm flex items-center text-blue-800">
                                      <svg xmlns="http://www.w3.org/2000/svg" className="h-4 w-4 mr-1" viewBox="0 0 20 20" fill="currentColor">
                                        <path fillRule="evenodd" d="M18 10a8 8 0 11-16 0 8 8 0 0116 0zm-7-4a1 1 0 11-2 0 1 1 0 012 0zM9 9a1 1 0 000 2v3a1 1 0 001 1h1a1 1 0 100-2v-3a1 1 0 00-1-1H9z" clipRule="evenodd" />
                                      </svg>
                                      Downpayment completed: ₱{paidAmount.toLocaleString('en-PH', {
                                        minimumFractionDigits: 2,
                                        maximumFractionDigits: 2
                                      })}
                                    </p>
                                    <p className="text-sm mt-1 text-blue-700">
                                      Remaining balance: ₱{remainingBalance.toLocaleString('en-PH', {
                                        minimumFractionDigits: 2,
                                        maximumFractionDigits: 2
                                      })} (to be paid upon service completion)
                                    </p>
                                  </div>
                                )}
                              </React.Fragment>
                            )}
                          </div>
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
              
              {/* Cash Payment Reference Code */}
              {isCashPayment && bookingDetails && (
                <div className="mb-8" ref={receiptRef}>
                  <h2 className="text-xl font-semibold text-[#495E57] mb-4 flex items-center">
                    <svg xmlns="http://www.w3.org/2000/svg" className="h-5 w-5 mr-2" viewBox="0 0 20 20" fill="currentColor">
                      <path fillRule="evenodd" d="M4 4a2 2 0 00-2 2v4a2 2 0 002 2V6h10a2 2 0 00-2-2H4zm2 6a2 2 0 012-2h8a2 2 0 012 2v4a2 2 0 01-2 2H8a2 2 0 01-2-2v-4zm6 4a2 2 0 100-4 2 2 0 000 4z" clipRule="evenodd" />
                    </svg>
                    Payment Reference
                  </h2>
                  
                  <div className="bg-white rounded-xl border border-gray-200 p-6 text-center">
                    <div className="bg-[#495E57]/5 p-4 rounded-xl mb-4">
                      <h3 className="text-[#495E57] font-medium mb-1">Payment Code</h3>
                      <p className="text-2xl font-mono font-bold tracking-wider">{paymentCode}</p>
                    </div>
                    
                    <p className="text-gray-600 mb-4">
                      Show this code to your service provider when they arrive. This confirms your booking.
                    </p>
                    
                    <button 
                      onClick={saveReceipt}
                      className="w-full bg-[#495E57] text-white py-3 rounded-lg flex items-center justify-center"
                    >
                      <svg xmlns="http://www.w3.org/2000/svg" className="h-5 w-5 mr-2" viewBox="0 0 20 20" fill="currentColor">
                        <path fillRule="evenodd" d="M3 17a1 1 0 011-1h12a1 1 0 110 2H4a1 1 0 01-1-1zm3.293-7.707a1 1 0 011.414 0L9 10.586V3a1 1 0 112 0v7.586l1.293-1.293a1 1 0 111.414 1.414l-3 3a1 1 0 01-1.414 0l-3-3a1 1 0 010-1.414z" clipRule="evenodd" />
                      </svg>
                      Save or Screenshot Payment Reference
                    </button>
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
                  
                  {isCashPayment ? (
                    <div className="flex">
                      <div className="flex-shrink-0 h-10 w-10 rounded-full bg-[#F4CE14]/20 flex items-center justify-center mr-4">
                        <span className="text-[#495E57] font-bold">3</span>
                      </div>
                      <div>
                        <h3 className="font-medium text-gray-800">Have Cash Ready</h3>
                        <p className="text-gray-600 mt-1">
                          Make sure to have the cash payment of ₱{bookingDetails?.totalCost?.toLocaleString('en-PH', {minimumFractionDigits: 2})} ready when the service is completed.
                          {!bookingDetails?.fullPayment && bookingDetails?.totalCost > 1000 && " A ₱500 deposit is required at the start of service."}
                        </p>
                      </div>
                    </div>
                  ) : (
                    <div className="flex">
                      <div className="flex-shrink-0 h-10 w-10 rounded-full bg-[#F4CE14]/20 flex items-center justify-center mr-4">
                        <span className="text-[#495E57] font-bold">3</span>
                      </div>
                      <div>
                        <h3 className="font-medium text-gray-800">Track Your Booking</h3>
                        <p className="text-gray-600 mt-1">You can view your booking status in your Booking History.</p>
                      </div>
                    </div>
                  )}
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
        
        {/* Canvas for confetti */}
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
