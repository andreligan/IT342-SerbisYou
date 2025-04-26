import React, { useState, useEffect, useRef } from 'react';
import ChatHeader from './ChatHeader';
import ChatList from './ChatList';
import UserSearch from './UserSearch';
import Conversation from './Conversation';

function ChatWindow({ onClose }) {
  const [selectedUser, setSelectedUser] = useState(null);
  const [view, setView] = useState('list'); // 'list', 'search', or 'conversation'
  const [searchQuery, setSearchQuery] = useState('');
  const chatWindowRef = useRef(null);
  const [refreshList, setRefreshList] = useState(0);

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