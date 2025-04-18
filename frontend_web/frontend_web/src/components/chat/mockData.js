// Mock data for development and fallback purposes

export const mockUsers = [
  {
    userId: 2,
    userName: "aukey_philippines",
    firstName: "Aukey",
    lastName: "Philippines",
    role: "Service Provider",
    businessName: "Aukey Electronics",
    lastMessage: "Hi there! How can I help you today?",
    lastMessageTime: "15:48",
    profileImage: null
  },
  {
    userId: 3,
    userName: "Jewelrydream.ph",
    firstName: "Jewelry",
    lastName: "Dream",
    role: "Service Provider",
    businessName: "Jewelry Dream PH",
    lastMessage: "Thank you for your order!",
    lastMessageTime: "10:41",
    profileImage: null
  },
  {
    userId: 4,
    userName: "maria_santos",
    firstName: "Maria",
    lastName: "Santos",
    role: "Customer",
    lastMessage: "When will my order arrive?",
    lastMessageTime: "Yesterday",
    profileImage: null
  },
  {
    userId: 5,
    userName: "juan_dela_cruz",
    firstName: "Juan",
    lastName: "Dela Cruz",
    role: "Customer",
    lastMessage: "Thank you for the excellent service!",
    lastMessageTime: "Monday",
    profileImage: null
  }
];

export const mockMessages = [
  {
    messageId: 1,
    sender: {
      userId: 2,
      userName: "aukey_philippines"
    },
    receiver: {
      userId: 1,
      userName: "current_user"
    },
    messageText: "Thank you for your interest in our products!",
    sentAt: "2025-04-18T15:40:00",
    status: "DELIVERED"
  },
  {
    messageId: 2,
    sender: {
      userId: 1,
      userName: "current_user"
    },
    receiver: {
      userId: 2,
      userName: "aukey_philippines"
    },
    messageText: "I have a question about your power banks. Which one would you recommend for travel?",
    sentAt: "2025-04-18T15:41:00",
    status: "DELIVERED"
  },
  {
    messageId: 3,
    sender: {
      userId: 2,
      userName: "aukey_philippines"
    },
    receiver: {
      userId: 1,
      userName: "current_user"
    },
    messageText: "For travel, I would recommend our 10000mAh slim power bank. It's lightweight and can charge your phone multiple times.",
    sentAt: "2025-04-18T15:43:00",
    status: "DELIVERED"
  }
];