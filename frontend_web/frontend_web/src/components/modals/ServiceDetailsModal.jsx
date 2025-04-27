import React, { useState, useEffect, useRef, useMemo } from "react";
import { useNavigate } from "react-router-dom";
import axios from "axios";
import BaseModal from "../shared/BaseModal";
import { motion } from "framer-motion";

const BASE_URL = "http://localhost:8080";

const ServiceDetailsModal = ({ 
  isOpen, 
  onClose, 
  service, 
  serviceRatings, 
  onBookService,
  renderStars,
  clickPosition
}) => {
  const [animationState, setAnimationState] = useState('closed');
  const modalRef = useRef(null);
  const navigate = useNavigate();
  const [imageFailed, setImageFailed] = useState(false);
  const [activeTab, setActiveTab] = useState('overview'); // State for tabs
  const [reviews, setReviews] = useState([]);
  const [loadingReviews, setLoadingReviews] = useState(false);
  const [reviewError, setReviewError] = useState(null);
  const [customerImages, setCustomerImages] = useState({}); // State for storing customer profile images
  const [reviewFilter, setReviewFilter] = useState('newest'); // State for review filter

  const sortedReviews = useMemo(() => {
    if (!reviews || !reviews.length) return [];
    
    const reviewsCopy = [...reviews];
    
    switch(reviewFilter) {
      case 'newest':
        return reviewsCopy.sort((a, b) => new Date(b.reviewDate) - new Date(a.reviewDate));
      case 'oldest':
        return reviewsCopy.sort((a, b) => new Date(a.reviewDate) - new Date(b.reviewDate));
      case 'highest':
        return reviewsCopy.sort((a, b) => b.rating - a.rating);
      case 'lowest':
        return reviewsCopy.sort((a, b) => a.rating - b.rating);
      default:
        return reviewsCopy;
    }
  }, [reviews, reviewFilter]);

  useEffect(() => {
    if (isOpen) {
      setAnimationState('opening');
      const timer = setTimeout(() => {
        setAnimationState('open');
      }, 10);
      return () => clearTimeout(timer);
    } else {
      setAnimationState('closed');
    }
  }, [isOpen]);

  // Load reviews when service changes or reviews tab is activated
  useEffect(() => {
    const fetchReviews = async () => {
      if (!service || !service.serviceId) return;
      
      // Only fetch if we're on the reviews tab
      if (activeTab !== 'reviews') return;
      
      try {
        setLoadingReviews(true);
        setReviewError(null);
        
        // Get the auth token
        const token = localStorage.getItem('authToken') || sessionStorage.getItem('authToken');
        
        if (!token) {
          console.error("No authentication token found");
          setLoadingReviews(false);
          return;
        }
        
        // First get the provider ID from the service
        const providerId = service.provider?.providerId;
        
        if (!providerId) {
          setReviewError("Provider information not found");
          setLoadingReviews(false);
          return;
        }
        
        // Get all reviews for this provider
        const reviewsResponse = await axios.get(`/api/reviews/getByProvider/${providerId}`, {
          headers: { Authorization: `Bearer ${token}` }
        });
        
        // Filter reviews for this specific service
        // This requires checking bookings to match the service ID
        const bookingsResponse = await axios.get('/api/bookings/getAll', {
          headers: { Authorization: `Bearer ${token}` }
        });
        
        // Get bookings for this service
        const serviceBookings = bookingsResponse.data.filter(booking => 
          booking.service?.serviceId === service.serviceId
        ).map(booking => booking.bookingId);
        
        // Filter reviews for these bookings
        const serviceReviews = reviewsResponse.data.filter(review => 
          serviceBookings.includes(review.booking?.bookingId) || 
          serviceBookings.includes(review.bookingId)
        );
        
        // Sort by date, newest first
        serviceReviews.sort((a, b) => 
          new Date(b.reviewDate) - new Date(a.reviewDate)
        );
        
        setReviews(serviceReviews);
      } catch (error) {
        console.error("Error fetching reviews:", error);
        setReviewError("Failed to load reviews");
      } finally {
        setLoadingReviews(false);
      }
    };
    
    fetchReviews();
  }, [service, activeTab]);

  // Fetch customer profile images
  const fetchCustomerImages = async (reviews) => {
    try {
      const token = localStorage.getItem('authToken') || sessionStorage.getItem('authToken');
      if (!token) return;

      const images = {};
      
      for (const review of reviews) {
        if (review.customer?.customerId) {
          try {
            const imageResponse = await axios.get(`/api/customers/getProfileImage/${review.customer.customerId}`, {
              headers: { Authorization: `Bearer ${token}` }
            });
            
            if (imageResponse.data) {
              // Prepend base URL to make a complete image path
              const baseURL = "http://localhost:8080";
              images[review.customer.customerId] = `${baseURL}${imageResponse.data}`;
            }
          } catch (err) {
            console.error(`Error fetching image for customer ${review.customer.customerId}:`, err);
            // Continue without setting the image
          }
        }
      }
      
      setCustomerImages(images);
    } catch (err) {
      console.error('Error fetching customer images:', err);
    }
  };

  useEffect(() => {
    if (reviews && reviews.length > 0) {
      fetchCustomerImages(reviews);
    }
  }, [reviews]);

  const formatTimeAgo = (dateString) => {
    const date = new Date(dateString);
    const now = new Date();
    const diffInSeconds = Math.floor((now - date) / 1000);
    
    if (diffInSeconds < 60) return `${diffInSeconds} seconds ago`;
    
    const diffInMinutes = Math.floor(diffInSeconds / 60);
    if (diffInMinutes < 60) return `${diffInMinutes} ${diffInMinutes === 1 ? 'minute' : 'minutes'} ago`;
    
    const diffInHours = Math.floor(diffInMinutes / 60);
    if (diffInHours < 24) return `${diffInHours} ${diffInHours === 1 ? 'hour' : 'hours'} ago`;
    
    const diffInDays = Math.floor(diffInHours / 24);
    if (diffInDays < 30) return `${diffInDays} ${diffInDays === 1 ? 'day' : 'days'} ago`;
    
    const diffInMonths = Math.floor(diffInDays / 30);
    if (diffInMonths < 12) return `${diffInMonths} ${diffInMonths === 1 ? 'month' : 'months'} ago`;
    
    const diffInYears = Math.floor(diffInMonths / 12);
    return `${diffInYears} ${diffInYears === 1 ? 'year' : 'years'} ago`;
  };

  if (!isOpen || !service) return null;
  
  let overlayStyle = {
    opacity: 0,
    transition: 'opacity 0.3s ease-out'
  };
  
  let modalStyle = {
    transform: 'scale(0.8)',
    opacity: 0,
    transition: 'transform 0.4s ease-out, opacity 0.3s ease-out',
  };
  
  let backdropStyle = {
    backdropFilter: 'blur(0px)',
    backgroundColor: 'rgba(0, 0, 0, 0)',
    transition: 'backdrop-filter 0.4s ease-out, background-color 0.3s ease-out',
  }
  
  if (animationState === 'opening') {
    overlayStyle.opacity = 0;
    modalStyle = {
      ...modalStyle,
      transform: clickPosition ? `translate(${clickPosition.x}px, ${clickPosition.y}px) scale(0.5)` : 'scale(0.8)',
      opacity: 0
    };
    backdropStyle = {
      ...backdropStyle,
      backdropFilter: 'blur(0px)',
      backgroundColor: 'rgba(0, 0, 0, 0)'
    };
  } else if (animationState === 'open') {
    overlayStyle.opacity = 1;
    modalStyle = {
      ...modalStyle,
      transform: 'scale(1) translate(0, 0)',
      opacity: 1
    };
    backdropStyle = {
      ...backdropStyle,
      backdropFilter: 'blur(10px)',
      backgroundColor: 'rgba(0, 0, 0, 0.7)'
    };
  }

  const getImageUrl = (imagePath) => {
    if (!imagePath) return "/default-profile.jpg";
    if (imagePath.startsWith('http')) {
      return imagePath;
    }
    if (imagePath.startsWith('/')) {
      return `${BASE_URL}${imagePath}`;
    }
    return `${BASE_URL}/${imagePath}`;
  };

  const handleImageError = (e) => {
    console.error("Provider image failed to load:", e.target.src);
    if (!imageFailed) {
      setImageFailed(true);
      e.target.src = "/default-profile.jpg";
    }
  };

  const handleProviderClick = (e) => {
    e.stopPropagation();
    console.log("Provider clicked, provider data:", service.provider);
    const providerId = service.provider?.providerId || service.provider?.userId || service.provider?.id;
    if (providerId) {
      console.log(`Navigating to provider details: ${providerId}`);
      onClose();
      setTimeout(() => {
        navigate(`/providerDetails/${providerId}`);
      }, 100);
    } else {
      console.error("No provider ID found in:", service.provider);
    }
  };

  const formatReviewDate = (dateString) => {
    if (!dateString) return "";
    
    const date = new Date(dateString);
    if (isNaN(date)) return "Invalid date";
    
    return date.toLocaleDateString('en-US', {
      year: 'numeric',
      month: 'long', 
      day: 'numeric'
    });
  };

  return (
    <BaseModal
      isOpen={isOpen}
      onClose={onClose}
      clickPosition={clickPosition}
      contentProps={{
        className: "relative bg-white rounded-2xl shadow-2xl overflow-hidden flex flex-col max-h-[90vh]",
        style: { width: '60%' } // Fixed width at 60% of screen width
      }}
    >
      {/* Header Image Area - Fixed height to prevent layout shifts */}
      <div className="relative h-[180px] flex-shrink-0 overflow-hidden w-full">
        <div className="absolute inset-0 bg-gradient-to-b from-[rgba(0,0,0,0.3)] to-[rgba(0,0,0,0.7)]"></div>
        <img 
          src={`${BASE_URL}${service.serviceImage}`}
          alt={service.serviceName}
          className="w-full h-full object-cover"
        />
        <div className="absolute top-6 right-6 bg-[#F4CE14] text-[#495E57] text-2xl font-bold px-6 py-2 rounded-lg shadow-lg">
          ₱{service.price}
        </div>
        <button
          onClick={onClose}
          className="absolute left-5 top-5 bg-white/20 backdrop-blur-sm hover:bg-white/40 transition-colors text-white p-3 rounded-full"
        >
          <svg xmlns="http://www.w3.org/2000/svg" className="h-5 w-5" viewBox="0 0 20 20" fill="currentColor">
            <path fillRule="evenodd" d="M4.293 4.293a1 1 0 011.414 0L10 8.586l4.293-4.293a1 1 0 111.414 1.414L11.414 10l4.293 4.293a1 1 0 01-1.414 1.414L10 11.414l-4.293 4.293a1 1 0 01-1.414-1.414L8.586 10 4.293 5.707a1 1 0 010-1.414z" clipRule="evenodd" />
          </svg>
        </button>
        <div className="absolute bottom-0 left-0 right-0 p-6 text-white">
          <div className="flex justify-between items-end">
            <div>
              <h2 className="text-3xl lg:text-4xl font-bold mb-2 drop-shadow-md">{service.serviceName}</h2>
              <div className="flex items-center gap-3 mb-2">
                <span className="bg-[#495E57]/80 backdrop-blur-sm px-3 py-1 rounded-full text-sm font-medium">
                  {service.categoryName}
                </span>
                <span className="bg-white/20 backdrop-blur-sm px-3 py-1 rounded-full text-sm">
                  {service.durationEstimate || "Duration not specified"}
                </span>
              </div>
            </div>
            <div className="flex items-center">
              {serviceRatings[service.serviceId]?.reviewCount > 0 ? (
                <div className="flex items-center bg-white/20 backdrop-blur-sm px-3 py-1.5 rounded-lg">
                  <div className="flex">
                    {renderStars(serviceRatings[service.serviceId]?.averageRating || 0)}
                  </div>
                  <span className="ml-1.5 font-medium">
                    {serviceRatings[service.serviceId]?.averageRating?.toFixed(1)}
                  </span>
                  <span className="ml-1 text-xs text-gray-200">
                    ({serviceRatings[service.serviceId]?.reviewCount})
                  </span>
                </div>
              ) : (
                <span className="bg-white/20 backdrop-blur-sm px-3 py-1.5 rounded-lg text-sm">
                  No reviews yet
                </span>
              )}
            </div>
          </div>
        </div>
      </div>

      {/* Tabs Navigation - Keep compact and consistent */}
      <div className="flex border-b border-gray-200 bg-white flex-shrink-0 w-full">
        <button 
          className={`px-6 py-4 font-medium text-sm flex items-center transition-colors ${activeTab === 'overview' ? 'text-[#495E57] border-b-2 border-[#F4CE14]' : 'text-gray-500 hover:text-gray-700'}`}
          onClick={() => setActiveTab('overview')}
        >
          <svg xmlns="http://www.w3.org/2000/svg" className="h-4 w-4 mr-2" viewBox="0 0 20 20" fill="currentColor">
            <path fillRule="evenodd" d="M18 10a8 8 0 11-16 0 8 8 0 0116 0zm-7-4a1 1 0 11-2 0 1 1 0 012 0zM9 9a1 1 0 000 2v3a1 1 0 001 1h1a1 1 0 100-2v-3a1 1 0 00-1-1H9z" clipRule="evenodd" />
          </svg>
          Overview
        </button>
        <button 
          className={`px-6 py-4 font-medium text-sm flex items-center transition-colors ${activeTab === 'provider' ? 'text-[#495E57] border-b-2 border-[#F4CE14]' : 'text-gray-500 hover:text-gray-700'}`}
          onClick={() => setActiveTab('provider')}
        >
          <svg xmlns="http://www.w3.org/2000/svg" className="h-4 w-4 mr-2" viewBox="0 0 20 20" fill="currentColor">
            <path fillRule="evenodd" d="M10 9a3 3 0 100-6 3 3 0 000 6zm-7 9a7 7 0 1114 0H3z" clipRule="evenodd" />
          </svg>
          Provider
        </button>
        <button 
          className={`px-6 py-4 font-medium text-sm flex items-center transition-colors ${activeTab === 'reviews' ? 'text-[#495E57] border-b-2 border-[#F4CE14]' : 'text-gray-500 hover:text-gray-700'}`}
          onClick={() => setActiveTab('reviews')}
        >
          <svg xmlns="http://www.w3.org/2000/svg" className="h-4 w-4 mr-2" viewBox="0 0 20 20" fill="currentColor">
            <path d="M9.049 2.927c.3-.921 1.603-.921 1.902 0l1.07 3.292a1 1 0 00.95.69h3.462c.969 0 1.371 1.24.588 1.81l-2.8 2.034a1 1 0 00-.364 1.118l1.07 3.292c.3.921-.755 1.688-1.54 1.118l-2.8-2.034a1 1 0 00-1.175 0l-2.8 2.034c-.784.57-1.838-.197-1.539-1.118l1.07-3.292a1 1 0 00-.364-1.118L2.98 8.72c-.783-.57-.38-1.81.588-1.81h3.461a1 1 0 00.951-.69l1.07-3.292z" />
          </svg>
          Reviews
        </button>
      </div>

      {/* Content Area - Make this scrollable with fixed width */}
      <div className="overflow-y-auto p-4 flex-grow w-full">
        {activeTab === 'overview' && (
          <div className="flex flex-col md:flex-row gap-4 w-full">
            <div className="md:w-2/3">
              <div className="mb-6">
                <h3 className="text-xl font-bold text-[#495E57] mb-4 flex items-center">
                  <svg xmlns="http://www.w3.org/2000/svg" className="h-5 w-5 mr-2" viewBox="0 0 20 20" fill="currentColor">
                    <path fillRule="evenodd" d="M4 4a2 2 0 012-2h4.586A2 2 0 0112 2.586L15.414 6A2 2 0 0116 7.414V16a2 2 0 01-2 2H6a2 2 0 01-2-2V4z" clipRule="evenodd" />
                  </svg>
                  Service Details
                </h3>
                <p className="text-gray-700 leading-relaxed bg-gray-50 p-4 rounded-lg border-l-4 border-[#495E57]">
                  {service.serviceDescription}
                </p>
              </div>
              <div className="mb-6">
                <h3 className="text-lg font-semibold text-[#495E57] mb-3 flex justify-start">What's Included</h3>
                <div className="bg-gray-50 p-4 rounded-lg">
                  <ul className="space-y-2">
                    <li className="flex items-start">
                      <svg xmlns="http://www.w3.org/2000/svg" className="h-5 w-5 text-green-500 mr-2 mt-0.5" viewBox="0 0 20 20" fill="currentColor">
                        <path fillRule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zm3.707-9.293a1 1 0 00-1.414-1.414L9 10.586 7.707 9.293a1 1 0 00-1.414 1.414l2 2a1 1 0 001.414 0l4-4z" clipRule="evenodd" />
                      </svg>
                      <span className="text-gray-700">{service.categoryName} service</span>
                    </li>
                    <li className="flex items-start">
                      <svg xmlns="http://www.w3.org/2000/svg" className="h-5 w-5 text-green-500 mr-2 mt-0.5" viewBox="0 0 20 20" fill="currentColor">
                        <path fillRule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zm3.707-9.293a1 1 0 00-1.414-1.414L9 10.586 7.707 9.293a1 1 0 00-1.414 1.414l2 2a1 1 0 001.414 0l4-4z" clipRule="evenodd" />
                      </svg>
                      <span className="text-gray-700">Service by experienced provider</span>
                    </li>
                    <li className="flex items-start">
                      <svg xmlns="http://www.w3.org/2000/svg" className="h-5 w-5 text-green-500 mr-2 mt-0.5" viewBox="0 0 20 20" fill="currentColor">
                        <path fillRule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zm3.707-9.293a1 1 0 00-1.414-1.414L9 10.586 7.707 9.293a1 1 0 00-1.414 1.414l2 2a1 1 0 001.414 0l4-4z" clipRule="evenodd" />
                      </svg>
                      <span className="text-gray-700">Estimated duration: {service.durationEstimate || "To be determined"}</span>
                    </li>
                  </ul>
                </div>
              </div>
              <div className="mb-6 bg-gray-50 p-4 rounded-lg">
                <h3 className="text-lg font-semibold text-[#495E57] mb-3 flex justify-start">About the Provider</h3>
                <div 
                  className="flex items-center cursor-pointer" 
                  onClick={handleProviderClick}
                >
                  <img 
                    src={getImageUrl(service.provider?.profileImage)} 
                    alt="Provider"
                    onError={handleImageError}
                    className="w-14 h-14 rounded-full border-2 border-[#F4CE14] object-cover mr-4"
                  />
                  <div>
                    <h4 className="font-medium text-[#495E57] flex items-center">
                      {service.provider?.firstName} {service.provider?.lastName}
                      {service.provider?.verified && (
                        <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 20 20" fill="#F4CE14" className="w-4 h-4 ml-1">
                          <path fillRule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zm3.707-9.293a1 1 0 00-1.414-1.414L9 10.586 7.707 9.293a1 1 0 00-1.414 1.414l2 2a1 1 0 001.414 0l4-4z" clipRule="evenodd" />
                        </svg>
                      )}
                    </h4>
                    <p className="text-sm text-gray-500 flex justify-start">{service.provider?.businessName || "Independent Service Provider"}</p>
                  </div>
                  <svg xmlns="http://www.w3.org/2000/svg" className="h-5 w-5 text-gray-400 ml-auto" viewBox="0 0 20 20" fill="currentColor">
                    <path fillRule="evenodd" d="M7.293 14.707a1 1 0 010-1.414L10.586 10 7.293 6.707a1 1 0 011.414-1.414l4 4a1 1 0 010 1.414l-4 4a1 1 0 01-1.414 0z" clipRule="evenodd" />
                  </svg>
                </div>
              </div>
            </div>
            <div className="md:w-1/3">
              <div className="bg-[#495E57]/5 p-5 rounded-xl border border-gray-200 sticky top-4">
                <h3 className="text-xl font-bold text-[#495E57] mb-4 text-center">Book This Service</h3>
                <div className="flex justify-between items-center mb-4 bg-white p-3 rounded-lg shadow-sm">
                  <span className="text-gray-700 font-medium">Service Price</span>
                  <span className="text-[#495E57] font-bold">₱{service.price}</span>
                </div>
                <div className="mb-2 text-sm text-gray-500">
                  <p className="flex items-center gap-2 mb-2">
                    <svg xmlns="http://www.w3.org/2000/svg" className="h-5 w-5 text-[#F4CE14]" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                      <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z" />
                    </svg>
                    Book now
                  </p>
                  <p className="flex items-center gap-2 mb-2">
                    <svg xmlns="http://www.w3.org/2000/svg" className="h-5 w-5 text-[#F4CE14]" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                      <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z" />
                    </svg>
                    Secure preferred time slot
                  </p>
                  <p className="flex items-center gap-2">
                    <svg xmlns="http://www.w3.org/2000/svg" className="h-5 w-5 text-[#F4CE14]" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                      <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z" />
                    </svg>
                    Reliable service
                  </p>
                </div>
              </div>
            </div>
          </div>
        )}
        
        {activeTab === 'provider' && (
          <div className="md:flex gap-4 w-full">
            <div className="md:w-1/3 mb-6 md:mb-0">
              <div className="bg-gray-50 p-5 rounded-xl border border-gray-200 flex flex-col items-center">
                <img
                  src={getImageUrl(service.provider?.profileImage)}
                  alt="Provider"
                  onError={handleImageError}
                  className="w-32 h-32 rounded-full border-4 border-[#F4CE14] object-cover shadow-lg mb-4"
                />
                <h3 className="text-xl font-bold text-[#495E57] mb-1 flex items-center">
                  {service.provider?.firstName} {service.provider?.lastName}
                  {service.provider?.verified && (
                    <span className="ml-2 bg-[#F4CE14]/20 p-1 rounded-full">
                      <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 20 20" fill="#F4CE14" className="w-4 h-4">
                        <path fillRule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zm3.707-9.293a1 1 0 00-1.414-1.414L9 10.586 7.707 9.293a1 1 0 00-1.414 1.414l2 2a1 1 0 001.414 0l4-4z" clipRule="evenodd" />
                      </svg>
                    </span>
                  )}
                </h3>
                <p className="text-gray-500 mb-4">{service.provider?.businessName || "Independent Provider"}</p>
                <div className="flex items-center mb-4">
                  {renderStars(service.provider?.averageRating || 0)}
                  <span className="ml-2 text-gray-600">
                    ({service.provider?.averageRating?.toFixed(1) || "No ratings"})
                  </span>
                </div>
                <div className="flex flex-col gap-3 w-full">
                  <div className="flex items-center justify-between bg-white p-3 rounded-lg shadow-sm">
                    <div className="flex items-center">
                      <svg xmlns="http://www.w3.org/2000/svg" className="h-5 w-5 text-[#495E57] mr-2" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M21 13.255A23.931 23.931 0 0112 15c-3.183 0-6.22-.62-9-1.745M16 6V4a2 2 0 00-2-2h-4a2 2 0 00-2 2v2m4 6h.01M5 20h14a2 2 0 002-2V8a2 2 0 00-2-2H5a2 2 0 00-2 2v10a2 2 0 002 2z" />
                      </svg>
                      <span className="text-sm">Experience</span>
                    </div>
                    <span className="text-sm font-medium text-[#495E57]">
                      {service.provider?.yearsOfExperience ? `${service.provider?.yearsOfExperience} years` : "Not specified"}
                    </span>
                  </div>
                  <div className="flex items-center justify-between bg-white p-3 rounded-lg shadow-sm">
                    <div className="flex items-center">
                      <svg xmlns="http://www.w3.org/2000/svg" className="h-5 w-5 text-[#495E57] mr-2" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M3 5a2 2 0 012-2h3.28a1 1 0 01.948.684l1.498 4.493a1 1 0 01-.502 1.21l-2.257 1.13a11.042 11.042 0 005.516 5.516l1.13-2.257a1 1 0 011.21-.502l4.493 1.498a1 1 0 01.684.949V19a2 2 0 01-2 2h-1C9.716 21 3 14.284 3 6V5z" />
                      </svg>
                      <span className="text-sm">Contact</span>
                    </div>
                    <span className="text-sm font-medium text-[#495E57]">
                      {service.provider?.phoneNumber || "Not available"}
                    </span>
                  </div>
                  <div className="flex items-center justify-between bg-white p-3 rounded-lg shadow-sm">
                    <div className="flex items-center">
                      <svg xmlns="http://www.w3.org/2000/svg" className="h-5 w-5 text-[#495E57] mr-2" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z" />
                      </svg>
                      <span className="text-sm">Availability</span>
                    </div>
                    <span className="text-sm font-medium text-[#495E57]">
                      {service.provider?.availabilitySchedule || "Contact for details"}
                    </span>
                  </div>
                </div>
              </div>
            </div>
            <div className="md:w-2/3">
              <div className="bg-white p-5 rounded-xl border border-gray-200 mb-6">
                <h3 className="text-xl font-bold text-[#495E57] mb-4 flex items-center">
                  About the Provider
                </h3>
                <p className="text-gray-600 mb-4 leading-relaxed text-left">
                  {service.provider?.firstName} {service.provider?.lastName} is a professional {service.categoryName} service provider 
                  {service.provider?.yearsOfExperience ? ` with ${service.provider.yearsOfExperience} years of experience` : ''}.
                  They specialize in providing high-quality services and are committed to customer satisfaction.
                </p>
                <button
                  onClick={handleProviderClick}
                  className="text-[#495E57] hover:text-[#F4CE14] font-medium flex items-center transition-colors"
                >
                  View Full Profile
                  <svg xmlns="http://www.w3.org/2000/svg" className="h-4 w-4 ml-1" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M14 5l7 7m0 0l-7 7m7-7H3" />
                  </svg>
                </button>
              </div>
              <div className="bg-white p-5 rounded-xl border border-gray-200">
                <h3 className="text-xl font-bold text-[#495E57] mb-4 flex items-center">
                  Service by {service.provider?.firstName}
                </h3>
                <div className="bg-[#FFFBEB] p-4 rounded-lg ">
                  <div className="flex justify-between items-center">
                    <div>
                      <h4 className="font-medium text-[#495E57]">{service.serviceName}</h4>
                      <p className="text-sm text-gray-600 text-left">{service.categoryName}</p>
                    </div>
                    <span className="text-lg font-bold text-[#495E57]">₱{service.price}</span>
                  </div>
                </div>
              </div>
            </div>
          </div>
        )}
        
        {activeTab === 'reviews' && (
          <div className="w-full">
            {reviews && reviews.length > 0 ? (
              <motion.div 
                className="space-y-6"
                initial={{ opacity: 0 }}
                animate={{ opacity: 1 }}
                transition={{ duration: 0.4 }}
              >
                <motion.div 
                  className="bg-gradient-to-r from-gray-50 to-white p-4 rounded-lg border border-gray-100 shadow-sm mb-6"
                  initial={{ y: 20, opacity: 0 }}
                  animate={{ y: 0, opacity: 1 }}
                  transition={{ duration: 0.4, delay: 0.1 }}
                >
                  <div className="flex flex-col sm:flex-row sm:items-center justify-between">
                    <div className="mb-4 sm:mb-0">
                      <h3 className="text-lg font-semibold text-gray-800">Customer Reviews</h3>
                      <div className="flex items-center mt-1">
                        <div className="flex">
                          {[...Array(5)].map((_, i) => {
                            const avgRating = reviews.reduce((acc, review) => acc + review.rating, 0) / reviews.length;
                            return (
                              <svg 
                                key={i} 
                                className={`w-5 h-5 ${i < Math.round(avgRating) ? 'text-yellow-400' : 'text-gray-300'}`}
                                fill="currentColor" 
                                viewBox="0 0 20 20"
                              >
                                <path d="M9.049 2.927c.3-.921 1.603-.921 1.902 0l1.07 3.292a1 1 0 00.95.69h3.462c.969 0 1.371 1.24.588 1.81l-2.8 2.034a1 1 0 00-.364 1.118l1.07 3.292c.3.921-.755 1.688-1.54 1.118l-2.8-2.034a1 1 0 00-1.175 0l-2.8 2.034c-.784.57-1.838-.197-1.539-1.118l1.07-3.292a1 1 0 00-.364-1.118L2.98 8.72c-.783-.57-.38-1.81.588-1.81h3.461a1 1 0 00.951-.69l1.07-3.292z" />
                              </svg>
                            );
                          })}
                        </div>
                        <span className="ml-2 text-sm text-gray-600">
                          <span className="font-medium text-gray-800">{reviews.length}</span> {reviews.length === 1 ? 'review' : 'reviews'}
                        </span>
                      </div>
                    </div>
                    
                    <div className="flex gap-1">
                      <select 
                        className="text-sm border border-gray-300 rounded-md px-3 py-2 bg-white focus:ring-[#F4CE14] focus:border-[#F4CE14]"
                        value={reviewFilter}
                        onChange={(e) => setReviewFilter(e.target.value)}
                      >
                        <option value="newest">Newest First</option>
                        <option value="oldest">Oldest First</option>
                        <option value="highest">Highest Rated</option>
                        <option value="lowest">Lowest Rated</option>
                      </select>
                    </div>
                  </div>
                </motion.div>
                
                {sortedReviews.map((review, index) => (
                  <motion.div 
                    key={index} 
                    className="bg-white p-5 rounded-xl shadow-sm border border-gray-200 hover:shadow-md transition-shadow duration-200"
                    initial={{ opacity: 0, y: 20 }}
                    animate={{ opacity: 1, y: 0 }}
                    transition={{ delay: 0.1 + index * 0.05, duration: 0.4 }}
                    whileHover={{ y: -2, transition: { duration: 0.2 } }}
                  >
                    <div className="flex items-start">
                      <div className="mr-4">
                        {customerImages[review.customer?.customerId] ? (
                          <img 
                            src={customerImages[review.customer.customerId]}
                            alt={`${review.customer?.firstName || 'Customer'}`}
                            className="w-12 h-12 rounded-full object-cover border-2 border-white shadow-sm"
                            onError={(e) => {
                              e.target.onerror = null;
                              setCustomerImages(prev => {
                                const updated = {...prev};
                                delete updated[review.customer.customerId];
                                return updated;
                              });
                            }}
                          />
                        ) : (
                          <div className="w-12 h-12 bg-gradient-to-br from-[#495E57] to-[#3e4f49] rounded-full flex items-center justify-center text-white text-lg font-semibold uppercase shadow-sm">
                            {review.customer?.firstName?.charAt(0) || '?'}
                          </div>
                        )}
                      </div>
                      
                      <div className="flex-1">
                        <div className="flex flex-wrap justify-between items-start">
                          <div>
                            <h4 className="font-semibold text-[#495E57]">
                              {review.customer?.firstName || 'Anonymous'} {review.customer?.lastName || ''}
                            </h4>
                            
                            <div className="flex items-center mt-1">
                              <div className="flex">
                                {[...Array(5)].map((_, i) => (
                                  <svg 
                                    key={i} 
                                    className={`w-4 h-4 ${i < review.rating ? 'text-yellow-400' : 'text-gray-300'}`}
                                    fill="currentColor" 
                                    viewBox="0 0 20 20"
                                  >
                                    <path d="M9.049 2.927c.3-.921 1.603-.921 1.902 0l1.07 3.292a1 1 0 00.95.69h3.462c.969 0 1.371 1.24.588 1.81l-2.8 2.034a1 1 0 00-.364 1.118l1.07 3.292c.3.921-.755 1.688-1.54 1.118l-2.8-2.034a1 1 0 00-1.175 0l-2.8 2.034c-.784.57-1.838-.197-1.539-1.118l1.07-3.292a1 1 0 00-.364-1.118L2.98 8.72c-.783-.57-.38-1.81.588-1.81h3.461a1 1 0 00.951-.69l1.07-3.292z" />
                                  </svg>
                                ))}
                              </div>
                            </div>
                          </div>
                          
                          <span className="text-xs text-gray-500 mt-1">
                            {formatTimeAgo(review.reviewDate)}
                          </span>
                        </div>
                        
                        <div className="mt-3">
                          <p className="text-gray-700 leading-relaxed text-left">
                            {review.comment}
                          </p>
                        </div>
                      </div>
                    </div>
                  </motion.div>
                ))}
              </motion.div>
            ) : (
              <motion.div 
                className="text-center py-12 bg-gray-50 rounded-xl shadow-sm flex flex-col items-center"
                initial={{ opacity: 0, y: 20 }}
                animate={{ opacity: 1, y: 0 }}
                transition={{ duration: 0.5 }}
              >
                <div className="mx-auto w-20 h-20 bg-gray-100 rounded-full flex items-center justify-center mb-4">
                  <svg className="w-10 h-10 text-gray-400" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="1.5" d="M8 12h.01M12 12h.01M16 12h.01M21 12c0 4.418-4.03 8-9 8a9.863 9.863 0 01-4.255-.949L3 20l1.395-3.72C3.512 15.042 3 13.574 3 12c0-4.418 4.03-8 9-8s9 3.582 9 8z" />
                  </svg>
                </div>
                <h3 className="text-xl font-medium text-gray-800 mb-2">No Reviews Yet</h3>
                <p className="text-gray-500 max-w-xs mx-auto mb-6">This service hasn't received any reviews from customers yet.</p>
                <p className="text-sm text-[#495E57]">Be the first to book this service and share your experience!</p>
              </motion.div>
            )}
          </div>
        )}
      </div>

      {/* Footer with Book Button - Fixed position */}
      <div className="p-3 border-t border-gray-200 bg-white flex-shrink-0 w-full">
        <button 
          onClick={onBookService}
          className="bg-[#F4CE14] hover:bg-[#e5c013] text-[#495E57] font-bold px-8 py-3 rounded-lg transition-colors shadow-sm flex items-center gap-2 w-full"
        >
          <svg xmlns="http://www.w3.org/2000/svg" className="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z" />
          </svg>
          Book This Service
        </button>
      </div>
    </BaseModal>
  );
};

export default React.memo(ServiceDetailsModal);
