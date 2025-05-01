import React, { useState, useEffect } from "react";
import apiClient, { getApiUrl, API_BASE_URL } from "../../../utils/apiConfig";

const CustomerProfileContent = () => {
  const [profile, setProfile] = useState({
    firstName: "",
    lastName: "",
    email: "",
    phoneNumber: "",
    userName: "",
    role: "",
  });
  const [customerId, setCustomerId] = useState(null);
  const [selectedImage, setSelectedImage] = useState(null);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState(null);
  const [successMessage, setSuccessMessage] = useState("");
  const [showPopup, setShowPopup] = useState(false);
  const [uploadMessage, setUploadMessage] = useState("");
  const [showUploadPopup, setShowUploadPopup] = useState(false);

  // Fetch customer profile data
  useEffect(() => {
    const fetchProfile = async () => {
      try {
        const userId = localStorage.getItem("userId") || sessionStorage.getItem("userId");
  
        if (!userId) {
          setError("User ID not found.");
          setIsLoading(false);
          return;
        }
  
        // Step 1: Get all customers
        const allCustomersResponse = await apiClient.get(getApiUrl("/customers/getAll"));

        console.log("All customers:", allCustomersResponse.data);
        
        // Step 2: Find the customer that matches this user's ID
        const matchingCustomer = allCustomersResponse.data.find(
          customer => customer.userAuth && customer.userAuth.userId == userId
        );
        
        if (!matchingCustomer) {
          console.error("No matching customer found for userId:", userId);
          setError("Customer profile not found. Please contact support.");
          setIsLoading(false);
          return;
        }
        
        console.log("Found matching customer:", matchingCustomer);
        
        // Step 3: Get user auth details
        const userAuthResponse = await apiClient.get(getApiUrl(`/user-auth/getById/${userId}`));
  
        console.log("User auth response:", userAuthResponse);
        const userAuth = userAuthResponse.data;
        
        if (!userAuth) {
          setError("User authentication details not found.");
          setIsLoading(false);
          return;
        }
  
        // Step 4: Set the profile data and customerId
        setCustomerId(matchingCustomer.customerId);
        
        setProfile({
          firstName: matchingCustomer.firstName || "",
          lastName: matchingCustomer.lastName || "",
          phoneNumber: matchingCustomer.phoneNumber || "",
          email: userAuth.email || "",
          userName: userAuth.userName || "",
          role: userAuth.role || "",
        });
        
        setIsLoading(false);
      } catch (err) {
        console.error("Error fetching profile:", err);
        setError("Failed to load profile.");
        setIsLoading(false);
      }
    };
  
    fetchProfile();
  }, []);

  // Fetch profile image after customerId is available
  useEffect(() => {
    const fetchProfileImage = async () => {
      if (!customerId) return;
      
      try {
        console.log("Fetching profile image for customerId:", customerId);
        
        const profileImageResponse = await apiClient.get(getApiUrl(`/customers/getProfileImage/${customerId}`));

        console.log("Fetched Profile Image URL:", profileImageResponse.data);
  
        if (profileImageResponse.data) {
          // Prepend the base URL to the image path
          const fullImageURL = `${API_BASE_URL}${profileImageResponse.data}`;
          setSelectedImage(fullImageURL);
        }
      } catch (err) {
        console.error("Error fetching profile image:", err);
      }
    };
    
    fetchProfileImage();
  }, [customerId]);

  const handleInputChange = (e) => {
    const { name, value } = e.target;
    setProfile((prevProfile) => ({
      ...prevProfile,
      [name]: value,
    }));
  };

  const handleImageUpload = async (e) => {
    const file = e.target.files[0];
    if (!file || !customerId) return;

    const reader = new FileReader();
    reader.onload = () => {
      setSelectedImage(reader.result);

      // Upload the image to the server
      const uploadImage = async () => {
        try {
          const formData = new FormData();
          formData.append("image", file);

          console.log("Uploading image for customerId:", customerId);

          const response = await apiClient.post(
            getApiUrl(`/customers/upload-image/${customerId}`), 
            formData,
            {
              headers: {
                "Content-Type": "multipart/form-data",
              },
            }
          );

          setUploadMessage(response.data || "Profile picture uploaded successfully.");
          setShowUploadPopup(true);
          setTimeout(() => setShowUploadPopup(false), 3000);
        } catch (err) {
          console.error("Error uploading image:", err);
          setUploadMessage("Failed to upload profile picture. Please try again.");
          setShowUploadPopup(true);
          setTimeout(() => setShowUploadPopup(false), 3000);
        }
      };

      uploadImage();
    };
    reader.readAsDataURL(file);
  };

  const handleUpdateProfile = async () => {
    try {
      if (!customerId) {
        setError("Customer ID not found. Cannot update profile.");
        setSuccessMessage("");
        setShowPopup(true);
        setTimeout(() => setShowPopup(false), 2000);
        return;
      }

      console.log("Updating profile for customerId:", customerId);
      console.log("Profile data to update:", profile);

      const response = await apiClient.put(
        getApiUrl(`/customers/updateCustomer/${customerId}`),
        {
          firstName: profile.firstName,
          lastName: profile.lastName,
          phoneNumber: profile.phoneNumber
        }
      );

      console.log("Profile update response:", response.data);

      setSuccessMessage("Profile updated successfully.");
      setError("");
      setShowPopup(true);
      setTimeout(() => setShowPopup(false), 2000);
    } catch (err) {
      console.error("Error updating profile:", err);
      setError(err.response?.data?.message || "Failed to update profile.");
      setSuccessMessage("");
      setShowPopup(true);
      setTimeout(() => setShowPopup(false), 2000);
    }
  };

  if (isLoading) {
    return (
      <div className="bg-white rounded-lg shadow-md p-8 flex justify-center items-center min-h-[200px]">
        <div className="w-12 h-12 border-4 border-t-4 border-[#F4CE14] rounded-full animate-spin"></div>
        <span className="ml-3 text-gray-700">Loading profile...</span>
      </div>
    );
  }

  return (
    <div className="bg-white rounded-lg shadow-md">
      {/* Header */}
      <div className="bg-[#495E57] text-white p-6 rounded-t-lg">
        <h1 className="text-3xl font-bold">My Profile</h1>
        <p className="text-gray-200 mt-2">Manage and protect your account</p>
      </div>

      {/* Error message outside of popup */}
      {error && !showPopup && (
        <div className="mx-6 mt-6 p-4 bg-red-50 border-l-4 border-red-500 text-red-700">
          {error}
        </div>
      )}

      {/* Popup for Profile Update */}
      {showPopup && (
        <div
          className={`fixed inset-0 flex items-center justify-center z-50 transition-opacity duration-500 ${
            showPopup ? "opacity-100" : "opacity-0"
          }`}
        >
          <div className="bg-white rounded-lg shadow-lg p-6 border border-gray-300">
            <h2
              className={`text-lg font-bold mb-4 ${
                error ? "text-red-600" : "text-green-600"
              }`}
            >
              {error ? "Update Failed" : "Update Successful"}
            </h2>
            <p className="text-gray-700">{error || successMessage}</p>
          </div>
        </div>
      )}

      {/* Popup for Image Upload */}
      {showUploadPopup && (
        <div
          className={`fixed inset-0 flex items-center justify-center z-50 transition-opacity duration-500 ${
            showUploadPopup ? "opacity-100" : "opacity-0"
          }`}
        >
          <div className="bg-white rounded-lg shadow-lg p-6 border border-gray-300">
            <h2 className="text-lg font-bold mb-4 text-green-600">Image Upload</h2>
            <p className="text-gray-700">{uploadMessage}</p>
          </div>
        </div>
      )}

      <form onSubmit={(e) => e.preventDefault()} className="p-6">
        <div className="flex flex-col lg:flex-row gap-8">
          {/* Left Column - Profile Image */}
          <div className="lg:w-1/3 flex flex-col items-center gap-4">
            <div className="relative">
              <div className="w-48 h-48 rounded-full overflow-hidden bg-gray-100 border-4 border-[#F4CE14]">
                {selectedImage ? (
                  <>
                    <img
                      src={selectedImage}
                      alt="Profile"
                      className="w-full h-full object-cover"
                    />
                  </>
                ) : (
                  <div className="w-full h-full flex items-center justify-center bg-gray-200 text-gray-400">
                    No Image
                  </div>
                )}
              </div>
              <label
                htmlFor="profile-image-upload"
                className="absolute bottom-0 right-0 bg-[#F4CE14] rounded-full p-2 cursor-pointer shadow-md hover:bg-yellow-400 transition"
              >
                <svg
                  xmlns="http://www.w3.org/2000/svg"
                  className="h-6 w-6 text-[#495E57]"
                  fill="none"
                  viewBox="0 0 24 24"
                  stroke="currentColor"
                >
                  <path
                    strokeLinecap="round"
                    strokeLinejoin="round"
                    strokeWidth={2}
                    d="M12 6v6m0 0v6m0-6h6m-6 0H6"
                  />
                </svg>
                <input
                  id="profile-image-upload"
                  type="file"
                  accept="image/*"
                  className="hidden"
                  onChange={handleImageUpload}
                />
              </label>
            </div>
          </div>

          {/* Right Column - Form Fields */}
          <div className="lg:w-2/3">
            {/* Account Information */}
            <div className="mb-8">
              <h2 className="text-xl font-semibold text-[#495E57] mb-4 pb-2 border-b border-gray-200">
                Account Information
              </h2>
              <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                <div>
                  <label className="block text-gray-700 text-sm font-medium mb-2">Username</label>
                  <input
                    type="text"
                    name="userName"
                    value={profile.userName}
                    readOnly
                    disabled
                    className="w-full px-4 py-2 border border-gray-300 bg-gray-50 rounded-lg focus:outline-none"
                  />
                </div>
                <div>
                  <label className="block text-gray-700 text-sm font-medium mb-2">Email</label>
                  <input
                    type="email"
                    name="email"
                    value={profile.email}
                    readOnly
                    disabled
                    className="w-full px-4 py-2 border border-gray-300 bg-gray-50 rounded-lg focus:outline-none"
                  />
                </div>
              </div>
            </div>

            {/* Personal Information */}
            <div className="mb-8">
              <h2 className="text-xl font-semibold text-[#495E57] mb-4 pb-2 border-b border-gray-200">
                Personal Information
              </h2>
              <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                <div>
                  <label className="block text-gray-700 text-sm font-medium mb-2">First Name</label>
                  <input
                    type="text"
                    name="firstName"
                    value={profile.firstName}
                    onChange={handleInputChange}
                    className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-[#F4CE14]"
                  />
                </div>
                <div>
                  <label className="block text-gray-700 text-sm font-medium mb-2">Last Name</label>
                  <input
                    type="text"
                    name="lastName"
                    value={profile.lastName}
                    onChange={handleInputChange}
                    className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-[#F4CE14]"
                  />
                </div>
                <div>
                  <label className="block text-gray-700 text-sm font-medium mb-2">Phone Number</label>
                  <input
                    type="text"
                    name="phoneNumber"
                    value={profile.phoneNumber}
                    onChange={handleInputChange}
                    className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-[#F4CE14]"
                  />
                </div>
              </div>
            </div>

            <button
              onClick={handleUpdateProfile}
              className="mt-6 px-6 bg-[#F4CE14] text-[#495E57] font-bold py-2 rounded-lg hover:bg-[#d4b012] transition shadow-md"
            >
              Update Profile
            </button>
          </div>
        </div>
      </form>
    </div>
  );
};

export default CustomerProfileContent;