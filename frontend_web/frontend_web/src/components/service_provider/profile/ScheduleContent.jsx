import React, { useState, useEffect } from 'react';
import { Calendar, momentLocalizer } from 'react-big-calendar';
import 'react-big-calendar/lib/css/react-big-calendar.css';
import moment from 'moment';
import { motion, AnimatePresence } from 'framer-motion';
import apiClient, { getApiUrl } from '../../../utils/apiConfig';

// Initialize the localizer for react-big-calendar
const localizer = momentLocalizer(moment);

function ScheduleContent() {
  const [schedules, setSchedules] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [selectedDay, setSelectedDay] = useState('MONDAY');
  const [startTime, setStartTime] = useState('08:00');
  const [endTime, setEndTime] = useState('17:00');
  const [isAvailable, setIsAvailable] = useState(true);
  const [successMessage, setSuccessMessage] = useState('');
  const [calendarEvents, setCalendarEvents] = useState([]);
  const [isFullScreen, setIsFullScreen] = useState(false);

  const daysOfWeek = ['MONDAY', 'TUESDAY', 'WEDNESDAY', 'THURSDAY', 'FRIDAY', 'SATURDAY', 'SUNDAY'];

  const [providerId, setProviderId] = useState(null);
  const token = localStorage.getItem('authToken') || sessionStorage.getItem('authToken');
  const userId = localStorage.getItem('userId') || sessionStorage.getItem('userId');

  const pageVariants = {
    initial: { opacity: 0 },
    animate: { 
      opacity: 1,
      transition: { 
        duration: 0.5, 
        when: "beforeChildren", 
        staggerChildren: 0.1 
      } 
    },
    exit: { opacity: 0 }
  };

  const itemVariants = {
    initial: { opacity: 0, y: 20 },
    animate: { 
      opacity: 1, 
      y: 0,
      transition: { 
        type: "spring", 
        stiffness: 100, 
        damping: 12 
      }
    }
  };

  const cardVariants = {
    initial: { opacity: 0, y: 15 },
    animate: { 
      opacity: 1, 
      y: 0,
      transition: { 
        type: "spring", 
        stiffness: 100, 
        damping: 10 
      }
    },
    hover: {
      y: -5,
      boxShadow: "0 10px 25px -5px rgba(0, 0, 0, 0.1), 0 10px 10px -5px rgba(0, 0, 0, 0.04)",
      transition: { duration: 0.3 }
    }
  };

  const listItemVariants = {
    initial: { opacity: 0, x: -10 },
    animate: { 
      opacity: 1, 
      x: 0,
      transition: { 
        type: "spring", 
        stiffness: 100 
      }
    },
    hover: {
      backgroundColor: "rgba(73, 94, 87, 0.05)",
      transition: { duration: 0.2 }
    }
  };

  useEffect(() => {
    const getProviderId = async () => {
      try {
        const providersResponse = await apiClient.get(getApiUrl("/service-providers/getAll"));
        
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
      const response = await apiClient.get(getApiUrl(`/schedules/provider/${providerIdToUse}`));
      
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
        isAvailable: isAvailable,
        available: isAvailable
      };
      
      await apiClient.post(getApiUrl(`/schedules/provider/${providerId}`), scheduleData);
      
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
      await apiClient.delete(getApiUrl(`/schedules/${scheduleId}`));
      
      setSuccessMessage('Schedule deleted successfully!');
      setTimeout(() => setSuccessMessage(''), 3000);
      fetchSchedules(providerId);
    } catch (err) {
      console.error('Error deleting schedule:', err);
      setError('Failed to delete schedule. Please try again.');
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

  const isScheduleAvailable = (schedule) => {
    if (schedule.isAvailable !== undefined) return schedule.isAvailable;
    if (schedule.available !== undefined) return schedule.available;
    return true;
  };

  const groupedSchedules = daysOfWeek.map(day => ({
    day,
    slots: schedules.filter(schedule => schedule.dayOfWeek === day)
  }));

  useEffect(() => {
    if (schedules.length > 0) {
      const events = [];
      const today = moment().startOf('week');
      
      schedules.forEach(schedule => {
        const dayMapping = {
          'MONDAY': 1, 'TUESDAY': 2, 'WEDNESDAY': 3, 'THURSDAY': 4,
          'FRIDAY': 5, 'SATURDAY': 6, 'SUNDAY': 0
        };
        
        const dayNumber = dayMapping[schedule.dayOfWeek];
        const scheduleDate = moment(today).add(dayNumber, 'days');
        
        const startTimeParts = schedule.startTime.split(':');
        const endTimeParts = schedule.endTime.split(':');
        
        const start = moment(scheduleDate)
          .hours(parseInt(startTimeParts[0]))
          .minutes(parseInt(startTimeParts[1]))
          .toDate();
          
        const end = moment(scheduleDate)
          .hours(parseInt(endTimeParts[0]))
          .minutes(parseInt(endTimeParts[1]))
          .toDate();
        
        events.push({
          title: isScheduleAvailable(schedule) ? 'Available' : 'Booked',
          start,
          end,
          allDay: false,
          resource: schedule,
        });
      });
      
      setCalendarEvents(events);
    }
  }, [schedules]);

  const eventStyleGetter = (event) => {
    const isAvailable = event.title === 'Available';
    
    return {
      style: {
        backgroundColor: isAvailable ? '#10B981' : '#EF4444',
        color: 'white',
        border: 'none',
        borderRadius: '4px'
      }
    };
  };

  const toggleFullScreen = () => {
    setIsFullScreen(!isFullScreen);
  };

  return (
    <motion.div 
      className="max-w-7xl px-4 sm:px-6 lg:px-8 py-8"
      variants={pageVariants}
      initial="initial"
      animate="animate"
      exit="exit"
    >
      <motion.div 
        className="mb-8"
        initial={{ opacity: 0, y: -20 }}
        animate={{ opacity: 1, y: 0 }}
        transition={{ delay: 0.2, duration: 0.5 }}
      >
        <h1 className="text-3xl font-bold text-gray-800">My Schedules</h1>
        <p className="text-gray-600 mt-2">
          Manage your availability and schedule for client bookings
        </p>
      </motion.div>
      
      {error && (
        <motion.div 
          className="mb-6 p-4 bg-red-50 border-l-4 border-red-500 rounded-md text-red-700 shadow"
          initial={{ opacity: 0, x: -20 }}
          animate={{ opacity: 1, x: 0 }}
          transition={{ duration: 0.3 }}
        >
          <div className="flex">
            <div className="flex-shrink-0">
              <svg className="h-5 w-5 text-red-400" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 20 20" fill="currentColor">
                <path fillRule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zM8.707 7.293a1 1 0 00-1.414 1.414L8.586 10l-1.293 1.293a1 1 0 101.414 1.414L10 11.414l1.293 1.293a1 1 0 001.414-1.414L11.414 10l1.293-1.293a1 1 0 00-1.414-1.414L10 8.586 8.707 7.293z" clipRule="evenodd" />
              </svg>
            </div>
            <div className="ml-3">
              <p className="text-sm font-medium">{error}</p>
            </div>
          </div>
        </motion.div>
      )}
      
      {successMessage && (
        <motion.div 
          className="mb-6 p-4 bg-green-50 border-l-4 border-green-500 rounded-md text-green-700 shadow"
          initial={{ opacity: 0, scale: 0.9 }}
          animate={{ opacity: 1, scale: 1 }}
          transition={{ duration: 0.3 }}
        >
          <div className="flex">
            <div className="flex-shrink-0">
              <svg className="h-5 w-5 text-green-400" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 20 20" fill="currentColor">
                <path fillRule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zm3.707-9.293a1 1 0 00-1.414-1.414L9 10.586 7.707 9.293a1 1 0 00-1.414 1.414l2 2a1 1 0 001.414 0l4-4z" clipRule="evenodd" />
              </svg>
            </div>
            <div className="ml-3">
              <p className="text-sm font-medium">{successMessage}</p>
            </div>
          </div>
        </motion.div>
      )}

      {!isFullScreen && (
        <motion.div 
          className="grid grid-cols-1 lg:grid-cols-2 gap-8"
          variants={itemVariants}
        >
          <motion.div 
            className="bg-white p-6 rounded-lg shadow-md"
            variants={cardVariants}
            whileHover="hover"
          >
            <motion.h2 
              className="text-xl font-semibold text-[#495E57] mb-4"
              variants={itemVariants}
            >
              Add New Schedule
            </motion.h2>
            
            <form onSubmit={handleAddSchedule} className="space-y-4">
              <motion.div variants={itemVariants}>
                <label className="block text-gray-700 mb-2">Day of Week</label>
                <select
                  value={selectedDay}
                  onChange={(e) => setSelectedDay(e.target.value)}
                  className="w-full p-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-[#495E57] transition-all"
                >
                  {daysOfWeek.map(day => (
                    <option key={day} value={day}>{day.charAt(0) + day.slice(1).toLowerCase()}</option>
                  ))}
                </select>
              </motion.div>
              
              <motion.div variants={itemVariants}>
                <label className="block text-gray-700 mb-2">Start Time</label>
                <input
                  type="time"
                  value={startTime}
                  onChange={(e) => setStartTime(e.target.value)}
                  className="w-full p-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-[#495E57] transition-all"
                  required
                />
              </motion.div>
              
              <motion.div variants={itemVariants}>
                <label className="block text-gray-700 mb-2">End Time</label>
                <input
                  type="time"
                  value={endTime}
                  onChange={(e) => setEndTime(e.target.value)}
                  className="w-full p-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-[#495E57] transition-all"
                  required
                />
              </motion.div>
              
              <motion.div 
                className="flex items-center"
                variants={itemVariants}
              >
                <input
                  type="checkbox"
                  checked={isAvailable}
                  onChange={(e) => setIsAvailable(e.target.checked)}
                  className="h-4 w-4 text-[#495E57] focus:ring-[#495E57] border-gray-300 rounded transition-all"
                />
                <label className="ml-2 text-gray-700">Available for bookings</label>
              </motion.div>
              
              <motion.div 
                className="flex gap-4"
                variants={itemVariants}
              >
                <motion.button
                  type="submit"
                  className="bg-[#495E57] text-white py-2 px-6 rounded-md hover:bg-[#3A4A47] transition-all"
                  whileHover={{ scale: 1.03, boxShadow: "0 4px 8px rgba(0, 0, 0, 0.1)" }}
                  whileTap={{ scale: 0.97 }}
                >
                  Add Schedule
                </motion.button>
              </motion.div>
            </form>
          </motion.div>

          <motion.div 
            className="bg-white p-6 rounded-lg shadow-md"
            variants={cardVariants}
            whileHover="hover"
          >
            <motion.h2 
              className="text-xl font-semibold text-[#495E57] mb-4"
              variants={itemVariants}
            >
              Current Schedule
            </motion.h2>
            
            {loading ? (
              <motion.div 
                className="flex justify-center items-center py-12"
                initial={{ opacity: 0 }}
                animate={{ opacity: 1 }}
              >
                <div className="w-12 h-12 rounded-full border-t-2 border-r-2 border-[#495E57] animate-spin"></div>
                <span className="ml-3 text-gray-500">Loading schedules...</span>
              </motion.div>
            ) : schedules.length === 0 ? (
              <motion.p 
                className="text-gray-500 py-8 text-center"
                variants={itemVariants}
              >
                No schedules found. Add your availability using the form.
              </motion.p>
            ) : (
              <motion.div 
                className="space-y-6 max-h-[400px] overflow-y-auto pr-2 hide-scrollbar bg-white"
                variants={itemVariants}
                style={{
                  scrollbarWidth: 'none',
                  msOverflowStyle: 'none',
                }}
              >
                {groupedSchedules.map(({ day, slots }, index) => (
                  <motion.div 
                    key={day} 
                    className="border-b border-gray-100 pb-4"
                    variants={itemVariants}
                    initial="initial"
                    animate="animate"
                    custom={index * 0.1}
                    transition={{ delay: index * 0.05 }}
                  >
                    <h3 className="font-medium text-lg mb-2 text-[#495E57]">{day.charAt(0) + day.slice(1).toLowerCase()}</h3>
                    {slots.length === 0 ? (
                      <p className="text-gray-500 text-sm">No time slots added</p>
                    ) : (
                      <ul className="space-y-3">
                        {slots.map(schedule => (
                          <motion.li 
                            key={schedule.scheduleId} 
                            className="flex justify-between items-center bg-white border border-gray-100 shadow-sm p-3 rounded-lg hover:shadow-md transition-shadow"
                            variants={listItemVariants}
                            whileHover={{
                              backgroundColor: "rgba(255, 255, 255, 1)",
                              boxShadow: "0 4px 6px -1px rgba(0, 0, 0, 0.1), 0 2px 4px -1px rgba(0, 0, 0, 0.06)"
                            }}
                            layout
                          >
                            <span className="text-gray-800 font-medium">
                              {formatTime(schedule.startTime)} - {formatTime(schedule.endTime)}
                              {isScheduleAvailable(schedule) ? 
                                <span className="ml-2 text-green-500 bg-green-50 px-2 py-0.5 rounded-full text-xs font-semibold">Available</span> : 
                                <span className="ml-2 text-red-500 bg-red-50 px-2 py-0.5 rounded-full text-xs font-semibold">Booked</span>}
                            </span>
                            <motion.button
                              onClick={() => handleDeleteSchedule(schedule.scheduleId)}
                              className="text-red-500 hover:text-red-700 p-1 rounded-full hover:bg-red-50"
                              whileHover={{ scale: 1.2, rotate: 20 }}
                              whileTap={{ scale: 0.9 }}
                            >
                              <svg xmlns="http://www.w3.org/2000/svg" className="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16" />
                              </svg>
                            </motion.button>
                          </motion.li>
                        ))}
                      </ul>
                    )}
                  </motion.div>
                ))}
              </motion.div>
            )}
          </motion.div>
        </motion.div>
      )}

      <AnimatePresence>
        <motion.div 
          key={isFullScreen ? 'fullscreen' : 'normal'}
          className={`mt-8 bg-white rounded-lg shadow-lg border border-gray-100 ${
            isFullScreen ? 'fixed inset-0 z-50 p-4' : ''
          }`}
          initial={{ opacity: 0, y: 20 }}
          animate={{ 
            opacity: 1, 
            y: 0,
            height: isFullScreen ? '100vh' : 'auto',
            width: isFullScreen ? '100vw' : 'auto'
          }}
          exit={{ opacity: 0, y: -20 }}
          transition={{ 
            type: "spring", 
            stiffness: 200, 
            damping: 25,
            duration: 0.4
          }}
          layout
        >
          <motion.div 
            className="flex justify-between items-center mb-4 px-6 pt-4"
            layout
          >
            <motion.h2 
              className="text-xl font-semibold text-[#495E57]"
              layout
            >
              Schedule Visualization
            </motion.h2>
            <motion.button 
              onClick={toggleFullScreen}
              className="flex items-center gap-2 bg-[#495E57] text-white px-4 py-2 rounded-lg hover:bg-[#3e4f49] transition-colors"
              whileHover={{ scale: 1.05 }}
              whileTap={{ scale: 0.95 }}
            >
              {isFullScreen ? (
                <>
                  <svg xmlns="http://www.w3.org/2000/svg" className="h-5 w-5" viewBox="0 0 20 20" fill="currentColor">
                    <path fillRule="evenodd" d="M5 10a1 1 0 011-1h8a1 1 0 110 2H6a1 1 0 01-1-1z" clipRule="evenodd" />
                  </svg>
                  Exit Fullscreen
                </>
              ) : (
                <>
                  <svg xmlns="http://www.w3.org/2000/svg" className="h-5 w-5" viewBox="0 0 20 20" fill="currentColor">
                    <path fillRule="evenodd" d="M3 4a1 1 0 011-1h4a1 1 0 010 2H6.414l2.293 2.293a1 1 0 01-1.414 1.414L5 6.414V8a1 1 0 01-2 0V4zm9 1a1 1 0 010-2h4a1 1 0 011 1v4a1 1 0 01-2 0V6.414l-2.293 2.293a1 1 0 11-1.414-1.414L13.586 5H12zm-9 7a1 1 0 012 0v1.586l2.293-2.293a1 1 0 011.414 1.414L6.414 15H8a1 1 0 010 2H4a1 1 0 01-1-1v-4zm13-1a1 1 0 011 1v4a1 1 0 01-1 1h-4a1 1 0 010-2h1.586l-2.293-2.293a1 1 0 011.414-1.414L15 13.586V12a1 1 0 011-1z" clipRule="evenodd" />
                  </svg>
                  Expand Calendar
                </>
              )}
            </motion.button>
          </motion.div>
          
          <motion.div 
            className={`px-6 pb-6 ${isFullScreen ? 'h-[calc(100vh-120px)]' : 'h-[500px]'}`}
            layout
          >
            {!loading && schedules.length > 0 ? (
              <motion.div 
                className="h-full"
                initial={{ opacity: 0 }}
                animate={{ opacity: 1 }}
                transition={{ duration: 0.5, delay: 0.2 }}
              >
                <Calendar
                  localizer={localizer}
                  events={calendarEvents}
                  startAccessor="start"
                  endAccessor="end"
                  defaultView="week"
                  views={['week', 'day', 'month']}
                  step={60}
                  timeslots={1}
                  eventPropGetter={eventStyleGetter}
                  toolbar={true}
                  className="rounded-md border"
                />
              </motion.div>
            ) : (
              <motion.div 
                className="flex flex-col items-center justify-center h-full"
                initial={{ opacity: 0 }}
                animate={{ opacity: 1 }}
                transition={{ duration: 0.5, delay: 0.2 }}
              >
                {loading ? (
                  <motion.div 
                    initial={{ scale: 0 }}
                    animate={{ scale: 1 }}
                    transition={{ type: "spring", stiffness: 200, delay: 0.2 }}
                  >
                    <div className="w-16 h-16 border-4 border-[#F4CE14] border-t-transparent rounded-full animate-spin"></div>
                    <p className="text-gray-500 mt-4 text-center">Loading calendar...</p>
                  </motion.div>
                ) : (
                  <motion.div 
                    className="text-center"
                    initial={{ y: 20, opacity: 0 }}
                    animate={{ y: 0, opacity: 1 }}
                    transition={{ delay: 0.3 }}
                  >
                    <svg className="w-16 h-16 text-gray-300 mx-auto" fill="none" stroke="currentColor" viewBox="0 0 24 24" xmlns="http://www.w3.org/2000/svg">
                      <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={1.5} d="M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z"></path>
                    </svg>
                    <p className="text-gray-500 mt-4">Add schedules to see your calendar visualization</p>
                    <motion.button
                      onClick={() => window.scrollTo({ top: 0, behavior: 'smooth' })}
                      className="mt-4 px-4 py-2 bg-[#495E57] text-white rounded-md text-sm"
                      whileHover={{ scale: 1.05 }}
                      whileTap={{ scale: 0.95 }}
                    >
                      Add your first schedule
                    </motion.button>
                  </motion.div>
                )}
              </motion.div>
            )}
          </motion.div>
        </motion.div>
      </AnimatePresence>

      <AnimatePresence>
        {isFullScreen && (
          <motion.div
            className="fixed inset-0 bg-black bg-opacity-50 z-40"
            initial={{ opacity: 0 }}
            animate={{ opacity: 1 }}
            exit={{ opacity: 0 }}
            transition={{ duration: 0.3 }}
            onClick={toggleFullScreen}
          />
        )}
      </AnimatePresence>
    </motion.div>
  );
}

export default ScheduleContent;
