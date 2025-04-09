import React, { useState, useEffect } from "react";
import { Box, Typography, Button, Grid, CircularProgress, Alert } from "@mui/material";
import { styled } from "@mui/system"; // Removed unused 'style' import
import { useNavigate } from "react-router-dom";
import Footer from "../Footer";
import axios from "axios";
import serviceImage1 from "../../assets/appliance repair.jpg";
import serviceImage2 from "../../assets/carpentry.jpg";
import serviceImage3 from "../../assets/cleaning.jpg";
import electrical from "../../assets/electrical.jpg";
import plumbing from "../../assets/plumbing.jpg";
import pestControl from "../../assets/pest control.jpg";
import homePainting from "../../assets/home painting.jpg";
import lawnCare from "../../assets/lawn care.jpg";


const HeroSection = styled(Box)({
  display: "flex",
  height: "80vh",
  position: "relative",
});

const LeftContent = styled(Box)({
  flex: 1,
  backgroundColor: "#495E57",
  display: "flex",
  flexDirection: "column",
  justifyContent: "center",
  alignItems: "center",
  padding: "40px",
  zIndex: 2,
});

const RightContent = styled(Box)({
  flex: 1,
  position: "relative",
  overflow: "hidden",
  },
);

const SlideshowImage = styled("img")({
  width: "100%",
  height: "100%",
  objectFit: "cover",
  position: "absolute",
  animation: "fade 10s infinite",
  "@keyframes fade": {
    "0%": { opacity: 1 },
    "33%": { opacity: 0 },
    "66%": { opacity: 1 },
    "100%": { opacity: 0 },
  },
});

const ServicesOfferedSection = styled(Box)({
  padding: "40px",
  backgroundColor: "#F5F7F8",
});

const ServiceCard = styled(Box)({
  border: "1px solid #ddd",
  borderRadius: "8px",
  padding: "20px",
  backgroundColor: "white",
  boxShadow: "0 4px 6px rgba(0, 0, 0, 0.1)",
  transition: "transform 0.3s ease",
  "&:hover": {
    transform: "scale(1.05)",
  },
});

const ServiceImage = styled("img")({
  width: "100%",
  height: "150px",
  objectFit: "cover",
  borderRadius: "8px",
});

const ServiceTitle = styled(Typography)({
  fontSize: "18px",
  fontWeight: "bold",
  marginTop: "10px",
  color: "#495E57",
});

// Keeping ServiceSubtitle but will use it in a future component update
const ServiceSubtitle = styled(Typography)({
  fontSize: "14px",
  color: "#677483",
  marginTop: "5px",
});

function ServiceProviderHomePage() {
  const navigate = useNavigate();
  const [services, setServices] = useState([]);
  const [categoryGroups, setCategoryGroups] = useState({});
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [providerName, setProviderName] = useState("Service Provider");
  
  // Image mapping for categories
  const categoryImageMap = {
    "Plumbing": plumbing,
    "Electrical": electrical,
    "Cleaning": serviceImage3,
    "Pest Control": pestControl,
    "Appliance Repair": serviceImage1,
    "Home Painting": homePainting,
    "Carpentry": serviceImage2,
    "Lawn Care": lawnCare
  };

  // Default images if category doesn't match
  const defaultImages = [serviceImage1, serviceImage2, serviceImage3, electrical, plumbing];

  // Get appropriate image for a service
  const getServiceImage = (categoryName, index) => {
    if (categoryImageMap[categoryName]) {
      return categoryImageMap[categoryName];
    }
    // Fallback to default images with rotation
    return defaultImages[index % defaultImages.length];
  };

  useEffect(() => {
    const fetchProviderServices = async () => {
      try {
        setLoading(true);
        const token = localStorage.getItem('authToken') || sessionStorage.getItem('authToken');
        const userId = localStorage.getItem('userId') || sessionStorage.getItem('userId');
        
        if (!token || !userId) {
          setError("Authentication information not found. Please login again.");
          setLoading(false);
          return;
        }

        // Step 1: Get service provider ID by matching userId
        const providersResponse = await axios.get("/api/service-providers/getAll", {
          headers: { 'Authorization': `Bearer ${token}` }
        });
        
        const provider = providersResponse.data.find(
          p => p.userAuth && p.userAuth.userId == userId
        );
        
        if (!provider) {
          setError("No service provider profile found for this account.");
          setLoading(false);
          return;
        }
        
        // Set provider name from fetched data
        setProviderName(`${provider.firstName || ''} ${provider.lastName || ''}`.trim() || "Service Provider");
        
        // Step 2: Get all services
        const servicesResponse = await axios.get("/api/services/getAll", {
          headers: { 'Authorization': `Bearer ${token}` }
        });
        
        // Step 3: Filter to get only this provider's services
        const providerServices = servicesResponse.data.filter(
          service => service.provider && service.provider.providerId === provider.providerId
        );
        
        // Step 4: Get all categories for display
        const categoriesResponse = await axios.get("/api/service-categories/getAll", {
          headers: { 'Authorization': `Bearer ${token}` }
        });
        
        // Create a map of category IDs to names
        const categoryMap = categoriesResponse.data.reduce((map, category) => {
          map[category.categoryId] = category.categoryName;
          return map;
        }, {});
        
        // Enrich services with category names and images
        const enhancedServices = providerServices.map((service, index) => ({
          id: service.serviceId,
          title: service.serviceName,
          subtitle: service.serviceDescription,
          priceRange: service.priceRange,
          durationEstimate: service.durationEstimate,
          categoryId: service.category?.categoryId,
          categoryName: categoryMap[service.category?.categoryId] || "Uncategorized",
          image: getServiceImage(categoryMap[service.category?.categoryId], index)
        }));
        
        setServices(enhancedServices);
        
        // Group services by category
        const groupedServices = enhancedServices.reduce((groups, service) => {
          const categoryName = service.categoryName;
          if (!groups[categoryName]) {
            groups[categoryName] = [];
          }
          groups[categoryName].push(service);
          return groups;
        }, {});
        
        setCategoryGroups(groupedServices);
        setLoading(false);
      } catch (error) {
        console.error("Error fetching service provider data:", error);
        setError("Failed to load services. Please try again later.");
        setLoading(false);
      }
    };
    
    fetchProviderServices();
  }, []);

  return (
    <>
      {/* Hero Section */}
      <HeroSection>
        <LeftContent>
        <Typography variant="h4" sx={{ fontWeight: "bold", color: "#F4CE14", mb: 2 }}>
  Welcome, {" "}
  <Box 
    component="span" 
    onClick={() => navigate("/serviceProviderProfile")}
    sx={{ 
      cursor: "pointer",
      textDecoration: "none",
      "&:hover": {
        textDecoration: "underline",
      },
      transition: "color 0.2s ease-in-out",
      "&:active": {
        color: "#E9C412"
      }
    }}
  >
    {providerName}
  </Box>
  !
</Typography>
          <Typography variant="body1" sx={{ color: "white", mb: 4, textAlign: "center" }}>
            Add your services and start connecting with customers today.
          </Typography>
          <Button
            variant="contained"
            sx={{
              backgroundColor: "#4caf50",
              color: "#fff",
              textTransform: "none",
              "&:hover": {
                backgroundColor: "#388e3c",
              },
            }}
            onClick={() => navigate("/addService")}
          >
            Add a Service
          </Button>
        </LeftContent>
        <RightContent>
          <SlideshowImage src={serviceImage1} alt="Service 1" />
          <SlideshowImage src={serviceImage2} alt="Service 2" style={{ animationDelay: "3.3s" }} />
          <SlideshowImage src={serviceImage3} alt="Service 3" style={{ animationDelay: "6.6s" }} />
        </RightContent>
      </HeroSection>

      {/* Services Offered Section */}
      <ServicesOfferedSection>
        <Typography variant="h5" sx={{ fontWeight: "bold", color: "#495E57", mb: 4, textAlign: "center" }}>
          Your Services
        </Typography>
        
        {loading ? (
          <Box sx={{ display: "flex", justifyContent: "center", my: 8 }}>
            <CircularProgress color="primary" />
          </Box>
        ) : error ? (
          <Alert severity="error" sx={{ maxWidth: "600px", mx: "auto", my: 4 }}>
            {error}
          </Alert>
        ) : services.length === 0 ? (
          <Box sx={{ textAlign: "center", my: 6 }}>
            <Typography variant="h6" color="text.secondary">
              You haven't added any services yet.
            </Typography>
            <Button 
              variant="contained" 
              sx={{ mt: 2, backgroundColor: "#4caf50", color: "#fff" }}
              onClick={() => navigate("/addService")}
            >
              Add Your First Service
            </Button>
          </Box>
        ) : (
          <Box sx={{ 
            overflowX: "auto", 
            pb: 2,
            '&::-webkit-scrollbar': {
              height: '8px',
            },
            '&::-webkit-scrollbar-thumb': {
              backgroundColor: '#d0d0d0',
              borderRadius: '4px',
            }
          }}>
            <Grid 
              container 
              spacing={3} 
              sx={{ 
                display: "flex", 
                flexDirection: "row",
                justifyContent: "center",
                flexWrap: "nowrap", 
                mt: 1,
                pb: 1,
                width: "max-content",
                minWidth: "100%",
              }}
            >
              {Object.keys(categoryGroups).map((categoryName, index) => (
                <Grid item sx={{ width: "220px", flex: "0 0 auto" }} key={categoryName}>
                  <ServiceCard>
                    <ServiceImage 
                      src={categoryImageMap[categoryName] || defaultImages[index % defaultImages.length]} 
                      alt={categoryName} 
                    />
                    <ServiceTitle sx={{ textAlign: "center" }}>{categoryName}</ServiceTitle>
                    <Typography 
                      sx={{ 
                        fontSize: "14px", 
                        color: "#555", 
                        textAlign: "center",
                        mt: 1
                      }}
                    >
                      {categoryGroups[categoryName].length} service{categoryGroups[categoryName].length !== 1 ? 's' : ''}
                    </Typography>
                  </ServiceCard>
                </Grid>
              ))}
            </Grid>
          </Box>
        )}
        
        <Box sx={{ display: "flex", justifyContent: "center", gap: 2, mt: 4 }}>
          <Button
            variant="contained"
            sx={{
              backgroundColor: "#4caf50",
              color: "#fff",
              textTransform: "none",
              paddingX: 3,
              paddingY: 1,
              "&:hover": {
                backgroundColor: "#388e3c",
              },
            }}
            onClick={() => navigate("/addService")}
          >
            Add New Service
          </Button>
          
          <Button
            variant="contained"
            sx={{
              backgroundColor: "#495E57",
              color: "#fff",
              textTransform: "none",
              paddingX: 3,
              paddingY: 1,
              "&:hover": {
                backgroundColor: "#3a4a45",
              },
            }}
            onClick={() => navigate("/myServices")}
          >
            Manage Services
          </Button>
        </Box>
      </ServicesOfferedSection>

      {/* Footer Section */}
      <Footer />
    </>
  );
}

export default ServiceProviderHomePage;