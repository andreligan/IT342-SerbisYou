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
import AdminHomePage from "./components/admin/AdminHomePage"; // Import AdminHomePage
import UserManagement from "./components/admin/UserManagement"; // Import UserManagement
import CategoryManagement from "./components/admin/CategoryManagement"; // Import CategoryManagement
import ProviderVerification from "./components/admin/ProviderVerification"; // Import ProviderVerification
import PlumbingServicesPage from "./components/PlumbingServicesPage";
import AddServicePage from "./components/service_provider/AddServicePage";
import LogoutConfirmationPopup from "./components/LogoutConfirmationPopup";
import { useState, useEffect } from "react";
import ServiceProviderProfile from "./components/service_provider/ServiceProviderProfile";
import CustomerProfilePage from "./components/customer/CustomerProfilePage";
import ServiceDetails from "./components/service_provider/ServiceDetails";
import ServiceProviderDetails from "./components/customer/ServiceProviderDetails";
import BookingDetailPage from "./components/customer/profile/BookingDetailPage"; // Import BookingDetailPage
import ServiceProviderBookings from "./components/service_provider/ServiceProviderBookings"; // Import ServiceProviderBookings
import serbisyoLogo from "./assets/Serbisyo_Logo_New.png";
import apiClient, { getApiUrl, API_BASE_URL, getImageUrl } from "./utils/apiConfig";
import ChatIcon from './components/chat/ChatIcon';
import ChatWindow from './components/chat/ChatWindow';
import OAuth2RedirectHandler from "./components/OAuth2RedirectHandler";
import NotificationIcon from "./components/notifications/NotificationIcon";
import NotificationsPage from "./components/notifications/NotificationsPage";
import PaymentSuccessPage from './components/payment/PaymentSuccessPage';
import PaymentCancelPage from './components/payment/PaymentCancelPage';
import { motion, AnimatePresence } from 'framer-motion';
import MandatoryPasswordChange from './components/MandatoryPasswordChange';
import RouteGuard from './components/RouteGuard';

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
    } else if (userRole?.toLowerCase() === "admin") {
      return <Navigate to="/adminHomePage" replace />;
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

  // Update this line to also check if we're on the add service page
  const isHeaderHidden = location.pathname === "/bookService" || location.pathname === "/addService";

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
        const userId = localStorage.getItem('userId') || sessionStorage.getItem('userId');
        const token = localStorage.getItem('authToken') || sessionStorage.getItem('authToken');
        const role = localStorage.getItem('userRole') || sessionStorage.getItem('userRole');
        
        if (!userId || !token || !role) {
          console.log("Missing auth info for profile image fetch");
          return;
        }
        
        let entityId;
        
        if (role.toLowerCase() === "customer") {
          try {
            // Get all customers to find the one matching this user
            const customersResponse = await apiClient.get(getApiUrl('customers/getAll'));
            
            // Ensure data is an array before using .find
            const customersData = Array.isArray(customersResponse.data) ? customersResponse.data : [];
            console.log("Customers data type:", typeof customersData, "Is array:", Array.isArray(customersData), "Length:", customersData.length);
            
            // Find the customer that matches this user ID
            const customer = customersData.find(c => 
              c.userAuth && c.userAuth.userId == userId
            );
            
            if (!customer) {
              console.log("No matching customer found for user ID:", userId);
              return;
            }
            entityId = customer.customerId;
            
            // Store the customer's first name
            setUserFirstName(customer.firstName || "");
            
            // Now fetch image with the correct customer ID
            const imageResponse = await apiClient.get(getApiUrl(`customers/getProfileImage/${entityId}`));
            
            if (imageResponse.data) {
              console.log("Image path received:", imageResponse.data);
              
              // Normalize image path using utility function
              const normalizedPath = getImageUrl(imageResponse.data);
              console.log("Normalized image path:", normalizedPath);
              
              setProfileImage(normalizedPath);
            }
          } catch (error) {
            console.error("Error fetching customer data:", error);
          }
        } 
        else if (role.toLowerCase() === "service provider") {
          try {
            // Get all providers to find the one matching this user
            const providersResponse = await apiClient.get(getApiUrl('service-providers/getAll'));
            
            // Ensure data is an array before using .find
            const providersData = Array.isArray(providersResponse.data) ? providersResponse.data : [];
            console.log("Providers data type:", typeof providersData, "Is array:", Array.isArray(providersData), "Length:", providersData.length);
            
            // Find the provider that matches this user ID
            const provider = providersData.find(p => 
              p.userAuth && p.userAuth.userId == userId
            );
            
            if (!provider) {
              console.log("No matching provider found for user ID:", userId);
              return;
            }
            entityId = provider.providerId;
            
            // Store the provider's first name
            setUserFirstName(provider.firstName || "");
            
            // Now fetch image with the correct provider ID
            const imageResponse = await apiClient.get(getApiUrl(`service-providers/getServiceProviderImage/${entityId}`));
            
            if (imageResponse.data) {
              console.log("Image path received:", imageResponse.data);
              
              // Normalize image path using utility function
              const normalizedPath = getImageUrl(imageResponse.data);
              console.log("Normalized image path:", normalizedPath);
              
              setProfileImage(normalizedPath);
            }
          } catch (error) {
            console.error("Error fetching provider data:", error);
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

  // Listen for requests to open the chat window
  useEffect(() => {
    const handleOpenChat = () => {
      setIsChatOpen(true);
    };
    
    window.addEventListener('openChatWithUser', handleOpenChat);
    
    return () => {
      window.removeEventListener('openChatWithUser', handleOpenChat);
    };
  }, []);

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

  const dropdownVariants = {
    hidden: { opacity: 0, y: -20, scale: 0.95 },
    visible: { 
      opacity: 1, 
      y: 0, 
      scale: 1,
      transition: {
        type: "spring",
        damping: 25,
        stiffness: 300
      }
    },
    exit: { 
      opacity: 0, 
      y: -10, 
      scale: 0.95,
      transition: { 
        duration: 0.2 
      }
    }
  };

  const renderNavigationLinks = () => {
    if (!isAuthenticated) return null;

    return (
      <div className="flex items-center gap-6 mr-6">
        {/* Home Icon with SVG glow effect */}
        <button
          onClick={() => navigate(userRole === "customer" ? "/customerHomePage" : userRole === "admin" ? "/adminHomePage" : "/serviceProviderHomePage")}
          className="p-2.5 rounded-full flex items-center justify-center"
          aria-label="Home"
        >
          <svg 
            xmlns="http://www.w3.org/2000/svg" 
            viewBox="0 0 24 24" 
            fill="none" 
            stroke="currentColor" 
            className="h-6 w-6 text-[#495E57] hover:text-[#F4CE14] transition-colors duration-200"
            strokeWidth="2" 
            strokeLinecap="round" 
            strokeLinejoin="round"
          >
            <path d="M3 9l9-7 9 7v11a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z"></path>
            <polyline points="9 22 9 12 15 12 15 22"></polyline>
          </svg>
        </button>

        {/* Notification Icon Wrapper with SVG glow effect */}
        <div className="relative">
          <div className="p-2.5 rounded-full flex items-center justify-center">
            <NotificationIcon glowEffect={true} />
          </div>
        </div>

        {/* Enhanced User Profile Dropdown */}
        <div className="relative">
          <button
            onMouseEnter={() => setDropdownOpen(true)}
            onMouseLeave={() => setTimeout(() => {
              if (!document.querySelector('.dropdown-menu:hover')) {
                setDropdownOpen(false);
              }
            }, 100)}
            className={`rounded-full overflow-hidden transition-all duration-200 transform hover:scale-105 ${
              dropdownOpen ? 'ring-2 ring-[#F4CE14] shadow-lg' : 'hover:shadow-md'
            }`}
            aria-label="User menu"
          >
            {profileImage ? (
              <div className="h-11 w-11 rounded-full overflow-hidden border-2 border-white shadow-inner">
                <img 
                  src={profileImage}
                  alt="Profile"
                  className="h-full w-full object-cover transform transition-transform duration-500 hover:scale-110"
                  onError={(e) => {
                    e.target.onerror = null;
                    setProfileImage(null);
                  }}
                />
              </div>
            ) : (
              <div className="h-11 w-11 rounded-full bg-gradient-to-br from-[#e0c03b] to-[#F4CE14] flex items-center justify-center shadow-md">
                <svg
                  xmlns="http://www.w3.org/2000/svg"
                  className="h-6 w-6 text-white"
                  viewBox="0 0 20 20"
                  fill="currentColor"
                >
                  <path
                    fillRule="evenodd"
                    d="M10 9a3 3 0 100-6 3 3 0 000 6zm-7 9a7 7 0 1114 0H3z"
                    clipRule="evenodd"
                  />
                </svg>
              </div>
            )}
            {userFirstName && (
              <span className="absolute -bottom-1 -right-1 h-4 w-4 bg-green-400 rounded-full border-2 border-white"></span>
            )}
          </button>
          
          <AnimatePresence>
            {dropdownOpen && (
              <motion.div 
                className="absolute right-0 mt-3 w-64 bg-white border border-gray-200 rounded-xl shadow-lg z-50 dropdown-menu transform transition-all duration-200 ease-in-out"
                variants={dropdownVariants}
                initial="hidden"
                animate="visible"
                exit="exit"
                onMouseEnter={() => setDropdownOpen(true)}
                onMouseLeave={() => setDropdownOpen(false)}
              >
                <div className="p-4 border-b border-gray-100 bg-gradient-to-r from-[#f6f2e1] to-white rounded-t-xl">
                  <div className="flex items-center space-x-4">
                    {profileImage ? (
                      <div className="h-14 w-14 rounded-full overflow-hidden border-2 border-[#F4CE14] shadow-md">
                        <img src={profileImage} alt="Profile" className="h-full w-full object-cover" />
                      </div>
                    ) : (
                      <div className="h-14 w-14 rounded-full bg-gradient-to-br from-[#e0c03b] to-[#F4CE14] flex items-center justify-center shadow-md">
                        <svg
                          xmlns="http://www.w3.org/2000/svg"
                          className="h-8 w-8 text-white"
                          viewBox="0 0 20 20"
                          fill="currentColor"
                        >
                          <path
                            fillRule="evenodd"
                            d="M10 9a3 3 0 100-6 3 3 0 000 6zm-7 9a7 7 0 1114 0H3z"
                            clipRule="evenodd"
                          />
                        </svg>
                      </div>
                    )}
                    <div>
                      <p className="font-medium text-gray-900">{userFirstName || "User"}</p>
                      <p className="text-xs text-gray-500 mt-1">{userRole?.toLowerCase() || "user"}</p>
                    </div>
                  </div>
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
                      <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={1.5} d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z" />
                    </svg>
                    Manage Profile
                  </button>
                  <button
                    onClick={() => setIsLogoutPopupVisible(true)}
                    className="w-full flex items-center px-4 py-3 text-left text-gray-700 hover:bg-gray-50 rounded-lg transition-colors duration-150"
                  >
                    <svg xmlns="http://www.w3.org/2000/svg" className="h-5 w-5 mr-3 text-gray-500" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                      <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={1.5} d="M17 16l4-4m0 0l-4-4m4 4H7m6 4v1a3 3 0 01-3 3H6a3 3 0 01-3-3V7a3 3 0 01-3-3h4a3 3 0 013 3v1" />
                    </svg>
                    Logout
                  </button>
                </div>
              </motion.div>
            )}
          </AnimatePresence>
        </div>
      </div>
    );
  };

  // Handle logo click to redirect to appropriate homepage
  const handleLogoClick = () => {
    if (isAuthenticated) {
      if (userRole?.toLowerCase() === "customer") {
        navigate("/customerHomePage");
      } else if (userRole?.toLowerCase() === "service provider") {
        navigate("/serviceProviderHomePage");
      } else if (userRole?.toLowerCase() === "admin") {
        navigate("/adminHomePage");
      }
    } else {
      navigate("/");
    }
  };

  return (
    <RouteGuard>
      <>
        {/* Only render header when not on booking page or add service page */}
        {!isHeaderHidden && (
          <header className="flex justify-between items-center px-4 py-2 bg-white shadow-md sticky top-0 z-30">
            <div 
              className="flex items-center cursor-pointer" 
              onClick={handleLogoClick}
              aria-label="Go to homepage"
            >
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
                  to={
                    userRole?.toLowerCase() === "customer" 
                      ? "/customerHomePage" 
                      : userRole?.toLowerCase() === "admin"
                        ? "/adminHomePage"
                        : "/serviceProviderHomePage"
                  } 
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
          <Route path="/booking-details/:bookingId" element={<BookingDetailPage />} /> {/* Add BookingDetailPage route */}
          <Route
            path="/serviceProviderBookings"
            element={<ProtectedRoute element={<ServiceProviderBookings />} allowedRoles={["service provider"]} />}
          />
          <Route
            path="/provider-booking-details/:bookingId"
            element={<ProtectedRoute element={<BookingDetailPage />} allowedRoles={["service provider"]} />}
          />
          <Route
            path="/adminHomePage"
            element={<ProtectedRoute element={<AdminHomePage />} allowedRoles={["admin"]} />}
          />
          <Route
            path="/adminHomePage/users"
            element={<ProtectedRoute element={<UserManagement />} allowedRoles={["admin"]} />}
          />
          <Route
            path="/adminHomePage/categories"
            element={<ProtectedRoute element={<CategoryManagement />} allowedRoles={["admin"]} />}
          />
          <Route
            path="/adminHomePage/verification"
            element={<ProtectedRoute element={<ProviderVerification />} allowedRoles={["admin"]} />}
          />
          <Route path="/change-password" element={<MandatoryPasswordChange />} />
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
    </RouteGuard>
  );
}

export default App;