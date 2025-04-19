import React from 'react';
import { Link, useNavigate } from 'react-router-dom';
import GoogleIcon from '../assets/google-icon.svg';

function SignupOptions() {
  const navigate = useNavigate();

  const handleGoogleSignup = () => {
    // Redirect to Google OAuth2 signup endpoint
    window.location.href = 'http://localhost:8080/oauth2/authorization/google';
  };

  const handleManualSignup = () => {
    navigate('/signup/type');
  };

  return (
    <div className="container mx-auto px-4 py-8">
      <div className="bg-white shadow-xl rounded p-8 md:p-10 max-w-4xl mx-auto">
        <h1 className="text-4xl font-bold text-center mb-8 text-gray-800">Sign Up for Serbisyo</h1>
        
        <div className="flex flex-col space-y-6 mt-10">
          <button
            onClick={handleGoogleSignup}
            className="flex items-center justify-center gap-3 w-full py-3 px-4 bg-white border border-gray-300 rounded-lg shadow-sm hover:bg-gray-50 transition-colors"
          >
            <img src={GoogleIcon} alt="Google" className="w-6 h-6" />
            <span className="text-gray-700 font-medium">Continue with Google</span>
          </button>
          
          <div className="relative flex items-center justify-center">
            <div className="border-t border-gray-300 w-full"></div>
            <span className="bg-white px-4 text-gray-500 text-sm">OR</span>
            <div className="border-t border-gray-300 w-full"></div>
          </div>
          
          <button
            onClick={handleManualSignup}
            className="w-full py-3 px-4 bg-[#F4CE14] text-[#495E57] rounded-lg hover:bg-opacity-90 transition-colors font-medium"
          >
            Continue with Email
          </button>
        </div>
        
        <p className="text-center mt-8 text-gray-600">
          Already have an account?{' '}
          <Link to="/" className="text-[#495E57] hover:underline font-medium">
            Sign In
          </Link>
        </p>
      </div>
    </div>
  );
}

export default SignupOptions;