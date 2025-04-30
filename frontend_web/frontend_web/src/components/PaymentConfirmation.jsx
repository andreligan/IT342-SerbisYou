import React, { useState } from 'react';
import { motion } from 'framer-motion';
import apiClient, { getApiUrl } from '../utils/apiConfig';

function PaymentConfirmation({ service, provider, bookingData, onBookingDataChange, onBack, onFinish }) {
  const [paymentMethod, setPaymentMethod] = useState('CASH');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);
  const [success, setSuccess] = useState(false);
  const [bookingId, setBookingId] = useState(null);
  const [processingGcash, setProcessingGcash] = useState(false);
  
  const handlePaymentMethodChange = (method) => {
    setPaymentMethod(method);
    onBookingDataChange({
      payment: {
        ...bookingData.payment,
        paymentMethod: method
      }
    });
  };
  
  const createBooking = async (paymentIntent = null) => {
    try {
      setLoading(true);
      setError(null);
      
      // Prepare booking data
      const newBooking = {
        serviceId: service.serviceId,
        customerId: bookingData.customerDetails.customerId,
        providerId: provider.providerId,
        addressId: bookingData.address.addressId,
        bookingDate: bookingData.selectedDate,
        bookingTime: bookingData.selectedTime,
        specialInstructions: bookingData.specialInstructions || '',
        bookingStatus: 'PENDING',
        payment: {
          amount: parseFloat(service.price),
          paymentMethod: paymentMethod,
          paymentStatus: paymentMethod === 'CASH' ? 'PENDING' : 'COMPLETED',
          paymentIntentId: paymentIntent ? paymentIntent.id : null
        }
      };
      
      // Create booking
      const response = await apiClient.post(getApiUrl('/bookings/create'), newBooking);
      
      if (response.data && response.data.bookingId) {
        setBookingId(response.data.bookingId);
        setSuccess(true);
        return response.data;
      } else {
        throw new Error('Failed to create booking');
      }
    } catch (err) {
      console.error('Error creating booking:', err);
      setError(err.message || 'Failed to create booking. Please try again.');
      return null;
    } finally {
      setLoading(false);
    }
  };
  
  const handleCashPayment = async () => {
    await createBooking();
  };
  
  const handleGCashPayment = async () => {
    try {
      setProcessingGcash(true);
      setError(null);
      
      // Create GCash checkout session
      const gcashData = {
        amount: parseFloat(service.price),
        description: `Payment for ${service.serviceName}`,
        successUrl: `${window.location.origin}/payment-success`,
        cancelUrl: `${window.location.origin}/payment-cancel`
      };
      
      // Get checkout URL from backend
      const checkoutResponse = await apiClient.post(getApiUrl('/payments/create-gcash-checkout'), gcashData);
      
      if (checkoutResponse.data && checkoutResponse.data.checkoutUrl) {
        // Create booking with payment intent
        const booking = await createBooking({
          id: checkoutResponse.data.paymentIntentId,
          status: 'pending'
        });
        
        if (booking) {
          // Redirect to GCash checkout
          window.location.href = checkoutResponse.data.checkoutUrl;
        }
      } else {
        throw new Error('Failed to create GCash payment session');
      }
    } catch (err) {
      console.error('Error processing GCash payment:', err);
      setError(err.message || 'Failed to process GCash payment. Please try again.');
      setProcessingGcash(false);
    }
  };
  
  const handleConfirm = async () => {
    if (paymentMethod === 'CASH') {
      await handleCashPayment();
    } else if (paymentMethod === 'GCASH') {
      await handleGCashPayment();
    }
  };
  
  if (success) {
    return (
      <motion.div 
        className="bg-white rounded-lg shadow-lg overflow-hidden"
        initial={{ opacity: 0, scale: 0.95 }}
        animate={{ opacity: 1, scale: 1 }}
        transition={{ duration: 0.5 }}
      >
        <div className="p-8 text-center">
          <div className="w-20 h-20 mx-auto bg-green-100 rounded-full flex items-center justify-center mb-6">
            <svg className="w-12 h-12 text-green-600" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M5 13l4 4L19 7" />
            </svg>
          </div>
          <h2 className="text-2xl font-bold text-gray-800 mb-4">Booking Confirmed!</h2>
          <p className="text-gray-600 mb-6">
            Your service has been booked successfully. You can view your booking details in your bookings page.
          </p>
          <div className="bg-gray-50 p-4 rounded-lg mb-6 max-w-sm mx-auto">
            <div className="text-left">
              <div className="flex justify-between mb-2">
                <span className="text-gray-600">Booking ID:</span>
                <span className="font-medium">{bookingId}</span>
              </div>
              <div className="flex justify-between mb-2">
                <span className="text-gray-600">Service:</span>
                <span className="font-medium">{service.serviceName}</span>
              </div>
              <div className="flex justify-between mb-2">
                <span className="text-gray-600">Date:</span>
                <span className="font-medium">{bookingData.selectedDate}</span>
              </div>
              <div className="flex justify-between">
                <span className="text-gray-600">Time:</span>
                <span className="font-medium">{bookingData.selectedTime}</span>
              </div>
            </div>
          </div>
          <motion.button
            type="button"
            className="px-8 py-3 bg-[#495E57] text-white rounded-lg hover:bg-[#3a4a45] transition-colors"
            onClick={onFinish}
            whileHover={{ scale: 1.05 }}
            whileTap={{ scale: 0.95 }}
          >
            View My Bookings
          </motion.button>
        </div>
      </motion.div>
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
        <h2 className="text-2xl font-bold text-[#495E57]">Payment Method</h2>
        <p className="text-gray-600">Choose how you'd like to pay for this service</p>
      </div>
      
      <div className="p-6">
        <div className="grid grid-cols-1 lg:grid-cols-2 gap-8">
          {/* Left column - Order Summary */}
          <div>
            <div className="bg-gray-50 p-5 rounded-lg">
              <h3 className="font-semibold text-lg mb-4 text-[#495E57] border-b border-gray-200 pb-2">Order Summary</h3>
              <div className="space-y-4">
                <div className="flex justify-between">
                  <span className="text-gray-600">Service:</span>
                  <span className="font-medium">{service.serviceName}</span>
                </div>
                <div className="flex justify-between">
                  <span className="text-gray-600">Provider:</span>
                  <span className="font-medium">{provider.businessName || `${provider.firstName} ${provider.lastName}`}</span>
                </div>
                <div className="flex justify-between">
                  <span className="text-gray-600">Date & Time:</span>
                  <span className="font-medium">{bookingData.selectedDate} at {bookingData.selectedTime}</span>
                </div>
                <div className="flex justify-between">
                  <span className="text-gray-600">Location:</span>
                  <span className="font-medium">{bookingData.address?.streetName}, {bookingData.address?.barangay}</span>
                </div>
              </div>
              
              <div className="mt-6 pt-4 border-t border-gray-200">
                <div className="flex justify-between items-center">
                  <span className="text-lg text-[#495E57] font-medium">Total:</span>
                  <span className="text-xl font-bold text-[#495E57]">₱{service.price}</span>
                </div>
              </div>
            </div>
          </div>
          
          {/* Right column - Payment Options */}
          <div>
            <h3 className="font-semibold text-lg mb-4 text-[#495E57]">Select Payment Method</h3>
            
            {/* Payment methods */}
            <div className="space-y-4">
              {/* Cash on Delivery */}
              <div 
                className={`p-4 border rounded-lg cursor-pointer transition-all ${
                  paymentMethod === 'CASH' ? 'border-[#F4CE14] bg-yellow-50' : 'border-gray-200 hover:border-gray-300'
                }`}
                onClick={() => handlePaymentMethodChange('CASH')}
              >
                <div className="flex items-center">
                  <div className="flex-shrink-0">
                    <div className={`w-5 h-5 rounded-full border flex items-center justify-center ${
                      paymentMethod === 'CASH' ? 'border-[#F4CE14]' : 'border-gray-300'
                    }`}>
                      {paymentMethod === 'CASH' && <div className="w-3 h-3 bg-[#F4CE14] rounded-full"></div>}
                    </div>
                  </div>
                  <div className="ml-3">
                    <span className="font-medium">Cash Payment</span>
                    <p className="text-gray-600 text-sm mt-1">Pay in cash when the service is completed</p>
                  </div>
                </div>
              </div>
              
              {/* GCash */}
              <div 
                className={`p-4 border rounded-lg cursor-pointer transition-all ${
                  paymentMethod === 'GCASH' ? 'border-[#F4CE14] bg-yellow-50' : 'border-gray-200 hover:border-gray-300'
                }`}
                onClick={() => handlePaymentMethodChange('GCASH')}
              >
                <div className="flex items-center">
                  <div className="flex-shrink-0">
                    <div className={`w-5 h-5 rounded-full border flex items-center justify-center ${
                      paymentMethod === 'GCASH' ? 'border-[#F4CE14]' : 'border-gray-300'
                    }`}>
                      {paymentMethod === 'GCASH' && <div className="w-3 h-3 bg-[#F4CE14] rounded-full"></div>}
                    </div>
                  </div>
                  <div className="ml-3">
                    <span className="font-medium">GCash</span>
                    <p className="text-gray-600 text-sm mt-1">Pay online using GCash</p>
                  </div>
                </div>
              </div>
            </div>
            
            {/* Error message */}
            {error && (
              <div className="mt-6 p-3 bg-red-50 border border-red-200 rounded-lg">
                <p className="text-red-700 text-sm">{error}</p>
              </div>
            )}
            
            {/* Payment details explanation based on selection */}
            <div className="mt-6 p-4 bg-blue-50 border border-blue-100 rounded-lg">
              <h4 className="font-medium text-blue-800 mb-2">
                {paymentMethod === 'CASH' ? 'Cash Payment Details' : 'GCash Payment Details'}
              </h4>
              <p className="text-sm text-blue-700">
                {paymentMethod === 'CASH' 
                  ? 'You will pay the service provider directly in cash after the service is completed. No advance payment is required.'
                  : 'You will be redirected to GCash payment gateway to complete your payment. The booking will be confirmed after successful payment.'}
              </p>
            </div>
          </div>
        </div>
        
        {/* Navigation Buttons */}
        <div className="mt-8 flex justify-between">
          <motion.button
            type="button"
            className="px-6 py-2 border border-gray-300 rounded-lg text-gray-700 hover:bg-gray-50 transition-colors"
            onClick={onBack}
            disabled={loading || processingGcash}
            whileHover={{ scale: 1.05 }}
            whileTap={{ scale: 0.95 }}
          >
            Back
          </motion.button>
          
          <motion.button
            type="button"
            className={`px-8 py-3 rounded-lg text-white font-medium flex items-center ${
              loading || processingGcash ? 'bg-gray-400 cursor-not-allowed' : 'bg-[#495E57] hover:bg-[#3a4a45]'
            }`}
            onClick={handleConfirm}
            disabled={loading || processingGcash}
            whileHover={!loading && !processingGcash ? { scale: 1.05 } : {}}
            whileTap={!loading && !processingGcash ? { scale: 0.95 } : {}}
          >
            {loading || processingGcash ? (
              <>
                <div className="w-5 h-5 border-2 border-white border-t-transparent rounded-full animate-spin mr-2"></div>
                <span>Processing...</span>
              </>
            ) : (
              <>
                <span>{paymentMethod === 'CASH' ? 'Confirm Booking' : 'Pay with GCash'}</span>
                <svg className="ml-2 w-5 h-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M14 5l7 7m0 0l-7 7m7-7H3" />
                </svg>
              </>
            )}
          </motion.button>
        </div>
      </div>
    </motion.div>
  );
}

export default PaymentConfirmation;
