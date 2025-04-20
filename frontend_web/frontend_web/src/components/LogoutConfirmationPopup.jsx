import React from "react";

const LogoutConfirmationPopup = ({ open, onClose, onConfirm }) => {
  if (!open) return null;

  return (
    <div className="fixed inset-0 bg-opacity-50 flex items-center justify-center z-50 backdrop-blur-sm">
      <div className="bg-white rounded-lg shadow-xl w-full max-w-sm mx-4 p-10">
        {/* Title */}
        <div className="mb-2">
          <h2 className="text-3xl font-bold text-center text-[#495E57]">
            Logout
          </h2>
        </div>

        {/* Content */}
        <div className="py-3">
          <p className="text-gray-600 text-center mb-4">
            Are you sure you want to log out?
          </p>
        </div>

        {/* Actions */}
        <div className="flex justify-center gap-8 w-full">
        <button
          onClick={onClose}
          className="w-25 px-4 py-2 bg-white border border-[#495E57] text-[#495E57] rounded-md hover:bg-gray-200 transition-colors duration-200"
        >
          Cancel
        </button>
          <button
            onClick={onConfirm}
            className="w-25 px-4 py-2 bg-[#F4CE14] hover:bg-[#e0bd13] text-gray-700 rounded-md transition-colors duration-200"
          >
            Confirm
          </button>
        </div>
      </div>
    </div>
  );
};

export default LogoutConfirmationPopup;