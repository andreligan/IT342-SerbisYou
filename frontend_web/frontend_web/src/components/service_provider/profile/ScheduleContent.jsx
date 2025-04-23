import React, { useState, useEffect } from 'react';
import axios from 'axios';

function ScheduleContent() {
  const [schedules, setSchedules] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [selectedDay, setSelectedDay] = useState('MONDAY');
  const [startTime, setStartTime] = useState('08:00');
  const [endTime, setEndTime] = useState('17:00');
  const [isAvailable, setIsAvailable] = useState(true);
  const [successMessage, setSuccessMessage] = useState('');

  const daysOfWeek = ['MONDAY', 'TUESDAY', 'WEDNESDAY', 'THURSDAY', 'FRIDAY', 'SATURDAY', 'SUNDAY'];

  // Get provider ID from storage
  const [providerId, setProviderId] = useState(null);
  const token = localStorage.getItem('authToken') || sessionStorage.getItem('authToken');
  const userId = localStorage.getItem('userId') || sessionStorage.getItem('userId');

  // Fetch provider ID first
  useEffect(() => {
    const getProviderId = async () => {
      try {
        const providersResponse = await axios.get("/api/service-providers/getAll", {
          headers: { 'Authorization': `Bearer ${token}` }
        });
        
        const provider = providersResponse.data.find(
          p => p.userAuth && p.userAuth.userId == userId
        );
        
        if (provider) {
          setProviderId(provider.providerId);
          fetchSchedules(provider.providerId);
        } else {
          setError("No service provider profile found for this account.");
          setLoading(false);
        }
      } catch (err) {
        console.error('Error fetching provider details:', err);
        setError('Failed to load provider details.');
        setLoading(false);
      }
    };
    
    if (userId && token) {
      getProviderId();
    }
  }, [userId, token]);

  const fetchSchedules = async (providerIdToUse) => {
    setLoading(true);
    try {
      const response = await axios.get(`/api/schedules/provider/${providerIdToUse}`, {
        headers: {
          Authorization: `Bearer ${token}`
        }
      });
      setSchedules(response.data || []);
      setError(null);
    } catch (err) {
      console.error('Error fetching schedules:', err);
      setError('Failed to load schedules. Please try again later.');
    } finally {
      setLoading(false);
    }
  };

  const handleAddSchedule = async (e) => {
    e.preventDefault();
    if (!providerId) {
      setError('Provider ID not found. Please reload the page.');
      return;
    }
    
    try {
      const scheduleData = {
        dayOfWeek: selectedDay,
        startTime: startTime,
        endTime: endTime,
        isAvailable: isAvailable
      };
      
      await axios.post(`/api/schedules/provider/${providerId}`, scheduleData, {
        headers: {
          Authorization: `Bearer ${token}`,
          'Content-Type': 'application/json'
        }
      });
      
      setSuccessMessage('Schedule added successfully!');
      setTimeout(() => setSuccessMessage(''), 3000);
      fetchSchedules(providerId);
    } catch (err) {
      console.error('Error adding schedule:', err);
      setError('Failed to add schedule. Please try again.');
    }
  };

  const handleDeleteSchedule = async (scheduleId) => {
    if (!window.confirm('Are you sure you want to delete this schedule?')) {
      return;
    }
    
    try {
      await axios.delete(`/api/schedules/${scheduleId}`, {
        headers: {
          Authorization: `Bearer ${token}`
        }
      });
      
      setSuccessMessage('Schedule deleted successfully!');
      setTimeout(() => setSuccessMessage(''), 3000);
      fetchSchedules(providerId);
    } catch (err) {
      console.error('Error deleting schedule:', err);
      setError('Failed to delete schedule. Please try again.');
    }
  };

  const handleInitializeSchedule = async () => {
    if (!providerId) {
      setError('Provider ID not found. Please reload the page.');
      return;
    }
    
    if (!window.confirm('This will create a default weekly schedule (Mon-Fri, 9AM-5PM). Continue?')) {
      return;
    }
    
    try {
      await axios.post(`/api/schedules/provider/${providerId}/initialize`, {}, {
        headers: {
          Authorization: `Bearer ${token}`
        }
      });
      
      setSuccessMessage('Default schedule initialized successfully!');
      setTimeout(() => setSuccessMessage(''), 3000);
      fetchSchedules(providerId);
    } catch (err) {
      console.error('Error initializing schedule:', err);
      setError('Failed to initialize schedule. Please try again.');
    }
  };

  const formatTime = (time) => {
    if (!time) return '';
    const [hours, minutes] = time.split(':');
    const hour = parseInt(hours);
    const ampm = hour >= 12 ? 'PM' : 'AM';
    const formattedHour = hour % 12 || 12;
    return `${formattedHour}:${minutes} ${ampm}`;
  };

  const groupedSchedules = daysOfWeek.map(day => ({
    day,
    slots: schedules.filter(schedule => schedule.dayOfWeek === day)
  }));

  return (
    <div className="container mx-auto py-4">
      <h1 className="text-3xl font-bold text-[#495E57] mb-6">Manage Your Schedule</h1>
      
      {error && (
        <div className="bg-red-100 border border-red-400 text-red-700 px-4 py-3 rounded mb-4">
          {error}
        </div>
      )}
      
      {successMessage && (
        <div className="bg-green-100 border border-green-400 text-green-700 px-4 py-3 rounded mb-4">
          {successMessage}
        </div>
      )}

      <div className="grid grid-cols-1 lg:grid-cols-2 gap-8">
        {/* Add Schedule Form */}
        <div className="bg-white p-6 rounded-lg shadow-md">
          <h2 className="text-xl font-semibold text-[#495E57] mb-4">Add New Schedule</h2>
          <form onSubmit={handleAddSchedule} className="space-y-4">
            <div>
              <label className="block text-gray-700 mb-2">Day of Week</label>
              <select
                value={selectedDay}
                onChange={(e) => setSelectedDay(e.target.value)}
                className="w-full p-2 border border-gray-300 rounded focus:outline-none focus:ring-2 focus:ring-[#495E57]"
              >
                {daysOfWeek.map(day => (
                  <option key={day} value={day}>{day.charAt(0) + day.slice(1).toLowerCase()}</option>
                ))}
              </select>
            </div>
            
            <div>
              <label className="block text-gray-700 mb-2">Start Time</label>
              <input
                type="time"
                value={startTime}
                onChange={(e) => setStartTime(e.target.value)}
                className="w-full p-2 border border-gray-300 rounded focus:outline-none focus:ring-2 focus:ring-[#495E57]"
                required
              />
            </div>
            
            <div>
              <label className="block text-gray-700 mb-2">End Time</label>
              <input
                type="time"
                value={endTime}
                onChange={(e) => setEndTime(e.target.value)}
                className="w-full p-2 border border-gray-300 rounded focus:outline-none focus:ring-2 focus:ring-[#495E57]"
                required
              />
            </div>
            
            <div className="flex items-center">
              <input
                type="checkbox"
                checked={isAvailable}
                onChange={(e) => setIsAvailable(e.target.checked)}
                className="h-4 w-4 text-[#495E57] focus:ring-[#495E57] border-gray-300 rounded"
              />
              <label className="ml-2 text-gray-700">Available for bookings</label>
            </div>
            
            <div className="flex gap-4">
              <button
                type="submit"
                className="bg-[#495E57] text-white py-2 px-4 rounded hover:bg-[#3A4A47] transition"
              >
                Add Schedule
              </button>
              
              <button
                type="button"
                onClick={handleInitializeSchedule}
                className="bg-yellow-400 text-black py-2 px-4 rounded hover:bg-yellow-500 transition"
              >
                Initialize Default Schedule
              </button>
            </div>
          </form>
        </div>

        {/* Schedule Display */}
        <div className="bg-white p-6 rounded-lg shadow-md">
          <h2 className="text-xl font-semibold text-[#495E57] mb-4">Current Schedule</h2>
          {loading ? (
            <p className="text-gray-500">Loading schedules...</p>
          ) : schedules.length === 0 ? (
            <p className="text-gray-500">No schedules found. Add your availability using the form.</p>
          ) : (
            <div className="space-y-6">
              {groupedSchedules.map(({ day, slots }) => (
                <div key={day} className="border-b pb-4">
                  <h3 className="font-medium text-lg mb-2">{day.charAt(0) + day.slice(1).toLowerCase()}</h3>
                  {slots.length === 0 ? (
                    <p className="text-gray-500 text-sm">No time slots added</p>
                  ) : (
                    <ul className="space-y-2">
                      {slots.map(schedule => (
                        <li key={schedule.scheduleId} className="flex justify-between items-center bg-gray-50 p-2 rounded">
                          <span>
                            {formatTime(schedule.startTime)} - {formatTime(schedule.endTime)}
                            {!schedule.isAvailable && <span className="ml-2 text-red-500">(Unavailable)</span>}
                          </span>
                          <button
                            onClick={() => handleDeleteSchedule(schedule.scheduleId)}
                            className="text-red-500 hover:text-red-700"
                          >
                            <svg xmlns="http://www.w3.org/2000/svg" className="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16" />
                            </svg>
                          </button>
                        </li>
                      ))}
                    </ul>
                  )}
                </div>
              ))}
            </div>
          )}
        </div>
      </div>
    </div>
  );
}

export default ScheduleContent;
