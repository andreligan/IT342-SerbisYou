import React, { useState, useEffect } from 'react';
import { motion } from 'framer-motion';
import { format, addDays, isSameDay, isBefore, startOfToday, parse } from 'date-fns';
import apiClient, { getApiUrl } from '../utils/apiConfig';

function DateTimeSelection({ service, provider, bookingData, onBookingDataChange, onNext, onCancel }) {
  const [selectedDate, setSelectedDate] = useState(null);
  const [selectedTime, setSelectedTime] = useState(null);
  const [specialInstructions, setSpecialInstructions] = useState('');
  const [availableDates, setAvailableDates] = useState([]);
  const [availableTimes, setAvailableTimes] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    const fetchSchedule = async () => {
      if (!provider || !provider.providerId) {
        setLoading(false);
        setError('Provider information is missing');
        return;
      }

      try {
        // Get provider schedule from backend
        const response = await apiClient.get(getApiUrl(`/schedules/provider/${provider.providerId}`));
        console.log('Provider schedule:', response.data);

        // Process schedule to get available dates (next 14 days)
        const today = startOfToday();
        const nextTwoWeeks = [];
        const availableDatesByDay = {};

        // Get day of week for the next 14 days
        for (let i = 0; i < 14; i++) {
          const date = addDays(today, i);
          const dayName = format(date, 'EEEE').toUpperCase();
          nextTwoWeeks.push({
            date,
            dayName
          });
        }

        if (response.data && Array.isArray(response.data)) {
          // Group schedules by day
          response.data.forEach(schedule => {
            if (schedule.isAvailable || schedule.available) {
              const dayName = schedule.dayOfWeek;
              if (!availableDatesByDay[dayName]) {
                availableDatesByDay[dayName] = [];
              }
              
              // Add the time slot
              availableDatesByDay[dayName].push({
                startTime: schedule.startTime,
                endTime: schedule.endTime
              });
            }
          });
          
          // Filter dates that have available schedules
          const availableDatesArray = nextTwoWeeks.filter(day => 
            availableDatesByDay[day.dayName] && availableDatesByDay[day.dayName].length > 0
          );
          
          setAvailableDates(availableDatesArray);
        } else {
          setAvailableDates([]);
        }

        setLoading(false);
      } catch (error) {
        console.error('Error fetching provider schedule:', error);
        setError('Failed to load provider schedule. Please try again later.');
        setLoading(false);
      }
    };

    fetchSchedule();
  }, [provider]);

  useEffect(() => {
    // When a date is selected, find available times for that day
    if (selectedDate) {
      const dayName = format(selectedDate, 'EEEE').toUpperCase();
      
      // Get existing bookings for this date and provider to exclude already booked times
      const fetchBookings = async () => {
        try {
          const formattedDate = format(selectedDate, 'yyyy-MM-dd');
          
          // Only fetch bookings if we have a provider ID
          if (provider && provider.providerId) {
            const response = await apiClient.get(
              getApiUrl(`/bookings/provider/${provider.providerId}/date/${formattedDate}`)
            );
            
            const bookedTimes = new Set(
              response.data.map(booking => booking.bookingTime)
            );
            
            // Get available time slots for this day from the schedule
            const daySchedule = availableDates.find(day => 
              isSameDay(day.date, selectedDate)
            );
            
            if (daySchedule) {
              const dayName = daySchedule.dayName;
              
              // Generate time slots from the schedule (e.g., hourly from start to end)
              const generateTimeSlots = (startTime, endTime) => {
                // Parse the time strings to get hours and minutes
                const parseTimeString = (timeStr) => {
                  if (!timeStr) return { hours: 0, minutes: 0 };
                  
                  try {
                    const [hours, minutes] = timeStr.split(':').map(Number);
                    return { hours, minutes };
                  } catch (error) {
                    console.error('Error parsing time string:', timeStr, error);
                    return { hours: 0, minutes: 0 };
                  }
                };
                
                const start = parseTimeString(startTime);
                const end = parseTimeString(endTime);
                
                const slots = [];
                // Generate hourly slots
                for (let h = start.hours; h < end.hours; h++) {
                  const timeStr = `${h.toString().padStart(2, '0')}:00`;
                  if (!bookedTimes.has(timeStr)) {
                    slots.push(timeStr);
                  }
                }
                
                return slots;
              };
              
              // Get all schedules for this day
              const daySchedules = provider.schedules?.filter(s => s.dayOfWeek === dayName) || [];
              
              // Generate all available time slots
              let allTimeSlots = [];
              if (daySchedules.length > 0) {
                daySchedules.forEach(schedule => {
                  if (schedule.isAvailable || schedule.available) {
                    const slots = generateTimeSlots(schedule.startTime, schedule.endTime);
                    allTimeSlots = [...allTimeSlots, ...slots];
                  }
                });
              } else {
                // If no specific schedules are found, use a default schedule
                const defaultStartTime = '08:00';
                const defaultEndTime = '17:00';
                allTimeSlots = generateTimeSlots(defaultStartTime, defaultEndTime);
              }
              
              setAvailableTimes(allTimeSlots);
            } else {
              setAvailableTimes([]);
            }
          }
        } catch (error) {
          console.error('Error fetching bookings for date:', error);
          setError('Failed to check time availability. Please try again.');
        }
      };
      
      fetchBookings();
    } else {
      setAvailableTimes([]);
    }
  }, [selectedDate, provider, availableDates]);

  const handleDateClick = (date) => {
    setSelectedDate(date);
    setSelectedTime(null);
  };

  const handleTimeClick = (time) => {
    setSelectedTime(time);
  };

  const handleContinue = () => {
    if (!selectedDate || !selectedTime) {
      alert('Please select both date and time to continue');
      return;
    }

    const formattedDate = format(selectedDate, 'yyyy-MM-dd');
    
    onBookingDataChange({
      selectedDate: formattedDate,
      selectedTime,
      specialInstructions
    });
    
    onNext();
  };

  if (loading) {
    return (
      <div className="flex justify-center items-center min-h-[400px]">
        <div className="w-12 h-12 border-4 border-t-transparent border-[#F4CE14] rounded-full animate-spin"></div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="bg-red-50 border border-red-200 p-6 rounded-lg text-center">
        <svg className="w-12 h-12 text-red-500 mx-auto mb-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
          <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 8v4m0 4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
        </svg>
        <h3 className="text-lg font-medium text-red-800">Error Loading Schedule</h3>
        <p className="text-red-700 mt-2">{error}</p>
      </div>
    );
  }

  return (
    <motion.div 
      className="bg-white rounded-lg shadow-lg overflow-hidden"
      initial={{ opacity: 0, y: 20 }}
      animate={{ opacity: 1, y: 0 }}
      transition={{ duration: 0.5 }}
    >
      <div className="p-6 border-b border-gray-200">
        <h2 className="text-2xl font-bold text-[#495E57]">Select Date & Time</h2>
        <p className="text-gray-600">Choose when you'd like to book this service</p>
      </div>
      
      <div className="p-6">
        <div className="grid grid-cols-1 md:grid-cols-2 gap-8">
          {/* Service Summary */}
          <div className="bg-gray-50 p-4 rounded-lg">
            <h3 className="font-semibold text-lg mb-2 text-[#495E57]">Service Details</h3>
            <div className="flex items-start mb-4">
              <div>
                <p className="font-medium">{service?.serviceName}</p>
                <p className="text-sm text-gray-600 mt-1">{service?.serviceDescription}</p>
                <div className="mt-3 flex items-center">
                  <svg className="w-5 h-5 text-gray-500 mr-2" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 8c-1.657 0-3 .895-3 2s1.343 2 3 2 3 .895 3 2-1.343 2-3 2m0-8c1.11 0 2.08.402 2.599 1M12 8V7m0 1v8m0 0v1m0-1c-1.11 0-2.08-.402-2.599-1M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
                  </svg>
                  <span className="font-medium">₱{service?.price}</span>
                </div>
                <div className="mt-2 flex items-center">
                  <svg className="w-5 h-5 text-gray-500 mr-2" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z" />
                  </svg>
                  <span className="text-gray-600">{service?.durationEstimate}</span>
                </div>
                <div className="mt-2 flex items-center">
                  <svg className="w-5 h-5 text-gray-500 mr-2" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z" />
                  </svg>
                  <span className="text-gray-600">{provider?.businessName || `${provider?.firstName} ${provider?.lastName}`}</span>
                </div>
              </div>
            </div>
          </div>

          {/* Date Selection */}
          <div>
            <h3 className="font-semibold text-lg mb-3 text-[#495E57]">Select a Date</h3>
            <div className="grid grid-cols-2 sm:grid-cols-3 gap-2 mb-6">
              {availableDates.length > 0 ? (
                availableDates.map((day, index) => (
                  <motion.button
                    key={index}
                    type="button"
                    className={`p-3 rounded-lg flex flex-col items-center justify-center border transition-colors ${
                      selectedDate && isSameDay(day.date, selectedDate)
                        ? 'bg-[#F4CE14] border-[#F4CE14] text-[#495E57]'
                        : 'border-gray-200 hover:border-[#F4CE14]'
                    }`}
                    onClick={() => handleDateClick(day.date)}
                    whileHover={{ scale: 1.05 }}
                    whileTap={{ scale: 0.95 }}
                  >
                    <span className="text-xs uppercase">{format(day.date, 'EEE')}</span>
                    <span className="text-lg font-bold">{format(day.date, 'd')}</span>
                    <span className="text-xs">{format(day.date, 'MMM')}</span>
                  </motion.button>
                ))
              ) : (
                <div className="col-span-3 text-center py-4 text-gray-500">
                  No available dates in the next 2 weeks.
                </div>
              )}
            </div>

            {/* Time Selection */}
            {selectedDate && (
              <>
                <h3 className="font-semibold text-lg mb-3 text-[#495E57]">Select a Time</h3>
                <div className="grid grid-cols-3 sm:grid-cols-4 gap-2 mb-6">
                  {availableTimes.length > 0 ? (
                    availableTimes.map((time, index) => (
                      <motion.button
                        key={index}
                        type="button"
                        className={`p-3 rounded-lg text-center border transition-colors ${
                          selectedTime === time
                            ? 'bg-[#F4CE14] border-[#F4CE14] text-[#495E57]'
                            : 'border-gray-200 hover:border-[#F4CE14]'
                        }`}
                        onClick={() => handleTimeClick(time)}
                        whileHover={{ scale: 1.05 }}
                        whileTap={{ scale: 0.95 }}
                      >
                        {time}
                      </motion.button>
                    ))
                  ) : (
                    <div className="col-span-4 text-center py-4 text-gray-500">
                      No available time slots for this date.
                    </div>
                  )}
                </div>
              </>
            )}
          </div>
        </div>

        {/* Special Instructions */}
        <div className="mt-6">
          <h3 className="font-semibold text-lg mb-3 text-[#495E57]">Special Instructions (Optional)</h3>
          <textarea
            value={specialInstructions}
            onChange={(e) => setSpecialInstructions(e.target.value)}
            placeholder="Add any special requests or instructions for the service provider..."
            className="w-full p-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-[#F4CE14] focus:border-[#F4CE14] h-32"
          />
        </div>
        
        <div className="mt-8 flex justify-between">
          <motion.button
            type="button"
            className="px-6 py-2 border border-gray-300 rounded-lg text-gray-700 hover:bg-gray-50 transition-colors"
            onClick={onCancel}
            whileHover={{ scale: 1.05 }}
            whileTap={{ scale: 0.95 }}
          >
            Cancel
          </motion.button>
          
          <motion.button
            type="button"
            className={`px-8 py-3 rounded-lg text-[#495E57] font-medium transition-colors ${
              selectedDate && selectedTime 
                ? 'bg-[#F4CE14] hover:bg-[#e6c013]' 
                : 'bg-gray-300 cursor-not-allowed'
            }`}
            onClick={handleContinue}
            disabled={!selectedDate || !selectedTime}
            whileHover={selectedDate && selectedTime ? { scale: 1.05 } : {}}
            whileTap={selectedDate && selectedTime ? { scale: 0.95 } : {}}
          >
            Continue
          </motion.button>
        </div>
      </div>
    </motion.div>
  );
}

export default DateTimeSelection;
