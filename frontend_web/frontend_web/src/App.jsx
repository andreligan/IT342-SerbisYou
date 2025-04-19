import { Routes, Route, useNavigate, useLocation, Navigate } from "react-router-dom";
import LandingPage from "./components/LandingPage";
import SignupStepWizard from "./components/SignupStepWizard";
import LoginPopup from "./components/LoginPopup";
import CustomerHomePage from "./components/CustomerHomePage";
import BrowseServicesPage from "./components/BrowseServicesPage";
import BookServicePage from "./components/BookServicePage";
import ServiceProviderHomePage from "./components/service_provider/ServiceProviderHomePage";
import PlumbingServicesPage from "./components/PlumbingServicesPage";
import AddServicePage from "./components/service_provider/AddServicePage";
import LogoutConfirmationPopup from "./components/LogoutConfirmationPopup";
import { useState, useEffect } from "react";
import ServiceProviderProfile from "./components/service_provider/ServiceProviderProfile";
import CustomerProfilePage from "./components/customer/CustomerProfilePage";
import ServiceDetails from "./components/service_provider/ServiceDetails";
import serbisyoLogo from "./assets/SerbisYo Logo.png";
import API from "./utils/API";
import axios from "axios";
import ChatIcon from './components/chat/ChatIcon';
import ChatWindow from './components/chat/ChatWindow';
import OAuth2RedirectHandler from './components/OAuth2RedirectHandler';

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
    if (userRole?.toLowerCase() === "customer") {
      return <Navigate to="/customerHomePage" replace />;
    } else if (userRole?.toLowerCase() === "service provider") {
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
  const [dropdownOpen, setDropdownOpen] = useState(false); // For user profile dropdown
  const [isChatOpen, setIsChatOpen] = useState(false); // State for chat window
  const navigate = useNavigate();
  const location = useLocation();

  // Update the dependency array to include an empty dependency
  useEffect(() => {
    const token = localStorage.getItem("authToken") || sessionStorage.getItem("authToken");
    const role = localStorage.getItem("userRole") || sessionStorage.getItem("userRole");

    setIsAuthenticated(!!token);
    setUserRole(role);
    console.log("Authentication check. isAuthenticated:", !!token, "Role:", role);
  }, [location]);

  const confirmLogout = () => {
    console.log("User has logged out.");
    // Clear all authentication data
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
    setDropdownOpen(false); // Close the profile menu
    setIsLogoutPopupVisible(false); // Close the logout confirmation popup
    navigate("/");
  };

  const toggleDropdown = () => {
    setDropdownOpen(!dropdownOpen);
  };

  const toggleChat = () => {
    setIsChatOpen(!isChatOpen);
  };

  const renderNavigationLinks = () => {
    if (!isAuthenticated) return null;
  
    return (
      <div className="flex items-center gap-6">
        {/* Home Link */}
        <button
          onClick={() => navigate(userRole === "customer" ? "/customerHomePage" : "/serviceProviderHomePage")}
          className="p-2 rounded-full hover:bg-gray-200"
        >
          <svg
            xmlns="http://www.w3.org/2000/svg"
            className="h-6 w-6"
            fill="none"
            viewBox="0 0 24 24"
            stroke="currentColor"
          >
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M3 10l9-7 9 7v11a2 2 0 01-2 2H5a2 2 0 01-2-2V10z" />
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 21V9h6v12" />
          </svg>
        </button>

        {/* Chat/Messages Link (Exclude for Admin) */}
        {userRole !== "admin" && (
          <button
            onClick={() => navigate("/messages")}
            className="p-2 rounded-full hover:bg-gray-200"
          >
            <svg
              xmlns="http://www.w3.org/2000/svg"
              className="h-6 w-6"
              fill="none"
              viewBox="0 0 24 24"
              stroke="currentColor"
            >
              <path
                strokeLinecap="round"
                strokeLinejoin="round"
                strokeWidth={2}
                d="M8 10h.01M12 10h.01M16 10h.01M21 16.5a2.5 2.5 0 01-2.5 2.5H5.5A2.5 2.5 0 013 16.5V5.5A2.5 2.5 0 015.5 3h13a2.5 2.5 0 012.5 2.5v11z"
              />
            </svg>
          </button>
        )}

        {/* Notifications Link */}
        <button
          onClick={() => navigate("/notifications")}
          className="p-2 rounded-full hover:bg-gray-200"
        >
          <svg
            xmlns="http://www.w3.org/2000/svg"
            className="h-6 w-6"
            fill="none"
            viewBox="0 0 24 24"
            stroke="currentColor"
          >
            <path
              strokeLinecap="round"
              strokeLinejoin="round"
              strokeWidth={2}
              d="M15 17h5l-1.405-1.405A2.032 2.032 0 0118 14.158V11a6.002 6.002 0 00-4-5.659V4a2 2 0 10-4 0v1.341C7.67 6.165 6 8.388 6 11v3.159c0 .538-.214 1.055-.595 1.436L4 17h5m6 0a3 3 0 11-6 0m6 0H9"
            />
          </svg>
        </button>
  
        {/* User Profile Dropdown */}
        <div className="relative">
          <button
            onClick={toggleDropdown}
            className="p-2 rounded-full hover:bg-gray-200"
          >
            <svg
              xmlns="http://www.w3.org/2000/svg"
              className="h-6 w-6"
              fill="none"
              viewBox="0 0 24 24"
              stroke="currentColor"
            >
              <path
                strokeLinecap="round"
                strokeLinejoin="round"
                strokeWidth={2}
                d="M12 12c2.21 0 4-1.79 4-4s-1.79-4-4-4-4 1.79-4 4 1.79 4 4 4zm0 2c-2.67 0-8 1.34-8 4v2h16v-2c0-2.66-5.33-4-8-4z"
              />
            </svg>
          </button>
          {dropdownOpen && (
            <div className="absolute right-0 mt-2 w-48 bg-white border border-gray-300 rounded-lg shadow-lg z-50">
              <div className="p-4 border-b border-gray-200">
                <p className="text-sm font-semibold text-gray-800">User Menu</p>
              </div>
              <button
                onClick={() => {
                  const normalizedRole = userRole?.toLowerCase();
                  setDropdownOpen(false);
                  if (normalizedRole === "customer") {
                    navigate("/customerProfile");
                  } else if (normalizedRole === "service provider") {
                    navigate("/serviceProviderProfile");
                  } else {
                    console.error("Unknown user role:", userRole);
                  }
                }}
                className="block w-full px-4 py-2 text-left text-gray-700 hover:bg-gray-100"
              >
                Manage Profile
              </button>
              <button
                onClick={() => setIsLogoutPopupVisible(true)} // Show the logout confirmation popup
                className="block w-full px-4 py-2 text-left text-gray-700 hover:bg-gray-100"
              >
                Logout
              </button>
            </div>
          )}
        </div>
      </div>
    );
  };

  return (
    <>
      <header className="flex justify-between items-center px-8 py-4 bg-white shadow-md">
        <div className="flex items-center">
          <img src={serbisyoLogo} alt="SerbisYo Logo" className="h-12 mr-4" />
          <h1 className="text-2xl font-bold text-gray-800">Serbisyo</h1>
        </div>
        <div>
          {!isAuthenticated ? (
            <div className="flex gap-4">
              <button
                onClick={() => navigate("/signup")}
                className="px-4 py-2 bg-yellow-400 text-black rounded hover:bg-yellow-500"
              >
                Sign Up
              </button>
              <button
                onClick={() => setIsLoginPopupVisible(true)}
                className="px-4 py-2 bg-yellow-400 text-black rounded hover:bg-yellow-500"
              >
                Sign In
              </button>
            </div>
          ) : (
            renderNavigationLinks()
          )}
        </div>
      </header>
      <Routes>
        {/* Public routes */}
        <Route 
          path="/" 
          element={
            isAuthenticated ? 
              <Navigate 
                to={userRole?.toLowerCase() === "customer" 
                  ? "/customerHomePage" 
                  : "/serviceProviderHomePage"} 
                replace 
              /> 
              : <LandingPage />
          } 
        />
        <Route path="/signup/*" element={<SignupStepWizard />} />
        <Route path="/oauth2/redirect" element={<OAuth2RedirectHandler />} />

        {/* Protected routes */}
        <Route
          path="/customerHomePage"
          element={<ProtectedRoute element={<CustomerHomePage />} allowedRoles={["customer"]} />}
        />
        <Route
          path="/customerProfile"
          element={<ProtectedRoute element={<CustomerProfilePage />} allowedRoles={["customer"]} />}
        />
        <Route
          path="/browseServices"
          element={<ProtectedRoute element={<BrowseServicesPage />} allowedRoles={["customer"]} />}
        />
        <Route
          path="/bookService"
          element={<ProtectedRoute element={<BookServicePage />} allowedRoles={["customer"]} />}
        />
        <Route
          path="/serviceProviderHomePage"
          element={<ProtectedRoute element={<ServiceProviderHomePage />} allowedRoles={["service provider"]} />}
        />
        <Route
          path="/plumbingServices"
          element={<ProtectedRoute element={<PlumbingServicesPage />} allowedRoles={["customer"]} />}
        />
        <Route
          path="/addService"
          element={<ProtectedRoute element={<AddServicePage />} allowedRoles={["service provider"]} />}
        />
        <Route
          path="/serviceProviderProfile"
          element={<ProtectedRoute element={<ServiceProviderProfile />} allowedRoles={["service provider"]} />}
        />
        <Route
          path="/service/:serviceId"
          element={<ProtectedRoute element={<ServiceDetails />} allowedRoles={["service provider"]} />}
        />
      </Routes>

      {/* Chat components */}
      {isAuthenticated && (
        <>
          <ChatIcon isOpen={isChatOpen} onClick={toggleChat} />
          {isChatOpen && <ChatWindow onClose={toggleChat} />}
        </>
      )}

      {/* Login Popup */}
      <LoginPopup
        open={isLoginPopupVisible}
        onClose={() => setIsLoginPopupVisible(false)}
      />

      {/* Logout Confirmation Popup */}
      <LogoutConfirmationPopup
        open={isLogoutPopupVisible}
        onClose={() => setIsLogoutPopupVisible(false)}
        onConfirm={confirmLogout}
      />
    </>
  );
}

export default App;