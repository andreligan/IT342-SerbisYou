import React, { useState, useEffect } from 'react';
import ChatService from '../../services/ChatService';

function UserSearch({ onSelectUser, searchQuery }) {
  const [users, setUsers] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    const fetchUsers = async () => {
      try {
        setLoading(true);
        const allUsers = await ChatService.getAllUsers();
        setUsers(allUsers);
        setError(null);
      } catch (err) {
        console.error('Failed to fetch users:', err);
        setError('Unable to load users. Please try again later.');
      } finally {
        setLoading(false);
      }
    };

    fetchUsers();
  }, []);

  // Filter users based on search query
  const filteredUsers = users.filter(user => {
    if (!searchQuery) return true;
    
    const query = searchQuery.toLowerCase();
    return (
      (user.firstName?.toLowerCase().includes(query)) ||
      (user.lastName?.toLowerCase().includes(query)) ||
      (user.userName?.toLowerCase().includes(query)) ||
      (user.businessName?.toLowerCase().includes(query))
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
      <div className="flex-1 flex flex-col justify-center items-center bg-gray-50">
        <p className="text-red-500 mb-2">{error}</p>
        <button 
          onClick={() => window.location.reload()}
          className="px-4 py-2 bg-[#495E57] text-white rounded-md"
        >
          Retry
        </button>
      </div>
    );
  }

  return (
    <div className="flex-1 overflow-y-auto bg-gray-50">
      <div className="p-2 border-b border-gray-200 bg-blue-50 flex items-center">
        <span className="text-sm text-gray-600">Found {filteredUsers.length} users</span>
      </div>
      
      {filteredUsers.length === 0 ? (
        <div className="text-center p-6 text-gray-500">
          {searchQuery ? 'No users found matching your search.' : 'No users available.'}
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
                <img src={user.profileImage} alt={user.firstName} className="w-full h-full object-cover" />
              ) : (
                <span className="font-medium text-gray-600">
                  {user.firstName.charAt(0).toUpperCase() || user.userName?.charAt(0).toUpperCase() || '?'}
                </span>
              )}
            </div>
            <div className="flex-1">
              <div className="flex justify-between items-baseline">
                <span className="font-medium">
                  {user.firstName} {user.lastName}
                </span>
                <span className="text-xs bg-gray-200 px-2 py-1 rounded text-gray-700">
                  {user.role}
                </span>
              </div>
              {user.businessName && (
                <p className="text-sm text-gray-600">
                  {user.businessName}
                </p>
              )}
              <p className="text-xs text-gray-500">
                @{user.userName}
              </p>
            </div>
          </div>
        ))
      )}
    </div>
  );
}

export default UserSearch;