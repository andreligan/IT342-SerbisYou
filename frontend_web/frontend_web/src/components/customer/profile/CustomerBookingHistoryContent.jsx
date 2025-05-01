import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import apiClient, { getApiUrl } from '../../../utils/apiConfig';
import { Calendar, Clock } from 'lucide-react';

const CustomerBookingHistoryContent = () => {
  const [bookings, setBookings] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [customerId, setCustomerId] = useState(null);
  const navigate = useNavigate();

  useEffect(() => {
    const fetchCustomerId = async () => {
      try {
        const userId = localStorage.getItem('userId') || sessionStorage.getItem('userId');
        if (!userId) {
          setError('User ID not found');
          setLoading(false);
          return;
        }

        const customersResponse = await apiClient.get(getApiUrl('/customers/getAll'));
        const customer = customersResponse.data.find(c => 
          c.userAuth && c.userAuth.userId == userId
        );

        if (!customer) {
          setError('Customer profile not found');
          setLoading(false);
          return;
        }

        setCustomerId(customer.customerId);
        fetchBookings(customer.customerId);
      } catch (err) {
        console.error('Error fetching customer ID:', err);
        setError('Failed to load customer profile');
        setLoading(false);
      }
    };

    const fetchBookings = async (id) => {
      try {
        const response = await apiClient.get(getApiUrl(`/bookings/getByCustomerId/${id}`));
        setBookings(response.data);
        setLoading(false);
      } catch (err) {
        console.error('Error fetching bookings:', err);
        setError('Failed to load booking history');
        setLoading(false);
      }
    };

    fetchCustomerId();
  }, []);

  const getStatusBadgeClass = (status) => {
    switch (status?.toUpperCase()) {
      case 'COMPLETED':
        return 'bg-green-100 text-green-800 border-green-300';
      case 'PENDING':
        return 'bg-yellow-100 text-yellow-800 border-yellow-300';
      case 'CANCELLED':
        return 'bg-red-100 text-red-800 border-red-300';
      case 'CONFIRMED':
        return 'bg-blue-100 text-blue-800 border-blue-300';
      default:
        return 'bg-gray-100 text-gray-800 border-gray-300';
    }
  };

  const formatDate = (dateString) => {
    if (!dateString) return 'N/A';
    const date = new Date(dateString);
    return date.toLocaleDateString('en-US', {
      year: 'numeric',
      month: 'long',
      day: 'numeric'
    });
  };

  const formatTime = (timeString) => {
    if (!timeString) return 'N/A';
    return timeString;
  };

  const handleViewDetails = (bookingId) => {
    navigate(`/booking-details/${bookingId}`);
  };

  if (loading) {
    return (
      <div className="p-8 flex justify-center items-center">
        <div className="w-12 h-12 border-4 border-t-4 border-[#F4CE14] rounded-full animate-spin"></div>
        <span className="ml-3 text-gray-700">Loading booking history...</span>
      </div>
    );
  }

  if (error) {
    return (
      <div className="p-8 text-center">
        <div className="bg-red-50 border border-red-200 rounded-lg p-6">
          <svg className="w-12 h-12 text-red-500 mx-auto mb-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 8v4m0 4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
          </svg>
          <h3 className="text-lg font-medium text-red-800 mb-2">Error Loading Bookings</h3>
          <p className="text-red-600">{error}</p>
        </div>
      </div>
    );
  }

  return (
    <div className="bg-white">
      <div className="bg-[#495E57] text-white p-6 rounded-t-lg">
        <h1 className="text-3xl font-bold">Booking History</h1>
        <p className="text-gray-200 mt-2">View and manage your service bookings</p>
      </div>
      
      <div className="p-6">
        {bookings.length > 0 ? (
          <div className="space-y-6">
            {bookings.map((booking) => (
              <div
                key={booking.bookingId}
                className="border border-gray-200 rounded-lg shadow-sm hover:shadow-md transition-shadow p-4 bg-white"
              >
                <div className="flex flex-col md:flex-row justify-between gap-4 mb-4">
                  <div>
                    <h3 className="text-lg font-semibold text-[#495E57]">
                      {booking.service?.serviceName || 'Unnamed Service'}
                    </h3>
                    <p className="text-gray-600">
                      Provider: {booking.serviceProvider?.firstName} {booking.serviceProvider?.lastName || 'Unknown Provider'}
                    </p>
                  </div>
                  <div className="flex flex-col sm:flex-row gap-2 sm:items-center">
                    <span
                      className={`px-3 py-1 rounded-full text-xs font-medium border ${getStatusBadgeClass(
                        booking.status
                      )}`}
                    >
                      {booking.status || 'Unknown Status'}
                    </span>
                    <span className="text-sm text-gray-500">
                      Booking ID: #{booking.bookingId}
                    </span>
                  </div>
                </div>
                
                <div className="grid grid-cols-1 md:grid-cols-3 gap-4 mb-4">
                  <div className="flex items-center gap-2">
                    <Calendar size={18} className="text-[#F4CE14]" />
                    <span className="text-gray-700">{formatDate(booking.bookingDate)}</span>
                  </div>
                  <div className="flex items-center gap-2">
                    <Clock size={18} className="text-[#F4CE14]" />
                    <span className="text-gray-700">{formatTime(booking.bookingTime)}</span>
                  </div>
                  <div className="flex items-center gap-2">
                    {booking.payment?.amount ? (
                      <span className="font-semibold text-[#495E57]">₱{booking.payment.amount}</span>
                    ) : (
                      <span className="text-gray-500">Price not specified</span>
                    )}
                  </div>
                </div>
                
                <div className="flex justify-end">
                  <button
                    onClick={() => handleViewDetails(booking.bookingId)}
                    className="px-4 py-2 bg-[#495E57] text-white rounded-lg hover:bg-[#3e4e47] transition-colors text-sm"
                  >
                    View Details
                  </button>
                </div>
              </div>
            ))}
          </div>
        ) : (
          <div className="text-center py-12">
            <div className="mx-auto w-24 h-24 bg-gray-100 rounded-full flex items-center justify-center mb-4">
              <Calendar size={36} className="text-gray-400" />
            </div>
            <h3 className="text-xl font-medium text-gray-800 mb-2">No Bookings Found</h3>
            <p className="text-gray-500 max-w-md mx-auto">
              You haven't made any service bookings yet. Browse available services and make your first booking.
            </p>
          </div>
        )}
      </div>
    </div>
  );
};

export default CustomerBookingHistoryContent;