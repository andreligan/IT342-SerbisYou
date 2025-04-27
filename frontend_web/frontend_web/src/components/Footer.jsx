import React, { useState, useEffect } from "react";
import { useNavigate } from "react-router-dom";
import serbisyoLogo from "../assets/Serbisyo_Logo_New.png";

function Footer() {
  const [isAuthenticated, setIsAuthenticated] = useState(false);
  const navigate = useNavigate();

  useEffect(() => {
    // Check if the user is authenticated
    const token = localStorage.getItem("authToken") || sessionStorage.getItem("authToken");
    setIsAuthenticated(!!token);
  }, []);

  const handleSignUpClick = () => {
    navigate("/signup");
  };

  return (
    <div className="bg-[#495E57] text-white px-10 py-10 flex justify-between flex-wrap gap-5">
      {/* First Column */}
      <div className="flex-1 min-w-[200px]">
        <div className="flex items-center mb-4">
          <img src={serbisyoLogo} alt="SerbisYo Logo" className="mr-3 w-80 h-30" />
        </div>
        <p className="text-sm mb-2">Find trusted professionals for any home service you need.</p>
      </div>

      {/* Second Column */}
      <div className="flex-1 min-w-[200px]">
        <h2 className="text-lg font-bold text-[#F4CE14] leading-10 mb-2">Quick Links</h2>
        <p className="text-sm mb-2 hover:text-[#F4CE14] cursor-pointer transition-colors">About Us</p>
        <p className="text-sm mb-2 hover:text-[#F4CE14] cursor-pointer transition-colors">Services</p>
        <p className="text-sm mb-2 hover:text-[#F4CE14] cursor-pointer transition-colors">How It Works</p>
        <p className="text-sm mb-2 hover:text-[#F4CE14] cursor-pointer transition-colors">Contact</p>
      </div>

      {/* Third Column */}
      <div className="flex-1 min-w-[200px]">
        <h2 className="text-lg font-bold text-[#F4CE14] leading-10 mb-2">Ready to Get Started</h2>
        <p className="text-sm mb-4">
          Join thousands of satisfied customers who found their perfect home service providers.
        </p>
        {!isAuthenticated && (
          <button 
            onClick={handleSignUpClick}
            className="bg-[#F4CE14] text-black px-5 py-2.5 rounded font-light text-sm hover:bg-[#e0b813] transition-colors"
          >
            Sign Up
          </button>
        )}
      </div>
    </div>
  );
}

export default Footer;