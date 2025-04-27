import React, { useState, useEffect, useRef } from 'react';
import { motion, AnimatePresence } from 'framer-motion';
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

  // Animation variants for the chat window
  const chatWindowVariants = {
    hidden: { opacity: 0, y: 20, scale: 0.95 },
    visible: { 
      opacity: 1, 
      y: 0, 
      scale: 1,
      transition: {
        type: "spring",
        damping: 25,
        stiffness: 300
      }
    },
    exit: { 
      opacity: 0, 
      y: 20, 
      scale: 0.95,
      transition: { 
        duration: 0.2 
      }
    }
  };

  // Animation variants for view transitions - ONLY for content
  const contentVariants = {
    hidden: { opacity: 0, x: 20 },
    visible: { 
      opacity: 1, 
      x: 0,
      transition: {
        type: "tween",
        ease: "easeOut",
        duration: 0.3
      }
    },
    exit: { 
      opacity: 0, 
      x: -20,
      transition: { 
        duration: 0.2 
      }
    }
  };

  return (
    <motion.div 
      ref={chatWindowRef}
      className="fixed bottom-6 right-25 z-10 w-105 h-5/6 bg-white rounded-lg shadow-xl flex flex-col overflow-hidden"
      initial="hidden"
      animate="visible"
      exit="exit"
      variants={chatWindowVariants}
    >
      {/* The ChatHeader is now OUTSIDE the AnimatePresence to keep it static */}
      {view === 'conversation' ? (
        // For conversation view, only render Conversation component with its own header
        <motion.div 
          key="conversation"
          initial="hidden"
          animate="visible"
          exit="exit"
          variants={contentVariants}
          className="flex-1 h-full overflow-hidden" // Ensure full height and prevent overflow
        >
          <Conversation 
            user={selectedUser} 
            messages={selectedUser?.messages?.length > 0 ? selectedUser.messages : []}
            onBack={handleBackToList}
            onClose={onClose}
            onMessageSent={() => setRefreshList(prev => prev + 1)}
          />
        </motion.div>
      ) : (
        // For other views (list/search), keep the current structure
        <>
          <ChatHeader 
            title={view === 'search' ? 'Search Users' : 'Chat'} 
            onClose={onClose} 
            searchQuery={searchQuery}
            onSearchChange={(e) => setSearchQuery(e.target.value)}
          />
          
          <AnimatePresence mode="wait">
            {view === 'search' ? (
              <motion.div
                key="search"
                initial="hidden"
                animate="visible"
                exit="exit"
                variants={contentVariants}
                className="flex-1 overflow-hidden"
              >
                <UserSearch 
                  onSelectUser={handleUserSelect}
                  searchQuery={searchQuery}
                />
              </motion.div>
            ) : (
              <motion.div
                key="list"
                initial="hidden"
                animate="visible"
                exit="exit"
                variants={contentVariants}
                className="flex-1 overflow-hidden"
              >
                <ChatList 
                  onSelectUser={handleUserSelect}
                  searchQuery=""
                  key={refreshList} // Add key to force refresh when sending a message
                />
              </motion.div>
            )}
          </AnimatePresence>
        </>
      )}
    </motion.div>
  );
}

export default ChatWindow;