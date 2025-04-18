import React, { useState, useEffect } from "react";
import axios from "axios";

const AddServicePage = () => {
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
    
    try {
      const token = localStorage.getItem('authToken') || sessionStorage.getItem('authToken');
      
      if (!token) {
        console.error("No authentication token found");
        return;
      }
      
      const userId = localStorage.getItem('userId') || sessionStorage.getItem('userId');

      if (!userId) {
        console.error("User ID not found");
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
      setFormData({
        category: "",
        name: "",
        serviceDescription: "",
        price: "",
        durationEstimate: "",
      });
    } catch (error) {
      console.error("Error adding service:", error);
    }
  };

  return (
    <div className="min-h-screen bg-gray-50 py-12 px-4 sm:px-6 lg:px-8">
      <div className="max-w-2xl mx-auto">
        <div className="text-center mb-8">
          <h1 className="text-3xl font-bold text-[#495E57]">Add Your Service</h1>
          <p className="mt-2 text-gray-600">Tell us about the service you offer</p>
        </div>

        <div className="bg-white shadow-lg rounded-lg overflow-hidden">
          <div className="bg-[#495E57] py-4 px-6">
            <h2 className="text-xl font-semibold text-white">Service Details</h2>
          </div>

          <form onSubmit={handleSubmit} className="p-6 space-y-6">
            {/* Service Category */}
            <div>
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
                className="w-full px-4 py-2 border border-gray-300 rounded-md focus:ring-[#F4CE14] focus:border-[#F4CE14] disabled:bg-gray-100"
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
            </div>

            {/* Service Name */}
            <div>
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
                className="w-full px-4 py-2 border border-gray-300 rounded-md focus:ring-[#F4CE14] focus:border-[#F4CE14]"
              />
            </div>

            {/* Service Description */}
            <div>
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
                className="w-full px-4 py-2 border border-gray-300 rounded-md focus:ring-[#F4CE14] focus:border-[#F4CE14]"
              />
            </div>

            {/* Two-column layout for Price and Duration */}
            <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
              <div>
                <label htmlFor="price" className="block text-sm font-medium text-gray-700 mb-1">
                  Price
                </label>
                <input
                  type="text"
                  id="price"
                  name="price"
                  value={formData.price}
                  onChange={handleChange}
                  required
                  className="w-full px-4 py-2 border border-gray-300 rounded-md focus:ring-[#F4CE14] focus:border-[#F4CE14]"
                  placeholder="e.g. 100"
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
                  className="w-full px-4 py-2 border border-gray-300 rounded-md focus:ring-[#F4CE14] focus:border-[#F4CE14]"
                  placeholder="e.g. 1-2 hours"
                />
              </div>
            </div>

            {/* Submit Button */}
            <div className="pt-4">
              <button
                type="submit"
                className="w-full bg-[#F4CE14] text-[#495E57] font-semibold py-3 px-4 rounded-md shadow hover:bg-yellow-400 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-yellow-500 transition-colors"
              >
                Add Service
              </button>
            </div>
          </form>
        </div>
      </div>

      {/* Confirmation Dialog */}
      {isPopupOpen && (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center p-4">
          <div className="bg-white rounded-lg max-w-md w-full overflow-hidden">
            <div className="bg-[#495E57] px-6 py-4">
              <h3 className="text-lg font-medium text-white">Confirm Service Details</h3>
            </div>
            <div className="p-6">
              <div className="space-y-4">
                <div>
                  <p className="text-sm text-gray-500">Category</p>
                  <p className="font-medium">
                    {serviceCategories.find((cat) => cat.categoryId === formData.category)?.categoryName || ""}
                  </p>
                </div>
                <div>
                  <p className="text-sm text-gray-500">Service Name</p>
                  <p className="font-medium">{formData.name}</p>
                </div>
                <div>
                  <p className="text-sm text-gray-500">Description</p>
                  <p className="font-medium">{formData.serviceDescription}</p>
                </div>
                <div className="grid grid-cols-2 gap-4">
                  <div>
                    <p className="text-sm text-gray-500">Price</p>
                    <p className="font-medium">{formData.price}</p>
                  </div>
                  <div>
                    <p className="text-sm text-gray-500">Duration</p>
                    <p className="font-medium">{formData.durationEstimate}</p>
                  </div>
                </div>
              </div>
              <div className="mt-6 grid grid-cols-2 gap-3">
                <button
                  type="button"
                  onClick={() => setIsPopupOpen(false)}
                  className="text-gray-700 bg-gray-100 px-4 py-2 rounded-md hover:bg-gray-200 focus:outline-none focus:ring-2 focus:ring-gray-300"
                >
                  Cancel
                </button>
                <button
                  type="button"
                  onClick={handleConfirm}
                  className="bg-[#F4CE14] text-[#495E57] px-4 py-2 font-medium rounded-md hover:bg-yellow-400 focus:outline-none focus:ring-2 focus:ring-yellow-400"
                >
                  Confirm
                </button>
              </div>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

export default AddServicePage;