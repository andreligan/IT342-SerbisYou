import React, { useState, useEffect, useRef } from 'react';
import axios from 'axios'; // Add this import
import ChatHeader from './ChatHeader';
import ChatList from './ChatList';
import UserSearch from './UserSearch';
import Conversation from './Conversation';
import ChatService from '../../services/ChatService';

function ChatWindow({ onClose }) {
  const [selectedUser, setSelectedUser] = useState(null);
  const [view, setView] = useState('list'); // 'list', 'search', or 'conversation'
  const [searchQuery, setSearchQuery] = useState('');
  const chatWindowRef = useRef(null);
  const [refreshList, setRefreshList] = useState(0);
  const [isProcessingPendingChat, setIsProcessingPendingChat] = useState(false);

  const handleUserSelect = (user) => {
    setSelectedUser(user);
    setView('conversation');
    // User selected, will mark messages as read in Conversation component
  };

  const handleBackToList = () => {
    setView('list');
    setSelectedUser(null);
    // Refresh the list to show updated message status
    setRefreshList(prev => prev + 1);
  };

  // Toggle between list and search views based on search query
  useEffect(() => {
    if (searchQuery && view === 'list') {
      setView('search');
    } else if (!searchQuery && view === 'search') {
      setView('list');
    }
  }, [searchQuery, view]);

  useEffect(() => {
    function handleClickOutside(event) {
      if (chatWindowRef.current && !chatWindowRef.current.contains(event.target)) {
        // Only close if clicking outside and not on the chat icon
        const chatIcon = document.querySelector('#chat-icon');
        if (!chatIcon || !chatIcon.contains(event.target)) {
          onClose();
        }
      }
    }

    document.addEventListener('mousedown', handleClickOutside);
    return () => {
      document.removeEventListener('mousedown', handleClickOutside);
    };
  }, [onClose]);

  // Handle opening chat when triggered by clicking a message notification
  useEffect(() => {
    const handleOpenChatWithUser = async () => {
      // Get the pending chat user info from localStorage
      const pendingChatUserJSON = localStorage.getItem('pendingChatUser');
      
      if (pendingChatUserJSON && !isProcessingPendingChat) {
        setIsProcessingPendingChat(true);
        
        try {
          const pendingChatUser = JSON.parse(pendingChatUserJSON);
          
          // Get all users to find the one that matches by name or reference ID
          const allUsers = await ChatService.getAllUsers();
          const messageId = pendingChatUser.messageId;
          const referenceId = pendingChatUser.referenceId;
          
          if (referenceId) {
            // Fetch the specific message to find the sender
            const allMessagesResponse = await axios.get('/api/messages/getAll', {
              headers: ChatService.getAuthHeaders().headers
            });
            
            if (allMessagesResponse.data) {
              const message = allMessagesResponse.data.find(msg => msg.messageId === parseInt(referenceId));
              
              if (message && message.sender) {
                const senderId = message.sender.userId;
                
                // Find the user with this ID
                const userToSelect = allUsers.find(user => user.userId === senderId);
                
                if (userToSelect) {
                  // Open the chat window
                  handleUserSelect(userToSelect);
                }
              }
            }
          }
          
          // Clear the pending chat user
          localStorage.removeItem('pendingChatUser');
        } catch (error) {
          console.error('Error processing pending chat:', error);
        } finally {
          setIsProcessingPendingChat(false);
        }
      }
    };
    
    // Listen for the custom event
    window.addEventListener('openChatWithUser', handleOpenChatWithUser);
    
    // Check for pending chat when component mounts
    handleOpenChatWithUser();
    
    return () => {
      window.removeEventListener('openChatWithUser', handleOpenChatWithUser);
    };
  }, []);

  return (
    <div 
      ref={chatWindowRef}
      className="fixed bottom-6 right-6 z-10 w-96 h-5/6 bg-white rounded-lg shadow-xl flex flex-col overflow-hidden"
    >
      {view === 'conversation' ? (
        <Conversation 
          user={selectedUser} 
          messages={selectedUser?.messages?.length > 0 ? selectedUser.messages : []}
          onBack={handleBackToList}
          onClose={onClose}
          onMessageSent={() => setRefreshList(prev => prev + 1)}
        />
      ) : (
        <>
          <ChatHeader 
            title={view === 'search' ? 'Search Users' : 'Chat'} 
            onClose={onClose} 
            searchQuery={searchQuery}
            onSearchChange={(e) => setSearchQuery(e.target.value)}
          />
          
          {view === 'search' ? (
            <UserSearch 
              onSelectUser={handleUserSelect}
              searchQuery={searchQuery}
            />
          ) : (
            <ChatList 
              onSelectUser={handleUserSelect}
              searchQuery=""
              key={refreshList} // Add key to force refresh when sending a message
            />
          )}
        </>
      )}
    </div>
  );
}

export default ChatWindow;