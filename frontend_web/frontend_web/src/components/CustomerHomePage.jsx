import React from 'react';

const CustomerHomePage = () => {
  return (
    <div style={{ textAlign: 'center', padding: '20px' }}>
      <h1>Welcome to SerbisYou</h1>
      <p>Your one-stop solution for all your service needs!</p>
      <div style={{ marginTop: '20px' }}>
        <button style={{ margin: '10px', padding: '10px 20px' }}>View Services</button>
        <button style={{ margin: '10px', padding: '10px 20px' }}>My Bookings</button>
        <button style={{ margin: '10px', padding: '10px 20px' }}>Contact Support</button>
      </div>
    </div>
  );
};

export default CustomerHomePage;