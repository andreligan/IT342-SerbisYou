import React, { useState, useEffect } from "react";
import { useNavigate, useLocation } from "react-router-dom";
import axios from "axios";
import { format, addDays } from 'date-fns';
import DateTimeSelection from "./DateTimeSelection";
import ReviewBookingDetails from "./ReviewBookingDetails";
import PaymentConfirmation from "./PaymentConfirmation";

const BookServicePage = () => {
  const navigate = useNavigate();
  const location = useLocation();
  
  // States for multi-step form
  const [step, setStep] = useState(1);

  // States for booking data
  const [bookingDate, setBookingDate] = useState(new Date());
  const [bookingTime, setBookingTime] = useState("");
  const [address, setAddress] = useState("");
  const [note, setNote] = useState("");
  
  // States for UI
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
  
  // Get service data from navigation state or default to placeholder
  const serviceData = location.state?.service || {
    serviceName: "Service",
    price: 1000,
    provider: { providerId: null },
  };

  // Debug serviceData to see if it contains the provider information
  useEffect(() => {
    console.log("Service data received:", serviceData);
    console.log("Provider ID:", serviceData?.provider?.providerId);
    // Store provider info separately for debugging
    if (serviceData?.provider) {
      setProviderInfo({
        providerId: serviceData.provider.providerId,
        firstName: serviceData.provider.firstName,
        lastName: serviceData.provider.lastName
      });
    }
  }, [serviceData]);

  // Updated fee calculation with detailed breakdown
  useEffect(() => {
    if (serviceData && serviceData.price) {
      const price = parseFloat(serviceData.price);
      const payMongoFeeAmount = price * 0.025; // 2.5% PayMongo fee
      const appFeeAmount = price * 0.025; // 2.5% app fee
      
      setPayMongoFee(payMongoFeeAmount);
      setAppFee(appFeeAmount);
      setServiceFee(payMongoFeeAmount + appFeeAmount); // Total service fee
      setTotalPrice(price + payMongoFeeAmount + appFeeAmount);
    }
  }, [serviceData]);

  // Fetch user address - improved approach using existing APIs
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
        
        // Get all customers - we'll find our match here
        const customersResponse = await axios.get("/api/customers/getAll", {
          headers: { Authorization: `Bearer ${token}` },
        });
        
        console.log("Customers received:", customersResponse.data.length);
        
        // More careful matching of customer to user ID
        const customer = customersResponse.data.find(cust => {
          // Handle different data formats and potential type issues
          if (!cust.userAuth) return false;
          
          const custUserId = cust.userAuth.userId;
          return custUserId == userId; // Use loose equality to handle string/number differences
        });
        
        console.log("Matching customer found:", customer ? "Yes" : "No");
        
        if (!customer) {
          setError("Customer profile not found. Please complete your profile setup.");
          setIsLoading(false);
          return;
        }

        // Store customerId for later use
        setCustomerId(customer.customerId);
        console.log("Customer ID stored:", customer.customerId);
        
        // Now get all addresses
        const addressesResponse = await axios.get("/api/addresses/getAll", {
          headers: { Authorization: `Bearer ${token}` },
        });
        
        console.log("Total addresses:", addressesResponse.data.length);
        
        // Find addresses that match this customer
        const addresses = addressesResponse.data.filter(addr => {
          if (!addr.customer) return false;
          return addr.customer.customerId == customer.customerId;
        });
        
        console.log("Found addresses for customer:", addresses.length);
        
        // Prefer the main address, fall back to any address
        let selectedAddress = addresses.find(addr => addr.main === true);
        if (!selectedAddress && addresses.length > 0) {
          selectedAddress = addresses[0];
        }
        
        if (selectedAddress) {
          // Safely construct address string, handling possible null values
          const parts = [
            selectedAddress.streetName, 
            selectedAddress.barangay, 
            selectedAddress.city, 
            selectedAddress.province
          ].filter(Boolean); // Remove any null/undefined/empty values
          
          setAddress(parts.join(', '));
          setError(null); // Clear any previous errors
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

  // Fetch time slots when date changes
  useEffect(() => {
    if (bookingDate && serviceData?.provider?.providerId) {
      fetchAvailableTimeSlots();
    }
  }, [bookingDate, serviceData?.provider?.providerId]);

  // Fetch available time slots for the selected date
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
      
      // Get the day of week for the selected date
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
      
      // Fetch available schedule for the selected day
      const response = await axios.get(
        `/api/schedules/provider/${providerId}/day/${dayOfWeek}`,
        {
          headers: { Authorization: `Bearer ${token}` },
        }
      );
      
      console.log("Schedule API response:", response.data);
      
      // Create time slots from the schedule
      const slots = response.data
        .filter(schedule => {
          console.log("Schedule availability:", schedule.isAvailable);
          return schedule.isAvailable !== false;  // Include if not explicitly false
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

  // Helper function to format time with AM/PM
  const formatTimeWithAMPM = (time) => {
    if (!time) return "";
    const [hours, minutes] = time.split(":");
    const hour = parseInt(hours, 10);
    const period = hour >= 12 ? "PM" : "AM";
    const formattedHours = hour % 12 || 12;
    return `${formattedHours}:${minutes} ${period}`;
  };

  // Helper function to format date in readable format
  const formatDateForDisplay = (date) => {
    return format(date, 'EEEE, MMMM d, yyyy');
  };

  // Function to ensure proper time format for Java's LocalTime (HH:mm:ss)
  const formatTimeForBackend = (timeStr) => {
    if (!timeStr) return null;
    
    // If the time already has seconds, return as is
    if (timeStr.split(':').length === 3) return timeStr;
    
    // If the time has hours and minutes only, add :00 for seconds
    if (timeStr.split(':').length === 2) return `${timeStr}:00`;
    
    // If the format is unexpected, log and return null
    console.error("Unexpected time format:", timeStr);
    return null;
  };

  // Proceed to next step
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
    
    setError(null); // Clear any errors
    setStep(step + 1);
  };

  // Go back to previous step
  const handleBack = () => {
    setStep(step - 1);
  };

  // Handle booking submission
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
      
      // Extract and format time correctly for Java LocalTime
      let timeString = bookingTime.split('-')[0].trim();
      timeString = formatTimeForBackend(timeString);
      
      if (!timeString) {
        setError("Invalid time format. Please select a time slot again.");
        setIsProcessingPayment(false);
        return;
      }
      
      // Create booking object with all required fields AND explicitly include provider information
      const bookingRequest = {
        customer: { customerId: customerId },
        service: { 
          serviceId: serviceData.serviceId,
          provider: serviceData.provider ? { providerId: serviceData.provider.providerId } : null 
        },
        bookingDate: format(bookingDate, 'yyyy-MM-dd'),
        bookingTime: timeString,  // Now properly formatted with seconds
        status: "Pending",
        totalCost: totalPrice,
        note: note || "" // Ensure note is not null
      };
      
      // Update debug info to show provider data
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
          customerAddress: address
        }
      };
      
      setDebugInfo(debugData);
      console.log('DEBUG - Complete Booking Request:', debugData);
      
      // Send booking request with error handling
      try {
        const response = await axios.post('/api/bookings/postBooking', bookingRequest, {
          headers: { Authorization: `Bearer ${token}` }
        });
        
        console.log('Booking successful:', response.data);
        
        // Handle different payment methods
        if (paymentMethod === 'gcash') {
          navigate('/payment-success');
        } else {
          navigate('/payment-success', { state: { bookingData: response.data } });
        }
      } catch (apiError) {
        console.error("API Error:", apiError);
        
        // Extract detailed error message if possible
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
    <div className="p-4 sm:p-8 max-w-5xl mx-auto">
      <h1 className="text-3xl sm:text-4xl font-bold text-[#495E57] mb-6 text-center">
        Book Service
      </h1>

      {/* Progress Bar */}
      <div className="w-full mb-8">
        <div className="flex mb-2">
          <div className="flex-1"></div>
          {[1, 2, 3].map((stepNumber) => (
            <div className="flex-1 flex flex-col items-center" key={stepNumber}>
              <div
                className={`w-10 h-10 flex items-center justify-center rounded-full border-2 
                ${step === stepNumber
                  ? "border-[#F4CE14] bg-[#F4CE14] text-[#495E57]"
                  : step > stepNumber
                  ? "border-[#495E57] bg-[#495E57] text-white"
                  : "border-gray-300 bg-white text-gray-300"
                }`}
              >
                {stepNumber}
              </div>
            </div>
          ))}
          <div className="flex-1"></div>
        </div>
        <div className="overflow-hidden h-2 mb-4 text-xs flex rounded bg-gray-200">
          <div
            style={{ width: `${((step - 1) / 2) * 100}%` }}
            className="shadow-none flex flex-col text-center whitespace-nowrap text-white justify-center bg-[#495E57]"
          ></div>
        </div>
        <div className="flex text-xs sm:text-sm text-gray-500">
          <div className="flex-1 text-center">
            <div>Select Date & Time</div>
          </div>
          <div className="flex-1 text-center">
            <div>Review Details</div>
          </div>
          <div className="flex-1 text-center">
            <div>Payment</div>
          </div>
        </div>
      </div>

      {/* Error Display */}
      {error && (
        <div className="mb-6 p-4 bg-red-50 border-l-4 border-red-500 text-red-700 rounded-md">
          <p className="flex items-center">
            <svg
              className="w-5 h-5 mr-2"
              fill="currentColor"
              viewBox="0 0 20 20"
            >
              <path
                fillRule="evenodd"
                d="M18 10a8 8 0 11-16 0 8 8 0 0116 0zm-7 4a1 1 0 11-2 0 1 1 0 012 0zm-1-9a1 1 0 00-1 1v4a1 1 0 102 0V6a1 1 0 00-1-1z"
                clipRule="evenodd"
              ></path>
            </svg>
            {error}
          </p>
        </div>
      )}

      {/* Step 1: Schedule Selection - Using DateTimeSelection component */}
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

      {/* Step 2: Review Details - Using ReviewBookingDetails component */}
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

      {/* Step 3: Payment - Using PaymentConfirmation component */}
      {step === 3 && (
        <PaymentConfirmation
          serviceData={serviceData}
          payMongoFee={payMongoFee}
          appFee={appFee}
          totalPrice={totalPrice}
          debugInfo={debugInfo}
          isProcessingPayment={isProcessingPayment}
          handleBack={handleBack}
          handleSubmit={handleSubmit}
        />
      )}
    </div>
  );
};

export default BookServicePage;