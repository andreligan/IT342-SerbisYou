import React, { useState, useEffect } from 'react';
import NotificationDropdown from './NotificationDropdown';
import NotificationService from '../../services/NotificationService';

const NotificationIcon = ({ glowEffect }) => {
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
        className="flex items-center justify-center"
        onMouseEnter={handleMouseEnter}
        onMouseLeave={handleMouseLeave}
        aria-label="Notifications"
      >
        <div className="relative">
          <svg 
            xmlns="http://www.w3.org/2000/svg" 
            viewBox="0 0 24 24" 
            fill="none" 
            stroke="currentColor" 
            className="h-6 w-6 text-[#495E57] hover:text-[#F4CE14] transition-colors duration-200"
            strokeWidth="2" 
            strokeLinecap="round" 
            strokeLinejoin="round"
          >
            <path d="M18 8A6 6 0 0 0 6 8c0 7-3 9-3 9h18s-3-2-3-9"></path>
            <path d="M13.73 21a2 2 0 0 1-3.46 0"></path>
          </svg>
          
          {unreadCount > 0 && (
            <div className="absolute -top-1 -right-1 bg-red-500 text-white text-xs rounded-full h-5 w-5 flex items-center justify-center border border-white shadow-sm">
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
