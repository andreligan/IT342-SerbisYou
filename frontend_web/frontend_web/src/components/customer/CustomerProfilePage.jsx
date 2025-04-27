import React, { useState, useEffect } from 'react';
import { User, Home, MapPin, History, Lock } from 'lucide-react';
import { useParams, useNavigate } from 'react-router-dom';
import Footer from '../Footer';
import defaultProfileImage from '../../assets/tate.webp';

// Import components for customer profile sections
import CustomerProfileContent from './profile/CustomerProfileContent';
import CustomerAddressContent from './profile/CustomerAddressContent';
import CustomerBookingHistoryContent from './profile/CustomerBookingHistoryContent';
import CustomerChangePasswordContent from './profile/CustomerChangePasswordContent';

function CustomerProfilePage() {
  const [selectedImage, setSelectedImage] = useState(defaultProfileImage);
  const [activeSection, setActiveSection] = useState('profile');
  const { tab } = useParams();
  const navigate = useNavigate();

  // Set active section based on URL parameter when component mounts or tab changes
  useEffect(() => {
    if (!tab) {
      // If no tab parameter is provided, redirect to the default tab
      navigate('/customerProfile/profile', { replace: true });
    } else if (['profile', 'address', 'bookingHistory', 'password'].includes(tab)) {
      setActiveSection(tab);
    } else {
      // If invalid tab parameter, navigate to the default tab
      navigate('/customerProfile/profile', { replace: true });
    }
  }, [tab, navigate]);

  // Handle tab change
  const handleTabChange = (tabName) => {
    setActiveSection(tabName);
    navigate(`/customerProfile/${tabName}`);
  };

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
            <div className="bg-white pt-8 pb-8 pl-5 rounded shadow">
              <div className="flex items-center gap-1 mb-2">
                <User size={20} />
                <h2 className="text-lg font-semibold">My Account</h2>
              </div>
              <nav>
                <ul>
                  <li 
                    className={`flex items-center p-2 rounded cursor-pointer ${activeSection === 'profile' ? 'bg-yellow-50 text-[#F4CE14]' : 'hover:bg-gray-100'}`}
                    onClick={() => handleTabChange('profile')}
                  >
                    <span className="w-10"><Home size={18} /></span>
                    <span>Profile</span>
                  </li>
                  <li 
                    className={`flex items-center p-2 rounded cursor-pointer ${activeSection === 'address' ? 'bg-yellow-50 text-[#F4CE14]' : 'hover:bg-gray-100'}`}
                    onClick={() => handleTabChange('address')}
                  >
                    <span className="w-10"><MapPin size={18} /></span>
                    <span>Address</span>
                  </li>
                  <li 
                    className={`flex items-center p-2 rounded cursor-pointer ${activeSection === 'bookingHistory' ? 'bg-yellow-50 text-[#F4CE14]' : 'hover:bg-gray-100'}`}
                    onClick={() => handleTabChange('bookingHistory')}
                  >
                    <span className="w-10"><History size={18} /></span>
                    <span>Booking History</span>
                  </li>
                  <li 
                    className={`flex items-center p-2 rounded cursor-pointer ${activeSection === 'password' ? 'bg-yellow-50 text-[#F4CE14]' : 'hover:bg-gray-100'}`}
                    onClick={() => handleTabChange('password')}
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