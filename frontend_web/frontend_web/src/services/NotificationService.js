import apiClient, { getApiUrl } from '../utils/apiConfig';
import ChatService from './ChatService';

const NotificationService = {
  // Get auth token and headers (reusing the pattern from ChatService)
  getAuthToken: () => {
    const token = localStorage.getItem('authToken') || sessionStorage.getItem('authToken');
    if (!token) {
      console.warn('No auth token found in storage');
    }
    return token;
  },
  
  getAuthHeaders: () => {
    // Note: We don't need to set Authorization headers as apiClient already does this
    return {
      headers: {
        'Content-Type': 'application/json'
      }
    };
  },
  
  // Create a new notification
  createNotification: async (notification) => {
    try {
      console.log("Creating notification with data:", notification);
      
      // Use apiClient instead of axios and getApiUrl for proper endpoint construction
      const response = await apiClient.post(getApiUrl('/notifications/create'), notification);
      console.log("Notification created response:", response.data);
      return response.data;
    } catch (error) {
      console.error('Failed to create notification:', error);
      if (error.response) {
        console.error('Response data:', error.response.data);
        console.error('Response status:', error.response.status);
      }
      throw error;
    }
  },
  
  // Get all notifications for current user
  getNotifications: async () => {
    try {
      // Use apiClient instead of axios
      const response = await apiClient.get(getApiUrl('/notifications/getAll'));
      
      // Extract the current user's ID from localStorage
      const currentUserId = localStorage.getItem('userId') || sessionStorage.getItem('userId');
      
      // Filter notifications to only show those for the current user
      if (currentUserId && response.data) {
        const userNotifications = response.data.filter(
          notification => notification.user && notification.user.userId === parseInt(currentUserId)
        );
        return userNotifications;
      }
      
      return response.data || [];
    } catch (error) {
      console.error('Error getting notifications:', error);
      return []; // Return empty array as fallback
    }
  },
  
  // Get unread notifications count (update to account for grouped messages)
  getUnreadCount: async () => {
    try {
      const notifications = await NotificationService.getNotifications();
      
      // Process notifications to group messages by sender
      const processedNotifications = NotificationService.processNotificationsForCount(notifications);
      
      return processedNotifications.filter(n => !n.read).length;
    } catch (error) {
      return 0;
    }
  },
  
  // Helper method to process notifications for unread count
  processNotificationsForCount: (notificationsList) => {
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
    
    // Combine with other notifications
    const combinedNotifications = [...latestMessages, ...otherNotifications];
    
    return combinedNotifications;
  },
  
  // Mark a notification as read
  markAsRead: async (notificationId, notificationData) => {
    try {
      // Since we don't have a GET endpoint for a single notification,
      // we'll use the notification data passed from the components
      const notification = notificationData;
      
      // If it's a message notification, update the message status too and find related messages
      if (notification && notification.type?.toLowerCase() === 'message' && notification.referenceId) {
        try {
          // Get all messages first
          const allMessagesResponse = await apiClient.get(getApiUrl('/messages/getAll'));
          const allMessages = allMessagesResponse.data || [];
          
          // Get all notifications in one call
          const allNotifications = await NotificationService.getNotifications();
          
          // Find the referenced message
          const targetMessage = allMessages.find(msg => msg.messageId === parseInt(notification.referenceId));
          
          if (targetMessage) {
            // Get all messages from the same sender to the current user
            const senderId = targetMessage.sender?.userId;
            const currentUserId = ChatService.getCurrentUserId();
            
            const messagesFromSender = allMessages.filter(msg => 
              msg.sender?.userId === senderId && 
              msg.receiver?.userId === currentUserId
            );
            
            // Filter unread messages (status = "SENT")
            const unreadMessages = messagesFromSender.filter(msg => msg.status === "SENT");
            
            // Mark all unread messages as READ
            for (const unreadMsg of unreadMessages) {
              try {
                await ChatService.markMessageAsRead(unreadMsg.messageId);
                
                // Find and handle the notification for this message
                const msgNotification = allNotifications.find(n => 
                  parseInt(n.referenceId) === unreadMsg.messageId && 
                  n.type?.toLowerCase() === 'message'
                );
                
                if (msgNotification) {
                  // Mark as read
                  const updatedNotification = { read: true };
                  await apiClient.put(
                    getApiUrl(`/notifications/update/${msgNotification.notificationId}`), 
                    updatedNotification
                  );
                  
                  // Delete notification - only for message notifications
                  await NotificationService.deleteNotification(msgNotification.notificationId);
                }
              } catch (updateError) {
                console.error(`Failed to update message ${unreadMsg.messageId}:`, updateError);
              }
            }
            
            return { success: true };
          }
        } catch (msgError) {
          console.error('Failed to update message status or find related messages:', msgError);
        }
      }
      
      // For non-message notifications, just mark as read without deleting
      if (notification && notification.type?.toLowerCase() !== 'message') {
        const updatedNotification = {
          read: true
        };
        
        const response = await apiClient.put(
          getApiUrl(`/notifications/update/${notificationId}`), 
          updatedNotification
        );
        return response.data;
      } else {
        // For message notifications that weren't handled above
        // Mark as read first
        const updatedNotification = { read: true };
        await apiClient.put(
          getApiUrl(`/notifications/update/${notificationId}`), 
          updatedNotification
        );
        
        // Delete the message notification after marking as read
        try {
          await NotificationService.deleteNotification(notificationId);
        } catch (deleteError) {
          console.error('Failed to delete message notification:', deleteError);
        }
      }
      
      return { success: true };
    } catch (error) {
      throw error;
    }
  },
  
  // Delete a notification
  deleteNotification: async (notificationId) => {
    try {
      const response = await apiClient.delete(getApiUrl(`/notifications/delete/${notificationId}`));
      return response.data;
    } catch (error) {
      throw error;
    }
  },
  
  // Mark all notifications as read
  markAllAsRead: async (visibleNotifications) => {
    try {
      // Fetch ALL notifications, not just the visible ones
      const allNotifications = await NotificationService.getNotifications();
      
      // Filter ALL unread notifications
      const allUnreadNotifications = allNotifications.filter(n => !n.read);
      
      // Process each notification individually to handle message-specific logic
      for (const notification of allUnreadNotifications) {
        // For message notifications, we need to update the message status
        if (notification.type?.toLowerCase() === 'message' && notification.referenceId) {
          try {
            // Update message status to READ
            await ChatService.markMessageAsRead(notification.referenceId);
            
            // Mark as read first
            const updatedNotification = { read: true };
            await apiClient.put(
              getApiUrl(`/notifications/update/${notification.notificationId}`), 
              updatedNotification
            );
            
            // Then delete it (only for message notifications)
            await NotificationService.deleteNotification(notification.notificationId);
          } catch (msgError) {
            // Continue even if message update fails
          }
        } else {
          // For non-message notifications, just mark as read without deleting
          try {
            const updatedNotification = { read: true };
            await apiClient.put(
              getApiUrl(`/notifications/update/${notification.notificationId}`),
              updatedNotification
            );
          } catch (notifError) {
            // Continue with next notification even if this one fails
          }
        }
      }
      
      return true;
    } catch (error) {
      throw error;
    }
  }
};

export default NotificationService;
