import React, { useState, useEffect } from "react";
import { useParams, useNavigate } from "react-router-dom";
import axios from "axios";
import ReviewModal from "./ReviewModal";
import { Link } from "react-router-dom";

const BookingDetailPage = () => {
  const { bookingId } = useParams();
  const navigate = useNavigate();
  const [booking, setBooking] = useState(null);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState(null);
  const [isReviewModalOpen, setIsReviewModalOpen] = useState(false);

  const handleOpenReviewModal = () => {
    setIsReviewModalOpen(true);
  };

  const handleCloseReviewModal = () => {
    setIsReviewModalOpen(false);
  };

  const handleSubmitReview = async (reviewData) => {
    try {
      const token = localStorage.getItem("authToken") || sessionStorage.getItem("authToken");
      const userId = localStorage.getItem("userId") || sessionStorage.getItem("userId");

      const customersResponse = await axios.get("/api/customers/getAll", {
        headers: { Authorization: `Bearer ${token}` }
      });

      const customer = customersResponse.data.find(c => c.userAuth && c.userAuth.userId == userId);

      if (!customer) {
        alert("Could not identify customer for this review");
        return;
      }

      const review = {
        customerId: customer.customerId,
        providerId: booking.service.provider.providerId,
        bookingId: booking.bookingId,
        rating: reviewData.rating,
        comment: reviewData.comment,
        reviewDate: new Date().toISOString()
      };

      console.log("Submitting review:", review); // For debugging

      await axios.post("/api/reviews/create", review, {
        headers: { Authorization: `Bearer ${token}` }
      });

      handleCloseReviewModal();

      alert("Thank you for your review!");
    } catch (err) {
      console.error("Error submitting review:", err);
      alert("Failed to submit review. Please try again.");
    }
  };

  useEffect(() => {
    const fetchBookingDetails = async () => {
      try {
        const token = localStorage.getItem("authToken") || sessionStorage.getItem("authToken");
        if (!token) {
          setError("Authentication token not found.");
          setIsLoading(false);
          return;
        }

        const response = await axios.get(`/api/bookings/getById/${bookingId}`, {
          headers: {
            Authorization: `Bearer ${token}`,
          },
        });

        setBooking(response.data);
      } catch (err) {
        console.error("Error fetching booking details:", err);
        setError("Failed to load booking details.");
      } finally {
        setIsLoading(false);
      }
    };

    if (bookingId) {
      fetchBookingDetails();
    }
  }, [bookingId]);

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

  const getRemainingBalance = () => {
    if (!booking || !booking.totalCost) return 0;
    
    const isGCashPartial = booking.paymentMethod?.toLowerCase() === 'gcash' && booking.fullPayment === false;
    const isCash = booking.paymentMethod?.toLowerCase() === 'cash';
    
    if (isGCashPartial) {
      return booking.totalCost * 0.5;
    } else if (isCash) {
      return booking.totalCost;
    }
    
    return 0;
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

  if (isLoading) {
    return (
      <div className="min-h-screen bg-gray-50 flex justify-center items-center p-4">
        <div className="animate-spin rounded-full h-12 w-12 border-t-4 border-b-4 border-[#495E57]"></div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="min-h-screen bg-gray-50 p-4">
        <div className="max-w-4xl mx-auto bg-white rounded-lg shadow-lg p-8">
          <div className="p-4 bg-red-50 border-l-4 border-red-500 text-red-700 rounded-md">
            <h3 className="text-lg font-medium">Error</h3>
            <p>{error}</p>
          </div>
          <button 
            className="mt-4 px-4 py-2 bg-gray-200 hover:bg-gray-300 rounded-md"
            onClick={() => navigate(-1)}
          >
            Go Back
          </button>
        </div>
      </div>
    );
  }

  if (!booking) {
    return (
      <div className="min-h-screen bg-gray-50 p-4">
        <div className="max-w-4xl mx-auto bg-white rounded-lg shadow-lg p-8">
          <div className="text-center">
            <h3 className="text-lg font-medium text-gray-700">Booking not found</h3>
            <p className="text-gray-500">The booking you are looking for does not exist or has been removed.</p>
          </div>
          <div className="mt-6 text-center">
            <button 
              className="px-4 py-2 bg-[#495E57] hover:bg-[#3a4a43] text-white rounded-md"
              onClick={() => navigate(-1)}
            >
              Go Back
            </button>
          </div>
        </div>
      </div>
    );
  }

  const remainingBalance = getRemainingBalance();
  const paymentReference = `SY-${booking.bookingId}-${Math.floor(Math.random() * 1000000).toString().padStart(6, '0')}`;
  
  return (
    <div className="min-h-screen bg-gray-50 p-4 py-8">
      <div className="max-w-4xl mx-auto">
        <div className="mb-6 flex items-center justify-between">
          <button 
            className="flex items-center text-gray-600 hover:text-gray-900"
            onClick={() => navigate(-1)}
          >
            <svg xmlns="http://www.w3.org/2000/svg" className="h-5 w-5 mr-1" viewBox="0 0 20 20" fill="currentColor">
              <path fillRule="evenodd" d="M9.707 16.707a1 1 0 01-1.414 0l-6-6a1 1 0 010-1.414l6-6a1 1 0 011.414 1.414L5.414 9H17a1 1 0 110 2H5.414l4.293 4.293a1 1 0 010 1.414z" clipRule="evenodd" />
            </svg>
            Back to Bookings
          </button>
          <span className={`px-3 py-1 rounded-full text-sm font-medium ${getStatusColor(booking.status)}`}>
            {booking.status || "Unknown Status"}
          </span>
        </div>
        
        <div className="bg-white rounded-xl shadow-lg overflow-hidden">
          <div className="bg-gradient-to-r from-[#495E57] to-[#364945] text-white p-6">
            <div className="flex flex-col md:flex-row justify-between items-start md:items-center">
              <div>
                <h1 className="text-2xl font-bold mb-2">{booking.service?.serviceName || "Service Booking"}</h1>
                <p className="text-white/80">Booking #{booking.bookingId}</p>
              </div>
              <div className="mt-4 md:mt-0">
                <div className="inline-block bg-white/10 backdrop-blur-sm px-4 py-2 rounded-lg">
                  <span className="block text-xs text-white/70">Total Cost</span>
                  <span className="text-xl font-bold">₱{booking.totalCost?.toLocaleString('en-PH')}</span>
                </div>
              </div>
            </div>
          </div>
          
          <div className="p-6">
            <div className="grid grid-cols-1 md:grid-cols-2 gap-8">
              <div>
                <h2 className="text-lg font-semibold text-gray-800 mb-4">Booking Details</h2>
                
                <div className="space-y-4">
                  <div className="flex items-start">
                    <svg className="h-5 w-5 text-[#495E57] mt-0.5 mr-3" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                      <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z" />
                    </svg>
                    <div>
                      <p className="text-sm text-gray-500">Date & Time</p>
                      <p className="font-medium text-gray-800">{formatDate(booking.bookingDate)}</p>
                      <p className="font-medium text-gray-800">{formatTime(booking.bookingTime)}</p>
                    </div>
                  </div>
                  
                  <div className="flex items-start">
                    <svg className="h-5 w-5 text-[#495E57] mt-0.5 mr-3" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                      <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M17.657 16.657L13.414 20.9a1.998 1.998 0 01-2.827 0l-4.244-4.243a8 8 0 1111.314 0z" />
                      <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M15 11a3 3 0 11-6 0 3 3 0 016 0z" />
                    </svg>
                    <div>
                      <p className="text-sm text-gray-500">Location</p>
                      <p className="font-medium text-gray-800">Customer Address</p>
                    </div>
                  </div>
                  
                  {booking.note && (
                    <div className="flex items-start">
                      <svg className="h-5 w-5 text-[#495E57] mt-0.5 mr-3" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M11 5H6a2 2 0 00-2 2v11a2 2 0 002 2h11a2 2 0 002-2v-5m-1.414-9.414a2 2 0 112.828 2.828L11.828 15H9v-2.828l8.586-8.586z" />
                      </svg>
                      <div>
                        <p className="text-sm text-gray-500">Special Instructions</p>
                        <p className="font-medium text-gray-800">{booking.note}</p>
                      </div>
                    </div>
                  )}
                </div>

                <div className="mt-8">
                  <h2 className="text-lg font-semibold text-gray-800 mb-4">Service Provider</h2>
                  
                  <div className="bg-gray-50 rounded-lg p-4 flex items-center">
                    {booking.service?.provider?.profileImage ? (
                      <img 
                        src={`http://localhost:8080${booking.service.provider.profileImage}`} 
                        alt="Provider" 
                        className="h-14 w-14 rounded-full object-cover mr-4" 
                      />
                    ) : (
                      <div className="h-14 w-14 rounded-full bg-[#495E57]/20 flex items-center justify-center mr-4">
                        <svg className="h-6 w-6 text-[#495E57]" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                          <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z" />
                        </svg>
                      </div>
                    )}
                    
                    <div>
                      <p className="font-medium text-gray-800">
                        {booking.service?.provider?.firstName
                          ? `${booking.service.provider.firstName} ${booking.service.provider.lastName || ''}`
                          : "Provider information not available"}
                      </p>
                      {booking.service?.provider?.phone && (
                        <p className="text-sm text-gray-600 mt-1">
                          {booking.service.provider.phone}
                        </p>
                      )}
                      {booking.service?.provider?.verified && (
                        <span className="inline-flex items-center mt-1 px-2 py-0.5 rounded-full text-xs font-medium bg-green-100 text-green-800">
                          <svg className="h-3 w-3 mr-1" fill="currentColor" viewBox="0 0 20 20">
                            <path fillRule="evenodd" d="M16.707 5.293a1 1 0 010 1.414l-8 8a1 1 0 01-1.414 0l-4-4a1 1 0 011.414-1.414L8 12.586l7.293-7.293a1 1 0 011.414 0z" clipRule="evenodd" />
                          </svg>
                          Verified
                        </span>
                      )}
                    </div>
                  </div>
                </div>
              </div>
              
              <div>
                <h2 className="text-lg font-semibold text-gray-800 mb-4">Payment Information</h2>
                
                <div className="bg-white border border-gray-200 rounded-lg overflow-hidden">
                  <div className="border-b border-gray-200 px-4 py-3 bg-gray-50">
                    <h3 className="font-medium text-gray-700">Payment Summary</h3>
                  </div>
                  
                  <div className="p-4">
                    <div className="space-y-3">
                      <div className="flex justify-between items-center">
                        <span className="text-gray-600">Service Base Price</span>
                        <span className="font-medium">₱{booking.service?.price?.toLocaleString('en-PH') || booking.totalCost?.toLocaleString('en-PH')}</span>
                      </div>
                      
                      <div className="flex justify-between items-center">
                        <span className="text-gray-600">Service Fee</span>
                        <span className="font-medium">Included</span>
                      </div>
                      
                      <div className="border-t border-gray-200 pt-3 mt-3">
                        <div className="flex justify-between items-center">
                          <span className="font-semibold">Total Amount</span>
                          <span className="font-bold text-[#495E57]">₱{booking.totalCost?.toLocaleString('en-PH')}</span>
                        </div>
                      </div>
                    </div>
                    
                    <div className="mt-5 pt-4 border-t border-gray-200">
                      <div className="flex justify-between items-center mb-2">
                        <span className="font-semibold">Payment Method</span>
                        <span className="capitalize">{booking.paymentMethod || "Not specified"}</span>
                      </div>
                      
                      <div className="flex justify-between items-center">
                        <span className="font-semibold">Payment Status</span>
                        <span>
                          {booking.paymentMethod?.toLowerCase() === 'gcash' && booking.fullPayment !== false ? (
                            <span className="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium bg-green-100 text-green-800">
                              Paid in Full
                            </span>
                          ) : booking.paymentMethod?.toLowerCase() === 'gcash' ? (
                            <span className="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium bg-blue-100 text-blue-800">
                              50% Paid (Downpayment)
                            </span>
                          ) : (
                            <span className="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium bg-orange-100 text-orange-800">
                              Pay on Completion
                            </span>
                          )}
                        </span>
                      </div>
                    </div>
                  </div>
                </div>
                
                {remainingBalance > 0 && (
                  <div className="mt-6 bg-orange-50 border border-orange-200 rounded-lg p-4">
                    <h3 className="font-medium text-orange-800 mb-2 flex items-center">
                      <svg className="h-5 w-5 mr-2" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M13 16h-1v-4h-1m1-4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
                      </svg>
                      Remaining Balance
                    </h3>
                    
                    <div className="flex justify-between items-center mb-3">
                      <span className="text-orange-800">Amount Due:</span>
                      <span className="font-bold text-orange-800">₱{remainingBalance.toLocaleString('en-PH')}</span>
                    </div>
                    
                    <p className="text-sm text-orange-700 mb-4">
                      {booking.paymentMethod?.toLowerCase() === 'gcash' 
                        ? "The remaining 50% of the payment is due upon completion of service."
                        : "Full payment is due upon completion of service."}
                    </p>
                    
                    <button className="w-full bg-orange-600 hover:bg-orange-700 text-white py-2 rounded-md transition-colors font-medium flex items-center justify-center">
                      <svg className="h-4 w-4 mr-2" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M3 10h18M7 15h1m4 0h1m-7 4h12a3 3 0 003-3V8a3 3 0 00-3-3H6a3 3 0 00-3 3v8a3 3 0 003 3z" />
                      </svg>
                      Pay Remaining Balance
                    </button>
                  </div>
                )}
                
                {booking.paymentMethod?.toLowerCase() === 'cash' && (
                  <div className="mt-6 bg-white border border-gray-200 rounded-lg overflow-hidden">
                    <div className="border-b border-gray-200 px-4 py-3 bg-gray-50">
                      <h3 className="font-medium text-gray-700">Payment Reference</h3>
                    </div>
                    
                    <div className="p-6 text-center">
                      <p className="text-sm text-gray-600 mb-3">Show this reference code to your service provider:</p>
                      <div className="bg-gray-100 py-3 px-4 rounded-lg mb-4">
                        <p className="font-mono text-lg font-bold text-gray-800">{paymentReference}</p>
                      </div>
                      
                      <button className="text-sm text-[#495E57] hover:text-[#364945] flex items-center mx-auto">
                        <svg className="h-4 w-4 mr-1" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                          <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M8 16H6a2 2 0 01-2-2V6a2 2 0 012-2h8a2 2 0 012 2v2m-6 12h8a2 2 0 002-2v-8a2 2 0 00-2-2h-8a2 2 0 00-2 2v8a2 2 0 002 2z" />
                        </svg>
                        Copy Reference Code
                      </button>
                    </div>
                  </div>
                )}
              </div>
            </div>
            
            <div className="mt-8 pt-6 border-t border-gray-200 flex flex-wrap justify-end gap-3">
              {booking.status?.toLowerCase() === "pending" && (
                <button 
                  className="px-4 py-2 bg-red-100 hover:bg-red-200 text-red-700 rounded-md transition-colors"
                >
                  Cancel Booking
                </button>
              )}
              
              {booking.status?.toLowerCase() === "completed" && (
                <button 
                  onClick={handleOpenReviewModal}
                  className="px-4 py-2 bg-yellow-100 hover:bg-yellow-200 text-yellow-700 rounded-md transition-colors flex items-center"
                >
                  <svg className="h-4 w-4 mr-2" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M11.049 2.927c.3-.921 1.603-.921 1.902 0l1.519 4.674a1 1 0 00.95.69h4.915c.969 0 1.371 1.24.588 1.81l-3.976 2.888a1 1 0 00-.363 1.118l1.518 4.674c.3.922-.755 1.688-1.538 1.118l-3.976-2.888a1 1 0 00-1.176 0l-3.976-2.888c-.783.57-1.838-.197-1.538-1.118l1.518-4.674a1 1 0 00-.363-1.118l-3.976-2.888c-.784-.57-.38-1.81.588-1.81h4.914a1 1 0 00.951-.69l1.519-4.674z" />
                  </svg>
                  Write Review
                </button>
              )}
              
              {booking.status?.toLowerCase() !== "cancelled" && booking.status?.toLowerCase() !== "completed" && (
                <button 
                  className="px-4 py-2 bg-[#495E57] hover:bg-[#364945] text-white rounded-md transition-colors flex items-center"
                >
                  <svg className="h-4 w-4 mr-2" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M8 10h.01M12 10h.01M16 10h.01M9 16H5a2 2 0 01-2-2V6a2 2 0 012-2h14a2 2 0 012 2v8a2 2 0 01-2 2h-5l-5 5v-5z" />
                  </svg>
                  Contact Support
                </button>
              )}
            </div>
          </div>
        </div>
      </div>
      
      {isReviewModalOpen && (
        <ReviewModal 
          booking={booking}
          onClose={handleCloseReviewModal}
          onSubmit={handleSubmitReview}
        />
      )}
    </div>
  );
};

export default BookingDetailPage;
