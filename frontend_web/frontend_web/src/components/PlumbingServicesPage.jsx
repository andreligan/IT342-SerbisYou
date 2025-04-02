import React from "react";
import Footer from "./Footer"; // Import the Footer component
import leakRepairImage from "../assets/leakRepair.jpg"; // Import the image for Leak Repairs
import pipeInstallationImage from "../assets/pipeInstallation.jpg"; // Import the image for Pipe Installation
import cloggedDrainsImage from "../assets/fixCloggedDrains.jpg"; // Import the image for Fix Clogged Drains

const PlumbingServicesPage = () => {
  const services = [
    {
      name: "Leak Repairs",
      description: "Fixing leaks in pipes, faucets, and other plumbing fixtures.",
      priceRange: "₱500 - ₱1,500",
      duration: "1-2 hours",
      image: leakRepairImage, // Use the imported image
    },
    {
      name: "Pipe Installation",
      description: "Installing new pipes for water supply or drainage systems.",
      priceRange: "₱2,000 - ₱5,000",
      duration: "3-5 hours",
      image: pipeInstallationImage, // Use the imported image
    },
    {
      name: "Fix Clogged Drains",
      description: "Unclogging blocked drains and ensuring proper water flow.",
      priceRange: "₱800 - ₱2,000",
      duration: "1-3 hours",
      image: cloggedDrainsImage, // Use the imported image
    },
  ];

  const styles = {
    container: {
      padding: "20px",
    },
    titleSection: {
      textAlign: "center",
      margin: "20px 0",
    },
    title: {
      fontSize: "32px",
      fontWeight: "bold",
    },
    servicesSection: {
      display: "flex",
      justifyContent: "space-around",
      flexWrap: "wrap",
      gap: "20px",
      marginTop: "20px",
    },
    serviceBox: {
      width: "300px",
      border: "1px solid #ddd",
      borderRadius: "8px",
      overflow: "hidden",
      boxShadow: "0 4px 6px rgba(0, 0, 0, 0.1)",
      transition: "transform 0.3s ease, box-shadow 0.3s ease", // Smooth transition for hover effects
      cursor: "pointer", // Add pointer cursor on hover
    },
    serviceBoxHover: {
      boxShadow: "0 8px 16px rgba(244, 206, 20, 0.8)", // Glow effect with #F4CE14
      transform: "scale(1.05)", // Slightly enlarge the box on hover
    },
    serviceImage: {
      width: "100%",
      height: "200px",
      objectFit: "cover",
    },
    serviceDetails: {
      padding: "15px",
    },
    serviceName: {
      fontSize: "20px",
      fontWeight: "bold",
      marginBottom: "10px",
    },
    serviceDescription: {
      fontSize: "14px",
      marginBottom: "10px",
    },
    serviceInfo: {
      fontSize: "14px",
      color: "#555",
    },
  };

  return (
    <>
      <div style={styles.container}>
        {/* Service Category Title Section */}
        <div style={styles.titleSection}>
          <h1 style={styles.title}>Plumbing Services</h1>
        </div>

        {/* Services Section */}
        <div style={styles.servicesSection}>
          {services.map((service, index) => (
            <div
              key={index}
              style={styles.serviceBox}
              onMouseEnter={(e) => {
                e.currentTarget.style.boxShadow = styles.serviceBoxHover.boxShadow;
                e.currentTarget.style.transform = styles.serviceBoxHover.transform;
              }}
              onMouseLeave={(e) => {
                e.currentTarget.style.boxShadow = styles.serviceBox.boxShadow;
                e.currentTarget.style.transform = "scale(1)";
              }}
            >
              <img
                src={service.image}
                alt={service.name}
                style={styles.serviceImage}
              />
              <div style={styles.serviceDetails}>
                <h2 style={styles.serviceName}>{service.name}</h2>
                <p style={styles.serviceDescription}>{service.description}</p>
                <p style={styles.serviceInfo}>
                  <strong>Price Range:</strong> {service.priceRange}
                </p>
                <p style={styles.serviceInfo}>
                  <strong>Duration:</strong> {service.duration}
                </p>
              </div>
            </div>
          ))}
        </div>
      </div>
      <Footer /> {/* Display the footer */}
    </>
  );
};

export default PlumbingServicesPage;