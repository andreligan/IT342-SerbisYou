import React from 'react';

function MessageBubble({ message, isCurrentUser }) {
  const formatTime = (dateTime) => {
    if (!dateTime) return '';
    const date = new Date(dateTime);
    return date.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' });
  };

  return (
    <div className={`flex ${isCurrentUser ? 'justify-end' : 'justify-start'}`}>
      <div className={`max-w-xs lg:max-w-md rounded-lg py-2 px-3 ${
        isCurrentUser 
          ? 'bg-[#F4CE14] text-[#495E57] rounded-br-none' 
          : 'bg-white border border-gray-200 rounded-bl-none'
      }`}>
        <p className="text-sm">{message.messageText}</p>
        <span className={`text-xs mt-1 block text-right ${
          isCurrentUser ? 'text-[#495E57]/70' : 'text-gray-500'
        }`}>
          {formatTime(message.sentAt)}
        </span>
      </div>
    </div>
  );
}

export default MessageBubble;