import React, { useState, useRef, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { motion, AnimatePresence } from 'framer-motion';
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

      // Process notifications to group messages by sender
      const processedNotifications = processNotifications(data);

      setNotifications(processedNotifications);
    } catch (error) {
      console.error('Failed to fetch notifications:', error);
      setError('Failed to load notifications');
    } finally {
      setLoading(false);
    }
  };

  // Process notifications to group messages by sender
  const processNotifications = (notificationsList) => {
    // First, separate message notifications from other types
    const messageNotifications = notificationsList.filter(
      notification => notification.type?.toLowerCase() === 'message'
    );
    const otherNotifications = notificationsList.filter(
      notification => notification.type?.toLowerCase() !== 'message'
    );

    // Group message notifications by senderId (using referenceId as the key)
    const messageGroups = {};

    messageNotifications.forEach(notification => {
      // Extract sender ID from message (assuming the format includes sender information)
      // This might need adjustment based on your actual data structure
      const messageContent = notification.message || '';
      const senderMatch = messageContent.match(/^([^:]+) sent you a message/);
      const senderName = senderMatch ? senderMatch[1] : 'Unknown';

      if (!messageGroups[senderName]) {
        messageGroups[senderName] = [];
      }
      messageGroups[senderName].push(notification);
    });

    // For each sender, only keep the most recent message
    const latestMessages = Object.values(messageGroups).map(group => {
      // Sort by timestamp (newest first)
      group.sort((a, b) => new Date(b.createdAt) - new Date(a.createdAt));
      return group[0]; // Return only the most recent
    });

    // Combine with other notifications and sort by timestamp (newest first)
    const combinedNotifications = [...latestMessages, ...otherNotifications];
    combinedNotifications.sort((a, b) => new Date(b.createdAt) - new Date(a.createdAt));

    return combinedNotifications;
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
      // Show loading state
      setLoading(true);

      // Call the enhanced markAllAsRead method that handles all notifications
      await NotificationService.markAllAsRead();

      // After marking all as read, refresh the list
      fetchNotifications();
    } catch (error) {
      console.error('Failed to mark notifications as read:', error);
      setError('Failed to mark all notifications as read');
    } finally {
      setLoading(false);
    }
  };

  const markAsRead = async (notificationId, notificationData) => {
    try {
      // Pass both ID and notification data
      await NotificationService.markAsRead(notificationId, notificationData);

      // After marking as read (and possibly deleting), refresh the list
      fetchNotifications();
    } catch (error) {
      console.error(`Failed to mark notification ${notificationId} as read:`, error);
    }
  };

  const unreadCount = notifications.filter(n => !n.read).length;

  const viewAllNotifications = () => {
    onClose();
    navigate('/notifications');
  };

  // Animation variants - simplified to only animate the container
  const dropdownVariants = {
    hidden: { opacity: 0, y: -20, scale: 0.95 },
    visible: { 
      opacity: 1, 
      y: 0, 
      scale: 1,
      transition: {
        type: "spring",
        damping: 25,
        stiffness: 300
      }
    },
    exit: { 
      opacity: 0, 
      y: -10, 
      scale: 0.95,
      transition: { 
        duration: 0.2,
        ease: "easeOut" 
      }
    }
  };

  if (!isOpen) return null;

  return (
    <AnimatePresence>
      {isOpen && (
        <motion.div 
          className="absolute right-0 mt-2 w-80 bg-white border border-gray-300 rounded-lg shadow-lg z-50 overflow-hidden notification-dropdown"
          variants={dropdownVariants}
          initial="hidden"
          animate="visible"
          exit="exit"
          onMouseEnter={() => {}} // Keep dropdown open when mouse enters
          onMouseLeave={onClose}  // Close when mouse leaves the dropdown
        >
          {/* Regular non-animated content */}
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
                <div className="inline-block h-6 w-6 border-2 border-t-transparent border-gray-300 rounded-full animate-spin"></div>
                <p className="mt-2">Loading notifications...</p>
              </div>
            ) : error ? (
              <div className="p-4 text-center text-red-500">
                {error}
              </div>
            ) : notifications.length > 0 ? (
              notifications.map((notification) => (
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
                  onMarkAsRead={() => markAsRead(notification.notificationId, notification)}
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
        </motion.div>
      )}
    </AnimatePresence>
  );
};

export default NotificationDropdown;
