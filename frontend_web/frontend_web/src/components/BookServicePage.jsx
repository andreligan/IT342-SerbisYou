import React, { useState, useEffect } from "react";
import { useNavigate, useLocation } from "react-router-dom";
import { format, addDays } from 'date-fns';
import DateTimeSelection from "./DateTimeSelection";
import ReviewBookingDetails from "./ReviewBookingDetails";
import PaymentConfirmation from "./PaymentConfirmation";
import NotificationService from '../services/NotificationService';
import { motion, AnimatePresence } from "framer-motion";
import apiClient, { getApiUrl } from "../utils/apiConfig";

const BookServicePage = () => {
  const navigate = useNavigate();
  const location = useLocation();
  
  const [step, setStep] = useState(1);
  const [bookingDate, setBookingDate] = useState(new Date());
  const [bookingTime, setBookingTime] = useState("");
  const [address, setAddress] = useState("");
  const [note, setNote] = useState("");
  const [isLoading, setIsLoading] = useState(true);
  const [isLoadingTimeSlots, setIsLoadingTimeSlots] = useState(false);
  const [error, setError] = useState(null);
  const [availableTimeSlots, setAvailableTimeSlots] = useState([]);
  const [paymentMethod, setPaymentMethod] = useState("cash");
  const [isProcessingPayment, setIsProcessingPayment] = useState(false);
  const [serviceFee, setServiceFee] = useState(0);
  const [totalPrice, setTotalPrice] = useState(0);
  const [selectedTimeSlotIndex, setSelectedTimeSlotIndex] = useState(null);
  const [payMongoFee, setPayMongoFee] = useState(0);
  const [appFee, setAppFee] = useState(0);
  const [customerId, setCustomerId] = useState(null);
  const [debugInfo, setDebugInfo] = useState(null);
  const [providerInfo, setProviderInfo] = useState(null);
  const [isFullPayment, setIsFullPayment] = useState(true);

  const serviceData = location.state?.service || {
    serviceName: "Service",
    price: 1000,
    provider: { providerId: null },
  };

  useEffect(() => {
    console.log("Service data received:", serviceData);
    console.log("Provider ID:", serviceData?.provider?.providerId);
    if (serviceData?.provider) {
      setProviderInfo({
        providerId: serviceData.provider.providerId,
        firstName: serviceData.provider.firstName,
        lastName: serviceData.provider.lastName
      });
    }
  }, [serviceData]);

  useEffect(() => {
    if (serviceData && serviceData.price) {
      const price = parseFloat(serviceData.price);
      const payMongoFeeAmount = price * 0.025;
      const appFeeAmount = price * 0.025;
      
      setPayMongoFee(payMongoFeeAmount);
      setAppFee(appFeeAmount);
      setServiceFee(payMongoFeeAmount + appFeeAmount);
      setTotalPrice(price + payMongoFeeAmount + appFeeAmount);
    }
  }, [serviceData]);

  useEffect(() => {
    const fetchAddress = async () => {
      try {
        setIsLoading(true);
        const token = localStorage.getItem("authToken") || sessionStorage.getItem("authToken");
        const userId = localStorage.getItem("userId") || sessionStorage.getItem("userId");
        
        if (!token || !userId) {
          setError("Authentication information not found.");
          setIsLoading(false);
          return;
        }

        console.log("Current User ID:", userId);
        
        const customersResponse = await apiClient.get(getApiUrl("/customers/getAll"));
        
        console.log("Customers received:", customersResponse.data.length);
        
        const customer = customersResponse.data.find(cust => {
          if (!cust.userAuth) return false;
          const custUserId = cust.userAuth.userId;
          return custUserId == userId;
        });
        
        console.log("Matching customer found:", customer ? "Yes" : "No");
        
        if (!customer) {
          setError("Customer profile not found. Please complete your profile setup.");
          setIsLoading(false);
          return;
        }

        setCustomerId(customer.customerId);
        console.log("Customer ID stored:", customer.customerId);
        
        const addressesResponse = await apiClient.get(getApiUrl("/addresses/getAll"));
        
        console.log("Total addresses:", addressesResponse.data.length);
        
        const addresses = addressesResponse.data.filter(addr => {
          if (!addr.customer) return false;
          return addr.customer.customerId == customer.customerId;
        });
        
        console.log("Found addresses for customer:", addresses.length);
        
        let selectedAddress = addresses.find(addr => addr.main === true);
        if (!selectedAddress && addresses.length > 0) {
          selectedAddress = addresses[0];
        }
        
        if (selectedAddress) {
          const parts = [
            selectedAddress.streetName, 
            selectedAddress.barangay, 
            selectedAddress.city, 
            selectedAddress.province
          ].filter(Boolean);
          
          setAddress(parts.join(', '));
          setError(null);
        } else {
          setError("No address found. Please add your address in your profile.");
        }
      } catch (error) {
        console.error("Error fetching customer address:", error);
        setError("Failed to load your address information.");
      } finally {
        setIsLoading(false);
      }
    };

    fetchAddress();
  }, []);

  useEffect(() => {
    if (bookingDate && serviceData?.provider?.providerId) {
      fetchAvailableTimeSlots();
    }
  }, [bookingDate, serviceData?.provider?.providerId]);

  const fetchAvailableTimeSlots = async () => {
    setIsLoadingTimeSlots(true);
    setError(null);
    
    try {
      const token = localStorage.getItem("authToken") || sessionStorage.getItem("authToken");
      if (!token) {
        setError("Authentication token not found.");
        setIsLoadingTimeSlots(false);
        return;
      }
      
      const dayOfWeek = format(bookingDate, 'EEEE').toUpperCase();
      const providerId = serviceData.provider.providerId;
      
      console.log("Fetching schedules with:", {
        providerId,
        dayOfWeek,
        date: format(bookingDate, 'yyyy-MM-dd')
      });
      
      if (!providerId) {
        console.error("Missing provider ID");
        setError("Service provider information is missing.");
        setIsLoadingTimeSlots(false);
        return;
      }
      
      const response = await apiClient.get(
        getApiUrl(`/schedules/provider/${providerId}/day/${dayOfWeek}`)
      );
      
      console.log("Schedule API response:", response.data);
      
      const slots = response.data
        .filter(schedule => {
          console.log("Schedule availability:", schedule.isAvailable);
          return schedule.isAvailable !== false;
        })
        .map(schedule => {
          const startTimeString = Array.isArray(schedule.startTime) 
            ? `${schedule.startTime[0]}:${schedule.startTime[1]}`
            : schedule.startTime;
          
          const endTimeString = Array.isArray(schedule.endTime) 
            ? `${schedule.endTime[0]}:${schedule.endTime[1]}`
            : schedule.endTime;
          
          return {
            value: `${startTimeString}-${endTimeString}`,
            label: `${formatTimeWithAMPM(schedule.startTime)} - ${formatTimeWithAMPM(schedule.endTime)}`,
            startTime: startTimeString,
            endTime: endTimeString
          };
        })
        .sort((a, b) => a.startTime.localeCompare(b.startTime));
      
      console.log("Formatted time slots:", slots);
      setAvailableTimeSlots(slots);
    } catch (error) {
      console.error("Error fetching available time slots:", error);
      setError("Failed to load available time slots. Please try again.");
    } finally {
      setIsLoadingTimeSlots(false);
    }
  };

  const formatTimeWithAMPM = (time) => {
    if (!time) return "";
    
    let hours, minutes;
    
    if (Array.isArray(time)) {
      [hours, minutes] = time;
    } else if (typeof time === 'string') {
      [hours, minutes] = time.split(":");
    } else {
      console.error("Unexpected time format:", time);
      return "Invalid time";
    }
    
    const hour = parseInt(hours, 10);
    const period = hour >= 12 ? "PM" : "AM";
    const formattedHours = hour % 12 || 12;
    return `${formattedHours}:${minutes} ${period}`;
  };

  const formatDateForDisplay = (date) => {
    return format(date, 'EEEE, MMMM d, yyyy');
  };

  const formatTimeForBackend = (timeStr) => {
    if (!timeStr) return null;
    
    // Parse the time string to ensure proper formatting
    const timeParts = timeStr.split(':');
    if (timeParts.length >= 2) {
      // Ensure hours and minutes have 2 digits
      const hours = timeParts[0].padStart(2, '0');
      const minutes = timeParts[1].padStart(2, '0');
      return `${hours}:${minutes}:00`;
    }
    
    console.error("Unexpected time format:", timeStr);
    return null;
  };

  const createGCashCheckout = async (checkoutPayload) => {
    try {
      console.log("Creating GCash checkout with payload:", checkoutPayload);
      
      const response = await apiClient.post(getApiUrl('/create-gcash-checkout'), checkoutPayload);
      
      console.log("Checkout session created:", response.data);
      
      return response.data.checkout_url;
    } catch (error) {
      console.error("Error creating GCash checkout:", error);
      throw new Error("Failed to create GCash payment session.");
    }
  };

  const handleNext = () => {
    if (step === 1) {
      if (!bookingDate || !bookingTime) {
        setError("Please select both date and time for your booking.");
        return;
      }
      if (!address) {
        setError("Please add your address in your profile.");
        return;
      }
    }
    
    setError(null);
    setStep(step + 1);
  };

  const handleBack = () => {
    setStep(step - 1);
  };

  const handleSubmit = async () => {
    setIsProcessingPayment(true);
    setError(null);
    setDebugInfo(null);
    
    try {
      const token = localStorage.getItem("authToken") || sessionStorage.getItem("authToken");
      
      if (!customerId) {
        setError("Customer ID not found. Please make sure you have a complete profile.");
        setIsProcessingPayment(false);
        return;
      }
      
      // Extract and properly format the time
      let timeString = bookingTime.split('-')[0].trim();
      timeString = formatTimeForBackend(timeString);
      
      if (!timeString) {
        setError("Invalid time format. Please select a time slot again.");
        setIsProcessingPayment(false);
        return;
      }
      
      const actualPaymentAmount = paymentMethod === 'gcash' && !isFullPayment 
        ? totalPrice * 0.5
        : totalPrice;
      
      // Create booking request with proper structure
      const bookingRequest = {
        customer: { customerId: customerId },
        service: { serviceId: serviceData.serviceId },
        provider: { providerId: serviceData.provider.providerId }, // Extract provider as a top-level field
        bookingDate: format(bookingDate, 'yyyy-MM-dd'),
        bookingTime: timeString,
        status: "PENDING", // Use uppercase to match enum values on backend
        totalCost: parseFloat(totalPrice.toFixed(2)), // Ensure it's a proper number
        note: note || "",
        paymentMethod: paymentMethod.toUpperCase(), // Use uppercase to match enum values
        fullPayment: isFullPayment
      };
      
      // Remove the provider from nested service object to avoid duplication
      delete bookingRequest.service.provider;
      
      const debugData = {
        requestData: bookingRequest,
        additionalInfo: {
          serviceName: serviceData.serviceName,
          providerName: `${serviceData?.provider?.firstName || ''} ${serviceData?.provider?.lastName || ''}`,
          providerId: serviceData?.provider?.providerId || 'Not set',
          providerObject: providerInfo,
          formattedTime: formatTimeWithAMPM(timeString),
          basePrice: serviceData.price,
          payMongoFee: payMongoFee,
          appFee: appFee,
          customerAddress: address,
          paymentMethod: paymentMethod,
          isFullPayment: isFullPayment,
          actualPaymentAmount: actualPaymentAmount
        }
      };
      
      setDebugInfo(debugData);
      console.log('DEBUG - Complete Booking Request:', debugData);
      
      if (paymentMethod === 'gcash') {
        try {
          const checkoutPayload = {
            amount: isFullPayment ? totalPrice : (totalPrice * 0.5),
            description: `${isFullPayment ? 'Full Payment' : 'Downpayment (50%)'} for ${serviceData.serviceName}`,
            successUrl: `${window.location.origin}/payment-success`,
            cancelUrl: `${window.location.origin}/payment-cancel`
          };
          
          const checkoutUrl = await createGCashCheckout(checkoutPayload);
          
          sessionStorage.setItem('pendingBooking', JSON.stringify(bookingRequest));
          
          console.log("Redirecting to GCash payment page:", checkoutUrl);
          window.location.href = checkoutUrl;
          return;
        } catch (paymentError) {
          console.error("Payment processing error:", paymentError);
          setError("Failed to process GCash payment: " + paymentError.message);
          setIsProcessingPayment(false);
          return;
        }
      }
      
      try {
        const response = await apiClient.post(getApiUrl('/bookings/postBooking'), bookingRequest);
        
        console.log('Booking successful:', response.data);

        if (response.data && response.data.bookingId) {
          try {
            const notificationData = {
              user: { userId: serviceData.provider.userAuth.userId },
              type: "booking",
              message: `New booking request: ${serviceData.serviceName} on ${formatDateForDisplay(bookingDate)} at ${formatTimeWithAMPM(timeString)}`,
              isRead: false,
              createdAt: new Date().toISOString(),
              referenceId: response.data.bookingId,
              referenceType: "Booking"
            };
            
            await NotificationService.createNotification(notificationData);
            console.log("Booking notification created successfully");
          } catch (notifError) {
            console.error("Error creating booking notification:", notifError);
          }
        }

        navigate('/payment-success', { state: { bookingData: response.data } });
      } catch (apiError) {
        console.error("API Error:", apiError);
        
        let errorMessage = "Failed to create your booking. Please try again.";
        if (apiError.response) {
          console.log("API response status:", apiError.response.status);
          console.log("API response data:", apiError.response.data);
          
          if (apiError.response.data && typeof apiError.response.data === 'string') {
            errorMessage = apiError.response.data;
          }
        }
        
        setError(errorMessage);
      }
    } catch (error) {
      console.error("Error in submission process:", error);
      setError("An unexpected error occurred. Please try again.");
    } finally {
      setIsProcessingPayment(false);
    }
  };

  const pageVariants = {
    initial: { opacity: 0, x: 20 },
    animate: { opacity: 1, x: 0 },
    exit: { opacity: 0, x: -20 }
  };

  const stepVariants = {
    inactive: { scale: 0.9, opacity: 0.7 },
    active: { scale: 1, opacity: 1 },
    completed: { scale: 1, opacity: 1 }
  };

  return (
    <div className="min-h-screen bg-gradient-to-b from-gray-50 to-white py-8 px-4">
      <div className="max-w-5xl mx-auto">
        <h1 className="text-3xl sm:text-4xl font-bold text-[#495E57] mb-6 text-center">
          Book Service
        </h1>

        <div className="mb-12 relative">
          <div className="flex justify-between">
            {['Select Date & Time', 'Review Details', 'Payment'].map((label, index) => (
              <div key={index} className="flex flex-col items-center z-10">
                <motion.div 
                  className={`w-16 h-16 md:w-20 md:h-20 rounded-full flex items-center justify-center text-lg md:text-2xl font-semibold shadow-md ${
                    step >= index + 1 
                      ? 'bg-[#F4CE14] text-[#495E57]' 
                      : 'bg-gray-100 text-gray-300'
                  }`}
                  whileHover={step >= index + 1 ? { scale: 1.05, boxShadow: "0 4px 12px rgba(0,0,0,0.1)" } : {}}
                >
                  {index + 1}
                </motion.div>
                <div className="text-xs md:text-sm mt-2 text-center px-1">{label}</div>
              </div>
            ))}
          </div>
          
          <div className="absolute top-8 md:top-10 left-0 right-0 flex justify-center w-4/5 mx-auto z-0">
            {[1, 2].map((_, index) => (
              <div key={`connector-${index}`} className="w-full mx-2">
                <div className={`h-1 ${
                  step > index + 1 ? 'bg-[#F4CE14]' : 'bg-gray-200'
                } transition-all duration-500`}></div>
              </div>
            ))}
          </div>
        </div>

        {error && (
          <div className="text-red-500 text-center mb-4">{error}</div>
        )}

        <AnimatePresence mode="wait">
          <motion.div
            key={step}
            initial="initial"
            animate="animate"
            exit="exit"
            variants={pageVariants}
            transition={{ duration: 0.3, ease: "easeInOut" }}
          >
            {step === 1 && (
              <DateTimeSelection
                serviceData={serviceData}
                bookingDate={bookingDate}
                setBookingDate={setBookingDate}
                bookingTime={bookingTime}
                setBookingTime={setBookingTime}
                address={address}
                isLoading={isLoading}
                isLoadingTimeSlots={isLoadingTimeSlots}
                error={error}
                availableTimeSlots={availableTimeSlots}
                selectedTimeSlotIndex={selectedTimeSlotIndex}
                setSelectedTimeSlotIndex={setSelectedTimeSlotIndex}
                navigate={navigate}
                payMongoFee={payMongoFee}
                appFee={appFee}
                totalPrice={totalPrice}
                handleNext={handleNext}
                formatTimeWithAMPM={formatTimeWithAMPM}
                fetchAvailableTimeSlots={fetchAvailableTimeSlots}
              />
            )}

            {step === 2 && (
              <ReviewBookingDetails 
                serviceData={serviceData}
                bookingDate={bookingDate}
                bookingTime={bookingTime}
                address={address}
                note={note}
                setNote={setNote}
                paymentMethod={paymentMethod}
                setPaymentMethod={setPaymentMethod}
                isFullPayment={isFullPayment}
                setIsFullPayment={setIsFullPayment}
                payMongoFee={payMongoFee}
                appFee={appFee}
                totalPrice={totalPrice}
                handleBack={handleBack}
                handleNext={handleNext}
                formatDateForDisplay={formatDateForDisplay}
                formatTimeWithAMPM={formatTimeWithAMPM}
                navigate={navigate}
              />
            )}

            {step === 3 && (
              <PaymentConfirmation
                serviceData={serviceData}
                paymentMethod={paymentMethod}
                isFullPayment={isFullPayment}
                payMongoFee={payMongoFee}
                appFee={appFee}
                totalPrice={totalPrice}
                debugInfo={debugInfo}
                isProcessingPayment={isProcessingPayment}
                handleBack={handleBack}
                handleSubmit={handleSubmit}
              />
            )}
          </motion.div>
        </AnimatePresence>
      </div>
    </div>
  );
};

export default BookServicePage;