import React, { useRef } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import QRCode from 'react-qr-code';
import html2canvas from 'html2canvas';

const CashPaymentConfirmation = ({ bookingDetails }) => {
  const receiptRef = useRef(null);
  const navigate = useNavigate();
  const paymentCode = `SY-${bookingDetails?.bookingId}-${Date.now().toString().slice(-6)}`;
  
  const saveReceipt = async () => {
    if (receiptRef.current) {
      const canvas = await html2canvas(receiptRef.current);
      const imgData = canvas.toDataURL('image/png');
      const link = document.createElement('a');
      link.href = imgData;
      link.download = `receipt-${bookingDetails?.bookingId}.png`;
      link.click();
    }
  };
  
  return (
    <div className="max-w-lg mx-auto p-6">
      <div className="bg-white rounded-xl shadow-lg overflow-hidden" ref={receiptRef}>
        <div className="bg-[#495E57] text-white p-6">
          <h2 className="text-2xl font-bold">Payment Receipt</h2>
          <p className="text-white/80">Cash Payment - Show to service provider</p>
        </div>
        
        <div className="p-6">
          <div className="flex justify-center mb-6">
            <QRCode value={paymentCode} size={150} />
          </div>
          
          <div className="text-center mb-6">
            <h3 className="text-xl font-bold text-[#495E57]">Payment Code</h3>
            <p className="text-2xl font-mono mt-2 bg-gray-50 py-2 rounded-lg">{paymentCode}</p>
          </div>
          
          <div className="border-t border-dashed border-gray-200 pt-4 mt-4">
            <div className="flex justify-between mb-2">
              <span className="text-gray-600">Service:</span>
              <span className="font-medium">{bookingDetails?.service?.serviceName}</span>
            </div>
            <div className="flex justify-between mb-2">
              <span className="text-gray-600">Amount Due:</span>
              <span className="font-medium">₱{bookingDetails?.totalCost.toLocaleString('en-PH')}</span>
            </div>
            <div className="flex justify-between">
              <span className="text-gray-600">Date:</span>
              <span className="font-medium">{new Date().toLocaleDateString()}</span>
            </div>
          </div>
          
          <div className="mt-6 text-center text-sm text-gray-500">
            <p>Present this receipt to your service provider to confirm payment</p>
          </div>
        </div>
      </div>
      
      <div className="mt-6 flex flex-col space-y-3">
        <button 
          onClick={saveReceipt}
          className="bg-[#495E57] text-white py-3 rounded-lg flex items-center justify-center"
        >
          <svg xmlns="http://www.w3.org/2000/svg" className="h-5 w-5 mr-2" viewBox="0 0 20 20" fill="currentColor">
            <path fillRule="evenodd" d="M3 17a1 1 0 011-1h12a1 1 0 110 2H4a1 1 0 01-1-1zm3.293-7.707a1 1 0 011.414 0L9 10.586V3a1 1 0 112 0v7.586l1.293-1.293a1 1 0 111.414 1.414l-3 3a1 1 0 01-1.414 0l-3-3a1 1 0 010-1.414z" clipRule="evenodd" />
          </svg>
          Save Receipt
        </button>
        
        <button 
          onClick={() => navigate('/customerProfile/bookingHistory')}
          className="bg-gray-100 text-gray-700 py-3 rounded-lg"
        >
          View Bookings
        </button>
      </div>
    </div>
  );
};

export default CashPaymentConfirmation;
