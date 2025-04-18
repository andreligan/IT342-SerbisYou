import React, { useState } from 'react';
import { Dialog, DialogContent, DialogTitle, TextField, Button, Checkbox, FormControlLabel, Typography, IconButton, Box, Divider, Link } from '@mui/material';
import CloseIcon from '@mui/icons-material/Close';
import GoogleIcon from '@mui/icons-material/Google';
import axios from 'axios';
import { useNavigate } from 'react-router-dom'; // Import useNavigate for redirection

const LoginPopup = ({ open, onClose }) => {
  const [userName, setuserName] = useState(''); // Changed from email to userName
  const [password, setPassword] = useState('');
  const [rememberMe, setRememberMe] = useState(false);
  const [errorMessage, setErrorMessage] = useState('');
  const navigate = useNavigate(); // Initialize useNavigate

  const handleSubmit = async (e) => {
    e.preventDefault();
    try {
      // Send login request to the backend
      const response = await axios.post('/api/user-auth/login', {
        userName,
        password,
      });
    
      // Log first to see what's actually in the response
      console.log('Full response data:', response.data);
      
      // Then extract the data with potential fallback
      const token = response.data.token;
      const role = response.data.role;
      const userId = response.data.userId || response.data.id; // Try alternative property names
      
      // console.log('Login successful. Token:', token, 'Role:', role, 'User ID:', userId);
      
      // Store in localStorage/sessionStorage and add userId too
      if (rememberMe) {
        localStorage.setItem('authToken', token);
        localStorage.setItem('userRole', role);
        localStorage.setItem('userId', userId); // Store userId too
        localStorage.setItem('isAuthenticated', 'true');
      } else {
        sessionStorage.setItem('authToken', token);
        sessionStorage.setItem('userRole', role);
        sessionStorage.setItem('userId', userId); // Store userId too
        sessionStorage.setItem('isAuthenticated', 'true');
      }
  
      // Redirect based on role
      switch (role.toLowerCase()) { // Convert role to lowercase for case-insensitive comparison
        case 'customer':
          navigate('/customerHomePage');
          break;
        case 'admin':
          navigate('/adminDashboard');
          break;
        case 'service provider': // Match the exact string from the database
          navigate('/serviceProviderHomePage');
          break;
        default:
          navigate('/defaultPage'); // Fallback route
          break;
      }
  
      // Close the popup
      onClose();
    } catch (error) {
      // Handle login error
      const errorMsg = error.response?.data?.message || 'Invalid username or password.';
      setErrorMessage(errorMsg);
    }
  };

  return (
    <Dialog 
      open={open} 
      onClose={onClose}
      maxWidth="sm"
      fullWidth
    >
      <DialogTitle sx={{ mb: 2, textAlign: 'center', pt: 4 }}>
        <IconButton
          aria-label="close"
          onClick={onClose}
          sx={{
            position: 'absolute',
            right: 8,
            top: 8,
            color: (theme) => theme.palette.grey[500],
          }}
        >
          <CloseIcon />
        </IconButton>
        <Typography variant="h4" component="div" sx={{ fontWeight: 500, color: '#445561' }}>
          Welcome Back!
        </Typography>
        <Typography variant="subtitle1" sx={{ mt: 1, color: '#677483' }}>
          Please login to your account
        </Typography>
      </DialogTitle>
      
      <DialogContent>
        <Box component="form" onSubmit={handleSubmit} sx={{ mt: 1 }}>
          {errorMessage && (
            <Typography color="error" sx={{ mb: 2 }}>
              {errorMessage}
            </Typography>
          )}
          <Typography variant="subtitle1" sx={{ mb: 1 }}>userName</Typography> {/* Updated label */}
          <TextField
            margin="dense"
            id="userName"
            type="text" // Changed type to text
            fullWidth
            variant="outlined"
            value={userName} // Updated state
            onChange={(e) => setuserName(e.target.value)} // Updated handler
            required
            sx={{ mb: 3 }}
          />
          
          <Typography variant="subtitle1" sx={{ mb: 1 }}>Password</Typography>
          <TextField
            margin="dense"
            id="password"
            type="password"
            fullWidth
            variant="outlined"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            required
            sx={{ mb: 1 }}
          />
          
          <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 3 }}>
            <FormControlLabel
              control={
                <Checkbox 
                  checked={rememberMe} 
                  onChange={() => setRememberMe(!rememberMe)} 
                  name="rememberMe" 
                />
              }
              label="Remember Me"
            />
            <Link href="#forgot-password" underline="hover">
              Forgot Password?
            </Link>
          </Box>
          
          <Button
            type="submit"
            fullWidth
            variant="contained"
            sx={{ 
              py: 1.5, 
              textTransform: 'none', 
              backgroundColor: '#f5d14e', 
              color: '#000',
              '&:hover': {
                backgroundColor: '#e9c53a',
              }
            }}
          >
            Log In
          </Button>
          
          <Box sx={{ textAlign: 'center', mt: 3 }}>
            <Typography variant="body1">
              No account yet? <Link href="#signup" underline="hover" sx={{ fontWeight: 500 }}>Sign up here</Link>
            </Typography>
          </Box>
          
          <Divider sx={{ my: 3 }} />
          
          <Button
            fullWidth
            variant="outlined"
            startIcon={<GoogleIcon />}
            sx={{ textTransform: 'none', mb: 2 }}
          >
            Log in with Google
          </Button>
        </Box>
      </DialogContent>
    </Dialog>
  );
};

export default LoginPopup;