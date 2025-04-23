import React, { useState, useEffect } from "react";
import { useNavigate, useLocation } from "react-router-dom";
import axios from "axios";
import { Calendar } from 'react-date-range';
import 'react-date-range/dist/styles.css';
import 'react-date-range/dist/theme/default.css';
import { format, addDays } from 'date-fns';

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
  const [paymentMethod, setPaymentMethod] = useState("card");
  const [isProcessingPayment, setIsProcessingPayment] = useState(false);
  const [serviceFee, setServiceFee] = useState(0);
  const [totalPrice, setTotalPrice] = useState(0);
  const [selectedTimeSlotIndex, setSelectedTimeSlotIndex] = useState(null);

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
  }, [serviceData]);

  // Set service fee and total price when service price is available
  useEffect(() => {
    if (serviceData && serviceData.price) {
      const price = parseFloat(serviceData.price);
      const fee = price * 0.05; // 5% service fee
      setServiceFee(fee);
      setTotalPrice(price + fee);
    }
  }, [serviceData]);

  // Fetch user address
  useEffect(() => {
    const fetchAddress = async () => {
      try {
        const token = localStorage.getItem("authToken") || sessionStorage.getItem("authToken");
        if (!token) {
          setError("Authentication token not found.");
          setIsLoading(false);
          return;
        }

        // Decode the token to get the user ID
        const decodedToken = JSON.parse(atob(token.split(".")[1]));
        const userId = decodedToken.userId;

        // Fetch all addresses
        const response = await axios.get("/api/addresses/getAll", {
          headers: { Authorization: `Bearer ${token}` },
        });

        // Find the address for this user
        const matchedAddress = response.data.find(
          (addr) => addr.userAuth?.userId === userId
        );

        if (matchedAddress) {
          setAddress(
            `${matchedAddress.streetName}, ${matchedAddress.barangay}, ${matchedAddress.city}, ${matchedAddress.province}`
          );
        } else {
          setError("No address found for your account.");
        }
        setIsLoading(false);
      } catch (error) {
        console.error("Error fetching addresses:", error);
        setError("An error occurred while fetching your address information.");
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

  // Helper function to check if a schedule is available
  const isScheduleAvailable = (schedule) => {
    if (schedule.isAvailable !== undefined) return schedule.isAvailable;
    if (schedule.available !== undefined) return schedule.available;
    return true;  // Default to true if property is missing
  };

  // Handle date change
  const handleDateChange = (date) => {
    setBookingDate(date);
    setBookingTime(""); // Reset time when date changes
    setSelectedTimeSlotIndex(null);
  };

  // Handle time slot selection
  const handleTimeSlotSelect = (timeSlot, index) => {
    setBookingTime(timeSlot.value);
    setSelectedTimeSlotIndex(index);
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
    
    try {
      const token = localStorage.getItem("authToken") || sessionStorage.getItem("authToken");
      
      // Create booking object
      const bookingRequest = {
        service: { serviceId: serviceData.serviceId },
        bookingDate: format(bookingDate, 'yyyy-MM-dd'),
        bookingTime: bookingTime.split('-')[0], // Use the start time
        status: "Pending",
        totalCost: totalPrice,
        note: note
        // Add any additional fields needed
      };
      
      // Send booking request
      const response = await axios.post('/api/bookings/postBooking', bookingRequest, {
        headers: { Authorization: `Bearer ${token}` }
      });
      
      console.log('Booking successful:', response.data);
      
      // Handle different payment methods
      if (paymentMethod === 'card') {
        // Redirect to payment page (this would be replaced with your actual payment flow)
        navigate('/payment-success');
      } else {
        // For cash payment, simply show confirmation
        navigate('/payment-success', { state: { bookingData: response.data } });
      }
    } catch (error) {
      console.error("Error creating booking:", error);
      setError("Failed to create your booking. Please try again.");
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

      {/* Step 1: Schedule Selection */}
      {step === 1 && (
        <div className="bg-white rounded-lg shadow-lg overflow-hidden">
          <div className="p-5 bg-[#495E57] text-white">
            <h2 className="text-xl font-semibold">Select Date & Time</h2>
            <p className="text-sm opacity-90">Choose when you need the service</p>
          </div>

          <div className="p-6">
            {/* Show debugging info during development */}
            <div className="mb-4 p-2 border border-gray-200 rounded bg-gray-50">
              <p className="text-sm text-gray-500">Debug info:</p>
              <p className="text-sm">Provider ID: {serviceData?.provider?.providerId || 'Not set'}</p>
              <p className="text-sm">Selected date: {format(bookingDate, 'yyyy-MM-dd')}</p>
              <p className="text-sm">Day of week: {format(bookingDate, 'EEEE')}</p>
              <p className="text-sm">Available slots: {availableTimeSlots.length}</p>
            </div>

            <div className="grid grid-cols-1 md:grid-cols-2 gap-8">
              {/* Date Selection */}
              <div>
                <h3 className="font-medium text-lg mb-4 text-gray-700">Select Date</h3>
                <div className="border border-gray-200 rounded-lg overflow-hidden">
                  <Calendar
                    date={bookingDate}
                    onChange={handleDateChange}
                    minDate={new Date()}
                    maxDate={addDays(new Date(), 30)}
                    color="#495E57"
                  />
                </div>
              </div>

              {/* Time Selection */}
              <div>
                <h3 className="font-medium text-lg mb-4 text-gray-700">
                  Select Time
                  <span className="ml-2 text-sm font-normal text-gray-500">
                    ({format(bookingDate, 'MMM dd, yyyy')})
                  </span>
                </h3>
                
                {isLoadingTimeSlots ? (
                  <div className="flex items-center justify-center h-64 bg-gray-50 rounded-lg">
                    <div className="flex flex-col items-center">
                      <div className="w-8 h-8 border-4 border-[#F4CE14] border-t-transparent rounded-full animate-spin"></div>
                      <p className="mt-3 text-gray-500">Loading available time slots...</p>
                    </div>
                  </div>
                ) : availableTimeSlots.length > 0 ? (
                  <div className="grid grid-cols-2 sm:grid-cols-3 gap-3">
                    {availableTimeSlots.map((slot, index) => (
                      <button
                        key={index}
                        type="button"
                        onClick={() => handleTimeSlotSelect(slot, index)}
                        className={`py-2 px-3 border rounded-lg text-center transition-colors
                          ${selectedTimeSlotIndex === index
                            ? "bg-[#F4CE14] border-[#F4CE14] text-[#495E57] font-medium"
                            : "bg-white border-gray-300 hover:border-[#F4CE14] text-gray-700"
                          }
                        `}
                      >
                        {slot.label}
                      </button>
                    ))}
                  </div>
                ) : (
                  <div className="flex flex-col items-center justify-center h-64 bg-gray-50 rounded-lg">
                    <svg
                      className="w-12 h-12 text-gray-400"
                      fill="none"
                      stroke="currentColor"
                      viewBox="0 0 24 24"
                    >
                      <path
                        strokeLinecap="round"
                        strokeLinejoin="round"
                        strokeWidth="2"
                        d="M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z"
                      ></path>
                    </svg>
                    <p className="mt-2 text-gray-500">No available time slots for this date</p>
                    <p className="text-sm text-gray-400">Please select another date</p>
                    <button 
                      onClick={fetchAvailableTimeSlots}
                      className="mt-4 px-4 py-2 bg-gray-200 rounded hover:bg-gray-300"
                    >
                      Retry Loading Times
                    </button>
                  </div>
                )}

                {/* Service Location */}
                <div className="mt-8">
                  <h3 className="font-medium text-lg mb-2 text-gray-700">Service Location</h3>
                  {isLoading ? (
                    <div className="h-12 bg-gray-100 animate-pulse rounded"></div>
                  ) : (
                    <div className="p-3 bg-gray-50 border border-gray-200 rounded-lg">
                      <div className="flex items-start">
                        <div className="mr-3 mt-1">
                          <svg
                            className="w-5 h-5 text-[#495E57]"
                            fill="none"
                            stroke="currentColor"
                            viewBox="0 0 24 24"
                          >
                            <path
                              strokeLinecap="round"
                              strokeLinejoin="round"
                              strokeWidth="2"
                              d="M17.657 16.657L13.414 20.9a1.998 1.998 0 01-2.827 0l-4.244-4.243a8 8 0 1111.314 0z"
                            ></path>
                            <path
                              strokeLinecap="round"
                              strokeLinejoin="round"
                              strokeWidth="2"
                              d="M15 11a3 3 0 11-6 0 3 3 0 016 0z"
                            ></path>
                          </svg>
                        </div>
                        <div>
                          <p className="text-gray-700">{address || "No address available"}</p>
                          <button
                            onClick={() => navigate('/customerProfile/address')}
                            className="text-sm text-[#495E57] hover:underline mt-1"
                          >
                            {address ? "Change address" : "Add an address"}
                          </button>
                        </div>
                      </div>
                    </div>
                  )}
                </div>
              </div>
            </div>

            {/* Price Summary */}
            <div className="mt-8 border-t border-gray-200 pt-6">
              <div className="flex justify-between mb-2">
                <span className="text-gray-600">{serviceData.serviceName} Price:</span>
                <span className="font-medium">₱{serviceData.price.toLocaleString()}</span>
              </div>
              <div className="flex justify-between mb-2">
                <span className="text-gray-600">Service Fee:</span>
                <span className="font-medium">₱{serviceFee.toLocaleString()}</span>
              </div>
              <div className="flex justify-between text-lg font-bold mt-2 pt-2 border-t">
                <span className="text-gray-800">Total:</span>
                <span className="text-[#495E57]">₱{totalPrice.toLocaleString()}</span>
              </div>
            </div>

            {/* Navigation Buttons */}
            <div className="mt-8 flex justify-end">
              <button
                onClick={handleNext}
                disabled={!bookingDate || !bookingTime || !address}
                className={`px-6 py-3 bg-[#F4CE14] text-[#495E57] rounded-lg font-medium shadow-sm hover:bg-yellow-300 transition-colors
                  ${(!bookingDate || !bookingTime || !address) ? 'opacity-50 cursor-not-allowed' : ''}
                `}
              >
                Continue to Review
              </button>
            </div>
          </div>
        </div>
      )}

      {/* Step 2: Review Details */}
      {step === 2 && (
        // Review details UI would go here
        <div className="bg-white rounded-lg shadow-lg p-6">
          <h2 className="text-xl font-semibold text-[#495E57] mb-4">Review Your Booking</h2>
          
          {/* Navigation buttons for step 2 */}
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
      )}

      {/* Step 3: Payment */}
      {step === 3 && (
        // Payment UI would go here
        <div className="bg-white rounded-lg shadow-lg p-6">
          <h2 className="text-xl font-semibold text-[#495E57] mb-4">Payment</h2>
          
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
      )}
    </div>
  );
};

export default BookServicePage;