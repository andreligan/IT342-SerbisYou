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
  }
};

export default ChatService;