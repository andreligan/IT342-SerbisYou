import React, { useState, useEffect } from 'react';
import axios from 'axios';
import { Link } from 'react-router-dom';

const ServiceProviderBookings = () => {
  const [bookings, setBookings] = useState([]);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState(null);
  const [filter, setFilter] = useState('all'); // all, pending, in-progress, completed, cancelled

  useEffect(() => {
    fetchProviderBookings();
  }, []);

  const fetchProviderBookings = async () => {
    try {
      setIsLoading(true);
      const token = localStorage.getItem("authToken") || sessionStorage.getItem("authToken");
      
      if (!token) {
        setError("Authentication token not found. Please log in again.");
        setIsLoading(false);
        return;
      }
      
      // Add a timeout to prevent hanging requests
      const controller = new AbortController();
      const timeoutId = setTimeout(() => controller.abort(), 10000); // 10-second timeout
      
      const response = await axios.get("/api/bookings/getProviderBookings", {
        headers: { Authorization: `Bearer ${token}` },
        signal: controller.signal
      });
      
      clearTimeout(timeoutId);
      
      if (response.status === 200) {
        console.log("Bookings fetched successfully:", response.data);
        setBookings(response.data || []);
        setError(null);
      } else {
        console.error("Unexpected response status:", response.status);
        setError(`Failed to load bookings. Server returned status: ${response.status}`);
      }
    } catch (err) {
      console.error("Error fetching service provider bookings:", err);
      
      // Provide more detailed error messages
      if (err.response) {
        // The request was made and the server responded with an error status
        console.error("Response error data:", err.response.data);
        console.error("Response error status:", err.response.status);
        
        if (err.response.data && typeof err.response.data === 'string') {
          setError(`Server error: ${err.response.data}`);
        } else {
          setError(`Failed to load bookings. Server responded with status: ${err.response.status}`);
        }
      } else if (err.request) {
        // The request was made but no response was received
        setError("Failed to connect to the server. Please check your internet connection.");
      } else {
        // Something happened in setting up the request
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
      const token = localStorage.getItem("authToken") || sessionStorage.getItem("authToken");
      
      if (newStatus === 'COMPLETED') {
        // Use the complete endpoint which also releases the schedule slot
        await axios.put(`/api/bookings/complete/${bookingId}`, {}, {
          headers: { Authorization: `Bearer ${token}` }
        });
      } else {
        // For other statuses, use the updateStatus endpoint
        await axios.put(`/api/bookings/updateStatus/${bookingId}`, 
          { status: newStatus },
          { headers: { Authorization: `Bearer ${token}` }
        });
      }
      
      // Update the local state to reflect the change
      setBookings(bookings.map(booking => 
        booking.bookingId === bookingId 
          ? {...booking, status: newStatus} 
          : booking
      ));
      
      // Show success message when completing a service
      if (newStatus === 'COMPLETED') {
        alert("Service completed successfully! The schedule slot is now available again.");
      }
      
    } catch (err) {
      console.error("Error updating booking status:", err);
      alert("Failed to update booking status. Please try again.");
    }
  };

  const handleConfirmCashPayment = async (bookingId) => {
    if (window.confirm("Confirm that you've received cash payment for this booking?")) {
      try {
        const token = localStorage.getItem("authToken") || sessionStorage.getItem("authToken");
        
        await axios.post(`/api/transactions/confirm-cash-payment/${bookingId}`, {}, {
          headers: { Authorization: `Bearer ${token}` }
        });
        
        alert("Payment has been confirmed successfully.");
        fetchProviderBookings(); // Refresh bookings
      } catch (err) {
        console.error("Error confirming cash payment:", err);
        alert("Failed to confirm payment. Please try again.");
      }
    }
  };

  const filteredBookings = filter === 'all' 
    ? bookings 
    : bookings.filter(booking => booking.status?.toLowerCase() === filter);

  return (
    <div className="py-6">
      <div className="flex flex-col sm:flex-row justify-between items-start sm:items-center mb-6">
        <h2 className="text-xl font-bold text-gray-800">Customer Bookings</h2>
        
        <div className="mt-3 sm:mt-0 flex flex-wrap gap-2">
          <button 
            onClick={() => setFilter('all')}
            className={`px-3 py-1.5 rounded-md text-sm font-medium ${filter === 'all' ? 'bg-[#495E57] text-white' : 'bg-gray-100 text-gray-700'}`}
          >
            All
          </button>
          <button 
            onClick={() => setFilter('pending')}
            className={`px-3 py-1.5 rounded-md text-sm font-medium ${filter === 'pending' ? 'bg-yellow-500 text-white' : 'bg-gray-100 text-gray-700'}`}
          >
            Pending
          </button>
          <button 
            onClick={() => setFilter('in progress')}
            className={`px-3 py-1.5 rounded-md text-sm font-medium ${filter === 'in progress' ? 'bg-blue-500 text-white' : 'bg-gray-100 text-gray-700'}`}
          >
            In Progress
          </button>
          <button 
            onClick={() => setFilter('completed')}
            className={`px-3 py-1.5 rounded-md text-sm font-medium ${filter === 'completed' ? 'bg-green-500 text-white' : 'bg-gray-100 text-gray-700'}`}
          >
            Completed
          </button>
          <button 
            onClick={() => setFilter('cancelled')}
            className={`px-3 py-1.5 rounded-md text-sm font-medium ${filter === 'cancelled' ? 'bg-red-500 text-white' : 'bg-gray-100 text-gray-700'}`}
          >
            Cancelled
          </button>
        </div>
      </div>

      {isLoading ? (
        <div className="flex justify-center items-center py-12">
          <div className="animate-spin rounded-full h-12 w-12 border-t-2 border-b-2 border-[#495E57]"></div>
        </div>
      ) : error ? (
        <div className="p-4 bg-red-50 border-l-4 border-red-500 text-red-700 rounded-md">
          <p>{error}</p>
        </div>
      ) : filteredBookings.length === 0 ? (
        <div className="text-center py-10 bg-gray-50 rounded-lg border border-gray-200">
          <svg className="mx-auto h-12 w-12 text-gray-400" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 5H7a2 2 0 00-2 2v12a2 2 0 002 2h10a2 2 0 002-2V7a2 2 0 00-2-2h-2M9 5a2 2 0 002 2h2a2 2 0 002-2M9 5a2 2 0 012-2h2a2 2 0 012 2" />
          </svg>
          <p className="mt-2 text-gray-600">No bookings found matching your filter.</p>
        </div>
      ) : (
        <div className="space-y-6">
          {filteredBookings.map((booking, index) => (
            <div key={index} className="bg-white rounded-lg shadow-md border border-gray-200 overflow-hidden">
              <div className="p-6">
                <div className="flex flex-col sm:flex-row justify-between items-start sm:items-center mb-4">
                  <div className="flex flex-col">
                    <h3 className="font-semibold text-lg text-[#495E57]">
                      {booking.service?.serviceName || "Service"}
                    </h3>
                    <div className="flex flex-wrap gap-2 mt-2">
                      <span className={`px-3 py-1 rounded-full text-xs font-medium ${getStatusColor(booking.status)}`}>
                        {booking.status || "Unknown"}
                      </span>
                      {getPaymentBadge(booking)}
                    </div>
                  </div>
                </div>
                
                <div className="grid grid-cols-1 md:grid-cols-2 gap-6 mb-4">
                  <div className="space-y-3">
                    <div className="flex items-start">
                      <svg className="h-5 w-5 text-[#495E57] mt-0.5 mr-2" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z" />
                      </svg>
                      <div>
                        <p className="text-sm text-gray-500">Customer</p>
                        <p className="font-medium">{booking.customer?.firstName} {booking.customer?.lastName}</p>
                      </div>
                    </div>
                    
                    <div className="flex items-start">
                      <svg className="h-5 w-5 text-[#495E57] mt-0.5 mr-2" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M3 5a2 2 0 012-2h3.28a1 1 0 01.948.684l1.498 4.493a1 1 0 01-.502 1.21l-2.257 1.13a11.042 11.042 0 005.516 5.516l1.13-2.257a1 1 0 011.21-.502l4.493 1.498a1 1 0 01.684.949V19a2 2 0 01-2 2h-1C9.716 21 3 14.284 3 6V5z" />
                      </svg>
                      <div>
                        <p className="text-sm text-gray-500">Contact</p>
                        <p className="font-medium">{booking.customer?.phone || "Not provided"}</p>
                      </div>
                    </div>
                    
                    <div className="flex items-start">
                      <svg className="h-5 w-5 text-[#495E57] mt-0.5 mr-2" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M17.657 16.657L13.414 20.9a1.998 1.998 0 01-2.827 0l-4.244-4.243a8 8 0 1111.314 0z" />
                        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M15 11a3 3 0 11-6 0 3 3 0 016 0z" />
                      </svg>
                      <div>
                        <p className="text-sm text-gray-500">Address</p>
                        <p className="font-medium">{booking.address || "Contact customer for address"}</p>
                      </div>
                    </div>
                  </div>
                  
                  <div className="space-y-3">
                    <div className="flex items-start">
                      <svg className="h-5 w-5 text-[#495E57] mt-0.5 mr-2" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z" />
                      </svg>
                      <div>
                        <p className="text-sm text-gray-500">Date & Time</p>
                        <p className="font-medium">{formatDate(booking.bookingDate)}</p>
                        <p className="font-medium">{formatTime(booking.bookingTime)}</p>
                      </div>
                    </div>
                    
                    <div className="flex items-start">
                      <svg className="h-5 w-5 text-[#495E57] mt-0.5 mr-2" fill="none" viewBox="0 0 24 24" stroke="currentColor">
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
                
                {/* Notes Section */}
                {booking.note && (
                  <div className="mb-4 p-3 bg-gray-50 rounded-md">
                    <p className="text-sm font-medium text-gray-700">Special Instructions:</p>
                    <p className="text-sm text-gray-600 mt-1">{booking.note}</p>
                  </div>
                )}
                
                {/* Action buttons */}
                <div className="mt-4 pt-4 border-t border-gray-200 flex flex-wrap justify-end gap-3">
                  {/* Status Update Buttons - Show appropriate buttons based on current status */}
                  {booking.status?.toLowerCase() === 'pending' && (
                    <>
                      <button 
                        onClick={() => handleUpdateStatus(booking.bookingId, 'CONFIRMED')}
                        className="px-4 py-2 bg-green-100 hover:bg-green-200 text-green-700 rounded-md transition-colors text-sm"
                      >
                        Confirm Booking
                      </button>
                      <button 
                        onClick={() => handleUpdateStatus(booking.bookingId, 'CANCELLED')}
                        className="px-4 py-2 bg-red-100 hover:bg-red-200 text-red-700 rounded-md transition-colors text-sm"
                      >
                        Cancel
                      </button>
                    </>
                  )}
                  
                  {booking.status?.toLowerCase() === 'confirmed' && (
                    <button 
                      onClick={() => handleUpdateStatus(booking.bookingId, 'IN PROGRESS')}
                      className="px-4 py-2 bg-blue-100 hover:bg-blue-200 text-blue-700 rounded-md transition-colors text-sm"
                    >
                      Start Service
                    </button>
                  )}
                  
                  {booking.status?.toLowerCase() === 'in progress' && (
                    <button 
                      onClick={() => handleUpdateStatus(booking.bookingId, 'COMPLETED')}
                      className="px-4 py-2 bg-green-100 hover:bg-green-200 text-green-700 rounded-md transition-colors text-sm"
                    >
                      Complete Service
                    </button>
                  )}
                  
                  {/* Cash Payment Confirmation - Show for cash payments that are in progress or completed */}
                  {booking.paymentMethod?.toLowerCase() === 'cash' && 
                   (booking.status?.toLowerCase() === 'in progress' || booking.status?.toLowerCase() === 'completed') && (
                    <button 
                      onClick={() => handleConfirmCashPayment(booking.bookingId)}
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
                    className="px-4 py-2 bg-[#495E57]/10 hover:bg-[#495E57]/20 text-[#495E57] rounded-md transition-colors text-sm flex items-center"
                  >
                    <svg className="h-4 w-4 mr-1.5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                      <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M15 12a3 3 0 11-6 0 3 3 0 016 0z" />
                      <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M2.458 12C3.732 7.943 7.523 5 12 5c4.478 0 8.268 2.943 9.542 7-1.274 4.057-5.064 7-9.542 7-4.477 0-8.268-2.943-9.542-7z" />
                    </svg>
                    View Details
                  </Link>
                </div>
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  );
};

export default ServiceProviderBookings;
