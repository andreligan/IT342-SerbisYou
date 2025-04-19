import React, { useState, useRef, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import NotificationItem from './NotificationItem';
import NotificationService from '../../services/NotificationService';

const NotificationDropdown = ({ isOpen, onClose }) => {
  const [notifications, setNotifications] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);
  const navigate = useNavigate();
  const notificationsListRef = useRef(null);
  
  // Fetch notifications when dropdown is opened
  useEffect(() => {
    if (isOpen) {
      fetchNotifications();
    }
  }, [isOpen]);
  
  const fetchNotifications = async () => {
    try {
      setLoading(true);
      setError(null);
      const data = await NotificationService.getNotifications();
      // Sort by created date (newest first)
      const sortedNotifications = data.sort((a, b) => 
        new Date(b.createdAt) - new Date(a.createdAt)
      );
      setNotifications(sortedNotifications);
    } catch (error) {
      console.error('Failed to fetch notifications:', error);
      setError('Failed to load notifications');
    } finally {
      setLoading(false);
    }
  };
  
  // Handle scroll within the notification list
  useEffect(() => {
    const handleWheel = (event) => {
      const container = notificationsListRef.current;
      if (!container) return;
      
      // Check if we're at the top or bottom boundary of the scrollable area
      const { scrollTop, scrollHeight, clientHeight } = container;
      const isAtTop = scrollTop === 0;
      const isAtBottom = scrollTop + clientHeight >= scrollHeight - 1; // -1 for potential rounding issues
      
      // If at top boundary and scrolling up, or at bottom boundary and scrolling down,
      // prevent the parent from scrolling
      if ((isAtTop && event.deltaY < 0) || (isAtBottom && event.deltaY > 0)) {
        event.preventDefault();
        event.stopPropagation();
      }
    };
    
    const container = notificationsListRef.current;
    if (container) {
      container.addEventListener('wheel', handleWheel, { passive: false });
    }
    
    return () => {
      if (container) {
        container.removeEventListener('wheel', handleWheel);
      }
    };
  }, [isOpen]);

  const markAllAsRead = async () => {
    try {
      const unreadNotifications = notifications.filter(n => !n.read);
      
      // Update each unread notification
      await Promise.all(unreadNotifications.map(notification => 
        NotificationService.markAsRead(notification.notificationId)
      ));
      
      // Update local state
      setNotifications(notifications.map(n => ({ ...n, read: true })));
    } catch (error) {
      console.error('Failed to mark notifications as read:', error);
    }
  };
  
  const markAsRead = async (notificationId) => {
    try {
      await NotificationService.markAsRead(notificationId);
      setNotifications(notifications.map(n => 
        n.notificationId === notificationId ? { ...n, read: true } : n
      ));
    } catch (error) {
      console.error(`Failed to mark notification ${notificationId} as read:`, error);
    }
  };

  const unreadCount = notifications.filter(n => !n.read).length;

  const viewAllNotifications = () => {
    onClose();
    navigate('/notifications');
  };

  if (!isOpen) return null;

  return (
    <div 
      className="absolute right-0 mt-2 w-80 bg-white border border-gray-300 rounded-lg shadow-lg z-50 overflow-hidden notification-dropdown"
      onMouseEnter={() => {}} // Keep dropdown open when mouse enters
      onMouseLeave={onClose}  // Close when mouse leaves the dropdown
    >
      <div className="p-4 border-b border-gray-200 flex justify-between items-center">
        <div>
          <h3 className="text-lg font-medium text-gray-800">Notifications</h3>
          <p className="text-xs text-gray-500">{unreadCount} unread</p>
        </div>
        {unreadCount > 0 && (
          <button 
            onClick={markAllAsRead}
            className="text-xs font-medium text-blue-500 hover:text-blue-700"
          >
            Mark all as read
          </button>
        )}
      </div>
      
      <div 
        ref={notificationsListRef}
        className="max-h-96 overflow-y-auto"
      >
        {loading ? (
          <div className="p-4 text-center text-gray-500">
            Loading notifications...
          </div>
        ) : error ? (
          <div className="p-4 text-center text-red-500">
            {error}
          </div>
        ) : notifications.length > 0 ? (
          notifications.map(notification => (
            <NotificationItem 
              key={notification.notificationId} 
              notification={{
                id: notification.notificationId,
                type: notification.type?.toLowerCase() || 'system',
                message: notification.message,
                timestamp: notification.createdAt,
                read: notification.read,
                referenceId: notification.referenceId,
                referenceType: notification.referenceType
              }}
              onMarkAsRead={() => markAsRead(notification.notificationId)}
            />
          ))
        ) : (
          <div className="p-4 text-center text-gray-500">
            No notifications
          </div>
        )}
      </div>

      <div className="p-3 border-t border-gray-200 bg-gray-50 text-center">
        <button 
          onClick={viewAllNotifications}
          className="text-sm font-medium text-blue-500 hover:text-blue-700"
        >
          View all notifications
        </button>
      </div>
    </div>
  );
};

export default NotificationDropdown;
