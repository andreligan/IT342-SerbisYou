import React, { useState } from "react";
import { Box, Typography, Avatar, Grid, Button } from "@mui/material";
import { styled } from "@mui/system";
import Footer from "./Footer";
import { Link } from "react-router-dom"; // Import Link for navigation
import plumbing from "../assets/plumbing.jpg";
import electrical from "../assets/electrical.jpg";
import cleaning from "../assets/cleaning.jpg";
import pestControl from "../assets/pest control.jpg";
import applianceRepair from "../assets/appliance repair.jpg";
import homePainting from "../assets/home painting.jpg";
import carpentry from "../assets/carpentry.jpg";
import movingPacking from "../assets/home moving.jpg";
import handyman from "../assets/handyman.jpeg";
import lawnCare from "../assets/lawn care.jpg";

const BoxContainer = styled(Box)({
  backgroundColor: "#495E57", // Box color
  color: "white",
  padding: "40px",
  textAlign: "center",
  width: "100%", // Full width
  height: "50vh", // Height of the box
  display: "flex",
  flexDirection: "column", // Stack items vertically
  justifyContent: "center", // Center items vertically
  alignItems: "center", // Center items horizontally
});

const Title = styled(Typography)({
  fontSize: "45px",
  marginBottom: "16px",
  fontWeight: "bold",
});

const Subtitle = styled(Typography)({
  fontSize: "15px",
  marginBottom: "24px",
  color: "#F5F7F8", // Yellow color for the subtitle
});

const ButtonContainer = styled(Box)({
  display: "flex",
  justifyContent: "center",
  gap: "10px",
});

const CustomerButton = styled(Box)({
  backgroundColor: "#F4CE14", // Yellow button
  color: "black",
  padding: "10px 20px",
  borderRadius: "4px",
  fontSize: "14px",
  "&:hover": {
    backgroundColor: "#e0b813",
  },
});

const ProviderButton = styled(Box)({
  backgroundColor: "white", // White button
  color: "black",
  padding: "10px 20px",
  border: "1px solid #ddd",
  borderRadius: "4px",
  fontSize: "14px",
  "&:hover": {
    backgroundColor: "#f5f5f5",
  },
});

// Featured Home Categories Section
const categories = [
  { name: "Plumbing Services", image: plumbing, link: "/plumbingServices" },
  { name: "Electrical Services", image: electrical },
  { name: "Cleaning Services", image: cleaning },
  { name: "Pest Control", image: pestControl },
  { name: "Appliance Repair", image: applianceRepair },
  { name: "Home Painting", image: homePainting },
  { name: "Carpentry Services", image: carpentry },
  { name: "Moving & Packing Services", image: movingPacking },
  { name: "Handyman Services", image: handyman },
  { name: "Lawn Care & Gardening", image: lawnCare },
];

const FeaturedCategoriesContainer = styled(Box)({
  padding: "20px",
  textAlign: "center",
  backgroundColor: "#F5F7F8", // Background color for contrast
  height: "400px", // Increased height
  display: "flex", // Enable flexbox
  flexDirection: "column", // Stack items vertically
  justifyContent: "center", // Center content vertically
  alignItems: "center", // Center content horizontally
});

const CategoryCircle = styled(Box)(({ src }) => ({
  width: "100px",
  height: "100px",
  margin: "0 auto",
  backgroundColor: "#f5f5f5",
  borderRadius: "50%",
  backgroundImage: `url(${src})`, // Dynamically set the image from props
  backgroundSize: "cover", // Ensure the image covers the circle
  backgroundPosition: "center", // Center the image
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
  margin: "0 10px", // Add margin to bring buttons closer
});

// How It Works Section
const HowItWorksContainer = styled(Box)({
  padding: "20px",
  height: "350px",
  textAlign: "center",
  display: "flex",
  flexDirection: "column", // Stack items vertically
  justifyContent: "center", // Center items vertically
  alignItems: "center", // Center items horizontally
});

const Circle = styled(Box)({
  width: "80px", // Increased size for the circle
  height: "80px", // Increased size for the circle
  borderRadius: "50%",
  backgroundColor: "#495E57", // Circle background color
  color: "#F4CE14", // Circle text color
  display: "flex",
  alignItems: "center",
  justifyContent: "center",
  fontSize: "32px", // Increased font size for the step numbers
  fontWeight: "bold",
  margin: "0 auto",
});

const HowItWorksSubtitle = styled(Typography)({
  fontSize: "18px",
  fontWeight: "bold",
  color: "#495E57", // Subtitle color
  marginTop: "16px",
  marginBottom: "8px", // Add space below the subtitle
});

const HowItWorksText = styled(Typography)({
  fontSize: "14px",
  color: "black", // Normal text color
  maxWidth: "300px", // Limit the width to ensure wrapping
  margin: "0 auto", // Center the text
  textAlign: "center", // Align text to the center
});

// Featured Service Providers Section
const FeaturedProvidersContainer = styled(Box)({
  padding: "20px",
  height: "400px",
  textAlign: "center",
  backgroundColor: "#F5F7F8", // White background for contrast
  display: "flex", // Enable flexbox
  flexDirection: "column", // Stack items vertically
  justifyContent: "center", // Center content vertically
  alignItems: "center",
});

const ProviderCard = styled(Box)({
  display: "flex",
  flexDirection: "column", // Stack items vertically
  alignItems: "center", // Center items horizontally
  padding: "20px",
  border: "1px solid #ddd", // Border for the card
  borderRadius: "8px",
  backgroundColor: "#F9F9F9", // Light background for the card
  boxShadow: "0 4px 6px rgba(0, 0, 0, 0.1)", // Subtle shadow for the card
  width: "300px", // Set a fixed width for the boxes
  transition: "box-shadow 0.3s ease, transform 0.3s ease", // Smooth transition for hover effects
  "&:hover": {
    boxShadow: "0 8px 16px rgba(244, 206, 20, 0.8)", // Glow effect with #F4CE14
    transform: "scale(1.05)", // Slightly enlarge the box on hover
  },
});

const ProviderImage = styled(Avatar)({
  width: "80px",
  height: "80px",
  marginBottom: "10px", // Space below the image
});

const ProviderName = styled(Typography)({
  fontSize: "16px",
  fontWeight: "bold",
  color: "#333",
  marginBottom: "8px", // Space below the name
});

const ProviderRating = styled(Box)({
  display: "flex",
  alignItems: "center",
  marginBottom: "8px", // Space below the ratings
});

const ProviderJob = styled(Typography)({
  fontSize: "14px",
  color: "#666",
  textAlign: "center",
});

function ServiceProviderHomePage() {
  const [page, setPage] = useState(1);

  const handlePageChange = (newPage) => {
    setPage(newPage);
  };

  // Get categories for the current page
  const itemsPerPage = 5;
  const startIndex = (page - 1) * itemsPerPage;
  const endIndex = startIndex + itemsPerPage;
  const currentCategories = categories.slice(startIndex, endIndex);

  return (
    <>
      <BoxContainer>
        <Title variant="h1">Find Trusted Professionals for Any Home Service!</Title>
        <Subtitle variant="h3">
          Book services from verified professionals with ease and security.
        </Subtitle>
        <ButtonContainer>
          <CustomerButton>Be a Customer daw</CustomerButton>
          <ProviderButton>Be a Provider daw</ProviderButton>
        </ButtonContainer>
      </BoxContainer>

      {/* Featured Home Categories Section */}
      <FeaturedCategoriesContainer>
        <Typography variant="h4" fontWeight={"bold"} color={"#495E57"} gutterBottom style={{ marginBottom: "40px" }}>
          Featured Home Categories
        </Typography>
        <Box display="flex" alignItems="center" justifyContent="center" gap="10px">
          {/* Previous Button */}
          <ArrowButton
            variant="contained"
            color="primary"
            disabled={page === 1}
            onClick={() => handlePageChange(page - 1)}
          >
            &lt;
          </ArrowButton>

          {/* Categories */}
          <Grid container spacing={4} justifyContent="center" style={{ flex: 1 }}>
            {currentCategories.map((category, index) => (
              <Grid item xs={6} sm={4} md={2} key={index}>
                <Link to={category.link || "#"} style={{ textDecoration: "none" }}>
                  <CategoryCircle src={category.image} alt={category.name} />
                  <CategoryName>{category.name}</CategoryName>
                </Link>
              </Grid>
            ))}
          </Grid>

          {/* Next Button */}
          <ArrowButton
            variant="contained"
            color="primary"
            disabled={page === Math.ceil(categories.length / itemsPerPage)}
            onClick={() => handlePageChange(page + 1)}
          >
            &gt;
          </ArrowButton>
        </Box>

        {/* Page Number */}
        <Typography
          variant="body1"
          style={{
            fontWeight: "bold",
            marginTop: "40px",
          }}
        >
          {page}
        </Typography>
      </FeaturedCategoriesContainer>

      {/* How It Works Section */}
      <HowItWorksContainer>
        <Typography variant="h4" fontWeight={"bold"} color={"#495E57"} gutterBottom style={{ marginBottom: "20px" }}>
          How It Works
        </Typography>
        <Grid container spacing={15} justifyContent="center">
          {/* Step 1 */}
          <Grid item xs={12} sm={12}>
            <Circle>1</Circle>
            <HowItWorksSubtitle>Search for a Service</HowItWorksSubtitle>
            <HowItWorksText>
              Browse through the different home service categories or search for specific services.
            </HowItWorksText>
          </Grid>

          {/* Step 2 */}
          <Grid item xs={12} sm={12}>
            <Circle>2</Circle>
            <HowItWorksSubtitle>Book an Appointment</HowItWorksSubtitle>
            <HowItWorksText>
              Choose your preferred time and date.
            </HowItWorksText>
          </Grid>

          {/* Step 3 */}
          <Grid item xs={12} sm={12}>
            <Circle>3</Circle>
            <HowItWorksSubtitle>Pay Securely & Review</HowItWorksSubtitle>
            <HowItWorksText>
              Complete your booking and share your experience!
            </HowItWorksText>
          </Grid>
        </Grid>
      </HowItWorksContainer>

      {/* Featured Service Providers Section */}
      <FeaturedProvidersContainer>
        <Typography
          variant="h4"
          fontWeight={"bold"}
          color={"#495E57"}
          gutterBottom
          style={{ marginBottom: "30px" }}
        >
          Featured Service Providers
        </Typography>
        <Grid container spacing={4} justifyContent="center">
          {/* Provider 1 */}
          <Grid item xs={12} sm={6} md={4}>
            <ProviderCard>
              <ProviderImage src="/images/provider1.jpg" alt="Provider 1" />
              <ProviderName>John Doe</ProviderName>
              <ProviderRating>
                {"⭐".repeat(5)} {/* Replace with a star icon if needed */}
              </ProviderRating>
              <ProviderJob>Licensed Plumber</ProviderJob>
            </ProviderCard>
          </Grid>

          {/* Provider 2 */}
          <Grid item xs={12} sm={6} md={4}>
            <ProviderCard>
              <ProviderImage src="/images/provider2.jpg" alt="Provider 2" />
              <ProviderName>Jane Smith</ProviderName>
              <ProviderRating>
                {"⭐".repeat(4)} {/* Replace with a star icon if needed */}
              </ProviderRating>
              <ProviderJob>Certified Electrician</ProviderJob>
            </ProviderCard>
          </Grid>

          {/* Provider 3 */}
          <Grid item xs={12} sm={6} md={4}>
            <ProviderCard>
              <ProviderImage src="/images/provider3.jpg" alt="Provider 3" />
              <ProviderName>Michael Brown</ProviderName>
              <ProviderRating>
                {"⭐".repeat(5)} {/* Replace with a star icon if needed */}
              </ProviderRating>
              <ProviderJob>Professional Cleaner</ProviderJob>
            </ProviderCard>
          </Grid>
        </Grid>
      </FeaturedProvidersContainer>

      {/* Footer Section */}
      <Footer />
    </>
  );
}

export default ServiceProviderHomePage;