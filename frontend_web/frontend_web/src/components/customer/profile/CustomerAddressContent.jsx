import React, { useState, useEffect } from "react";
import axios from "axios";

const CustomerAddressContent = () => {
  const [addresses, setAddresses] = useState([]);
  const [newAddress, setNewAddress] = useState({
    streetName: "",
    barangay: "",
    city: "",
    province: "",
    zipCode: "",
  });
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState(null);
  const [successMessage, setSuccessMessage] = useState(null);

  useEffect(() => {
    const fetchAddresses = async () => {
      try {
        const token = localStorage.getItem("authToken") || sessionStorage.getItem("authToken");
        if (!token) {
          setError("Authentication token not found.");
          setIsLoading(false);
          return;
        }

        const response = await axios.get("/api/addresses/getAll", {
          headers: {
            Authorization: `Bearer ${token}`,
          },
        });

        setAddresses(response.data);
        setIsLoading(false);
      } catch (err) {
        console.error("Error fetching addresses:", err);
        setError("Failed to load addresses.");
        setIsLoading(false);
      }
    };

    fetchAddresses();
  }, []);

  const handleInputChange = (e) => {
    const { name, value } = e.target;
    setNewAddress((prev) => ({
      ...prev,
      [name]: value,
    }));
  };

  const handleAddAddress = async () => {
    if (!newAddress.streetName.trim() || !newAddress.barangay.trim() || !newAddress.city.trim() || !newAddress.province.trim() || !newAddress.zipCode.trim()) {
      alert("All fields are required.");
      return;
    }

    try {
      const token = localStorage.getItem("authToken") || sessionStorage.getItem("authToken");
      if (!token) {
        setError("Authentication token not found.");
        return;
      }

      const response = await axios.post(
        "/api/addresses/add",
        newAddress,
        {
          headers: {
            Authorization: `Bearer ${token}`,
          },
        }
      );

      setAddresses([...addresses, response.data]);
      setNewAddress({
        streetName: "",
        barangay: "",
        city: "",
        province: "",
        zipCode: "",
      });
      setSuccessMessage("Address added successfully!");
      setTimeout(() => setSuccessMessage(null), 3000);
    } catch (err) {
      console.error("Error adding address:", err);
      setError("Failed to add address.");
    }
  };

  return (
    <div className="bg-white rounded-lg shadow-md">
      {/* Header */}
      <div className="bg-[#495E57] text-white p-6 rounded-t-lg">
        <h1 className="text-3xl font-bold">My Addresses</h1>
        <p className="text-gray-200 mt-2">Manage your saved addresses</p>
      </div>

      {/* Alerts */}
      {error && (
        <div className="bg-red-50 border-l-4 border-red-500 p-4 m-4 text-red-700">
          {error}
        </div>
      )}

      {successMessage && (
        <div className="bg-green-50 border-l-4 border-green-500 p-4 m-4 text-green-700">
          {successMessage}
        </div>
      )}

      {/* Address Form */}
      <div className="p-6">
        <form className="max-w-4xl mx-auto">
          <div className="bg-gray-50 p-6 rounded-lg border border-gray-200 mb-6">
            <h2 className="text-xl font-semibold text-[#495E57] mb-4 pb-2 border-b border-gray-200">
              Add New Address
            </h2>

            <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
              <div>
                <label className="block text-gray-700 text-sm font-medium mb-2">Street Name</label>
                <input
                  type="text"
                  name="streetName"
                  value={newAddress.streetName}
                  onChange={handleInputChange}
                  className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-[#F4CE14]"
                  placeholder="123 Main Street"
                />
              </div>

              <div>
                <label className="block text-gray-700 text-sm font-medium mb-2">Barangay</label>
                <input
                  type="text"
                  name="barangay"
                  value={newAddress.barangay}
                  onChange={handleInputChange}
                  className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-[#F4CE14]"
                  placeholder="Barangay name"
                />
              </div>

              <div>
                <label className="block text-gray-700 text-sm font-medium mb-2">City</label>
                <input
                  type="text"
                  name="city"
                  value={newAddress.city}
                  onChange={handleInputChange}
                  className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-[#F4CE14]"
                  placeholder="City"
                />
              </div>

              <div>
                <label className="block text-gray-700 text-sm font-medium mb-2">Province</label>
                <input
                  type="text"
                  name="province"
                  value={newAddress.province}
                  onChange={handleInputChange}
                  className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-[#F4CE14]"
                  placeholder="Province"
                />
              </div>

              <div>
                <label className="block text-gray-700 text-sm font-medium mb-2">Zip Code</label>
                <input
                  type="text"
                  name="zipCode"
                  value={newAddress.zipCode}
                  onChange={handleInputChange}
                  className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-[#F4CE14]"
                  placeholder="Zip Code"
                />
              </div>
            </div>

            <div className="flex justify-center mt-8">
              <button
                type="button"
                onClick={handleAddAddress}
                className="bg-[#F4CE14] text-[#495E57] font-bold py-3 px-8 rounded-lg hover:bg-yellow-400 transition shadow-md"
              >
                Save Address
              </button>
            </div>
          </div>
        </form>
      </div>

      {/* Address List */}
      <div className="px-6 pb-6">
        <h2 className="text-2xl font-semibold text-[#495E57] mb-4">Saved Addresses</h2>

        {isLoading ? (
          <div className="flex justify-center p-8">
            <div className="w-12 h-12 border-4 border-t-4 border-t-[#F4CE14] rounded-full animate-spin border-gray-200"></div>
          </div>
        ) : !addresses.length ? (
          <div className="bg-gray-50 p-8 text-center rounded-lg border border-gray-200">
            <p className="text-gray-500">You haven't added any addresses yet.</p>
          </div>
        ) : (
          <div className="space-y-4">
            {addresses.map((address, index) => (
              <div key={index} className="bg-white rounded-lg border border-gray-200 p-4">
                <h3 className="text-lg font-medium">
                  {address.streetName}, {address.barangay}
                </h3>
                <p className="text-gray-600">
                  {address.city}, {address.province} {address.zipCode}
                </p>
              </div>
            ))}
          </div>
        )}
      </div>
    </div>
  );
};

export default CustomerAddressContent;