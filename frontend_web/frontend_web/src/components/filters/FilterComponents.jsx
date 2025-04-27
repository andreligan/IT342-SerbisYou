import React from 'react';

// Checkbox filter component (for categories, verification status)
export const CheckboxFilter = React.memo(({ label, options, selectedValues, onChange }) => {
  return (
    <div className="mb-6">
      <h3 className="font-semibold text-[#495E57] mb-3 pb-2 border-b border-gray-200">{label}</h3>
      <div className="space-y-2">
        {options.map(option => (
          <div key={option.value} className="flex items-center">
            <div className="relative flex items-center">
              <input
                type="checkbox"
                id={`checkbox-${option.value}`}
                checked={selectedValues.includes(option.value)}
                onChange={() => onChange(option.value)}
                className="w-4 h-4 rounded border-gray-300 text-[#F4CE14] focus:ring-[#F4CE14]/30 transition-all duration-200 cursor-pointer"
              />
              <span className={`absolute w-full h-full pointer-events-none ${selectedValues.includes(option.value) ? 'scale-100' : 'scale-0'} transition-transform duration-200`}>
                <span className="absolute inset-0 bg-[#F4CE14]/10 rounded-sm"></span>
              </span>
            </div>
            <label 
              htmlFor={`checkbox-${option.value}`} 
              className={`ml-3 text-sm ${selectedValues.includes(option.value) ? 'text-[#495E57] font-medium' : 'text-gray-600'} cursor-pointer transition-colors duration-200`}
            >
              {option.label}
            </label>
          </div>
        ))}
      </div>
    </div>
  );
});

// Range slider filter component (for price, rating)
export const RangeFilter = React.memo(({ label, min, max, step, value, onChange, formatValue }) => {
  return (
    <div className="mb-6">
      <h3 className="font-semibold text-[#495E57] mb-3 pb-2 border-b border-gray-200">{label}</h3>
      <div className="flex flex-col">
        <div className="relative pt-1">
          <div className="flex items-center justify-between mb-2">
            <div className="text-sm font-medium text-gray-700 bg-gray-50 px-2 py-1 rounded-md">
              {formatValue ? formatValue(value) : value}
            </div>
          </div>
          <div className="overflow-hidden h-2 bg-gray-200 rounded-lg relative">
            <div 
              className="absolute top-0 h-2 rounded-l-lg bg-gradient-to-r from-[#F4CE14] to-[#F4CE14]/70" 
              style={{width: `${((value - min) / (max - min)) * 100}%`}}
            ></div>
          </div>
          <input
            type="range"
            min={min}
            max={max}
            step={step}
            value={value}
            onChange={(e) => onChange(Number(e.target.value))}
            className="absolute top-0 w-full h-2 opacity-0 cursor-pointer"
          />
        </div>
      </div>
    </div>
  );
});

// Radio button filter component (for sorting options)
export const RadioFilter = React.memo(({ label, options, value, onChange }) => {
  return (
    <div className="mb-6">
      <h3 className="font-semibold text-[#495E57] mb-3 pb-2 border-b border-gray-200">{label}</h3>
      <div className="space-y-2">
        {options.map(option => (
          <div key={option.value} className="flex items-center">
            <div className="relative w-4 h-4">
              <input
                type="radio"
                id={`radio-${option.value}`}
                name={label.replace(/\s+/g, '')}
                value={option.value}
                checked={value === option.value}
                onChange={() => onChange(option.value)}
                className="absolute w-4 h-4 opacity-0 cursor-pointer"
              />
              <div className={`w-4 h-4 border rounded-full transition-all duration-200 ${value === option.value ? 'border-[#F4CE14]' : 'border-gray-400'}`}>
                <div className={`w-2 h-2 rounded-full bg-[#F4CE14] absolute top-1/2 left-1/2 transform -translate-x-1/2 -translate-y-1/2 transition-all duration-200 ${value === option.value ? 'opacity-100 scale-100' : 'opacity-0 scale-0'}`}></div>
              </div>
            </div>
            <label 
              htmlFor={`radio-${option.value}`} 
              className={`ml-3 text-sm ${value === option.value ? 'text-[#495E57] font-medium' : 'text-gray-600'} cursor-pointer`}
            >
              {option.label}
            </label>
          </div>
        ))}
      </div>
    </div>
  );
});

// Dual range slider for price range
export const DualRangeFilter = React.memo(({ label, min, max, value, onChange, formatValue }) => {
  // Calculate percentages for styling
  const minPercent = ((value[0] - min) / (max - min)) * 100;
  const maxPercent = ((value[1] - min) / (max - min)) * 100;
  
  return (
    <div className="mb-6">
      <h3 className="font-semibold text-[#495E57] mb-3 pb-2 border-b border-gray-200">{label}</h3>
      
      <div className="flex items-center justify-between mb-2">
        <div className="text-sm font-medium text-gray-700 bg-gray-50 px-2 py-1 rounded-md">
          {formatValue ? formatValue(value[0]) : value[0]}
        </div>
        <div className="text-sm font-medium text-gray-700 bg-gray-50 px-2 py-1 rounded-md">
          {formatValue ? formatValue(value[1]) : value[1]}
        </div>
      </div>
      
      <div className="relative h-2 mb-6">
        {/* Track and colored range */}
        <div className="absolute inset-0 rounded-lg bg-gray-200"></div>
        <div 
          className="absolute h-full bg-gradient-to-r from-[#F4CE14]/80 to-[#F4CE14] rounded-lg"
          style={{ left: `${minPercent}%`, right: `${100 - maxPercent}%` }}
        ></div>
        
        {/* Thumbs */}
        <div 
          className="absolute w-5 h-5 bg-white border-2 border-[#F4CE14] rounded-full shadow-md -mt-1.5 transform -translate-x-1/2 hover:scale-110 transition-transform duration-150"
          style={{ left: `${minPercent}%` }}
        ></div>
        <div 
          className="absolute w-5 h-5 bg-white border-2 border-[#F4CE14] rounded-full shadow-md -mt-1.5 transform -translate-x-1/2 hover:scale-110 transition-transform duration-150"
          style={{ left: `${maxPercent}%` }}
        ></div>
        
        {/* Sliders - transparent but functional */}
        <input
          type="range"
          min={min}
          max={max}
          value={value[0]}
          onChange={(e) => {
            const newValue = Number(e.target.value);
            if (newValue < value[1]) {
              onChange([newValue, value[1]]);
            }
          }}
          className="absolute w-full h-2 bg-transparent appearance-none z-20 cursor-pointer"
          style={{ 
            pointerEvents: "auto",
            opacity: 0
          }}
        />
        <input
          type="range"
          min={min}
          max={max}
          value={value[1]}
          onChange={(e) => {
            const newValue = Number(e.target.value);
            if (newValue > value[0]) {
              onChange([value[0], newValue]);
            }
          }}
          className="absolute w-full h-2 bg-transparent appearance-none z-20 cursor-pointer"
          style={{ 
            pointerEvents: "auto",
            opacity: 0
          }}
        />
      </div>
    </div>
  );
});
