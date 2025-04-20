import React, { useCallback, useState, useEffect } from 'react';
import { CheckboxFilter, RangeFilter, RadioFilter, DualRangeFilter } from './FilterComponents';

const ServiceFilters = ({ services, onFilterChange, className }) => {
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

  return (
    <div className={className}>
      <div className="md:hidden mb-4">
        <button 
          onClick={toggleFilterVisibility}
          className="w-full flex items-center justify-between bg-white p-3 rounded-md shadow"
        >
          <span className="font-medium text-gray-700">Filters</span>
          <svg className={`h-5 w-5 transition-transform ${isFilterVisible ? 'transform rotate-180' : ''}`} fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M19 9l-7 7-7-7" />
          </svg>
        </button>
      </div>

      <div className={`${!isFilterVisible && 'hidden md:block'} bg-white p-4 rounded-lg shadow-md`}>
        <h2 className="text-xl font-bold text-gray-800 mb-4">Filters</h2>
        
        {/* Category Filter */}
        {categories.length > 0 && (
          <CheckboxFilter 
            label="Categories" 
            options={categories}
            selectedValues={selectedCategories}
            onChange={handleCategoryChange}
          />
        )}
        
        {/* Price Range Filter */}
        <DualRangeFilter 
          label="Price Range" 
          min={0}
          max={priceRange[1]} 
          value={priceRange}
          onChange={handlePriceRangeChange}
          formatValue={(value) => `PHP ${value}`}
        />
        
        {/* Rating Filter */}
        <RangeFilter 
          label="Minimum Rating" 
          min={0}
          max={5}
          step={0.5}
          value={ratingFilter}
          onChange={handleRatingChange}
          formatValue={(value) => `${value} ★`}
        />
        
        {/* Experience Filter */}
        <RangeFilter 
          label="Minimum Years Experience" 
          min={0}
          max={20}
          step={1}
          value={experienceYears}
          onChange={handleExperienceChange}
          formatValue={(value) => `${value} years`}
        />
        
        {/* Verification Filter */}
        <div className="mb-4">
          <div className="flex items-center">
            <input
              type="checkbox"
              id="verified-only"
              checked={verifiedOnly}
              onChange={handleVerifiedChange}
              className="rounded border-gray-300 text-blue-600 focus:ring-blue-500"
            />
            <label htmlFor="verified-only" className="ml-2 text-sm text-gray-600">
              Verified Providers Only
            </label>
          </div>
        </div>
        
        {/* Sort Options */}
        <RadioFilter 
          label="Sort By" 
          options={[
            { value: 'recommended', label: 'Recommended' },
            { value: 'price_low', label: 'Price: Low to High' },
            { value: 'price_high', label: 'Price: High to Low' },
            { value: 'rating', label: 'Highest Rating' },
            { value: 'experience', label: 'Most Experienced' }
          ]}
          value={sortOption}
          onChange={handleSortChange}
        />
        
        <div className="mt-6">
          <button
            className="w-full bg-blue-500 text-white py-2 px-4 rounded hover:bg-blue-600"
            onClick={() => {
              setSelectedCategories([]);
              setPriceRange([0, 10000]);
              setRatingFilter(0);
              setVerifiedOnly(false);
              setSortOption('recommended');
              setExperienceYears(0);
            }}
          >
            Reset Filters
          </button>
        </div>
      </div>
    </div>
  );
};

export default React.memo(ServiceFilters);
