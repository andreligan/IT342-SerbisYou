import React, { useEffect } from 'react';
import axios from 'axios';

function AddressForm({
  addressForm, setAddressForm,
  provinces, setProvinces,
  cities, setCities,
  barangays, setBarangays,
  selectedProvinceCode, setSelectedProvinceCode,
  selectedCityCode, setSelectedCityCode,
  selectedBarangayCode, setSelectedBarangayCode,
  isLoading, setIsLoading,
  loading, editMode, handleSubmit, handleCancel
}) {
  // Fetch provinces on component mount
  useEffect(() => {
    const fetchProvinces = async () => {
      setIsLoading(prev => ({ ...prev, provinces: true }));
      try {
        const response = await axios.get('https://psgc.gitlab.io/api/provinces');
        setProvinces(response.data);
      } catch (error) {
        console.error("Failed to fetch provinces:", error);
      } finally {
        setIsLoading(prev => ({ ...prev, provinces: false }));
      }
    };
    
    if (provinces.length === 0) {
      fetchProvinces();
    }
  }, []);

  // Handle province change - fetch cities
  const handleProvinceChange = async (e) => {
    const provinceCode = e.target.value;
    const provinceName = e.target.options[e.target.selectedIndex].text;
    
    setSelectedProvinceCode(provinceCode);
    setAddressForm(prev => ({ 
      ...prev, 
      province: provinceName,
      city: '',
      barangay: '' 
    }));
    
    setCities([]);
    setBarangays([]);
    
    if (!provinceCode) return;
    
    setIsLoading(prev => ({ ...prev, cities: true }));
    try {
      const response = await axios.get(`https://psgc.gitlab.io/api/provinces/${provinceCode}/cities-municipalities`);
      setCities(response.data);
    } catch (error) {
      console.error("Failed to fetch cities:", error);
    } finally {
      setIsLoading(prev => ({ ...prev, cities: false }));
    }
  };
  
  // Handle city change - fetch barangays
  const handleCityChange = async (e) => {
    const cityCode = e.target.value;
    const cityName = e.target.options[e.target.selectedIndex].text;
    
    setSelectedCityCode(cityCode);
    setAddressForm(prev => ({ 
      ...prev, 
      city: cityName,
      barangay: '' 
    }));
    
    setBarangays([]);
    
    if (!cityCode) return;
    
    setIsLoading(prev => ({ ...prev, barangays: true }));
    try {
      const response = await axios.get(`https://psgc.gitlab.io/api/cities-municipalities/${cityCode}/barangays`);
      setBarangays(response.data);
    } catch (error) {
      console.error("Failed to fetch barangays:", error);
    } finally {
      setIsLoading(prev => ({ ...prev, barangays: false }));
    }
  };
  
  // Handle barangay selection
  const handleBarangayChange = (e) => {
    const barangayCode = e.target.value;
    setSelectedBarangayCode(barangayCode);
    const barangayName = e.target.options[e.target.selectedIndex].text;
    setAddressForm(prev => ({ ...prev, barangay: barangayName }));
  };
  
  // Handle other input changes
  const handleChange = (e) => {
    const { name, value } = e.target;
    setAddressForm(prev => ({
      ...prev,
      [name]: value
    }));
  };

  return (
    <form onSubmit={handleSubmit} className="max-w-4xl mx-auto">
      <div className="bg-gray-50 p-6 rounded-lg border border-gray-200 mb-6">
        <h2 className="text-xl font-semibold text-[#495E57] mb-4 pb-2 border-b border-gray-200">
          {editMode ? 'Edit Address' : 'Add New Address'}
        </h2>
        
        <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
          {/* Province Dropdown */}
          <div>
            <label className="block text-gray-700 text-sm font-medium mb-2">Province *</label>
            <select
              name="province"
              value={selectedProvinceCode}
              onChange={handleProvinceChange}
              required
              className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-[#F4CE14]"
            >
              <option value="">Select Province</option>
              {provinces.map(province => (
                <option key={province.code} value={province.code}>
                  {province.name}
                </option>
              ))}
            </select>
            {isLoading.provinces && <span className="text-sm text-gray-500">Loading provinces...</span>}
            {editMode && !selectedProvinceCode && addressForm.province && (
              <p className="text-sm text-amber-600 mt-1">
                Current value: {addressForm.province}. Select from dropdown to change.
              </p>
            )}
          </div>
          
          {/* City Dropdown */}
          <div>
            <label className="block text-gray-700 text-sm font-medium mb-2">City/Municipality *</label>
            <select
              name="city"
              value={selectedCityCode}
              onChange={handleCityChange}
              disabled={!selectedProvinceCode && !editMode}
              required
              className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-[#F4CE14]"
            >
              <option value="">Select City/Municipality</option>
              {cities.map(city => (
                <option key={city.code} value={city.code}>
                  {city.name}
                </option>
              ))}
            </select>
            {isLoading.cities && <span className="text-sm text-gray-500">Loading cities...</span>}
            {editMode && !selectedCityCode && addressForm.city && (
              <p className="text-sm text-amber-600 mt-1">
                Current value: {addressForm.city}. Select province first to update.
              </p>
            )}
          </div>
          
          {/* Barangay Dropdown */}
          <div>
            <label className="block text-gray-700 text-sm font-medium mb-2">Barangay *</label>
            <select
              name="barangay"
              value={selectedBarangayCode}
              onChange={handleBarangayChange}
              disabled={!selectedCityCode && !editMode}
              required
              className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-[#F4CE14]"
            >
              <option value="">Select Barangay</option>
              {barangays.map(barangay => (
                <option key={barangay.code} value={barangay.code}>
                  {barangay.name}
                </option>
              ))}
            </select>
            {isLoading.barangays && <span className="text-sm text-gray-500">Loading barangays...</span>}
            {editMode && !barangays.length && addressForm.barangay && (
              <p className="text-sm text-amber-600 mt-1">
                Current value: {addressForm.barangay}. Select city first to update.
              </p>
            )}
          </div>
          
          {/* Street Name Input */}
          <div>
            <label className="block text-gray-700 text-sm font-medium mb-2">Street Name</label>
            <input
              type="text"
              name="streetName"
              value={addressForm.streetName}
              onChange={handleChange}
              required
              className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-[#F4CE14]"
              placeholder="123 Main Street"
            />
          </div>
          
          {/* Zip Code Input */}
          <div>
            <label className="block text-gray-700 text-sm font-medium mb-2">Zip Code</label>
            <input
              type="text"
              name="zipCode"
              value={addressForm.zipCode}
              onChange={handleChange}
              required
              className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-[#F4CE14]"
              placeholder="Zip Code"
            />
          </div>
        </div>
        
        <div className="flex justify-center mt-8 space-x-4">
          <button 
            type="submit" 
            disabled={loading}
            className="bg-[#F4CE14] text-[#495E57] font-bold py-3 px-8 rounded-lg hover:bg-yellow-400 transition shadow-md flex items-center disabled:opacity-50 disabled:cursor-not-allowed"
          >
            {loading ? (
              <div className="w-5 h-5 border-2 border-t-2 border-[#495E57] rounded-full animate-spin mr-2"></div>
            ) : editMode ? (
              <svg xmlns="http://www.w3.org/2000/svg" className="h-5 w-5 mr-2" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M11 5H6a2 2 0 00-2 2v11a2 2 0 002 2h11a2 2 0 002-2v-5m-1.414-9.414a2 2 0 112.828 2.828L11.828 15H9v-2.828l8.586-8.586z" />
              </svg>
            ) : (
              <svg xmlns="http://www.w3.org/2000/svg" className="h-5 w-5 mr-2" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 6v6m0 0v6m0-6h6m-6 0H6" />
              </svg>
            )}
            {editMode ? 'Update Address' : 'Save Address'}
          </button>
          
          {editMode && (
            <button 
              type="button" 
              onClick={handleCancel}
              className="border border-gray-300 text-gray-700 font-medium py-3 px-8 rounded-lg hover:bg-gray-50 transition"
            >
              Cancel
            </button>
          )}
        </div>
      </div>
    </form>
  );
}

export default AddressForm;
