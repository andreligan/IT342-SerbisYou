import React, { useState, useEffect } from 'react';
import ChatService from '../../services/ChatService';
import axios from 'axios';

function ChatList({ onSelectUser, searchQuery }) {
  const [users, setUsers] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const baseURL = "http://localhost:8080";

  useEffect(() => {
    const fetchConversationPartners = async () => {
      try {
        setLoading(true);
        // Fetch users that the current user has had conversations with
        const conversationPartners = await ChatService.getConversationPartners();

        if (conversationPartners && conversationPartners.length > 0) {
          // Enhance users with proper profile images
          const enhancedUsers = await Promise.all(conversationPartners.map(async (user) => {
            try {
              let profileImagePath = null;
              const token = localStorage.getItem("authToken") || sessionStorage.getItem("authToken");

              if (user.role?.toLowerCase() === 'customer') {
                // Get customer profile image
                const customersResponse = await axios.get('/api/customers/getAll', {
                  headers: { Authorization: `Bearer ${token}` }
                });

                const customer = customersResponse.data.find(
                  c => c.userAuth && c.userAuth.userId == user.userId
                );

                if (customer) {
                  const imageResponse = await axios.get(`/api/customers/getProfileImage/${customer.customerId}`, {
                    headers: { Authorization: `Bearer ${token}` }
                  });

                  if (imageResponse.data) {
                    profileImagePath = `${baseURL}${imageResponse.data}`;
                  }
                }
              } else if (user.role?.toLowerCase() === 'service provider') {
                // Get service provider profile image
                const providersResponse = await axios.get('/api/service-providers/getAll', {
                  headers: { Authorization: `Bearer ${token}` }
                });

                const provider = providersResponse.data.find(
                  p => p.userAuth && p.userAuth.userId == user.userId
                );

                if (provider) {
                  const imageResponse = await axios.get(`/api/service-providers/getServiceProviderImage/${provider.providerId}`, {
                    headers: { Authorization: `Bearer ${token}` }
                  });

                  if (imageResponse.data) {
                    profileImagePath = `${baseURL}${imageResponse.data}`;
                  }
                }
              }

              return {
                ...user,
                profileImage: profileImagePath || user.profileImage
              };
            } catch (err) {
              console.warn(`Could not fetch profile image for user ${user.userId}:`, err);
              return user;
            }
          }));

          setUsers(enhancedUsers);
        } else {
          console.log('No conversation partners found');
          setUsers([]);
        }
        setError(null);
      } catch (err) {
        console.error('Failed to fetch conversation partners:', err);
        setError('Failed to load conversations');
        setUsers([]);
      } finally {
        setLoading(false);
      }
    };

    fetchConversationPartners();
  }, []);

  // Filter users based on search query
  const filteredUsers = users.filter(user => {
    if (!searchQuery) return true;

    const query = searchQuery.toLowerCase();
    return (
      (user.userName?.toLowerCase().includes(query)) ||
      (user.firstName?.toLowerCase().includes(query)) ||
      (user.lastName?.toLowerCase().includes(query))
    );
  });

  if (loading) {
    return (
      <div className="flex-1 flex justify-center items-center bg-gray-50">
        <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-[#495E57]"></div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="text-center p-6 text-red-500">
        {error}
      </div>
    );
  }

  return (
    <div className="flex-1 overflow-y-auto bg-gray-50">
      <div className="p-2 border-b border-gray-200 bg-green-50 flex items-center">
        <span className="inline-block w-3 h-3 bg-green-500 rounded-full mr-2"></span>
        <span className="text-sm text-gray-600">Recent Conversations</span>
      </div>

      {filteredUsers.length === 0 ? (
        <div className="text-center p-6 text-gray-500">
          No recent conversations found.
          <p className="mt-2 text-sm">
            Use the search to find users and start chatting!
          </p>
        </div>
      ) : (
        filteredUsers.map(user => (
          <div 
            key={user.userId}
            onClick={() => onSelectUser(user)}
            className="flex items-center p-3 border-b border-gray-100 hover:bg-gray-100 cursor-pointer"
          >
            <div className="w-10 h-10 rounded-full bg-gray-300 mr-3 flex items-center justify-center overflow-hidden">
              {user.profileImage ? (
                <img 
                  src={user.profileImage} 
                  alt={user.userName} 
                  className="w-full h-full object-cover" 
                  onError={(e) => {
                    e.target.onerror = null;
                    e.target.src = `https://ui-avatars.com/api/?name=${encodeURIComponent(user.firstName || user.userName || 'User')}&background=random`;
                  }}
                />
              ) : (
                <span className="font-medium text-gray-600">
                  {(user.firstName?.charAt(0) || user.userName?.charAt(0) || '?').toUpperCase()}
                </span>
              )}
            </div>
            <div className="flex-1">
              <div className="flex justify-between items-baseline">
                <span className="font-medium">
                  {user.firstName && user.lastName 
                    ? `${user.firstName} ${user.lastName}` 
                    : user.userName}
                </span>
                <span className="text-xs text-gray-500">{user.lastMessageTime}</span>
              </div>
              <p className={`text-sm ${user.isUnread ? 'font-semibold text-black' : 'text-gray-600'} truncate`}>
                {user.lastMessage}
                {user.isUnread && <span className="ml-1 inline-block w-2 h-2 bg-blue-500 rounded-full"></span>}
              </p>
            </div>
          </div>
        ))
      )}
    </div>
  );
}

export default ChatList;