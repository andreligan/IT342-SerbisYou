import React from 'react';

function ChatHeader({ title, onClose, searchQuery, onSearchChange, onBack, showBackButton }) {
  return (
    <div className="bg-white border-b border-gray-200 p-4 flex items-center justify-between">
      <div className="flex items-center space-x-2">
        {showBackButton && (
          <button onClick={onBack} className="p-1">
            <svg xmlns="http://www.w3.org/2000/svg" className="h-5 w-5 text-[#495E57]" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M15 19l-7-7 7-7" />
            </svg>
          </button>
        )}
        <h2 className="font-medium text-lg text-[#495E57]">{title}</h2>
      </div>
      
      <div className="flex items-center space-x-2">
        {onSearchChange && (
          <div className="relative">
            <input
              type="text"
              placeholder="Search name"
              value={searchQuery}
              onChange={onSearchChange}
              className="pl-8 pr-4 py-1 border border-gray-300 rounded-md text-sm focus:outline-none focus:ring-1 focus:ring-[#F4CE14]"
            />
            <svg xmlns="http://www.w3.org/2000/svg" className="h-4 w-4 text-gray-400 absolute left-2 top-2" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z" />
            </svg>
          </div>
        )}
        
        <button onClick={onClose} className="p-1">
          <svg xmlns="http://www.w3.org/2000/svg" className="h-5 w-5 text-[#495E57]" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M6 18L18 6M6 6l12 12" />
          </svg>
        </button>
      </div>
    </div>
  );
}

export default ChatHeader;