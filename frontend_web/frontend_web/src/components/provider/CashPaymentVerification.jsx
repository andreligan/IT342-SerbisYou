import React, { useState } from 'react';
import axios from 'axios';

const CashPaymentVerification = ({ booking }) => {
  const [isVerifying, setIsVerifying] = useState(false);
  const [verificationCode, setVerificationCode] = useState('');
  const [status, setStatus] = useState('pending');
  const [error, setError] = useState('');
  
  const handleVerify = async () => {
    setIsVerifying(true);
    setError('');
    
    try {
      const token = localStorage.getItem("authToken") || sessionStorage.getItem("authToken");
      
      const response = await axios.post(
        `/api/transactions/verify-cash-payment/${booking.bookingId}`, 
        { verificationCode },
        { headers: { Authorization: `Bearer ${token}` } }
      );
      
      setStatus('completed');
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to verify payment. Please try again.');
    } finally {
      setIsVerifying(false);
    }
  };
  
  if (status === 'completed') {
    return (
      <div className="bg-green-50 p-4 rounded-lg border border-green-100">
        <div className="flex items-center">
          <div className="flex-shrink-0 bg-green-100 h-10 w-10 rounded-full flex items-center justify-center">
            <svg className="h-6 w-6 text-green-600" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M5 13l4 4L19 7" />
            </svg>
          </div>
          <div className="ml-3">
            <h3 className="text-green-800 font-medium">Payment Verified</h3>
            <p className="text-green-700 text-sm mt-1">Cash payment has been marked as received.</p>
          </div>
        </div>
      </div>
    );
  }
  
  return (
    <div className="bg-white p-4 rounded-lg border border-gray-200">
      <h3 className="font-medium text-gray-800 mb-3">Verify Cash Payment</h3>
      
      <div className="mb-4">
        <label className="block text-sm font-medium text-gray-700 mb-1">
          Payment Code
        </label>
        <input
          type="text"
          value={verificationCode}
          onChange={(e) => setVerificationCode(e.target.value)}
          placeholder="Enter payment code"
          className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-[#495E57]"
        />
      </div>
      
      {error && (
        <div className="mb-4 text-sm text-red-600 bg-red-50 p-2 rounded-md">
          {error}
        </div>
      )}
      
      <button
        onClick={handleVerify}
        disabled={!verificationCode || isVerifying}
        className={`w-full py-2 rounded-md ${
          !verificationCode || isVerifying 
            ? 'bg-gray-100 text-gray-400' 
            : 'bg-[#495E57] text-white hover:bg-[#3a4a44]'
        } transition-colors`}
      >
        {isVerifying ? 'Verifying...' : 'Mark as Paid'}
      </button>
    </div>
  );
};

export default CashPaymentVerification;
