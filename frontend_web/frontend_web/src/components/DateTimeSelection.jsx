import React from "react";
import { Calendar } from 'react-date-range';
import 'react-date-range/dist/styles.css';
import 'react-date-range/dist/theme/default.css';
import { format } from 'date-fns';

const DateTimeSelection = ({
  serviceData,
  bookingDate,
  setBookingDate,
  bookingTime,
  address,
  isLoading,
  isLoadingTimeSlots,
  error,
  availableTimeSlots,
  selectedTimeSlotIndex,
  setSelectedTimeSlotIndex,
  setBookingTime,
  navigate,
  payMongoFee,
  appFee,
  totalPrice,
  handleNext,
  formatTimeWithAMPM,
  fetchAvailableTimeSlots
}) => {
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

  return (
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
                maxDate={new Date(new Date().setDate(new Date().getDate() + 30))}
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

        {/* Price Summary - Updated with detailed fee breakdown */}
        <div className="mt-8 border-t border-gray-200 pt-6">
          <div className="flex justify-between mb-2">
            <span className="text-gray-600">{serviceData.serviceName} Price:</span>
            <span className="font-medium">₱{serviceData.price.toLocaleString()}</span>
          </div>
          <div className="flex justify-between mb-2">
            <span className="text-gray-600">PayMongo Fee (2.5%):</span>
            <span className="font-medium">₱{payMongoFee.toLocaleString()}</span>
          </div>
          <div className="flex justify-between mb-2">
            <span className="text-gray-600">App Fee (2.5%):</span>
            <span className="font-medium">₱{appFee.toLocaleString()}</span>
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
  );
};

export default DateTimeSelection;
