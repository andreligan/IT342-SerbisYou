import apiClient, { getApiUrl, API_BASE_URL } from '../utils/apiConfig';
import NotificationService from './NotificationService';

// Use API_BASE_URL from apiConfig instead of hardcoded value
const baseURL = API_BASE_URL;

// Mock data for fallback when API is unavailable
const MOCK_USERS = [
  {
    userId: 1,
    userName: "current_user",
    firstName: "Current",
    lastName: "User",
    phoneNumber: "123-456-7890",
    role: "Customer",
    profileImage: null,
    lastMessage: "Hello there!",
    lastMessageTime: "Just now"
  },
  {
    userId: 2,
    userName: "aukey_philippines",
    firstName: "Aukey",
    lastName: "Philippines",
    phoneNumber: "987-654-3210",
    businessName: "Aukey Philippines",
    role: "Service Provider",
    profileImage: null,
    lastMessage: "Thank you for your inquiry",
    lastMessageTime: "5 min ago"
  },
  // Add more mock users as needed
];

const ChatService = {
  // Get the current user's ID from storage
  getCurrentUserId: () => {
    // First try to get userId directly from storage
    const userId = localStorage.getItem('userId') || sessionStorage.getItem('userId');
    if (userId) return parseInt(userId, 10);
    
    return 1; // Default to user ID 1 if not found
  },
  
  // Get all users (both customers and service providers)
  getAllUsers: async () => {
    try {
      // Make parallel requests to get both customers and providers
      const [customersResponse, providersResponse] = await Promise.allSettled([
        apiClient.get(getApiUrl('customers/getAll')),
        apiClient.get(getApiUrl('service-providers/getAll'))
      ]);

      // If both requests failed, use mock data
      if (customersResponse.status === 'rejected' && providersResponse.status === 'rejected') {
        console.warn('Using mock data for chat users due to API failure');
        return MOCK_USERS;
      }

      // Process customer data if request was successful
      const customers = customersResponse.status === 'fulfilled'
        ? (customersResponse.value?.data || []).map(customer => {
            // Format profile image URL
            let profileImage = customer.profileImage;
            if (profileImage && !profileImage.startsWith('http')) {
              profileImage = `${baseURL}${profileImage}`;
            }
            
            return {
              userId: customer.userAuth?.userId,
              userName: customer.userAuth?.userName,
              firstName: customer.firstName || '',
              lastName: customer.lastName || '',
              phoneNumber: customer.phoneNumber,
              role: 'Customer',
              profileImage: profileImage,
              lastMessage: "No messages yet",
              lastMessageTime: ""
            };
          }).filter(user => user.userId)
        : [];

      // Process service provider data if request was successful
      const providers = providersResponse.status === 'fulfilled'
        ? (providersResponse.value?.data || []).map(provider => {
            // Format profile image URL
            let profileImage = provider.serviceProviderImage;
            if (profileImage && !profileImage.startsWith('http')) {
              profileImage = `${baseURL}${profileImage}`;
            }
            
            return {
              userId: provider.userAuth?.userId,
              userName: provider.userAuth?.userName,
              firstName: provider.firstName || '',
              lastName: provider.lastName || '',
              phoneNumber: provider.phoneNumber,
              businessName: provider.businessName,
              role: 'Service Provider',
              profileImage: profileImage,
              lastMessage: "No messages yet",
              lastMessageTime: ""
            };
          }).filter(user => user.userId)
        : [];

      return [...customers, ...providers];
    } catch (error) {
      console.error('Failed to fetch users:', error);
      // Return mock data as fallback
      return MOCK_USERS;
    }
  },

  // Send a message to another user
  sendMessage: async (receiverId, messageText) => {
    try {
      const currentUserId = ChatService.getCurrentUserId();
      
      if (!currentUserId) {
        throw new Error("No authenticated user found");
      }
      
      const messageData = {
        sender: { userId: currentUserId },
        receiver: { userId: receiverId },
        messageText: messageText,
        sentAt: new Date().toISOString(),
        status: "SENT"
      };
      
      console.log('Sending message:', messageData);
      // Send message using apiClient and getApiUrl
      const response = await apiClient.post(getApiUrl('messages/postMessage'), messageData);
      const sentMessage = response.data;
      console.log('Message sent successfully:', sentMessage);
      
      // Create notification for the receiver
      try {
        console.log('Attempting to create notification for user:', receiverId);
        // Get the sender's name for the notification message
        const sender = await ChatService.getUserById(currentUserId);
        const senderName = sender?.firstName && sender?.lastName 
          ? `${sender.firstName} ${sender.lastName}`
          : sender?.userName || "Someone";
          
        const notification = {
          user: { userId: receiverId },
          type: "Message",
          message: `${senderName} sent you a message: "${messageText.substring(0, 30)}${messageText.length > 30 ? '...' : ''}"`,
          isRead: false,
          createdAt: new Date().toISOString(),
          referenceId: sentMessage.messageId,
          referenceType: "Message"
        };
        
        console.log('Creating notification with data:', notification);
        const createdNotification = await NotificationService.createNotification(notification);
        console.log('Notification created successfully:', createdNotification);
      } catch (notifError) {
        console.error('Failed to create notification:', notifError);
        // Continue even if notification creation fails
      }
      
      return sentMessage;
    } catch (error) {
      console.error('Failed to send message:', error);
      throw error;
    }
  },
  
  // Get user by ID (find user with matching userId from all customers and providers)
  getUserById: async (userId) => {
    try {
      // Get all customers and providers using apiClient
      const [customersResponse, providersResponse] = await Promise.allSettled([
        apiClient.get(getApiUrl('customers/getAll')),
        apiClient.get(getApiUrl('service-providers/getAll'))
      ]);
      
      // Look through customers
      if (customersResponse.status === 'fulfilled') {
        const customers = customersResponse.value?.data || [];
        const matchingCustomer = customers.find(customer => customer.userAuth?.userId === userId);
        
        if (matchingCustomer) {
          // Format profile image URL
          let profileImage = matchingCustomer.profileImage;
          if (profileImage && !profileImage.startsWith('http')) {
            profileImage = `${baseURL}${profileImage}`;
          }
          
          return {
            userId: matchingCustomer.userAuth?.userId,
            userName: matchingCustomer.userAuth?.userName,
            firstName: matchingCustomer.firstName || '',
            lastName: matchingCustomer.lastName || '',
            phoneNumber: matchingCustomer.phoneNumber,
            role: 'Customer',
            profileImage: profileImage
          };
        }
      }
      
      // Look through service providers
      if (providersResponse.status === 'fulfilled') {
        const providers = providersResponse.value?.data || [];
        const matchingProvider = providers.find(provider => provider.userAuth?.userId === userId);
        
        if (matchingProvider) {
          // Format profile image URL
          let profileImage = matchingProvider.serviceProviderImage;
          if (profileImage && !profileImage.startsWith('http')) {
            profileImage = `${baseURL}${profileImage}`;
          }
          
          return {
            userId: matchingProvider.userAuth?.userId,
            userName: matchingProvider.userAuth?.userName,
            firstName: matchingProvider.firstName || '',
            lastName: matchingProvider.lastName || '',
            phoneNumber: matchingProvider.phoneNumber,
            businessName: matchingProvider.businessName,
            role: 'Service Provider',
            profileImage: profileImage
          };
        }
      }
      
      // If we reach here, no user with matching userId was found
      throw new Error(`User ${userId} not found`);
    } catch (error) {
      console.error(`Failed to fetch user ${userId}:`, error);
      // Return a minimal user object if the API call fails
      return { userId: userId, userName: "user_" + userId };
    }
  },

  // Get conversation history between two users
  getConversation: async (otherUserId) => {
    try {
      const currentUserId = ChatService.getCurrentUserId();
      
      if (!currentUserId) {
        throw new Error("No authenticated user found");
      }
      
      // Use apiClient and getApiUrl for endpoint
      const response = await apiClient.get(getApiUrl(`messages/conversation/${currentUserId}/${otherUserId}`));
      return response.data;
    } catch (error) {
      console.error('Failed to fetch conversation:', error);
      // Return empty array as fallback
      return [];
    }
  },

  // Get conversation partners
  getConversationPartners: async () => {
    try {
      const currentUserId = ChatService.getCurrentUserId();
      
      if (!currentUserId) {
        throw new Error("No authenticated user found");
      }
      
      // Try to use the dedicated endpoint with apiClient
      try {
        const response = await apiClient.get(getApiUrl(`messages/conversation-partners/${currentUserId}`));
        
        // Get the user roles for each partner to help with profile image fetching
        const partners = response.data || [];
        
        if (partners.length > 0) {
          const enhancedPartners = await Promise.all(partners.map(async (partner) => {
            try {
              if (partner.userId) {
                const userResponse = await apiClient.get(getApiUrl(`user-auth/getById/${partner.userId}`));
                if (userResponse.data) {
                  return {
                    ...partner,
                    role: userResponse.data.role
                  };
                }
              }
              return partner;
            } catch (err) {
              console.warn(`Could not fetch role for user ${partner.userId}:`, err);
              return partner;
            }
          }));
          
          return enhancedPartners;
        }
        
        return partners;
      } catch (error) {
        console.warn('Conversation partners endpoint not available, falling back to alternative method');
        
        // Fallback: Fetch all messages for this user and process them using apiClient
        const messagesResponse = await apiClient.get(getApiUrl('messages/getAll'));
        const allMessages = messagesResponse.data || [];
        
        // Filter messages where current user is sender or receiver
        const relevantMessages = allMessages.filter(msg => 
          msg.sender?.userId === currentUserId || msg.receiver?.userId === currentUserId
        );
        
        // Group by partner
        const conversationsByPartner = {};
        const partnerIds = new Set();
        
        relevantMessages.forEach(msg => {
          const isCurrentUserSender = msg.sender.userId === currentUserId;
          const partnerId = isCurrentUserSender ? msg.receiver.userId : msg.sender.userId;
          partnerIds.add(partnerId);
          
          if (!conversationsByPartner[partnerId]) {
            conversationsByPartner[partnerId] = [];
          }
          conversationsByPartner[partnerId].push(msg);
        });
        
        // We need to fetch the user details for all these partners
        const users = await ChatService.getAllUsers();
        const partners = users.filter(user => partnerIds.has(user.userId));
        
        // Add the latest message to each partner
        return partners.map(partner => {
          const conversations = conversationsByPartner[partner.userId] || [];
          // Sort by sent time (newest first)
          conversations.sort((a, b) => new Date(b.sentAt) - new Date(a.sentAt));
          const latestMessage = conversations[0];
          
          return {
            ...partner,
            lastMessage: latestMessage?.messageText || "No messages yet",
            lastMessageTime: latestMessage?.sentAt ? new Date(latestMessage.sentAt).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' }) : "",
            isUnread: latestMessage && latestMessage.receiver.userId === currentUserId && latestMessage.status !== 'READ'
          };
        });
      }
    } catch (error) {
      console.error('Failed to fetch conversation partners:', error);
      return [];
    }
  },

  // Mark a message as read while preserving the message text
  markMessageAsRead: async (messageId) => {
    try {
      let messageText = null;
      
      // First try to find the message from the conversation API
      try {
        // Get all messages using apiClient
        const allMessagesResponse = await apiClient.get(getApiUrl('messages/getAll'));
        const allMessages = allMessagesResponse.data;
        
        // Find the specific message
        const message = allMessages.find(msg => msg.messageId === messageId);
        
        if (message) {
          messageText = message.messageText;
          console.log(`Found message ${messageId} with text: "${messageText}"`);
        } else {
          console.warn(`Message ${messageId} not found in all messages`);
        }
      } catch (error) {
        console.error('Failed to find message:', error);
      }
      
      // Create update data with both status AND messageText if found
      const updateData = {
        status: "READ"
      };
      
      // Only add messageText if we found it
      if (messageText) {
        updateData.messageText = messageText;
      }
      
      console.log(`Updating message ${messageId} status to READ${messageText ? ' with preserved text' : ''}`);
      const response = await apiClient.put(getApiUrl(`messages/updateMessage/${messageId}`), updateData);
      console.log('Message marked as read:', response.data);
      return response.data;
    } catch (error) {
      console.error(`Failed to mark message ${messageId} as read:`, error);
      throw error;
    }
  }
};

export default ChatService;