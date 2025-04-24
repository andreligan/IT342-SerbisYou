import React from 'react';
import { useNavigate } from 'react-router-dom';

const PaymentCancelPage = () => {
  const navigate = useNavigate();
  
  // Clean up the pending booking data
  React.useEffect(() => {
    localStorage.removeItem('pendingBooking');
  }, []);

  return (
    <div className="min-h-screen flex items-center justify-center bg-gray-50">
      <div className="bg-white p-8 rounded-lg shadow-lg max-w-md w-full text-center">
        <div className="text-yellow-500 text-5xl mb-4">
          <i className="fas fa-times-circle"></i>
        </div>
        <h2 className="text-2xl font-bold text-gray-800 mb-2">Payment Cancelled</h2>
        <p className="text-gray-600 mb-6">Your payment process has been cancelled. No charges were made.</p>
        
        <div className="flex flex-col space-y-4">
          <button 
            onClick={() => navigate('/bookService')} 
            className="bg-[#495E57] text-white py-2 px-6 rounded-lg hover:bg-[#3A4A47] transition"
          >
            Try Again
          </button>
          
          <button 
            onClick={() => navigate('/customerHomePage')} 
            className="border border-[#495E57] text-[#495E57] py-2 px-6 rounded-lg hover:bg-gray-50 transition"
          >
            Go to Home
          </button>
        </div>
      </div>
    </div>
  );
};

export default PaymentCancelPage;
