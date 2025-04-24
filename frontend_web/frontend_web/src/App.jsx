import { Routes, Route, useNavigate, useLocation, Navigate } from "react-router-dom";
import LandingPage from "./components/LandingPage";
import SignupStepWizard from "./components/SignupStepWizard";
import SignupOptionsPopup from "./components/SignupOptionsPopup"; // Update import
import OAuthRoleSelection from "./components/OAuthRoleSelection";
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
import ServiceProviderDetails from "./components/customer/ServiceProviderDetails";
import serbisyoLogo from "./assets/Serbisyo_Logo_New.png";
import API from "./utils/API";
import axios from "axios";
import ChatIcon from './components/chat/ChatIcon';
import ChatWindow from './components/chat/ChatWindow';
import OAuth2RedirectHandler from "./components/OAuth2RedirectHandler";
import NotificationIcon from "./components/notifications/NotificationIcon";
import NotificationsPage from "./components/notifications/NotificationsPage";
import PaymentSuccessPage from './components/payment/PaymentSuccessPage';
import PaymentCancelPage from './components/payment/PaymentCancelPage';

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
  const [userRole, setUserRole] = useState(null);
  const [isLoginPopupVisible, setIsLoginPopupVisible] = useState(false);
  const [isLogoutPopupVisible, setIsLogoutPopupVisible] = useState(false);
  const [dropdownOpen, setDropdownOpen] = useState(false);
  const [isChatOpen, setIsChatOpen] = useState(false);
  const [isSignupPopupVisible, setIsSignupPopupVisible] = useState(false); // Add state for signup popup visibility
  const [profileImage, setProfileImage] = useState(null); // Add new state for profile image
  const [userFirstName, setUserFirstName] = useState(""); // Add state for user's first name
  const navigate = useNavigate();
  const location = useLocation();

  // Add this line to check if we're on the booking page
  const isBookingPage = location.pathname === "/bookService";

  useEffect(() => {
    const token = localStorage.getItem("authToken") || sessionStorage.getItem("authToken");
    const role = localStorage.getItem("userRole") || sessionStorage.getItem("userRole");

    setIsAuthenticated(!!token);
    setUserRole(role);
    console.log("Authentication check. isAuthenticated:", !!token, "Role:", role);
  }, [location]);

  // Fetch user's profile image - updated to also fetch first name
  useEffect(() => {
    const fetchProfileImage = async () => {
      try {
        const token = localStorage.getItem("authToken") || sessionStorage.getItem("authToken");
        const userId = localStorage.getItem("userId") || sessionStorage.getItem("userId");
        const role = localStorage.getItem("userRole") || sessionStorage.getItem("userRole");
        
        if (!token || !userId || !role) return;
        
        let entityId;
        
        if (role.toLowerCase() === "customer") {
          // Get all customers to find the one matching this user
          const customersResponse = await axios.get("/api/customers/getAll", {
            headers: { Authorization: `Bearer ${token}` }
          });
          
          // Find the customer that matches this user ID
          const customer = customersResponse.data.find(c => 
            c.userAuth && c.userAuth.userId == userId
          );
          
          if (!customer) return;
          entityId = customer.customerId;
          
          // Store the customer's first name
          setUserFirstName(customer.firstName || "");
          
          // Now fetch image with the correct customer ID
          const imageResponse = await axios.get(`/api/customers/getProfileImage/${entityId}`, {
            headers: { Authorization: `Bearer ${token}` }
          });
          
          if (imageResponse.data) {
            // Prepend base URL to make a complete image path
            const baseURL = "http://localhost:8080"; // Backend base URL
            const fullImageURL = `${baseURL}${imageResponse.data}`;
            setProfileImage(fullImageURL);
          }
        } 
        else if (role.toLowerCase() === "service provider") {
          // Get all providers to find the one matching this user
          const providersResponse = await axios.get("/api/service-providers/getAll", {
            headers: { Authorization: `Bearer ${token}` }
          });
          
          // Find the provider that matches this user ID
          const provider = providersResponse.data.find(p => 
            p.userAuth && p.userAuth.userId == userId
          );
          
          if (!provider) return;
          entityId = provider.providerId;
          
          // Store the provider's first name
          setUserFirstName(provider.firstName || "");
          
          // Now fetch image with the correct provider ID
          const imageResponse = await axios.get(`/api/service-providers/getServiceProviderImage/${entityId}`, {
            headers: { Authorization: `Bearer ${token}` }
          });
          
          if (imageResponse.data) {
            // Prepend base URL to make a complete image path
            const baseURL = "http://localhost:8080"; // Backend base URL
            const fullImageURL = `${baseURL}${imageResponse.data}`;
            setProfileImage(fullImageURL);
          }
        }
      } catch (error) {
        console.error("Error fetching profile image:", error);
      }
    };
    
    if (isAuthenticated) {
      fetchProfileImage();
    }
  }, [isAuthenticated]);

  const confirmLogout = () => {
    console.log("User has logged out.");
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
    setDropdownOpen(false);
    setIsLogoutPopupVisible(false);
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
      <div className="flex items-center gap-6 mr-6">
        <button
          onClick={() => navigate(userRole === "customer" ? "/customerHomePage" : "/serviceProviderHomePage")}
          className="p-2 rounded-full hover:bg-gray-200 transition-colors duration-200 flex items-center justify-center"
          aria-label="Home"
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

        {/* {userRole !== "admin" && (
          <button
            onClick={() => navigate("/messages")}
            className="p-2 rounded-full hover:bg-gray-200 transition-colors duration-200 flex items-center justify-center"
            aria-label="Messages"
          >
            <svg
              xmlns="http://www.w3.org/2000/svg"
              className="h-7 w-7"
              fill="none"
              viewBox="0 0 24 24"
              stroke="currentColor"
            >
              <path
                strokeLinecap="round"
                strokeLinejoin="round"
                strokeWidth={1.5}
                d="M8 10h.01M12 10h.01M16 10h.01M21 16.5a2.5 2.5 0 01-2.5 2.5H5.5A2.5 2.5 0 013 16.5V5.5A2.5 2.5 0 015.5 3h13a2.5 2.5 0 012.5 2.5v11z"
              />
            </svg>
          </button>
        )} */}

        {/* Replace the notification button with our new NotificationIcon component */}
        <NotificationIcon />

        <div className="relative">
          <button
            onMouseEnter={() => setDropdownOpen(true)}
            onMouseLeave={() => setTimeout(() => {
              // Small timeout to allow cursor to move to dropdown
              if (!document.querySelector('.dropdown-menu:hover')) {
                setDropdownOpen(false);
              }
            }, 100)}
            className={`rounded-full overflow-hidden hover:ring-2 hover:ring-[#F4CE14] transition-all duration-200 ${dropdownOpen ? 'ring-2 ring-[#F4CE14]' : ''}`}
            aria-label="User menu"
          >
            {profileImage ? (
              <div className="h-11 w-11 rounded-full border-2 border-[#F4CE14] overflow-hidden">
                <img 
                  src={profileImage}
                  alt="Profile"
                  className="h-full w-full object-cover"
                  onError={(e) => {
                    // Fallback to default icon if image fails to load
                    e.target.onerror = null;
                    setProfileImage(null);
                  }}
                />
              </div>
            ) : (
              <div className="h-11 w-11 rounded-full border-2 border-[#F4CE14] bg-gray-100 flex items-center justify-center">
                <svg
                  xmlns="http://www.w3.org/2000/svg"
                  className="h-7 w-7 text-gray-500"
                  fill="none"
                  viewBox="0 0 24 24"
                  stroke="currentColor"
                >
                  <path
                    strokeLinecap="round"
                    strokeLinejoin="round"
                    strokeWidth={2}
                    d="M12 12c2.21 0 4-1.79 4-4s-1.79-4-4-4-4 1.79-4 4 1.79-4 4 4zm0 2c-2.67 0-8 1.34-8 4v2h16v-2c0-2.66-5.33-4-8-4z"
                  />
                </svg>
              </div>
            )}
          </button>
          {dropdownOpen && (
            <div 
              className="absolute right-0 mt-3 w-64 bg-white border border-gray-200 rounded-xl shadow-lg z-50 dropdown-menu transform transition-all duration-200 ease-in-out"
              onMouseEnter={() => setDropdownOpen(true)}
              onMouseLeave={() => setDropdownOpen(false)}
            >
              <div className="p-4 border-b border-gray-100">
                {profileImage && (
                  <div className="flex items-center space-x-4">
                    <div className="h-12 w-12 rounded-full overflow-hidden border-2 border-[#F4CE14]">
                      <img src={profileImage} alt="Profile" className="h-full w-full object-cover" />
                    </div>
                    <div>
                      {/* Display first name instead of role */}
                      <p className="text-sm font-medium text-gray-900">{userFirstName || "User"}</p>
                    </div>
                  </div>
                )}
              </div>
              <div className="p-2">
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
                  className="w-full flex items-center px-4 py-3 text-left text-gray-700 hover:bg-gray-50 rounded-lg transition-colors duration-150"
                >
                  <svg xmlns="http://www.w3.org/2000/svg" className="h-5 w-5 mr-3 text-gray-500" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z" />
                  </svg>
                  Manage Profile
                </button>
                <button
                  onClick={() => setIsLogoutPopupVisible(true)}
                  className="w-full flex items-center px-4 py-3 text-left text-gray-700 hover:bg-gray-50 rounded-lg transition-colors duration-150"
                >
                  <svg xmlns="http://www.w3.org/2000/svg" className="h-5 w-5 mr-3 text-gray-500" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M17 16l4-4m0 0l-4-4m4 4H7m6 4v1a3 3 0 01-3 3H6a3 3 0 01-3-3V7a3 3 0 013-3h4a3 3 0 013 3v1" />
                  </svg>
                  Logout
                </button>
              </div>
            </div>
          )}
        </div>
      </div>
    );
  };

  return (
    <>
      {/* Only render header when not on booking page */}
      {!isBookingPage && (
        <header className="flex justify-between items-center px-4 py-2 bg-white shadow-md sticky top-0 z-30">
          <div className="flex items-center">
            <img src={serbisyoLogo} alt="SerbisYo Logo" className="h-16 ml-4 mr-2" />
          </div>
          <div>
            {!isAuthenticated ? (
              <div className="flex gap-4 mr-6">
                <button
                  onClick={() => setIsSignupPopupVisible(true)}
                  className="px-5 py-2 bg-[#F4CE14] text-[#495E57] font-medium rounded-lg hover:bg-yellow-500 transition-colors duration-200 shadow-sm"
                >
                  Sign Up
                </button>
                <button
                  onClick={() => setIsLoginPopupVisible(true)}
                  className="px-5 py-2 bg-[#495E57] text-white font-medium rounded-lg hover:bg-[#3a4a45] transition-colors duration-200 shadow-sm"
                >
                  Sign In
                </button>
              </div>
            ) : (
              renderNavigationLinks()
            )}
          </div>
        </header>
      )}
      <Routes>
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
        <Route path="/oauth-role-selection" element={<OAuthRoleSelection />} />
        <Route path="/oauth2/redirect" element={<OAuth2RedirectHandler />} />
        <Route
          path="/customerHomePage"
          element={<ProtectedRoute element={<CustomerHomePage />} allowedRoles={["customer"]} />}
        />
        <Route
          path="/customerProfile"
          element={<ProtectedRoute element={<CustomerProfilePage />} allowedRoles={["customer"]} />}
        />
        <Route
          path="/customerProfile/:tab"
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
          path="/serviceProviderProfile/:tab"
          element={<ProtectedRoute element={<ServiceProviderProfile />} allowedRoles={["service provider"]} />}
        />
        <Route
          path="/service/:serviceId"
          element={<ProtectedRoute element={<ServiceDetails />} allowedRoles={["service provider"]} />}
        />
        <Route
          path="/notifications"
          element={<ProtectedRoute element={<NotificationsPage />} allowedRoles={["customer", "service provider"]} />}
        />
        <Route
          path="/providerDetails/:providerId"
          element={<ProtectedRoute element={<ServiceProviderDetails />} allowedRoles={["customer"]} />}
        />
        <Route path="/payment-success" element={<PaymentSuccessPage />} />
        <Route path="/payment-cancel" element={<PaymentCancelPage />} />
      </Routes>

      <SignupOptionsPopup
        open={isSignupPopupVisible}
        onClose={() => setIsSignupPopupVisible(false)}
      />

      <LoginPopup
        open={isLoginPopupVisible}
        onClose={() => setIsLoginPopupVisible(false)}
      />

      <LogoutConfirmationPopup
        open={isLogoutPopupVisible}
        onClose={() => setIsLogoutPopupVisible(false)}
        onConfirm={confirmLogout}
      />

      {isAuthenticated && (
        <>
          <ChatIcon isOpen={isChatOpen} onClick={toggleChat} />
          {isChatOpen && <ChatWindow onClose={toggleChat} />}
        </>
      )}
    </>
  );
}

export default App;