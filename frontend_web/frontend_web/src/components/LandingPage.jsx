import React from "react";
import { Box, Typography, Button } from "@mui/material";
import { styled } from "@mui/system";

const Container = styled(Box)({
  marginTop: "80px", // Space below the header
});

const BoxContainer = styled(Box)({
  backgroundColor: "#495E57", // Box color
  color: "white",
  padding: "40px",
  textAlign: "center",
  width: "100%", // Full width
});

const Title = styled(Typography)({
  fontSize: "24px",
  marginBottom: "16px",
});

const Subtitle = styled(Typography)({
  fontSize: "18px",
  marginBottom: "24px",
});

const ButtonContainer = styled(Box)({
  display: "flex",
  justifyContent: "center",
  gap: "10px",
});

const CustomerButton = styled(Button)({
  backgroundColor: "#F4CE14", // Yellow button
  color: "black",
  padding: "10px 20px",
  borderRadius: "4px",
  fontSize: "16px",
  "&:hover": {
    backgroundColor: "#e0b813",
  },
});

const ProviderButton = styled(Button)({
  backgroundColor: "white", // White button
  color: "black",
  padding: "10px 20px",
  border: "1px solid #ddd",
  borderRadius: "4px",
  fontSize: "16px",
  "&:hover": {
    backgroundColor: "#f5f5f5",
  },
});

function LandingPage() {
  return (
    <Container>
      <BoxContainer>
        <Title variant="h1">Find Trusted Professionals for Any Home Service!</Title>
        <Subtitle variant="h3">
          Book services from verified professionals with ease and security.
        </Subtitle>
        <ButtonContainer>
          <CustomerButton variant="contained">Be a Customer</CustomerButton>
          <ProviderButton variant="outlined">Be a Provider</ProviderButton>
        </ButtonContainer>
      </BoxContainer>
    </Container>
  );
}

export default LandingPage;