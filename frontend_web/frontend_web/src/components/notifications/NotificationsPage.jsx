import React, { useState, useRef, useEffect } from 'react';
import NotificationItem from './NotificationItem';
import NotificationService from '../../services/NotificationService';

const NotificationsPage = () => {
  const [notifications, setNotifications] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [filter, setFilter] = useState('all');
  const notificationsListRef = useRef(null);
  
  useEffect(() => {
    fetchNotifications();
  }, []);
  
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
      setError('Failed to load notifications. Please try again later.');
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
    
    // Group message notifications by sender
    const messageGroups = {};
    
    messageNotifications.forEach(notification => {
      // Extract sender info from message content
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
  }, []);

  const markAllAsRead = async () => {
    try {
      // Pass the full notifications array
      await NotificationService.markAllAsRead(notifications);
      
      // Refresh notifications list
      fetchNotifications();
    } catch (error) {
      console.error('Failed to mark notifications as read:', error);
    }
  };
  
  const markAsRead = async (notificationId, notificationData) => {
    try {
      // Pass both ID and notification data
      await NotificationService.markAsRead(notificationId, notificationData);
      
      // Refresh notifications list after marking as read (and possibly deleting)
      fetchNotifications();
    } catch (error) {
      console.error(`Failed to mark notification ${notificationId} as read:`, error);
    }
  };

  const filteredNotifications = notifications.filter(notif => {
    if (filter === 'all') return true;
    if (filter === 'unread') return !notif.read;
    return notif.type?.toLowerCase() === filter.toLowerCase();
  });

  return (
    <div className="container mx-auto px-4 py-8 max-w-4xl">
      <div className="flex justify-between items-center mb-6">
        <h1 className="text-2xl font-bold text-gray-800">Notifications</h1>
        {notifications.some(n => !n.read) && (
          <button 
            onClick={markAllAsRead}
            className="px-4 py-2 bg-blue-500 text-white rounded-md hover:bg-blue-600"
          >
            Mark all as read
          </button>
        )}
      </div>

      <div className="mb-6 border-b border-gray-200">
        <div className="flex space-x-4">
          <button 
            className={`pb-2 px-1 ${filter === 'all' ? 'border-b-2 border-blue-500 text-blue-500' : 'text-gray-500'}`}
            onClick={() => setFilter('all')}
          >
            All
          </button>
          <button 
            className={`pb-2 px-1 ${filter === 'unread' ? 'border-b-2 border-blue-500 text-blue-500' : 'text-gray-500'}`}
            onClick={() => setFilter('unread')}
          >
            Unread
          </button>
          <button 
            className={`pb-2 px-1 ${filter === 'message' ? 'border-b-2 border-blue-500 text-blue-500' : 'text-gray-500'}`}
            onClick={() => setFilter('message')}
          >
            Messages
          </button>
          <button 
            className={`pb-2 px-1 ${filter === 'booking' ? 'border-b-2 border-blue-500 text-blue-500' : 'text-gray-500'}`}
            onClick={() => setFilter('booking')}
          >
            Bookings
          </button>
          <button 
            className={`pb-2 px-1 ${filter === 'transaction' ? 'border-b-2 border-blue-500 text-blue-500' : 'text-gray-500'}`}
            onClick={() => setFilter('transaction')}
          >
            Payments
          </button>
          <button 
            className={`pb-2 px-1 ${filter === 'review' ? 'border-b-2 border-blue-500 text-blue-500' : 'text-gray-500'}`}
            onClick={() => setFilter('review')}
          >
            Reviews
          </button>
        </div>
      </div>

      <div 
        ref={notificationsListRef}
        className="bg-white shadow rounded-lg overflow-y-auto max-h-[70vh]"
      >
        {loading ? (
          <div className="p-8 text-center text-gray-500">
            Loading notifications...
          </div>
        ) : error ? (
          <div className="p-8 text-center text-red-500">
            {error}
          </div>
        ) : filteredNotifications.length > 0 ? (
          filteredNotifications.map(notification => (
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
          <div className="p-8 text-center text-gray-500">
            No notifications to display
          </div>
        )}
      </div>
    </div>
  );
};

export default NotificationsPage;
