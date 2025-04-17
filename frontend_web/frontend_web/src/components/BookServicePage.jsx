import React, { useState, useEffect } from "react";
import { useNavigate } from "react-router-dom";
import axios from "axios";

const BookServicePage = () => {
  const navigate = useNavigate();
  const [step, setStep] = useState(1); // Track the current step
  const [bookingDate, setBookingDate] = useState("");
  const [bookingTime, setBookingTime] = useState(""); // Track the selected time
  const [isTimePickerOpen, setIsTimePickerOpen] = useState(false); // Track custom time picker state
  const [address, setAddress] = useState(""); // Address field
  const [isLoading, setIsLoading] = useState(true); // Loading state for address
  const [error, setError] = useState(null); // Error state for address fetching

  const service = {
    serviceName: "Leak Repairs",
    priceRange: 1000, // Example service fee
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
    const serviceFee = service.priceRange;
    const payMongoFee = serviceFee * 0.025; // 2.5% PayMongo fee
    const appFee = serviceFee * 0.02; // 2% app fee
    const total = serviceFee + payMongoFee + appFee;
    return { serviceFee, payMongoFee, appFee, total };
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