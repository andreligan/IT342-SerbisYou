import React, { useState, useEffect } from 'react';
import { motion } from 'framer-motion';
import apiClient, { getApiUrl } from '../utils/apiConfig';

function ReviewBookingDetails({ service, provider, bookingData, onBookingDataChange, onNext, onBack, onCancel }) {
  const [addresses, setAddresses] = useState([]);
  const [selectedAddress, setSelectedAddress] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  
  const userId = localStorage.getItem('userId') || sessionStorage.getItem('userId');
  const token = localStorage.getItem('authToken') || sessionStorage.getItem('authToken');
  
  useEffect(() => {
    const fetchCustomerAddresses = async () => {
      try {
        setLoading(true);
        
        if (!userId || !token) {
          setError('Authentication required');
          setLoading(false);
          return;
        }
        
        // Fetch all addresses
        const addressResponse = await apiClient.get(getApiUrl('/addresses/getAll'));
        
        // Get customer ID from bookingData if available
        const customerId = bookingData.customerDetails?.customerId;
        
        if (!customerId) {
          setError('Customer information is missing');
          setLoading(false);
          return;
        }
        
        // Filter addresses for this customer
        const customerAddresses = addressResponse.data.filter(addr => 
          addr.customer?.customerId === customerId || addr.customerId === customerId
        );
        
        setAddresses(customerAddresses || []);
        
        // Check if customer has a main address and select it by default
        const mainAddress = customerAddresses.find(addr => addr.main === true);
        
        if (mainAddress) {
          setSelectedAddress(mainAddress);
          onBookingDataChange({ address: mainAddress });
        } else if (customerAddresses.length > 0) {
          // If no main address, select the first one
          setSelectedAddress(customerAddresses[0]);
          onBookingDataChange({ address: customerAddresses[0] });
        }
        
        setLoading(false);
      } catch (error) {
        console.error('Error fetching customer addresses:', error);
        setError('Failed to load address information');
        setLoading(false);
      }
    };
    
    fetchCustomerAddresses();
  }, [userId, token, bookingData.customerDetails]);
  
  const handleAddressSelect = (address) => {
    setSelectedAddress(address);
    onBookingDataChange({ address });
  };
  
  const handleContinue = () => {
    if (!selectedAddress) {
      alert('Please select an address to continue');
      return;
    }
    
    onNext();
  };
  
  if (loading) {
    return (
      <div className="flex justify-center items-center min-h-[400px]">
        <div className="w-12 h-12 border-4 border-t-transparent border-[#F4CE14] rounded-full animate-spin"></div>
      </div>
    );
  }
  
  return (
    <motion.div 
      className="bg-white rounded-lg shadow-lg overflow-hidden"
      initial={{ opacity: 0, y: 20 }}
      animate={{ opacity: 1, y: 0 }}
      transition={{ duration: 0.5 }}
    >
      <div className="p-6 border-b border-gray-200">
        <h2 className="text-2xl font-bold text-[#495E57]">Review Booking Details</h2>
        <p className="text-gray-600">Please confirm your service booking details</p>
      </div>
      
      <div className="p-6">
        <div className="grid grid-cols-1 lg:grid-cols-2 gap-8">
          {/* Left column - Service & Date Details */}
          <div>
            <div className="bg-gray-50 p-5 rounded-lg mb-6">
              <h3 className="font-semibold text-lg mb-3 text-[#495E57]">Service Details</h3>
              <div className="space-y-4">
                <div className="flex justify-between">
                  <span className="text-gray-600">Service:</span>
                  <span className="font-medium">{service?.serviceName}</span>
                </div>
                <div className="flex justify-between">
                  <span className="text-gray-600">Provider:</span>
                  <span className="font-medium">{provider?.businessName || `${provider?.firstName} ${provider?.lastName}`}</span>
                </div>
                <div className="flex justify-between">
                  <span className="text-gray-600">Price:</span>
                  <span className="font-medium">₱{service?.price}</span>
                </div>
                <div className="flex justify-between">
                  <span className="text-gray-600">Duration:</span>
                  <span className="font-medium">{service?.durationEstimate}</span>
                </div>
                <div className="border-t border-gray-200 pt-3 mt-3">
                  <p className="text-sm text-gray-500 mb-2">Description:</p>
                  <p className="text-sm">{service?.serviceDescription}</p>
                </div>
              </div>
            </div>
            
            <div className="bg-gray-50 p-5 rounded-lg">
              <h3 className="font-semibold text-lg mb-3 text-[#495E57]">Appointment Details</h3>
              <div className="space-y-4">
                <div className="flex justify-between">
                  <span className="text-gray-600">Date:</span>
                  <span className="font-medium">{bookingData.selectedDate}</span>
                </div>
                <div className="flex justify-between">
                  <span className="text-gray-600">Time:</span>
                  <span className="font-medium">{bookingData.selectedTime}</span>
                </div>
              </div>
              {bookingData.specialInstructions && (
                <div className="mt-4 pt-3 border-t border-gray-200">
                  <p className="text-sm text-gray-500 mb-2">Special Instructions:</p>
                  <p className="text-sm">{bookingData.specialInstructions}</p>
                </div>
              )}
            </div>
          </div>
          
          {/* Right column - Customer Details & Address Selection */}
          <div>
            <div className="bg-gray-50 p-5 rounded-lg mb-6">
              <h3 className="font-semibold text-lg mb-3 text-[#495E57]">Customer Details</h3>
              <div className="space-y-4">
                <div className="flex justify-between">
                  <span className="text-gray-600">Name:</span>
                  <span className="font-medium">
                    {bookingData.customerDetails?.firstName} {bookingData.customerDetails?.lastName}
                  </span>
                </div>
                <div className="flex justify-between">
                  <span className="text-gray-600">Phone:</span>
                  <span className="font-medium">{bookingData.customerDetails?.phoneNumber || 'Not provided'}</span>
                </div>
                <div className="flex justify-between">
                  <span className="text-gray-600">Email:</span>
                  <span className="font-medium">{bookingData.customerDetails?.userAuth?.email || 'Not provided'}</span>
                </div>
              </div>
            </div>
            
            <div>
              <h3 className="font-semibold text-lg mb-3 text-[#495E57]">Service Location</h3>
              
              {error ? (
                <div className="p-4 bg-red-50 border border-red-200 rounded-lg text-center">
                  <p className="text-red-700">{error}</p>
                </div>
              ) : addresses.length === 0 ? (
                <div className="p-4 bg-yellow-50 border border-yellow-200 rounded-lg text-center">
                  <p className="text-yellow-700">No addresses found. Please add an address in your profile.</p>
                </div>
              ) : (
                <div className="space-y-3">
                  {addresses.map((address) => (
                    <div 
                      key={address.addressId} 
                      className={`p-4 border rounded-lg cursor-pointer transition-all ${
                        selectedAddress?.addressId === address.addressId 
                          ? 'border-[#F4CE14] bg-yellow-50' 
                          : 'border-gray-200 hover:border-gray-300'
                      }`}
                      onClick={() => handleAddressSelect(address)}
                    >
                      <div className="flex items-start">
                        <div className="flex-shrink-0">
                          <div className={`w-5 h-5 rounded-full border flex items-center justify-center ${
                            selectedAddress?.addressId === address.addressId 
                              ? 'border-[#F4CE14]' 
                              : 'border-gray-300'
                          }`}>
                            {selectedAddress?.addressId === address.addressId && (
                              <div className="w-3 h-3 bg-[#F4CE14] rounded-full"></div>
                            )}
                          </div>
                        </div>
                        <div className="ml-3">
                          <div className="flex items-center mb-1">
                            <span className="font-medium">{address.streetName}</span>
                            {address.main && (
                              <span className="ml-2 bg-blue-100 text-blue-800 text-xs px-2 py-0.5 rounded">Default</span>
                            )}
                          </div>
                          <p className="text-gray-600 text-sm">
                            {address.barangay}, {address.city}, {address.province}, {address.zipCode}
                          </p>
                        </div>
                      </div>
                    </div>
                  ))}
                </div>
              )}
              
              <div className="mt-6 text-center">
                <a 
                  href="/customer-profile/address" 
                  target="_blank"
                  rel="noopener noreferrer"
                  className="text-[#495E57] hover:text-[#F4CE14] text-sm inline-flex items-center"
                >
                  <svg className="w-4 h-4 mr-1" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 6v6m0 0v6m0-6h6m-6 0H6" />
                  </svg>
                  Add a new address
                </a>
              </div>
            </div>
          </div>
        </div>
        
        {/* Total Price */}
        <div className="mt-8 p-5 bg-[#495E57]/5 rounded-lg">
          <div className="flex justify-between items-center">
            <span className="text-lg text-[#495E57] font-medium">Total Price:</span>
            <span className="text-xl font-bold text-[#495E57]">₱{service?.price}</span>
          </div>
        </div>
        
        {/* Navigation Buttons */}
        <div className="mt-8 flex justify-between">
          <div>
            <motion.button
              type="button"
              className="px-6 py-2 border border-gray-300 rounded-lg text-gray-700 hover:bg-gray-50 transition-colors mr-3"
              onClick={onBack}
              whileHover={{ scale: 1.05 }}
              whileTap={{ scale: 0.95 }}
            >
              Back
            </motion.button>
            <motion.button
              type="button"
              className="px-6 py-2 border border-gray-300 rounded-lg text-gray-700 hover:bg-gray-50 transition-colors"
              onClick={onCancel}
              whileHover={{ scale: 1.05 }}
              whileTap={{ scale: 0.95 }}
            >
              Cancel
            </motion.button>
          </div>
          
          <motion.button
            type="button"
            className={`px-8 py-3 rounded-lg bg-[#F4CE14] text-[#495E57] font-medium hover:bg-[#e6c013] transition-colors ${
              !selectedAddress ? 'opacity-50 cursor-not-allowed' : ''
            }`}
            onClick={handleContinue}
            disabled={!selectedAddress}
            whileHover={selectedAddress ? { scale: 1.05 } : {}}
            whileTap={selectedAddress ? { scale: 0.95 } : {}}
          >
            Proceed to Payment
          </motion.button>
        </div>
      </div>
    </motion.div>
  );
}

export default ReviewBookingDetails;
