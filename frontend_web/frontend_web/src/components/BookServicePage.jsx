import React from "react";
import { useLocation, useNavigate } from "react-router-dom";
import {
  Box,
  Typography,
  TextField,
  Button,
  Grid,
  Card,
  CardContent,
} from "@mui/material";

const BookServicePage = () => {
  const location = useLocation();
  const navigate = useNavigate();
  const service = location.state?.service; // Get the service details from state

  if (!service) {
    return (
      <Box sx={{ padding: "40px", textAlign: "center" }}>
        <Typography variant="h5" sx={{ color: "#495E57", mb: 2 }}>
          No service selected.
        </Typography>
        <Button
          variant="contained"
          sx={{
            backgroundColor: "#495E57",
            color: "#fff",
            textTransform: "none",
            "&:hover": {
              backgroundColor: "#3A4A47",
            },
          }}
          onClick={() => navigate("/browseServices")}
        >
          Back to Browse Services
        </Button>
      </Box>
    );
  }

  const handleSubmit = (event) => {
    event.preventDefault();
    // Handle booking logic here (e.g., send booking details to the backend)
    alert("Service booked successfully!");
    navigate("/customerHomePage"); // Redirect to the customer's home page after booking
  };

  return (
    <Box sx={{ padding: "40px" }}>
      <Typography variant="h4" sx={{ fontWeight: "bold", color: "#495E57", mb: 4, textAlign: "center" }}>
        Book Service
      </Typography>
      <Grid container spacing={4}>
        {/* Service Details */}
        <Grid item xs={12} md={6}>
          <Card sx={{ borderRadius: "12px", boxShadow: "0 4px 10px rgba(0, 0, 0, 0.1)" }}>
            <CardContent>
              <Typography variant="h6" sx={{ fontWeight: "bold", color: "#495E57", mb: 2 }}>
                Service Details
              </Typography>
              <Typography variant="body1" sx={{ mb: 1 }}>
                <strong>Service Name:</strong> {service.serviceName}
              </Typography>
              <Typography variant="body1" sx={{ mb: 1 }}>
                <strong>Description:</strong> {service.serviceDescription}
              </Typography>
              <Typography variant="body1" sx={{ mb: 1 }}>
                <strong>Price Range:</strong> {service.priceRange}
              </Typography>
              <Typography variant="body1" sx={{ mb: 1 }}>
                <strong>Duration:</strong> {service.durationEstimate || "Not specified"}
              </Typography>
              <Typography variant="body1" sx={{ mb: 1 }}>
                <strong>Provider:</strong> {service.provider.firstName} {service.provider.lastName}
              </Typography>
            </CardContent>
          </Card>
        </Grid>

        {/* Booking Form */}
        <Grid item xs={12} md={6}>
          <Card sx={{ borderRadius: "12px", boxShadow: "0 4px 10px rgba(0, 0, 0, 0.1)" }}>
            <CardContent>
              <Typography variant="h6" sx={{ fontWeight: "bold", color: "#495E57", mb: 2 }}>
                Booking Form
              </Typography>
              <form onSubmit={handleSubmit}>
                <TextField
                  fullWidth
                  label="Full Name"
                  variant="outlined"
                  required
                  sx={{ mb: 2 }}
                />
                <TextField
                  fullWidth
                  label="Contact Number"
                  variant="outlined"
                  required
                  sx={{ mb: 2 }}
                />
                <TextField
                  fullWidth
                  label="Preferred Date"
                  type="date"
                  InputLabelProps={{ shrink: true }}
                  variant="outlined"
                  required
                  sx={{ mb: 2 }}
                />
                <TextField
                  fullWidth
                  label="Additional Notes"
                  variant="outlined"
                  multiline
                  rows={4}
                  sx={{ mb: 2 }}
                />
                <Button
                  type="submit"
                  variant="contained"
                  fullWidth
                  sx={{
                    backgroundColor: "#495E57",
                    color: "#fff",
                    textTransform: "none",
                    "&:hover": {
                      backgroundColor: "#3A4A47",
                    },
                  }}
                >
                  Confirm Booking
                </Button>
              </form>
            </CardContent>
          </Card>
        </Grid>
      </Grid>
    </Box>
  );
};

export default BookServicePage;