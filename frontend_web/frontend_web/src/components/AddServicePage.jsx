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
    description: "",
    priceRange: "",
    duration: "",
  });

  const [serviceCategories, setServiceCategories] = useState([]); // State to store categories
  const [isPopupOpen, setIsPopupOpen] = useState(false);
  const [isLoading, setIsLoading] = useState(true); // State to track loading status

  // Fetch service categories from the backend
  useEffect(() => {
    const fetchCategories = async () => {
      try {
        const response = await axios.get("/api/service-categories/getAll");
        setServiceCategories(response.data); // Set categories from the backend
        setIsLoading(false); // Set loading to false
      } catch (error) {
        console.error("Error fetching service categories:", error);
        setIsLoading(false); // Set loading to false even if there's an error
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
    console.log("Service added:", formData);
    // Backend integration for adding the service will go here
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

        {/* Description */}
        <TextField
          label="Description"
          name="description"
          value={formData.description}
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

        {/* Duration */}
        <TextField
          label="Duration"
          name="duration"
          value={formData.duration}
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
            <strong>Description:</strong> {formData.description}
          </Typography>
          <Typography variant="body1" sx={{ marginBottom: "10px" }}>
            <strong>Price Range:</strong> {formData.priceRange}
          </Typography>
          <Typography variant="body1" sx={{ marginBottom: "10px" }}>
            <strong>Duration:</strong> {formData.duration}
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