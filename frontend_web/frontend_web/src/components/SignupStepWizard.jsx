import React, { useState } from 'react';
import { Button, TextField, Typography, Box, Stepper, Step, StepLabel, Container, Paper } from '@mui/material';
import { StepConnector } from '@mui/material';
import { styled } from '@mui/system';
import axios from 'axios';

const SignupStepWizard = () => {
  const [currentStep, setCurrentStep] = useState(0);
  const [formData, setFormData] = useState({
    accountType: '',
    lastName: '',
    firstName: '',
    phoneNumber: '',
    address: '',
    businessName: '', // Added businessName for Service Provider
    yearsOfExperience: '',
    userName: '',
    email: '',
    password: '',
    confirmPassword: '',
  });
  const [errorMessage, setErrorMessage] = useState('');

  const steps = ['Choose Account Type', 'User Details', 'Account Credentials', 'Complete'];

  const handleSelection = (type) => {
    setFormData({ ...formData, accountType: type });
    handleNext();
  };

  const handleNext = () => {
    setCurrentStep((prevStep) => prevStep + 1);
  };

  const handlePrevious = () => {
    setCurrentStep((prevStep) => prevStep - 1);
  };

  const handleChange = (event) => {
    setFormData({ ...formData, [event.target.name]: event.target.value });
  };

  const handleSubmit = async () => {
    if (formData.password !== formData.confirmPassword) {
      setErrorMessage('Passwords do not match.');
      return;
    }
  
    try {
      // Prepare UserAuthEntity
      const userAuth = {
        userName: formData.userName,
        email: formData.email,
        password: formData.password,
        role: formData.accountType,
      };
  
      // Prepare CustomerEntity or ServiceProviderEntity based on account type
      const customer =
        formData.accountType === 'Customer'
          ? {
              firstName: formData.firstName,
              lastName: formData.lastName,
              phoneNumber: formData.phoneNumber,
              address: {
                addressId: 1, // Replace with actual address ID or logic
              },
            }
          : null;
  
      const serviceProvider =
        formData.accountType === 'Service Provider'
          ? {
              firstName: formData.firstName,
              lastName: formData.lastName,
              phoneNumber: formData.phoneNumber,
              businessName: formData.businessName,
              yearsOfExperience: parseInt(formData.yearsOfExperience, 10),
              address: {
                addressId: 1, // Replace with actual address ID or logic
              },
            }
          : null;
  
      // Send both UserAuthEntity and either CustomerEntity or ServiceProviderEntity in a single request
      const requestBody = { userAuth, customer, serviceProvider };
      const response = await axios.post('/api/user-auth/register', requestBody);
  
      alert(response.data); // Show success message
      setCurrentStep((prevStep) => prevStep + 1); // Move to the next step
    } catch (error) {
      const errorMsg = error.response?.data?.message || 'An error occurred during registration.';
      setErrorMessage(errorMsg);
    }
  };

  const CustomStepConnector = styled(StepConnector)(({ theme }) => ({
    '& .MuiStepConnector-line': {
      borderTopWidth: 5,
      width: '80%',
      marginLeft: '10%',
      marginTop: '0.5rem',
      transform: 'translateY(-50%)',
    },
  }));

  return (
    <Container maxWidth="lg">
      <Paper elevation={3} sx={{ p: 10, mt: 5, borderRadius: '20px' }}>
        <Typography variant="h2" align="center" gutterBottom>
          Get Started
        </Typography>

        <Stepper activeStep={currentStep} alternativeLabel connector={<CustomStepConnector />} sx={{ marginTop: 5 }}>
          {steps.map((label, index) => (
            <Step key={index}>
              <StepLabel sx={{ '& .MuiStepIcon-root': { fontSize: '3rem' } }}>{label}</StepLabel>
            </Step>
          ))}
        </Stepper>

        <Box mt={8}>
          {currentStep === 0 && (
            <Box textAlign="center">
              <Typography variant="h6">I am a</Typography>
              <Box mt={8}>
                <Button variant="contained" sx={{ m: 5, width: '200px', height: '50px' }} onClick={() => handleSelection('Customer')}>
                  Customer
                </Button>
                <Button variant="contained" sx={{ m: 5, width: '200px', height: '50px' }} onClick={() => handleSelection('Service Provider')}>
                  Service Provider
                </Button>
              </Box>
            </Box>
          )}

          {currentStep === 1 && (
            <Box>
              <Typography variant="h6" align="center">
                Enter Your Details
              </Typography>
              <Box mt={3}>
                <TextField fullWidth label="Last Name" name="lastName" value={formData.lastName} onChange={handleChange} margin="normal" />
                <TextField fullWidth label="First Name" name="firstName" value={formData.firstName} onChange={handleChange} margin="normal" />
                <TextField fullWidth label="Phone Number" name="phoneNumber" value={formData.phoneNumber} onChange={handleChange} margin="normal" />
                <TextField fullWidth label="Address" name="address" value={formData.address} onChange={handleChange} margin="normal" />

                {formData.accountType === 'Service Provider' && (
                  <>
                    <TextField fullWidth label="Business Name" name="businessName" value={formData.businessName} onChange={handleChange} margin="normal" />
                    <TextField fullWidth label="Years of Experience" name="yearsOfExperience" value={formData.yearsOfExperience} onChange={handleChange} margin="normal" />
                  </>
                )}
              </Box>
            </Box>
          )}

          {currentStep === 2 && (
            <Box>
              <Typography variant="h6" align="center">
                Create Your Account
              </Typography>
              <Box mt={3}>
                <TextField fullWidth label="Username" name="userName" value={formData.userName} onChange={handleChange} margin="normal" />
                <TextField fullWidth label="Email" name="email" value={formData.email} onChange={handleChange} margin="normal" />
                <TextField fullWidth label="Password" name="password" type="password" value={formData.password} onChange={handleChange} margin="normal" />
                <TextField fullWidth label="Confirm Password" name="confirmPassword" type="password" value={formData.confirmPassword} onChange={handleChange} margin="normal" />
              </Box>
            </Box>
          )}

          {currentStep === 3 && (
            <Box textAlign="center">
              <Typography variant="h5">Registration Complete!</Typography>
              <Typography variant="body1" mt={2}>
                Thank you for signing up. You can now access your account.
              </Typography>
              <Button variant="contained" color="success" sx={{ mt: 3 }}>
                Go to Dashboard
              </Button>
            </Box>
          )}
        </Box>

        {errorMessage && (
          <Typography color="error" align="center" mt={2}>
            {errorMessage}
          </Typography>
        )}

        <Box mt={4} display="flex" justifyContent="space-between">
          {currentStep > 0 && currentStep < 3 && (
            <Button variant="outlined" onClick={handlePrevious}>
              Previous
            </Button>
          )}
          {currentStep < 2 && (
            <Button variant="contained" onClick={handleNext}>
              Next
            </Button>
          )}
          {currentStep === 2 && (
            <Button variant="contained" onClick={handleSubmit}>
              Submit
            </Button>
          )}
        </Box>
      </Paper>
    </Container>
  );
};

export default SignupStepWizard;