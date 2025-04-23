import React, { useState, useEffect } from "react";
import { useNavigate, useLocation } from "react-router-dom";
import axios from "axios";

const BookServicePage = () => {
  const navigate = useNavigate();
  const location = useLocation();
  const [step, setStep] = useState(1); // Track the current step
  const [bookingDate, setBookingDate] = useState("");
  const [bookingTime, setBookingTime] = useState(""); // Track the selected time
  const [isTimePickerOpen, setIsTimePickerOpen] = useState(false); // Track custom time picker state
  const [address, setAddress] = useState(""); // Address field
  const [isLoading, setIsLoading] = useState(true); // Loading state for address
  const [error, setError] = useState(null); // Error state for address fetching
  const [paymentMethod, setPaymentMethod] = useState(""); // Payment method selection
  const [isProcessingPayment, setIsProcessingPayment] = useState(false);

  // Get service data from navigation state
  const serviceData = location.state?.service || {
    serviceName: "Service",
    price: 1000, // Default price if no service data
  };

  // Fetch all addresses and match with the logged-in user
  useEffect(() => {
    const fetchAddress = async () => {
      try {
        const token = localStorage.getItem("authToken") || sessionStorage.getItem("authToken");
        if (!token) {
          console.error("No authentication token found");
          setError("Authentication token not found.");
          setIsLoading(false);
          return;
        }

        // Decode the token to get the user ID (assuming JWT structure)
        const decodedToken = JSON.parse(atob(token.split(".")[1]));
        const userId = decodedToken.userId;

        // Fetch all addresses
        const response = await axios.get("/api/addresses/getAll", {
          headers: {
            Authorization: `Bearer ${token}`,
          },
        });

        // Find the address that matches the logged-in user
        const matchedAddress = response.data.find(
          (addr) => addr.userAuth?.userId === userId
        );

        if (matchedAddress) {
          setAddress(
            `${matchedAddress.streetName}, ${matchedAddress.barangay}, ${matchedAddress.city}, ${matchedAddress.province}`
          );
        } else {
          setError("No address found for the logged-in user.");
        }
        setIsLoading(false);
      } catch (error) {
        console.error("Error fetching addresses:", error);
        setError("An error occurred while fetching the address.");
        setIsLoading(false);
      }
    };

    fetchAddress();
  }, []);

  const formatTimeWithAMPM = (time) => {
    const [hours, minutes] = time.split(":");
    const hoursInt = parseInt(hours, 10);
    const period = hoursInt >= 12 ? "PM" : "AM";
    const formattedHours = hoursInt % 12 || 12; // Convert 0 to 12 for 12-hour format
    return `${formattedHours}:${minutes} ${period}`;
  };

  const handleNext = () => {
    if (step === 1 && (!bookingDate || !bookingTime || !address)) {
      alert("Please fill in all fields.");
      return;
    }
    if (step === 2) {
      setStep(3); // Move to payment method selection
      return;
    }
    if (step === 3 && !paymentMethod) {
      alert("Please select a payment method.");
      return;
    }
    if (step === 3 && paymentMethod === "gcash") {
      handleGCashPayment();
      return;
    }
    // For cash payment or other steps
    setStep(step + 1);
  };

  const handlePrevious = () => {
    setStep(step - 1);
  };

  const handleBooking = () => {
    alert("Booking Successful!");
    navigate("/customerHomePage");
  };

  const calculateFees = () => {
    const serviceFee = parseFloat(serviceData.price) || 1000;
    const payMongoFee = serviceFee * 0.025; // 2.5% PayMongo fee
    const appFee = serviceFee * 0.02; // 2% app fee
    const total = serviceFee + payMongoFee + appFee;
    return { serviceFee, payMongoFee, appFee, total };
  };

  const handleGCashPayment = async () => {
    try {
      setIsProcessingPayment(true);
      const { total } = calculateFees();
      
      const token = localStorage.getItem("authToken") || sessionStorage.getItem("authToken");
      if (!token) {
        alert("You must be logged in to make a payment.");
        setIsProcessingPayment(false);
        return;
      }

      // Create a checkout session with PayMongo
      const response = await axios.get('http://localhost:8080/api/test-gcash-payment', {
        headers: {
          Authorization: `Bearer ${token}`
        }
      });

      const checkoutUrl = response.data.checkout_url;
      
      // Save booking information to local storage so we can retrieve it after payment
      localStorage.setItem('pendingBooking', JSON.stringify({
        serviceId: serviceData.serviceId,
        serviceName: serviceData.serviceName,
        providerId: serviceData.provider?.providerId,
        bookingDate,
        bookingTime,
        address,
        amount: total,
        paymentMethod: 'gcash'
      }));

      // Redirect to PayMongo checkout
      window.location.href = checkoutUrl;
    } catch (error) {
      console.error("Error initiating GCash payment:", error);
      alert("Failed to initiate payment. Please try again later.");
      setIsProcessingPayment(false);
    }
  };

  const { serviceFee, payMongoFee, appFee, total } = calculateFees();

  return (
    <div className="p-10">
      <h1 className="text-4xl font-bold text-[#495E57] mb-8 text-center">
        Book Service
      </h1>

      {/* Step Indicator */}
      <div className="flex justify-center items-center mb-8">
        <div
          className={`w-10 h-10 rounded-full flex justify-center items-center text-white font-bold ${
            step >= 1 ? "bg-yellow-400" : "bg-[#495E57]"
          }`}
        >
          1
        </div>
        <div className="w-12 h-[2px] bg-[#495E57] border-dashed"></div>
        <div
          className={`w-10 h-10 rounded-full flex justify-center items-center text-white font-bold ${
            step >= 2 ? "bg-yellow-400" : "bg-[#495E57]"
          }`}
        >
          2
        </div>
        <div className="w-12 h-[2px] bg-[#495E57] border-dashed"></div>
        <div
          className={`w-10 h-10 rounded-full flex justify-center items-center text-white font-bold ${
            step >= 3 ? "bg-yellow-400" : "bg-[#495E57]"
          }`}
        >
          3
        </div>
        <div className="w-12 h-[2px] bg-[#495E57] border-dashed"></div>
        <div
          className={`w-10 h-10 rounded-full flex justify-center items-center text-white font-bold ${
            step >= 4 ? "bg-yellow-400" : "bg-[#495E57]"
          }`}
        >
          4
        </div>
      </div>

      {/* Step 1: Booking Form */}
      {step === 1 && (
        <div className="bg-white rounded-lg shadow-lg p-6">
          <h2 className="text-xl font-bold text-[#495E57] mb-4">Booking Form</h2>
          {/* Booking Date Field */}
          <div className="mb-4">
            <label className="block text-sm font-medium text-gray-700 mb-1">
              Booking Date
            </label>
            <input
              type="date"
              value={bookingDate}
              onChange={(e) => setBookingDate(e.target.value)}
              className="w-full border border-gray-300 rounded-lg px-3 py-2 focus:outline-none focus:ring-2 focus:ring-[#495E57]"
              required
            />
          </div>

          {/* Booking Time Field */}
          <div className="mb-4">
            <label className="block text-sm font-medium text-gray-700 mb-1">
              Booking Time
            </label>
            <button
              onClick={() => setIsTimePickerOpen(true)}
              className="w-full bg-[#495E57] text-white py-2 rounded-lg hover:bg-[#3A4A47] transition"
            >
              Select Time
            </button>
            {bookingTime && (
              <p className="text-sm text-gray-700 mt-2">
                Selected Time: <strong>{formatTimeWithAMPM(bookingTime)}</strong>
              </p>
            )}
          </div>

          {/* Address Field */}
          <div className="mb-4">
            <label className="block text-sm font-medium text-gray-700 mb-1">
              Address
            </label>
            {isLoading ? (
              <p className="text-sm text-gray-500">Loading address...</p>
            ) : error ? (
              <p className="text-sm text-red-500">{error}</p>
            ) : (
              <input
                type="text"
                value={address}
                onChange={(e) => setAddress(e.target.value)}
                className="w-full border border-gray-300 rounded-lg px-3 py-2 focus:outline-none focus:ring-2 focus:ring-[#495E57]"
                required
              />
            )}
          </div>

          {/* Next Button */}
          <button
            onClick={handleNext}
            className="w-full bg-[#495E57] text-white py-2 rounded-lg hover:bg-[#3A4A47] transition"
          >
            Next
          </button>
        </div>
      )}

      {/* Step 2: Price Details */}
      {step === 2 && (
        <div className="bg-white rounded-lg shadow-lg p-6">
          <h2 className="text-xl font-bold text-[#495E57] mb-4">Price Details</h2>
          <p className="text-xl font-bold mb-4">Service: {serviceData.serviceName}</p>
          <p className="text-sm mb-2">
            <strong>Service Fee:</strong> Php {serviceFee.toFixed(2)}
          </p>
          <p className="text-sm mb-2">
            <strong>PayMongo Fee (2.5%):</strong> Php {payMongoFee.toFixed(2)}
          </p>
          <p className="text-sm mb-2">
            <strong>App Fee (2%):</strong> Php {appFee.toFixed(2)}
          </p>
          <p className="text-sm mb-4">
            <strong>Total:</strong> Php {total.toFixed(2)}
          </p>
          <div className="flex justify-between">
            <button
              onClick={handlePrevious}
              className="bg-[#495E57] text-white py-2 px-4 rounded-lg hover:bg-[#3A4A47] transition"
            >
              Previous
            </button>
            <button
              onClick={handleNext}
              className="bg-[#495E57] text-white py-2 px-4 rounded-lg hover:bg-[#3A4A47] transition"
            >
              Next
            </button>
          </div>
        </div>
      )}

      {/* Step 3: Payment Method Selection */}
      {step === 3 && (
        <div className="bg-white rounded-lg shadow-lg p-6">
          <h2 className="text-xl font-bold text-[#495E57] mb-4">Select Payment Method</h2>
          
          <div className="flex flex-col space-y-4 mb-6">
            <div 
              className={`border p-4 rounded-lg cursor-pointer ${
                paymentMethod === "cash" 
                  ? "border-[#495E57] bg-[#f0f5f4]" 
                  : "border-gray-200 hover:border-[#495E57]"
              }`}
              onClick={() => setPaymentMethod("cash")}
            >
              <div className="flex items-center">
                <div className={`w-5 h-5 rounded-full border ${
                  paymentMethod === "cash" 
                    ? "border-[#495E57]" 
                    : "border-gray-400"
                  } flex items-center justify-center mr-3`}
                >
                  {paymentMethod === "cash" && (
                    <div className="w-3 h-3 bg-[#495E57] rounded-full"></div>
                  )}
                </div>
                <div>
                  <h3 className="font-medium text-gray-800">Cash Payment</h3>
                  <p className="text-sm text-gray-500">Pay in cash when the service is completed</p>
                </div>
              </div>
            </div>
            
            <div 
              className={`border p-4 rounded-lg cursor-pointer ${
                paymentMethod === "gcash" 
                  ? "border-[#495E57] bg-[#f0f5f4]" 
                  : "border-gray-200 hover:border-[#495E57]"
              }`}
              onClick={() => setPaymentMethod("gcash")}
            >
              <div className="flex items-center">
                <div className={`w-5 h-5 rounded-full border ${
                  paymentMethod === "gcash" 
                    ? "border-[#495E57]" 
                    : "border-gray-400"
                  } flex items-center justify-center mr-3`}
                >
                  {paymentMethod === "gcash" && (
                    <div className="w-3 h-3 bg-[#495E57] rounded-full"></div>
                  )}
                </div>
                <div>
                  <h3 className="font-medium text-gray-800">GCash</h3>
                  <p className="text-sm text-gray-500">Pay online using GCash</p>
                </div>
                <img src="/gcash-logo.png" alt="GCash" className="h-8 ml-auto" />
              </div>
            </div>
          </div>

          <div className="flex justify-between">
            <button
              onClick={handlePrevious}
              className="bg-[#495E57] text-white py-2 px-4 rounded-lg hover:bg-[#3A4A47] transition"
              disabled={isProcessingPayment}
            >
              Previous
            </button>
            <button
              onClick={handleNext}
              className={`bg-[#495E57] text-white py-2 px-4 rounded-lg hover:bg-[#3A4A47] transition ${
                isProcessingPayment ? "opacity-50 cursor-not-allowed" : ""
              }`}
              disabled={isProcessingPayment}
            >
              {isProcessingPayment ? (
                <span className="flex items-center">
                  <svg className="animate-spin -ml-1 mr-3 h-5 w-5 text-white" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24">
                    <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4"></circle>
                    <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
                  </svg>
                  Processing...
                </span>
              ) : (
                "Proceed to Payment"
              )}
            </button>
          </div>
        </div>
      )}

      {/* Step 4: Booking Success */}
      {step === 4 && (
        <div className="text-center bg-white rounded-lg shadow-lg p-6">
          <div className="text-5xl text-green-500 mb-4">
            <i className="fas fa-check-circle"></i>
          </div>
          <h2 className="text-2xl font-bold text-[#495E57] mb-4">
            Booking Successful!
          </h2>
          <p className="text-gray-600 mb-6">
            Your booking has been confirmed. The service provider will contact you shortly.
          </p>
          <button
            onClick={() => navigate("/customerHomePage")}
            className="bg-[#495E57] text-white py-2 px-6 rounded-lg hover:bg-[#3A4A47] transition"
          >
            Go back to Home
          </button>
        </div>
      )}

      {/* Custom Time Picker */}
      {isTimePickerOpen && (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
          <div className="bg-white rounded-lg shadow-lg p-6 w-96">
            <h2 className="text-xl font-bold text-[#495E57] mb-4">Select Time</h2>
            <input
              type="time"
              value={bookingTime}
              onChange={(e) => setBookingTime(e.target.value)}
              className="w-full border border-gray-300 rounded-lg px-3 py-2 focus:outline-none focus:ring-2 focus:ring-[#495E57] mb-4"
              required
            />
            <div className="flex justify-end gap-4">
              <button
                onClick={() => setIsTimePickerOpen(false)}
                className="bg-gray-300 text-gray-800 py-2 px-4 rounded-lg hover:bg-gray-400 transition"
              >
                Cancel
              </button>
              <button
                onClick={() => setIsTimePickerOpen(false)}
                className="bg-[#495E57] text-white py-2 px-4 rounded-lg hover:bg-[#3A4A47] transition"
              >
                Save
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

export default BookServicePage;