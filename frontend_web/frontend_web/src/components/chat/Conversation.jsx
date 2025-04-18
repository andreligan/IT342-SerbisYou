import React, { useState, useRef, useEffect } from 'react';
import ChatHeader from './ChatHeader';
import MessageBubble from './MessageBubble';
import ChatService from '../../services/ChatService';

function Conversation({ user, messages: initialMessages, onBack, onClose }) {
  const [newMessage, setNewMessage] = useState('');
  const [messages, setMessages] = useState(initialMessages || []);
  const [sending, setSending] = useState(false);
  const [error, setError] = useState(null);
  const messageEndRef = useRef(null);
  const currentUserId = ChatService.getCurrentUserId();
  
  // Fetch conversation history when component mounts
  useEffect(() => {
    const fetchMessages = async () => {
      try {
        if (user?.userId) {
          const conversationHistory = await ChatService.getConversation(user.userId);
          if (conversationHistory && conversationHistory.length > 0) {
            setMessages(conversationHistory);
          }
        }
      } catch (err) {
        console.error('Failed to load conversation history:', err);
        // If API call fails, keep showing the initial messages
      }
    };
    
    fetchMessages();
  }, [user?.userId]);
  
  const handleSendMessage = async (e) => {
    e.preventDefault();
    if (newMessage.trim() === '' || !user?.userId) return;
    
    const tempMessage = {
      id: `temp-${Date.now()}`,
      sender: { userId: currentUserId },
      receiver: { userId: user.userId },
      messageText: newMessage,
      sentAt: new Date().toISOString(),
      status: 'SENDING',
      isTemporary: true
    };
    
    // Add message to UI immediately (optimistic update)
    setMessages(prevMessages => [...prevMessages, tempMessage]);
    setNewMessage('');
    setSending(true);
    setError(null);
    
    try {
      // Send message to server
      const sentMessage = await ChatService.sendMessage(user.userId, newMessage);
      
      // Replace temporary message with the real one from server
      setMessages(prevMessages => 
        prevMessages.map(msg => 
          msg.id === tempMessage.id ? sentMessage : msg
        )
      );
    } catch (err) {
      console.error('Failed to send message:', err);
      setError('Failed to send message. Please try again.');
      
      // Update temporary message to show error state
      setMessages(prevMessages => 
        prevMessages.map(msg => 
          msg.id === tempMessage.id 
            ? {...msg, status: 'ERROR'} 
            : msg
        )
      );
    } finally {
      setSending(false);
    }
  };
  
  // Resend a failed message
  const handleResend = async (tempMessageId, messageText) => {
    // Remove the failed message
    setMessages(prevMessages => 
      prevMessages.filter(msg => msg.id !== tempMessageId)
    );
    
    // Set the message text and trigger send
    setNewMessage(messageText);
    setTimeout(() => {
      handleSendMessage({ preventDefault: () => {} });
    }, 0);
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
        {messages.length === 0 ? (
          <div className="flex items-center justify-center h-full">
            <p className="text-gray-500">No messages yet. Say hello!</p>
          </div>
        ) : (
          <div className="space-y-4">
            {messages.map((message, index) => (
              <MessageBubble 
                key={message.messageId || message.id || index}
                message={message}
                isCurrentUser={message.sender?.userId === currentUserId}
                onResend={message.status === 'ERROR' ? 
                  () => handleResend(message.id, message.messageText) : 
                  undefined}
              />
            ))}
            <div ref={messageEndRef} />
          </div>
        )}
      </div>
      
      {error && (
        <div className="bg-red-50 border-l-4 border-red-500 p-2 text-red-700 text-sm">
          {error}
        </div>
      )}
      
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
          disabled={sending}
        />
        
        <button 
          type="submit" 
          disabled={!newMessage.trim() || sending}
          className="bg-[#F4CE14] text-[#495E57] p-2 rounded-full disabled:opacity-50"
        >
          {sending ? (
            <div className="h-5 w-5 border-2 border-t-transparent border-[#495E57] rounded-full animate-spin"></div>
          ) : (
            <svg xmlns="http://www.w3.org/2000/svg" className="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 19l9 2-9-18-9 18 9-2zm0 0v-8" />
            </svg>
          )}
        </button>
      </form>
    </>
  );
}

export default Conversation;