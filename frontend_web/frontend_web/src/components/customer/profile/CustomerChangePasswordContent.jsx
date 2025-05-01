import React, { useState } from "react";
import apiClient, { getApiUrl } from "../../../utils/apiConfig";

const CustomerChangePasswordContent = ({ interceptSuccessMessage, isMandatory }) => {
  const [currentPassword, setCurrentPassword] = useState("");
  const [newPassword, setNewPassword] = useState("");
  const [confirmPassword, setConfirmPassword] = useState("");
  const [successMessage, setSuccessMessage] = useState("");
  const [errorMessage, setErrorMessage] = useState("");
  const [isLoading, setIsLoading] = useState(false);

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
      const authId = localStorage.getItem("userId") || sessionStorage.getItem("userId");
  
      if (!authId) {
        setErrorMessage("Authentication token or user ID not found.");
        setIsLoading(false);
        return;
      }
  
      // Call the backend API to change the password
      const response = await apiClient.put(
        getApiUrl(`/user-auth/change-password/${authId}`),
        { oldPassword: currentPassword, newPassword }
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
        <div className="mb-4">
          <label className="block text-sm font-medium text-gray-700 mb-1">
            {isMandatory ? "Default Password (123456)" : "Current Password"}
          </label>
          <input
            type="password"
            value={currentPassword}
            onChange={(e) => setCurrentPassword(e.target.value)}
            className="w-full border border-gray-300 rounded-lg px-4 py-2 focus:outline-none focus:ring-2 focus:ring-[#F4CE14]"
            placeholder={isMandatory ? "Enter default password (123456)" : "Enter current password"}
          />
        </div>
        <div className="mb-4">
          <label className="block text-sm font-medium text-gray-700 mb-1">New Password</label>
          <input
            type="password"
            value={newPassword}
            onChange={(e) => setNewPassword(e.target.value)}
            className="w-full border border-gray-300 rounded-lg px-4 py-2 focus:outline-none focus:ring-2 focus:ring-[#F4CE14]"
            placeholder="Enter new password"
          />
        </div>
        <div className="mb-4">
          <label className="block text-sm font-medium text-gray-700 mb-1">Confirm New Password</label>
          <input
            type="password"
            value={confirmPassword}
            onChange={(e) => setConfirmPassword(e.target.value)}
            className="w-full border border-gray-300 rounded-lg px-4 py-2 focus:outline-none focus:ring-2 focus:ring-[#F4CE14]"
            placeholder="Confirm new password"
          />
        </div>
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

export default CustomerChangePasswordContent;