import React, { useState } from 'react';
import { User, Home, MapPin, History, Lock } from 'lucide-react';
import Footer from '../Footer';
import defaultProfileImage from '../../assets/tate.webp';// Adjust the path as needed

// Import components for customer profile sections
import CustomerProfileContent from './profile/CustomerProfileContent';
import CustomerAddressContent from './profile/CustomerAddressContent';
import CustomerBookingHistoryContent from './profile/CustomerBookingHistoryContent';
import CustomerChangePasswordContent from './profile/CustomerChangePasswordContent';

function CustomerProfilePage() {
  const [selectedImage, setSelectedImage] = useState(defaultProfileImage);
  const [activeSection, setActiveSection] = useState('profile');

  // Render the appropriate content based on active section
  const renderContent = () => {
    switch (activeSection) {
      case 'profile':
        return <CustomerProfileContent selectedImage={selectedImage} setSelectedImage={setSelectedImage} />;
      case 'address':
        return <CustomerAddressContent />;
      case 'bookingHistory':
        return <CustomerBookingHistoryContent />;
      case 'password':
        return <CustomerChangePasswordContent />;
      default:
        return <CustomerProfileContent selectedImage={selectedImage} setSelectedImage={setSelectedImage} />;
    }
  };

  return (
    <div className="min-h-screen bg-gray-50">
      <div className="container mx-auto py-4 px-4 border-solid">
        <div className="grid grid-cols-1 md:grid-cols-12 gap-3">
          {/* Sidebar */}
          <div className="md:col-span-3">
            <div className="bg-white p-2 rounded shadow">
              <div className="flex items-center gap-1 mb-2">
                <User size={20} />
                <h2 className="text-lg font-semibold">My Account</h2>
              </div>
              <nav>
                <ul>
                  <li 
                    className={`flex items-center p-2 rounded cursor-pointer ${activeSection === 'profile' ? 'bg-blue-50 text-blue-600' : 'hover:bg-gray-100'}`}
                    onClick={() => setActiveSection('profile')}
                  >
                    <span className="w-10"><Home size={18} /></span>
                    <span>Profile</span>
                  </li>
                  <li 
                    className={`flex items-center p-2 rounded cursor-pointer ${activeSection === 'address' ? 'bg-blue-50 text-blue-600' : 'hover:bg-gray-100'}`}
                    onClick={() => setActiveSection('address')}
                  >
                    <span className="w-10"><MapPin size={18} /></span>
                    <span>Address</span>
                  </li>
                  <li 
                    className={`flex items-center p-2 rounded cursor-pointer ${activeSection === 'bookingHistory' ? 'bg-blue-50 text-blue-600' : 'hover:bg-gray-100'}`}
                    onClick={() => setActiveSection('bookingHistory')}
                  >
                    <span className="w-10"><History size={18} /></span>
                    <span>Booking History</span>
                  </li>
                  <li 
                    className={`flex items-center p-2 rounded cursor-pointer ${activeSection === 'password' ? 'bg-blue-50 text-blue-600' : 'hover:bg-gray-100'}`}
                    onClick={() => setActiveSection('password')}
                  >
                    <span className="w-10"><Lock size={18} /></span>
                    <span>Change Password</span>
                  </li>
                </ul>
              </nav>
            </div>
          </div>

          {/* Main Content */}
          <div className="md:col-span-9">
            <div className="bg-white p-4 rounded shadow flex flex-col gap-2">
              {renderContent()}
            </div>
          </div>
        </div>
      </div>
      <Footer />
    </div>  
  );
}

export default CustomerProfilePage;