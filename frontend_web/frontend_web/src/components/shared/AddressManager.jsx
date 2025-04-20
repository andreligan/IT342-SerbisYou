import React from 'react';
import AddressForm from './AddressForm';
import AddressList from './AddressList';
import DeleteConfirmationDialog from './DeleteConfirmationDialog';
import useAddressManager from '../../hooks/useAddressManager';

function AddressManager({ userType = 'Service Provider', title = 'My Addresses' }) {
  const {
    addressForm,
    setAddressForm,
    provinces,
    setProvinces,
    cities,
    setCities,
    barangays,
    setBarangays,
    selectedProvinceCode,
    setSelectedProvinceCode,
    selectedCityCode,
    setSelectedCityCode,
    selectedBarangayCode,
    setSelectedBarangayCode,
    addresses,
    loading,
    error,
    success,
    editMode,
    setEditMode,
    currentAddressId,
    setCurrentAddressId,
    deleteDialogOpen,
    setDeleteDialogOpen,
    addressToDelete,
    setAddressToDelete,
    isLoading,
    setIsLoading,
    handleSubmit,
    handleDelete,
    setAsMainAddress,
    resetForm
  } = useAddressManager(userType);

  // Populate form data when editing
  const handleEdit = async (address) => {
    // Set form data
    setAddressForm({
      barangay: address.barangay || '',
      city: address.city || '',
      province: address.province || '',
      streetName: address.streetName || '',
      zipCode: address.zipCode || ''
    });
    
    // Try to find and select the province code
    const matchedProvince = provinces.find(p => p.name === address.province);
    if (matchedProvince) {
      setSelectedProvinceCode(matchedProvince.code);
      
      // Load cities for this province
      setIsLoading(prev => ({ ...prev, cities: true }));
      try {
        const citiesResponse = await fetch(`https://psgc.gitlab.io/api/provinces/${matchedProvince.code}/cities-municipalities`);
        const citiesData = await citiesResponse.json();
        setCities(citiesData);
        
        // Try to find and select the city code
        const matchedCity = citiesData.find(c => c.name === address.city);
        if (matchedCity) {
          setSelectedCityCode(matchedCity.code);
          
          // Load barangays for this city
          setIsLoading(prev => ({ ...prev, barangays: true }));
          const barangaysResponse = await fetch(`https://psgc.gitlab.io/api/cities-municipalities/${matchedCity.code}/barangays`);
          const barangaysData = await barangaysResponse.json();
          setBarangays(barangaysData);
          
          // Try to find and select the barangay code
          const matchedBarangay = barangaysData.find(b => b.name === address.barangay);
          if (matchedBarangay) {
            setSelectedBarangayCode(matchedBarangay.code);
          }
          setIsLoading(prev => ({ ...prev, barangays: false }));
        }
      } catch (error) {
        console.error("Error loading location data:", error);
      } finally {
        setIsLoading(prev => ({ ...prev, cities: false }));
      }
    }
    
    // Set edit mode and current address ID
    setEditMode(true);
    setCurrentAddressId(address.addressId);
    
    // Scroll to form
    window.scrollTo({ top: 0, behavior: 'smooth' });
  };

  // Open delete confirmation dialog
  const openDeleteDialog = (addressId) => {
    setAddressToDelete(addressId);
    setDeleteDialogOpen(true);
  };

  // Handle delete confirmation
  const confirmDelete = () => {
    if (addressToDelete) {
      handleDelete(addressToDelete);
    }
  };

  return (
    <div className="bg-white rounded-lg shadow-md">
      {/* Header */}
      <div className="bg-[#495E57] text-white p-6 rounded-t-lg">
        <h1 className="text-3xl font-bold">{title}</h1>
        <p className="text-gray-200 mt-2">
          {editMode ? 'Edit your address details' : 'Add a new address to your profile'}
        </p>
      </div>

      {/* Alerts */}
      {error && (
        <div className="bg-red-50 border-l-4 border-red-500 p-4 m-4 text-red-700">
          {error}
        </div>
      )}

      {success && (
        <div className="bg-green-50 border-l-4 border-green-500 p-4 m-4 text-green-700">
          {success}
        </div>
      )}

      {/* Address Form */}
      <div className="p-6">
        <AddressForm
          addressForm={addressForm}
          setAddressForm={setAddressForm}
          provinces={provinces}
          setProvinces={setProvinces}
          cities={cities}
          setCities={setCities}
          barangays={barangays}
          setBarangays={setBarangays}
          selectedProvinceCode={selectedProvinceCode}
          setSelectedProvinceCode={setSelectedProvinceCode}
          selectedCityCode={selectedCityCode}
          setSelectedCityCode={setSelectedCityCode}
          selectedBarangayCode={selectedBarangayCode}
          setSelectedBarangayCode={setSelectedBarangayCode}
          isLoading={isLoading}
          setIsLoading={setIsLoading}
          loading={loading}
          editMode={editMode}
          handleSubmit={handleSubmit}
          handleCancel={resetForm}
        />
      </div>

      {/* Address List */}
      <div className="px-6 pb-6">
        <h2 className="text-2xl font-semibold text-[#495E57] mb-4">Saved Addresses</h2>
        <AddressList
          addresses={addresses}
          loading={loading}
          handleEdit={handleEdit}
          setAsMainAddress={setAsMainAddress}
          openDeleteDialog={openDeleteDialog}
        />
      </div>

      {/* Delete Confirmation Dialog */}
      <DeleteConfirmationDialog
        isOpen={deleteDialogOpen}
        onClose={() => setDeleteDialogOpen(false)}
        onDelete={confirmDelete}
      />
    </div>
  );
}

export default AddressManager;
