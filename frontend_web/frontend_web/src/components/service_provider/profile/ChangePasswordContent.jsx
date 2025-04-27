import React, { useState } from "react";
import axios from "axios";

const ChangePasswordContent = ({ interceptSuccessMessage, isMandatory }) => {
  const [currentPassword, setCurrentPassword] = useState("");
  const [newPassword, setNewPassword] = useState("");
  const [confirmPassword, setConfirmPassword] = useState("");
  const [successMessage, setSuccessMessage] = useState("");
  const [errorMessage, setErrorMessage] = useState("");
  const [isLoading, setIsLoading] = useState(false);

  // State to toggle password visibility
  const [showCurrentPassword, setShowCurrentPassword] = useState(false);
  const [showNewPassword, setShowNewPassword] = useState(false);
  const [showConfirmPassword, setShowConfirmPassword] = useState(false);

  const handleChangePassword = async () => {
    if (!currentPassword || !newPassword || !confirmPassword) {
      setErrorMessage("All fields are required.");
      return;
    }

    if (newPassword !== confirmPassword) {
      setErrorMessage("New password and confirm password do not match.");
      return;
    }

    try {
      setIsLoading(true);
      const token = localStorage.getItem("authToken") || sessionStorage.getItem("authToken");
      const authId = localStorage.getItem("userId") || sessionStorage.getItem("userId");

      if (!token || !authId) {
        setErrorMessage("Authentication token or user ID not found.");
        setIsLoading(false);
        return;
      }

      // Call the backend API to change the password
      const response = await axios.put(
        `/api/user-auth/change-password/${authId}`,
        { oldPassword: currentPassword, newPassword },
        {
          headers: {
            Authorization: `Bearer ${token}`,
          },
        }
      );

      const successMsg = response.data || "Password updated successfully.";
      
      // If this is a mandatory change and we have an interceptor, use it
      if (isMandatory && interceptSuccessMessage) {
        setSuccessMessage(interceptSuccessMessage(successMsg));
      } else {
        setSuccessMessage(successMsg);
      }
      
      setErrorMessage("");
      setCurrentPassword("");
      setNewPassword("");
      setConfirmPassword("");
    } catch (err) {
      console.error("Error changing password:", err);
      setErrorMessage(err.response?.data || "Failed to update password.");
      setSuccessMessage("");
    } finally {
      setIsLoading(false);
    }
  };

  const renderPasswordField = (label, value, setValue, showPassword, setShowPassword, placeholder) => (
    <div className="mb-4 relative">
      <label className="block text-sm font-medium text-gray-700 mb-1">
        {isMandatory && label === "Current Password" ? "Default Password (123456)" : label}
      </label>
      <div className="relative">
        <input
          type={showPassword ? "text" : "password"}
          value={value}
          onChange={(e) => setValue(e.target.value)}
          className="w-full border border-gray-300 rounded-lg px-4 py-2 pr-10 focus:outline-none focus:ring-2 focus:ring-[#F4CE14]"
          placeholder={isMandatory && label === "Current Password" 
            ? "Enter default password (123456)" 
            : placeholder}
        />
        <button
          type="button"
          onClick={() => setShowPassword(!showPassword)}
          className="absolute inset-y-0 right-3 flex items-center text-gray-500 focus:outline-none"
        >
          {showPassword ? (
            <svg className="h-5 w-5" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth="2">
              <path d="M2 12s3-7 10-7 10 7 10 7-3 7-10 7-10-7-10-7Z" />
              <circle cx="12" cy="12" r="3" />
            </svg>
          ) : (
            <svg className="h-5 w-5" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth="2">
              <path d="M9.88 9.88a3 3 0 1 0 4.24 4.24" />
              <path d="M10.73 5.08A10.43 10.43 0 0 1 12 5c7 0 10 7 10 7a13.16 13.16 0 0 1-1.67 2.68" />
              <path d="M6.61 6.61A13.526 13.526 0 0 0 2 12s3 7 10 7a9.74 9.74 0 0 0 5.39-1.61" />
              <line x1="2" x2="22" y1="2" y2="22" />
            </svg>
          )}
        </button>
      </div>
    </div>
  );

  return (
    <div className="bg-white rounded-lg shadow-md p-6 w-full">
      {/* Header */}
      <div className="bg-[#495E57] text-white p-6 rounded-t-lg">
        <h2 className="text-2xl font-bold">Change Password</h2>
        <p className="text-gray-200 mt-1">
          {isMandatory 
            ? "You need to change your default password before continuing"
            : "Update your account password securely"}
        </p>
      </div>

      {/* Alerts */}
      {successMessage && !isMandatory && (
        <div className="bg-green-50 border-l-4 border-green-500 p-4 mt-4 text-green-700">
          {successMessage}
        </div>
      )}
      {errorMessage && (
        <div className="bg-red-50 border-l-4 border-red-500 p-4 mt-4 text-red-700">
          {errorMessage}
        </div>
      )}

      {/* Form */}
      <div className="mt-6 max-w-2xl mx-auto">
        {renderPasswordField(
          "Current Password",
          currentPassword,
          setCurrentPassword,
          showCurrentPassword,
          setShowCurrentPassword,
          "Enter current password"
        )}
        {renderPasswordField(
          "New Password",
          newPassword,
          setNewPassword,
          showNewPassword,
          setShowNewPassword,
          "Enter new password"
        )}
        {renderPasswordField(
          "Confirm New Password",
          confirmPassword,
          setConfirmPassword,
          showConfirmPassword, 
          setShowConfirmPassword,
          "Confirm new password"
        )}

        <div className="flex justify-center">
          <button
            onClick={handleChangePassword}
            disabled={isLoading}
            className={`bg-[#F4CE14] text-[#495E57] font-bold py-3 px-6 rounded-lg hover:bg-yellow-400 transition shadow-md ${
              isLoading ? "opacity-50 cursor-not-allowed" : ""
            }`}
          >
            {isLoading ? (
              <div className="flex justify-center items-center">
                <div className="w-5 h-5 border-2 border-t-2 border-[#495E57] rounded-full animate-spin mr-2"></div>
                Updating...
              </div>
            ) : (
              "Update Password"
            )}
          </button>
        </div>
      </div>
    </div>
  );
};

export default ChangePasswordContent;