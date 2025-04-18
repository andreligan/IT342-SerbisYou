import React, { useEffect, useState } from 'react';
import { useParams, Link } from 'react-router-dom';
import axios from 'axios';
import { Star, MapPin, Clock, Phone, Mail, Briefcase, CreditCard, CheckCircle, Calendar, User, Shield, Award, Tag } from 'lucide-react';

// StarRating component
function StarRating({ rating }) {
  return (
    <div className="flex items-center">
      {[...Array(5)].map((_, index) => (
        <Star
          key={index}
          className={`w-5 h-5 ${
            index < rating ? 'fill-yellow-400 text-yellow-400' : 'text-gray-300'
          }`}
        />
      ))}
    </div>
  );
}

function ServiceDetails() {
  const { serviceId } = useParams();
  const [service, setService] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [reviews, setReviews] = useState([]);
  const [customers, setCustomers] = useState([]);
  const [bookings, setBookings] = useState([]);
  const [calculatedStats, setCalculatedStats] = useState({
    totalBookings: 0,
    completionRate: 0,
    repeatCustomers: 0,
    averageRating: 0
  });
  const [serviceCategories, setServiceCategories] = useState([]);
  const [currentCategory, setCurrentCategory] = useState(null);
  const userId = localStorage.getItem('userId') || sessionStorage.getItem('userId');
  const userRole = localStorage.getItem('userRole') || sessionStorage.getItem('userRole');
  const token = localStorage.getItem('authToken') || sessionStorage.getItem('authToken');

  useEffect(() => {
    const fetchService = async () => {
      try {
        const response = await axios.get(`/api/services/getById/${serviceId}`, {
          headers: { Authorization: `Bearer ${token}` },
        });
        setService(response.data);
        setLoading(false);
      } catch (err) {
        console.error('Error fetching service details:', err);
        setError('Failed to load service details.');
        setLoading(false);
      }
    };

    fetchService();
  }, [serviceId, token]);

  useEffect(() => {
    const fetchCustomers = async () => {
      try {
        const response = await axios.get('/api/customers/getAll', {
          headers: { Authorization: `Bearer ${token}` },
        });
        setCustomers(response.data);
      } catch (err) {
        console.error('Error fetching customers:', err);
      }
    };
    
    fetchCustomers();
  }, [token]);

  useEffect(() => {
    const fetchReviews = async () => {
      if (!service) return;
      
      try {
        // Step 1: Get all bookings for this specific service
        const bookingsResponse = await axios.get('/api/bookings/getAll', {
          headers: { Authorization: `Bearer ${token}` },
        });
        
        // Step 2: Filter bookings for this specific service
        const serviceBookings = bookingsResponse.data.filter(booking => {
          const bookingServiceId = booking.service?.serviceId || booking.serviceId;
          return bookingServiceId === parseInt(serviceId);
        });
        
        // Save the service bookings to state for reference
        setBookings(serviceBookings);
        
        // Step 3: Extract booking IDs and customer IDs from these service-specific bookings
        const bookingIds = serviceBookings.map(booking => booking.bookingId);
        const customerIds = serviceBookings.map(booking => {
          return booking.customer?.customerId || booking.customerId;
        });
        
        // Step 4: Get provider ID from service
        const providerId = service.provider?.providerId;
        
        // Step 5: Get all reviews for this provider
        const reviewsResponse = await axios.get(`/api/reviews/getByProvider/${providerId}`, {
          headers: { Authorization: `Bearer ${token}` },
        });
        
        // Step 6: IMPROVED FILTERING - Multiple conditions to make it service-specific
        const serviceSpecificReviews = reviewsResponse.data.filter(review => {
          // First try: Direct match by booking ID if available
          if (review.booking?.bookingId || review.bookingId) {
            const reviewBookingId = review.booking?.bookingId || review.bookingId;
            return bookingIds.includes(reviewBookingId);
          }
          
          // Second try: Match by customer ID + ensure customer booked THIS service
          const reviewCustomerId = review.customer?.customerId || review.customerId;
          return customerIds.includes(reviewCustomerId);
        });
        
        // If we got reviews with the improved filtering, use them
        if (serviceSpecificReviews.length > 0) {
          setReviews(serviceSpecificReviews);
        } else {
          // Fallback: Use the original approach but note this is less accurate
          const enrichedReviews = reviewsResponse.data.map(review => {
            const customerId = getCustomerIdFromBookings(review, serviceBookings);
            return { ...review, _derivedCustomerId: customerId };
          });
          setReviews(enrichedReviews);
        }
        
        // CALCULATE STATISTICS
        
        // 1. Total Bookings - simple count of service-specific bookings
        const totalBookings = serviceBookings.length;
        
        // 2. Completion Rate - percentage of bookings marked as "completed"
        const completedBookings = serviceBookings.filter(booking => 
          booking.status && booking.status.toLowerCase() === "completed"
        );
        const completionRate = serviceBookings.length > 0 
          ? Math.round((completedBookings.length / serviceBookings.length) * 100) 
          : 0;
        
        // 3. Repeat Customers - percentage of customers who booked this service more than once
        // Algorithm: Group bookings by customer ID and count customers with multiple bookings
        const customerBookingCounts = {};
        serviceBookings.forEach(booking => {
          const customerId = booking.customer?.customerId || booking.customerId;
          if (customerId) {
            customerBookingCounts[customerId] = (customerBookingCounts[customerId] || 0) + 1;
          }
        });
        
        const totalUniqueCustomers = Object.keys(customerBookingCounts).length;
        const repeatCustomers = Object.values(customerBookingCounts).filter(count => count > 1).length;
        const repeatCustomerRate = totalUniqueCustomers > 0 
          ? Math.round((repeatCustomers / totalUniqueCustomers) * 100)
          : 0;
        
        // 4. Average Rating - calculated from service-specific reviews
        const reviewsToUse = serviceSpecificReviews.length > 0 ? serviceSpecificReviews : enrichedReviews;
        const totalRating = reviewsToUse.reduce((sum, review) => sum + review.rating, 0);
        const averageRating = reviewsToUse.length > 0 
          ? parseFloat((totalRating / reviewsToUse.length).toFixed(1)) 
          : 0;
        
        // Update calculated statistics
        setCalculatedStats({
          totalBookings,
          completionRate,
          repeatCustomers: repeatCustomerRate,
          averageRating
        });
        
      } catch (err) {
        console.error('Error fetching reviews:', err);
      }
    };

    if (service) {
      fetchReviews();
    }
  }, [service, serviceId, token]);

  useEffect(() => {
    const fetchServiceCategories = async () => {
      try {
        const response = await axios.get('/api/service-categories/getAll', {
          headers: { Authorization: `Bearer ${token}` },
        });
        setServiceCategories(response.data);
        console.log('Service Categories:', response.data);
      } catch (err) {
        console.error('Error fetching service categories:', err);
      }
    };
    
    fetchServiceCategories();
  }, [token]);
  
  useEffect(() => {
    if (service && serviceCategories.length > 0) {
      const categoryId = service.categoryId || (service.category && service.category.categoryId);
      
      if (categoryId) {
        const matchedCategory = serviceCategories.find(cat => cat.categoryId === categoryId);
        if (matchedCategory) {
          setCurrentCategory(matchedCategory);
        }
      }
      console.log('Current Category:', currentCategory);
    }
  }, [service, serviceCategories]);

  const getCustomerNameById = (customerId) => {
    if (!customerId) {
      return 'Anonymous';
    }
    
    const customer = customers.find(c => c.customerId === customerId);
    
    if (customer) {
      return `${customer.firstName} ${customer.lastName}`;
    } else {
      return 'Anonymous';
    }
  };

  const getBookingDateForReview = (review) => {
    // Check if we can access the date directly from the review
    if (review.booking && review.booking.bookingDate) {
      return new Date(review.booking.bookingDate).toLocaleDateString();
    }
    
    // Check if there's a direct bookingDate property
    if (review.bookingDate) {
      return new Date(review.bookingDate).toLocaleDateString();
    }
    
    // Find the matching booking in our bookings array
    const bookingId = review.bookingId || (review.booking && review.booking.bookingId);
    if (bookingId && bookings.length > 0) {
      const matchingBooking = bookings.find(booking => booking.bookingId === bookingId);
      if (matchingBooking && matchingBooking.bookingDate) {
        return new Date(matchingBooking.bookingDate).toLocaleDateString();
      }
    }
    
    // If we couldn't find a date, return empty string
    return '';
  };

  if (loading) {
    return <div className="flex justify-center items-center h-screen">Loading...</div>;
  }

  if (error) {
    return <div className="flex justify-center items-center h-screen text-red-500">{error}</div>;
  }

  const provider = service.provider || {
    firstName: "Service Provider",
    lastName: "",
    phoneNumber: "N/A",
    businessName: "Service Provider",
    yearsOfExperience: "N/A",
    availabilitySchedule: "N/A",
    verified: false,
    paymentMethod: "N/A"
  };
  
  const category = service.category || { name: "Service", description: "Service description" };

  return (
    <div className="min-h-screen bg-gray-50">
      {/* Header */}
      <div className="bg-white shadow">
        <div className="max-w-7xl mx-auto px-4 py-6 sm:px-6 lg:px-8">
          <div className="flex flex-col md:flex-row md:items-center md:justify-between">
            <div>
              <h1 className="text-3xl font-bold text-gray-900">{service.serviceName}</h1>
              <p className="mt-1 text-lg text-gray-500">
                {provider.businessName && <span className="font-medium">{provider.businessName}</span>}
                {provider.businessName && provider.firstName && " - "}
                {(provider.firstName || provider.lastName) && 
                  <span>{provider.firstName} {provider.lastName}</span>
                }
                {provider.verified && (
                  <CheckCircle className="inline-block ml-2 w-5 h-5 text-green-500" />
                )}
              </p>
            </div>
            <div className="mt-4 md:mt-0 text-right">
              <div className="text-4xl font-bold text-green-800">₱{service.price}.00</div>
              <div className="text-sm text-yellow-500">Estimated working time: {service.durationEstimate} hours</div>
            </div>
          </div>
        </div>
      </div>

      {/* Main Content */}
      <main className="max-w-7xl mx-auto px-4 py-8 sm:px-6 lg:px-8">
        <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
          {/* Left Column - Service Details */}
          <div className="lg:col-span-2">
            {/* Service Description */}
            <div className="bg-white shadow rounded-lg p-6 mb-8">
              <h2 className="text-xl font-semibold mb-4">Service Description</h2>
              <p className="text-gray-600 mb-6">{service.serviceDescription}</p>
            </div>

            {/* Updated Service Statistics */}
            <div className="bg-white shadow rounded-lg p-6 mb-8">
              <h2 className="text-xl font-semibold mb-4">Service Performance</h2>
              <div className="grid grid-cols-2 md:grid-cols-4 gap-4">
                <div className="text-center">
                  <div className="text-2xl font-bold text-blue-600">{calculatedStats.totalBookings}</div>
                  <div className="text-sm text-gray-500">Total Bookings</div>
                </div>
                <div className="text-center">
                  <div className="text-2xl font-bold text-green-600">{calculatedStats.completionRate}%</div>
                  <div className="text-sm text-gray-500">Completion Rate</div>
                </div>
                <div className="text-center">
                  <div className="text-2xl font-bold text-purple-600">{calculatedStats.repeatCustomers}%</div>
                  <div className="text-sm text-gray-500">Repeat Customers</div>
                </div>
                <div className="text-center">
                  <div className="text-2xl font-bold text-yellow-600">{calculatedStats.averageRating}</div>
                  <div className="text-sm text-gray-500">Average Rating</div>
                </div>
              </div>
            </div>

            {/* Reviews */}
            {reviews && reviews.length > 0 ? (
              <div className="bg-white shadow rounded-lg p-6">
                <h2 className="text-xl font-semibold mb-4">Customer Reviews</h2>
                <div className="space-y-6">
                  {reviews.map((review) => {
                    const reviewCustomerId = review._derivedCustomerId || review.customer?.customerId || review.customerId;
                    const customerName = getCustomerNameById(reviewCustomerId);
                    
                    return (
                      <div key={review.reviewId} className="border-b border-yellow-300 pb-6 last:border-b-0 last:pb-0">
                        <div className="flex items-start">
                          <div className="flex-1">
                            <div className="flex items-center">
                              <StarRating rating={review.rating} />
                              <span className="ml-2 text-sm text-gray-500">
                                {new Date(review.reviewDate).toLocaleDateString()}
                              </span>
                            </div>
                            <p className="mt-2 text-gray-600">{review.comment}</p>
                            <div className="mt-2 flex items-center justify-between">
                              <p className="text-sm text-gray-500">
                                - {customerName}
                              </p>
                              <span className="text-sm text-gray-400">
                                {getBookingDateForReview(review) && `Service Date: ${getBookingDateForReview(review)}`}
                              </span>
                            </div>
                          </div>
                        </div>
                      </div>
                    );
                  })}
                </div>
              </div>
            ) : (
              <div className="bg-white shadow rounded-lg p-6">
                <h2 className="text-xl font-semibold mb-4">Customer Reviews</h2>
                <p className="text-gray-500 text-center py-4">No reviews available for this service yet.</p>
              </div>
            )}
          </div>

          {/* Right Column - Provider Info & Booking */}
          <div className="lg:col-span-1">
            {/* Provider Information */}
            <div className="bg-white shadow rounded-lg p-6 mb-8">
              <h2 className="text-xl font-semibold mb-4">Service Provider</h2>
              <div className="space-y-4">
                <div className="flex items-center">
                  <User className="w-5 h-5 text-gray-400 mr-2" />
                  <span>{provider.firstName} {provider.lastName}</span>
                </div>
                {provider.yearsOfExperience && (
                  <div className="flex items-center">
                    <Briefcase className="w-5 h-5 text-gray-400 mr-2" />
                    <span>{provider.yearsOfExperience} years of experience</span>
                  </div>
                )}
                {provider.phoneNumber && (
                  <div className="flex items-center">
                    <Phone className="w-5 h-5 text-gray-400 mr-2" />
                    <span>{provider.phoneNumber}</span>
                  </div>
                )}
                {provider.availabilitySchedule && (
                  <div className="flex items-center">
                    <Clock className="w-5 h-5 text-gray-400 mr-2" />
                    <span>{provider.availabilitySchedule}</span>
                  </div>
                )}
                {provider.paymentMethod && (
                  <div className="flex items-center">
                    <CreditCard className="w-5 h-5 text-gray-400 mr-2" />
                    <span>{provider.paymentMethod}</span>
                  </div>
                )}
              </div>
            </div>

            {/* Category Information - Updated to use matched category */}
            {currentCategory ? (
              <div className="bg-white shadow rounded-lg p-6 mb-8">
                <h2 className="text-xl font-semibold mb-4">Service Category</h2>
                <div className="flex items-start">
                  <Tag className="w-5 h-5 text-gray-400 mr-2 mt-1" />
                  <div>
                    <h3 className="font-medium">{currentCategory.categoryName}</h3>
                    {/* <p className="text-sm text-gray-500">{currentCategory.description}</p> */}
                  </div>
                </div>
              </div>
            ) : category ? (
              <div className="bg-white shadow rounded-lg p-6 mb-8">
                <h2 className="text-xl font-semibold mb-4">Service Category</h2>
                <div className="flex items-start">
                  <Tag className="w-5 h-5 text-gray-400 mr-2 mt-1" />
                  <div>
                    <h3 className="font-medium">{category.categoryName}</h3>
                    {/* <p className="text-sm text-gray-500">{category.description}</p> */}
                  </div>
                </div>
              </div>
            ) : null}
          </div>
        </div>
      </main>
    </div>
  );
}

export default ServiceDetails;