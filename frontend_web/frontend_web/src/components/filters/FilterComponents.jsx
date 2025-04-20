import React from 'react';

// Checkbox filter component (for categories, verification status)
export const CheckboxFilter = React.memo(({ label, options, selectedValues, onChange }) => {
  return (
    <div className="mb-4">
      <h3 className="font-semibold text-gray-700 mb-2">{label}</h3>
      <div className="space-y-1">
        {options.map(option => (
          <div key={option.value} className="flex items-center">
            <input
              type="checkbox"
              id={`checkbox-${option.value}`}
              checked={selectedValues.includes(option.value)}
              onChange={() => onChange(option.value)}
              className="rounded border-gray-300 text-blue-600 focus:ring-blue-500"
            />
            <label htmlFor={`checkbox-${option.value}`} className="ml-2 text-sm text-gray-600">
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
    <div className="mb-4">
      <h3 className="font-semibold text-gray-700 mb-2">{label}</h3>
      <div className="flex flex-col">
        <input
          type="range"
          min={min}
          max={max}
          step={step}
          value={value}
          onChange={(e) => onChange(Number(e.target.value))}
          className="w-full h-2 bg-gray-200 rounded-lg appearance-none cursor-pointer"
        />
        <span className="text-sm text-gray-600 mt-1">
          {formatValue ? formatValue(value) : value}
        </span>
      </div>
    </div>
  );
});

// Radio button filter component (for sorting options)
export const RadioFilter = React.memo(({ label, options, value, onChange }) => {
  return (
    <div className="mb-4">
      <h3 className="font-semibold text-gray-700 mb-2">{label}</h3>
      <div className="space-y-1">
        {options.map(option => (
          <div key={option.value} className="flex items-center">
            <input
              type="radio"
              id={`radio-${option.value}`}
              name={label.replace(/\s+/g, '')}
              value={option.value}
              checked={value === option.value}
              onChange={() => onChange(option.value)}
              className="border-gray-300 text-blue-600 focus:ring-blue-500"
            />
            <label htmlFor={`radio-${option.value}`} className="ml-2 text-sm text-gray-600">
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
  return (
    <div className="mb-4">
      <h3 className="font-semibold text-gray-700 mb-2">{label}</h3>
      <div className="flex items-center justify-between mb-2">
        <span className="text-sm text-gray-600">{formatValue ? formatValue(value[0]) : value[0]}</span>
        <span className="text-sm text-gray-600">{formatValue ? formatValue(value[1]) : value[1]}</span>
      </div>
      <div className="relative h-2 bg-gray-200 rounded-lg">
        <input
          type="range"
          min={min}
          max={max}
          value={value[0]}
          onChange={(e) => onChange([Number(e.target.value), value[1]])}
          className="absolute w-full h-2 bg-transparent appearance-none pointer-events-none"
          style={{ zIndex: 1 }}
        />
        <input
          type="range"
          min={min}
          max={max}
          value={value[1]}
          onChange={(e) => onChange([value[0], Number(e.target.value)])}
          className="absolute w-full h-2 bg-transparent appearance-none pointer-events-none"
          style={{ zIndex: 2 }}
        />
      </div>
    </div>
  );
});
