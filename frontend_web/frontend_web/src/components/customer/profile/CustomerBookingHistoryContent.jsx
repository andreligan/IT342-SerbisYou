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

  return (
    <div>
      <h2 className="text-xl font-bold mb-4">My Booking History</h2>
      {isLoading ? (
        <p>Loading booking history...</p>
      ) : error ? (
        <p className="text-red-500">{error}</p>
      ) : bookings.length === 0 ? (
        <p>No bookings found.</p>
      ) : (
        <ul className="space-y-4">
          {bookings.map((booking, index) => (
            <li key={index} className="border border-gray-300 rounded-lg p-4">
              <p>
                <strong>Service:</strong> {booking.serviceName}
              </p>
              <p>
                <strong>Date:</strong> {new Date(booking.date).toLocaleDateString()}
              </p>
              <p>
                <strong>Time:</strong> {booking.time}
              </p>
              <p>
                <strong>Status:</strong> {booking.status}
              </p>
            </li>
          ))}
        </ul>
      )}
    </div>
  );
};

export default CustomerBookingHistoryContent;