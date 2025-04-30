import apiClient, { getApiUrl } from './apiConfig';

/**
 * Fetch user addresses based on user type
 */
export const fetchUserAddresses = async (userId, token, userType) => {
  if (!userId || !token) {
    throw new Error("Authentication information missing. Please login again.");
  }
  
  // Verify user role if needed
  const userRole = localStorage.getItem('userRole') || sessionStorage.getItem('userRole');
  if (userRole !== userType) {
    throw new Error(`Only ${userType}s can access this feature`);
  }
  
  let entityId, mainAddressId;
  
  if (userType === 'Service Provider') {
    // Logic for service providers
    const providersResponse = await apiClient.get(getApiUrl("/service-providers/getAll"));
    
    const provider = providersResponse.data.find(
      p => p.userAuth && p.userAuth.userId == userId
    );
    
    if (!provider) {
      throw new Error("No service provider profile found for this account.");
    }
    
    entityId = provider.providerId;
    
    // If provider has a main address, store its ID
    if (provider.address) {
      mainAddressId = provider.address.addressId;
    }
    
    // Get all addresses
    const addressesResponse = await apiClient.get(getApiUrl('/addresses/getAll'));
    
    // Filter addresses by providerId
    const userAddresses = addressesResponse.data.filter(
      address => address.serviceProvider?.providerId === provider.providerId || address.providerId === provider.providerId
    );
    
    return { 
      addresses: userAddresses, 
      userInfo: provider, 
      mainAddressId 
    };
    
  } else {
    // Logic for customers
    const customersResponse = await apiClient.get(getApiUrl("/customers/getAll"));
    
    const customer = customersResponse.data.find(
      c => c.userAuth && c.userAuth.userId == userId
    );
    
    if (!customer) {
      throw new Error("No customer profile found for this account.");
    }
    
    entityId = customer.customerId;
    
    // If customer has a main address, store its ID
    if (customer.address) {
      mainAddressId = customer.address.addressId;
    }
    
    // Get all addresses
    const addressesResponse = await apiClient.get(getApiUrl('/addresses/getAll'));
    
    // Filter addresses by customerId
    const userAddresses = addressesResponse.data.filter(
      address => address.customer?.customerId === customer.customerId || address.customerId === customer.customerId
    );
    
    return { 
      addresses: userAddresses, 
      userInfo: customer, 
      mainAddressId 
    };
  }
};

/**
 * Create a new address
 */
export const createAddress = async (token, addressData, entityId, isFirstAddress, userType) => {
  let payload;
  
  // Add validation to ensure entityId exists
  if (!entityId) {
    console.error(`Error: entityId is ${entityId} for ${userType}`);
    throw new Error(`Unable to create address: missing ${userType === 'Service Provider' ? 'provider' : 'customer'} ID. User data may not be loaded correctly.`);
  }
  
  if (userType === 'Service Provider') {
    console.log(`Creating address for provider with ID: ${entityId}`);
    payload = {
      ...addressData,
      main: isFirstAddress,
      serviceProvider: {
        providerId: entityId
      },
      customer: null
    };
  } else { // Customer
    console.log(`Creating address for customer with ID: ${entityId}`);
    payload = {
      ...addressData,
      main: isFirstAddress,
      customer: {
        customerId: entityId
      },
      serviceProvider: null
    };
  }
  
  console.log("Creating address with payload:", payload);
  
  const response = await apiClient.post(
    getApiUrl('/addresses/postAddress'),
    payload
  );
  
  console.log("Address created successfully:", response.data);
  
  const newAddressId = response.data.addressId;
  
  // If this is the first address, set it as the user's main address
  if (isFirstAddress) {
    if (userType === 'Service Provider') {
      // Get full provider data
      const providersResponse = await apiClient.get(getApiUrl("/service-providers/getAll"));
      
      const userId = localStorage.getItem('userId') || sessionStorage.getItem('userId');
      const provider = providersResponse.data.find(
        p => p.userAuth && p.userAuth.userId == userId
      );
      
      // Update provider with new address
      await apiClient.put(
        getApiUrl(`/service-providers/update/${entityId}`),
        {
          addressId: newAddressId,
          firstName: provider.firstName,
          lastName: provider.lastName,
          phoneNumber: provider.phoneNumber,
          businessName: provider.businessName,
          yearsOfExperience: provider.yearsOfExperience,
          availabilitySchedule: provider.availabilitySchedule,
          paymentMethod: provider.paymentMethod,
          status: provider.status
        }
      );
    } else { // Customer
      // Get full customer data
      const customersResponse = await apiClient.get(getApiUrl("/customers/getAll"));
      
      const userId = localStorage.getItem('userId') || sessionStorage.getItem('userId');
      const customer = customersResponse.data.find(
        c => c.userAuth && c.userAuth.userId == userId
      );
      
      // Update customer with new address if needed
    }
    
    // Set main flag in the response for UI consistency
    response.data.main = true;
  }
  
  return response.data;
};

/**
 * Update an existing address
 */
export const updateAddress = async (token, addressId, addressData, entityId, isMain, userType) => {
  // Add validation to ensure entityId exists
  if (!entityId) {
    console.error(`Error: entityId is ${entityId} for ${userType}`);
    throw new Error(`Unable to update address: missing ${userType === 'Service Provider' ? 'provider' : 'customer'} ID`);
  }
  
  let payload;
  
  if (userType === 'Service Provider') {
    console.log(`Updating address for provider with ID: ${entityId}`);
    payload = {
      ...addressData,
      serviceProvider: {
        providerId: entityId
      },
      customer: null,
      main: isMain
    };
  } else { // Customer
    console.log(`Updating address for customer with ID: ${entityId}`);
    payload = {
      ...addressData,
      customer: {
        customerId: entityId
      },
      serviceProvider: null,
      main: isMain
    };
  }
  
  console.log("Updating address with payload:", payload);
  
  const response = await apiClient.put(
    getApiUrl(`/addresses/updateAddress/${addressId}`),
    payload,
    {
      headers: { 
        'Content-Type': 'application/json'
      }
    }
  );
  
  return response.data;
};

/**
 * Delete an address
 */
export const deleteAddress = async (token, addressId) => {
  return await apiClient.delete(getApiUrl(`/addresses/delete/${addressId}`));
};

/**
 * Update main address
 */
export const updateAddressMain = async (token, addressId, addresses, userInfo, userType) => {
  const userId = localStorage.getItem('userId') || sessionStorage.getItem('userId');
  
  if (userType === 'Service Provider') {
    // Get all service providers
    const providersResponse = await apiClient.get(getApiUrl("/service-providers/getAll"));
    
    // Find the one that matches our userId
    const completeProvider = providersResponse.data.find(
      p => p.userAuth && p.userAuth.userId == userId
    );
    
    // Validate the provider data
    if (!completeProvider || !completeProvider.providerId) {
      console.error("Error: Provider data incomplete", completeProvider);
      throw new Error("Provider data is incomplete or missing ID");
    }
    
    console.log(`Setting main address for provider with ID: ${completeProvider.providerId}`);
    
    if (!completeProvider) {
      throw new Error("Provider not found");
    }
    
    // Find the address to update
    const addressToUpdate = addresses.find(addr => addr.addressId === addressId);
    
    if (!addressToUpdate) {
      throw new Error("Address not found");
    }
    
    // Update the address to set main to true
    const updatedMainAddress = {
      ...addressToUpdate,
      main: true,
      serviceProvider: {
        providerId: completeProvider.providerId
      },
      customer: null
    };
    
    console.log("Updating address as main with payload:", updatedMainAddress);
    
    await apiClient.put(
      getApiUrl(`/addresses/updateAddress/${addressId}`), 
      updatedMainAddress
    );
    
    // Set all other addresses to main: false
    for (const addr of addresses) {
      if (addr.addressId !== addressId && addr.main) {
        const otherAddress = {
          ...addr,
          main: false,
          serviceProvider: {
            providerId: completeProvider.providerId
          },
          customer: null
        };
        
        await apiClient.put(
          getApiUrl(`/addresses/updateAddress/${addr.addressId}`), 
          otherAddress
        );
      }
    }
    
  } else {
    // Logic for customers
    try {
      const customersResponse = await apiClient.get(getApiUrl("/customers/getAll"));
      
      const completeCustomer = customersResponse.data.find(
        c => c.userAuth && c.userAuth.userId == userId
      );
      
      // Validate the customer data
      if (!completeCustomer || !completeCustomer.customerId) {
        console.error("Error: Customer data incomplete", completeCustomer);
        throw new Error("Customer data is incomplete or missing ID");
      }
      
      console.log(`Setting main address for customer with ID: ${completeCustomer.customerId}`);
      
      if (!completeCustomer) {
        throw new Error("Customer not found");
      }
      
      // Find the address to update
      const addressToUpdate = addresses.find(addr => addr.addressId === addressId);
      
      if (!addressToUpdate) {
        throw new Error("Address not found");
      }
      
      // Update the address to set main to true
      const updatedMainAddress = {
        ...addressToUpdate,
        main: true,
        customer: {
          customerId: completeCustomer.customerId
        },
        serviceProvider: null
      };
      
      console.log("Updating address as main with payload:", updatedMainAddress);
      
      await apiClient.put(
        getApiUrl(`/addresses/updateAddress/${addressId}`), 
        updatedMainAddress
      );
      
      // Set all other addresses to main: false
      for (const addr of addresses) {
        if (addr.addressId !== addressId && addr.main) {
          const otherAddress = {
            ...addr,
            main: false,
            customer: {
              customerId: completeCustomer.customerId
            },
            serviceProvider: null
          };
          
          await apiClient.put(
            getApiUrl(`/addresses/updateAddress/${addr.addressId}`), 
            otherAddress
          );
        }
      }
    } catch (err) {
      console.error("Error in customer address update flow:", err);
      throw err;
    }
  }
};

/**
 * Fetch PSGC provinces
 */
export const fetchProvinces = async () => {
  const response = await fetch('https://psgc.gitlab.io/api/provinces');
  return await response.json();
};

/**
 * Fetch PSGC cities for a province
 */
export const fetchCities = async (provinceCode) => {
  const response = await fetch(`https://psgc.gitlab.io/api/provinces/${provinceCode}/cities-municipalities`);
  return await response.json();
};

/**
 * Fetch PSGC barangays for a city
 */
export const fetchBarangays = async (cityCode) => {
  const response = await fetch(`https://psgc.gitlab.io/api/cities-municipalities/${cityCode}/barangays`);
  return await response.json();
};
