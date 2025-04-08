import React from "react";
import { Box, Typography, Button, Grid } from "@mui/material";
import { styled } from "@mui/system";
import { useNavigate } from "react-router-dom";
import Footer from "./Footer";
import serviceImage1 from "../assets/appliance repair.jpg";
import serviceImage2 from "../assets/carpentry.jpg";
import serviceImage3 from "../assets/cleaning.jpg";

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

const ServiceSubtitle = styled(Typography)({
  fontSize: "14px",
  color: "#677483",
  marginTop: "5px",
});

function ServiceProviderHomePage() {
  const navigate = useNavigate();

  const services = [
    {
      id: 1,
      title: "Plumbing Services",
      subtitle: "Fix leaks, install pipes, and more.",
      image: serviceImage1,
    },
    {
      id: 2,
      title: "Electrical Services",
      subtitle: "Wiring, repairs, and installations.",
      image: serviceImage2,
    },
    {
      id: 3,
      title: "Cleaning Services",
      subtitle: "Professional cleaning for your home.",
      image: serviceImage3,
    },
  ];

  return (
    <>
      {/* Hero Section */}
      <HeroSection>
        <LeftContent>
          <Typography variant="h4" sx={{ fontWeight: "bold", color: "#F4CE14", mb: 2 }}>
            Welcome, Service Provider!
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
          Services Offered
        </Typography>
        <Grid container spacing={4}>
          {services.map((service) => (
            <Grid item xs={12} sm={6} md={4} key={service.id}>
              <ServiceCard>
                <ServiceImage src={service.image} alt={service.title} />
                <ServiceTitle>{service.title}</ServiceTitle>
                <ServiceSubtitle>{service.subtitle}</ServiceSubtitle>
              </ServiceCard>
            </Grid>
          ))}
        </Grid>
      </ServicesOfferedSection>

      {/* Footer Section */}
      <Footer />
    </>
  );
}

export default ServiceProviderHomePage;