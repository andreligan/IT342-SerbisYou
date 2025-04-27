import React, { useState } from 'react';

function MessageBubble({ message, isCurrentUser, onResend, isSelected, onMessageClick }) {
  const formatTime = (dateTime) => {
    if (!dateTime) return '';
    const date = new Date(dateTime);
    return date.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' });
  };

  // Handle message status
  const getStatusIcon = () => {
    switch (message.status) {
      case 'SENDING':
        return (
          <span className="inline-block ml-1">
            <div className="h-2 w-2 border-t-transparent border border-gray-400 rounded-full animate-spin"></div>
          </span>
        );
      case 'ERROR':
        return (
          <span className="inline-block ml-1 text-red-500 cursor-pointer" onClick={(e) => {
            e.stopPropagation();
            onResend && onResend();
          }}>
            <svg xmlns="http://www.w3.org/2000/svg" className="h-4 w-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z" />
            </svg>
          </span>
        );
      case 'DELIVERED':
        return (
          <span className="inline-block ml-1 text-blue-500">
            <svg xmlns="http://www.w3.org/2000/svg" className="h-4 w-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M5 13l4 4L19 7" />
            </svg>
          </span>
        );
      case 'READ':
        return (
          <span className="inline-block ml-1 text-blue-500">
            <svg xmlns="http://www.w3.org/2000/svg" className="h-4 w-4" viewBox="0 0 20 20" fill="currentColor">
              <path fillRule="evenodd" d="M6.267 3.455a3.066 3.066 0 001.745-.723 3.066 3.066 0 013.976 0 3.066 3.066 0 001.745.723 3.066 3.066 0 012.812 2.812c.051.643.304 1.254.723 1.745a3.066 3.066 0 010 3.976 3.066 3.066 0 00-.723 1.745 3.066 3.066 0 01-2.812 2.812 3.066 3.066 0 00-1.745.723 3.066 3.066 0 01-3.976 0 3.066 3.066 0 00-1.745-.723 3.066 3.066 0 01-2.812-2.812 3.066 3.066 0 00-.723-1.745 3.066 3.066 0 010-3.976 3.066 3.066 0 00.723-1.745 3.066 3.066 0 012.812-2.812zm7.44 5.252a1 1 0 00-1.414-1.414L9 10.586 7.707 9.293a1 1 0 00-1.414 1.414l2 2a1 1 0 001.414 0l4-4z" clipRule="evenodd" />
            </svg>
          </span>
        );
      default:
        return null;
    }
  };

  const handleClick = () => {
    onMessageClick && onMessageClick(message.messageId || message.id);
  };

  return (
    <div className={`flex flex-col ${isCurrentUser ? 'items-end' : 'items-start'} mb-3`}>
      <div 
        className={`max-w-xs lg:max-w-md rounded-lg py-2 px-3 cursor-pointer ${
          isCurrentUser 
            ? 'bg-[#F4CE14] text-[#495E57] rounded-br-none' 
            : 'bg-white border border-gray-200 rounded-bl-none'
        }`}
        onClick={handleClick}
      >
        <p className="text-sm">{message.messageText}</p>
      </div>
      
      {/* Time and status - only shown when selected */}
      {isSelected && (
        <div className="flex justify-between items-center bg-[#FEF9E7]">
          <div className={`flex items-center mt-1 text-xs ${isCurrentUser ? 'justify-end' : 'justify-start'}`}>
            <span className="text-gray-500">{formatTime(message.sentAt)}</span>
            {isCurrentUser && getStatusIcon()}
            
            {/* Retry button for failed messages - shown inline with time when selected */}
            {message.status === 'ERROR' && onResend && (
              <button 
                onClick={(e) => {
                  e.stopPropagation();
                  onResend(message.id, message.messageText);
                }}
                className="ml-2 text-red-500 p-1 rounded-full hover:bg-red-50"
                title="Retry sending message"
              >
                <svg xmlns="http://www.w3.org/2000/svg" className="h-4 w-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M4 4v5h.582m15.356 2A8.001 8.001 0 004.582 9m0 0H9m11 11v-5h-.581m0 0a8.003 8.003 0 01-15.357-2m15.357 2H15" />
                </svg>
              </button>
            )}
          </div>
        </div>
      )}
    </div>
  );
}

export default MessageBubble;