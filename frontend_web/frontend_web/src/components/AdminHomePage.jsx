import React, { useState, useEffect } from 'react';
import axios from 'axios';
import { Link } from 'react-router-dom';

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

  useEffect(() => {
    // Simulate loading stats - in a real app, you would fetch this from your API
    const fetchStats = async () => {
      try {
        setLoading(true);
        // Mock data - replace with actual API calls
        setTimeout(() => {
          setStats({
            totalUsers: 125,
            totalCustomers: 92,
            totalServiceProviders: 33,
            totalServices: 48,
            totalBookings: 215,
          });
          setLoading(false);
        }, 1000);
      } catch (err) {
        setError('Failed to load statistics');
        setLoading(false);
        console.error(err);
      }
    };

    fetchStats();
  }, []);

  const StatCard = ({ title, value, icon, color }) => (
    <div className={`p-6 rounded-lg shadow-md ${color}`}>
      <div className="flex justify-between items-center">
        <div>
          <p className="text-sm text-gray-600 font-medium">{title}</p>
          <h3 className="text-3xl font-bold mt-1">{loading ? '...' : value}</h3>
        </div>
        <div className={`p-3 rounded-full bg-opacity-20 ${color}`}>
          {icon}
        </div>
      </div>
    </div>
  );

  return (
    <div className="min-h-screen bg-gray-100">
      <div className="container mx-auto px-4 py-8">
        {/* Header */}
        <div className="mb-8">
          <h1 className="text-3xl font-bold text-[#495E57]">Admin Dashboard</h1>
          <p className="text-gray-600">Welcome to SerbisYo admin panel</p>
        </div>

        {/* Stats Cards */}
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6 mb-8">
          <StatCard 
            title="Total Users" 
            value={stats.totalUsers} 
            color="bg-blue-50"
            icon={
              <svg xmlns="http://www.w3.org/2000/svg" className="h-8 w-8 text-blue-500" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 4.354a4 4 0 110 5.292M15 21H3v-1a6 6 0 0112 0v1zm0 0h6v-1a6 6 0 00-9-5.197M13 7a4 4 0 11-8 0 4 4 0 018 0z" />
              </svg>
            }
          />
          
          <StatCard 
            title="Service Providers" 
            value={stats.totalServiceProviders} 
            color="bg-yellow-50"
            icon={
              <svg xmlns="http://www.w3.org/2000/svg" className="h-8 w-8 text-yellow-500" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M21 13.255A23.931 23.931 0 0112 15c-3.183 0-6.22-.62-9-1.745M16 6V4a2 2 0 00-2-2h-4a2 2 0 00-2 2v2m4 6h.01M5 20h14a2 2 0 002-2V8a2 2 0 00-2-2H5a2 2 0 00-2 2v10a2 2 0 002 2z" />
              </svg>
            }
          />
          
          <StatCard 
            title="Total Services" 
            value={stats.totalServices} 
            color="bg-green-50"
            icon={
              <svg xmlns="http://www.w3.org/2000/svg" className="h-8 w-8 text-green-500" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 5H7a2 2 0 00-2 2v12a2 2 0 002 2h10a2 2 0 002-2V7a2 2 0 00-2-2h-2M9 5a2 2 0 002 2h2a2 2 0 002-2M9 5a2 2 0 012-2h2a2 2 0 012 2" />
              </svg>
            }
          />
          
          <StatCard 
            title="Total Customers" 
            value={stats.totalCustomers} 
            color="bg-indigo-50"
            icon={
              <svg xmlns="http://www.w3.org/2000/svg" className="h-8 w-8 text-indigo-500" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M17 20h5v-2a3 3 0 00-5.356-1.857M17 20H7m10 0v-2c0-.656-.126-1.283-.356-1.857M7 20H2v-2a3 3 0 015.356-1.857M7 20v-2c0-.656.126-1.283.356-1.857m0 0a5.002 5.002 0 019.288 0M15 7a3 3 0 11-6 0 3 3 0 016 0zm6 3a2 2 0 11-4 0 2 2 0 014 0zM7 10a2 2 0 11-4 0 2 2 0 014 0z" />
              </svg>
            }
          />
          
          <StatCard 
            title="Total Bookings" 
            value={stats.totalBookings} 
            color="bg-purple-50"
            icon={
              <svg xmlns="http://www.w3.org/2000/svg" className="h-8 w-8 text-purple-500" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z" />
              </svg>
            }
          />
        </div>

        {/* Management Cards */}
        <div className="mb-8">
          <h2 className="text-2xl font-bold text-[#495E57] mb-4">Management</h2>
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
            <div className="bg-white p-6 rounded-lg shadow-md">
              <h3 className="text-lg font-semibold mb-3 flex items-center">
                <svg xmlns="http://www.w3.org/2000/svg" className="h-6 w-6 mr-2 text-[#F4CE14]" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 4.354a4 4 0 110 5.292M15 21H3v-1a6 6 0 0112 0v1zm0 0h6v-1a6 6 0 00-9-5.197M13 7a4 4 0 11-8 0 4 4 0 018 0z" />
                </svg>
                User Management
              </h3>
              <p className="text-gray-600 mb-4">Manage all users in the system, including customers and service providers.</p>
              <button className="bg-[#495E57] text-white px-4 py-2 rounded hover:bg-[#3a4a45] transition-colors">
                Manage Users
              </button>
            </div>
            
            <div className="bg-white p-6 rounded-lg shadow-md">
              <h3 className="text-lg font-semibold mb-3 flex items-center">
                <svg xmlns="http://www.w3.org/2000/svg" className="h-6 w-6 mr-2 text-[#F4CE14]" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 5H7a2 2 0 00-2 2v12a2 2 0 002 2h10a2 2 0 002-2V7a2 2 0 00-2-2h-2M9 5a2 2 0 002 2h2a2 2 0 002-2M9 5a2 2 0 012-2h2a2 2 0 012 2" />
                </svg>
                Service Management
              </h3>
              <p className="text-gray-600 mb-4">Approve, review, and manage all services listed on the platform.</p>
              <button className="bg-[#495E57] text-white px-4 py-2 rounded hover:bg-[#3a4a45] transition-colors">
                Manage Services
              </button>
            </div>
            
            <div className="bg-white p-6 rounded-lg shadow-md">
              <h3 className="text-lg font-semibold mb-3 flex items-center">
                <svg xmlns="http://www.w3.org/2000/svg" className="h-6 w-6 mr-2 text-[#F4CE14]" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z" />
                </svg>
                Booking Oversight
              </h3>
              <p className="text-gray-600 mb-4">Monitor and manage all service bookings across the platform.</p>
              <button className="bg-[#495E57] text-white px-4 py-2 rounded hover:bg-[#3a4a45] transition-colors">
                View Bookings
              </button>
            </div>
          </div>
        </div>

        {/* Recent Activity */}
        <div>
          <h2 className="text-2xl font-bold text-[#495E57] mb-4">Recent Activity</h2>
          <div className="bg-white rounded-lg shadow-md p-6">
            <div className="overflow-x-auto">
              <table className="min-w-full bg-white">
                <thead>
                  <tr>
                    <th className="py-3 px-4 border-b-2 border-gray-200 text-left text-sm font-semibold text-gray-700">Activity</th>
                    <th className="py-3 px-4 border-b-2 border-gray-200 text-left text-sm font-semibold text-gray-700">User</th>
                    <th className="py-3 px-4 border-b-2 border-gray-200 text-left text-sm font-semibold text-gray-700">Time</th>
                  </tr>
                </thead>
                <tbody>
                  <tr>
                    <td className="py-3 px-4 border-b border-gray-200">New service provider registered</td>
                    <td className="py-3 px-4 border-b border-gray-200">John Doe</td>
                    <td className="py-3 px-4 border-b border-gray-200">2 hours ago</td>
                  </tr>
                  <tr>
                    <td className="py-3 px-4 border-b border-gray-200">New service listing added</td>
                    <td className="py-3 px-4 border-b border-gray-200">Jane Smith</td>
                    <td className="py-3 px-4 border-b border-gray-200">5 hours ago</td>
                  </tr>
                  <tr>
                    <td className="py-3 px-4 border-b border-gray-200">Booking completed</td>
                    <td className="py-3 px-4 border-b border-gray-200">Mike Johnson</td>
                    <td className="py-3 px-4 border-b border-gray-200">8 hours ago</td>
                  </tr>
                  <tr>
                    <td className="py-3 px-4 border-b border-gray-200">Customer profile updated</td>
                    <td className="py-3 px-4 border-b border-gray-200">Sara Wilson</td>
                    <td className="py-3 px-4 border-b border-gray-200">1 day ago</td>
                  </tr>
                </tbody>
              </table>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

export default AdminHomePage;
