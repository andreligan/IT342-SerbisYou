// Create a new file: OAuth2RedirectHandler.jsx
import { useEffect } from 'react';
import { useNavigate, useLocation } from 'react-router-dom';

function OAuth2RedirectHandler() {
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
      if (role?.toLowerCase() === 'customer') {
        navigate('/customerHomePage');
      } else if (role?.toLowerCase() === 'service provider') {
        navigate('/serviceProviderHomePage');
      } else {
        navigate('/');
      }
    } else {
      navigate('/login', { state: { error: "Authentication failed" }});
    }
  }, [location, navigate]);
  
  return (
    <div className="flex justify-center items-center h-screen">
      <div className="animate-spin rounded-full h-12 w-12 border-t-2 border-b-2 border-yellow-500"></div>
      <p className="ml-3 text-lg">Processing authentication...</p>
    </div>
  );
}

export default OAuth2RedirectHandler;