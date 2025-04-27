import React, { useState } from "react";
import { Calendar } from 'react-date-range';
import 'react-date-range/dist/styles.css';
import 'react-date-range/dist/theme/default.css';
import { format, isSameDay } from 'date-fns';

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
  // Show/hide debug info
  const [showDebug, setShowDebug] = useState(false);
  
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
    <div className="bg-white rounded-2xl shadow-xl overflow-hidden border border-gray-100">
      {/* Enhanced Header with Service Info */}
      <div className="relative bg-gradient-to-r from-[#445954] to-[#495E57] text-white">
        <div className="absolute top-0 right-0 w-64 h-64 bg-[#F4CE14]/5 rounded-full -translate-x-16 -translate-y-32 blur-3xl"></div>
        <div className="absolute bottom-0 left-1/4 w-40 h-40 bg-[#F4CE14]/10 rounded-full -translate-y-10 blur-2xl"></div>
        
        <div className="relative p-6 sm:p-8 z-10">
          <div className="flex flex-col md:flex-row md:items-center gap-4">
            <div className="flex-1">
              <div className="text-[#F4CE14] text-sm font-medium mb-1">Step 1 of 3</div>
              <h2 className="text-2xl font-bold">Schedule Your Service</h2>
              <p className="text-sm text-white/80 mt-1 max-w-lg">
                Please select your preferred date and time for 
                <span className="font-medium text-[#F4CE14]"> {serviceData.serviceName}</span>
              </p>
            </div>
            
            <div className="flex-shrink-0 bg-white/10 backdrop-blur-sm p-3 rounded-xl border border-white/20">
              <div className="text-xs text-white/70">Service Provider</div>
              <div className="flex items-center gap-2.5 mt-1">
                {serviceData.provider?.profileImage ? (
                  <img 
                    src={`http://localhost:8080${serviceData.provider.profileImage}`}
                    alt={`${serviceData.provider?.firstName} ${serviceData.provider?.lastName}`} 
                    className="w-10 h-10 rounded-full object-cover border-2 border-[#F4CE14]" 
                  />
                ) : (
                  <div className="w-10 h-10 rounded-full bg-[#F4CE14]/30 flex items-center justify-center">
                    <svg xmlns="http://www.w3.org/2000/svg" className="h-5 w-5 text-[#F4CE14]" viewBox="0 0 20 20" fill="currentColor">
                      <path fillRule="evenodd" d="M10 9a3 3 0 100-6 3 3 0 000 6zm-7 9a7 7 0 1114 0H3z" clipRule="evenodd" />
                    </svg>
                  </div>
                )}
                <div>
                  <div className="font-medium text-white">
                    {serviceData.provider?.firstName} {serviceData.provider?.lastName}
                  </div>
                  {serviceData.provider?.verified && (
                    <div className="flex items-center gap-1 text-xs text-[#F4CE14]">
                      <svg xmlns="http://www.w3.org/2000/svg" className="h-3.5 w-3.5" viewBox="0 0 20 20" fill="currentColor">
                        <path fillRule="evenodd" d="M6.267 3.455a3.066 3.066 0 001.745-.723 3.066 3.066 0 013.976 0 3.066 3.066 0 001.745.723 3.066 3.066 0 012.812 2.812c.051.643.304 1.254.723 1.745a3.066 3.066 0 010 3.976 3.066 3.066 0 00-.723 1.745 3.066 3.066 0 01-2.812 2.812 3.066 3.066 0 00-1.745.723 3.066 3.066 0 01-3.976 0 3.066 3.066 0 00-1.745-.723 3.066 3.066 0 01-2.812-2.812 3.066 3.066 0 00-.723-1.745 3.066 3.066 0 010-3.976 3.066 3.066 0 00.723-1.745 3.066 3.066 0 012.812-2.812zm7.44 5.252a1 1 0 00-1.414-1.414L9 10.586 7.707 9.293a1 1 0 00-1.414 1.414l2 2a1 1 0 001.414 0l4-4z" clipRule="evenodd" />
                      </svg>
                      Verified
                    </div>
                  )}
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <div className="p-6 sm:p-8">
        {/* Debug Panel (Toggleable) */}
        <div className="mb-4">
          <button 
            onClick={() => setShowDebug(!showDebug)}
            className="text-xs px-3 py-1.5 text-gray-500 bg-gray-50 hover:bg-gray-100 rounded-lg transition-colors duration-150 focus:outline-none"
          >
            {showDebug ? "Hide Debug Info" : "Show Debug Info"}
          </button>
          
          {showDebug && (
            <div className="mt-2 p-3 border border-gray-200 rounded-lg bg-gray-50">
              <p className="text-xs text-gray-500 font-medium mb-1">Debug Information:</p>
              <div className="grid grid-cols-2 gap-2 text-xs text-gray-600">
                <div>Provider ID: <span className="font-mono">{serviceData?.provider?.providerId || 'Not set'}</span></div>
                <div>Selected date: <span className="font-mono">{format(bookingDate, 'yyyy-MM-dd')}</span></div>
                <div>Day of week: <span className="font-mono">{format(bookingDate, 'EEEE')}</span></div>
                <div>Available slots: <span className="font-mono">{availableTimeSlots.length}</span></div>
                <div>Selected time: <span className="font-mono">{bookingTime || 'None'}</span></div>
              </div>
            </div>
          )}
        </div>

        <div className="grid grid-cols-1 lg:grid-cols-2 gap-8">
          {/* Date Selection - Enhanced */}
          <div className="bg-white rounded-xl shadow-sm border border-gray-100 p-4 transition-all duration-300">
            <h3 className="text-[#495E57] font-semibold text-lg mb-4 flex items-center">
              <svg xmlns="http://www.w3.org/2000/svg" className="h-5 w-5 mr-2" viewBox="0 0 20 20" fill="currentColor">
                <path fillRule="evenodd" d="M6 2a1 1 0 00-1 1v1H4a2 2 0 00-2 2v10a2 2 0 002 2h12a2 2 0 002-2V6a2 2 0 00-2-2h-1V3a1 1 0 10-2 0v1H7V3a1 1 0 00-1-1zm0 5a1 1 0 000 2h8a1 1 0 100-2H6z" clipRule="evenodd" />
              </svg>
              Select Date
            </h3>
            
            <div className="bg-white rounded-xl overflow-hidden">
              <div className="calendar-wrapper custom-calendar">
                <Calendar
                  date={bookingDate}
                  onChange={handleDateChange}
                  minDate={new Date()}
                  maxDate={new Date(new Date().setDate(new Date().getDate() + 30))}
                  color="#495E57"
                  className="border-0 custom-calendar"
                  dayContentRenderer={(date) => {
                    const isSelectedDay = isSameDay(date, bookingDate);
                    return (
                      <div className={`day-content ${isSelectedDay ? 'selected-day' : ''}`}>
                        {date.getDate()}
                      </div>
                    );
                  }}
                />
              </div>
            </div>
            
            <div className="mt-4 bg-[#f8f9fc] rounded-lg p-3 text-center">
              <p className="text-gray-600 text-sm font-medium">
                {format(bookingDate, 'EEEE, MMMM d, yyyy')}
              </p>
            </div>
          </div>

          {/* Time Selection - Enhanced */}
          <div>
            <h3 className="text-[#495E57] font-semibold text-lg mb-4 flex items-center">
              <svg xmlns="http://www.w3.org/2000/svg" className="h-5 w-5 mr-2" viewBox="0 0 20 20" fill="currentColor">
                <path fillRule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zm1-12a1 1 0 10-2 0v4a1 1 0 00.293.707l2.828 2.829a1 1 0 101.415-1.415L11 9.586V6z" clipRule="evenodd" />
              </svg>
              Select Time
            </h3>
            
            <div className="bg-white rounded-xl shadow-sm border border-gray-100 p-4 h-[320px] relative transition-all duration-300">
              {isLoadingTimeSlots ? (
                <div className="absolute inset-0 flex items-center justify-center bg-white/70 backdrop-blur-sm rounded-xl z-10">
                  <div className="flex flex-col items-center">
                    <div className="w-10 h-10 rounded-full border-4 border-[#F4CE14] border-t-transparent animate-spin"></div>
                    <p className="mt-3 text-gray-600">Loading available times...</p>
                  </div>
                </div>
              ) : availableTimeSlots.length > 0 ? (
                <div className="grid grid-cols-2 sm:grid-cols-3 gap-3 h-full overflow-y-auto pr-2 py-2 time-slots-container">
                  {availableTimeSlots.map((slot, index) => (
                    <button
                      key={index}
                      type="button"
                      onClick={() => handleTimeSlotSelect(slot, index)}
                      className={`py-3 px-3 rounded-xl text-center transition-all duration-200 relative overflow-hidden group
                        ${selectedTimeSlotIndex === index
                          ? "bg-[#F4CE14] text-[#495E57] font-medium shadow-md transform scale-105"
                          : "bg-gray-50 hover:bg-gray-100 text-gray-700 hover:shadow-sm"
                        }
                      `}
                    >
                      {/* Background glow effect on hover */}
                      <div className={`absolute inset-0 bg-[#F4CE14]/20 blur-xl transform scale-0 group-hover:scale-100 transition-transform duration-300 ${selectedTimeSlotIndex === index ? 'opacity-100' : 'opacity-0'}`}></div>
                      
                      {/* Time content */}
                      <div className="relative z-10">
                        <div className="text-sm">{slot.label.split(' - ')[0]}</div>
                        <div className="text-xs opacity-75">to</div>
                        <div className="text-sm">{slot.label.split(' - ')[1]}</div>
                      </div>
                    </button>
                  ))}
                </div>
              ) : (
                <div className="flex flex-col items-center justify-center h-full">
                  <div className="w-16 h-16 bg-gray-100 rounded-full flex items-center justify-center mb-3">
                    <svg xmlns="http://www.w3.org/2000/svg" className="h-8 w-8 text-gray-400" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                      <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="1.5" d="M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z" />
                    </svg>
                  </div>
                  <h4 className="text-gray-700 font-medium mb-1">No Available Times</h4>
                  <p className="text-sm text-gray-500 text-center max-w-[220px] mb-4">There are no available time slots for the selected date.</p>
                  <button 
                    onClick={fetchAvailableTimeSlots}
                    className="px-4 py-2 text-sm bg-[#495E57]/10 text-[#495E57] rounded-lg hover:bg-[#495E57]/20 transition-colors flex items-center"
                  >
                    <svg xmlns="http://www.w3.org/2000/svg" className="h-4 w-4 mr-1.5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                      <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M4 4v5h.582m15.356 2A8.001 8.001 0 004.582 9m0 0H9m11 11v-5h-.581m0 0a8.003 8.003 0 01-15.357-2m15.357 2H15" />
                    </svg>
                    Retry Loading
                  </button>
                </div>
              )}
            </div>

            {/* Service Location - Enhanced */}
            <div className="mt-6">
              <h3 className="text-[#495E57] font-semibold text-lg mb-4 flex items-center">
                <svg xmlns="http://www.w3.org/2000/svg" className="h-5 w-5 mr-2" viewBox="0 0 20 20" fill="currentColor">
                  <path fillRule="evenodd" d="M5.05 4.05a7 7 0 119.9 9.9L10 18.9l-4.95-4.95a7 7 0 010-9.9zM10 11a2 2 0 100-4 2 2 0 000 4z" clipRule="evenodd" />
                </svg>
                Service Location
              </h3>
              
              {isLoading ? (
                <div className="bg-white rounded-xl shadow-sm border border-gray-100 p-4">
                  <div className="flex gap-3 items-center">
                    <div className="h-10 w-10 rounded-full bg-gray-200 animate-pulse"></div>
                    <div className="flex-1">
                      <div className="h-4 bg-gray-200 rounded animate-pulse mb-2 w-3/4"></div>
                      <div className="h-3 bg-gray-200 rounded animate-pulse w-1/2"></div>
                    </div>
                  </div>
                </div>
              ) : (
                <div className="bg-white rounded-xl shadow-sm border border-gray-100 p-4 transition-all duration-300 hover:shadow-md">
                  <div className="flex items-start">
                    <div className="h-10 w-10 rounded-full bg-[#495E57]/10 flex items-center justify-center flex-shrink-0 mr-3">
                      <svg xmlns="http://www.w3.org/2000/svg" className="h-5 w-5 text-[#495E57]" viewBox="0 0 20 20" fill="currentColor">
                        <path fillRule="evenodd" d="M5.05 4.05a7 7 0 119.9 9.9L10 18.9l-4.95-4.95a7 7 0 010-9.9zM10 11a2 2 0 100-4 2 2 0 000 4z" clipRule="evenodd" />
                      </svg>
                    </div>
                    <div>
                      {address ? (
                        <>
                          <p className="text-gray-800 font-medium">Delivery Address</p>
                          <p className="text-gray-600 text-sm mt-1">{address}</p>
                          <button
                            onClick={() => navigate('/customerProfile/address')}
                            className="text-sm text-[#495E57] hover:text-[#F4CE14] transition-colors mt-2 flex items-center"
                          >
                            <svg xmlns="http://www.w3.org/2000/svg" className="h-3.5 w-3.5 mr-1" viewBox="0 0 20 20" fill="currentColor">
                              <path d="M13.586 3.586a2 2 0 112.828 2.828l-.793.793-2.828-2.828.793-.793zM11.379 5.793L3 14.172V17h2.828l8.38-8.379-2.83-2.828z" />
                            </svg>
                            Change address
                          </button>
                        </>
                      ) : (
                        <>
                          <p className="text-gray-800 font-medium">No Address Available</p>
                          <p className="text-gray-600 text-sm mt-1">Please add a delivery address to continue.</p>
                          <button
                            onClick={() => navigate('/customerProfile/address')}
                            className="mt-2 px-4 py-2 bg-[#F4CE14] text-[#495E57] text-sm font-medium rounded-lg flex items-center"
                          >
                            <svg xmlns="http://www.w3.org/2000/svg" className="h-4 w-4 mr-1.5" viewBox="0 0 20 20" fill="currentColor">
                              <path fillRule="evenodd" d="M10 5a1 1 0 011 1v3h3a1 1 0 110 2h-3v3a1 1 0 11-2 0v-3H6a1 1 0 110-2h3V6a1 1 0 011-1z" clipRule="evenodd" />
                            </svg>
                            Add Address
                          </button>
                        </>
                      )}
                    </div>
                  </div>
                </div>
              )}
            </div>
          </div>
        </div>

        {/* Price Summary - Enhanced */}
        <div className="mt-8 bg-gradient-to-r from-gray-50 to-white rounded-xl border border-gray-200 p-5">
          <h3 className="text-lg font-semibold text-gray-800 mb-4 flex items-center">
            <svg xmlns="http://www.w3.org/2000/svg" className="h-5 w-5 mr-2 text-[#F4CE14]" viewBox="0 0 20 20" fill="currentColor">
              <path fillRule="evenodd" d="M4 4a2 2 0 00-2 2v4a2 2 0 002 2V6h10a2 2 0 00-2-2H4zm2 6a2 2 0 012-2h8a2 2 0 012 2v4a2 2 0 01-2 2H8a2 2 0 01-2-2v-4zm6 4a2 2 0 100-4 2 2 0 000 4z" clipRule="evenodd" />
            </svg>
            Price Summary
          </h3>
          
          <div className="space-y-3">
            <div className="flex justify-between items-center">
              <div className="flex items-center">
                <span className="text-gray-600">Service Base Price</span>
                <div className="group relative ml-2 cursor-help">
                  <svg xmlns="http://www.w3.org/2000/svg" className="h-4 w-4 text-gray-400" viewBox="0 0 20 20" fill="currentColor">
                    <path fillRule="evenodd" d="M18 10a8 8 0 11-16 0 8 8 0 0116 0zm-8-3a1 1 0 00-.867.5 1 1 0 11-1.731-1A3 3 0 0113 8a3.001 3.001 0 01-2 2.83V11a1 1 0 11-2 0v-1a1 1 0 011-1 1 1 0 100-2zm0 8a1 1 0 100-2 1 1 0 000 2z" clipRule="evenodd" />
                  </svg>
                  <div className="opacity-0 group-hover:opacity-100 transition-opacity absolute bottom-full left-1/2 transform -translate-x-1/2 mb-2 w-48 p-2 bg-gray-800 text-white text-xs rounded-lg pointer-events-none">
                    Base price charged by the service provider
                  </div>
                </div>
              </div>
              <span className="font-medium text-gray-800">₱{serviceData.price.toLocaleString('en-PH', {minimumFractionDigits: 2})}</span>
            </div>
            <div className="flex justify-between items-center">
              <div className="flex items-center">
                <span className="text-gray-600">Payment Processing Fee (2.5%)</span>
                <div className="group relative ml-2 cursor-help">
                  <svg xmlns="http://www.w3.org/2000/svg" className="h-4 w-4 text-gray-400" viewBox="0 0 20 20" fill="currentColor">
                    <path fillRule="evenodd" d="M18 10a8 8 0 11-16 0 8 8 0 0116 0zm-8-3a1 1 0 00-.867.5 1 1 0 11-1.731-1A3 3 0 0113 8a3.001 3.001 0 01-2 2.83V11a1 1 0 11-2 0v-1a1 1 0 011-1 1 1 0 100-2zm0 8a1 1 0 100-2 1 1 0 000 2z" clipRule="evenodd" />
                  </svg>
                  <div className="opacity-0 group-hover:opacity-100 transition-opacity absolute bottom-full left-1/2 transform -translate-x-1/2 mb-2 w-48 p-2 bg-gray-800 text-white text-xs rounded-lg pointer-events-none">
                    Fee charged by our payment processor
                  </div>
                </div>
              </div>
              <span className="font-medium text-gray-800">₱{payMongoFee.toLocaleString('en-PH', {minimumFractionDigits: 2})}</span>
            </div>
            <div className="flex justify-between items-center">
              <div className="flex items-center">
                <span className="text-gray-600">Platform Fee (2.5%)</span>
                <div className="group relative ml-2 cursor-help">
                  <svg xmlns="http://www.w3.org/2000/svg" className="h-4 w-4 text-gray-400" viewBox="0 0 20 20" fill="currentColor">
                    <path fillRule="evenodd" d="M18 10a8 8 0 11-16 0 8 8 0 0116 0zm-8-3a1 1 0 00-.867.5 1 1 0 11-1.731-1A3 3 0 0113 8a3.001 3.001 0 01-2 2.83V11a1 1 0 11-2 0v-1a1 1 0 011-1 1 1 0 100-2zm0 8a1 1 0 100-2 1 1 0 000 2z" clipRule="evenodd" />
                  </svg>
                  <div className="opacity-0 group-hover:opacity-100 transition-opacity absolute bottom-full left-1/2 transform -translate-x-1/2 mb-2 w-48 p-2 bg-gray-800 text-white text-xs rounded-lg pointer-events-none">
                    Fee to maintain our platform and services
                  </div>
                </div>
              </div>
              <span className="font-medium text-gray-800">₱{appFee.toLocaleString('en-PH', {minimumFractionDigits: 2})}</span>
            </div>
            <div className="border-t border-gray-200 pt-3 mt-3 flex justify-between items-center">
              <span className="font-semibold text-gray-800">Total Price</span>
              <span className="font-bold text-xl text-[#495E57]">₱{totalPrice.toLocaleString('en-PH', {minimumFractionDigits: 2})}</span>
            </div>
          </div>
        </div>

        {/* Navigation Buttons - Enhanced */}
        <div className="mt-8 flex justify-end">
          <button
            onClick={() => navigate(-1)}
            className="px-6 py-3 mr-3 bg-gray-100 hover:bg-gray-200 text-gray-800 rounded-lg font-medium transition-colors duration-200 flex items-center"
          >
            <svg xmlns="http://www.w3.org/2000/svg" className="h-5 w-5 mr-1.5" viewBox="0 0 20 20" fill="currentColor">
              <path fillRule="evenodd" d="M9.707 16.707a1 1 0 01-1.414 0l-6-6a1 1 0 010-1.414l6-6a1 1 0 011.414 1.414L5.414 9H17a1 1 0 110 2H5.414l4.293 4.293a1 1 0 010 1.414z" clipRule="evenodd" />
            </svg>
            Back
          </button>
          <button
            onClick={handleNext}
            disabled={!bookingDate || !bookingTime || !address}
            className={`px-6 py-3 bg-[#F4CE14] text-[#495E57] rounded-lg font-medium shadow-md hover:bg-[#f3d028] active:bg-[#e5bc12] transition-all duration-200 transform hover:-translate-y-0.5 flex items-center
              ${(!bookingDate || !bookingTime || !address) ? 'opacity-50 cursor-not-allowed hover:translate-y-0' : ''}
            `}
          >
            Review Details
            <svg xmlns="http://www.w3.org/2000/svg" className="h-5 w-5 ml-1.5" viewBox="0 0 20 20" fill="currentColor">
              <path fillRule="evenodd" d="M10.293 3.293a1 1 0 011.414 0l6 6a1 1 0 010 1.414l-6 6a1 1 0 01-1.414-1.414L14.586 11H3a1 1 0 110-2h11.586l-4.293-4.293a1 1 0 010-1.414z" clipRule="evenodd" />
            </svg>
          </button>
        </div>
      </div>

      {/* Add custom CSS for Calendar component */}
      <style jsx>{`
        .custom-calendar .rdrMonth {
          width: 100% !important;
        }
        
        .custom-calendar .rdrCalendarWrapper {
          border-radius: 12px;
          font-family: inherit;
          width: 100% !important;
        }
        
        .custom-calendar .rdrDateDisplayWrapper {
          background-color: #f8f9fc;
        }
        
        .custom-calendar .rdrDayToday .rdrDayNumber span:after {
          background: #F4CE14;
        }
        
        .custom-calendar .rdrDayDisabled {
          opacity: 0.3;
        }
        
        .custom-calendar .rdrDay:not(.rdrDayDisabled):hover .rdrDayNumber span {
          background: rgba(244, 206, 20, 0.2) !important;
          border-color: transparent !important;
          color: #495E57 !important;
        }
        
        .custom-calendar .rdrMonthName {
          font-weight: 600;
          color: #495E57;
          padding: 0.75rem 1rem;
        }
        
        .custom-calendar .rdrNextPrevButton {
          background: #f4f4f5;
          border-radius: 50%;
          margin: 0 8px;
        }
        
        .custom-calendar .rdrNextPrevButton:hover {
          background: #e4e4e7;
        }
        
        .custom-calendar .rdrDayNumber {
          font-weight: 500;
        }
        
        .time-slots-container {
          scrollbar-width: thin;
          scrollbar-color: #d1d5db #f3f4f6;
        }
        
        .time-slots-container::-webkit-scrollbar {
          width: 6px;
        }
        
        .time-slots-container::-webkit-scrollbar-track {
          background: #f3f4f6;
          border-radius: 20px;
        }
        
        .time-slots-container::-webkit-scrollbar-thumb {
          background-color: #d1d5db;
          border-radius: 20px;
          border: 2px solid #f3f4f6;
        }
        
        .time-slots-container::-webkit-scrollbar-thumb:hover {
          background-color: #9ca3af;
        }
      `}</style>
    </div>
  );
};

export default DateTimeSelection;
