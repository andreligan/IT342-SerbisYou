import React, { useState, useEffect } from "react";
import axios from "axios";
import { useNavigate } from "react-router-dom";
import { motion, AnimatePresence } from "framer-motion";

const AddServicePage = () => {
  const navigate = useNavigate();
  const [formData, setFormData] = useState({
    category: "",
    name: "",
    serviceDescription: "",
    price: "",
    durationEstimate: "",
  });

  const [serviceCategories, setServiceCategories] = useState([]);
  const [isPopupOpen, setIsPopupOpen] = useState(false);
  const [isLoading, setIsLoading] = useState(true);
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [submitSuccess, setSubmitSuccess] = useState(false);

  useEffect(() => {
    const fetchCategories = async () => {
      try {
        const token = localStorage.getItem('authToken') || sessionStorage.getItem('authToken');
        
        if (!token) {
          console.error("No authentication token found");
          setIsLoading(false);
          return;
        }
        
        const response = await axios.get("/api/service-categories/getAll", {
          headers: {
            'Authorization': `Bearer ${token}`
          }
        });
        
        setServiceCategories(response.data);
        setIsLoading(false);
      } catch (error) {
        console.error("Error fetching service categories:", error);
        setIsLoading(false);
      }
    };
  
    fetchCategories();
  }, []);

  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData({ ...formData, [name]: value });
  };

  const handleSubmit = (e) => {
    e.preventDefault();
    setIsPopupOpen(true);
  };

  const handleConfirm = async () => {
    setIsPopupOpen(false);
    setIsSubmitting(true);
    
    try {
      const token = localStorage.getItem('authToken') || sessionStorage.getItem('authToken');
      
      if (!token) {
        console.error("No authentication token found");
        setIsSubmitting(false);
        return;
      }
      
      const userId = localStorage.getItem('userId') || sessionStorage.getItem('userId');

      if (!userId) {
        console.error("User ID not found");
        setIsSubmitting(false);
        return;
      }
      
      const providerResponse = await axios.get("/api/service-providers/getAll", {
        headers: {
          'Authorization': `Bearer ${token}`
        }
      });

      console.log("Current user ID:", userId);
      console.log("Sample provider object structure:", 
        providerResponse.data.length > 0 ? JSON.stringify(providerResponse.data[0], null, 2) : "No providers found");

      const providerData = providerResponse.data.find(provider => {
        console.log("Provider object:", provider);
        return provider.userAuth?.userId == userId;
      });

      if (!providerData) {
        console.error("No service provider found for this user");
        setIsSubmitting(false);
        return;
      }

      console.log("Found provider:", providerData);
      const providerId = providerData.providerId;
      
      const serviceDetails = {
        serviceName: formData.name,
        serviceDescription: formData.serviceDescription, 
        price: formData.price,
        durationEstimate: formData.durationEstimate
      };
      
      const response = await axios.post(
        `/api/services/postService/${providerId}/${formData.category}`, 
        serviceDetails,
        {
          headers: {
            'Authorization': `Bearer ${token}`
          }
        }
      );
      
      console.log("Service added successfully:", response.data);
      
      setSubmitSuccess(true);
      
      // Show success message briefly before navigating back
      setTimeout(() => {
        // Navigate back to the previous page
        navigate(-1);
      }, 1500);
      
    } catch (error) {
      console.error("Error adding service:", error);
      setIsSubmitting(false);
    }
  };

  // Page transition variants
  const pageVariants = {
    initial: { opacity: 0, y: 20 },
    in: { opacity: 1, y: 0 },
    out: { opacity: 0, y: -20 }
  };

  // Animation transition options
  const pageTransition = {
    type: "tween",
    ease: "anticipate",
    duration: 0.5
  };

  // Success message animation variants
  const successVariants = {
    initial: { opacity: 0, scale: 0.8 },
    in: { opacity: 1, scale: 1 },
    out: { opacity: 0, scale: 0.8, transition: { duration: 0.3 } }
  };

  return (
    <AnimatePresence mode="wait">
      <motion.div
        key="add-service-page"
        className="min-h-screen bg-gradient-to-b from-gray-50 to-white py-8 px-4 overflow-x-hidden"
        initial="initial"
        animate="in"
        exit="out"
        variants={pageVariants}
        transition={pageTransition}
      >
        <motion.div 
          className="max-w-6xl mx-auto h-full"
          initial={{ opacity: 0 }}
          animate={{ opacity: 1 }}
          transition={{ duration: 0.6, delay: 0.2 }}
        >
          <AnimatePresence mode="wait">
            {submitSuccess ? (
              // Success message - centered vertically on the page
              <motion.div 
                key="success"
                className="flex items-center justify-center min-h-[80vh]"
                initial="initial"
                animate="in"
                exit="out"
                variants={successVariants}
                transition={{ type: "spring", stiffness: 300, damping: 25 }}
              >
                <motion.div 
                  className="bg-white shadow-lg rounded-lg p-8 text-center max-w-md w-full"
                  initial={{ y: 20, opacity: 0 }}
                  animate={{ y: 0, opacity: 1 }}
                  transition={{ delay: 0.2, duration: 0.5 }}
                >
                  <motion.div 
                    className="inline-flex items-center justify-center w-20 h-20 rounded-full bg-green-100 mb-6"
                    initial={{ scale: 0, rotate: -180 }}
                    animate={{ scale: 1, rotate: 0 }}
                    transition={{ type: "spring", stiffness: 260, damping: 20, delay: 0.3 }}
                  >
                    <svg className="w-10 h-10 text-green-600" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                      <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M5 13l4 4L19 7" />
                    </svg>
                  </motion.div>
                  
                  <motion.h2 
                    className="text-2xl font-semibold text-gray-800 mb-3"
                    initial={{ opacity: 0, y: 10 }}
                    animate={{ opacity: 1, y: 0 }}
                    transition={{ delay: 0.4 }}
                  >
                    Service Added Successfully!
                  </motion.h2>
                  
                  <motion.p 
                    className="text-gray-600 mb-6"
                    initial={{ opacity: 0 }}
                    animate={{ opacity: 1 }}
                    transition={{ delay: 0.5 }}
                  >
                    Your service is now available for customers to discover and book.
                  </motion.p>
                  
                  <motion.div 
                    className="flex justify-center"
                    initial={{ opacity: 0 }}
                    animate={{ opacity: 1 }}
                    transition={{ delay: 0.6 }}
                  >
                    <div className="w-8 h-8 border-4 border-green-500 border-t-transparent rounded-full animate-spin"></div>
                  </motion.div>
                  
                  <motion.p 
                    className="text-sm text-gray-500 mt-4"
                    initial={{ opacity: 0 }}
                    animate={{ opacity: 1 }}
                    transition={{ delay: 0.7 }}
                  >
                    Redirecting you back...
                  </motion.p>
                </motion.div>
              </motion.div>
            ) : (
              // Normal layout for the form
              <motion.div 
                key="form"
                className="flex flex-col lg:flex-row gap-8"
                initial={{ opacity: 0 }}
                animate={{ opacity: 1 }}
                exit={{ opacity: 0 }}
              >
                {/* Left sidebar with back button and title */}
                <motion.div 
                  className="lg:sticky lg:top-8 lg:self-start lg:w-1/3 p-6"
                  initial={{ opacity: 0, x: -40 }}
                  animate={{ opacity: 1, x: 0 }}
                  transition={{ duration: 0.5 }}
                >
                  <motion.button 
                    onClick={() => navigate(-1)} 
                    className="text-gray-600 hover:text-gray-900 flex items-center group transition duration-300 mb-8"
                    whileHover={{ x: -5 }}
                    whileTap={{ scale: 0.95 }}
                  >
                    <svg xmlns="http://www.w3.org/2000/svg" className="h-5 w-5 mr-2 transform group-hover:-translate-x-1 transition-transform" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                      <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M10 19l-7-7m0 0l7-7m-7 7h18" />
                    </svg>
                    Back
                  </motion.button>

                  <motion.div 
                    initial={{ opacity: 0, y: 10 }}
                    animate={{ opacity: 1, y: 0 }}
                    transition={{ duration: 0.5, delay: 0.2 }}
                    className="mb-6"
                  >
                    <h1 className="text-3xl font-bold text-[#495E57] mb-4">Add Your Service</h1>
                    <p className="text-gray-600">Create a new service offering to showcase your skills and expertise to potential customers</p>
                  </motion.div>

                  {/* Additional info card - optional */}
                  <motion.div
                    className="bg-white p-6 rounded-xl shadow-md mt-8 border-l-4 border-[#F4CE14] hidden lg:block"
                    initial={{ opacity: 0, y: 20 }}
                    animate={{ opacity: 1, y: 0 }}
                    transition={{ duration: 0.5, delay: 0.4 }}
                  >
                    <h3 className="font-medium text-gray-800 mb-2">Tips for a Great Service</h3>
                    <ul className="text-sm text-gray-600 space-y-2">
                      <li className="flex items-start">
                        <svg className="h-5 w-5 text-[#F4CE14] mr-2 flex-shrink-0" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                          <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M5 13l4 4L19 7" />
                        </svg>
                        <span>Be specific about what your service includes</span>
                      </li>
                      <li className="flex items-start">
                        <svg className="h-5 w-5 text-[#F4CE14] mr-2 flex-shrink-0" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                          <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M5 13l4 4L19 7" />
                        </svg>
                        <span>Set clear expectations for duration and pricing</span>
                      </li>
                      <li className="flex items-start">
                        <svg className="h-5 w-5 text-[#F4CE14] mr-2 flex-shrink-0" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                          <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M5 13l4 4L19 7" />
                        </svg>
                        <span>Highlight your unique skills and experience</span>
                      </li>
                    </ul>
                  </motion.div>
                </motion.div>

                {/* Right content - form area */}
                <motion.div 
                  className="lg:w-2/3"
                  initial={{ opacity: 0, x: 40 }}
                  animate={{ opacity: 1, x: 0 }}
                  transition={{ duration: 0.5, delay: 0.1 }}
                >
                  <motion.div 
                    className="bg-white shadow-xl rounded-lg overflow-hidden border border-gray-100"
                    initial={{ opacity: 0, y: 20 }}
                    animate={{ opacity: 1, y: 0 }}
                    transition={{ duration: 0.5, delay: 0.2 }}
                    whileHover={{ boxShadow: "0 10px 25px -5px rgba(0, 0, 0, 0.1), 0 10px 10px -5px rgba(0, 0, 0, 0.04)" }}
                  >
                    <div className="bg-gradient-to-r from-[#495E57] to-[#3e4f49] py-5 px-6">
                      <h2 className="text-xl font-semibold text-white flex items-center">
                        <svg xmlns="http://www.w3.org/2000/svg" className="h-6 w-6 mr-2" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                          <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 6v6m0 0v6m0-6h6m-6 0H6" />
                        </svg>
                        Service Details
                      </h2>
                    </div>

                    <form onSubmit={handleSubmit} className="p-6 space-y-6">
                      <motion.div
                        initial={{ opacity: 0, y: 15 }}
                        animate={{ opacity: 1, y: 0 }}
                        transition={{ duration: 0.3, delay: 0.3 }}
                      >
                        <label htmlFor="category" className="block text-sm font-medium text-gray-700 mb-1">
                          Service Category
                        </label>
                        <select
                          id="category"
                          name="category"
                          value={formData.category}
                          onChange={handleChange}
                          required
                          disabled={isLoading}
                          className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-[#F4CE14] focus:border-[#F4CE14] disabled:bg-gray-100 shadow-sm transition-all"
                        >
                          <option value="" disabled>
                            {isLoading ? "Loading categories..." : "Select a category"}
                          </option>
                          {serviceCategories.map((category) => (
                            <option key={category.categoryId} value={category.categoryId}>
                              {category.categoryName}
                            </option>
                          ))}
                        </select>
                      </motion.div>

                      <motion.div
                        initial={{ opacity: 0, y: 15 }}
                        animate={{ opacity: 1, y: 0 }}
                        transition={{ duration: 0.3, delay: 0.4 }}
                      >
                        <label htmlFor="name" className="block text-sm font-medium text-gray-700 mb-1">
                          Service Name
                        </label>
                        <input
                          type="text"
                          id="name"
                          name="name"
                          value={formData.name}
                          onChange={handleChange}
                          required
                          placeholder="Enter a descriptive name for your service"
                          className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-[#F4CE14] focus:border-[#F4CE14] shadow-sm transition-all"
                        />
                      </motion.div>

                      <motion.div
                        initial={{ opacity: 0, y: 15 }}
                        animate={{ opacity: 1, y: 0 }}
                        transition={{ duration: 0.3, delay: 0.5 }}
                      >
                        <label htmlFor="serviceDescription" className="block text-sm font-medium text-gray-700 mb-1">
                          Service Description
                        </label>
                        <textarea
                          id="serviceDescription"
                          name="serviceDescription"
                          value={formData.serviceDescription}
                          onChange={handleChange}
                          required
                          rows={4}
                          placeholder="Describe what your service includes, your expertise, and what customers can expect"
                          className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-[#F4CE14] focus:border-[#F4CE14] shadow-sm transition-all resize-none"
                        />
                      </motion.div>

                      <motion.div 
                        className="grid grid-cols-1 md:grid-cols-2 gap-6"
                        initial={{ opacity: 0, y: 15 }}
                        animate={{ opacity: 1, y: 0 }}
                        transition={{ duration: 0.3, delay: 0.6 }}
                      >
                        <div>
                          <label htmlFor="price" className="block text-sm font-medium text-gray-700 mb-1">
                            Price (₱)
                          </label>
                          <input
                            type="text"
                            id="price"
                            name="price"
                            value={formData.price}
                            onChange={handleChange}
                            required
                            className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-[#F4CE14] focus:border-[#F4CE14] shadow-sm transition-all"
                            placeholder="e.g. 500"
                          />
                        </div>

                        <div>
                          <label htmlFor="durationEstimate" className="block text-sm font-medium text-gray-700 mb-1">
                            Duration Estimate
                          </label>
                          <input
                            type="text"
                            id="durationEstimate"
                            name="durationEstimate"
                            value={formData.durationEstimate}
                            onChange={handleChange}
                            required
                            className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-[#F4CE14] focus:border-[#F4CE14] shadow-sm transition-all"
                            placeholder="e.g. 1-2 hours"
                          />
                        </div>
                      </motion.div>

                      <motion.div 
                        className="pt-6"
                        initial={{ opacity: 0, y: 15 }}
                        animate={{ opacity: 1, y: 0 }}
                        transition={{ duration: 0.3, delay: 0.7 }}
                      >
                        <motion.button
                          type="submit"
                          disabled={isSubmitting}
                          className={`w-full font-semibold py-4 px-6 rounded-lg transition-all ${
                            isSubmitting 
                              ? "bg-gray-300 text-gray-700 cursor-not-allowed" 
                              : "bg-[#F4CE14] text-[#495E57] hover:bg-yellow-400 hover:shadow-lg focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-yellow-500"
                          }`}
                          whileHover={!isSubmitting ? { scale: 1.02 } : {}}
                          whileTap={!isSubmitting ? { scale: 0.98 } : {}}
                        >
                          {isSubmitting ? (
                            <span className="flex items-center justify-center">
                              <svg className="animate-spin -ml-1 mr-3 h-5 w-5 text-gray-700" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24">
                                <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4"></circle>
                                <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
                              </svg>
                              Adding Service...
                            </span>
                          ) : "Add Service"}
                        </motion.button>
                      </motion.div>
                    </form>
                  </motion.div>
                </motion.div>
              </motion.div>
            )}
          </AnimatePresence>
        </motion.div>

        {/* Confirmation Dialog */}
        <AnimatePresence>
          {isPopupOpen && (
            <motion.div 
              className="fixed inset-0 backdrop-blur-sm bg-gray-500/30 flex items-center justify-center p-4 z-50"
              initial={{ opacity: 0 }}
              animate={{ opacity: 1 }}
              exit={{ opacity: 0 }}
            >
              <motion.div 
                className="bg-white rounded-xl max-w-md w-full overflow-hidden shadow-2xl border border-gray-100"
                initial={{ scale: 0.9, opacity: 0, y: 20 }}
                animate={{ scale: 1, opacity: 1, y: 0 }}
                exit={{ scale: 0.9, opacity: 0, y: 20 }}
                transition={{ 
                  type: "spring", 
                  stiffness: 300, 
                  damping: 30,
                  duration: 0.4
                }}
              >
                <motion.div 
                  className="bg-gradient-to-r from-[#495E57] to-[#3e4f49] px-6 py-4"
                  initial={{ opacity: 0, x: -20 }}
                  animate={{ opacity: 1, x: 0 }}
                  transition={{ delay: 0.1, duration: 0.3 }}
                >
                  <h3 className="text-lg font-medium text-white flex items-center">
                    <svg xmlns="http://www.w3.org/2000/svg" className="h-5 w-5 mr-2" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                      <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 5H7a2 2 0 00-2 2v12a2 2 0 002 2h10a2 2 0 002-2V7a2 2 0 00-2-2h-2M9 5a2 2 0 002 2h2a2 2 0 002-2M9 5a2 2 0 012-2h2a2 2 0 012 2" />
                    </svg>
                    Confirm Service Details
                  </h3>
                </motion.div>
                <div className="p-6">
                  <div className="space-y-5">
                    <motion.div
                      initial={{ opacity: 0, y: 10 }}
                      animate={{ opacity: 1, y: 0 }}
                      transition={{ delay: 0.2, duration: 0.3 }}
                    >
                      <p className="text-sm text-gray-500 font-medium mb-1">Category</p>
                      <p className="font-semibold text-gray-800">
                        {serviceCategories.find((cat) => cat.categoryId === formData.category)?.categoryName || ""}
                      </p>
                    </motion.div>
                    
                    <motion.div
                      initial={{ opacity: 0, y: 10 }}
                      animate={{ opacity: 1, y: 0 }}
                      transition={{ delay: 0.3, duration: 0.3 }}
                    >
                      <p className="text-sm text-gray-500 font-medium mb-1">Service Name</p>
                      <p className="font-semibold text-gray-800">{formData.name}</p>
                    </motion.div>
                    
                    <motion.div
                      initial={{ opacity: 0, y: 10 }}
                      animate={{ opacity: 1, y: 0 }}
                      transition={{ delay: 0.4, duration: 0.3 }}
                    >
                      <p className="text-sm text-gray-500 font-medium mb-1">Description</p>
                      <p className="font-medium text-gray-700 line-clamp-3">{formData.serviceDescription}</p>
                    </motion.div>
                    
                    <motion.div 
                      className="grid grid-cols-2 gap-4"
                      initial={{ opacity: 0, y: 10 }}
                      animate={{ opacity: 1, y: 0 }}
                      transition={{ delay: 0.5, duration: 0.3 }}
                    >
                      <div>
                        <p className="text-sm text-gray-500 font-medium mb-1">Price</p>
                        <p className="font-semibold text-gray-800">₱{formData.price}</p>
                      </div>
                      <div>
                        <p className="text-sm text-gray-500 font-medium mb-1">Duration</p>
                        <p className="font-semibold text-gray-800">{formData.durationEstimate}</p>
                      </div>
                    </motion.div>
                  </div>
                  
                  <motion.div 
                    className="mt-8 grid grid-cols-2 gap-4"
                    initial={{ opacity: 0, y: 10 }}
                    animate={{ opacity: 1, y: 0 }}
                    transition={{ delay: 0.6, duration: 0.3 }}
                  >
                    <motion.button
                      type="button"
                      onClick={() => setIsPopupOpen(false)}
                      className="flex justify-center items-center text-gray-700 bg-gray-100 px-4 py-3 rounded-lg hover:bg-gray-200 focus:outline-none focus:ring-2 focus:ring-gray-300 transition-colors font-medium"
                      whileHover={{ scale: 1.03 }}
                      whileTap={{ scale: 0.97 }}
                    >
                      <svg xmlns="http://www.w3.org/2000/svg" className="h-5 w-5 mr-2" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M6 18L18 6M6 6l12 12" />
                      </svg>
                      Cancel
                    </motion.button>
                    <motion.button
                      type="button"
                      onClick={handleConfirm}
                      className="flex justify-center items-center bg-[#F4CE14] text-[#495E57] px-4 py-3 font-medium rounded-lg hover:bg-yellow-400 focus:outline-none focus:ring-2 focus:ring-yellow-400 transition-colors"
                      whileHover={{ scale: 1.03 }}
                      whileTap={{ scale: 0.97 }}
                    >
                      <svg xmlns="http://www.w3.org/2000/svg" className="h-5 w-5 mr-2" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M5 13l4 4L19 7" />
                      </svg>
                      Confirm
                    </motion.button>
                  </motion.div>
                </div>
              </motion.div>
            </motion.div>
          )}
        </AnimatePresence>
      </motion.div>
    </AnimatePresence>
  );
};

export default AddServicePage;