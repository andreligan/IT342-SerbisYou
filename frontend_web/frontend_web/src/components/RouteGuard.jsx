import { Navigate } from 'react-router-dom';

const RouteGuard = ({ children }) => {
  const isAuthenticated = localStorage.getItem('isAuthenticated') === 'true' || 
    sessionStorage.getItem('isAuthenticated') === 'true';
  const isOAuthNew = localStorage.getItem('isOAuthNew') === 'true' || 
    sessionStorage.getItem('isOAuthNew') === 'true';
  
  // If user is authenticated but needs to change password, redirect to password change page
  if (isAuthenticated && isOAuthNew && window.location.pathname !== '/change-password') {
    return <Navigate to="/change-password" replace />;
  }
  
  return children;
};

export default RouteGuard;
