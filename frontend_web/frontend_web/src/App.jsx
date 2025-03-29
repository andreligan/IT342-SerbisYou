import { BrowserRouter as Router, Routes, Route } from "react-router-dom";
import LandingPage from "./components/LandingPage"; // Import the LandingPage component
import { useState } from "react";

function App() {
  const [userRole, setUserRole] = useState(null); // null means no user is logged in

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

  const renderHeaderContent = () => {
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
                onClick={() => setUserRole("SignIn")}
              >
                Sign In
              </button>
              <button
                style={styles.button}
                onMouseOver={(e) => (e.target.style.backgroundColor = styles.buttonHover.backgroundColor)}
                onMouseOut={(e) => (e.target.style.backgroundColor = styles.button.backgroundColor)}
                onClick={() => setUserRole("SignUp")}
              >
                Sign Up
              </button>
            </div>
          </>
        );
    }
  };

  return (
    <Router>
      <header style={styles.appHeader}>
        {renderHeaderContent()}
      </header>
      <Routes>
        <Route path="/" element={<LandingPage />} />
        {/* Add more routes here as needed */}
      </Routes>
    </Router>
  );
}

export default App;