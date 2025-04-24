import React, { useCallback, useState, useEffect } from 'react';

const ServiceFilters = ({ services, onFilterChange, className, colorScheme = {} }) => {
  // Filter state
  const [categories, setCategories] = useState([]);
  const [selectedCategories, setSelectedCategories] = useState([]);
  const [priceRange, setPriceRange] = useState([0, 10000]);
  const [ratingFilter, setRatingFilter] = useState(0);
  const [verifiedOnly, setVerifiedOnly] = useState(false);
  const [sortOption, setSortOption] = useState('recommended');
  const [experienceYears, setExperienceYears] = useState(0);
  const [isFilterVisible, setIsFilterVisible] = useState(true);

  // Add accordion state
  const [expandedSections, setExpandedSections] = useState({
    categories: true,
    price: true,
    rating: true, 
    experience: true,
    verified: true,
    sortBy: true
  });

  // Toggle accordion section
  const toggleSection = (section) => {
    setExpandedSections(prev => ({
      ...prev,
      [section]: !prev[section]
    }));
  };

  // Extract unique categories from services
  useEffect(() => {
    if (services && services.length > 0) {
      const uniqueCategories = [...new Set(services.map(service => service.categoryName))]
        .filter(Boolean)
        .map(category => ({
          value: category,
          label: category
        }));
      
      setCategories(uniqueCategories);
      
      // Find min and max price for price range initialization
      const prices = services.map(service => parseFloat(service.price) || 0);
      const minPrice = Math.floor(Math.min(...prices));
      const maxPrice = Math.ceil(Math.max(...prices));
      
      setPriceRange([minPrice, maxPrice > minPrice ? maxPrice : minPrice + 10000]);
    }
  }, [services]);

  // Handle category filter changes
  const handleCategoryChange = useCallback((category) => {
    setSelectedCategories(prev => {
      const newCategories = prev.includes(category)
        ? prev.filter(c => c !== category)
        : [...prev, category];
      
      return newCategories;
    });
  }, []);

  // Handle price range changes
  const handlePriceRangeChange = useCallback((newRange) => {
    setPriceRange(newRange);
  }, []);

  // Handle rating filter changes
  const handleRatingChange = useCallback((rating) => {
    setRatingFilter(rating);
  }, []);

  // Handle verified filter changes
  const handleVerifiedChange = useCallback(() => {
    setVerifiedOnly(prev => !prev);
  }, []);

  // Handle sort option changes
  const handleSortChange = useCallback((option) => {
    setSortOption(option);
  }, []);

  // Handle experience filter changes
  const handleExperienceChange = useCallback((years) => {
    setExperienceYears(years);
  }, []);

  // Toggle filter visibility on mobile
  const toggleFilterVisibility = () => {
    setIsFilterVisible(prev => !prev);
  };

  // Apply filters and notify parent component
  useEffect(() => {
    const filters = {
      categories: selectedCategories,
      priceRange,
      rating: ratingFilter,
      verifiedOnly,
      sortBy: sortOption,
      experience: experienceYears
    };
    
    onFilterChange(filters);
  }, [selectedCategories, priceRange, ratingFilter, verifiedOnly, sortOption, experienceYears, onFilterChange]);

  // Use provided color scheme or default colors
  const colors = {
    primary: colorScheme.primary || '#495E57',
    accent: colorScheme.accent || '#F4CE14',
    textDark: colorScheme.textDark || '#333333',
    textLight: colorScheme.textLight || '#666666',
    bgLight: 'white'
  };

  return (
    <div className={`${className} sticky top-0`}>
      <div className="md:hidden mb-4">
        <button 
          onClick={toggleFilterVisibility}
          className="w-full flex items-center justify-between bg-white p-3 rounded-lg shadow-md transition-colors duration-200 hover:bg-gray-50"
        >
          <span className="font-medium text-[#495E57] flex items-center">
            <svg xmlns="http://www.w3.org/2000/svg" className="h-5 w-5 mr-2" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 6V4m0 2a2 2 0 100 4m0-4a2 2 0 110 4m-6 8a2 2 0 100-4m0 4a2 2 0 110-4m0 4v2m0-6V4m6 6v10m6-2a2 2 0 100-4m0 4a2 2 0 110-4m0 4v2m0-6V4" />
            </svg>
            Filter Services
          </span>
          <svg className={`h-5 w-5 transition-transform duration-300 ${isFilterVisible ? 'transform rotate-180' : ''}`} 
               fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M19 9l-7 7-7-7" />
          </svg>
        </button>
      </div>

      <div className={`${!isFilterVisible && 'hidden md:block'} bg-white rounded-lg shadow-md transition-all duration-300 overflow-hidden sticky top-4`}>
        <div className="bg-gradient-to-r from-[#495E57] to-[#3e4f49] text-white p-3 rounded-t-lg">
          <h2 className="font-bold flex items-center gap-2 text-lg">
            <svg xmlns="http://www.w3.org/2000/svg" className="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M3 4a1 1 0 011-1h16a1 1 0 011 1v2.586a1 1 0 01-.293.707l-6.414 6.414a1 1 0 00-.293.707V17l-4 4v-6.586a1 1 0 00-.293-.707L3.293 7.293A1 1 0 013 6.586V4z" />
            </svg>
            Filters
          </h2>
        </div>
        
        <div className="p-3">
          {/* Remove fixed height and make it content-adaptive */}
          <div className="max-h-[calc(100vh-130px)] overflow-y-auto pr-2 filter-scrollbar">
            {/* Category Filter - Accordion Style */}
            {categories.length > 0 && (
              <div className="mb-3">
                <button 
                  className="w-full flex items-center justify-between font-semibold text-[#495E57] mb-1 border-b border-gray-200 pb-1 text-sm"
                  onClick={() => toggleSection('categories')}
                >
                  <div className="flex items-center">
                    <svg xmlns="http://www.w3.org/2000/svg" className="h-3.5 w-3.5 mr-1.5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                      <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M19 11H5m14 0a2 2 0 012 2v6a2 2 0 01-2 2H5a2 2 0 01-2-2v-6a2 2 0 012-2m14 0V9a2 2 0 00-2-2M5 11V9a2 2 0 012-2m0 0V5a2 2 0 012-2h6a2 2 0 012 2v2M7 7h10" />
                    </svg>
                    Categories
                  </div>
                  <svg 
                    xmlns="http://www.w3.org/2000/svg" 
                    className={`h-3.5 w-3.5 transition-transform duration-300 ${expandedSections.categories ? 'transform rotate-180' : ''}`}
                    fill="none" 
                    viewBox="0 0 24 24" 
                    stroke="currentColor"
                  >
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M19 9l-7 7-7-7" />
                  </svg>
                </button>
                
                <div 
                  className={`transition-all duration-300 overflow-hidden ${
                    expandedSections.categories ? 'max-h-28 opacity-100' : 'max-h-0 opacity-0'
                  }`}
                >
                  <div className="max-h-28 overflow-auto grid grid-cols-1 gap-0.5 py-1 scrollbar-thin mt-1.5">
                    {categories.map(option => (
                      <div key={option.value} className="flex items-center hover:bg-gray-50 rounded-md py-0.5 transition-colors duration-150">
                        <input
                          type="checkbox"
                          id={`checkbox-${option.value}`}
                          checked={selectedCategories.includes(option.value)}
                          onChange={() => handleCategoryChange(option.value)}
                          className="w-3.5 h-3.5 rounded border-gray-300 text-[#F4CE14] focus:ring-0"
                        />
                        <label 
                          htmlFor={`checkbox-${option.value}`} 
                          className={`ml-2 text-xs ${selectedCategories.includes(option.value) ? 'text-[#495E57] font-medium' : 'text-gray-600'} cursor-pointer truncate`}
                        >
                          {option.label}
                        </label>
                      </div>
                    ))}
                  </div>
                </div>
              </div>
            )}
            
            {/* Price Range and Experience - Combined in one row */}
            <div className="mb-3">
              <div className="flex gap-3">
                {/* Price Range - Left column */}
                <div className="flex-1">
                  <h3 className="font-semibold text-[#495E57] mb-1 border-b border-gray-200 pb-1 flex items-center text-sm">
                    <svg xmlns="http://www.w3.org/2000/svg" className="h-3.5 w-3.5 mr-1.5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                      <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 8c-1.657 0-3 .895-3 2s1.343 2 3 2 3 .895 3 2-1.343 2-3 2m0-8c1.11 0 2.08.402 2.599 1M12 8V7m0 1v8m0 0v1m0-1c-1.11 0-2.08-.402-2.599-1M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
                    </svg>
                    Price
                  </h3>
                  <div className="flex items-center gap-1 mt-1.5">
                    <div className="flex items-center">
                      <span className="text-xs font-medium text-gray-600 mr-1">₱</span>
                      <input 
                        type="number" 
                        min="0"
                        value={priceRange[0]}
                        onChange={(e) => {
                          const val = Number(e.target.value);
                          if (val >= 0 && val <= priceRange[1]) {
                            handlePriceRangeChange([val, priceRange[1]]);
                          }
                        }}
                        className="w-full px-2 py-1 text-xs border border-gray-300 rounded focus:outline-none focus:ring-1 focus:ring-[#F4CE14] no-spinner"
                      />
                    </div>
                    <span className="text-xs text-gray-400">-</span>
                    <div className="flex items-center">
                      <span className="text-xs font-medium text-gray-600 mr-1">₱</span>
                      <input 
                        type="number"
                        min="0" 
                        value={priceRange[1]}
                        onChange={(e) => {
                          const val = Number(e.target.value);
                          if (val >= priceRange[0]) {
                            handlePriceRangeChange([priceRange[0], val]);
                          }
                        }}
                        className="w-full px-2 py-1 text-xs border border-gray-300 rounded focus:outline-none focus:ring-1 focus:ring-[#F4CE14] no-spinner"
                      />
                    </div>
                  </div>
                </div>
                
                {/* Experience - Right column */}
                <div className="flex-1">
                  <h3 className="font-semibold text-[#495E57] mb-1 border-b border-gray-200 pb-1 flex items-center text-sm">
                    <svg xmlns="http://www.w3.org/2000/svg" className="h-3.5 w-3.5 mr-1.5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                      <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M21 13.255A23.931 23.931 0 0112 15c-3.183 0-6.22-.62-9-1.745M16 6V4a2 2 0 00-2-2h-4a2 2 0 00-2 2v2m4 6h.01M5 20h14a2 2 0 002-2V8a2 2 0 00-2-2H5a2 2 0 00-2 2v10a2 2 0 002 2z" />
                    </svg>
                    Experience
                  </h3>
                  <div className="flex items-center gap-2 mt-1.5">
                    <input 
                      type="number" 
                      min="0"
                      max="50"
                      value={experienceYears}
                      onChange={(e) => handleExperienceChange(Number(e.target.value))}
                      className="w-full px-2 py-1 text-xs border border-gray-300 rounded focus:outline-none focus:ring-1 focus:ring-[#F4CE14] no-spinner"
                    />
                    <span className="text-xs font-medium whitespace-nowrap">years+</span>
                  </div>
                </div>
              </div>
            </div>
            
            {/* Rating and Verification Filter - Combined in one row */}
            <div className="mb-3">
              <div className="flex gap-3">
                {/* Rating Filter - Left column */}
                <div className="flex-1">
                  <h3 className="font-semibold text-[#495E57] mb-1 border-b border-gray-200 pb-1 flex items-center text-sm">
                    <svg xmlns="http://www.w3.org/2000/svg" className="h-3.5 w-3.5 mr-1.5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                      <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M11.049 2.927c.3-.921 1.603-.921 1.902 0l1.519 4.674a1 1 0 00.95.69h4.915c.969 0 1.371 1.24.588 1.81l-3.976 2.888a1 1 0 00-.363 1.118l1.518 4.674c.3.922-.755 1.688-1.538 1.118l-3.976-2.888a1 1 0 00-1.176 0l-3.976-2.888c-.783.57-1.838-.197-1.538-1.118l1.518-4.674a1 1 0 00-.363-1.118l-3.976-2.888c-.784-.57-.38-1.81.588-1.81h4.914a1 1 0 00.951-.69l1.519-4.674z" />
                    </svg>
                    Rating
                  </h3>
                  <div className="flex justify-between items-center mt-1.5">
                    <div className="flex">
                      {[...Array(5)].map((_, i) => (
                        <span 
                          key={i} 
                          onClick={() => handleRatingChange(i + 1)}
                          className={`text-lg cursor-pointer ${i < Math.floor(ratingFilter) ? "text-[#F4CE14]" : "text-gray-300"} hover:text-[#F4CE14]`}
                        >
                          ★
                        </span>
                      ))}
                    </div>
                    <span className="text-xs text-gray-600 bg-gray-50 px-2 py-1 rounded">
                      {ratingFilter.toFixed(1)}
                    </span>
                  </div>
                </div>
                
                {/* Verification Filter - Right column */}
                <div className="flex-1">
                  <h3 className="font-semibold text-[#495E57] mb-1 border-b border-gray-200 pb-1 flex items-center text-sm">
                    <svg xmlns="http://www.w3.org/2000/svg" className="h-3.5 w-3.5 mr-1.5" viewBox="0 0 20 20" fill="currentColor">
                      <path fillRule="evenodd" d="M2.166 4.999A11.954 11.954 0 0010 1.944A11.954 11.954 0 0017.834 5c.11.65.166 1.32.166 2.001 0 5.225-3.34 9.67-8 11.317C5.34 16.67 2 12.225 2 7c0-.682.057-1.35.166-2.001zm11.541 3.708a1 1 0 00-1.414-1.414L9 10.586 7.707 9.293a1 1 0 00-1.414 1.414l2 2a1 1 0 001.414 0l4-4z" clipRule="evenodd" />
                    </svg>
                    Verified Only
                  </h3>
                  <div className="flex items-center h-full mt-2">
                    <div className="relative inline-block w-10 mr-2 align-middle select-none">
                      <input 
                        type="checkbox" 
                        id="verified-only" 
                        checked={verifiedOnly}
                        onChange={handleVerifiedChange}
                        className="toggle-checkbox absolute block w-5 h-5 rounded-full bg-white appearance-none cursor-pointer"
                        style={{
                          transform: verifiedOnly ? 'translateX(100%)' : 'translateX(0)',
                          border: verifiedOnly ? '2px solid #F4CE14' : '2px solid #ccc',
                        }}
                      />
                      <label 
                        htmlFor="verified-only" 
                        className={`block h-5 rounded-full cursor-pointer ${verifiedOnly ? 'bg-[#F4CE14]/30' : 'bg-gray-300'}`}
                      ></label>
                    </div>
                    <span className="text-xs text-gray-600">
                      {verifiedOnly ? 'Yes' : 'No'}
                    </span>
                  </div>
                </div>
              </div>
            </div>
            
            {/* Sort Options - Accordion Style */}
            <div className="mb-3">
              <button 
                className="w-full flex items-center justify-between font-semibold text-[#495E57] mb-1 border-b border-gray-200 pb-1 text-sm"
                onClick={() => toggleSection('sortBy')}
              >
                <div className="flex items-center">
                  <svg xmlns="http://www.w3.org/2000/svg" className="h-3.5 w-3.5 mr-1.5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M3 4h13M3 8h9m-9 4h9m5-4v12m0 0l-4-4m4 4l4-4" />
                  </svg>
                  Sort By
                </div>
                <svg 
                  xmlns="http://www.w3.org/2000/svg" 
                  className={`h-3.5 w-3.5 transition-transform duration-300 ${expandedSections.sortBy ? 'transform rotate-180' : ''}`}
                  fill="none" 
                  viewBox="0 0 24 24" 
                  stroke="currentColor"
                >
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M19 9l-7 7-7-7" />
                </svg>
              </button>
              
              <div 
                className={`transition-all duration-300 overflow-hidden ${
                  expandedSections.sortBy ? 'max-h-40 opacity-100' : 'max-h-0 opacity-0'
                }`}
              >
                <div className="grid grid-cols-1 gap-0.5 bg-gray-50 rounded-lg p-1.5 mt-1.5">
                  {[
                    { value: 'recommended', label: 'Recommended' },
                    { value: 'price_low', label: 'Price: Low to High' },
                    { value: 'price_high', label: 'Price: High to Low' },
                    { value: 'rating', label: 'Highest Rating' },
                    { value: 'experience', label: 'Most Experienced' }
                  ].map(option => (
                    <div 
                      key={option.value} 
                      onClick={() => handleSortChange(option.value)}
                      className={`flex items-center py-1 px-2 rounded-md cursor-pointer transition-colors duration-150 ${sortOption === option.value ? 'bg-[#F4CE14]/20 text-[#495E57]' : 'hover:bg-gray-100'}`}
                    >
                      <span className={`text-xs ${sortOption === option.value ? 'font-medium' : ''}`}>
                        {option.label}
                      </span>
                      {sortOption === option.value && (
                        <svg xmlns="http://www.w3.org/2000/svg" className="h-3 w-3 ml-auto text-[#F4CE14]" viewBox="0 0 20 20" fill="currentColor">
                          <path fillRule="evenodd" d="M16.707 5.293a1 1 0 010 1.414l-8 8a1 1 0 01-1.414 0l-4-4a1 1 0 011.414-1.414L8 12.586l7.293-7.293a1 1 0 011.414 0z" clipRule="evenodd" />
                        </svg>
                      )}
                    </div>
                  ))}
                </div>
              </div>
            </div>
          </div>
          
          {/* Action buttons - more compact and fixed at bottom */}
          <div className="flex gap-2 pt-2 border-t border-gray-100 mt-2 sticky bottom-0 bg-white">
            <button
              className="flex-1 bg-gray-100 hover:bg-gray-200 text-gray-700 font-medium py-1.5 px-2 rounded-lg text-sm flex items-center justify-center"
              onClick={() => {
                setSelectedCategories([]);
                setPriceRange([0, 10000]);
                setRatingFilter(0);
                setVerifiedOnly(false);
                setSortOption('recommended');
                setExperienceYears(0);
              }}
            >
              <svg xmlns="http://www.w3.org/2000/svg" className="h-3.5 w-3.5 mr-1" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M4 4v5h.582m15.356 2A8.001 8.001 0 004.582 9m0 0H9m11 11v-5h-.581m0 0a8.003 8.003 0 01-15.357-2m15.357 2H15" />
              </svg>
              Reset
            </button>
            <button
              className="flex-1 bg-[#F4CE14] hover:bg-[#e5c113] text-[#495E57] font-medium py-1.5 px-2 rounded-lg text-sm shadow-sm flex items-center justify-center"
              onClick={() => {
                onFilterChange({
                  categories: selectedCategories,
                  priceRange,
                  rating: ratingFilter,
                  verifiedOnly,
                  sortBy: sortOption,
                  experience: experienceYears
                });
              }}
            >
              <svg xmlns="http://www.w3.org/2000/svg" className="h-3.5 w-3.5 mr-1" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M5 13l4 4L19 7" />
              </svg>
              Apply
            </button>
          </div>
        </div>
      </div>
    </div>
  );
};

export default React.memo(ServiceFilters);
