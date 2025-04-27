import React, { useState, useRef, useEffect } from 'react';
import ChatHeader from './ChatHeader';
import MessageBubble from './MessageBubble';
import ChatService from '../../services/ChatService';

function Conversation({ user, messages: initialMessages, onBack, onClose, onMessageSent }) {
  const [newMessage, setNewMessage] = useState('');
  const [messages, setMessages] = useState(initialMessages || []);
  const [sending, setSending] = useState(false);
  const [error, setError] = useState(null);
  const messageEndRef = useRef(null);
  const currentUserId = ChatService.getCurrentUserId();
  const [selectedMessageId, setSelectedMessageId] = useState(null);
  
  // Fetch conversation history and mark messages as read when component mounts
  useEffect(() => {
    const fetchMessagesAndMarkRead = async () => {
      try {
        if (user?.userId) {
          // Get conversation history
          const conversationHistory = await ChatService.getConversation(user.userId);
          
          if (conversationHistory && conversationHistory.length > 0) {
            setMessages(conversationHistory);
            
            // Mark unread messages from the other user as read
            const unreadMessages = conversationHistory.filter(msg => 
              msg.sender?.userId === user.userId && // Message from the other user
              msg.status !== 'READ' && // Not already read
              msg.messageId // Has a valid ID
            );
            
            console.log(`Found ${unreadMessages.length} unread messages to mark as read`);
            
            // Mark each unread message as read
            for (const message of unreadMessages) {
              try {
                await ChatService.markMessageAsRead(message.messageId);
              } catch (err) {
                console.error(`Failed to mark message ${message.messageId} as read:`, err);
                // Continue with other messages even if one fails
              }
            }
          }
        }
      } catch (err) {
        console.error('Failed to load conversation history:', err);
      }
    };
    
    fetchMessagesAndMarkRead();
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
      // Send message to server (this now also creates a notification)
      const sentMessage = await ChatService.sendMessage(user.userId, newMessage);
      
      // Replace temporary message with the real one from server
      setMessages(prevMessages => 
        prevMessages.map(msg => 
          msg.id === tempMessage.id ? sentMessage : msg
        )
      );
      
      // Notify parent component that a message was sent
      if (onMessageSent) onMessageSent();
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

  // Handle message selection
  const handleMessageClick = (messageId) => {
    setSelectedMessageId(prev => prev === messageId ? null : messageId);
  };

  return (
    <div className="flex flex-col h-full"> {/* Add container with flex column and full height */}
      <ChatHeader 
        title={displayName} 
        onClose={onClose}
        onBack={onBack}
        showBackButton={true}
      />
      
      <div className="flex-grow overflow-y-auto p-4 bg-gray-50">
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
                isSelected={selectedMessageId === (message.messageId || message.id)}
                onMessageClick={handleMessageClick}
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
      
      <form onSubmit={handleSendMessage} className="p-3 border-t border-gray-200 flex items-center space-x-2 mt-auto"> {/* Added mt-auto to ensure it stays at the bottom */}
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
          className=" text-[#495E57] p-2 rounded-full disabled:opacity-50"
        >
          {sending ? (
            <div className="h-6 w-6 border-2 border-t-transparent border-[#495E57] rounded-full animate-spin"></div>
          ) : (
            <svg xmlns="http://www.w3.org/2000/svg" className="h-7 w-7 transform rotate-90" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 19l9 2-9-18-9 18 9-2zm0 0v-8" />
            </svg>
          )}
        </button>
      </form>
    </div>
  );
}

export default Conversation;