import React, { useState, useEffect } from "react";
import axios from "axios";

const CustomerBookingHistoryContent = () => {
  const [bookings, setBookings] = useState([]);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    const fetchBookingHistory = async () => {
      try {
        const token = localStorage.getItem("authToken") || sessionStorage.getItem("authToken");
        const userId = localStorage.getItem("userId") || sessionStorage.getItem("userId");
        if (!token) {
          setError("Authentication token not found.");
          setIsLoading(false);
          return;
        }

        const response = await axios.get("/api/bookings/getCustomerBookings", {
          headers: {
            Authorization: `Bearer ${token}`,
          },
        });

        setBookings(response.data);
        setIsLoading(false);
      } catch (err) {
        console.error("Error fetching booking history:", err);
        setError("Failed to load booking history.");
        setIsLoading(false);
      }
    };

    fetchBookingHistory();
  }, []);

  // Function to get appropriate status color
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

  // Handle cancellation of booking
  const handleCancelBooking = async (bookingId) => {
    if (window.confirm("Are you sure you want to cancel this booking?")) {
      try {
        const token = localStorage.getItem("authToken") || sessionStorage.getItem("authToken");
        
        await axios.put(`/api/bookings/cancel/${bookingId}`, {}, {
          headers: { Authorization: `Bearer ${token}` }
        });
        
        // Update the booking status in the UI
        setBookings(prevBookings => 
          prevBookings.map(booking => 
            booking.bookingId === bookingId 
              ? {...booking, status: "Cancelled"} 
              : booking
          )
        );
      } catch (err) {
        console.error("Error cancelling booking:", err);
        alert("Failed to cancel booking. Please try again.");
      }
    }
  };

  // Format date for display
  const formatDate = (dateString) => {
    if (!dateString) return "N/A";
    const options = { weekday: 'long', year: 'numeric', month: 'long', day: 'numeric' };
    try {
      return new Date(dateString).toLocaleDateString(undefined, options);
    } catch (e) {
      return "Invalid Date";
    }
  };

  return (
    <div>
      <h2 className="text-xl font-bold mb-4">My Booking History</h2>
      {isLoading ? (
        <div className="flex justify-center items-center py-8">
          <div className="animate-spin rounded-full h-10 w-10 border-t-2 border-b-2 border-[#495E57]"></div>
        </div>
      ) : error ? (
        <div className="p-4 bg-red-100 border-l-4 border-red-500 text-red-700 rounded-md">
          <p>{error}</p>
        </div>
      ) : bookings.length === 0 ? (
        <div className="text-center py-6 bg-gray-50 rounded-lg border border-gray-200">
          <svg className="mx-auto h-12 w-12 text-gray-400" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 5H7a2 2 0 00-2 2v12a2 2 0 002 2h10a2 2 0 002-2V7a2 2 0 00-2-2h-2M9 5a2 2 0 002 2h2a2 2 0 002-2M9 5a2 2 0 012-2h2a2 2 0 012 2" />
          </svg>
          <p className="mt-2 text-gray-600">No bookings found in your history.</p>
        </div>
      ) : (
        <div className="space-y-6">
          {bookings.map((booking, index) => (
            <div 
              key={index} 
              className="bg-white rounded-lg shadow-md overflow-hidden border border-gray-200 hover:shadow-lg transition-shadow"
            >
              <div className="p-4 sm:p-6">
                <div className="flex flex-col sm:flex-row justify-between items-start sm:items-center mb-4">
                  <h3 className="font-semibold text-lg text-[#495E57]">
                    {booking.service?.serviceName || booking.serviceName || "Unnamed Service"}
                  </h3>
                  <span className={`px-3 py-1 rounded-full text-xs font-medium mt-2 sm:mt-0 ${getStatusColor(booking.status)}`}>
                    {booking.status || "Unknown"}
                  </span>
                </div>
                
                <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                  <div className="space-y-2">
                    <div className="flex items-start">
                      <svg className="h-5 w-5 text-[#495E57] mt-0.5 mr-2" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z" />
                      </svg>
                      <div>
                        <p className="text-sm text-gray-500">Service Provider</p>
                        <p className="font-medium">
                          {booking.service?.provider?.firstName
                            ? `${booking.service.provider.firstName} ${booking.service.provider.lastName || ''}`
                            : "Not specified"}
                        </p>
                      </div>
                    </div>
                    
                    <div className="flex items-start">
                      <svg className="h-5 w-5 text-[#495E57] mt-0.5 mr-2" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z" />
                      </svg>
                      <div>
                        <p className="text-sm text-gray-500">Date</p>
                        <p className="font-medium">{formatDate(booking.bookingDate || booking.date)}</p>
                      </div>
                    </div>
                    
                    <div className="flex items-start">
                      <svg className="h-5 w-5 text-[#495E57] mt-0.5 mr-2" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z" />
                      </svg>
                      <div>
                        <p className="text-sm text-gray-500">Time</p>
                        <p className="font-medium">{booking.bookingTime || booking.time || "Not specified"}</p>
                      </div>
                    </div>
                  </div>
                  
                  <div className="space-y-2">
                    <div className="flex items-start">
                      <svg className="h-5 w-5 text-[#495E57] mt-0.5 mr-2" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M15 5v2m0 4v2m0 4v2M5 5a2 2 0 00-2 2v3a2 2 0 110 4v3a2 2 0 002 2h14a2 2 0 002-2v-3a2 2 0 110-4V7a2 2 0 00-2-2H5z" />
                      </svg>
                      <div>
                        <p className="text-sm text-gray-500">Cost</p>
                        <p className="font-medium">
                          ₱{booking.totalCost?.toLocaleString(undefined, {minimumFractionDigits: 2, maximumFractionDigits: 2}) || "Price not available"}
                        </p>
                      </div>
                    </div>
                    
                    {booking.service?.category && (
                      <div className="flex items-start">
                        <svg className="h-5 w-5 text-[#495E57] mt-0.5 mr-2" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                          <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M7 7h.01M7 3h5c.512 0 1.024.195 1.414.586l7 7a2 2 0 010 2.828l-7 7a2 2 0 01-2.828 0l-7-7A1.994 1.994 0 013 12V7a4 4 0 014-4z" />
                        </svg>
                        <div>
                          <p className="text-sm text-gray-500">Category</p>
                          <p className="font-medium">{booking.service.category.categoryName}</p>
                        </div>
                      </div>
                    )}
                    
                    <div className="flex items-start">
                      <svg className="h-5 w-5 text-[#495E57] mt-0.5 mr-2" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M17 20h5v-2a3 3 0 00-5.356-1.857M17 20H7m10 0v-2c0-.656-.126-1.283-.356-1.857M7 20H2v-2a3 3 0 015.356-1.857M7 20v-2c0-.656.126-1.283.356-1.857m0 0a5.002 5.002 0 019.288 0M15 7a3 3 0 11-6 0 3 3 0 016 0zm6 3a2 2 0 11-4 0 2 2 0 014 0zM7 10a2 2 0 11-4 0 2 2 0 014 0z" />
                      </svg>
                      <div>
                        <p className="text-sm text-gray-500">Booking Reference</p>
                        <p className="font-medium">{booking.bookingId || "ID not available"}</p>
                      </div>
                    </div>
                  </div>
                </div>
                
                {/* Special notes section */}
                {booking.note && (
                  <div className="mt-4 p-3 bg-gray-50 rounded-md">
                    <p className="text-sm font-medium text-gray-700">Notes:</p>
                    <p className="text-sm text-gray-600 mt-1">{booking.note}</p>
                  </div>
                )}
                
                {/* Actions section */}
                <div className="mt-4 pt-4 border-t border-gray-200 flex justify-end">
                  {booking.status?.toLowerCase() === "pending" && (
                    <button
                      onClick={() => handleCancelBooking(booking.bookingId)}
                      className="px-4 py-2 bg-red-100 hover:bg-red-200 text-red-700 rounded-md transition-colors text-sm"
                    >
                      Cancel Booking
                    </button>
                  )}
                  
                  {/* Additional action buttons can be added here */}
                </div>
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  );
};

export default CustomerBookingHistoryContent;