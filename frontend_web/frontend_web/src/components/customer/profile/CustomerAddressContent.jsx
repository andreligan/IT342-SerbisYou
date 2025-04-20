import React from 'react';
import AddressManager from '../../shared/AddressManager';

function CustomerAddressContent() {
  return (
    <AddressManager 
      userType="Customer" 
      title="My Addresses"
    />
  );
}

export default CustomerAddressContent;