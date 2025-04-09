import React, { useState, useEffect } from "react";
import axios from "axios";
import {
  Box,
  Typography,
  TextField,
  MenuItem,
  Button,
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
} from "@mui/material";

const AddServicePage = () => {
  const [formData, setFormData] = useState({
    category: "",
    name: "",
    serviceDescription: "",
    priceRange: "",
    durationEstimate: "",
  });

  const [serviceCategories, setServiceCategories] = useState([]); // State to store categories
  const [isPopupOpen, setIsPopupOpen] = useState(false);
  const [isLoading, setIsLoading] = useState(true); // State to track loading status

  // Fetch service categories from the backend
  useEffect(() => {
    const fetchCategories = async () => {
      try {
        // Get token from localStorage or sessionStorage
        const token = localStorage.getItem('authToken') || sessionStorage.getItem('authToken');
        
        if (!token) {
          console.error("No authentication token found");
          setIsLoading(false);
          return;
        }
        
        const response = await axios.get("/api/service-categories/getAll", {
          headers: {
            'Authorization': `Bearer ${token}`  // Add token as Bearer token
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
    setIsPopupOpen(true); // Open the confirmation popup
  };

  const handleConfirm = async () => {
    setIsPopupOpen(false);
    
    try {
      const token = localStorage.getItem('authToken') || sessionStorage.getItem('authToken');
      
      if (!token) {
        console.error("No authentication token found");
        return;
      }
      
      // Step 1: Get user ID from auth data
      // Update this line to check both storage options
      const userId = localStorage.getItem('userId') || sessionStorage.getItem('userId');

      if (!userId) {
        console.error("User ID not found");
        return;
      }
      
      // Step 2: Fetch service provider details using user ID
      // Since there's no direct endpoint to get provider by userId, we need to:
      // 1. Get all providers
      // 2. Filter to find the one matching current user
// Step 2: Fetch service provider details using user ID
const providerResponse = await axios.get("/api/service-providers/getAll", {
  headers: {
    'Authorization': `Bearer ${token}`
  }
});

// Examine a single provider object to understand its structure
console.log("Current user ID:", userId);
console.log("Sample provider object structure:", 
  providerResponse.data.length > 0 ? JSON.stringify(providerResponse.data[0], null, 2) : "No providers found");

    // Try different property paths to find the user ID
    const providerData = providerResponse.data.find(provider => {
      // Log the whole provider object to inspect all properties
      console.log("Provider object:", provider);
      
      // Check if the userAuth.userId matches the current user ID
      return provider.userAuth?.userId == userId;
    });

    if (!providerData) {
      console.error("No service provider found for this user");
      return;
    }

// Log the found provider
console.log("Found provider:", providerData);

// Extract the provider ID (assuming it's called providerId)
const providerId = providerData.providerId;
      
      // Create service entity that matches your backend expectations
      const serviceDetails = {
        serviceName: formData.name,
        serviceDescription: formData.serviceDescription, 
        priceRange: formData.priceRange,
        durationEstimate: formData.durationEstimate
      };
      
      // Use the correct API endpoint format
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
      // Clear form or redirect user after success
      setFormData({
        category: "",
        name: "",
        serviceDescription: "",
        priceRange: "",
        durationEstimate: "",
      });
      // Add success notification here
    } catch (error) {
      console.error("Error adding service:", error);
      // Handle error - show error message to user
    }
  };

  return (
    <Box sx={{ padding: "40px", maxWidth: "600px", margin: "0 auto" }}>
      {/* Title */}
      <Typography
        variant="h2"
        sx={{
          textAlign: "center",
          color: "#495E57",
          marginBottom: "20px",
          fontWeight: "bold",
        }}
      >
        What is your service about?
      </Typography>

      {/* Form */}
      <Box
        component="form"
        onSubmit={handleSubmit}
        sx={{
          display: "flex",
          flexDirection: "column",
          gap: "20px",
          backgroundColor: "#f9f9f9",
          padding: "20px",
          borderRadius: "8px",
          boxShadow: "0 4px 6px rgba(0, 0, 0, 0.1)",
        }}
      >
        {/* Service Category */}
        <TextField
          select
          label="Service Category"
          name="category"
          value={formData.category}
          onChange={handleChange}
          required
          fullWidth
          disabled={isLoading} // Disable dropdown while loading
        >
          {isLoading ? (
            <MenuItem value="" disabled>
              Loading categories...
            </MenuItem>
          ) : (
            serviceCategories.map((category) => (
              <MenuItem key={category.categoryId} value={category.categoryId}>
                {category.categoryName}
              </MenuItem>
            ))
          )}
        </TextField>

        {/* Service Name */}
        <TextField
          label="Service Name"
          name="name"
          value={formData.name}
          onChange={handleChange}
          required
          fullWidth
        />

        {/* serviceDescription */}
        <TextField
          label="serviceDescription"
          name="serviceDescription"
          value={formData.serviceDescription}
          onChange={handleChange}
          required
          multiline
          rows={3}
          fullWidth
        />

        {/* Price Range */}
        <TextField
          label="Price Range"
          name="priceRange"
          value={formData.priceRange}
          onChange={handleChange}
          required
          fullWidth
        />

        {/* durationEstimate */}
        <TextField
          label="durationEstimate"
          name="durationEstimate"
          value={formData.durationEstimate}
          onChange={handleChange}
          required
          fullWidth
        />

        {/* Submit Button */}
        <Button
          type="submit"
          variant="contained"
          sx={{
            backgroundColor: "#495E57",
            color: "#fff",
            textTransform: "none",
            "&:hover": {
              backgroundColor: "#3A4A47",
            },
          }}
        >
          Submit
        </Button>
      </Box>

      {/* Confirmation Popup */}
      <Dialog open={isPopupOpen} onClose={() => setIsPopupOpen(false)}>
        <DialogTitle sx={{ textAlign: "center", fontWeight: "bold", color: "#495E57" }}>
          Confirm Service Details
        </DialogTitle>
        <DialogContent>
          <Typography variant="body1" sx={{ marginBottom: "10px" }}>
            <strong>Category:</strong>{" "}
            {serviceCategories.find((cat) => cat.categoryId === formData.category)?.categoryName || ""}
          </Typography>
          <Typography variant="body1" sx={{ marginBottom: "10px" }}>
            <strong>Name:</strong> {formData.name}
          </Typography>
          <Typography variant="body1" sx={{ marginBottom: "10px" }}>
            <strong>serviceDescription:</strong> {formData.serviceDescription}
          </Typography>
          <Typography variant="body1" sx={{ marginBottom: "10px" }}>
            <strong>Price Range:</strong> {formData.priceRange}
          </Typography>
          <Typography variant="body1" sx={{ marginBottom: "10px" }}>
            <strong>durationEstimate:</strong> {formData.durationEstimate}
          </Typography>
        </DialogContent>
        <DialogActions>
          <Button
            onClick={() => setIsPopupOpen(false)}
            sx={{
              color: "#f44336",
              textTransform: "none",
            }}
          >
            Cancel
          </Button>
          <Button
            onClick={handleConfirm}
            sx={{
              backgroundColor: "#495E57",
              color: "#fff",
              textTransform: "none",
              "&:hover": {
                backgroundColor: "#3A4A47",
              },
            }}
          >
            Confirm
          </Button>
        </DialogActions>
      </Dialog>
    </Box>
  );
};

export default AddServicePage;