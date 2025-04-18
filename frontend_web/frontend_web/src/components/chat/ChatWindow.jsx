import React, { useState, useEffect, useRef } from 'react';
import ChatHeader from './ChatHeader';
import ChatList from './ChatList';
import UserSearch from './UserSearch';
import Conversation from './Conversation';
import { mockMessages } from './mockData';

function ChatWindow({ onClose }) {
  const [selectedUser, setSelectedUser] = useState(null);
  const [view, setView] = useState('list'); // 'list', 'search', or 'conversation'
  const [searchQuery, setSearchQuery] = useState('');
  const chatWindowRef = useRef(null);

  const handleUserSelect = (user) => {
    setSelectedUser(user);
    setView('conversation');
  };

  const handleBackToList = () => {
    setView('list');
    setSelectedUser(null);
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
          messages={mockMessages.filter(m => 
            m.sender.userId === selectedUser.userId || 
            m.receiver.userId === selectedUser.userId
          )} 
          onBack={handleBackToList}
          onClose={onClose}
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
              searchQuery={searchQuery}
            />
          )}
        </>
      )}
    </div>
  );
}

export default ChatWindow;