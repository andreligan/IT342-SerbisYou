import React, { useState } from 'react';
import { User, Home, MapPin, Briefcase, Lock, Package } from 'lucide-react';
import Footer from '../Footer';
import tate from '../../assets/tate.webp';
import {
  Box,
  Container,
  Grid,
  List,
  ListItem,
  ListItemIcon,
  ListItemText,
  Paper,
  Typography,
} from '@mui/material';

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
        return <ProfileContent selectedImage={selectedImage}  setSelectedImage={setSelectedImage}/>;
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
    <Box sx={{ minHeight: '100vh', bgcolor: '#F9FAFB' }}>
      <Container maxWidth="lg" sx={{ py: 4,}}>
        <Grid container spacing={3}>
          {/* Sidebar */}
          <Grid item xs={12} md={3}>
            <Paper sx={{ p: 2 }}>
              <Box sx={{ display: 'flex', alignItems: 'center', gap: 1, mb: 2 }}>
                <User size={20} />
                <Typography variant="h6">My Account</Typography>
              </Box>
              <List component="nav" disablePadding>
                <ListItem 
                  button 
                  selected={activeSection === 'profile'} 
                  onClick={() => setActiveSection('profile')}
                  sx={{ borderRadius: 1, cursor: 'pointer' }}
                >
                  <ListItemIcon sx={{ minWidth: 40 }}><Home size={18} /></ListItemIcon>
                  <ListItemText primary="Profile" />
                </ListItem>
                <ListItem 
                  button 
                  selected={activeSection === 'address'} 
                  onClick={() => setActiveSection('address')}
                  sx={{ borderRadius: 1, cursor: 'pointer' }}
                >
                  <ListItemIcon sx={{ minWidth: 40 }}><MapPin size={18} /></ListItemIcon>
                  <ListItemText primary="Address" />
                </ListItem>
                <ListItem 
                  button 
                  selected={activeSection === 'business'} 
                  onClick={() => setActiveSection('business')}
                  sx={{ borderRadius: 1, cursor: 'pointer' }}
                >
                  <ListItemIcon sx={{ minWidth: 40 }}><Briefcase size={18} /></ListItemIcon>
                  <ListItemText primary="Business Details" />
                </ListItem>
                <ListItem 
                  button 
                  selected={activeSection === 'services'} 
                  onClick={() => setActiveSection('services')}
                  sx={{ borderRadius: 1, cursor: 'pointer' }}
                >
                  <ListItemIcon sx={{ minWidth: 40 }}><Package size={18} /></ListItemIcon>
                  <ListItemText primary="My Services" />
                </ListItem>
                <ListItem 
                  button 
                  selected={activeSection === 'password'} 
                  onClick={() => setActiveSection('password')}
                  sx={{ borderRadius: 1, cursor: 'pointer' }}
                >
                  <ListItemIcon sx={{ minWidth: 40 }}><Lock size={18} /></ListItemIcon>
                  <ListItemText primary="Change Password" />
                </ListItem>
              </List>
            </Paper>
          </Grid>

          {/* Main Content */}
          <Grid item xs={12} md={7} >
            <Paper sx={{ p: 4, display: 'flex', flexDirection: 'column', gap: 2 }}>
              {renderContent()}
            </Paper>
          </Grid>
        </Grid>
      </Container>
      <Footer />
    </Box>  
  );
}

export default ServiceProviderProfile;