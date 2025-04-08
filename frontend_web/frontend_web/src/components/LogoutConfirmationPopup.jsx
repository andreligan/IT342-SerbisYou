import React from "react";
import {
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  Button,
  Typography,
  Box,
} from "@mui/material";

const LogoutConfirmationPopup = ({ open, onClose, onConfirm }) => {
  return (
    <Dialog
      open={open}
      onClose={onClose}
      maxWidth="xs"
      fullWidth
      sx={{
        "& .MuiPaper-root": {
          borderRadius: "12px",
          padding: "16px",
        },
      }}
    >
      {/* Title */}
      <DialogTitle>
        <Typography
          variant="h5"
          align="center"
          sx={{ fontWeight: "bold", color: "#495E57" }}
        >
          Logout
        </Typography>
      </DialogTitle>

      {/* Content */}
      <DialogContent>
        <Typography
          variant="body1"
          align="center"
          sx={{ color: "#677483", marginBottom: "16px" }}
        >
          Are you sure you want to log out?
        </Typography>
      </DialogContent>

      {/* Actions */}
      <DialogActions>
        <Box
          sx={{
            display: "flex",
            justifyContent: "center",
            gap: "16px",
            width: "100%",
          }}
        >
          <Button
            variant="contained"
            onClick={onClose}
            sx={{
              backgroundColor: "#f44336",
              color: "#fff",
              textTransform: "none",
              "&:hover": {
                backgroundColor: "#d32f2f",
              },
            }}
          >
            Cancel
          </Button>
          <Button
            variant="contained"
            onClick={onConfirm}
            sx={{
              backgroundColor: "#4caf50",
              color: "#fff",
              textTransform: "none",
              "&:hover": {
                backgroundColor: "#388e3c",
              },
            }}
          >
            Confirm
          </Button>
        </Box>
      </DialogActions>
    </Dialog>
  );
};

export default LogoutConfirmationPopup;