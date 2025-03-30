import React from "react";
import { Box, Typography, Avatar, Button } from "@mui/material";
import { styled } from "@mui/system";

const FooterContainer = styled(Box)({
  backgroundColor: "#495E57", // Footer background color
  color: "white", // Text color
  padding: "40px 40px", // Add padding on left and right
  display: "flex", // Flexbox for layout
  justifyContent: "space-between", // Space between columns
  flexWrap: "wrap", // Wrap columns on smaller screens
  gap: "20px", // Gap between columns
});

const FooterColumn = styled(Box)({
  flex: "1", // Each column takes equal space
  minWidth: "200px", // Minimum width for responsiveness
});

const FooterLogo = styled(Box)({
  display: "flex",
  alignItems: "center", // Align logo and subtitle vertically
  marginBottom: "16px", // Space below the logo
});

const FooterLogoImage = styled(Avatar)({
  width: "40px",
  height: "40px",
  marginRight: "10px", // Space between logo and subtitle
});

const FooterSubtitle = styled(Typography)({
  fontSize: "18px",
  fontWeight: "bold",
  color: "#F4CE14", // Set subtitle color to #F4CE14
  lineHeight: "40px", // Match the height of the logo for perfect vertical alignment
});

const FooterText = styled(Typography)({
  fontSize: "14px",
  marginBottom: "8px", // Space below each text
});

const FooterButton = styled(Button)({
  backgroundColor: "#F4CE14", // Button background color
  color: "black", // Button text color
  padding: "10px 20px",
  borderRadius: "4px",
  fontSize: "14px",
  fontWeight: "bold",
  "&:hover": {
    backgroundColor: "#e0b813", // Darker yellow on hover
  },
});

function Footer() {
  return (
    <FooterContainer>
      {/* First Column */}
      <FooterColumn>
        <FooterLogo>
          <FooterLogoImage src="/images/logo.png" alt="SerbisYo Logo" />
          <FooterSubtitle>SerbisYo</FooterSubtitle>
        </FooterLogo>
        <FooterText>Find trusted professionals for any home service you need.</FooterText>
      </FooterColumn>

      {/* Second Column */}
      <FooterColumn>
        <FooterSubtitle>Quick Links</FooterSubtitle>
        <FooterText>About Us</FooterText>
        <FooterText>Services</FooterText>
        <FooterText>How It Works</FooterText>
        <FooterText>Contact</FooterText>
      </FooterColumn>

      {/* Third Column */}
      <FooterColumn>
        <FooterSubtitle>Ready to Get Started</FooterSubtitle>
        <FooterText>
          Join thousands of satisfied customers who found their perfect home service providers.
        </FooterText>
        <FooterButton>Sign Up</FooterButton>
      </FooterColumn>
    </FooterContainer>
  );
}

export default Footer;