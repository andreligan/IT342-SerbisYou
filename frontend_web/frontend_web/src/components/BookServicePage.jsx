import React, { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { motion } from 'framer-motion';
import DateTimeSelection from './DateTimeSelection';
import ReviewBookingDetails from './ReviewBookingDetails';
import PaymentConfirmation from './PaymentConfirmation';
import apiClient, { getApiUrl } from '../utils/apiConfig';

function BookServicePage() {
  const { serviceId } = useParams();
  const navigate = useNavigate();
  const [currentStep, setCurrentStep] = useState(1);
  const [service, setService] = useState(null);
  const [provider, setProvider] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [bookingData, setBookingData] = useState({
    serviceId: serviceId,
    selectedDate: '',
    selectedTime: '',
    serviceDetails: {},
    customerDetails: {},
    specialInstructions: '',
    bookingStatus: 'PENDING',
    payment: {
      amount: 0,
      paymentMethod: 'CASH',
      paymentStatus: 'PENDING'
    }
  });

  // Get user information from localStorage or sessionStorage
  const userId = localStorage.getItem('userId') || sessionStorage.getItem('userId');
  const token = localStorage.getItem('authToken') || sessionStorage.getItem('authToken');

  useEffect(() => {
    const fetchData = async () => {
      try {
        setLoading(true);
        
        if (!serviceId || !userId || !token) {
          throw new Error('Required information missing');
        }

        // Fetch service details
        const serviceResponse = await apiClient.get(getApiUrl(`/services/getById/${serviceId}`));
        const serviceData = serviceResponse.data;
        
        if (!serviceData || !serviceData.serviceId) {
          throw new Error('Service not found');
        }
        
        setService(serviceData);
        
        // Update booking data with service price
        const price = parseFloat(serviceData.price || 0);
        setBookingData(prev => ({
          ...prev,
          serviceDetails: serviceData,
          payment: {
            ...prev.payment,
            amount: price
          }
        }));
        
        // If service has provider info, use it to get provider details
        if (serviceData.provider && serviceData.provider.providerId) {
          const providerResponse = await apiClient.get(getApiUrl(`/service-providers/getById/${serviceData.provider.providerId}`));
          setProvider(providerResponse.data);
        }
        
        // Fetch customer details for the booking
        const customerResponse = await apiClient.get(getApiUrl(`/customers/getByAuthId?authId=${userId}`));
        
        if (customerResponse.data && customerResponse.data.customerId) {
          setBookingData(prev => ({
            ...prev,
            customerDetails: customerResponse.data,
          }));
        } else {
          throw new Error('Customer profile not found. Please complete your profile before booking.');
        }
        
        setLoading(false);
      } catch (err) {
        console.error('Error fetching data:', err);
        setError(err.message || 'Failed to load service information');
        setLoading(false);
      }
    };

    fetchData();
  }, [serviceId, userId, token]);

  const handleStepChange = (direction) => {
    if (direction === 'next') {
      setCurrentStep(currentStep + 1);
    } else {
      setCurrentStep(currentStep - 1);
    }
  };

  const handleBookingDataChange = (newData) => {
    setBookingData(prev => ({ ...prev, ...newData }));
  };

  const handleCancel = () => {
    const confirmCancel = window.confirm('Are you sure you want to cancel this booking?');
    if (confirmCancel) {
      navigate('/services');
    }
  };

  const renderStep = () => {
    switch (currentStep) {
      case 1:
        return (
          <DateTimeSelection
            service={service}
            provider={provider}
            bookingData={bookingData}
            onBookingDataChange={handleBookingDataChange}
            onNext={() => handleStepChange('next')}
            onCancel={handleCancel}
          />
        );
      case 2:
        return (
          <ReviewBookingDetails
            service={service}
            provider={provider}
            bookingData={bookingData}
            onBookingDataChange={handleBookingDataChange}
            onNext={() => handleStepChange('next')}
            onBack={() => handleStepChange('back')}
            onCancel={handleCancel}
          />
        );
      case 3:
        return (
          <PaymentConfirmation
            service={service}
            provider={provider}
            bookingData={bookingData}
            onBookingDataChange={handleBookingDataChange}
            onBack={() => handleStepChange('back')}
            onFinish={() => navigate('/customer-bookings')}
          />
        );
      default:
        return <div>Unknown step</div>;
    }
  };

  if (loading) {
    return (
      <div className="flex justify-center items-center min-h-screen bg-gray-50">
        <div className="text-center">
          <div className="w-16 h-16 border-4 border-[#F4CE14] border-t-transparent rounded-full animate-spin mx-auto mb-4"></div>
          <p className="text-gray-600">Loading service details...</p>
        </div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="flex justify-center items-center min-h-screen bg-gray-50 px-4">
        <div className="bg-white p-8 rounded-lg shadow-lg max-w-md w-full text-center">
          <svg className="w-16 h-16 text-red-500 mx-auto mb-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 8v4m0 4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
          </svg>
          <h2 className="text-2xl font-bold text-gray-800 mb-2">Error</h2>
          <p className="text-gray-600 mb-6">{error}</p>
          <button 
            onClick={() => navigate('/services')}
            className="bg-[#495E57] text-white px-6 py-2 rounded-lg hover:bg-[#3a4a45] transition-colors"
          >
            Back to Services
          </button>
        </div>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-gray-50 py-8 px-4">
      <div className="max-w-6xl mx-auto">
        {/* Progress Indicator */}
        <div className="mb-12">
          <div className="flex justify-between items-center max-w-3xl mx-auto">
            {['Select Date & Time', 'Review Details', 'Confirm & Pay'].map((step, index) => (
              <motion.div 
                key={index}
                className="flex flex-col items-center"
                initial={{ opacity: 0, y: 20 }}
                animate={{ opacity: 1, y: 0 }}
                transition={{ delay: 0.1 * index }}
              >
                <div 
                  className={`w-10 h-10 rounded-full flex items-center justify-center text-white font-medium ${
                    currentStep > index + 1 
                      ? 'bg-green-500' 
                      : currentStep === index + 1 
                        ? 'bg-[#F4CE14]' 
                        : 'bg-gray-300'
                  }`}
                >
                  {currentStep > index + 1 ? (
                    <svg className="w-6 h-6" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                      <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M5 13l4 4L19 7" />
                    </svg>
                  ) : (
                    index + 1
                  )}
                </div>
                <span className={`mt-2 text-sm ${currentStep === index + 1 ? 'text-[#495E57] font-medium' : 'text-gray-500'}`}>
                  {step}
                </span>
              </motion.div>
            ))}
          </div>
          <div className="relative max-w-3xl mx-auto mt-4">
            <div className="absolute top-0 left-[calc(16.67%+20px)] right-[calc(16.67%+20px)] h-1 bg-gray-200"></div>
            <div 
              className="absolute top-0 left-[calc(16.67%+20px)] h-1 bg-[#F4CE14] transition-all duration-300"
              style={{
                width: currentStep === 1 
                  ? '0%' 
                  : currentStep === 2 
                    ? '50%' 
                    : '100%'
              }}
            ></div>
          </div>
        </div>

        {/* Step content */}
        {renderStep()}
      </div>
    </div>
  );
}

export default BookServicePage;