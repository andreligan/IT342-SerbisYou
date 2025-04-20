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
          className="w-full flex items-center justify-between bg-white p-3 rounded-md shadow"
          style={{ backgroundColor: colors.bgLight, color: colors.primary }}
        >
          <span className="font-medium">Filters</span>
          <svg className={`h-5 w-5 transition-transform ${isFilterVisible ? 'transform rotate-180' : ''}`} 
               fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M19 9l-7 7-7-7" />
          </svg>
        </button>
      </div>

      <div className={`${!isFilterVisible && 'hidden md:block'} bg-white rounded-lg shadow-md`}>
        <div className="bg-[#495E57] text-white p-4 rounded-t-lg">
          <h2 className="text-xl font-bold flex items-center gap-2">
            <i className="fas fa-sliders-h"></i> Filters
          </h2>
        </div>
        
        <div className="p-4">
          {/* Category Filter */}
          {categories.length > 0 && (
            <div className="mb-6">
              <h3 className="font-semibold text-[#495E57] mb-2 border-b border-gray-200 pb-1">Categories</h3>
              <div className="space-y-1">
                {categories.map(option => (
                  <div key={option.value} className="flex items-center">
                    <input
                      type="checkbox"
                      id={`checkbox-${option.value}`}
                      checked={selectedCategories.includes(option.value)}
                      onChange={() => handleCategoryChange(option.value)}
                      className="rounded border-gray-300 text-[#495E57] focus:ring-[#495E57]"
                    />
                    <label htmlFor={`checkbox-${option.value}`} className="ml-2 text-sm text-gray-600">
                      {option.label}
                    </label>
                  </div>
                ))}
              </div>
            </div>
          )}
          
          {/* Price Range Filter */}
          <div className="mb-6">
            <h3 className="font-semibold text-[#495E57] mb-2 border-b border-gray-200 pb-1">Price Range</h3>
            <div className="flex items-center justify-between mb-2">
              <span className="text-sm text-gray-600 bg-gray-50 px-2 py-1 rounded">₱{priceRange[0]}</span>
              <span className="text-sm text-gray-600 bg-gray-50 px-2 py-1 rounded">₱{priceRange[1]}</span>
            </div>
            <div className="relative h-2 bg-gray-200 rounded-lg">
              {/* ...existing price range inputs... */}
            </div>
          </div>
          
          {/* Rating Filter */}
          <div className="mb-6">
            <h3 className="font-semibold text-[#495E57] mb-2 border-b border-gray-200 pb-1">Minimum Rating</h3>
            <div className="flex items-center gap-2 mb-1">
              <input
                type="range"
                min={0}
                max={5}
                step={0.5}
                value={ratingFilter}
                onChange={(e) => handleRatingChange(Number(e.target.value))}
                className="w-full h-2 bg-gray-200 rounded-lg appearance-none cursor-pointer accent-[#F4CE14]"
              />
            </div>
            <div className="flex justify-between items-center">
              <span className="text-sm text-gray-600">{ratingFilter} stars</span>
              <div className="flex">
                {[...Array(5)].map((_, i) => (
                  <span key={i} className={i < Math.floor(ratingFilter) ? "text-[#F4CE14]" : "text-gray-300"}>★</span>
                ))}
              </div>
            </div>
          </div>
          
          {/* Experience Filter */}
          <div className="mb-6">
            <h3 className="font-semibold text-[#495E57] mb-2 border-b border-gray-200 pb-1">Experience (years)</h3>
            <input
              type="range"
              min={0}
              max={20}
              step={1}
              value={experienceYears}
              onChange={(e) => handleExperienceChange(Number(e.target.value))}
              className="w-full h-2 bg-gray-200 rounded-lg appearance-none cursor-pointer accent-[#F4CE14]"
            />
            <span className="text-sm text-gray-600">{experienceYears}+ years</span>
          </div>
          
          {/* Verification Filter */}
          <div className="mb-6">
            <div className="flex items-center">
              <input
                type="checkbox"
                id="verified-only"
                checked={verifiedOnly}
                onChange={handleVerifiedChange}
                className="rounded border-gray-300 text-[#495E57] focus:ring-[#495E57]"
              />
              <label htmlFor="verified-only" className="ml-2 text-sm text-gray-600">
                Verified Providers Only
              </label>
            </div>
          </div>
          
          {/* Sort Options */}
          <div className="mb-6">
            <h3 className="font-semibold text-[#495E57] mb-2 border-b border-gray-200 pb-1">Sort By</h3>
            <div className="space-y-1">
              {[
                { value: 'recommended', label: 'Recommended' },
                { value: 'price_low', label: 'Price: Low to High' },
                { value: 'price_high', label: 'Price: High to Low' },
                { value: 'rating', label: 'Highest Rating' },
                { value: 'experience', label: 'Most Experienced' }
              ].map(option => (
                <div key={option.value} className="flex items-center">
                  <input
                    type="radio"
                    id={`radio-${option.value}`}
                    name="sortOption"
                    value={option.value}
                    checked={sortOption === option.value}
                    onChange={() => handleSortChange(option.value)}
                    className="border-gray-300 text-[#495E57] focus:ring-[#495E57]"
                  />
                  <label htmlFor={`radio-${option.value}`} className="ml-2 text-sm text-gray-600">
                    {option.label}
                  </label>
                </div>
              ))}
            </div>
          </div>
          
          <div className="flex gap-2">
            <button
              className="flex-1 bg-[#F4CE14] hover:bg-[#e6c113] text-[#495E57] font-bold py-2 px-4 rounded transition-colors"
              onClick={() => {
                setSelectedCategories([]);
                setPriceRange([0, 10000]);
                setRatingFilter(0);
                setVerifiedOnly(false);
                setSortOption('recommended');
                setExperienceYears(0);
              }}
            >
              Reset
            </button>
            <button
              className="flex-1 bg-[#495E57] hover:bg-[#3e4f49] text-white font-bold py-2 px-4 rounded transition-colors"
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
              Apply
            </button>
          </div>
        </div>
      </div>
    </div>
  );
};

export default React.memo(ServiceFilters);
