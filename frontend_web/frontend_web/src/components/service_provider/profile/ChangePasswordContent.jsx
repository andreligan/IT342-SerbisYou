import React from 'react';
import {
  Box,
  Button,
  Grid,
  TextField,
  Typography,
} from '@mui/material';

function ChangePasswordContent() {
  return (
    <>
      <Box>
        <Typography variant="h4" gutterBottom>Change Password</Typography>
        <Typography variant="body1" color="text.secondary" sx={{ mb: 4 }}>
          Update your password
        </Typography>
      </Box>

      <Grid container spacing={2}>
        <Grid item xs={12}>
          <TextField
            fullWidth
            label="Current Password"
            type="password"
            variant="outlined"
          />
        </Grid>
        <Grid item xs={12}>
          <TextField
            fullWidth
            label="New Password"
            type="password"
            variant="outlined"
          />
        </Grid>
        <Grid item xs={12}>
          <TextField
            fullWidth
            label="Confirm New Password"
            type="password"
            variant="outlined"
          />
        </Grid>
      </Grid>

      <Box sx={{ display: 'flex', justifyContent: 'center', mt: 4 }}>
        <Button variant="contained" color="primary" sx={{ width: 150 }}>
          Update
        </Button>
      </Box>
    </>
  );
}

export default ChangePasswordContent;