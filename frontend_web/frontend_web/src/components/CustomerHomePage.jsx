import React, { useState, useEffect } from "react";
import axios from "axios";
import { Box, Typography, Avatar, Grid, Button } from "@mui/material";
import { styled } from "@mui/system";
import Footer from "./Footer";
import { Link, useNavigate } from "react-router-dom"; // Import Link for navigation
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

const HeroSection = styled(Box)({
  display: "flex",
  height: "50vh",
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
});

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

// Featured Home Categories Section
const FeaturedCategoriesContainer = styled(Box)({
  padding: "20px",
  textAlign: "center",
  backgroundColor: "#F5F7F8",
  height: "400px",
  display: "flex",
  flexDirection: "column",
  justifyContent: "center",
  alignItems: "center",
});

const CategoryCircle = styled(Box)(({ src }) => ({
  width: "100px",
  height: "100px",
  margin: "0 auto",
  backgroundColor: "#f5f5f5",
  borderRadius: "50%",
  backgroundImage: `url(${src})`,
  backgroundSize: "cover",
  backgroundPosition: "center",
}));

const CategoryName = styled(Typography)({
  marginTop: "10px",
  fontSize: "14px",
  fontWeight: "bold",
  color: "#333",
});

const ArrowButton = styled(Button)({
  minWidth: "40px",
  minHeight: "40px",
  fontSize: "20px",
  fontWeight: "bold",
  borderRadius: "50%",
  display: "flex",
  alignItems: "center",
  justifyContent: "center",
  margin: "0 10px",
});

// Featured Service Providers Section
const FeaturedProvidersContainer = styled(Box)({
  padding: "20px",
  height: "400px",
  textAlign: "center",
  backgroundColor: "#F5F7F8",
  display: "flex",
  flexDirection: "column",
  justifyContent: "center",
  alignItems: "center",
});

const ProviderCard = styled(Box)({
  display: "flex",
  flexDirection: "column",
  alignItems: "center",
  padding: "20px",
  border: "1px solid #ddd",
  borderRadius: "8px",
  backgroundColor: "#F9F9F9",
  boxShadow: "0 4px 6px rgba(0, 0, 0, 0.1)",
  width: "300px",
  transition: "box-shadow 0.3s ease, transform 0.3s ease",
  "&:hover": {
    boxShadow: "0 8px 16px rgba(244, 206, 20, 0.8)",
    transform: "scale(1.05)",
  },
});

const ProviderImage = styled(Avatar)({
  width: "80px",
  height: "80px",
  marginBottom: "10px",
});

const ProviderName = styled(Typography)({
  fontSize: "16px",
  fontWeight: "bold",
  color: "#333",
  marginBottom: "8px",
});

const ProviderRating = styled(Box)({
  display: "flex",
  alignItems: "center",
  marginBottom: "8px",
});

const ProviderJob = styled(Typography)({
  fontSize: "14px",
  color: "#666",
  textAlign: "center",
});

function CustomerHomePage() {
  const navigate = useNavigate();

  const [categories, setCategories] = useState([]); // State to store categories
  const [isLoading, setIsLoading] = useState(true); // State to track loading status
  const [page, setPage] = useState(1);

  const itemsPerPage = 5;

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
        console.log("Categories fetched:", response.data); // Log the fetched categories
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
    <>
      {/* Hero Section */}
      <HeroSection>
        <LeftContent>
          <Typography variant="h4" sx={{ fontWeight: "bold", color: "#F4CE14", mb: 2 }}>
            Welcome, Our Dear Customer!
          </Typography>
          <Typography variant="body1" sx={{ color: "white", mb: 4, textAlign: "center" }}>
            Helpful services to ease your stress are here. Start connecting with reliable service providers today!
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
            onClick={() => navigate("/browseServices")}
          >
            Book a Service
          </Button>
        </LeftContent>
        <RightContent>
          <SlideshowImage src={serviceImage1} alt="Service 1" />
          <SlideshowImage src={serviceImage2} alt="Service 2" style={{ animationDelay: "3.3s" }} />
          <SlideshowImage src={serviceImage3} alt="Service 3" style={{ animationDelay: "6.6s" }} />
        </RightContent>
      </HeroSection>

      {/* Featured Home Categories Section */}
      <FeaturedCategoriesContainer>
        <Typography variant="h4" fontWeight={"bold"} color={"#495E57"} gutterBottom style={{ marginBottom: "40px" }}>
          Featured Home Categories
        </Typography>
        {isLoading ? (
          <Typography>Loading categories...</Typography>
        ) : (
          <Box display="flex" alignItems="center" justifyContent="center" gap="10px">
            <ArrowButton
              variant="contained"
              color="primary"
              disabled={page === 1}
              onClick={() => handlePageChange(page - 1)}
            >
              &lt;
            </ArrowButton>

            <Grid container spacing={4} justifyContent="center" style={{ flex: 1 }}>
              {currentCategories.map((category, index) => (
                <Grid item xs={6} sm={4} md={2} key={index}>
                  <Link to="#" style={{ textDecoration: "none" }}>
                    <CategoryCircle
                      src={category.image || categoryImageMap[category.categoryName] || "/default-category.jpg"}
                      alt={category.categoryName} />
                    <CategoryName>{category.categoryName}</CategoryName>
                  </Link>
                </Grid>
              ))}
            </Grid>

            <ArrowButton
              variant="contained"
              color="primary"
              disabled={page === Math.ceil(categories.length / itemsPerPage)}
              onClick={() => handlePageChange(page + 1)}
            >
              &gt;
            </ArrowButton>
          </Box>
        )}
        <Typography variant="body1" style={{ fontWeight: "bold", marginTop: "40px" }}>
          {page}
        </Typography>
      </FeaturedCategoriesContainer>

      {/* Footer */}
      <Footer />
    </>
  );
}

export default CustomerHomePage;