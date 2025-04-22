import axios from 'axios';

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
  
  let endpoint, userIdField, entityId, mainAddressId;
  
  if (userType === 'Service Provider') {
    // Logic for service providers
    const providersResponse = await axios.get("/api/service-providers/getAll", {
      headers: { 'Authorization': `Bearer ${token}` }
    });
    
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
    const addressesResponse = await axios.get('/api/addresses/getAll', {
      headers: { 'Authorization': `Bearer ${token}` }
    });
    
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
    const customersResponse = await axios.get("/api/customers/getAll", {
      headers: { 'Authorization': `Bearer ${token}` }
    });
    
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
    const addressesResponse = await axios.get('/api/addresses/getAll', {
      headers: { 'Authorization': `Bearer ${token}` }
    });
    
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
  
  const response = await axios.post(
    '/api/addresses/postAddress',
    payload,
    {
      headers: { 'Authorization': `Bearer ${token}` }
    }
  );
  
  console.log("Address created successfully:", response.data);
  
  const newAddressId = response.data.addressId;
  
  // If this is the first address, set it as the user's main address
  if (isFirstAddress) {
    if (userType === 'Service Provider') {
      // Get full provider data
      const providersResponse = await axios.get(
        "/api/service-providers/getAll", 
        { headers: { 'Authorization': `Bearer ${token}` }}
      );
      
      const userId = localStorage.getItem('userId') || sessionStorage.getItem('userId');
      const provider = providersResponse.data.find(
        p => p.userAuth && p.userAuth.userId == userId
      );
      
      // Update provider with new address
      await axios.put(
        `/api/service-providers/update/${entityId}`, 
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
        },
        { headers: { 'Authorization': `Bearer ${token}` }}
      );
    } else { // Customer
      // Get full customer data
      const customersResponse = await axios.get(
        "/api/customers/getAll", 
        { headers: { 'Authorization': `Bearer ${token}` }}
      );
      
      const userId = localStorage.getItem('userId') || sessionStorage.getItem('userId');
      const customer = customersResponse.data.find(
        c => c.userAuth && c.userAuth.userId == userId
      );
      
      // Update customer with new address
      // await axios.put(
      //   `/api/customers/update/${entityId}`, 
      //   {
      //     addressId: newAddressId,
      //     firstName: customer.firstName,
      //     lastName: customer.lastName,
      //     phoneNumber: customer.phoneNumber,
      //     // Add any other required customer fields
      //   },
      //   { headers: { 'Authorization': `Bearer ${token}` }}
      // );
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
  
  const response = await axios.put(
    `/api/addresses/updateAddress/${addressId}`,
    payload,
    {
      headers: { 
        'Authorization': `Bearer ${token}`,
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
  return await axios.delete(`/api/addresses/delete/${addressId}`, {
    headers: { 'Authorization': `Bearer ${token}` }
  });
};

/**
 * Update main address
 */
export const updateAddressMain = async (token, addressId, addresses, userInfo, userType) => {
  const userId = localStorage.getItem('userId') || sessionStorage.getItem('userId');
  
  if (userType === 'Service Provider') {
    // Get all service providers
    const providersResponse = await axios.get(
      "/api/service-providers/getAll", 
      { headers: { 'Authorization': `Bearer ${token}` }}
    );
    
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
    
    await axios.put(
      `/api/addresses/updateAddress/${addressId}`, 
      updatedMainAddress,
      { headers: { 'Authorization': `Bearer ${token}` }}
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
        
        await axios.put(
          `/api/addresses/updateAddress/${addr.addressId}`, 
          otherAddress,
          { headers: { 'Authorization': `Bearer ${token}` }}
        );
      }
    }
    
  } else {
    // Logic for customers
    try {
      const customersResponse = await axios.get(
        "/api/customers/getAll", 
        { headers: { 'Authorization': `Bearer ${token}` }}
      );
      
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
      
      await axios.put(
        `/api/addresses/updateAddress/${addressId}`, 
        updatedMainAddress,
        { headers: { 'Authorization': `Bearer ${token}` }}
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
          
          await axios.put(
            `/api/addresses/updateAddress/${addr.addressId}`, 
            otherAddress,
            { headers: { 'Authorization': `Bearer ${token}` }}
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
  const response = await axios.get('https://psgc.gitlab.io/api/provinces');
  return response.data;
};

/**
 * Fetch PSGC cities for a province
 */
export const fetchCities = async (provinceCode) => {
  const response = await axios.get(`https://psgc.gitlab.io/api/provinces/${provinceCode}/cities-municipalities`);
  return response.data;
};

/**
 * Fetch PSGC barangays for a city
 */
export const fetchBarangays = async (cityCode) => {
  const response = await axios.get(`https://psgc.gitlab.io/api/cities-municipalities/${cityCode}/barangays`);
  return response.data;
};
