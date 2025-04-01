import { BrowserRouter as Router, Routes, Route, useNavigate } from "react-router-dom";
import LandingPage from "./components/LandingPage"; // Import the LandingPage component
import SignupStepWizard from "./components/SignupStepWizard";
import LoginPopup from "./components/LoginPopup"; // Import the LoginPopup component
import CustomerHomePage from "./components/CustomerHomePage";
import { useState } from "react";

function App() {
  const [userRole, setUserRole] = useState(null); // null means no user is logged in
  const [isLoginPopupVisible, setIsLoginPopupVisible] = useState(false); // State to control LoginPopup visibility

  const styles = {
    appHeader: {
      display: "flex",
      justifyContent: "space-between",
      alignItems: "center",
      padding: "10px 20px",
      backgroundColor: "#f8f9fa",
      borderBottom: "1px solid #ddd",
    },
    leftSection: {
      display: "flex",
      alignItems: "center",
      gap: "10px",
    },
    logo: {
      width: "50px",
      height: "auto",
    },
    rightSection: {
      display: "flex",
      gap: "10px",
    },
    button: {
      padding: "8px 16px",
      border: "none",
      borderRadius: "4px",
      backgroundColor: "#007bff",
      color: "white",
      cursor: "pointer",
      fontSize: "14px",
    },
    buttonHover: {
      backgroundColor: "#0056b3",
    },
  };

  const HeaderContent = () => {
    const navigate = useNavigate(); // Hook for navigation

    switch (userRole) {
      case "Admin":
        return <p>Welcome, Admin! Manage the platform here.</p>;
      case "Customer":
        return <p>Welcome, Customer! Explore our services.</p>;
      case "Service Provider":
        return <p>Welcome, Service Provider! Manage your services here.</p>;
      default:
        return (
          <>
            <div style={styles.leftSection}>
              <img src="/logo.png" alt="SerbisYo Logo" style={styles.logo} />
              <h1>Serbisyo</h1>
            </div>
            <div style={styles.rightSection}>
              <button
                style={styles.button}
                onMouseOver={(e) => (e.target.style.backgroundColor = styles.buttonHover.backgroundColor)}
                onMouseOut={(e) => (e.target.style.backgroundColor = styles.button.backgroundColor)}
                onClick={() => navigate("/signup")} // Navigate to the signup page
              >
                Sign Up
              </button>
              <button
                style={styles.button}
                onMouseOver={(e) => (e.target.style.backgroundColor = styles.buttonHover.backgroundColor)}
                onMouseOut={(e) => (e.target.style.backgroundColor = styles.button.backgroundColor)}
                onClick={() => setIsLoginPopupVisible(true)} // Show the LoginPopup
              >
                Sign In
              </button>
            </div>
          </>
        );
    }
  };

  return (
    <>
      <header style={styles.appHeader}>
        <HeaderContent />
      </header>
      <Routes>
        <Route path="/" element={<LandingPage />} />
        <Route path="/signup" element={<SignupStepWizard />} />
        <Route path="/customer-home" element={<CustomerHomePage />} /> {/* Add more routes here as needed */}
        {/* Add more routes here as needed */}
      </Routes>
      <LoginPopup 
        open={isLoginPopupVisible} // Pass the open state to the LoginPopup
        onClose={() => setIsLoginPopupVisible(false)} // Pass a callback to close the popup
      />
    </>
  );
}

export default App;