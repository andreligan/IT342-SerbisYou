import React, { useState, useEffect } from "react";
import { useNavigate } from "react-router-dom";
import CustomerChangePasswordContent from "./customer/profile/CustomerChangePasswordContent";
import ChangePasswordContent from "./service_provider/profile/ChangePasswordContent";

const MandatoryPasswordChange = () => {
  const [userRole, setUserRole] = useState("");
  const navigate = useNavigate();

  useEffect(() => {
    // Get user role from localStorage or sessionStorage
    const role = localStorage.getItem("userRole") || sessionStorage.getItem("userRole");
    const isOAuthNew = localStorage.getItem("isOAuthNew") === "true";
    
    if (!isOAuthNew) {
      // If not a new OAuth user, redirect to appropriate homepage
      redirectToHomepage(role);
      return;
    }
    
    setUserRole(role);
  }, []);

  const redirectToHomepage = (role) => {
    if (role === "Customer") {
      navigate("/customerHomePage");
    } else if (role === "Service Provider") {
      navigate("/serviceProviderHomePage");
    } else {
      navigate("/");
    }
  };

  const handlePasswordChangeSuccess = () => {
    // Remove the OAuth new user flag
    localStorage.removeItem("isOAuthNew");
    sessionStorage.removeItem("isOAuthNew");
    
    // Redirect to appropriate homepage based on role
    redirectToHomepage(userRole);
  };

  return (
    <div className="min-h-screen bg-gray-100 py-12 px-4 sm:px-6 lg:px-8">
      <div className="max-w-4xl mx-auto">
        <div className="mb-8 text-center">
          <h1 className="text-3xl font-bold text-[#495E57]">Almost there!</h1>
          <p className="text-xl text-gray-600 mt-2">
            For your security, please change your default password before continuing.
          </p>
        </div>

        {userRole === "Customer" ? (
          <PasswordChangeWrapper
            Component={CustomerChangePasswordContent}
            onSuccess={handlePasswordChangeSuccess}
          />
        ) : userRole === "Service Provider" ? (
          <PasswordChangeWrapper
            Component={ChangePasswordContent}
            onSuccess={handlePasswordChangeSuccess}
          />
        ) : (
          <div className="text-center">Loading...</div>
        )}
      </div>
    </div>
  );
};

// Wrapper that provides success handling for the password change components
const PasswordChangeWrapper = ({ Component, onSuccess }) => {
  const [hasChanged, setHasChanged] = useState(false);
  
  // Intercept the success message from the password change components
  const handleSuccess = (message) => {
    setHasChanged(true);
    setTimeout(() => {
      onSuccess();
    }, 2000);
    return message;
  };

  return (
    <div>
      {hasChanged && (
        <div className="bg-green-50 border-l-4 border-green-500 p-4 mb-6 text-green-700">
          Password changed successfully! Redirecting you...
        </div>
      )}
      <Component interceptSuccessMessage={handleSuccess} isMandatory={true} />
    </div>
  );
};

export default MandatoryPasswordChange;
