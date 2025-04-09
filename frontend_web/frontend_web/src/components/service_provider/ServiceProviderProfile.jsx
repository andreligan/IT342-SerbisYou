import React, { useState } from 'react';
import { User, Home, MapPin, Briefcase, Lock } from 'lucide-react';
import {
  AppBar,
  Box,
  Button,
  Container,
  Drawer,
  Grid,
  IconButton,
  List,
  ListItem,
  ListItemIcon,
  ListItemText,
  Paper,
  TextField,
  Toolbar,
  Typography,
  Link,
  Stack,
  Avatar,
} from '@mui/material';

function ServiceProviderProfile() {
  // ...existing code...
const [selectedImage, setSelectedImage] = useState('https://images.unsplash.com/photo-1633332755192-727a05c4013d?w=150&h=150&fit=crop');
// ...existing code...

  return (
    <Box sx={{ minHeight: '100vh', bgcolor: '#F9FAFB' }}>
      <Container maxWidth="lg" sx={{ py: 4 }}>
        <Grid container spacing={3}>
          {/* Sidebar */}
          <Grid item xs={12} md={3}>
            <Paper sx={{ p: 2 }}>
              <Box sx={{ display: 'flex', alignItems: 'center', gap: 1, mb: 2 }}>
                <User size={20} />
                <Typography variant="h6">My Account</Typography>
              </Box>
              <List component="nav" disablePadding>
                <ListItem button selected sx={{ borderRadius: 1 }}>
                  <ListItemIcon sx={{ minWidth: 40 }}><Home size={18} /></ListItemIcon>
                  <ListItemText primary="Profile" />
                </ListItem>
                <ListItem button sx={{ borderRadius: 1 }}>
                  <ListItemIcon sx={{ minWidth: 40 }}><MapPin size={18} /></ListItemIcon>
                  <ListItemText primary="Address" />
                </ListItem>
                <ListItem button sx={{ borderRadius: 1 }}>
                  <ListItemIcon sx={{ minWidth: 40 }}><Briefcase size={18} /></ListItemIcon>
                  <ListItemText primary="Business Details" />
                </ListItem>
                <ListItem button sx={{ borderRadius: 1 }}>
                  <ListItemIcon sx={{ minWidth: 40 }}><Lock size={18} /></ListItemIcon>
                  <ListItemText primary="Change Password" />
                </ListItem>
              </List>
            </Paper>
          </Grid>

          {/* Main Content */}
          <Grid item xs={12} md={9} sx={{ border: 'solid' }}>
            <Paper sx={{ p: 4 }}>
              <Typography variant="h4" gutterBottom>My Profile</Typography>
              <Typography variant="body1" color="text.secondary" sx={{ mb: 4 }}>
                Manage and protect your account
              </Typography>

              <Grid container spacing={3}>
                <Grid item xs={12}>
                  <TextField
                    fullWidth
                    label="Username"
                    variant="outlined"
                  />
                </Grid>
                <Grid item xs={12}>
                  <TextField
                    fullWidth
                    label="Name"
                    variant="outlined"
                  />
                </Grid>
                <Grid item xs={12}>
                  <TextField
                    fullWidth
                    label="Email"
                    type="email"
                    variant="outlined"
                  />
                </Grid>
                <Grid item xs={12}>
                  <TextField
                    fullWidth
                    label="Phone Number"
                    type="tel"
                    variant="outlined"
                  />
                </Grid>
                <Grid item xs={12}>
                  <TextField
                    fullWidth
                    label="Gender"
                    variant="outlined"
                  />
                </Grid>
                <Grid item xs={12}>
                  <TextField
                    fullWidth
                    label="Date of Birth"
                    type="date"
                    InputLabelProps={{ shrink: true }}
                    variant="outlined"
                  />
                </Grid>
                <Grid item xs={12}>
                  <Box sx={{ display: 'flex', alignItems: 'center', gap: 4, mt: 2 }}>
                    <Box>
                      <Avatar
                        src={selectedImage}
                        sx={{ width: 100, height: 100, mb: 1 }}
                      />
                      <Button variant="outlined" size="small" fullWidth>
                        Select Image
                      </Button>
                    </Box>
                    <Button variant="contained" sx={{ px: 4 }}>
                      Save
                    </Button>
                  </Box>
                </Grid>
              </Grid>
            </Paper>
          </Grid>
        </Grid>
      </Container>
    </Box>
  );
}

export default ServiceProviderProfile;