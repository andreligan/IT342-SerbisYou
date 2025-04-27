import React, { useEffect } from 'react';
import { motion, AnimatePresence } from 'framer-motion';

/**
 * BaseModal - Reusable modal component with standardized animations
 * 
 * @param {boolean} isOpen - Controls whether the modal is visible
 * @param {function} onClose - Function to call when closing the modal
 * @param {ReactNode} children - Content to display inside the modal
 * @param {string} maxWidth - Maximum width of the modal content (CSS value)
 * @param {object} contentProps - Additional props for the content wrapper
 */
const BaseModal = ({ 
  isOpen, 
  onClose, 
  children, 
  maxWidth = "max-w-md",
  contentProps = {},
  clickPosition = null,
}) => {
  // Trap focus within modal when open
  useEffect(() => {
    if (isOpen) {
      document.body.style.overflow = 'hidden';
    } else {
      document.body.style.overflow = 'auto';
    }
    
    return () => {
      document.body.style.overflow = 'auto';
    };
  }, [isOpen]);
  
  // Handle ESC key to close modal
  useEffect(() => {
    const handleEsc = (e) => {
      if (e.key === 'Escape') onClose();
    };
    
    if (isOpen) {
      document.addEventListener('keydown', handleEsc);
    }
    
    return () => {
      document.removeEventListener('keydown', handleEsc);
    };
  }, [isOpen, onClose]);

  // Animation variants
  const backdropVariants = {
    hidden: { opacity: 0 },
    visible: { 
      opacity: 1,
      transition: { duration: 0.3 }
    },
    exit: { 
      opacity: 0,
      transition: { duration: 0.3 }
    }
  };

  const modalVariants = {
    hidden: clickPosition 
      ? { 
          opacity: 0, 
          scale: 0.5, 
          x: clickPosition.x, 
          y: clickPosition.y 
        }
      : { 
          opacity: 0, 
          scale: 0.8,
          y: 20 
        },
    visible: { 
      opacity: 1, 
      scale: 1, 
      x: 0, 
      y: 0,
      transition: { 
        type: "spring",
        stiffness: 300,
        damping: 30,
        delay: 0.1
      }
    },
    exit: clickPosition
      ? {
          opacity: 0,
          scale: 0.5,
          x: clickPosition.x,
          y: clickPosition.y,
          transition: {
            type: "spring",
            stiffness: 300,
            damping: 25,
            duration: 0.3
          }
        }
      : { 
          opacity: 0, 
          scale: 0.8,
          y: 20,
          transition: { 
            type: "spring",
            stiffness: 300,
            damping: 25,
            duration: 0.3
          }
        }
  };

  return (
    <AnimatePresence mode="wait">
      {isOpen && (
        <div className="fixed inset-0 z-50 overflow-y-auto">
          <div className="flex items-center justify-center min-h-screen px-4 pt-4 pb-20 text-center sm:p-0">
            {/* Backdrop with blur effect */}
            <motion.div
              className="fixed inset-0 backdrop-blur-sm bg-gray-900/70 transition-opacity"
              initial="hidden"
              animate="visible"
              exit="exit"
              variants={backdropVariants}
              onClick={onClose}
            />

            {/* Modal container - vertically and horizontally centered */}
            <motion.div
              className={`relative inline-block w-full ${maxWidth} overflow-hidden rounded-lg bg-white text-left align-bottom shadow-xl sm:align-middle`}
              role="dialog"
              aria-modal="true"
              initial="hidden"
              animate="visible"
              exit="exit"
              variants={modalVariants}
              onClick={(e) => e.stopPropagation()}
              {...contentProps}
            >
              {children}
            </motion.div>
          </div>
        </div>
      )}
    </AnimatePresence>
  );
};

export default BaseModal;
