import React, { useState, useEffect } from "react";
import { useNavigate } from "react-router-dom";
import axios from "axios";
import {
  Box,
  Typography,
  Grid,
  Card,
  CardContent,
  CardMedia,
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  Button,
  Divider,
} from "@mui/material";
import VerifiedIcon from "@mui/icons-material/Verified"; // Icon for verification
import PhoneIcon from "@mui/icons-material/Phone"; // Icon for contact
import ScheduleIcon from "@mui/icons-material/Schedule"; // Icon for availability
import PersonIcon from "@mui/icons-material/Person"; // Icon for service provider name
import BusinessIcon from "@mui/icons-material/Business"; // Icon for business name
import StarIcon from "@mui/icons-material/Star"; // Filled star for ratings
import StarBorderIcon from "@mui/icons-material/StarBorder"; // Empty star for ratings
import WorkIcon from "@mui/icons-material/Work"; // Icon for years of experience
import AccessTimeIcon from "@mui/icons-material/AccessTime"; // Icon for duration
import AttachMoneyIcon from "@mui/icons-material/AttachMoney"; // Icon for price

const BrowseServicesPage = () => {
  const [services, setServices] = useState([]);
  const [selectedService, setSelectedService] = useState(null); // Store the selected service
  const [isModalOpen, setIsModalOpen] = useState(false); // Manage modal open/close
  const [isLoading, setIsLoading] = useState(true);
  const navigate = useNavigate();

  useEffect(() => {
    const fetchServicesAndProviders = async () => {
      try {
        const token = localStorage.getItem("authToken") || sessionStorage.getItem("authToken");
        if (!token) {
          console.error("No authentication token found");
          setIsLoading(false);
          return;
        }

        // Fetch services
        const servicesResponse = await axios.get("/api/services/getAll", {
          headers: {
            Authorization: `Bearer ${token}`,
          },
        });

        // Fetch service providers
        const providersResponse = await axios.get("/api/service-providers/getAll", {
          headers: {
            Authorization: `Bearer ${token}`,
          },
        });

        const services = servicesResponse.data;
        const providers = providersResponse.data;

        // Match providers with services
        const servicesWithProviders = services.map((service) => {
          const provider = providers.find((p) => p.providerId === service.provider?.providerId);
          return {
            ...service,
            providerFirstName: provider?.firstName || "Unknown",
            providerLastName: provider?.lastName || "Unknown",
            profilePicture: provider?.profilePicture || "/default-profile.jpg",
            businessName: provider?.businessName || "No Business Name",
            yearsOfExperience: provider?.yearsOfExperience || "Not specified",
            contactNumber: provider?.phoneNumber || "Not available",
            isVerified: provider?.verified || false,
            availabilitySchedule: provider?.availabilitySchedule || "Not specified",
            averageRating: provider?.averageRating || 0,
            status: provider?.status || "Not specified",
            paymentMethod: provider?.paymentMethod || "Not specified",
          };
        });

        setServices(servicesWithProviders);
        setIsLoading(false);
      } catch (error) {
        console.error("Error fetching services or providers:", error);
        setIsLoading(false);
      }
    };

    fetchServicesAndProviders();
  }, []);

  // Handle modal open/close
  const handleOpenModal = (service) => {
    setSelectedService(service); // Set the selected service
    setIsModalOpen(true); // Open the modal
  };

  const handleCloseModal = () => {
    setSelectedService(null); // Clear the selected service
    setIsModalOpen(false); // Close the modal
  };

  const handleBookService = () => {
    if (selectedService) {
      navigate("/bookService", { state: { service: selectedService } }); // Redirect with service details
    }
  };

  // Render star ratings dynamically based on the rating value
  const renderStars = (rating) => {
    const stars = [];
    for (let i = 1; i <= 5; i++) {
      stars.push(
        i <= rating ? (
          <StarIcon key={i} sx={{ color: "#FFD700" }} /> // Yellow filled star
        ) : (
          <StarBorderIcon key={i} sx={{ color: "#FFD700" }} /> // Yellow empty star
        )
      );
    }
    return stars;
  };

  return (
    <Box sx={{ padding: "40px" }}>
      <Typography variant="h4" sx={{ fontWeight: "bold", color: "#495E57", mb: 4, textAlign: "center" }}>
        Browse Services
      </Typography>
      {isLoading ? (
        <Typography variant="body1" sx={{ color: "#666", textAlign: "center" }}>
          Loading services...
        </Typography>
      ) : services.length === 0 ? (
        <Typography variant="body1" sx={{ color: "#666", textAlign: "center" }}>
          No services available at the moment.
        </Typography>
      ) : (
        <Grid container spacing={4}>
          {services.map((service) => (
            <Grid item xs={12} sm={6} md={4} key={service.serviceId}>
              <Card
                onClick={() => handleOpenModal(service)} // Open modal with service details
                sx={{
                  width: "min(140%, 300px)", // Fixed width for all cards
                  borderRadius: "12px",
                  boxShadow: "0 4px 10px rgba(0, 0, 0, 0.1)",
                  transition: "transform 0.3s ease, box-shadow 0.3s ease",
                  "&:hover": {
                    transform: "scale(1.05)",
                    boxShadow: "0 8px 20px rgba(0, 0, 0, 0.2)",
                  },
                  margin: "0 auto", // Center the card horizontally
                  cursor: "pointer", // Add pointer cursor for interactivity
                }}
              >
                <CardMedia
                  component="img"
                  height="180"
                  image={service.image || "/default-service.jpg"} // Use service image or a default image
                  alt={service.serviceName}
                  sx={{
                    borderTopLeftRadius: "12px",
                    borderTopRightRadius: "12px",
                  }}
                />
                <CardContent>
                  <Typography
                    gutterBottom
                    variant="h6"
                    component="div"
                    sx={{ fontWeight: "bold", color: "#333", textAlign: "center" }}
                  >
                    {service.serviceName}
                  </Typography>
                  <Typography
                    variant="body2"
                    color="text.secondary"
                    sx={{
                      textAlign: "center",
                      mb: 2,
                      overflow: "hidden",
                      textOverflow: "ellipsis",
                      display: "-webkit-box",
                      WebkitLineClamp: 3, // Limit to 3 lines
                      WebkitBoxOrient: "vertical",
                    }}
                  >
                    {service.serviceDescription}
                  </Typography>
                  <Typography
                    variant="body2"
                    color="text.primary"
                    sx={{ fontWeight: "bold", textAlign: "center" }}
                  >
                    Price Range: {service.priceRange}
                  </Typography>
                  <Typography
                    variant="body2"
                    color="text.primary"
                    sx={{ textAlign: "center" }}
                  >
                    Duration: {service.durationEstimate || "Not specified"}
                  </Typography>
                  <Typography
                    variant="body2"
                    color="text.secondary"
                    sx={{ mt: 1, fontStyle: "italic", textAlign: "center" }}
                  >
                    Provider: {service.providerFirstName && service.providerLastName
                      ? `${service.providerFirstName} ${service.providerLastName}`
                      : "Unknown"}
                  </Typography>
                </CardContent>
              </Card>
            </Grid>
          ))}
        </Grid>
      )}

      {/* Modal for Service Details */}
      <Dialog open={isModalOpen} onClose={handleCloseModal} maxWidth="md" fullWidth>
        <DialogTitle sx={{ fontWeight: "bold", color: "#495E57", textAlign: "center", mb: 2 }}>
          Service Provider Details
        </DialogTitle>
        <DialogContent>
          {selectedService && (
            <Box>
              {/* Upper Section: Service Provider Details */}
              <Box sx={{ mb: 3 }}>
                <Box sx={{ display: "flex", justifyContent: "space-between", alignItems: "center" }}>
                  {/* Left Box */}
                  <Box sx={{ flex: 1, display: "flex", alignItems: "center" }}>
                    <img
                      src={selectedService.profilePicture || "/default-profile.jpg"}
                      alt="Provider Profile"
                      style={{ width: "100px", height: "100px", borderRadius: "50%", marginRight: "16px" }}
                    />
                    <Box>
                      <Box sx={{ display: "flex", alignItems: "center", mb: 1 }}>
                        <PersonIcon sx={{ color: "#495E57", mr: 1 }} />
                        <Typography variant="h6" sx={{ fontWeight: "bold" }}>
                          {selectedService.providerFirstName} {selectedService.providerLastName}
                        </Typography>
                      </Box>
                      <Box sx={{ display: "flex", alignItems: "center", mb: 1 }}>
                        <BusinessIcon sx={{ color: "#495E57", mr: 1 }} />
                        <Typography variant="body2" color="text.secondary">
                          {selectedService.businessName}
                        </Typography>
                      </Box>
                      <Box sx={{ display: "flex", alignItems: "center", mb: 1 }}>
                        {renderStars(selectedService.averageRating)}
                      </Box>
                      <Box sx={{ display: "flex", alignItems: "center" }}>
                        <WorkIcon sx={{ color: "#495E57", mr: 1 }} />
                        <Typography variant="body2" color="text.secondary">
                          Years of Experience: {selectedService.yearsOfExperience}
                        </Typography>
                      </Box>
                    </Box>
                  </Box>

                  {/* Right Box */}
                  <Box sx={{ flex: 1, textAlign: "right", display: "flex", flexDirection: "column", alignItems: "flex-end" }}>
                    <Box sx={{ display: "flex", alignItems: "center", mb: 1 }}>
                      <VerifiedIcon sx={{ color: "#4caf50", mr: 1 }} />
                      <Typography variant="body2" color="text.secondary">
                        Verification: {selectedService.isVerified ? "Verified" : "Not Verified"}
                      </Typography>
                    </Box>
                    <Box sx={{ display: "flex", alignItems: "center", mb: 1 }}>
                      <PhoneIcon sx={{ color: "#2196f3", mr: 1 }} />
                      <Typography variant="body2" color="text.secondary">
                        Contact: {selectedService.contactNumber}
                      </Typography>
                    </Box>
                    <Box sx={{ display: "flex", alignItems: "center" }}>
                      <ScheduleIcon sx={{ color: "#ff9800", mr: 1 }} />
                      <Typography variant="body2" color="text.secondary">
                        Availability: {selectedService.availabilitySchedule}
                      </Typography>
                    </Box>
                  </Box>
                </Box>
              </Box>

              <Divider sx={{ my: 3 }} />

              {/* Lower Section: Service Details */}
              <Box>
                <Typography
                  variant="h6"
                  sx={{ fontWeight: "bold", mb: 2, textAlign: "center" }} // Centered title
                >
                  Service Details
                </Typography>
                <Box sx={{ display: "flex", alignItems: "center", mb: 2 }}>
                  <img
                    src={selectedService.image || "/default-service.jpg"}
                    alt="Service"
                    style={{ width: "100px", height: "100px", borderRadius: "50%", marginRight: "16px" }}
                  />
                  <Box>
                    <Typography variant="h6" sx={{ fontWeight: "bold" }}>
                      {selectedService.serviceName}
                    </Typography>
                    <Typography variant="body2" color="text.secondary">
                      {selectedService.serviceDescription}
                    </Typography>
                  </Box>
                </Box>
                <Box sx={{ display: "flex", alignItems: "center", mb: 1 }}>
                  <AccessTimeIcon sx={{ color: "#495E57", mr: 1 }} />
                  <Typography variant="body2" color="text.primary">
                    Duration: {selectedService.durationEstimate || "Not specified"}
                  </Typography>
                </Box>
                <Box sx={{ display: "flex", alignItems: "center" }}>
                  <AttachMoneyIcon sx={{ color: "#495E57", mr: 1 }} />
                  <Typography variant="body2" color="text.primary">
                    Price Range: {selectedService.priceRange}
                  </Typography>
                </Box>
              </Box>
            </Box>
          )}
        </DialogContent>
        <DialogActions>
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
            onClick={handleBookService}
          >
            Book Service
          </Button>
          <Button onClick={handleCloseModal} sx={{ textTransform: "none" }}>
            Close
          </Button>
        </DialogActions>
      </Dialog>
    </Box>
  );
};

export default BrowseServicesPage;