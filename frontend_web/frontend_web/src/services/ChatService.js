import axios from 'axios';

// Remove absolute URL to use relative URLs with Vite proxy
const API_URL = ''; 

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
  // Get auth token from storage
  getAuthToken: () => {
    const token = localStorage.getItem('authToken') || sessionStorage.getItem('authToken');
    if (!token) {
      console.warn('No auth token found in storage');
    }
    return token;
  },
  
  // Get authentication headers with bearer token
  getAuthHeaders: () => {
    const token = ChatService.getAuthToken();
    if (!token) return {};
    
    return {
      headers: {
        'Authorization': `Bearer ${token}`,
        'Content-Type': 'application/json'
      }
    };
  },
  
  // Get all users (both customers and service providers)
  getAllUsers: async () => {
    try {
      const authHeaders = ChatService.getAuthHeaders();
      
      // Make parallel requests to get both customers and providers
      const [customersResponse, providersResponse] = await Promise.allSettled([
        axios.get(`/api/customers/getAll`, authHeaders),
        axios.get(`/api/service-providers/getAll`, authHeaders)
      ]);

      // If both requests failed, use mock data
      if (customersResponse.status === 'rejected' && providersResponse.status === 'rejected') {
        console.warn('Using mock data for chat users due to API failure');
        return MOCK_USERS;
      }

      // Process customer data if request was successful
      const customers = customersResponse.status === 'fulfilled'
        ? (customersResponse.value?.data || []).map(customer => ({
            userId: customer.userAuth?.userId,
            userName: customer.userAuth?.userName,
            firstName: customer.firstName || '',
            lastName: customer.lastName || '',
            phoneNumber: customer.phoneNumber,
            role: 'Customer',
            profileImage: customer.profileImage,
            lastMessage: "No messages yet",
            lastMessageTime: ""
          })).filter(user => user.userId)
        : [];

      // Process service provider data if request was successful
      const providers = providersResponse.status === 'fulfilled'
        ? (providersResponse.value?.data || []).map(provider => ({
            userId: provider.userAuth?.userId,
            userName: provider.userAuth?.userName,
            firstName: provider.firstName || '',
            lastName: provider.lastName || '',
            phoneNumber: provider.phoneNumber,
            businessName: provider.businessName,
            role: 'Service Provider',
            profileImage: provider.profileImage,
            lastMessage: "No messages yet",
            lastMessageTime: ""
          })).filter(user => user.userId)
        : [];

      return [...customers, ...providers];
    } catch (error) {
      console.error('Failed to fetch users:', error);
      // Return mock data as fallback
      return MOCK_USERS;
    }
  },

  // Get the current user's ID from storage
  getCurrentUserId: () => {
    // First try to get userId directly from storage
    const userId = localStorage.getItem('userId') || sessionStorage.getItem('userId');
    if (userId) return parseInt(userId, 10);
    
    return 1; // Default to user ID 1 if not found
  },

  // Send a message to another user
  sendMessage: async (receiverId, messageText) => {
    try {
      const authHeaders = ChatService.getAuthHeaders();
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
      
      const response = await axios.post('/api/messages/postMessage', messageData, authHeaders);
      return response.data;
    } catch (error) {
      console.error('Failed to send message:', error);
      throw error;
    }
  },
  
  // Get conversation history between two users
  getConversation: async (otherUserId) => {
    try {
      const authHeaders = ChatService.getAuthHeaders();
      const currentUserId = ChatService.getCurrentUserId();
      
      if (!currentUserId) {
        throw new Error("No authenticated user found");
      }
      
      // This endpoint would need to be implemented on your backend
      const response = await axios.get(`/api/messages/conversation/${currentUserId}/${otherUserId}`, authHeaders);
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
      const authHeaders = ChatService.getAuthHeaders();
      const currentUserId = ChatService.getCurrentUserId();
      
      if (!currentUserId) {
        throw new Error("No authenticated user found");
      }
      
      // Try to use the dedicated endpoint
      try {
        const response = await axios.get(`/api/messages/conversation-partners/${currentUserId}`, authHeaders);
        return response.data;
      } catch (error) {
        console.warn('Conversation partners endpoint not available, falling back to alternative method');
        
        // Fallback: Fetch all messages for this user and process them
        const messagesResponse = await axios.get(`/api/messages/getAll`, authHeaders);
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
  }
};

export default ChatService;