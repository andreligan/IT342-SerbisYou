import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { motion, AnimatePresence } from 'framer-motion';
import apiClient, { getApiUrl } from '../../utils/apiConfig';

const ServiceProviderBookings = () => {
  const [bookings, setBookings] = useState([]);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState(null);
  const [filter, setFilter] = useState('all');
  const [expandedBookingId, setExpandedBookingId] = useState(null);

  // Page entrance animation variants
  const pageVariants = {
    hidden: { opacity: 0, y: 20 },
    visible: { 
      opacity: 1, 
      y: 0,
      transition: {
        duration: 0.5,
        when: "beforeChildren",
        staggerChildren: 0.1
      }
    },
    exit: { opacity: 0, y: -20 }
  };

  // Item animation variants
  const itemVariants = {
    hidden: { opacity: 0, y: 15 },
    visible: { 
      opacity: 1, 
      y: 0,
      transition: { 
        type: "spring", 
        stiffness: 100, 
        damping: 10 
      }
    }
  };

  useEffect(() => {
    fetchProviderBookings();
  }, []);

  const fetchProviderBookings = async () => {
    try {
      setIsLoading(true);
      
      const controller = new AbortController();
      const timeoutId = setTimeout(() => controller.abort(), 10000);
      
      const response = await apiClient.get(getApiUrl('bookings/getProviderBookings'), {
        signal: controller.signal
      });
      
      clearTimeout(timeoutId);
      
      if (response.status === 200) {
        setBookings(response.data || []);
        setError(null);
      } else {
        setError(`Failed to load bookings. Server returned status: ${response.status}`);
      }
    } catch (err) {
      if (err.response) {
        if (err.response.data && typeof err.response.data === 'string') {
          setError(`Server error: ${err.response.data}`);
        } else {
          setError(`Failed to load bookings. Server responded with status: ${err.response.status}`);
        }
      } else if (err.request) {
        setError("Failed to connect to the server. Please check your internet connection.");
      } else {
        setError(`Error preparing request: ${err.message}`);
      }
    } finally {
      setIsLoading(false);
    }
  };

  const getStatusColor = (status) => {
    switch(status?.toLowerCase()) {
      case "completed":
        return "bg-green-100 text-green-800 border-green-300";
      case "cancelled":
        return "bg-red-100 text-red-800 border-red-300";
      case "pending":
        return "bg-yellow-100 text-yellow-800 border-yellow-300";
      case "in progress":
        return "bg-blue-100 text-blue-800 border-blue-300";
      case "confirmed":
        return "bg-purple-100 text-purple-800 border-purple-300";
      default:
        return "bg-gray-100 text-gray-800 border-gray-300";
    }
  };

  const formatDate = (dateString) => {
    if (!dateString) return "N/A";
    const options = { weekday: 'long', year: 'numeric', month: 'long', day: 'numeric' };
    try {
      return new Date(dateString).toLocaleDateString(undefined, options);
    } catch (e) {
      return "Invalid Date";
    }
  };

  const formatTime = (timeString) => {
    if (!timeString) return "Not specified";
    
    const timeParts = timeString.toString().split(':');
    if (timeParts.length < 2) return timeString;
    
    const hour = parseInt(timeParts[0], 10);
    const minutes = timeParts[1];
    const period = hour >= 12 ? 'PM' : 'AM';
    const formattedHour = hour % 12 || 12;
    
    return `${formattedHour}:${minutes} ${period}`;
  };

  const getPaymentBadge = (booking) => {
    if (!booking.paymentMethod) return null;
    
    const isCash = booking.paymentMethod.toLowerCase() === 'cash';
    const isGCash = booking.paymentMethod.toLowerCase() === 'gcash';
    const isFullPayment = booking.fullPayment !== false;
    
    if (isCash) {
      return (
        <span className="px-3 py-1 bg-orange-100 text-orange-700 rounded-full text-xs font-medium border border-orange-200">
          Cash Payment (Upon Completion)
        </span>
      );
    } else if (isGCash && !isFullPayment) {
      return (
        <span className="px-3 py-1 bg-blue-100 text-blue-700 rounded-full text-xs font-medium border border-blue-200">
          GCash (50% Downpayment)
        </span>
      );
    } else if (isGCash && isFullPayment) {
      return (
        <span className="px-3 py-1 bg-green-100 text-green-700 rounded-full text-xs font-medium border border-green-200">
          GCash (Full Payment)
        </span>
      );
    }
    
    return null;
  };

  const handleUpdateStatus = async (bookingId, newStatus) => {
    try {
      if (newStatus === 'COMPLETED') {
        await apiClient.put(getApiUrl(`bookings/complete/${bookingId}`), {});
      } else {
        await apiClient.put(getApiUrl(`bookings/updateStatus/${bookingId}`), 
          { status: newStatus }
        );
      }
      
      setBookings(bookings.map(booking => 
        booking.bookingId === bookingId 
          ? {...booking, status: newStatus} 
          : booking
      ));
      
      if (newStatus === 'COMPLETED') {
        alert("Service completed successfully! The schedule slot is now available again.");
      }
      
    } catch (err) {
      alert("Failed to update booking status. Please try again.");
    }
  };

  const handleConfirmCashPayment = async (bookingId) => {
    if (window.confirm("Confirm that you've received cash payment for this booking?")) {
      try {
        await apiClient.post(getApiUrl(`transactions/confirm-cash-payment/${bookingId}`), {});
        
        alert("Payment has been confirmed successfully.");
        fetchProviderBookings();
      } catch (err) {
        alert("Failed to confirm payment. Please try again.");
      }
    }
  };

  const toggleBookingExpansion = (bookingId) => {
    setExpandedBookingId(expandedBookingId === bookingId ? null : bookingId);
  };

  const filteredBookings = filter === 'all' 
    ? bookings 
    : bookings.filter(booking => booking.status?.toLowerCase() === filter);

  return (
    <motion.div 
      className="py-8 px-4 sm:px-6 lg:px-8 max-w-7xl mx-auto"
      initial="hidden"
      animate="visible"
      exit="exit"
      variants={pageVariants}
    >
      <motion.div 
        className="flex flex-col sm:flex-row justify-between items-start sm:items-center mb-8"
        variants={itemVariants}
      >
        <div>
          <h1 className="text-2xl font-bold text-gray-900">Customer Bookings</h1>
          <p className="mt-1 text-sm text-gray-500">Manage and track your service appointments</p>
        </div>
        
        <motion.div 
          className="mt-4 sm:mt-0 flex flex-wrap gap-2" 
          variants={itemVariants}
        >
          <button 
            onClick={() => setFilter('all')}
            className={`px-4 py-2 rounded-md text-sm font-medium transition-colors duration-200 ${filter === 'all' ? 'bg-[#495E57] text-white shadow-md' : 'bg-gray-100 text-gray-700 hover:bg-gray-200'}`}
          >
            All
          </button>
          <button 
            onClick={() => setFilter('pending')}
            className={`px-4 py-2 rounded-md text-sm font-medium transition-colors duration-200 ${filter === 'pending' ? 'bg-yellow-500 text-white shadow-md' : 'bg-gray-100 text-gray-700 hover:bg-gray-200'}`}
          >
            Pending
          </button>
          <button 
            onClick={() => setFilter('in progress')}
            className={`px-4 py-2 rounded-md text-sm font-medium transition-colors duration-200 ${filter === 'in progress' ? 'bg-blue-500 text-white shadow-md' : 'bg-gray-100 text-gray-700 hover:bg-gray-200'}`}
          >
            In Progress
          </button>
          <button 
            onClick={() => setFilter('completed')}
            className={`px-4 py-2 rounded-md text-sm font-medium transition-colors duration-200 ${filter === 'completed' ? 'bg-green-500 text-white shadow-md' : 'bg-gray-100 text-gray-700 hover:bg-gray-200'}`}
          >
            Completed
          </button>
          <button 
            onClick={() => setFilter('cancelled')}
            className={`px-4 py-2 rounded-md text-sm font-medium transition-colors duration-200 ${filter === 'cancelled' ? 'bg-red-500 text-white shadow-md' : 'bg-gray-100 text-gray-700 hover:bg-gray-200'}`}
          >
            Cancelled
          </button>
        </motion.div>
      </motion.div>

      {isLoading ? (
        <motion.div 
          className="flex justify-center items-center py-20"
          variants={itemVariants}
        >
          <div className="flex flex-col items-center">
            <div className="w-16 h-16 border-4 border-[#F4CE14] border-t-transparent rounded-full animate-spin"></div>
            <p className="mt-4 text-gray-600">Loading your bookings...</p>
          </div>
        </motion.div>
      ) : error ? (
        <motion.div 
          className="p-6 bg-red-50 border-l-4 border-red-500 text-red-700 rounded-lg shadow-md"
          variants={itemVariants}
        >
          <div className="flex items-start">
            <svg className="h-6 w-6 text-red-500 mr-3" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 8v4m0 4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
            </svg>
            <div>
              <p className="font-medium">Error Loading Bookings</p>
              <p className="mt-1">{error}</p>
            </div>
          </div>
        </motion.div>
      ) : filteredBookings.length === 0 ? (
        <motion.div 
          className="text-center py-16 bg-gray-50 rounded-lg border border-gray-200 shadow-sm"
          variants={itemVariants}
        >
          <svg className="mx-auto h-16 w-16 text-gray-400" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={1.5} d="M9 5H7a2 2 0 00-2 2v12a2 2 0 002 2h10a2 2 0 002-2V7a2 2 0 00-2-2h-2M9 5a2 2 0 002 2h2a2 2 0 002-2M9 5a2 2 0 012-2h2a2 2 0 012 2" />
          </svg>
          <p className="mt-4 text-lg font-medium text-gray-600">No bookings found</p>
          <p className="mt-2 text-gray-500">Try changing your filter or check back later</p>
        </motion.div>
      ) : (
        <div className="space-y-4">
          <AnimatePresence>
            {filteredBookings.map((booking) => (
              <motion.div 
                key={booking.bookingId} 
                className="bg-white rounded-lg shadow-sm border border-gray-200 overflow-hidden"
                variants={itemVariants}
                layout
                initial={{ opacity: 0, y: 20 }}
                animate={{ opacity: 1, y: 0 }}
                exit={{ opacity: 0, height: 0 }}
                transition={{
                  layout: { type: "spring", stiffness: 150, damping: 30 }
                }}
              >
                <div 
                  className={`p-5 cursor-pointer ${expandedBookingId === booking.bookingId ? 'bg-gray-50' : ''}`}
                  onClick={() => toggleBookingExpansion(booking.bookingId)}
                >
                  <div className="flex flex-col sm:flex-row justify-between items-start sm:items-center">
                    <div className="flex items-start space-x-3">
                      <div className={`p-1 rounded-md ${getStatusColor(booking.status).replace('text-', 'bg-').replace('border-', 'bg-').replace('100', '200')}`}>
                        {booking.status?.toLowerCase() === 'pending' && (
                          <svg className="h-5 w-5 text-yellow-700" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z" />
                          </svg>
                        )}
                        {booking.status?.toLowerCase() === 'in progress' && (
                          <svg className="h-5 w-5 text-blue-700" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M21 13.255A23.931 23.931 0 0112 15c-3.183 0-6.22-.62-9-1.745M16 6V4a2 2 0 00-2-2h-4a2 2 0 00-2 2v2m4 6h.01M5 20h14a2 2 0 002-2V8a2 2 0 00-2-2H5a2 2 0 00-2 2v10a2 2 0 002 2z" />
                          </svg>
                        )}
                        {booking.status?.toLowerCase() === 'completed' && (
                          <svg className="h-5 w-5 text-green-700" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M5 13l4 4L19 7" />
                          </svg>
                        )}
                        {booking.status?.toLowerCase() === 'cancelled' && (
                          <svg className="h-5 w-5 text-red-700" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M6 18L18 6M6 6l12 12" />
                          </svg>
                        )}
                        {booking.status?.toLowerCase() === 'confirmed' && (
                          <svg className="h-5 w-5 text-purple-700" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z" />
                          </svg>
                        )}
                      </div>
                      <div>
                        <h3 className="font-semibold text-lg text-gray-900">
                          {booking.service?.serviceName || "Service"}
                        </h3>
                        <div className="flex items-center mt-1">
                          <svg className="h-4 w-4 text-gray-500 mr-1" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z" />
                          </svg>
                          <p className="text-sm text-gray-600">{booking.customer?.firstName} {booking.customer?.lastName}</p>
                        </div>
                      </div>
                    </div>
                    
                    <div className="flex flex-col items-end mt-3 sm:mt-0">
                      <div className="flex items-center">
                        <svg className="h-4 w-4 text-gray-500 mr-1" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                          <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z" />
                        </svg>
                        <p className="text-sm text-gray-600">{formatDate(booking.bookingDate)}</p>
                      </div>
                      
                      <div className="flex items-center mt-1">
                        <svg className="h-4 w-4 text-gray-500 mr-1" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                          <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z" />
                        </svg>
                        <p className="text-sm text-gray-600">{formatTime(booking.bookingTime)}</p>
                      </div>
                      
                      <span className={`mt-2 inline-flex items-center py-1 px-2 rounded-full text-xs font-medium ${getStatusColor(booking.status)}`}>
                        {booking.status || "Unknown"}
                      </span>
                    </div>
                  </div>
                  
                  <div className="flex justify-between items-center mt-4 pt-2 border-t border-gray-100">
                    <div className="flex items-center">
                      <svg className="h-4 w-4 text-gray-500 mr-1" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M17 9V7a2 2 0 00-2-2H5a2 2 0 00-2 2v6a2 2 0 002 2h2m2 4h10a2 2 0 002-2v-6a2 2 0 00-2-2H9a2 2 0 00-2 2v6a2 2 0 002 2zm7-5a2 2 0 11-4 0 2 2 0 014 0z" />
                      </svg>
                      <span className="text-sm font-medium text-gray-900">₱{booking.totalCost?.toLocaleString('en-PH', {minimumFractionDigits: 2})}</span>
                      {getPaymentBadge(booking) && (
                        <span className="ml-2">{getPaymentBadge(booking)}</span>
                      )}
                    </div>
                    
                    <div className="flex items-center">
                      <span className="text-sm text-gray-500 mr-2">
                        {expandedBookingId === booking.bookingId ? 'Hide Details' : 'View Details'}
                      </span>
                      <svg 
                        className={`h-5 w-5 text-gray-500 transform transition-transform duration-300 ${expandedBookingId === booking.bookingId ? 'rotate-180' : ''}`} 
                        fill="none" 
                        viewBox="0 0 24 24" 
                        stroke="currentColor"
                      >
                        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M19 9l-7 7-7-7" />
                      </svg>
                    </div>
                  </div>
                </div>
                
                <AnimatePresence>
                  {expandedBookingId === booking.bookingId && (
                    <motion.div 
                      initial={{ height: 0, opacity: 0 }}
                      animate={{ height: "auto", opacity: 1 }}
                      exit={{ height: 0, opacity: 0 }}
                      transition={{ duration: 0.3, ease: "easeInOut" }}
                      className="border-t border-gray-200 overflow-hidden"
                    >
                      <div className="p-5 bg-gray-50">
                        <div className="grid grid-cols-1 md:grid-cols-2 gap-6 mb-4">
                          <div className="space-y-4">
                            <div>
                              <h4 className="font-medium text-gray-700 mb-2">Customer Details</h4>
                              <div className="bg-white p-4 rounded-md shadow-sm">
                                <div className="flex items-start mb-3">
                                  <svg className="h-5 w-5 text-[#495E57] mt-0.5 mr-2 flex-shrink-0" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z" />
                                  </svg>
                                  <div>
                                    <p className="text-sm text-gray-500">Name</p>
                                    <p className="font-medium">{booking.customer?.firstName} {booking.customer?.lastName}</p>
                                  </div>
                                </div>
                                
                                <div className="flex items-start mb-3">
                                  <svg className="h-5 w-5 text-[#495E57] mt-0.5 mr-2 flex-shrink-0" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M3 5a2 2 0 012-2h3.28a1 1 0 01.948.684l1.498 4.493a1 1 0 01-.502 1.21l-2.257 1.13a11.042 11.042 0 005.516 5.516l1.13-2.257a1 1 0 011.21-.502l4.493 1.498a1 1 0 01.684.949V19a2 2 0 01-2 2h-1C9.716 21 3 14.284 3 6V5z" />
                                  </svg>
                                  <div>
                                    <p className="text-sm text-gray-500">Contact</p>
                                    <p className="font-medium">{booking.customer?.phone || "Not provided"}</p>
                                  </div>
                                </div>
                                
                                <div className="flex items-start">
                                  <svg className="h-5 w-5 text-[#495E57] mt-0.5 mr-2 flex-shrink-0" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M17.657 16.657L13.414 20.9a1.998 1.998 0 01-2.827 0l-4.244-4.243a8 8 0 1111.314 0z" />
                                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M15 11a3 3 0 11-6 0 3 3 0 016 0z" />
                                  </svg>
                                  <div>
                                    <p className="text-sm text-gray-500">Address</p>
                                    <p className="font-medium">{booking.address || "Contact customer for address"}</p>
                                  </div>
                                </div>
                              </div>
                            </div>
                            
                            {booking.note && (
                              <div>
                                <h4 className="font-medium text-gray-700 mb-2">Special Instructions</h4>
                                <div className="bg-white p-4 rounded-md shadow-sm">
                                  <p className="text-gray-600 italic">{booking.note}</p>
                                </div>
                              </div>
                            )}
                          </div>
                          
                          <div className="space-y-4">
                            <div>
                              <h4 className="font-medium text-gray-700 mb-2">Service Details</h4>
                              <div className="bg-white p-4 rounded-md shadow-sm">
                                <div className="flex items-start mb-3">
                                  <svg className="h-5 w-5 text-[#495E57] mt-0.5 mr-2 flex-shrink-0" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M21 13.255A23.931 23.931 0 0112 15c-3.183 0-6.22-.62-9-1.745M16 6V4a2 2 0 00-2-2h-4a2 2 0 00-2 2v2m4 6h.01M5 20h14a2 2 0 002-2V8a2 2 0 00-2-2H5a2 2 0 00-2 2v10a2 2 0 002 2z" />
                                  </svg>
                                  <div>
                                    <p className="text-sm text-gray-500">Service</p>
                                    <p className="font-medium">{booking.service?.serviceName}</p>
                                  </div>
                                </div>
                                
                                <div className="flex items-start mb-3">
                                  <svg className="h-5 w-5 text-[#495E57] mt-0.5 mr-2 flex-shrink-0" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z" />
                                  </svg>
                                  <div>
                                    <p className="text-sm text-gray-500">Duration</p>
                                    <p className="font-medium">{booking.service?.durationEstimate || "Not specified"}</p>
                                  </div>
                                </div>
                                
                                <div className="flex items-start">
                                  <svg className="h-5 w-5 text-[#495E57] mt-0.5 mr-2 flex-shrink-0" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 8c-1.657 0-3 .895-3 2s1.343 2 3 2 3 .895 3 2-1.343 2-3 2m0-8c1.11 0 2.08.402 2.599 1M12 8V7m0 1v8m0 0v1m0-1c-1.11 0-2.08-.402-2.599-1M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
                                  </svg>
                                  <div>
                                    <p className="text-sm text-gray-500">Payment</p>
                                    <p className="font-medium">₱{booking.totalCost?.toLocaleString('en-PH', {minimumFractionDigits: 2})}</p>
                                    <p className="text-sm text-gray-500 mt-1">{booking.paymentMethod || "Not specified"}</p>
                                  </div>
                                </div>
                              </div>
                            </div>
                            
                            <div>
                              <h4 className="font-medium text-gray-700 mb-2">Actions</h4>
                              <div className="bg-white p-4 rounded-md shadow-sm flex flex-wrap gap-3">
                                {booking.status?.toLowerCase() === 'pending' && (
                                  <>
                                    <button 
                                      onClick={(e) => {
                                        e.stopPropagation();
                                        handleUpdateStatus(booking.bookingId, 'CONFIRMED');
                                      }}
                                      className="px-4 py-2 bg-green-100 hover:bg-green-200 text-green-700 rounded-md transition-colors text-sm flex items-center"
                                    >
                                      <svg className="h-4 w-4 mr-1.5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                                        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M5 13l4 4L19 7" />
                                      </svg>
                                      Confirm Booking
                                    </button>
                                    <button 
                                      onClick={(e) => {
                                        e.stopPropagation();
                                        handleUpdateStatus(booking.bookingId, 'CANCELLED');
                                      }}
                                      className="px-4 py-2 bg-red-100 hover:bg-red-200 text-red-700 rounded-md transition-colors text-sm flex items-center"
                                    >
                                      <svg className="h-4 w-4 mr-1.5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                                        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M6 18L18 6M6 6l12 12" />
                                      </svg>
                                      Cancel
                                    </button>
                                  </>
                                )}
                                
                                {booking.status?.toLowerCase() === 'confirmed' && (
                                  <button 
                                    onClick={(e) => {
                                      e.stopPropagation();
                                      handleUpdateStatus(booking.bookingId, 'IN PROGRESS');
                                    }}
                                    className="px-4 py-2 bg-blue-100 hover:bg-blue-200 text-blue-700 rounded-md transition-colors text-sm flex items-center"
                                  >
                                    <svg className="h-4 w-4 mr-1.5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                                      <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M14.752 11.168l-3.197-2.132A1 1 0 0010 9.87v4.263a1 1 0 001.555.832l3.197-2.132a1 1 0 000-1.664z" />
                                      <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
                                    </svg>
                                    Start Service
                                  </button>
                                )}
                                
                                {booking.status?.toLowerCase() === 'in progress' && (
                                  <button 
                                    onClick={(e) => {
                                      e.stopPropagation();
                                      handleUpdateStatus(booking.bookingId, 'COMPLETED');
                                    }}
                                    className="px-4 py-2 bg-green-100 hover:bg-green-200 text-green-700 rounded-md transition-colors text-sm flex items-center"
                                  >
                                    <svg className="h-4 w-4 mr-1.5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                                      <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z" />
                                    </svg>
                                    Complete Service
                                  </button>
                                )}
                                
                                {booking.paymentMethod?.toLowerCase() === 'cash' && 
                                (booking.status?.toLowerCase() === 'in progress' || booking.status?.toLowerCase() === 'completed') && (
                                  <button 
                                    onClick={(e) => {
                                      e.stopPropagation();
                                      handleConfirmCashPayment(booking.bookingId);
                                    }}
                                    className="px-4 py-2 bg-orange-100 hover:bg-orange-200 text-orange-700 rounded-md transition-colors text-sm flex items-center"
                                  >
                                    <svg className="h-4 w-4 mr-1.5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                                      <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M17 9V7a2 2 0 00-2-2H5a2 2 0 00-2 2v6a2 2 0 002 2h2m2 4h10a2 2 0 002-2v-6a2 2 0 00-2-2H9a2 2 0 00-2 2v6a2 2 0 002 2zm7-5a2 2 0 11-4 0 2 2 0 014 0z" />
                                    </svg>
                                    Confirm Cash Payment
                                  </button>
                                )}
                                
                                <Link 
                                  to={`/provider-booking-details/${booking.bookingId}`}
                                  onClick={(e) => e.stopPropagation()}
                                  className="px-4 py-2 bg-[#495E57]/10 hover:bg-[#495E57]/20 text-[#495E57] rounded-md transition-colors text-sm flex items-center"
                                >
                                  <svg className="h-4 w-4 mr-1.5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M15 12a3 3 0 11-6 0 3 3 0 016 0z" />
                                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M2.458 12C3.732 7.943 7.523 5 12 5c4.478 0 8.268 2.943 9.542 7-1.274 4.057-5.064 7-9.542 7-4.477 0-8.268-2.943-9.542-7z" />
                                  </svg>
                                  View Full Details
                                </Link>
                              </div>
                            </div>
                          </div>
                        </div>
                      </div>
                    </motion.div>
                  )}
                </AnimatePresence>
              </motion.div>
            ))}
          </AnimatePresence>
        </div>
      )}
    </motion.div>
  );
};

export default ServiceProviderBookings;
