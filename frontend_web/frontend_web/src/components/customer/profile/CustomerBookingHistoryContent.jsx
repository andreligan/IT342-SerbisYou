import React, { useState, useEffect } from "react";
import { useNavigate, Link } from "react-router-dom";
import ReviewModal from "./ReviewModal";
import NotificationService from "../../../services/NotificationService";
import apiClient, { getApiUrl } from "../../../utils/apiConfig";

const CustomerBookingHistoryContent = () => {
  const [bookings, setBookings] = useState([]);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState(null);
  const navigate = useNavigate();
  const [isReviewModalOpen, setIsReviewModalOpen] = useState(false);
  const [currentBookingForReview, setCurrentBookingForReview] = useState(null);
  const [expandedBookingId, setExpandedBookingId] = useState(null);
  const [reviewedBookings, setReviewedBookings] = useState({});

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

        const response = await apiClient.get(getApiUrl("bookings/getCustomerBookings"));

        setBookings(response.data);

        const reviewStatusPromises = response.data
          .filter(booking => booking.status?.toLowerCase() === "completed")
          .map(async (booking) => {
            try {
              const reviewCheckResponse = await apiClient.get(getApiUrl("reviews/can-review"), {
                params: {
                  customerId: booking.customer?.customerId,
                  bookingId: booking.bookingId
                }
              });

              return {
                bookingId: booking.bookingId,
                canReview: reviewCheckResponse.data
              };
            } catch (err) {
              console.error("Error checking review status:", err);
              return {
                bookingId: booking.bookingId,
                canReview: true
              };
            }
          });

        const reviewStatuses = await Promise.all(reviewStatusPromises);

        const reviewedStatusMap = reviewStatuses.reduce((acc, status) => {
          acc[status.bookingId] = status.canReview;
          return acc;
        }, {});

        setReviewedBookings(reviewedStatusMap);
        setIsLoading(false);
      } catch (err) {
        console.error("Error fetching booking history:", err);
        setError("Failed to load booking history.");
        setIsLoading(false);
      }
    };

    fetchBookingHistory();
  }, []);

  const getStatusColor = (status) => {
    switch (status?.toLowerCase()) {
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

  const getPaymentBadge = (booking) => {
    if (!booking.paymentMethod) return null;

    const isCash = booking.paymentMethod.toLowerCase() === "cash";
    const isGCash = booking.paymentMethod.toLowerCase() === "gcash";
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

  const getRemainingBalance = (booking) => {
    if (!booking.totalCost) return 0;

    const isGCashPartial = booking.paymentMethod?.toLowerCase() === "gcash" && booking.fullPayment === false;
    const isCash = booking.paymentMethod?.toLowerCase() === "cash";

    if (isGCashPartial) {
      return booking.totalCost * 0.5;
    } else if (isCash) {
      return booking.totalCost;
    }

    return 0;
  };

  const formatDate = (dateString) => {
    if (!dateString) return "N/A";
    const options = { weekday: "long", year: "numeric", month: "long", day: "numeric" };
    try {
      return new Date(dateString).toLocaleDateString(undefined, options);
    } catch (e) {
      return "Invalid Date";
    }
  };

  const formatTime = (timeString) => {
    if (!timeString) return "Not specified";

    const timeParts = timeString.toString().split(":");
    if (timeParts.length < 2) return timeString;

    const hour = parseInt(timeParts[0], 10);
    const minutes = timeParts[1];
    const period = hour >= 12 ? "PM" : "AM";
    const formattedHour = hour % 12 || 12;

    return `${formattedHour}:${minutes} ${period}`;
  };

  const handleCancelBooking = async (bookingId) => {
    if (window.confirm("Are you sure you want to cancel this booking?")) {
      try {
        await apiClient.put(getApiUrl(`bookings/cancel/${bookingId}`), {});

        setBookings((prevBookings) =>
          prevBookings.map((booking) =>
            booking.bookingId === bookingId ? { ...booking, status: "Cancelled" } : booking
          )
        );
      } catch (err) {
        console.error("Error cancelling booking:", err);
        alert("Failed to cancel booking. Please try again.");
      }
    }
  };

  const handlePayRemainingBalance = (booking) => {
    if (window.confirm("Would you like to pay the remaining balance now?")) {
      alert("This would redirect to payment processing for the remaining amount.");
    }
  };

  const handleOpenReviewModal = (booking) => {
    setCurrentBookingForReview(booking);
    setIsReviewModalOpen(true);
  };

  const handleCloseReviewModal = () => {
    setIsReviewModalOpen(false);
    setCurrentBookingForReview(null);
  };

  const handleSubmitReview = async (reviewData) => {
    try {
      const params = new URLSearchParams();
      params.append('customerId', currentBookingForReview.customer.customerId);
      params.append('providerId', currentBookingForReview.service.provider.providerId);
      params.append('bookingId', currentBookingForReview.bookingId);
      params.append('rating', reviewData.rating);
      params.append('comment', reviewData.comment);
      params.append('reviewDate', new Date().toISOString());

      const reviewResponse = await apiClient.post(getApiUrl("reviews/createWithIDs"), params, {
        headers: {
          'Content-Type': 'application/x-www-form-urlencoded'
        }
      });

      try {
        const providerUserId = currentBookingForReview.service?.provider?.userAuth?.userId;
        const customerName = `${currentBookingForReview.customer?.firstName || ''} ${currentBookingForReview.customer?.lastName || ''}`.trim();
        const serviceName = currentBookingForReview.service?.serviceName || "your service";

        if (providerUserId) {
          const notificationData = {
            user: { userId: providerUserId },
            type: "review",
            message: `${customerName} left a ${reviewData.rating}-star review for ${serviceName}`,
            isRead: false,
            createdAt: new Date().toISOString(),
            referenceId: currentBookingForReview.bookingId.toString(),
            referenceType: "Review"
          };

          await NotificationService.createNotification(notificationData);
        }
      } catch (notifError) {
        console.error("Error creating review notification:", notifError);
      }

      setReviewedBookings(prev => ({
        ...prev,
        [currentBookingForReview.bookingId]: false
      }));

      handleCloseReviewModal();

      alert("Thank you for your review!");
    } catch (err) {
      console.error("Error submitting review:", err);
      alert("Failed to submit review. Please try again.");
    }
  };

  const toggleAccordion = (bookingId) => {
    setExpandedBookingId(expandedBookingId === bookingId ? null : bookingId);
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
            <path
              strokeLinejoin="round"
              strokeWidth={2}
              d="M9 5H7a2 2 0 00-2 2v12a2 2 0 002 2h10a2 2 0 002-2V7a2 2 0 00-2-2h-2M9 5a2 2 0 002 2h2a2 2 0 002-2M9 5a2 2 0 012-2h2a2 2 0 012 2"
            />
          </svg>
          <p className="mt-2 text-gray-600">No bookings found in your history.</p>
        </div>
      ) : (
        <div className="space-y-6">
          {bookings.map((booking, index) => {
            const remainingBalance = getRemainingBalance(booking);
            const isExpanded = expandedBookingId === booking.bookingId;

            return (
              <div
                key={index}
                className="bg-white rounded-lg shadow-md overflow-hidden border border-gray-200 hover:shadow-lg transition-shadow"
              >
                <div 
                  className="p-4 cursor-pointer flex justify-between items-center border-b border-gray-100"
                  onClick={() => toggleAccordion(booking.bookingId)}
                >
                  <div>
                    <h3 className="font-semibold text-lg text-[#495E57]">
                      {booking.service?.serviceName || booking.serviceName || "Unnamed Service"}
                    </h3>
                    <div className="flex flex-wrap gap-2 mt-2">
                      <span className={`px-3 py-1 rounded-full text-xs font-medium ${getStatusColor(booking.status)}`}>
                        {booking.status || "Unknown"}
                      </span>
                      {getPaymentBadge(booking)}
                    </div>
                  </div>
                  <div className="flex items-center">
                    <p className="mr-4 text-gray-500">{formatDate(booking.bookingDate || booking.date)}</p>
                    <svg 
                      className={`w-5 h-5 text-gray-500 transform transition-transform ${isExpanded ? 'rotate-180' : ''}`} 
                      xmlns="http://www.w3.org/2000/svg" 
                      viewBox="0 0 20 20" fill="currentColor"
                    >
                      <path fillRule="evenodd" d="M5.293 7.293a1 1 0 011.414 0L10 10.586l3.293-3.293a1 1 0 111.414 1.414l-4 4a1 1 0 01-1.414 0l-4-4a1 1 0 010-1.414z" clipRule="evenodd" />
                    </svg>
                  </div>
                </div>

                <div className={`transition-all duration-300 ease-in-out ${isExpanded ? 'max-h-[1000px] opacity-100' : 'max-h-0 opacity-0 overflow-hidden'}`}>
                  <div className="p-4 sm:p-6">
                    <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                      <div className="space-y-2">
                        <div className="flex items-start">
                          <svg
                            className="h-5 w-5 text-[#495E57] mt-0.5 mr-2"
                            fill="none"
                            viewBox="0 0 24 24"
                            stroke="currentColor"
                          >
                            <path
                              strokeLinecap="round"
                              strokeLinejoin="round"
                              strokeWidth={2}
                              d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z"
                            />
                          </svg>
                          <div>
                            <p className="text-sm text-gray-500">Service Provider</p>
                            <p className="font-medium">
                              {booking.service?.provider?.firstName
                                ? `${booking.service.provider.firstName} ${booking.service.provider.lastName || ""}`
                                : "Not specified"}
                            </p>
                          </div>
                        </div>

                        <div className="flex items-start">
                          <svg
                            className="h-5 w-5 text-[#495E57] mt-0.5 mr-2"
                            fill="none"
                            viewBox="0 0 24 24"
                            stroke="currentColor"
                          >
                            <path
                              strokeLinecap="round"
                              strokeLinejoin="round"
                              strokeWidth={2}
                              d="M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z"
                            />
                          </svg>
                          <div>
                            <p className="text-sm text-gray-500">Date</p>
                            <p className="font-medium">{formatDate(booking.bookingDate || booking.date)}</p>
                          </div>
                        </div>

                        <div className="flex items-start">
                          <svg
                            className="h-5 w-5 text-[#495E57] mt-0.5 mr-2"
                            fill="none"
                            viewBox="0 0 24 24"
                            stroke="currentColor"
                          >
                            <path
                              strokeLinecap="round"
                              strokeLinejoin="round"
                              strokeWidth={2}
                              d="M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z"
                            />
                          </svg>
                          <div>
                            <p className="text-sm text-gray-500">Time</p>
                            <p className="font-medium">{formatTime(booking.bookingTime)}</p>
                          </div>
                        </div>

                        {booking.paymentMethod?.toLowerCase() === "cash" &&
                          booking.status?.toLowerCase() !== "cancelled" && (
                            <div className="flex items-start">
                              <svg
                                className="h-5 w-5 text-[#495E57] mt-0.5 mr-2"
                                fill="none"
                                viewBox="0 0 24 24"
                                stroke="currentColor"
                              >
                                <path
                                  strokeLinecap="round"
                                  strokeLinejoin="round"
                                  strokeWidth={2}
                                  d="M7 20l4-16m2 16l4-16M6 9h14M4 15h14"
                                />
                              </svg>
                              <div>
                                <p className="text-sm text-gray-500">Payment Reference</p>
                                <p className="font-medium font-mono">
                                  SY-{booking.bookingId}-{Math.floor(Math.random() * 1000000)
                                    .toString()
                                    .padStart(6, "0")}
                                </p>
                              </div>
                            </div>
                          )}
                      </div>

                      <div className="space-y-2">
                        <div className="flex items-start">
                          <svg
                            className="h-5 w-5 text-[#495E57] mt-0.5 mr-2"
                            fill="none"
                            viewBox="0 0 24 24"
                            stroke="currentColor"
                          >
                            <path
                              strokeLinecap="round"
                              strokeLinejoin="round"
                              strokeWidth={2}
                              d="M17 9V7a2 2 0 00-2-2H5a2 2 0 00-2 2v6a2 2 0 110 4v3a2 2 0 002 2h2m2 4h10a2 2 0 002-2v-6a2 2 0 00-2-2H9a2 2 0 00-2 2v6a2 2 0 002 2zm7-5a2 2 0 11-4 0 2 2 0 014 0z"
                            />
                          </svg>
                          <div>
                            <p className="text-sm text-gray-500">Payment Method</p>
                            <p className="font-medium capitalize">{booking.paymentMethod || "Not specified"}</p>
                          </div>
                        </div>

                        <div className="flex items-start">
                          <svg
                            className="h-5 w-5 text-[#495E57] mt-0.5 mr-2"
                            fill="none"
                            viewBox="0 0 24 24"
                            stroke="currentColor"
                          >
                            <path
                              strokeLinecap="round"
                              strokeLinejoin="round"
                              strokeWidth={2}
                              d="M15 5v2m0 4v2m0 4v2M5 5a2 2 0 00-2 2v3a2 2 0 110 4v3a2 2 0 002 2h14a2 2 0 002-2v-3a2 2 0 110-4V7a2 2 0 00-2-2H5z"
                            />
                          </svg>
                          <div>
                            <p className="text-sm text-gray-500">Total Cost</p>
                            <p className="font-medium">
                              ₱
                              {booking.totalCost?.toLocaleString("en-PH", {
                                minimumFractionDigits: 2,
                                maximumFractionDigits: 2,
                              }) || "Price not available"}
                            </p>
                          </div>
                        </div>

                        {remainingBalance > 0 && (
                          <div className="flex items-start">
                            <svg
                              className="h-5 w-5 text-orange-500 mt-0.5 mr-2"
                              fill="none"
                              viewBox="0 0 24 24"
                              stroke="currentColor"
                            >
                              <path
                                strokeLinecap="round"
                                strokeLinejoin="round"
                                strokeWidth={2}
                                d="M12 8c-1.657 0-3 .895-3 2s1.343 2 3 2 3 .895 3 2-1.343 2-3 2m0-8c1.11 0 2.08.402 2.599 1M12 8V7m0 1v8m0 0v1m0-1c-1.11 0-2.08-.402-2.599-1M21 12a9 9 0 11-18 0 9 9 0 0118 0z"
                              />
                            </svg>
                            <div>
                              <p className="text-sm text-gray-500">Remaining Balance</p>
                              <p className="font-medium text-orange-600">
                                ₱
                                {remainingBalance.toLocaleString("en-PH", {
                                  minimumFractionDigits: 2,
                                  maximumFractionDigits: 2,
                                })}
                              </p>
                            </div>
                          </div>
                        )}

                        <div className="flex items-start">
                          <svg
                            className="h-5 w-5 text-[#495E57] mt-0.5 mr-2"
                            fill="none"
                            viewBox="0 0 24 24"
                            stroke="currentColor"
                          >
                            <path
                              strokeLinecap="round"
                              strokeLinejoin="round"
                              strokeWidth={2}
                              d="M17 20h5v-2a3 3 0 00-5.356-1.857M17 20H7m10 0v-2c0-.656-.126-1.283-.356-1.857M7 20H2v-2a3 3 0 015.356-1.857M7 20v-2c0-.656.126-1.283.356-1.857m0 0a5.002 5.002 0 019.288 0M15 7a3 3 0 11-6 0 3 3 0 016 0zm6 3a2 2 0 11-4 0 2 2 0 014 0zM7 10a2 2 0 11-4 0 2 2 0 014 0z"
                            />
                          </svg>
                          <div>
                            <p className="text-sm text-gray-500">Booking Reference</p>
                            <p className="font-medium">{booking.bookingId || "ID not available"}</p>
                          </div>
                        </div>
                      </div>
                    </div>

                    {booking.note && (
                      <div className="mt-4 p-3 bg-gray-50 rounded-md">
                        <p className="text-sm font-medium text-gray-700">Notes:</p>
                        <p className="text-sm text-gray-600 mt-1">{booking.note}</p>
                      </div>
                    )}
                  </div>
                </div>

                <div className="p-4 border-t border-gray-200 flex flex-wrap justify-end gap-3">
                  {booking.status?.toLowerCase() === "pending" && (
                    <button
                      onClick={() => handleCancelBooking(booking.bookingId)}
                      className="px-4 py-2 bg-red-100 hover:bg-red-200 text-red-700 rounded-md transition-colors text-sm"
                    >
                      Cancel Booking
                    </button>
                  )}

                  {remainingBalance > 0 &&
                    booking.status?.toLowerCase() !== "cancelled" &&
                    booking.status?.toLowerCase() !== "completed" && (
                      <button
                        onClick={() => handlePayRemainingBalance(booking)}
                        className="px-4 py-2 bg-blue-100 hover:bg-blue-200 text-blue-700 rounded-md transition-colors text-sm flex items-center"
                      >
                        <svg
                          className="h-4 w-4 mr-1"
                          fill="none"
                          viewBox="0 0 24 24"
                          stroke="currentColor"
                        >
                          <path
                            strokeLinecap="round"
                            strokeLinejoin="round"
                            strokeWidth={2}
                            d="M3 10h18M7 15h1m4 0h1m-7 4h12a3 3 0 003-3V8a3 3 0 00-3-3H6a3 3 0 00-3 3v8a3 3 0 003 3z"
                          />
                        </svg>
                        Pay Remaining Balance
                      </button>
                    )}

                  {booking.status?.toLowerCase() === "completed" && 
                   reviewedBookings[booking.bookingId] === true && (
                    <button
                      onClick={() => handleOpenReviewModal(booking)}
                      className="px-4 py-2 bg-yellow-100 hover:bg-yellow-200 text-yellow-700 rounded-md transition-colors text-sm flex items-center"
                    >
                      <svg className="h-4 w-4 mr-1.5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M11.049 2.927c.3-.921 1.603-.921 1.902 0l1.519 4.674a1 1 0 00.95.69h4.915c.969 0 1.371 1.24.588 1.81l-3.976 2.888a1 1 0 00-.363 1.118l1.518 4.674c.3.922-.755 1.688-1.538 1.118l-3.976-2.888a1 1 0 00-1.176 0l-3.976 2.888c-.783.57-1.838-.197-1.538-1.118l1.518-4.674a1 1 0 00-.363-1.118l-3.976-2.888c-.784-.57-.38-1.81.588-1.81h4.914a1 1 0 00.951-.69l1.519-4.674z" />
                      </svg>
                      Write Review
                    </button>
                  )}
                  
                  {booking.status?.toLowerCase() === "completed" && 
                   reviewedBookings[booking.bookingId] === false && (
                    <span className="px-4 py-2 bg-gray-100 text-gray-500 rounded-md text-sm flex items-center">
                      <svg className="h-4 w-4 mr-1.5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M5 13l4 4L19 7" />
                      </svg>
                      Already Reviewed
                    </span>
                  )}

                  <Link 
                    to={`/booking-details/${booking.bookingId}`}
                    className="px-4 py-2 bg-[#495E57]/10 hover:bg-[#495E57]/20 text-[#495E57] rounded-md transition-colors text-sm flex items-center"
                  >
                    <svg
                      className="h-4 w-4 mr-1"
                      fill="none"
                      viewBox="0 0 24 24"
                      stroke="currentColor"
                    >
                      <path
                        strokeLinecap="round"
                        strokeLinejoin="round"
                        strokeWidth={2}
                        d="M15 12a3 3 0 11-6 0 3 3 0 016 0z"
                      />
                      <path
                        strokeLinecap="round"
                        strokeLinejoin="round"
                        strokeWidth={2}
                        d="M2.458 12C3.732 7.943 7.523 5 12 5c4.478 0 8.268 2.943 9.542 7-1.274 4.057-5.064 7-9.542 7-4.477 0-8.268-2.943-9.542-7z"
                      />
                    </svg>
                    View Details
                  </Link>
                </div>
              </div>
            );
          })}
        </div>
      )}

      <ReviewModal
        booking={currentBookingForReview}
        isOpen={isReviewModalOpen}
        onClose={handleCloseReviewModal}
        onSubmit={handleSubmitReview}
      />
    </div>
  );
};

export default CustomerBookingHistoryContent;