import React from 'react';
import { useNavigate } from 'react-router-dom';

const NotificationItem = ({ notification, onMarkAsRead }) => {
  const navigate = useNavigate();

  const getTypeIcon = (type) => {
    switch (type) {
      case 'message':
        return (
          <div className="h-8 w-8 rounded-full bg-blue-100 flex items-center justify-center">
            <svg xmlns="http://www.w3.org/2000/svg" className="h-4 w-4 text-blue-500" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M8 10h.01M12 10h.01M16 10h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 8v4l2 2" />
            </svg>
          </div>
        );
      case 'booking':
        return (
          <div className="h-8 w-8 rounded-full bg-green-100 flex items-center justify-center">
            <svg xmlns="http://www.w3.org/2000/svg" className="h-4 w-4 text-green-500" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z" />
            </svg>
          </div>
        );
      case 'alert':
      case 'review':
        return (
          <div className="h-8 w-8 rounded-full bg-red-100 flex items-center justify-center">
            <svg xmlns="http://www.w3.org/2000/svg" className="h-4 w-4 text-red-500" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z" />
            </svg>
          </div>
        );
      case 'transaction':
        return (
          <div className="h-8 w-8 rounded-full bg-purple-100 flex items-center justify-center">
            <svg xmlns="http://www.w3.org/2000/svg" className="h-4 w-4 text-purple-500" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 8c-1.657 0-3 .895-3 2s1.343 2 3 2 3 .895 3 2-1.343 2-3 2m0-8c1.11 0 2.08.402 2.599 1M12 8V7m0 1v8m0 0v1m0-1c-1.11 0-2.08-.402-2.599-1M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
            </svg>
          </div>
        );
      default:
        return (
          <div className="h-8 w-8 rounded-full bg-gray-100 flex items-center justify-center">
            <svg xmlns="http://www.w3.org/2000/svg" className="h-4 w-4 text-gray-500" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M13 16h-1v-4h-1m1-4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
            </svg>
          </div>
        );
    }
  };

  const formatTimeAgo = (timestamp) => {
    if (!timestamp) return '';
    
    const now = new Date();
    const notificationTime = new Date(timestamp);
    const diffInSeconds = Math.floor((now - notificationTime) / 1000);
    
    if (diffInSeconds < 60) return `${diffInSeconds} sec ago`;
    if (diffInSeconds < 3600) return `${Math.floor(diffInSeconds / 60)} min ago`;
    if (diffInSeconds < 86400) return `${Math.floor(diffInSeconds / 3600)} hr ago`;
    return `${Math.floor(diffInSeconds / 86400)} days ago`;
  };

  const handleClick = () => {
    // If notification is not read, mark it as read
    if (!notification.read && onMarkAsRead) {
      onMarkAsRead(notification.id);
    }
    
    // Handle navigation based on notification type
    if (notification.type === 'message' && notification.referenceId) {
      // For message notifications, extract sender info and open chat
      const messageContent = notification.message || '';
      const senderMatch = messageContent.match(/^([^:]+) sent you a message/);
      const senderName = senderMatch ? senderMatch[1] : 'Unknown';
      
      // Trigger the chat to open with this sender
      // We'll store the sender info in localStorage for the ChatWindow to pick up
      localStorage.setItem('pendingChatUser', JSON.stringify({
        name: senderName,
        referenceId: notification.referenceId,
        messageId: notification.id
      }));
      
      // Dispatch a custom event to notify components that need to open the chat
      window.dispatchEvent(new CustomEvent('openChatWithUser'));
      
      // If we're on the notifications page, navigate to home to ensure the chat icon is visible
      if (window.location.pathname === '/notifications') {
        const userRole = localStorage.getItem('userRole') || sessionStorage.getItem('userRole');
        if (userRole?.toLowerCase() === 'customer') {
          navigate('/customerHomePage');
        } else if (userRole?.toLowerCase() === 'service provider') {
          navigate('/serviceProviderHomePage');
        }
      }
    } else if (notification.type === 'booking' && notification.referenceId) {
      // For booking notifications, navigate to the booking details page
      navigate(`/booking-details/${notification.referenceId}`);
    } else if (notification.type === 'transaction' && notification.referenceId) {
      // For transaction notifications, you could navigate to a transaction page
      // navigate(`/transaction/${notification.referenceId}`);
    }
  };

  return (
    <div 
      className={`flex items-start p-3 border-b border-gray-100 hover:bg-gray-50 cursor-pointer ${!notification.read ? 'bg-blue-50' : ''}`}
      onClick={handleClick}
    >
      <div className="mr-3">
        {getTypeIcon(notification.type)}
      </div>
      <div className="flex-1 min-w-0">
        <p className={`text-sm font-medium ${!notification.read ? 'text-blue-600' : 'text-gray-900'}`}>
          {notification.message}
        </p>
        <p className="text-xs text-gray-500 mt-1">{formatTimeAgo(notification.timestamp)}</p>
        {notification.actionLabel && (
          <button 
            className="mt-2 text-xs font-medium text-blue-500 hover:text-blue-700"
            onClick={(e) => {
              e.stopPropagation(); // Prevent triggering parent's onClick
              // Add action handling here
            }}
          >
            {notification.actionLabel}
          </button>
        )}
      </div>
      {!notification.read && (
        <div className="h-2 w-2 bg-blue-500 rounded-full"></div>
      )}
    </div>
  );
};

export default NotificationItem;
