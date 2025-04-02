import { Routes, Route, useNavigate, useLocation } from "react-router-dom";
import LandingPage from "./components/LandingPage";
import SignupStepWizard from "./components/SignupStepWizard";
import LoginPopup from "./components/LoginPopup";
import CustomerHomePage from "./components/CustomerHomePage";
import ServiceProviderHomePage from "./components/ServiceProviderHomePage";
import PlumbingServicesPage from "./components/PlumbingServicesPage";
import { useState, useEffect } from "react";

function App() {
  const [isAuthenticated, setIsAuthenticated] = useState(false); // Tracks if the user is logged in
  const [isLoginPopupVisible, setIsLoginPopupVisible] = useState(false);
  const navigate = useNavigate(); // React Router's navigation hook
  const location = useLocation(); // React Router's location hook

  // Check authentication status whenever the route changes
  useEffect(() => {
    const token = localStorage.getItem("authToken") || sessionStorage.getItem("authToken");
    setIsAuthenticated(!!token); // Update isAuthenticated based on token presence
    console.log("Route changed. isAuthenticated:", !!token); // Debug log
  }, [location]); // Re-run this effect whenever the route changes

  const handleLogout = () => {
    console.log("User has logged out."); // Log a message to confirm logout
    localStorage.removeItem("authToken"); // Remove the token from localStorage
    sessionStorage.removeItem("authToken"); // Remove the token from sessionStorage
    setIsAuthenticated(false); // Set authentication to false
    navigate("/"); // Redirect to the LandingPage
  };

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

  const HeaderContent = ({ isAuthenticated, onLogout, onLoginPopup }) => {
    return (
      <>
        <div style={styles.leftSection}>
          <img src="/logo.png" alt="SerbisYo Logo" style={styles.logo} />
          <h1>Serbisyo</h1>
        </div>
        <div style={styles.rightSection}>
          {!isAuthenticated ? (
            <>
              <button
                style={styles.button}
                onMouseOver={(e) => (e.target.style.backgroundColor = styles.buttonHover.backgroundColor)}
                onMouseOut={(e) => (e.target.style.backgroundColor = styles.button.backgroundColor)}
                onClick={() => navigate("/signup")}
              >
                Sign Up
              </button>
              <button
                style={styles.button}
                onMouseOver={(e) => (e.target.style.backgroundColor = styles.buttonHover.backgroundColor)}
                onMouseOut={(e) => (e.target.style.backgroundColor = styles.button.backgroundColor)}
                onClick={onLoginPopup}
              >
                Sign In
              </button>
            </>
          ) : (
            <button
              style={styles.button}
              onMouseOver={(e) => (e.target.style.backgroundColor = styles.buttonHover.backgroundColor)}
              onMouseOut={(e) => (e.target.style.backgroundColor = styles.button.backgroundColor)}
              onClick={onLogout}
            >
              Logout
            </button>
          )}
        </div>
      </>
    );
  };

  return (
    <>
      <header style={styles.appHeader}>
        <HeaderContent
          isAuthenticated={isAuthenticated} // Pass isAuthenticated as a prop
          onLogout={handleLogout}
          onLoginPopup={() => setIsLoginPopupVisible(true)}
        />
      </header>
      <Routes>
        <Route path="/" element={<LandingPage />} />
        <Route path="/signup" element={<SignupStepWizard />} />
        <Route path="/customerHomePage" element={<CustomerHomePage />} />
        <Route path="/serviceProviderHomePage" element={<ServiceProviderHomePage />} /> {/* New route */}
        <Route path="/plumbingServices" element={<PlumbingServicesPage />} />
      </Routes>
      <LoginPopup
        open={isLoginPopupVisible}
        onClose={() => setIsLoginPopupVisible(false)}
        onLogin={() => {
          console.log("LoginPopup: User logged in."); // Debug log for login
          setIsAuthenticated(true); // Set authentication to true upon successful login
          const token = localStorage.getItem("authToken") || sessionStorage.getItem("authToken");
          if (token) {
            // Redirect to the appropriate page based on role (if needed)
            navigate("/customerHomePage");
            navigate("/serviceProviderHomePage");
          }
        }}
      />
    </>
  );
}

export default App;