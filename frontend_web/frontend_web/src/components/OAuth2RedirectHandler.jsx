// Create a new file: OAuth2RedirectHandler.jsx
import { useEffect } from 'react';
import { useNavigate, useLocation } from 'react-router-dom';

const OAuth2RedirectHandler = () => {
  const navigate = useNavigate();
  const location = useLocation();
  
  useEffect(() => {
    const params = new URLSearchParams(location.search);
    const token = params.get('token');
    const userId = params.get('userId');
    const role = params.get('role');
    
    if (token) {
      // Store token and user info
      localStorage.setItem('authToken', token);
      localStorage.setItem('userId', userId);
      localStorage.setItem('userRole', role);
      localStorage.setItem('isAuthenticated', 'true');
      
      // Redirect based on role
      if (role.toLowerCase() === 'customer') {
        navigate('/customerHomePage');
      } else if (role.toLowerCase() === 'service provider') {
        navigate('/serviceProviderHomePage');
      } else {
        navigate('/');
      }
    } else {
      navigate('/login', { state: { error: "Authentication failed" }});
    }
  }, [location, navigate]);
  
  return (
    <div className="d-flex justify-content-center">
      <div className="spinner-border" role="status">
        <span className="visually-hidden">Loading...</span>
      </div>
    </div>
  );
};

export default OAuth2RedirectHandler;