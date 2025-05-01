import React, { useState, useEffect, useRef } from 'react';
import { useNavigate } from 'react-router-dom';
import { motion, AnimatePresence } from 'framer-motion';
import apiClient, { getApiUrl } from '../../utils/apiConfig';

const NotificationIcon = ({ glowEffect = false }) => {
  const [unreadCount, setUnreadCount] = useState(0);
  const [isDropdownOpen, setIsDropdownOpen] = useState(false);
  const [notifications, setNotifications] = useState([]);
  const [loading, setLoading] = useState(false);
  const dropdownRef = useRef(null);
  const navigate = useNavigate();
  const pollingIntervalRef = useRef(null);
  const lastFetchTimeRef = useRef(0);
  
  // Poll for notifications but with optimizations:
  // 1. Only when the component is mounted
  // 2. With a reasonable interval (30 seconds instead of continuous)
  // 3. Skip fetching if the last fetch was recent (within 10 seconds)
  useEffect(() => {
    const fetchNotifications = async (force = false) => {
      // Skip if we've fetched recently (within 10 seconds) unless forced
      const now = Date.now();
      if (!force && now - lastFetchTimeRef.current < 10000) {
        return;
      }
      
      try {
        setLoading(true);
        const userId = localStorage.getItem('userId') || sessionStorage.getItem('userId');
        
        if (!userId) {
          console.log('No user ID found for fetching notifications');
          return;
        }
        
        const response = await apiClient.get(getApiUrl(`/notifications/getByUserId/${userId}`));
        
        if (response.data && Array.isArray(response.data)) {
          setNotifications(response.data);
          
          // Count unread notifications
          const unread = response.data.filter(n => !n.isRead).length;
          setUnreadCount(unread);
        }
        
        lastFetchTimeRef.current = now;
      } catch (error) {
        console.error("Error fetching notifications:", error);
      } finally {
        setLoading(false);
      }
    };

    // Initial fetch when component mounts
    fetchNotifications(true);
    
    // Set up polling interval (every 30 seconds)
    pollingIntervalRef.current = setInterval(() => {
      fetchNotifications();
    }, 30000);
    
    // Click outside handler
    const handleClickOutside = (event) => {
      if (dropdownRef.current && !dropdownRef.current.contains(event.target)) {
        setIsDropdownOpen(false);
      }
    };
    
    document.addEventListener('mousedown', handleClickOutside);
    
    // Clean up
    return () => {
      clearInterval(pollingIntervalRef.current);
      document.removeEventListener('mousedown', handleClickOutside);
    };
  }, []);

  const handleNotificationClick = async (notification) => {
    try {
      // Mark as read if it's not read already
      if (!notification.isRead) {
        await apiClient.put(getApiUrl(`/notifications/markAsRead/${notification.notificationId}`));
        
        // Update local state to reflect the change
        setNotifications(prevNotifications => 
          prevNotifications.map(n => 
            n.notificationId === notification.notificationId 
              ? { ...n, isRead: true } 
              : n
          )
        );
        
        setUnreadCount(prev => Math.max(0, prev - 1));
      }
      
      // Navigate to relevant page based on notification type
      if (notification.linkUrl) {
        navigate(notification.linkUrl);
      }
      
      setIsDropdownOpen(false);
    } catch (error) {
      console.error("Error marking notification as read:", error);
    }
  };

  const markAllAsRead = async () => {
    try {
      const userId = localStorage.getItem('userId') || sessionStorage.getItem('userId');
      await apiClient.put(getApiUrl(`/notifications/markAllAsRead/${userId}`));
      
      // Update local state
      setNotifications(prevNotifications => 
        prevNotifications.map(n => ({ ...n, isRead: true }))
      );
      
      setUnreadCount(0);
    } catch (error) {
      console.error("Error marking all notifications as read:", error);
    }
  };

  const viewAllNotifications = () => {
    navigate('/notifications');
    setIsDropdownOpen(false);
  };

  return (
    <div className="relative" ref={dropdownRef}>
      <button 
        className="relative"
        onClick={() => setIsDropdownOpen(!isDropdownOpen)}
        aria-label="Notifications"
      >
        {/* Bell icon with optional glow effect */}
        <motion.svg 
          xmlns="http://www.w3.org/2000/svg" 
          className={`h-6 w-6 text-[#495E57] hover:text-[#F4CE14] transition-colors ${
            glowEffect && unreadCount > 0 ? 'drop-shadow-[0_0_8px_rgba(244,206,20,0.7)]' : ''
          }`}
          fill="none" 
          viewBox="0 0 24 24" 
          stroke="currentColor"
          animate={unreadCount > 0 ? { scale: [1, 1.1, 1] } : {}}
          transition={{ repeat: unreadCount > 0 ? Infinity : 0, duration: 1.5, repeatType: "reverse" }}
        >
          <path 
            strokeLinecap="round" 
            strokeLinejoin="round" 
            strokeWidth={2} 
            d="M15 17h5l-1.405-1.405A2.032 2.032 0 0118 14.158V11a6.002 6.002 0 00-4-5.659V5a2 2 0 10-4 0v.341C7.67 6.165 6 8.388 6 11v3.159c0 .538-.214 1.055-.595 1.436L4 17h5m6 0v1a3 3 0 11-6 0v-1m6 0H9" 
          />
        </motion.svg>
        
        {/* Notification badge */}
        {unreadCount > 0 && (
          <motion.span 
            initial={{ scale: 0 }}
            animate={{ scale: 1 }}
            className="absolute -top-2 -right-2 bg-red-500 text-white text-xs rounded-full h-5 w-5 flex items-center justify-center font-bold"
          >
            {unreadCount > 9 ? '9+' : unreadCount}
          </motion.span>
        )}
      </button>
      
      {/* Dropdown menu */}
      <AnimatePresence>
        {isDropdownOpen && (
          <motion.div 
            className="absolute right-0 mt-2 w-80 max-h-96 bg-white rounded-lg shadow-xl border border-gray-200 overflow-hidden z-50"
            initial={{ opacity: 0, y: -10, scale: 0.95 }}
            animate={{ opacity: 1, y: 0, scale: 1 }}
            exit={{ opacity: 0, y: -10, scale: 0.95 }}
            transition={{ type: "spring", duration: 0.3 }}
          >
            <div className="flex items-center justify-between border-b border-gray-100 px-4 py-3 bg-gray-50">
              <h3 className="font-medium text-gray-800">Notifications</h3>
              {unreadCount > 0 && (
                <button 
                  onClick={markAllAsRead}
                  className="text-sm text-blue-600 hover:text-blue-800"
                >
                  Mark all as read
                </button>
              )}
            </div>
            
            <div className="overflow-y-auto max-h-72">
              {loading && notifications.length === 0 ? (
                <div className="flex justify-center items-center p-4">
                  <div className="w-5 h-5 border-2 border-gray-300 border-t-[#F4CE14] rounded-full animate-spin"></div>
                </div>
              ) : notifications.length > 0 ? (
                notifications.slice(0, 5).map((notification) => (
                  <div 
                    key={notification.notificationId} 
                    className={`border-b border-gray-100 p-4 cursor-pointer hover:bg-gray-50 transition-colors ${!notification.isRead ? 'bg-blue-50' : ''}`}
                    onClick={() => handleNotificationClick(notification)}
                  >
                    <div className="flex items-start">
                      <div className={`h-2 w-2 mt-2 rounded-full flex-shrink-0 ${!notification.isRead ? 'bg-blue-500' : 'bg-gray-300'}`}></div>
                      <div className="ml-3 flex-1">
                        <p className={`text-sm ${!notification.isRead ? 'font-medium' : 'text-gray-800'}`}>
                          {notification.content}
                        </p>
                        <p className="text-xs text-gray-500 mt-1">
                          {new Date(notification.createdAt).toLocaleString()}
                        </p>
                      </div>
                    </div>
                  </div>
                ))
              ) : (
                <div className="py-8 px-4 text-center text-gray-500">
                  <svg xmlns="http://www.w3.org/2000/svg" className="h-10 w-10 mx-auto text-gray-300 mb-3" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={1.5} d="M15 17h5l-1.405-1.405A2.032 2.032 0 0118 14.158V11a6.002 6.002 0 00-4-5.659V5a2 2 0 10-4 0v.341C7.67 6.165 6 8.388 6 11v3.159c0 .538-.214 1.055-.595 1.436L4 17h5m6 0v1a3 3 0 11-6 0v-1m6 0H9" />
                  </svg>
                  <p>No notifications yet</p>
                </div>
              )}
            </div>
            
            <div className="border-t border-gray-100 p-2">
              <button 
                onClick={viewAllNotifications}
                className="w-full py-2 px-4 text-center text-sm text-[#495E57] hover:bg-gray-50 rounded-md transition-colors"
              >
                View all notifications
              </button>
            </div>
          </motion.div>
        )}
      </AnimatePresence>
    </div>
  );
};

export default NotificationIcon;
