import React from "react";
import BaseModal from "./shared/BaseModal";
import { motion } from "framer-motion";

const LogoutConfirmationPopup = ({ open, onClose, onConfirm }) => {
  return (
    <BaseModal
      isOpen={open}
      onClose={onClose}
      maxWidth="max-w-sm"
    >
      <div className="bg-white rounded-lg p-6">
        {/* Title */}
        <motion.div 
          className="mb-2 text-center"
          initial={{ opacity: 0, y: -10 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ duration: 0.3 }}
        >
          <h2 className="text-3xl font-bold text-[#495E57]">
            Logout
          </h2>
        </motion.div>

        {/* Content */}
        <motion.div 
          className="py-3"
          initial={{ opacity: 0 }}
          animate={{ opacity: 1 }}
          transition={{ delay: 0.1, duration: 0.3 }}
        >
          <p className="text-gray-600 text-center mb-4">
            Are you sure you want to log out?
          </p>
        </motion.div>

        {/* Actions */}
        <motion.div 
          className="flex justify-center gap-8 w-full"
          initial={{ opacity: 0, y: 10 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ delay: 0.2, duration: 0.3 }}
        >
          <motion.button
            onClick={onClose}
            className="w-25 px-4 py-2 bg-white border border-[#495E57] text-[#495E57] rounded-md hover:bg-gray-200 transition-colors duration-200"
            whileHover={{ scale: 1.05 }}
            whileTap={{ scale: 0.95 }}
          >
            Cancel
          </motion.button>
          <motion.button
            onClick={onConfirm}
            className="w-25 px-4 py-2 bg-[#F4CE14] hover:bg-[#e0bd13] text-gray-700 rounded-md transition-colors duration-200"
            whileHover={{ scale: 1.05 }}
            whileTap={{ scale: 0.95 }}
          >
            Confirm
          </motion.button>
        </motion.div>
      </div>
    </BaseModal>
  );
};

export default LogoutConfirmationPopup;