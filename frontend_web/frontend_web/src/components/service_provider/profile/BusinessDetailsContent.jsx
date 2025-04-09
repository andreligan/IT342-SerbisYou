import React from 'react';
import {
  Box,
  Button,
  Grid,
  TextField,
  Typography,
} from '@mui/material';

function BusinessDetailsContent() {
  return (
    <>
      <Box>
        <Typography variant="h4" gutterBottom>Business Details</Typography>
        <Typography variant="body1" color="text.secondary" sx={{ mb: 4 }}>
          Manage your business information
        </Typography>
      </Box>

      <Grid container spacing={2}>
        <Grid item xs={12}>
          <TextField
            fullWidth
            label="Business Name"
            variant="outlined"
          />
        </Grid>
        <Grid item xs={12}>
          <TextField
            fullWidth
            label="Business Description"
            variant="outlined"
            multiline
            rows={4}
          />
        </Grid>
        <Grid item xs={12} md={6}>
          <TextField
            fullWidth
            label="Business Category"
            variant="outlined"
          />
        </Grid>
        <Grid item xs={12} md={6}>
          <TextField
            fullWidth
            label="Year Established"
            variant="outlined"
          />
        </Grid>
      </Grid>

      <Box sx={{ display: 'flex', justifyContent: 'center', mt: 4 }}>
        <Button variant="contained" sx={{ width: 150 }} >
          Save
        </Button>
      </Box>
    </>
  );
}

export default BusinessDetailsContent;