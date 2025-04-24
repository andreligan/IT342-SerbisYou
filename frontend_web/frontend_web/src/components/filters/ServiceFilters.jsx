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
    <div className={className}>
      <div className="md:hidden mb-4">
        <button 
          onClick={toggleFilterVisibility}
          className="w-full flex items-center justify-between bg-white p-4 rounded-lg shadow-md transition-colors duration-200 hover:bg-gray-50"
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

      <div className={`${!isFilterVisible && 'hidden md:block'} bg-white rounded-lg shadow-md transition-all duration-300 overflow-hidden`}>
        <div className="bg-gradient-to-r from-[#495E57] to-[#3e4f49] text-white p-4 rounded-t-lg">
          <h2 className="text-xl font-bold flex items-center gap-2">
            <svg xmlns="http://www.w3.org/2000/svg" className="h-6 w-6" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M3 4a1 1 0 011-1h16a1 1 0 011 1v2.586a1 1 0 01-.293.707l-6.414 6.414a1 1 0 00-.293.707V17l-4 4v-6.586a1 1 0 00-.293-.707L3.293 7.293A1 1 0 013 6.586V4z" />
            </svg>
            Filter Options
          </h2>
          <p className="text-sm text-gray-200 mt-1">Refine your search results</p>
        </div>
        
        <div className="p-5">
          {/* Category Filter */}
          {categories.length > 0 && (
            <div className="mb-6">
              <h3 className="font-semibold text-[#495E57] mb-3 border-b border-gray-200 pb-2 flex items-center">
                <svg xmlns="http://www.w3.org/2000/svg" className="h-4 w-4 mr-2" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M19 11H5m14 0a2 2 0 012 2v6a2 2 0 01-2 2H5a2 2 0 01-2-2v-6a2 2 0 012-2m14 0V9a2 2 0 00-2-2M5 11V9a2 2 0 012-2m0 0V5a2 2 0 012-2h6a2 2 0 012 2v2M7 7h10" />
                </svg>
                Categories
              </h3>
              <div className="space-y-2 max-h-48 overflow-auto pr-2 py-1 scrollbar-thin">
                {categories.map(option => (
                  <div key={option.value} className="flex items-center hover:bg-gray-50 rounded-md p-1 transition-colors duration-150">
                    <div className="relative flex items-center">
                      <input
                        type="checkbox"
                        id={`checkbox-${option.value}`}
                        checked={selectedCategories.includes(option.value)}
                        onChange={() => handleCategoryChange(option.value)}
                        className="w-4 h-4 rounded border-gray-300 text-[#F4CE14] focus:ring-[#F4CE14]/30 transition-all duration-200 cursor-pointer"
                      />
                      <span className={`absolute w-full h-full pointer-events-none ${selectedCategories.includes(option.value) ? 'scale-100' : 'scale-0'} transition-transform duration-200`}>
                        <span className="absolute inset-0 bg-[#F4CE14]/10 rounded-sm"></span>
                      </span>
                    </div>
                    <label 
                      htmlFor={`checkbox-${option.value}`} 
                      className={`ml-3 text-sm ${selectedCategories.includes(option.value) ? 'text-[#495E57] font-medium' : 'text-gray-600'} cursor-pointer transition-colors duration-200`}
                    >
                      {option.label}
                    </label>
                  </div>
                ))}
              </div>
            </div>
          )}
          
          {/* Price Range Filter */}
          <div className="mb-6">
            <h3 className="font-semibold text-[#495E57] mb-3 border-b border-gray-200 pb-2 flex items-center">
              <svg xmlns="http://www.w3.org/2000/svg" className="h-4 w-4 mr-2" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 8c-1.657 0-3 .895-3 2s1.343 2 3 2 3 .895 3 2-1.343 2-3 2m0-8c1.11 0 2.08.402 2.599 1M12 8V7m0 1v8m0 0v1m0-1c-1.11 0-2.08-.402-2.599-1M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
              </svg>
              Price Range
            </h3>
            <div className="flex items-center justify-between mb-2">
              <div className="text-sm font-medium text-gray-700 bg-gray-50 px-3 py-1.5 rounded-md shadow-sm">
                ₱{priceRange[0]}
              </div>
              <div className="text-xs text-gray-500">to</div>
              <div className="text-sm font-medium text-gray-700 bg-gray-50 px-3 py-1.5 rounded-md shadow-sm">
                ₱{priceRange[1]}
              </div>
            </div>
            
            {/* Improved price range slider */}
            <div className="relative h-2 mb-6">
              {/* Track and colored range */}
              <div className="absolute inset-0 rounded-lg bg-gray-200"></div>
              <div 
                className="absolute h-full bg-gradient-to-r from-[#F4CE14]/80 to-[#F4CE14] rounded-lg"
                style={{ 
                  left: `${((priceRange[0] - 0) / (10000 - 0)) * 100}%`, 
                  right: `${100 - ((priceRange[1] - 0) / (10000 - 0)) * 100}%` 
                }}
              ></div>
              
              {/* Thumbs */}
              <div 
                className="absolute w-5 h-5 bg-white border-2 border-[#F4CE14] rounded-full shadow-md -mt-1.5 transform -translate-x-1/2 hover:scale-110 transition-transform duration-150"
                style={{ left: `${((priceRange[0] - 0) / (10000 - 0)) * 100}%` }}
              ></div>
              <div 
                className="absolute w-5 h-5 bg-white border-2 border-[#F4CE14] rounded-full shadow-md -mt-1.5 transform -translate-x-1/2 hover:scale-110 transition-transform duration-150"
                style={{ left: `${((priceRange[1] - 0) / (10000 - 0)) * 100}%` }}
              ></div>
              
              {/* Sliders - transparent but functional */}
              <input
                type="range"
                min={0}
                max={10000}
                value={priceRange[0]}
                onChange={(e) => {
                  const val = Number(e.target.value);
                  if (val < priceRange[1]) {
                    handlePriceRangeChange([val, priceRange[1]]);
                  }
                }}
                className="absolute w-full h-2 appearance-none z-20 cursor-pointer opacity-0"
              />
              <input
                type="range"
                min={0}
                max={10000}
                value={priceRange[1]}
                onChange={(e) => {
                  const val = Number(e.target.value);
                  if (val > priceRange[0]) {
                    handlePriceRangeChange([priceRange[0], val]);
                  }
                }}
                className="absolute w-full h-2 appearance-none z-20 cursor-pointer opacity-0"
              />
            </div>
          </div>
          
          {/* Rating Filter */}
          <div className="mb-6">
            <h3 className="font-semibold text-[#495E57] mb-3 border-b border-gray-200 pb-2 flex items-center">
              <svg xmlns="http://www.w3.org/2000/svg" className="h-4 w-4 mr-2" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M11.049 2.927c.3-.921 1.603-.921 1.902 0l1.519 4.674a1 1 0 00.95.69h4.915c.969 0 1.371 1.24.588 1.81l-3.976 2.888a1 1 0 00-.363 1.118l1.518 4.674c.3.922-.755 1.688-1.538 1.118l-3.976-2.888a1 1 0 00-1.176 0l-3.976 2.888c-.783.57-1.838-.197-1.538-1.118l1.518-4.674a1 1 0 00-.363-1.118l-3.976-2.888c-.784-.57-.38-1.81.588-1.81h4.914a1 1 0 00.951-.69l1.519-4.674z" />
              </svg>
              Minimum Rating
            </h3>
            <div className="relative pt-1">
              <div className="flex justify-between mb-2">
                <div className="flex items-center">
                  {[...Array(5)].map((_, i) => (
                    <span 
                      key={i} 
                      onClick={() => handleRatingChange(i + 1)}
                      className={`text-2xl cursor-pointer ${i < Math.floor(ratingFilter) ? "text-[#F4CE14]" : "text-gray-300"} hover:text-[#F4CE14] transition-colors duration-150`}
                    >
                      ★
                    </span>
                  ))}
                </div>
                <span className="text-sm text-gray-600 bg-gray-50 px-2 py-1 rounded-md">
                  {ratingFilter.toFixed(1)}
                </span>
              </div>
              <div className="relative h-2 bg-gray-200 rounded-lg">
                <div 
                  className="absolute top-0 h-2 rounded-lg bg-gradient-to-r from-[#F4CE14]/70 to-[#F4CE14]" 
                  style={{width: `${(ratingFilter / 5) * 100}%`}}
                ></div>
              </div>
              <input
                type="range"
                min={0}
                max={5}
                step={0.5}
                value={ratingFilter}
                onChange={(e) => handleRatingChange(Number(e.target.value))}
                className="absolute top-0 w-full h-2 opacity-0 cursor-pointer"
              />
            </div>
          </div>
          
          {/* Experience Filter with updated UI */}
          <div className="mb-6">
            <h3 className="font-semibold text-[#495E57] mb-3 border-b border-gray-200 pb-2 flex items-center">
              <svg xmlns="http://www.w3.org/2000/svg" className="h-4 w-4 mr-2" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M21 13.255A23.931 23.931 0 0112 15c-3.183 0-6.22-.62-9-1.745M16 6V4a2 2 0 00-2-2h-4a2 2 0 00-2 2v2m4 6h.01M5 20h14a2 2 0 002-2V8a2 2 0 00-2-2H5a2 2 0 00-2 2v10a2 2 0 002 2z" />
              </svg>
              Experience
            </h3>
            <div className="relative pt-1">
              <div className="flex justify-between mb-2">
                <span className="text-sm text-gray-600">Minimum Years</span>
                <span className="text-sm font-medium text-gray-700 bg-gray-50 px-2 py-1 rounded-md">
                  {experienceYears} {experienceYears === 1 ? 'year' : 'years'}
                </span>
              </div>
              <div className="relative h-2 bg-gray-200 rounded-lg">
                <div 
                  className="absolute top-0 h-2 rounded-lg bg-gradient-to-r from-[#F4CE14]/70 to-[#F4CE14]" 
                  style={{width: `${(experienceYears / 20) * 100}%`}}
                ></div>
              </div>
              <input
                type="range"
                min={0}
                max={20}
                step={1}
                value={experienceYears}
                onChange={(e) => handleExperienceChange(Number(e.target.value))}
                className="absolute top-0 w-full h-2 opacity-0 cursor-pointer"
              />
            </div>
          </div>
          
          {/* Verification Filter with visual enhancements */}
          <div className="mb-6">
            <div className="flex items-center justify-between p-3 border border-gray-200 rounded-lg hover:bg-gray-50 transition-colors duration-150">
              <div className="flex items-center">
                <svg xmlns="http://www.w3.org/2000/svg" className="h-5 w-5 text-[#495E57] mr-3" viewBox="0 0 20 20" fill="currentColor">
                  <path fillRule="evenodd" d="M2.166 4.999A11.954 11.954 0 0010 1.944 11.954 11.954 0 0017.834 5c.11.65.166 1.32.166 2.001 0 5.225-3.34 9.67-8 11.317C5.34 16.67 2 12.225 2 7c0-.682.057-1.35.166-2.001zm11.541 3.708a1 1 0 00-1.414-1.414L9 10.586 7.707 9.293a1 1 0 00-1.414 1.414l2 2a1 1 0 001.414 0l4-4z" clipRule="evenodd" />
                </svg>
                <div>
                  <label htmlFor="verified-only" className="text-sm font-medium text-gray-700 cursor-pointer">
                    Verified Providers Only
                  </label>
                  <p className="text-xs text-gray-500">Only show providers verified by SerbisYo</p>
                </div>
              </div>
              <div className="relative inline-block w-12 mr-2 align-middle select-none">
                <input 
                  type="checkbox" 
                  id="verified-only" 
                  checked={verifiedOnly}
                  onChange={handleVerifiedChange}
                  className="toggle-checkbox absolute block w-6 h-6 rounded-full bg-white border-4 appearance-none cursor-pointer transition-all duration-300 ease-in-out"
                  style={{
                    left: verifiedOnly ? '6px' : '0px',
                    border: verifiedOnly ? '4px solid #F4CE14' : '4px solid #ccc',
                    transform: verifiedOnly ? 'translateX(100%)' : 'translateX(0)',
                  }}
                />
                <label 
                  htmlFor="verified-only" 
                  className={`toggle-label block overflow-hidden h-6 rounded-full cursor-pointer transition-colors duration-300 ease-in-out ${verifiedOnly ? 'bg-[#F4CE14]/30' : 'bg-gray-300'}`}
                ></label>
              </div>
            </div>
          </div>
          
          {/* Sort Options with improved UI */}
          <div className="mb-6">
            <h3 className="font-semibold text-[#495E57] mb-3 border-b border-gray-200 pb-2 flex items-center">
              <svg xmlns="http://www.w3.org/2000/svg" className="h-4 w-4 mr-2" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M3 4h13M3 8h9m-9 4h9m5-4v12m0 0l-4-4m4 4l4-4" />
              </svg>
              Sort By
            </h3>
            <div className="space-y-1 bg-gray-50 rounded-lg p-2">
              {[
                { value: 'recommended', label: 'Recommended', icon: '👍' },
                { value: 'price_low', label: 'Price: Low to High', icon: '💲' },
                { value: 'price_high', label: 'Price: High to Low', icon: '💰' },
                { value: 'rating', label: 'Highest Rating', icon: '⭐' },
                { value: 'experience', label: 'Most Experienced', icon: '🏆' }
              ].map(option => (
                <div 
                  key={option.value} 
                  onClick={() => handleSortChange(option.value)}
                  className={`flex items-center p-2 rounded-md cursor-pointer transition-colors duration-150 ${sortOption === option.value ? 'bg-[#F4CE14]/20 text-[#495E57]' : 'hover:bg-gray-100'}`}
                >
                  <span className="mr-2">{option.icon}</span>
                  <span className={`text-sm ${sortOption === option.value ? 'font-medium' : ''}`}>
                    {option.label}
                  </span>
                  {sortOption === option.value && (
                    <svg xmlns="http://www.w3.org/2000/svg" className="h-4 w-4 ml-auto text-[#F4CE14]" viewBox="0 0 20 20" fill="currentColor">
                      <path fillRule="evenodd" d="M16.707 5.293a1 1 0 010 1.414l-8 8a1 1 0 01-1.414 0l-4-4a1 1 0 011.414-1.414L8 12.586l7.293-7.293a1 1 0 011.414 0z" clipRule="evenodd" />
                    </svg>
                  )}
                </div>
              ))}
            </div>
          </div>
          
          {/* Action buttons with improved styling */}
          <div className="flex gap-3 mt-8">
            <button
              className="flex-1 bg-gray-100 hover:bg-gray-200 text-gray-700 font-medium py-2.5 px-4 rounded-lg transition-colors duration-200 flex items-center justify-center"
              onClick={() => {
                setSelectedCategories([]);
                setPriceRange([0, 10000]);
                setRatingFilter(0);
                setVerifiedOnly(false);
                setSortOption('recommended');
                setExperienceYears(0);
              }}
            >
              <svg xmlns="http://www.w3.org/2000/svg" className="h-4 w-4 mr-2" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M4 4v5h.582m15.356 2A8.001 8.001 0 004.582 9m0 0H9m11 11v-5h-.581m0 0a8.003 8.003 0 01-15.357-2m15.357 2H15" />
              </svg>
              Reset All
            </button>
            <button
              className="flex-1 bg-[#F4CE14] hover:bg-[#e5c113] text-[#495E57] font-bold py-2.5 px-4 rounded-lg transition-colors duration-200 shadow-sm flex items-center justify-center"
              onClick={() => {
                // Apply current filters
                const filters = {
                  categories: selectedCategories,
                  priceRange,
                  rating: ratingFilter,
                  verifiedOnly,
                  sortBy: sortOption,
                  experience: experienceYears
                };
                onFilterChange(filters);
              }}
            >
              <svg xmlns="http://www.w3.org/2000/svg" className="h-4 w-4 mr-2" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M5 13l4 4L19 7" />
              </svg>
              Apply Filters
            </button>
          </div>
        </div>
      </div>
    </div>
  );
};

export default React.memo(ServiceFilters);
