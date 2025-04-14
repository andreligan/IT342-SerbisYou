import { Routes, Route, useNavigate, useLocation, Navigate } from "react-router-dom";
import LandingPage from "./components/LandingPage";
import SignupStepWizard from "./components/SignupStepWizard";
import LoginPopup from "./components/LoginPopup";
import CustomerHomePage from "./components/CustomerHomePage";
import ServiceProviderHomePage from "./components/service_provider/ServiceProviderHomePage";
import PlumbingServicesPage from "./components/PlumbingServicesPage";
import AddServicePage from "./components/service_provider/AddServicePage";
import LogoutConfirmationPopup from "./components/LogoutConfirmationPopup";
import { useState, useEffect } from "react";
import ServiceProviderProfile from "./components/service_provider/ServiceProviderProfile";
import API from "./utils/API";
import axios from "axios";

// Protected Route component for role-based access control
const ProtectedRoute = ({ element, allowedRoles }) => {
  const token = localStorage.getItem("authToken") || sessionStorage.getItem("authToken");
  const userRole = localStorage.getItem("userRole") || sessionStorage.getItem("userRole");
  
  // If not authenticated, redirect to landing page
  if (!token) {
    return <Navigate to="/" replace />;
  }
  
  // If role restriction exists and user's role is not allowed, redirect
  if (allowedRoles && !allowedRoles.includes(userRole?.toLowerCase())) {
    console.log(`Access denied: User role ${userRole} not allowed for this page`);
    // Redirect to appropriate home page based on role
    if (userRole?.toLowerCase() === 'customer') {
      return <Navigate to="/customerHomePage" replace />;
    } else if (userRole?.toLowerCase() === 'service provider') {
      return <Navigate to="/serviceProviderHomePage" replace />;
    } else {
      return <Navigate to="/" replace />;
    }
  }
  
  // If authenticated and authorized, render the requested component
  return element;
};

function App() {
  const [isAuthenticated, setIsAuthenticated] = useState(false);
  const [userRole, setUserRole] = useState(null); // Add state for user role
  const [isLoginPopupVisible, setIsLoginPopupVisible] = useState(false);
  const [isLogoutPopupVisible, setIsLogoutPopupVisible] = useState(false);
  const navigate = useNavigate();
  const location = useLocation();

  // Update the dependency array to include an empty dependency
  useEffect(() => {
    const token = localStorage.getItem("authToken") || sessionStorage.getItem("authToken");
    const role = localStorage.getItem("userRole") || sessionStorage.getItem("userRole");
    
    setIsAuthenticated(!!token);
    setUserRole(role);
    console.log("Authentication check. isAuthenticated:", !!token, "Role:", role);
  }, [location, /* empty dependency to run on mount as well */]);

  const handleLogout = () => {
    setIsLogoutPopupVisible(true);
  };

  const confirmLogout = () => {
    console.log("User has logged out.");
    // Clear all authentication data
    localStorage.removeItem("authToken");
    localStorage.removeItem("userRole");
    localStorage.removeItem("isAuthenticated");
    sessionStorage.removeItem("authToken");
    sessionStorage.removeItem("userRole");
    sessionStorage.removeItem("isAuthenticated");
    
    setIsAuthenticated(false);
    setUserRole(null);
    setIsLogoutPopupVisible(false);
    navigate("/");
  };

  // Define your styles here
  const styles = {
    appHeader: {
      display: 'flex',
      justifyContent: 'space-between',
      alignItems: 'center',
      padding: '1rem 2rem',
      backgroundColor: '#fff',
      boxShadow: '0 2px 4px rgba(0,0,0,0.1)'
    },
    leftSection: {
      display: 'flex',
      alignItems: 'center',
    },
    logo: {
      height: '40px',
      marginRight: '10px'
    },
    rightSection: {
      display: 'flex',
      gap: '10px'
    },
    button: {
      padding: '8px 16px',
      backgroundColor: '#F4CE14',
      color: '#000',
      border: 'none',
      borderRadius: '4px',
      cursor: 'pointer'
    },
    buttonHover: {
      backgroundColor: '#e0b813'
    }
  };

  const HeaderContent = ({ isAuthenticated, userRole, onLogout, onLoginPopup }) => {
    return (
      <>
        <div style={styles.leftSection}>
          <img src="SerbisYo Logo.png" alt="SerbisYo Logo" style={styles.logo} />
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
            <>
              {/* Optional: You can add role-specific navigation links here */}
              <button
                style={styles.button}
                onMouseOver={(e) => (e.target.style.backgroundColor = styles.buttonHover.backgroundColor)}
                onMouseOut={(e) => (e.target.style.backgroundColor = styles.button.backgroundColor)}
                onClick={onLogout}
              >
                Logout
              </button>
            </>
          )}
        </div>
      </>
    );
  };

  useEffect(() => {
    // Make axios use our configured instance with interceptors
    axios.defaults.baseURL = API.defaults.baseURL;
    axios.interceptors.response.handlers = API.interceptors.response.handlers;
    
    // Check token validity on app load
    const checkAuth = async () => {
      const token = localStorage.getItem("authToken") || sessionStorage.getItem("authToken");
      const role = localStorage.getItem("userRole") || sessionStorage.getItem("userRole");
      
      if (token) {
        try {
          // Make a lightweight call to verify token is still valid
          // If your API endpoint is returning 403, you might need to check the endpoint
          await axios.get('/api/user-auth/validate-token', {
            headers: { 'Authorization': `Bearer ${token}` }
          });
          
          // If validation succeeds, set authentication state
          setIsAuthenticated(true);
          setUserRole(role);
        } catch (error) {
          console.log("Token validation failed:", error.message);
          
          // Option 1: Trust the token exists and is valid, don't clear auth
          setIsAuthenticated(true);
          setUserRole(role);
          
          // Option 2: Clear auth data (uncomment if you want this behavior)
          /*
          localStorage.removeItem("authToken");
          localStorage.removeItem("userRole");
          localStorage.removeItem("userId");
          localStorage.removeItem("isAuthenticated");
          sessionStorage.removeItem("authToken");
          sessionStorage.removeItem("userRole");
          sessionStorage.removeItem("userId");
          sessionStorage.removeItem("isAuthenticated");
          
          setIsAuthenticated(false);
          setUserRole(null);
          */
        }
      }
    };
    
    checkAuth();
  }, []);

  return (
    <>
      <header style={styles.appHeader}>
        <HeaderContent
          isAuthenticated={isAuthenticated}
          userRole={userRole}
          onLogout={handleLogout}
          onLoginPopup={() => setIsLoginPopupVisible(true)}
        />
      </header>
      <Routes>
        {/* Public routes - accessible to anyone */}
        <Route path="/" element={<LandingPage />} />
        <Route path="/signup" element={<SignupStepWizard />} />
        
        {/* Protected routes with role-based access */}
        <Route 
          path="/customerHomePage" 
          element={<ProtectedRoute element={<CustomerHomePage />} allowedRoles={['customer']} />} 
        />
        <Route 
          path="/serviceProviderHomePage" 
          element={<ProtectedRoute element={<ServiceProviderHomePage />} allowedRoles={['service provider']} />} 
        />
        <Route 
          path="/plumbingServices" 
          element={<ProtectedRoute element={<PlumbingServicesPage />} allowedRoles={['customer']} />} 
        />
        <Route 
          path="/addService" 
          element={<ProtectedRoute element={<AddServicePage />} allowedRoles={['service provider']} />} 
        />
        <Route 
          path="/serviceProviderProfile"
          element={<ProtectedRoute element={<ServiceProviderProfile />} allowedRoles={['service provider']} />} 
        />
      </Routes>
      
      <LoginPopup
        open={isLoginPopupVisible}
        onClose={() => setIsLoginPopupVisible(false)}
      />
      <LogoutConfirmationPopup
        open={isLogoutPopupVisible}
        onClose={() => setIsLogoutPopupVisible(false)}
        onConfirm={confirmLogout}
      />
    </>
  );
}

export default App;