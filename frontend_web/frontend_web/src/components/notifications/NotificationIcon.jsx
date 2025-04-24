import React, { useState, useEffect } from 'react';
import NotificationDropdown from './NotificationDropdown';
import NotificationService from '../../services/NotificationService';

const NotificationIcon = () => {
  const [dropdownVisible, setDropdownVisible] = useState(false);
  const [unreadCount, setUnreadCount] = useState(0);

  // Fetch unread notifications count
  useEffect(() => {
    fetchUnreadCount();
    
    // Refresh count periodically
    const interval = setInterval(fetchUnreadCount, 60000); // Every minute
    
    return () => clearInterval(interval);
  }, []);
  
  const fetchUnreadCount = async () => {
    try {
      // Use the enhanced unread count method that groups messages by sender
      const count = await NotificationService.getUnreadCount();
      setUnreadCount(count);
    } catch (error) {
      console.error('Failed to fetch unread count:', error);
    }
  };

  const handleMouseEnter = () => {
    setDropdownVisible(true);
  };

  const handleMouseLeave = () => {
    setTimeout(() => {
      // Only hide if the mouse isn't over the dropdown
      if (!document.querySelector('.notification-dropdown:hover')) {
        setDropdownVisible(false);
      }
    }, 100);
  };
  
  const handleClose = () => {
    setDropdownVisible(false);
    // Refresh the unread count when dropdown closes
    fetchUnreadCount();
  };

  return (
    <div className="relative">
      <button
        className="p-2 rounded-full hover:bg-gray-200"
        onMouseEnter={handleMouseEnter}
        onMouseLeave={handleMouseLeave}
      >
        <div className="relative">
          <svg
            xmlns="http://www.w3.org/2000/svg"
            className="h-7 w-7"
            fill="none"
            viewBox="0 0 24 24"
            stroke="currentColor"
          >
            <path
              strokeLinecap="round"
              strokeLinejoin="round"
              strokeWidth={1.5} // Changed from 2 to 1.5 to make the icon thinner
              d="M15 17h5l-1.405-1.405A2.032 2.032 0 0118 14.158V11a6.002 6.002 0 00-4-5.659V4a2 2 0 10-4 0v1.341C7.67 6.165 6 8.388 6 11v3.159c0 .538-.214 1.055-.595 1.436L4 17h5m6 0a3 3 0 11-6 0m6 0H9"
            />
          </svg>
          
          {unreadCount > 0 && (
            <div className="absolute -top-1 -right-1 bg-red-500 text-white text-xs rounded-full h-5 w-5 flex items-center justify-center">
              {unreadCount > 9 ? '9+' : unreadCount}
            </div>
          )}
        </div>
      </button>

      {dropdownVisible && <NotificationDropdown isOpen={true} onClose={handleClose} />}
    </div>
  );
};

export default NotificationIcon;
