import axios from 'axios';

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
    const token = NotificationService.getAuthToken();
    if (!token) return {};
    
    return {
      headers: {
        'Authorization': `Bearer ${token}`,
        'Content-Type': 'application/json'
      }
    };
  },
  
  // Create a new notification
  createNotification: async (notification) => {
    try {
      console.log('NotificationService: Creating notification:', notification);
      const authHeaders = NotificationService.getAuthHeaders();
      console.log('NotificationService: Using auth headers:', 
        authHeaders.headers ? 'Headers present' : 'No headers');
      
      console.log('NotificationService: Sending POST request to /api/notifications/create');
      const response = await axios.post('/api/notifications/create', notification, authHeaders);
      console.log('NotificationService: Notification created successfully:', response.data);
      return response.data;
    } catch (error) {
      console.error('NotificationService: Failed to create notification:', error);
      console.error('NotificationService: Error details:', 
        error.response ? {
          status: error.response.status,
          data: error.response.data
        } : 'No response data');
      throw error;
    }
  },
  
  // Get all notifications for current user
  getNotifications: async () => {
    try {
      const authHeaders = NotificationService.getAuthHeaders();
      const response = await axios.get('/api/notifications/getAll', authHeaders);
      return response.data;
    } catch (error) {
      console.error('Failed to fetch notifications:', error);
      throw error;
    }
  },
  
  // Mark a notification as read
  markAsRead: async (notificationId) => {
    try {
      const authHeaders = NotificationService.getAuthHeaders();
      const updatedNotification = {
        read: true
      };
      const response = await axios.put(`/api/notifications/update/${notificationId}`, updatedNotification, authHeaders);
      return response.data;
    } catch (error) {
      console.error('Failed to mark notification as read:', error);
      throw error;
    }
  }
};

export default NotificationService;
