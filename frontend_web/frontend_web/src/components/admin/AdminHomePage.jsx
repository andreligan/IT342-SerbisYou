import React, { useState, useEffect } from 'react';
import axios from 'axios';
import { Link } from 'react-router-dom';
import CountUp from 'react-countup';

const AdminHomePage = () => {
  const [stats, setStats] = useState({
    totalUsers: 0,
    totalCustomers: 0,
    totalServiceProviders: 0,
    totalServices: 0,
    totalBookings: 0,
  });
  
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [adminData, setAdminData] = useState({
    firstName: '',
    lastName: ''
  });

  useEffect(() => {
    const fetchAdminData = async () => {
      try {
        const token = localStorage.getItem('authToken') || sessionStorage.getItem('authToken');
        const userId = localStorage.getItem('userId') || sessionStorage.getItem('userId');
        
        if (!token || !userId) {
          throw new Error('Authentication information not found');
        }
        
        const headers = {
          'Authorization': `Bearer ${token}`
        };
        
        // Fetch admin information using userId
        const response = await axios.get(`/api/admins/getByUserId/${userId}`, { headers });
        console.log('Admin Data:', response.data);
        if (response.data) {
          setAdminData({
            firstName: response.data.firstName || '',
            lastName: response.data.lastName || ''
          });
        }
      } catch (err) {
        console.error('Error fetching admin data:', err);
        // Set fallback name so the UI shows something useful
        setAdminData({
          firstName: 'Admin',
          lastName: 'User'
        });
      }
    };

    fetchAdminData();
  }, []);

  useEffect(() => {
    const fetchStats = async () => {
      try {
        setLoading(true);
        
        const token = localStorage.getItem('authToken') || sessionStorage.getItem('authToken');
        
        if (!token) {
          throw new Error('Authentication token not found');
        }
        
        const headers = {
          'Authorization': `Bearer ${token}`
        };
        
        const serviceProvidersResponse = await axios.get('/api/service-providers/getAll', { headers });
        const customersResponse = await axios.get('/api/customers/getAll', { headers });
        const servicesResponse = await axios.get('/api/services/getAll', { headers });
        const bookingsResponse = await axios.get('/api/bookings/getAll', { headers });
        
        const serviceProviderCount = serviceProvidersResponse.data ? serviceProvidersResponse.data.length : 0;
        const customerCount = customersResponse.data ? customersResponse.data.length : 0;
        const serviceCount = servicesResponse.data ? servicesResponse.data.length : 0;
        const bookingCount = bookingsResponse.data ? bookingsResponse.data.length : 0;
        const userCount = serviceProviderCount + customerCount;
        
        setStats({
          totalUsers: userCount,
          totalCustomers: customerCount,
          totalServiceProviders: serviceProviderCount,
          totalServices: serviceCount,
          totalBookings: bookingCount,
        });
        
        setLoading(false);
      } catch (err) {
        setError('Failed to load statistics');
        setLoading(false);
        console.error('Error fetching stats:', err);
      }
    };

    fetchStats();
  }, []);

  const StatCard = ({ title, value, icon, color, bgColor, iconBg }) => (
    <div className={`p-6 rounded-lg shadow-lg ${bgColor} transition-transform duration-300 transform hover:scale-105 hover:shadow-xl`}>
      <div className="flex justify-between items-center">
        <div>
          <p className="text-sm font-medium text-gray-600">{title}</p>
          <h3 className="text-3xl font-bold mt-1 text-gray-800">
            {loading ? '...' : 
              <CountUp 
                start={0} 
                end={value} 
                duration={2.5} 
                separator="," 
                delay={0.2}
              />
            }
          </h3>
        </div>
        <div className={`p-4 rounded-full ${iconBg}`}>
          {icon}
        </div>
      </div>
    </div>
  );

  return (
    <div className="min-h-screen bg-gray-100">

      <div className="container mx-auto px-4 py-8">
        {/* Header */}
        <div className="mb-8 p-6 rounded-lg border-l-4 border-[#495E57]">
          <div className="flex justify-between items-center">
            <div>
              <h1 className="text-3xl font-bold text-[#495E57]">
                {adminData.firstName && adminData.lastName 
                  ? `${adminData.firstName} ${adminData.lastName}` 
                  : "Admin Dashboard"}
              </h1>
              <p className="text-gray-600">Welcome to SerbisYo admin panel</p>
            </div>
            <div className="hidden md:block">
              <div className="bg-gray-100 rounded-lg p-2 text-sm text-gray-600 text-right">
                <span>{new Date().toLocaleDateString()}</span><br/>
                <span>{new Date().toLocaleTimeString()}</span>
              </div>
            </div>
          </div>
        </div>

        {/* Stats Cards */}
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6 mb-8">
          <StatCard 
            title="Total Users" 
            value={stats.totalUsers} 
            bgColor="bg-white"
            iconBg="bg-blue-100"
            icon={
              <svg xmlns="http://www.w3.org/2000/svg" className="h-8 w-8 text-blue-500" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 4.354a4 4 0 110 5.292M15 21H3v-1a6 6 0 0112 0v1zm0 0h6v-1a6 6 0 00-9-5.197M13 7a4 4 0 11-8 0 4 4 0 018 0z" />
              </svg>
            }
          />
          
          <StatCard 
            title="Service Providers" 
            value={stats.totalServiceProviders} 
            bgColor="bg-white"
            iconBg="bg-yellow-100"
            icon={
              <svg xmlns="http://www.w3.org/2000/svg" className="h-8 w-8 text-yellow-500" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M21 13.255A23.931 23.931 0 0112 15c-3.183 0-6.22-.62-9-1.745M16 6V4a2 2 0 00-2-2h-4a2 2 0 00-2 2v2m4 6h.01M5 20h14a2 2 0 002-2V8a2 2 0 00-2-2H5a2 2 0 00-2 2v10a2 2 0 002 2z" />
              </svg>
            }
          />
          
          <StatCard 
            title="Total Services" 
            value={stats.totalServices} 
            bgColor="bg-white"
            iconBg="bg-green-100"
            icon={
              <svg xmlns="http://www.w3.org/2000/svg" className="h-8 w-8 text-green-500" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 5H7a2 2 0 00-2 2v12a2 2 0 002 2h10a2 2 0 002-2V7a2 2 0 00-2-2h-2M9 5a2 2 0 002 2h2a2 2 0 002-2M9 5a2 2 0 012-2h2a2 2 0 012 2" />
              </svg>
            }
          />
          
          <StatCard 
            title="Total Customers" 
            value={stats.totalCustomers} 
            bgColor="bg-white"
            iconBg="bg-indigo-100"
            icon={
              <svg xmlns="http://www.w3.org/2000/svg" className="h-8 w-8 text-indigo-500" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M17 20h5v-2a3 3 0 00-5.356-1.857M17 20H7m10 0v-2c0-.656-.126-1.283-.356-1.857M7 20H2v-2a3 3 0 015.356-1.857M7 20v-2c0-.656.126-1.283.356-1.857m0 0a5.002 5.002 0 019.288 0M15 7a3 3 0 11-6 0 3 3 0 016 0zm6 3a2 2 0 11-4 0 2 2 0 014 0zM7 10a2 2 0 11-4 0 2 2 0 014 0z" />
              </svg>
            }
          />
          
          <StatCard 
            title="Total Bookings" 
            value={stats.totalBookings} 
            bgColor="bg-white"
            iconBg="bg-purple-100"
            icon={
              <svg xmlns="http://www.w3.org/2000/svg" className="h-8 w-8 text-purple-500" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z" />
              </svg>
            }
          />
        </div>

        {/* Management Cards */}
        <div className="mb-8">
          <h2 className="text-2xl font-bold text-[#495E57] mb-4 flex items-center">
            <svg xmlns="http://www.w3.org/2000/svg" className="h-6 w-6 mr-2 text-[#495E57]" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M10.325 4.317c.426-1.756 2.924-1.756 3.35 0a1.724 1.724 0 002.573 1.066c1.543-.94 3.31.826 2.37 2.37a1.724 1.724 0 001.065 2.572c1.756.426 1.756 2.924 0 3.35a1.724 1.724 0 00-1.066 2.573c.94 1.543-.826 3.31-2.37 2.37a1.724 1.724 0 00-2.572 1.065c-.426 1.756-2.924 1.756-3.35 0a1.724 1.724 0 00-2.573-1.066c-1.543.94-3.31-.826-2.37-2.37a1.724 1.724 0 00-1.065-2.572c-1.756-.426-1.756-2.924 0-3.35a1.724 1.724 0 001.066-2.573c-.94-1.543.826-3.31 2.37-2.37.996.608 2.296.07 2.572-1.065z" />
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M15 12a3 3 0 11-6 0 3 3 0 016 0z" />
            </svg>
            Management
          </h2>
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
            <div className="bg-white p-6 rounded-lg shadow-md hover:shadow-lg transition-shadow border-t-4 border-blue-500">
              <div className="mb-4 bg-blue-100 w-16 h-16 rounded-full flex items-center justify-center">
                <svg xmlns="http://www.w3.org/2000/svg" className="h-8 w-8 text-blue-500" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 4.354a4 4 0 110 5.292M15 21H3v-1a6 6 0 0112 0v1zm0 0h6v-1a6 6 0 00-9-5.197M13 7a4 4 0 11-8 0 4 4 0 018 0z" />
                </svg>
              </div>
              <h3 className="text-lg font-semibold mb-2 text-gray-800">User Management</h3>
              <p className="text-gray-600 mb-4">Manage all users in the system, including customers and service providers.</p>
              <Link to="/admin/users" className="w-full bg-[#495E57] text-white px-4 py-2 rounded hover:bg-[#3a4a45] transition-colors flex items-center justify-center">
                <span>Manage Users</span>
                <svg xmlns="http://www.w3.org/2000/svg" className="h-5 w-5 ml-2" viewBox="0 0 20 20" fill="currentColor">
                  <path fillRule="evenodd" d="M10.293 5.293a1 1 0 011.414 0l4 4a1 1 0 010 1.414l-4 4a1 1 0 01-1.414-1.414L12.586 11H5a1 1 0 110-2h7.586l-2.293-2.293a1 1 0 010-1.414z" clipRule="evenodd" />
                </svg>
              </Link>
            </div>
            
            <div className="bg-white p-6 rounded-lg shadow-md hover:shadow-lg transition-shadow border-t-4 border-green-500">
              <div className="mb-4 bg-green-100 w-16 h-16 rounded-full flex items-center justify-center">
                <svg xmlns="http://www.w3.org/2000/svg" className="h-8 w-8 text-green-500" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 5H7a2 2 0 00-2 2v12a2 2 0 002 2h10a2 2 0 002-2V7a2 2 0 00-2-2h-2M9 5a2 2 0 002 2h2a2 2 0 002-2M9 5a2 2 0 012-2h2a2 2 0 012 2" />
                </svg>
              </div>
              <h3 className="text-lg font-semibold mb-2 text-gray-800">Service Categories</h3>
              <p className="text-gray-600 mb-4">Add, edit, and manage service categories available on the platform.</p>
              <Link to="/admin/categories" className="w-full bg-[#495E57] text-white px-4 py-2 rounded hover:bg-[#3a4a45] transition-colors flex items-center justify-center">
                <span>Manage Categories</span>
                <svg xmlns="http://www.w3.org/2000/svg" className="h-5 w-5 ml-2" viewBox="0 0 20 20" fill="currentColor">
                  <path fillRule="evenodd" d="M10.293 5.293a1 1 0 011.414 0l4 4a1 1 0 010 1.414l-4 4a1 1 0 01-1.414-1.414L12.586 11H5a1 1 0 110-2h7.586l-2.293-2.293a1 1 0 010-1.414z" clipRule="evenodd" />
                </svg>
              </Link>
            </div>
            
            <div className="bg-white p-6 rounded-lg shadow-md hover:shadow-lg transition-shadow border-t-4 border-yellow-500">
              <div className="mb-4 bg-yellow-100 w-16 h-16 rounded-full flex items-center justify-center">
                <svg xmlns="http://www.w3.org/2000/svg" className="h-8 w-8 text-yellow-500" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z" />
                </svg>
              </div>
              <h3 className="text-lg font-semibold mb-2 text-gray-800">Provider Verification</h3>
              <p className="text-gray-600 mb-4">Review and approve service providers requesting verification on the platform.</p>
              <Link to="/admin/verification" className="w-full bg-[#495E57] text-white px-4 py-2 rounded hover:bg-[#3a4a45] transition-colors flex items-center justify-center">
                <span>View Requests</span>
                <svg xmlns="http://www.w3.org/2000/svg" className="h-5 w-5 ml-2" viewBox="0 0 20 20" fill="currentColor">
                  <path fillRule="evenodd" d="M10.293 5.293a1 1 0 011.414 0l4 4a1 1 0 010 1.414l-4 4a1 1 0 01-1.414-1.414L12.586 11H5a1 1 0 110-2h7.586l-2.293-2.293a1 1 0 010-1.414z" clipRule="evenodd" />
                </svg>
              </Link>
            </div>
          </div>
        </div>

      </div>
    </div>
  );
};

export default AdminHomePage;
