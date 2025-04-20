import React from 'react';

function AddressList({
  addresses,
  loading,
  handleEdit,
  setAsMainAddress,
  openDeleteDialog
}) {
  if (loading && !addresses.length) {
    return (
      <div className="flex justify-center p-8">
        <div className="w-12 h-12 border-4 border-t-4 border-t-[#F4CE14] rounded-full animate-spin border-gray-200"></div>
      </div>
    );
  }

  if (!addresses.length) {
    return (
      <div className="bg-gray-50 p-8 text-center rounded-lg border border-gray-200">
        <p className="text-gray-500">You haven't added any addresses yet.</p>
      </div>
    );
  }

  return (
    <div className="space-y-4">
      {addresses.map((address) => (
        <div 
          key={address.addressId} 
          className={`bg-white rounded-lg border ${address.main ? 'border-[#F4CE14] shadow-md' : 'border-gray-200'}`}
        >
          <div className="p-4">
            <div className="flex flex-col md:flex-row justify-between">
              <div className="mb-4 md:mb-0">
                <div className="flex items-center mb-1">
                  <h3 className="text-lg font-medium">
                    {address.streetName}, {address.barangay}
                  </h3>
                  {address.main && (
                    <span className="ml-2 bg-[#F4CE14] text-[#495E57] text-xs font-bold px-2 py-1 rounded">
                      Main Address
                    </span>
                  )}
                </div>
                <p className="text-gray-600">
                  {address.city}, {address.province} {address.zipCode}
                </p>
              </div>
              
              <div className="flex items-center space-x-2">
                {!address.main && (
                  <button 
                    onClick={() => setAsMainAddress(address.addressId)}
                    className="text-[#495E57] border border-[#495E57] text-sm font-medium px-3 py-1 rounded hover:bg-[#495E57] hover:text-white transition"
                  >
                    Set as Main
                  </button>
                )}
                
                <button 
                  onClick={() => handleEdit(address)}
                  className="bg-gray-100 text-gray-700 p-2 rounded hover:bg-gray-200 transition"
                >
                  <svg xmlns="http://www.w3.org/2000/svg" className="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M11 5H6a2 2 0 00-2 2v11a2 2 0 002 2h11a2 2 0 002-2v-5m-1.414-9.414a2 2 0 112.828 2.828L11.828 15H9v-2.828l8.586-8.586z" />
                  </svg>
                </button>
                
                <button 
                  onClick={() => openDeleteDialog(address.addressId)}
                  className={`p-2 rounded ${address.main 
                    ? 'bg-gray-100 text-gray-400 cursor-not-allowed' 
                    : 'bg-gray-100 text-red-500 hover:bg-gray-200 transition'}`}
                  disabled={address.main}
                >
                  <svg xmlns="http://www.w3.org/2000/svg" className="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16" />
                  </svg>
                </button>
              </div>
            </div>
          </div>
        </div>
      ))}
    </div>
  );
}

export default AddressList;
