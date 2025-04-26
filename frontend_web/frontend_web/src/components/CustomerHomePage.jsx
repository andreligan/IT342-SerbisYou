import React, { useState, useEffect } from "react";
import axios from "axios";
import Footer from "./Footer";
import { Link, useNavigate } from "react-router-dom";
import { motion } from "framer-motion";
import plumbing from "../assets/plumbing.jpg";
import electrical from "../assets/electrical.jpg";
import cleaning from "../assets/cleaning.jpg";
import applianceRepair from "../assets/appliance repair.jpg";
import homePainting from "../assets/home painting.jpg";
import carpentry from "../assets/carpentry.jpg";
import movingPacking from "../assets/home moving.jpg";
import handyman from "../assets/handyman.jpeg";
import lawnCare from "../assets/lawn care.jpg";
import serviceImage1 from "../assets/appliance repair.jpg";
import serviceImage2 from "../assets/carpentry.jpg";
import serviceImage3 from "../assets/cleaning.jpg";
import API from "../utils/API";

function CustomerHomePage() {
  const navigate = useNavigate();

  const [categories, setCategories] = useState([]); 
  const [isLoading, setIsLoading] = useState(true); 
  const [page, setPage] = useState(1);

  const itemsPerPage = 5;

  // Animation variants
  const pageVariants = {
    initial: { opacity: 0 },
    animate: { 
      opacity: 1,
      transition: {
        staggerChildren: 0.1,
      }
    }
  };

  const itemVariants = {
    initial: { opacity: 0, y: 20 },
    animate: { 
      opacity: 1, 
      y: 0,
      transition: {
        type: "spring",
        stiffness: 100,
        damping: 10
      }
    }
  };

  // Fallback mapping for category images
  const categoryImageMap = {
    "Plumbing Services": plumbing,
    "Electrical Services": electrical,
    "Cleaning Services": cleaning,
    "Appliance Repair": applianceRepair,
    "Home Painting": homePainting,
    "Carpentry Services": carpentry,
    "Moving & Packing Services": movingPacking,
    "Handyman Services": handyman,
    "Lawn Care & Gardening": lawnCare,
  };

  // Fetch categories from the backend
  useEffect(() => {
    const fetchCategories = async () => {
      try {
        const token = localStorage.getItem("authToken") || sessionStorage.getItem("authToken");
        if (!token) {
          console.error("No authentication token found");
          setIsLoading(false);
          return;
        }

        const response = await API.get("/api/service-categories/getAll");
        setCategories(response.data);
        setIsLoading(false);
      } catch (error) {
        console.error("Error fetching categories:", error);
        setIsLoading(false);
      }
    };

    fetchCategories();
  }, []);

  const startIndex = (page - 1) * itemsPerPage;
  const endIndex = startIndex + itemsPerPage;
  const currentCategories = categories.slice(startIndex, endIndex);

  const handlePageChange = (newPage) => {
    setPage(newPage);
  };

  return (
    <motion.div
      initial="initial"
      animate="animate"
      variants={pageVariants}
    >
      {/* Hero Section */}
      <motion.div 
        className="flex relative h-[50vh]"
        variants={itemVariants}
      >
        {/* Left Content */}
        <div className="flex-1 bg-[#495E57] flex flex-col justify-center items-center p-10 z-10">
          <motion.h1 
            className="font-bold text-3xl md:text-4xl text-[#F4CE14] mb-2 text-center"
            variants={itemVariants}
            initial={{ opacity: 0, y: -20 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ delay: 0.2 }}
          >
            Welcome, Our Dear Customer!
          </motion.h1>
          <motion.p 
            className="text-white mb-6 text-center max-w-md"
            variants={itemVariants}
            initial={{ opacity: 0 }}
            animate={{ opacity: 1 }}
            transition={{ delay: 0.3 }}
          >
            Helpful services to ease your stress are here. Start connecting with reliable service providers today!
          </motion.p>
          <motion.button
            className="bg-[#F4CE14] hover:bg-[#F4CE14] text-[#495E57] py-2 px-6 rounded-md transition-all duration-300 shadow-md hover:shadow-lg"
            onClick={() => navigate("/browseServices")}
            variants={itemVariants}
            initial={{ opacity: 0, y: 20 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ delay: 0.4 }}
            whileHover={{ scale: 1.05 }}
            whileTap={{ scale: 0.95 }}
          >
            Browse Services
          </motion.button>
        </div>
        
        {/* Right Content - Slideshow */}
        <div className="flex-1 relative overflow-hidden">
          <img 
            src={serviceImage1} 
            alt="Service 1" 
            className="w-full h-full object-cover absolute animate-fade"
          />
          <img 
            src={serviceImage2} 
            alt="Service 2" 
            className="w-full h-full object-cover absolute animate-fade-delayed-1"
            style={{ animationDelay: "3.3s" }}
          />
          <img 
            src={serviceImage3} 
            alt="Service 3" 
            className="w-full h-full object-cover absolute animate-fade-delayed-2"
            style={{ animationDelay: "6.6s" }}
          />
        </div>
      </motion.div>

      {/* Featured Home Categories Section */}
      <motion.div 
        className="py-16 px-4 text-center bg-gray-50"
        variants={itemVariants}
        initial={{ opacity: 0 }}
        animate={{ opacity: 1 }}
        transition={{ delay: 0.2 }}
      >
        <motion.h2 
          className="text-3xl font-bold text-[#495E57] mb-10"
          initial={{ opacity: 0, y: -10 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ delay: 0.3 }}
        >
          Featured Home Categories
        </motion.h2>
        
        {isLoading ? (
          <motion.div 
            className="flex justify-center items-center py-12"
            initial={{ opacity: 0 }}
            animate={{ opacity: 1 }}
          >
            <div className="w-12 h-12 border-4 border-[#F4CE14] border-t-transparent rounded-full animate-spin"></div>
            <span className="ml-3 text-gray-600">Loading categories...</span>
          </motion.div>
        ) : (
          <div className="flex items-center justify-center gap-4 max-w-6xl mx-auto">
            <motion.button
              onClick={() => handlePageChange(page - 1)}
              disabled={page === 1}
              className={`w-10 h-10 flex items-center justify-center rounded-full ${
                page === 1 ? 'bg-gray-200 text-gray-400 cursor-not-allowed' : 'bg-[#495E57] text-white hover:bg-[#3e4f49] transition-colors'
              }`}
              whileHover={page !== 1 ? { scale: 1.1 } : {}}
              whileTap={page !== 1 ? { scale: 0.9 } : {}}
            >
              &lt;
            </motion.button>

            <div className="grid grid-cols-2 sm:grid-cols-3 md:grid-cols-5 gap-4 md:gap-6 flex-1">
              {currentCategories.map((category, index) => (
                <motion.div 
                  key={category.categoryId || index}
                  initial={{ opacity: 0, y: 20 }}
                  animate={{ opacity: 1, y: 0 }}
                  transition={{ delay: 0.1 * (index + 1) }}
                  whileHover={{ y: -5, transition: { duration: 0.2 } }}
                >
                  <Link to={`/browseServices?category=${category.categoryId}`} className="block">
                    <motion.div 
                      className="w-20 h-20 md:w-24 md:h-24 rounded-full mx-auto bg-cover bg-center shadow-md border-2 border-white"
                      style={{ 
                        backgroundImage: `url(${category.image || categoryImageMap[category.categoryName] || "/default-category.jpg"})` 
                      }}
                      whileHover={{ scale: 1.05, boxShadow: "0 8px 15px rgba(0,0,0,0.1)" }}
                    />
                    <p className="mt-3 text-sm font-medium text-gray-800">{category.categoryName}</p>
                  </Link>
                </motion.div>
              ))}
            </div>

            <motion.button
              onClick={() => handlePageChange(page + 1)}
              disabled={page === Math.ceil(categories.length / itemsPerPage)}
              className={`w-10 h-10 flex items-center justify-center rounded-full ${
                page === Math.ceil(categories.length / itemsPerPage) 
                  ? 'bg-gray-200 text-gray-400 cursor-not-allowed' 
                  : 'bg-[#495E57] text-white hover:bg-[#3e4f49] transition-colors'
              }`}
              whileHover={page !== Math.ceil(categories.length / itemsPerPage) ? { scale: 1.1 } : {}}
              whileTap={page !== Math.ceil(categories.length / itemsPerPage) ? { scale: 0.9 } : {}}
            >
              &gt;
            </motion.button>
          </div>
        )}

        <motion.div 
          className="mt-8 flex justify-center"
          initial={{ opacity: 0 }}
          animate={{ opacity: 1 }}
          transition={{ delay: 0.6 }}
        >
          <span className="text-sm bg-gray-200 px-4 py-1 rounded-full font-medium text-gray-700">
            Page {page} of {Math.max(1, Math.ceil(categories.length / itemsPerPage))}
          </span>
        </motion.div>

        <motion.div 
          className="mt-12"
          initial={{ opacity: 0, y: 20 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ delay: 0.7 }}
        >
          <Link 
            to="/browseServices"
            className="inline-flex items-center px-6 py-3 bg-[#F4CE14] hover:bg-[#e0b813] text-[#333] rounded-lg shadow-md transition-all hover:shadow-lg font-medium"
          >
            View All Services
            <svg className="ml-2 w-5 h-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M14 5l7 7m0 0l-7 7m7-7H3" />
            </svg>
          </Link>
        </motion.div>
      </motion.div>

      {/* Add animation keyframes */}
      <style jsx>{`
        @keyframes fade {
          0% { opacity: 1; }
          33% { opacity: 0; }
          66% { opacity: 1; }
          100% { opacity: 0; }
        }
        .animate-fade {
          animation: fade 10s infinite;
        }
        .animate-fade-delayed-1 {
          animation: fade 10s infinite;
          animation-delay: 3.3s;
        }
        .animate-fade-delayed-2 {
          animation: fade 10s infinite;
          animation-delay: 6.6s;
        }
      `}</style>

      {/* Footer */}
      <Footer />
    </motion.div>
  );
}

export default CustomerHomePage;