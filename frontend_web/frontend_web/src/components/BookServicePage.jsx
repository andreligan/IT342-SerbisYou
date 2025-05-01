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

  // First log the location state for debugging
  useEffect(() => {
    console.log("Location state in BookServicePage:", location.state);
  }, [location]);
  
  // Get service from state or create a default
  const serviceData = location.state?.service || {
    serviceName: "Service",
    price: 1000,
    provider: { providerId: null },
  };
  
  // Add debug logging for serviceData
  useEffect(() => {
    console.log("Service data received:", serviceData);
    console.log("Provider ID:", serviceData?.provider?.providerId);
  }, [serviceData]);

  // Add useEffect to create a fallback if serviceData is empty
  useEffect(() => {
    if (!serviceData || !serviceData.serviceId) {
      console.error("No service data available in BookServicePage");
      setError("Service information is missing. Please try again.");
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
        .map(schedule => ({
          value: `${schedule.startTime}-${schedule.endTime}`,
          label: `${formatTimeWithAMPM(schedule.startTime)} - ${formatTimeWithAMPM(schedule.endTime)}`,
          startTime: schedule.startTime,
          endTime: schedule.endTime
        }))
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
      
      const bookingRequest = {
        customer: { customerId: customerId },
        service: { 
          serviceId: serviceData.serviceId,
          serviceName: serviceData.serviceName,
          provider: serviceData.provider ? { providerId: serviceData.provider.providerId } : null 
        },
        bookingDate: format(bookingDate, 'yyyy-MM-dd'),
        bookingTime: timeString,
        status: "Pending",
        totalCost: totalPrice,
        note: note || "",
        paymentMethod: paymentMethod,
        fullPayment: isFullPayment
      };
      
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

  return (
    <div className="min-h-screen bg-gradient-to-b from-gray-50 to-white py-8 px-4">
      <div className="max-w-5xl mx-auto">
        <h1 className="text-3xl sm:text-4xl font-bold text-[#495E57] mb-6 text-center">
          Book Service
        </h1>

        {/* Display error if service data is missing */}
        {!serviceData?.serviceId && (
          <div className="bg-red-50 border-l-4 border-red-500 p-4 mb-6">
            <p className="text-red-700">
              Service information is missing. Please go back and try again.
            </p>
            <button 
              onClick={() => navigate(-1)}
              className="mt-3 px-4 py-2 bg-[#495E57] text-white rounded-lg"
            >
              Go Back
            </button>
          </div>
        )}

        {/* JSX content */}
      </div>
    </div>
  );
};

export default BookServicePage;