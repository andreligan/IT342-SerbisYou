import React from 'react';
import { useNavigate } from 'react-router-dom';
import GoogleIcon from '../assets/google-icon.svg';
import BaseModal from './shared/BaseModal';
import { motion } from 'framer-motion';

const SignupOptionsPopup = ({ open, onClose }) => {
  const navigate = useNavigate();

  const handleGoogleSignup = () => {
    // Redirect to Google OAuth2 signup endpoint
    window.location.href = 'http://localhost:8080/oauth2/authorization/google';
  };

  const handleManualSignup = () => {
    onClose();
    navigate('/signup/type');
  };

  return (
    <BaseModal
      isOpen={open}
      onClose={onClose}
      maxWidth="max-w-md"
    >
      {/* Dialog Header */}
      <div className="relative pt-6 px-6 pb-2 text-center">
        <button 
          onClick={onClose}
          className="absolute right-4 top-4 text-yellow-500 hover:text-yellow-700"
          aria-label="close"
        >
          <svg xmlns="http://www.w3.org/2000/svg" className="h-6 w-6" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M6 18L18 6M6 6l12 12" />
          </svg>
        </button>
        <motion.h2 
          className="text-3xl font-bold text-[#495E57]"
          initial={{ opacity: 0, y: -10 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ delay: 0.1 }}
        >
          Sign Up for Serbisyo
        </motion.h2>
        <motion.p 
          className="mt-1 text-gray-500"
          initial={{ opacity: 0 }}
          animate={{ opacity: 1 }}
          transition={{ delay: 0.2 }}
        >
          How you'd like to create your account
        </motion.p>
      </div>
      
      {/* Dialog Content */}
      <div className="px-6 py-4 mt-4">
        <motion.button
          onClick={handleGoogleSignup}
          className="h-12 w-full flex items-center justify-center border border-gray-300 hover:bg-gray-50 text-gray-700 py-2 px-4 rounded-md"
          initial={{ opacity: 0, y: 10 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ delay: 0.3 }}
          whileHover={{ scale: 1.02, backgroundColor: "rgba(0, 0, 0, 0.05)" }}
          whileTap={{ scale: 0.98 }}
        >
          <img src={GoogleIcon} alt="Google" className="w-5 h-5 mr-2" />
          Continue with Google
        </motion.button>
        
        <div className="relative flex py-4 items-center my-2">
          <div className="flex-grow border-t border-gray-300"></div>
          <span className="flex-shrink mx-4 text-gray-400">or</span>
          <div className="flex-grow border-t border-gray-300"></div>
        </div>
        
        <motion.button
          onClick={handleManualSignup}
          className="mb-4 w-full bg-[#F4CE14] hover:bg-[#e0bd13] text-gray-700 py-3 rounded-md font-medium focus:outline-none focus:ring-2 focus:ring-yellow-600 focus:ring-opacity-50"
          initial={{ opacity: 0, y: 10 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ delay: 0.4 }}
          whileHover={{ scale: 1.02 }}
          whileTap={{ scale: 0.98 }}
        >
          Continue with Email
        </motion.button>
        
        <motion.div 
          className="text-center mt-4"
          initial={{ opacity: 0 }}
          animate={{ opacity: 1 }}
          transition={{ delay: 0.5 }}
        >
          <p className="text-gray-700">
            Already have an account?{' '}
            <button 
              onClick={onClose}
              className="text-yellow-600 hover:text-yellow-800 font-medium"
            >
              Sign In
            </button>
          </p>
        </motion.div>
      </div>
    </BaseModal>
  );
};

export default SignupOptionsPopup;