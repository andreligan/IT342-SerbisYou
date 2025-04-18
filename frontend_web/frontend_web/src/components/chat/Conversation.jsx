import React, { useState, useRef, useEffect } from 'react';
import ChatHeader from './ChatHeader';
import MessageBubble from './MessageBubble';
import ChatService from '../../services/ChatService';

function Conversation({ user, messages, onBack, onClose }) {
  const [newMessage, setNewMessage] = useState('');
  const messageEndRef = useRef(null);
  const currentUserId = ChatService.getCurrentUserId() || 1; // Fallback to 1 for testing
  
  const handleSendMessage = (e) => {
    e.preventDefault();
    if (newMessage.trim() === '') return;
    
    // In a real app, you would send this to your API
    console.log('Sending message:', { 
      sender: { userId: currentUserId }, 
      receiver: user, 
      messageText: newMessage,
      sentAt: new Date()
    });
    
    setNewMessage('');
  };

  useEffect(() => {
    messageEndRef.current?.scrollIntoView({ behavior: 'smooth' });
  }, [messages]);

  // Display full name if available, otherwise username
  const displayName = user.firstName && user.lastName 
    ? `${user.firstName} ${user.lastName}` 
    : user.userName;

  return (
    <>
      <ChatHeader 
        title={displayName} 
        onClose={onClose}
        onBack={onBack}
        showBackButton={true}
      />
      
      <div className="flex-1 overflow-y-auto p-4 bg-gray-50">
        <div className="space-y-4">
          {messages.map((message, index) => (
            <MessageBubble 
              key={index}
              message={message}
              isCurrentUser={message.sender.userId === currentUserId}
            />
          ))}
          <div ref={messageEndRef} />
        </div>
      </div>
      
      <form onSubmit={handleSendMessage} className="p-3 border-t border-gray-200 flex items-center space-x-2">
        <div className="flex items-center space-x-2">
          <button type="button" className="text-gray-500">
            <svg xmlns="http://www.w3.org/2000/svg" className="h-6 w-6" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M14.828 14.828a4 4 0 01-5.656 0M9 10h.01M15 10h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
            </svg>
          </button>
          <button type="button" className="text-gray-500">
            <svg xmlns="http://www.w3.org/2000/svg" className="h-6 w-6" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M4 16l4.586-4.586a2 2 0 012.828 0L16 16m-2-2l1.586-1.586a2 2 0 012.828 0L20 14m-6-6h.01M6 20h12a2 2 0 002-2V6a2 2 0 00-2-2H6a2 2 0 00-2 2v12a2 2 0 002 2z" />
            </svg>
          </button>
        </div>
        
        <input
          type="text"
          value={newMessage}
          onChange={(e) => setNewMessage(e.target.value)}
          placeholder="Type a message here"
          className="flex-1 py-2 px-3 border border-gray-300 rounded-full focus:outline-none focus:ring-1 focus:ring-[#F4CE14]"
        />
        
        <button 
          type="submit" 
          disabled={!newMessage.trim()}
          className="bg-[#F4CE14] text-[#495E57] p-2 rounded-full disabled:opacity-50"
        >
          <svg xmlns="http://www.w3.org/2000/svg" className="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 19l9 2-9-18-9 18 9-2zm0 0v-8" />
          </svg>
        </button>
      </form>
    </>
  );
}

export default Conversation;