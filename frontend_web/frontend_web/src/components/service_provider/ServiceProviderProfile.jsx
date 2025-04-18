import React, { useState } from 'react';
import { User, Home, MapPin, Briefcase, Lock, Package } from 'lucide-react';
import Footer from '../Footer';
import tate from '../../assets/tate.webp';

// Import components from profile directory
import ProfileContent from './profile/ProfileContent';
import AddressContent from './profile/AddressContent';
import BusinessDetailsContent from './profile/BusinessDetailsContent';
import MyServicesContent from './profile/MyServicesContent';
import ChangePasswordContent from './profile/ChangePasswordContent';

function ServiceProviderProfile() {
  const [selectedImage, setSelectedImage] = useState(tate);
  const [activeSection, setActiveSection] = useState('profile');

  // Render the appropriate content based on active section
  const renderContent = () => {
    switch (activeSection) {
      case 'profile':
        return <ProfileContent selectedImage={selectedImage} setSelectedImage={setSelectedImage} />;
      case 'address':
        return <AddressContent />;
      case 'business':
        return <BusinessDetailsContent />;
      case 'services':
        return <MyServicesContent />;
      case 'password':
        return <ChangePasswordContent />;
      default:
        return <ProfileContent selectedImage={selectedImage} />;
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
                    className={`flex items-center p-2 rounded cursor-pointer ${activeSection === 'business' ? 'bg-blue-50 text-blue-600' : 'hover:bg-gray-100'}`}
                    onClick={() => setActiveSection('business')}
                  >
                    <span className="w-10"><Briefcase size={18} /></span>
                    <span>Business Details</span>
                  </li>
                  <li 
                    className={`flex items-center p-2 rounded cursor-pointer ${activeSection === 'services' ? 'bg-blue-50 text-blue-600' : 'hover:bg-gray-100'}`}
                    onClick={() => setActiveSection('services')}
                  >
                    <span className="w-10"><Package size={18} /></span>
                    <span>My Services</span>
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

export default ServiceProviderProfile;