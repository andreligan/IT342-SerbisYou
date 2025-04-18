import React, { useState } from "react";
import Footer from "./Footer";

// Import images - adjust path as needed for your project structure
import plumbing from "../assets/plumbing.jpg";
import electrical from "../assets/electrical.jpg";
import cleaning from "../assets/cleaning.jpg";
import pestControl from "../assets/pest control.jpg";
import applianceRepair from "../assets/appliance repair.jpg";
import homePainting from "../assets/home painting.jpg";
import carpentry from "../assets/carpentry.jpg";
import movingPacking from "../assets/home moving.jpg";
import handyman from "../assets/handyman.jpeg";
import lawnCare from "../assets/lawn care.jpg";

// Service categories data
const categories = [
  { name: "Plumbing Services", image: plumbing },
  { name: "Electrical Services", image: electrical },
  { name: "Cleaning Services", image: cleaning },
  { name: "Pest Control", image: pestControl },
  { name: "Appliance Repair", image: applianceRepair },
  { name: "Home Painting", image: homePainting },
  { name: "Carpentry Services", image: carpentry },
  { name: "Moving & Packing", image: movingPacking },
  { name: "Handyman Services", image: handyman },
  { name: "Lawn Care & Gardening", image: lawnCare },
];

function LandingPage() {
  const [page, setPage] = useState(1);

  // Pagination logic
  const itemsPerPage = 5;
  const startIndex = (page - 1) * itemsPerPage;
  const endIndex = startIndex + itemsPerPage;
  const currentCategories = categories.slice(startIndex, endIndex);
  const totalPages = Math.ceil(categories.length / itemsPerPage);

  const handlePageChange = (newPage) => {
    setPage(newPage);
  };

  return (
    <div className="min-h-screen bg-gray-50">
      {/* Hero Section */}
      <div className="bg-[#495E57] text-white">
        <div className="container mx-auto px-4 py-24 flex flex-col items-center justify-center text-center">
          <h1 className="text-4xl md:text-5xl font-bold mb-6 leading-tight max-w-4xl">
            Find Trusted Professionals for Any Home Service!
          </h1>
          <p className="text-lg opacity-90 mb-10 max-w-2xl">
            Book services from verified professionals with ease and security.
          </p>
          <div className="flex flex-col sm:flex-row gap-4">
            <button className="bg-[#F4CE14] hover:bg-yellow-500 text-black font-medium px-8 py-3 rounded-lg transition duration-300 transform hover:scale-105">
              Browse Services
            </button>
            <button className="bg-white hover:bg-gray-100 text-[#495E57] font-medium px-8 py-3 rounded-lg transition duration-300 transform hover:scale-105">
              Be a Provider
            </button>
          </div>
        </div>
      </div>

      {/* Featured Home Categories Section */}
      <div className="bg-white py-16">
        <div className="container mx-auto px-4">
          <h2 className="text-3xl font-bold text-[#495E57] text-center mb-12">
            Featured Home Categories
          </h2>
          
          <div className="flex items-center justify-center mb-8">
            {/* Previous Button */}
            <button 
              onClick={() => handlePageChange(page - 1)}
              disabled={page === 1}
              className={`w-16 h-16 flex justify-center items-center rounded-full mr-4
                ${page === 1 
                  ? 'bg-gray-200 text-gray-400 cursor-not-allowed' 
                  : 'bg-[#495E57] text-white hover:bg-opacity-90'}`}
            >
              <svg xmlns="http://www.w3.org/2000/svg" className="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M15 19l-7-7 7-7" />
              </svg>
            </button>
            
            {/* Categories Grid */}
            <div className="grid grid-cols-2 md:grid-cols-5 flex-grow">
              {currentCategories.map((category, index) => (
                <div key={index} className="flex flex-col items-center group cursor-pointer">
                  <div className="w-40 h-40 rounded-full overflow-hidden mb-3 border-2 border-gray-100 group-hover:border-[#F4CE14] transition-all duration-300">
                    <div 
                      className="w-full h-full bg-cover bg-center transform group-hover:scale-110 transition-all duration-500"
                      style={{ backgroundImage: `url(${category.image})` }}
                    ></div>
                  </div>
                  <span className="font-medium text-sm text-center text-gray-700 group-hover:text-[#495E57]">
                    {category.name}
                  </span>
                </div>
              ))}
            </div>
            
            {/* Next Button */}
            <button 
              onClick={() => handlePageChange(page + 1)}
              disabled={page === totalPages}
              className={`w-16 h-16 flex justify-center items-center rounded-full ml-4
                ${page === totalPages 
                  ? 'bg-gray-200 text-gray-400 cursor-not-allowed' 
                  : 'bg-[#495E57] text-white hover:bg-opacity-90'}`}
            >
              <svg xmlns="http://www.w3.org/2000/svg" className="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 5l7 7-7 7" />
              </svg>
            </button>
          </div>
          
          {/* Pagination Indicators */}
          <div className="flex justify-center gap-2 mt-8">
            {[...Array(totalPages)].map((_, i) => (
              <button
                key={i}
                onClick={() => handlePageChange(i + 1)}
                className={`w-2 h-2 rounded-full transition-all duration-300 ${
                  page === i + 1 ? "w-6 bg-[#495E57]" : "bg-gray-300 hover:bg-[#495E57] hover:bg-opacity-50"
                }`}
                aria-label={`Page ${i + 1}`}
              ></button>
            ))}
          </div>
        </div>
      </div>

      {/* How It Works Section */}
      <div className="bg-gray-100 py-16">
        <div className="container mx-auto px-4">
          <h2 className="text-3xl font-bold text-[#495E57] text-center mb-16">
            How It Works
          </h2>
          
          <div className="flex flex-col md:flex-row justify-center items-start gap-8 md:gap-16">
            {/* Step 1 */}
            <div className="flex flex-col items-center max-w-xs">
              <div className="relative mb-6">
                <div className="w-24 h-24 bg-[#495E57] rounded-full flex items-center justify-center text-2xl font-bold text-[#F4CE14]">
                  1
                </div>
                <div className="hidden md:block absolute top-1/2 -right-40 h-0.5 w-15 bg-[#495E57] bg-opacity-50"></div>
              </div>
              <h3 className="text-xl font-semibold text-[#495E57] mb-3">Search for a Service</h3>
              <p className="text-center text-gray-600">
                Browse through the different home service categories or search for specific services.
              </p>
            </div>
            
            {/* Step 2 */}
            <div className="flex flex-col items-center max-w-xs">
              <div className="relative mb-6">
                <div className="w-24 h-24 bg-[#495E57] rounded-full flex items-center justify-center text-2xl font-bold text-[#F4CE14]">
                  2
                </div>
                <div className="hidden md:block absolute top-1/2 -right-40 h-0.5 w-15 bg-[#495E57] bg-opacity-50"></div>
              </div>
              <h3 className="text-xl font-semibold text-[#495E57] mb-3">Book an Appointment</h3>
              <p className="text-center text-gray-600">
                Choose your preferred time and date.
              </p>
            </div>
            
            {/* Step 3 */}
            <div className="flex flex-col items-center max-w-xs">
              <div className="mb-6">
                <div className="w-24 h-24 bg-[#495E57] rounded-full flex items-center justify-center text-2xl font-bold text-[#F4CE14]">
                  3
                </div>
              </div>
              <h3 className="text-xl font-semibold text-[#495E57] mb-3">Pay Securely & Review</h3>
              <p className="text-center text-gray-600">
                Complete your booking and share your experience!
              </p>
            </div>
          </div>
        </div>
      </div>

      {/* Featured Service Providers Section */}
      <div className="bg-white py-16">
        <div className="container mx-auto px-4">
          <h2 className="text-3xl font-bold text-[#495E57] text-center mb-12">
            Featured Service Providers
          </h2>
          
          <div className="grid grid-cols-1 md:grid-cols-3 gap-8">
            {/* Provider 1 */}
            <div className="bg-white rounded-lg shadow-lg hover:shadow-xl transition-shadow duration-300 overflow-hidden transform hover:-translate-y-1 transition-transform duration-300">
              <div className="p-6 flex flex-col items-center">
                <div className="w-24 h-24 rounded-full bg-gray-200 mb-4 overflow-hidden">
                  <img 
                    src="/api/placeholder/96/96" 
                    className="w-full h-full object-cover"
                  />
                </div>
                <h3 className="text-xl font-semibold mb-2">John Doe</h3>
                <div className="flex text-[#F4CE14] mb-3">
                  {"★★★★★"}
                </div>
                <p className="text-gray-600 font-medium">Licensed Plumber</p>
                <button className="mt-4 px-4 py-2 bg-gray-100 text-[#495E57] rounded-md hover:bg-gray-200 transition-colors duration-300 font-medium">
                  View Profile
                </button>
              </div>
            </div>
            
            {/* Provider 2 */}
            <div className="bg-white rounded-lg shadow-lg hover:shadow-xl transition-shadow duration-300 overflow-hidden transform hover:-translate-y-1 transition-transform duration-300">
              <div className="p-6 flex flex-col items-center">
                <div className="w-24 h-24 rounded-full bg-gray-200 mb-4 overflow-hidden">
                  <img 
                    src="/api/placeholder/96/96" 
                    className="w-full h-full object-cover"
                  />
                </div>
                <h3 className="text-xl font-semibold mb-2">Jane Smith</h3>
                <div className="flex text-[#F4CE14] mb-3">
                  {"★★★★☆"}
                </div>
                <p className="text-gray-600 font-medium">Certified Electrician</p>
                <button className="mt-4 px-4 py-2 bg-gray-100 text-[#495E57] rounded-md hover:bg-gray-200 transition-colors duration-300 font-medium">
                  View Profile
                </button>
              </div>
            </div>
            
            {/* Provider 3 */}
            <div className="bg-white rounded-lg shadow-lg hover:shadow-xl transition-shadow duration-300 overflow-hidden transform hover:-translate-y-1 transition-transform duration-300">
              <div className="p-6 flex flex-col items-center">
                <div className="w-24 h-24 rounded-full bg-gray-200 mb-4 overflow-hidden">
                  <img 
                    src="/api/placeholder/96/96" 
                    className="w-full h-full object-cover"
                  />
                </div>
                <h3 className="text-xl font-semibold mb-2">Michael Brown</h3>
                <div className="flex text-[#F4CE14] mb-3">
                  {"★★★★★"}
                </div>
                <p className="text-gray-600 font-medium">Professional Cleaner</p>
                <button className="mt-4 px-4 py-2 bg-gray-100 text-[#495E57] rounded-md hover:bg-gray-200 transition-colors duration-300 font-medium">
                  View Profile
                </button>
              </div>
            </div>
          </div>
          
          <div className="text-center mt-12">
            <button className="px-6 py-3 bg-[#495E57] text-white rounded-lg hover:bg-opacity-90 transition-colors duration-300 font-semibold">
              View All Service Providers
            </button>
          </div>
        </div>
      </div>

      {/* Footer Section */}
      <Footer />
    </div>
  );
}

export default LandingPage;