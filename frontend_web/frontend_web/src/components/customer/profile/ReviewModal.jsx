import React, { useState, useEffect } from 'react';

const ReviewModal = ({ booking, onClose, onSubmit }) => {
  const [rating, setRating] = useState(0);
  const [comment, setComment] = useState('');
  const [hoveredRating, setHoveredRating] = useState(0);
  const [errors, setErrors] = useState({});
  
  const handleRatingClick = (value) => {
    setRating(value);
  };
  
  const handleSubmit = () => {
    const newErrors = {};
    
    if (rating === 0) {
      newErrors.rating = "Please select a rating";
    }
    
    if (!comment.trim()) {
      newErrors.comment = "Please provide a comment";
    }
    
    if (Object.keys(newErrors).length > 0) {
      setErrors(newErrors);
      return;
    }
    
    onSubmit({ rating, comment });
  };
  
  // Prevent background scrolling when modal is open
  useEffect(() => {
    document.body.style.overflow = 'hidden';
    return () => {
      document.body.style.overflow = 'auto';
    };
  }, []);
  
  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4">
      {/* Backdrop */}
      <div className="absolute inset-0 bg-opacity-50" onClick={onClose}></div>
      
      {/* Modal */}
      <div className="bg-white rounded-xl shadow-2xl w-full max-w-lg z-10 relative overflow-hidden transform transition-all">
        {/* Header */}
        <div className="bg-gradient-to-r from-[#445954] to-[#495E57] text-white px-6 py-4 flex items-center justify-between">
          <h3 className="text-xl font-bold">Write a Review</h3>
          <button onClick={onClose} className="text-white hover:text-[#F4CE14] transition-colors">
            <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M6 18L18 6M6 6l12 12"></path>
            </svg>
          </button>
        </div>
        
        {/* Content */}
        <div className="p-6">
          {/* Service Info */}
          <div className="mb-6 pb-4 border-b border-gray-200">
            <p className="text-sm text-gray-500">Service</p>
            <p className="font-semibold text-[#495E57]">{booking?.service?.serviceName}</p>
            <div className="flex items-center mt-2">
              <div className="h-8 w-8 rounded-full bg-[#495E57]/10 flex items-center justify-center mr-2">
                <svg className="w-4 h-4 text-[#495E57]" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z"></path>
                </svg>
              </div>
              <span className="text-sm text-gray-600">
                {booking?.service?.provider?.firstName} {booking?.service?.provider?.lastName}
              </span>
            </div>
          </div>
          
          {/* Rating Section */}
          <div className="mb-6">
            <label className="block text-gray-700 font-medium mb-2">Your Rating</label>
            <div className="flex">
              {[1, 2, 3, 4, 5].map((value) => (
                <button
                  key={value}
                  type="button"
                  onMouseEnter={() => setHoveredRating(value)}
                  onMouseLeave={() => setHoveredRating(0)}
                  onClick={() => handleRatingClick(value)}
                  className="text-3xl p-1 focus:outline-none"
                >
                  <span className={`${(hoveredRating || rating) >= value ? 'text-yellow-400' : 'text-gray-300'}`}>★</span>
                </button>
              ))}
            </div>
            {errors.rating && <p className="text-xs text-red-500 mt-1">{errors.rating}</p>}
          </div>
          
          {/* Comment Section */}
          <div className="mb-6">
            <label htmlFor="comment" className="block text-gray-700 font-medium mb-2">Your Review</label>
            <textarea
              id="comment"
              className={`w-full border ${errors.comment ? 'border-red-500' : 'border-gray-300'} rounded-lg p-3 focus:outline-none focus:ring-2 focus:ring-[#F4CE14] focus:border-transparent`}
              rows="4"
              placeholder="Share your experience with this service..."
              value={comment}
              onChange={(e) => setComment(e.target.value)}
            ></textarea>
            {errors.comment && <p className="text-xs text-red-500 mt-1">{errors.comment}</p>}
          </div>
          
          {/* Buttons */}
          <div className="flex justify-end gap-3">
            <button
              type="button"
              onClick={onClose}
              className="px-4 py-2 bg-gray-100 text-gray-700 rounded-lg hover:bg-gray-200 transition-colors"
            >
              Cancel
            </button>
            <button
              type="button"
              onClick={handleSubmit}
              className="px-4 py-2 bg-[#F4CE14] text-[#495E57] rounded-lg hover:bg-[#f3d028] transition-colors font-medium"
            >
              Submit Review
            </button>
          </div>
        </div>
      </div>
    </div>
  );
};

export default ReviewModal;
