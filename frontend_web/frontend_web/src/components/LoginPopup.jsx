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
    setErrorMessage("");
    
    try {
      // Send login request to the backend
      const response = await axios.post('/api/user-auth/login', {
        userName,
        password,
      });
    
      // Extract the data
      const { token, role, userId } = response.data;  // Extract userId from response
      
      // Store in localStorage/sessionStorage
      if (rememberMe) {
        localStorage.setItem('authToken', token);
        localStorage.setItem('userRole', role);
        localStorage.setItem('userId', userId);  // Add this line
        localStorage.setItem('isAuthenticated', 'true');
      } else {
        sessionStorage.setItem('authToken', token);
        sessionStorage.setItem('userRole', role);
        sessionStorage.setItem('userId', userId);  // Add this line
        sessionStorage.setItem('isAuthenticated', 'true');
      }
  
      // Close the login popup
      onClose();
  
      // Redirect based on role WITH HISTORY REPLACEMENT
      if (role.toLowerCase() === "customer") {
        navigate('/customerHomePage', { replace: true });
      } else if (role.toLowerCase() === "service provider") {
        navigate('/serviceProviderHomePage', { replace: true });
      } else {
        // Handle other roles or unexpected cases
        console.error("Unknown user role:", role);
        navigate('/');
      }
    } catch (error) {
      // Handle login error
      console.error("Login failed:", error);
      setErrorMessage(error.response?.data?.message || "Login failed. Please check your credentials.");
    }
  };

  const handleGoogleLogin = () => {
    window.location.href = 'http://localhost:8080/oauth2/authorization/google';
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
            onClick={handleGoogleLogin}
          >
            Log in with Google
          </Button>
        </Box>
      </DialogContent>
    </Dialog>
  );
};

export default LoginPopup;