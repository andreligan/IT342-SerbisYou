import React, { useState, useEffect, useMemo } from 'react';
import { Link } from 'react-router-dom';
import { AnimatePresence, motion } from 'framer-motion';
import { PieChart, Pie, Cell, ResponsiveContainer, Tooltip } from 'recharts';
import BaseModal from '../shared/BaseModal';
import apiClient, { getApiUrl } from '../../utils/apiConfig';

const UserManagement = () => {
  const [users, setUsers] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [searchTerm, setSearchTerm] = useState('');
  const [filterRole, setFilterRole] = useState('all');
  const [uniqueRoles, setUniqueRoles] = useState(['all']);
  const [sortConfig, setSortConfig] = useState({ key: null, direction: 'ascending' });
  const [currentPage, setCurrentPage] = useState(1);
  const [usersPerPage] = useState(10);
  const [selectedUsers, setSelectedUsers] = useState([]);
  const [isDeleteModalOpen, setIsDeleteModalOpen] = useState(false);
  const [userToDelete, setUserToDelete] = useState(null);
  const [showSuccessAlert, setShowSuccessAlert] = useState(false);
  const [successMessage, setSuccessMessage] = useState('');

  // Define brand colors for consistent use across the component
  const BRAND_COLORS = {
    primary: '#F4CE14',    // Yellow
    secondary: '#495E57',  // Teal/Green
    charts: ['#F4CE14', '#495E57', '#EE9972', '#FBDABB', '#EDEFEE']
  };

  useEffect(() => {
    const fetchUsers = async () => {
      try {
        setLoading(true);
        
        const response = await apiClient.get(getApiUrl('user-auth/getAll'));
        console.log('User data from API:', response.data);

        // Verify that the response contains an array
        if (!Array.isArray(response.data)) {
          console.error('Expected an array of users but received:', response.data);
          throw new Error('Invalid data format received from server');
        }

        const rolesSet = new Set(['all']);
        response.data.forEach((user) => {
          if (user.role) {
            rolesSet.add(user.role);
          }
        });
        setUniqueRoles(Array.from(rolesSet));

        setUsers(response.data || []);
        setLoading(false);
      } catch (err) {
        setError('Failed to load users');
        setLoading(false);
        console.error('Error fetching users:', err);
        console.error('Error details:', err.response?.data || err.message);
      }
    };

    fetchUsers();
  }, []);

  const filteredUsers = users.filter((user) => {
    const userName = user.userName ? user.userName.toLowerCase() : '';
    const email = user.email ? user.email.toLowerCase() : '';
    const role = user.role || '';
    const searchLower = searchTerm.toLowerCase();

    const matchesSearch =
      searchTerm === '' || userName.includes(searchLower) || email.includes(searchLower);

    const matchesRole = filterRole === 'all' || role === filterRole;

    return matchesSearch && matchesRole;
  });

  const userStats = useMemo(() => {
    if (!users.length) return [];

    const roleStats = {};
    users.forEach((user) => {
      const role = user.role || 'Unknown';
      roleStats[role] = (roleStats[role] || 0) + 1;
    });

    return Object.entries(roleStats).map(([role, count]) => ({
      name: role,
      value: count,
    }));
  }, [users]);

  // Updated chart colors to use brand colors
  const COLORS = BRAND_COLORS.charts;

  const requestSort = (key) => {
    let direction = 'ascending';
    if (sortConfig.key === key && sortConfig.direction === 'ascending') {
      direction = 'descending';
    }
    setSortConfig({ key, direction });
  };

  const sortedFilteredUsers = useMemo(() => {
    const sortableUsers = [...filteredUsers];
    if (sortConfig.key) {
      sortableUsers.sort((a, b) => {
        const aValue = a[sortConfig.key] || '';
        const bValue = b[sortConfig.key] || '';

        if (aValue < bValue) {
          return sortConfig.direction === 'ascending' ? -1 : 1;
        }
        if (aValue > bValue) {
          return sortConfig.direction === 'ascending' ? 1 : -1;
        }
        return 0;
      });
    }
    return sortableUsers;
  }, [filteredUsers, sortConfig]);

  const indexOfLastUser = currentPage * usersPerPage;
  const indexOfFirstUser = indexOfLastUser - usersPerPage;
  const currentUsers = sortedFilteredUsers.slice(indexOfFirstUser, indexOfLastUser);

  const paginate = (pageNumber) => setCurrentPage(pageNumber);

  const handleSelectAll = (e) => {
    if (e.target.checked) {
      setSelectedUsers(currentUsers.map((user) => user.userId));
    } else {
      setSelectedUsers([]);
    }
  };

  const handleSelectUser = (userId) => {
    if (selectedUsers.includes(userId)) {
      setSelectedUsers(selectedUsers.filter((id) => id !== userId));
    } else {
      setSelectedUsers([...selectedUsers, userId]);
    }
  };

  const handleBulkDelete = async () => {
    if (!selectedUsers.length) return;

    if (window.confirm(`Are you sure you want to delete ${selectedUsers.length} users?`)) {
      try {
        const deletePromises = selectedUsers.map((userId) =>
          apiClient.delete(getApiUrl(`user-auth/${userId}`))
        );

        await Promise.all(deletePromises);
        setUsers(users.filter((user) => !selectedUsers.includes(user.userId)));
        setSelectedUsers([]);

        setSuccessMessage(`Successfully deleted ${selectedUsers.length} users`);
        setShowSuccessAlert(true);
        setTimeout(() => setShowSuccessAlert(false), 3000);
      } catch (err) {
        console.error('Error deleting users:', err);
        alert('Failed to delete one or more users');
      }
    }
  };

  const openDeleteModal = (userId) => {
    setUserToDelete(userId);
    setIsDeleteModalOpen(true);
  };

  const handleDeleteUser = async () => {
    if (!userToDelete) return;

    try {
      await apiClient.delete(getApiUrl(`user-auth/${userToDelete}`));
      setUsers(users.filter((user) => user.userId !== userToDelete));
      setIsDeleteModalOpen(false);
      setUserToDelete(null);

      setSuccessMessage('User deleted successfully');
      setShowSuccessAlert(true);
      setTimeout(() => setShowSuccessAlert(false), 3000);
    } catch (err) {
      console.error('Error deleting user:', err);
      alert('Failed to delete user');
    }
  };

  const getRoleBadgeClass = (role) => {
    if (!role) return 'bg-gray-100 text-gray-800';

    const roleLower = role.toLowerCase();

    if (roleLower.includes('admin')) return 'bg-purple-100 text-purple-800';
    if (roleLower.includes('customer')) return 'bg-green-100 text-green-800';
    if (roleLower.includes('provider') || roleLower.includes('service'))
      return 'bg-blue-100 text-blue-800';

    return 'bg-gray-100 text-gray-800';
  };

  const getSortDirectionIndicator = (column) => {
    if (sortConfig.key !== column) return null;
    return sortConfig.direction === 'ascending' ? ' ↑' : ' ↓';
  };

  // Create animation variants for staggered animations
  const containerVariants = {
    hidden: { opacity: 0 },
    visible: {
      opacity: 1,
      transition: {
        staggerChildren: 0.05
      }
    }
  };

  const rowVariants = {
    hidden: { opacity: 0, y: 20 },
    visible: { 
      opacity: 1, 
      y: 0,
      transition: { type: "spring", stiffness: 300, damping: 24 }
    },
    hover: { 
      backgroundColor: "#f9fafb",
      transition: { duration: 0.2 }
    }
  };

  const cardVariants = {
    hover: {
      y: -5,
      boxShadow: "0 10px 15px -3px rgba(0, 0, 0, 0.1), 0 4px 6px -2px rgba(0, 0, 0, 0.05)",
      transition: { type: "spring", stiffness: 400, damping: 10 }
    }
  };

  if (loading) {
    return (
      <div className="min-h-screen bg-gray-100 p-4">
        <div className="container mx-auto">
          <div className="bg-white p-6 rounded-lg shadow-md mb-6 animate-pulse">
            <div className="h-8 bg-gray-200 rounded w-1/3 mb-6"></div>
            <div className="flex flex-col md:flex-row gap-4 mb-6">
              <div className="flex-1 h-10 bg-gray-200 rounded"></div>
              <div className="w-40 h-10 bg-gray-200 rounded"></div>
            </div>
            {Array(5)
              .fill(0)
              .map((_, idx) => (
                <div key={idx} className="h-16 bg-gray-200 rounded mb-3"></div>
              ))}
          </div>
        </div>
      </div>
    );
  }

  if (error)
    return (
      <motion.div
        initial={{ opacity: 0 }}
        animate={{ opacity: 1 }}
        className="text-center p-8 bg-red-50 text-red-500 rounded-lg shadow-md"
      >
        <svg
          className="mx-auto h-12 w-12 text-red-400"
          fill="none"
          viewBox="0 0 24 24"
          stroke="currentColor"
        >
          <path
            strokeLinecap="round"
            strokeLinejoin="round"
            strokeWidth={2}
            d="M12 8v4m0 4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z"
          />
        </svg>
        <h3 className="mt-2 text-lg font-medium">{error}</h3>
        <p className="mt-1 text-sm">Please try again later or contact support</p>
      </motion.div>
    );

  return (
    <div className="min-h-screen bg-gray-100 p-4">
      <div className="container mx-auto">
        <AnimatePresence>
          {showSuccessAlert && (
            <motion.div
              initial={{ opacity: 0, y: -20 }}
              animate={{ opacity: 1, y: 0 }}
              exit={{ opacity: 0, y: -20, transition: { duration: 0.2 } }}
              transition={{ type: "spring", stiffness: 500, damping: 30 }}
              className="mb-4 p-4 bg-green-100 text-[#495E57] rounded-md flex justify-between items-center shadow-md"
            >
              <div className="flex items-center">
                <svg
                  className="h-5 w-5 mr-2"
                  fill="none"
                  viewBox="0 0 24 24"
                  stroke="currentColor"
                >
                  <path
                    strokeLinecap="round"
                    strokeLinejoin="round"
                    strokeWidth={2}
                    d="M5 13l4 4L19 7"
                  />
                </svg>
                <span>{successMessage}</span>
              </div>
              <button onClick={() => setShowSuccessAlert(false)} className="text-[#495E57]">
                <svg
                  className="h-4 w-4"
                  fill="none"
                  viewBox="0 0 24 24"
                  stroke="currentColor"
                >
                  <path
                    strokeLinecap="round"
                    strokeLinejoin="round"
                    strokeWidth={2}
                    d="M6 18L18 6M6 6l12 12"
                  />
                </svg>
              </button>
            </motion.div>
          )}
        </AnimatePresence>

        <motion.div
          className="bg-white p-6 rounded-lg shadow-md mb-6"
          initial={{ opacity: 0, y: 20 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ 
            type: "spring",
            stiffness: 300,
            damping: 25,
            duration: 0.3 
          }}
        >
          <div className="mb-8 ">
            <Link 
              to="/adminHomePage" 
              className="inline-flex items-center px-4 py-2 bg-white text-[#495E57] font-medium rounded-lg border border-gray-200 hover:bg-gray-50 transition-all duration-200 shadow-sm group"
            >
              <svg xmlns="http://www.w3.org/2000/svg" className="h-5 w-5 mr-2 group-hover:-translate-x-1 transition-transform" viewBox="0 0 20 20" fill="currentColor">
                <path fillRule="evenodd" d="M9.707 14.707a1 1 0 01-1.414 0l-4-4a1 1 0 010-1.414l4-4a1 1 0 011.414 1.414L7.414 9H15a1 1 0 110 2H7.414l2.293 2.293a1 1 0 010 1.414z" clipRule="evenodd" />
              </svg>
              Back to Dashboard
            </Link>
          </div>
          <div className="flex justify-between items-center mb-6">
            <h1 className="text-2xl font-bold text-[#495E57]">User Management</h1>
          </div>

          <div className="mb-8 grid grid-cols-1 md:grid-cols-3 gap-6 ">
            <motion.div
              variants={cardVariants}
              whileHover="hover"
              className="bg-gradient-to-r from-[#F4CE14] to-[#f8e06b] text-[#495E57] p-6 rounded-lg shadow-md"
            >
              <div className="flex items-center">
                <div className="mr-4 bg-[#495E57] bg-opacity-10 rounded-full p-3">
                  <svg
                    xmlns="http://www.w3.org/2000/svg"
                    className="h-8 w-8 text-white"
                    fill="none"
                    viewBox="0 0 24 24"
                    stroke="currentColor"
                  >
                    <path
                      strokeLinecap="round"
                      strokeLinejoin="round"
                      strokeWidth={2}
                      d="M12 4.354a4 4 0 110 5.292M15 21H3v-1a6 6 0 0112 0v1zm0 0h6v-1a6 6 0 00-9-5.197M13 7a4 4 0 11-8 0 4 4 0 018 0z"
                    />
                  </svg>
                </div>
                <div>
                  <h3 className="text-lg font-medium text-[#495E57]">Total Users</h3>
                  <p className="text-3xl font-bold text-[#495E57]">{users.length}</p>
                </div>
              </div>
            </motion.div>

            <motion.div
              variants={cardVariants}
              whileHover="hover"
              className="col-span-2 bg-white p-6 rounded-lg shadow-md"
            >
              <h3 className="text-lg font-medium text-[#495E57] mb-4">User Role Distribution</h3>
              <div className="flex flex-col md:flex-row items-center">
                <div className="w-full md:w-1/2 h-64">
                  <ResponsiveContainer width="100%" height="100%">
                    <PieChart>
                      <Pie
                        data={userStats}
                        cx="50%"
                        cy="50%"
                        labelLine={false}
                        label={({ name, percent }) =>
                          `${name} (${(percent * 100).toFixed(0)}%)`
                        }
                        outerRadius={80}
                        fill="#8884d8"
                        dataKey="value"
                      >
                        {userStats.map((entry, index) => (
                          <Cell
                            key={`cell-${index}`}
                            fill={COLORS[index % COLORS.length]}
                          />
                        ))}
                      </Pie>
                      <Tooltip formatter={(value) => [`${value} users`, 'Count']} />
                    </PieChart>
                  </ResponsiveContainer>
                </div>
                <div className="w-full md:w-1/2 pl-0 md:pl-4 mt-4 md:mt-0">
                  <div className="space-y-2">
                    {userStats.map((entry, index) => (
                      <div key={`stat-${index}`} className="flex items-center">
                        <div
                          className="w-4 h-4 mr-2"
                          style={{
                            backgroundColor: COLORS[index % COLORS.length],
                          }}
                        ></div>
                        <span>
                          {entry.name}: <strong>{entry.value}</strong>{' '}
                          {entry.value === 1 ? 'user' : 'users'}
                        </span>
                      </div>
                    ))}
                  </div>
                </div>
              </div>
            </motion.div>
          </div>

          <div className="bg-gray-50 p-4 rounded-lg mb-6">
            <div className="flex flex-col md:flex-row gap-4 mb-4">
              <div className="flex-1 relative">
                <div className="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
                  <svg
                    xmlns="http://www.w3.org/2000/svg"
                    className="h-5 w-5 text-gray-400"
                    fill="none"
                    viewBox="0 0 24 24"
                    stroke="currentColor"
                  >
                    <path
                      strokeLinecap="round"
                      strokeLinejoin="round"
                      strokeWidth={2}
                      d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z"
                    />
                  </svg>
                </div>
                <input
                  type="text"
                  placeholder="Search by username or email"
                  className="w-full pl-10 p-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500 transition-all"
                  value={searchTerm}
                  onChange={(e) => setSearchTerm(e.target.value)}
                />
              </div>
              <div className="md:w-64">
                <div className="relative">
                  <select
                    className="appearance-none w-full p-3 border border-gray-300 rounded-lg bg-white focus:ring-2 focus:ring-blue-500 focus:border-blue-500 transition-all"
                    value={filterRole}
                    onChange={(e) => setFilterRole(e.target.value)}
                  >
                    {uniqueRoles.map((role) => (
                      <option key={role} value={role}>
                        {role === 'all' ? 'All Roles' : role}
                      </option>
                    ))}
                  </select>
                  <div className="pointer-events-none absolute inset-y-0 right-0 flex items-center px-2 text-gray-700">
                    <svg
                      className="fill-current h-4 w-4"
                      xmlns="http://www.w3.org/2000/svg"
                      viewBox="0 0 20 20"
                    >
                      <path
                        fillRule="evenodd"
                        d="M5.293 7.293a1 1 0 011.414 0L10 10.586l3.293-3.293a1 1 0 011.414 1.414l-4 4a1 1 0 01-1.414 0l-4-4a1 1 0 010-1.414z"
                        clipRule="evenodd"
                      />
                    </svg>
                  </div>
                </div>
              </div>
            </div>

            <div className="flex flex-wrap gap-2">
              {uniqueRoles
                .filter((role) => role !== 'all')
                .map((role) => (
                  <button
                    key={role}
                    onClick={() => setFilterRole(role)}
                    className={`px-3 py-1 rounded-full text-xs font-medium transition-colors ${
                      filterRole === role
                        ? 'bg-[#495E57] text-white'
                        : 'bg-gray-200 text-gray-700 hover:bg-[#F4CE14] hover:text-[#495E57]'
                    }`}
                  >
                    {role}
                  </button>
                ))}
              <button
                onClick={() => setFilterRole('all')}
                className={`px-3 py-1 rounded-full text-xs font-medium transition-colors ${
                  filterRole === 'all'
                    ? 'bg-[#495E57] text-white'
                    : 'bg-gray-200 text-gray-700 hover:bg-[#F4CE14] hover:text-[#495E57]'
                }`}
              >
                All Roles
              </button>
            </div>
          </div>

          <div className="bg-white rounded-lg shadow overflow-hidden">
            {selectedUsers.length > 0 && (
              <div className="bg-[#f7f3e3] p-3 flex items-center justify-between">
                <span className="text-sm text-[#495E57] font-medium">
                  {selectedUsers.length} users selected
                </span>
                <button
                  onClick={handleBulkDelete}
                  className="bg-red-500 hover:bg-red-600 text-white px-3 py-1 rounded text-sm transition-colors"
                >
                  Delete Selected
                </button>
              </div>
            )}
            {/* Fix the overflow handling on the table container */}
            <div className="overflow-x-auto overflow-y-hidden">
              <table className="min-w-full divide-y divide-gray-200 table-fixed">
                <thead className="bg-gray-50">
                  <tr>
                    <th scope="col" className="px-4 py-3 text-left">
                      <div className="flex items-center">
                        <input
                          type="checkbox"
                          className="h-4 w-4 text-blue-600 border-gray-300 rounded"
                          onChange={handleSelectAll}
                          checked={
                            selectedUsers.length === currentUsers.length &&
                            currentUsers.length > 0
                          }
                        />
                      </div>
                    </th>
                    <th
                      scope="col"
                      className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider cursor-pointer hover:bg-gray-100"
                      onClick={() => requestSort('userId')}
                    >
                      User ID {getSortDirectionIndicator('userId')}
                    </th>
                    <th
                      scope="col"
                      className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider cursor-pointer hover:bg-gray-100"
                      onClick={() => requestSort('userName')}
                    >
                      Username {getSortDirectionIndicator('userName')}
                    </th>
                    <th
                      scope="col"
                      className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider cursor-pointer hover:bg-gray-100"
                      onClick={() => requestSort('email')}
                    >
                      Email {getSortDirectionIndicator('email')}
                    </th>
                    <th
                      scope="col"
                      className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider cursor-pointer hover:bg-gray-100"
                      onClick={() => requestSort('role')}
                    >
                      Role {getSortDirectionIndicator('role')}
                    </th>
                    <th
                      scope="col"
                      className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider cursor-pointer hover:bg-gray-100"
                      onClick={() => requestSort('createdAt')}
                    >
                      Created At {getSortDirectionIndicator('createdAt')}
                    </th>
                    <th
                      scope="col"
                      className="px-6 py-3 text-right text-xs font-medium text-gray-500 uppercase tracking-wider"
                    >
                      Actions
                    </th>
                  </tr>
                </thead>
                <motion.tbody 
                  className="bg-white divide-y divide-gray-200"
                  variants={containerVariants}
                  initial="hidden"
                  animate="visible"
                >
                  {currentUsers.length > 0 ? (
                    currentUsers.map((user) => (
                      <motion.tr
                        key={user.userId}
                        variants={rowVariants}
                        whileHover="hover"
                        className="hover:bg-gray-50"
                      >
                        <td className="px-4 py-3 whitespace-nowrap">
                          <input
                            type="checkbox"
                            className="h-4 w-4 text-blue-600 border-gray-300 rounded"
                            checked={selectedUsers.includes(user.userId)}
                            onChange={() => handleSelectUser(user.userId)}
                          />
                        </td>
                        <td className="px-6 py-4 whitespace-nowrap text-sm font-medium text-gray-900">
                          {user.userId}
                        </td>
                        <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-900">
                          {user.userName || 'N/A'}
                        </td>
                        <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-900">
                          {user.email || 'N/A'}
                        </td>
                        <td className="px-6 py-4 whitespace-nowrap text-sm">
                          <span
                            className={`px-2 py-1 inline-flex text-xs leading-5 font-semibold rounded-full ${getRoleBadgeClass(
                              user.role
                            )}`}
                          >
                            {user.role || 'Unknown'}
                          </span>
                        </td>
                        <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-900">
                          {user.createdAt
                            ? new Date(user.createdAt).toLocaleDateString()
                            : 'N/A'}
                        </td>
                        <td className="px-6 py-4 whitespace-nowrap text-right text-sm font-medium">
                          <button
                            className="text-red-600 hover:text-red-900 hover:underline transition-colors"
                            onClick={() => openDeleteModal(user.userId)}
                          >
                            Delete
                          </button>
                        </td>
                      </motion.tr>
                    ))
                  ) : (
                    <tr>
                      <td
                        colSpan="7"
                        className="px-6 py-4 text-center text-sm text-gray-500"
                      >
                        No users found matching your criteria
                      </td>
                    </tr>
                  )}
                </motion.tbody>
              </table>
            </div>

            {sortedFilteredUsers.length > usersPerPage && (
              <div className="bg-white px-4 py-3 flex items-center justify-between border-t border-gray-200 sm:px-6">
                <div className="hidden sm:flex-1 sm:flex sm:items-center sm:justify-between">
                  <div>
                    <p className="text-sm text-gray-700">
                      Showing <span className="font-medium">{indexOfFirstUser + 1}</span> to{' '}
                      <span className="font-medium">
                        {indexOfLastUser > sortedFilteredUsers.length
                          ? sortedFilteredUsers.length
                          : indexOfLastUser}
                      </span>{' '}
                      of <span className="font-medium">{sortedFilteredUsers.length}</span>{' '}
                      results
                    </p>
                  </div>
                  <div>
                    <nav
                      className="relative z-0 inline-flex rounded-md shadow-sm -space-x-px"
                      aria-label="Pagination"
                    >
                      <button
                        onClick={() => setCurrentPage((prev) => Math.max(prev - 1, 1))}
                        disabled={currentPage === 1}
                        className={`relative inline-flex items-center px-2 py-2 rounded-l-md border border-gray-300 bg-white text-sm font-medium ${
                          currentPage === 1
                            ? 'text-gray-300 cursor-not-allowed'
                            : 'text-gray-500 hover:bg-gray-50'
                        }`}
                      >
                        <span className="sr-only">Previous</span>
                        <svg
                          className="h-5 w-5"
                          xmlns="http://www.w3.org/2000/svg"
                          viewBox="0 0 20 20"
                          fill="currentColor"
                        >
                          <path
                            fillRule="evenodd"
                            d="M12.707 5.293a1 1 0 010 1.414L9.414 10l3.293 3.293a1 1 0 01-1.414 1.414l-4-4a1 1 0 010-1.414l4-4a1 1 0 011.414 0z"
                            clipRule="evenodd"
                          />
                        </svg>
                      </button>

                      {Array.from({
                        length: Math.ceil(sortedFilteredUsers.length / usersPerPage),
                      }).map((_, idx) => (
                        <button
                          key={idx}
                          onClick={() => paginate(idx + 1)}
                          className={`relative inline-flex items-center px-4 py-2 border text-sm font-medium ${
                            currentPage === idx + 1
                              ? 'z-10 bg-[#F4CE14] border-[#F4CE14] text-[#495E57] font-bold'
                              : 'bg-white border-gray-300 text-gray-500 hover:bg-gray-50'
                          }`}
                        >
                          {idx + 1}
                        </button>
                      ))}

                      <button
                        onClick={() =>
                          setCurrentPage((prev) =>
                            Math.min(prev + 1, Math.ceil(sortedFilteredUsers.length / usersPerPage))
                          )
                        }
                        disabled={
                          currentPage === Math.ceil(sortedFilteredUsers.length / usersPerPage)
                        }
                        className={`relative inline-flex items-center px-2 py-2 rounded-r-md border border-gray-300 bg-white text-sm font-medium ${
                          currentPage === Math.ceil(sortedFilteredUsers.length / usersPerPage)
                            ? 'text-gray-300 cursor-not-allowed'
                            : 'text-gray-500 hover:bg-gray-50'
                        }`}
                      >
                        <span className="sr-only">Next</span>
                        <svg
                          className="h-5 w-5"
                          xmlns="http://www.w3.org/2000/svg"
                          viewBox="0 0 20 20"
                          fill="currentColor"
                        >
                          <path
                            fillRule="evenodd"
                            d="M7.293 14.707a1 1 0 010-1.414L10.586 10 7.293 6.707a1 1 0 011.414-1.414l4 4a1 1 0 010 1.414l-4-4a1 1 0 01-1.414 0z"
                            clipRule="evenodd"
                          />
                        </svg>
                      </button>
                    </nav>
                  </div>
                </div>
              </div>
            )}
          </div>
        </motion.div>
      </div>

      <BaseModal
        isOpen={isDeleteModalOpen}
        onClose={() => setIsDeleteModalOpen(false)}
        maxWidth="max-w-md"
      >
        <div className="bg-white rounded-lg p-6">
          <motion.div 
            className="mx-auto flex items-center justify-center h-12 w-12 rounded-full bg-red-100 mb-4"
            animate={{ 
              scale: [1, 1.1, 1],
              rotate: [0, 10, -10, 0] 
            }}
            transition={{ 
              duration: 0.5,
              delay: 0.2,
              ease: "easeInOut"
            }}
          >
            <svg
              className="h-6 w-6 text-red-600"
              fill="none"
              viewBox="0 0 24 24"
              stroke="currentColor"
            >
              <path
                strokeLinecap="round"
                strokeLinejoin="round"
                strokeWidth="2"
                d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16"
              />
            </svg>
          </motion.div>
          <motion.h3 
            className="text-lg leading-6 font-medium text-gray-900 text-center mb-2"
            initial={{ opacity: 0 }}
            animate={{ opacity: 1 }}
            transition={{ delay: 0.3 }}
          >
            Delete User
          </motion.h3>
          <motion.div 
            className="mt-2"
            initial={{ opacity: 0 }}
            animate={{ opacity: 1 }}
            transition={{ delay: 0.4 }}
          >
            <p className="text-sm text-gray-500 text-center">
              Are you sure you want to delete this user? This action cannot be undone.
            </p>
          </motion.div>
          <motion.div 
            className="mt-5 sm:mt-6 grid grid-cols-2 gap-3"
            initial={{ opacity: 0 }}
            animate={{ opacity: 1 }}
            transition={{ delay: 0.5 }}
          >
            <motion.button
              type="button"
              className="w-full inline-flex justify-center rounded-md border border-gray-300 shadow-sm px-4 py-2 bg-white text-base font-medium text-[#495E57] hover:bg-[#F4CE14] focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-[#F4CE14]"
              onClick={() => setIsDeleteModalOpen(false)}
              whileHover={{ scale: 1.03 }}
              whileTap={{ scale: 0.97 }}
            >
              Cancel
            </motion.button>
            <motion.button
              type="button"
              className="w-full inline-flex justify-center rounded-md border border-transparent shadow-sm px-4 py-2 bg-red-600 text-base font-medium text-white hover:bg-red-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-red-500"
              onClick={handleDeleteUser}
              whileHover={{ scale: 1.03 }}
              whileTap={{ scale: 0.97 }}
            >
              Delete
            </motion.button>
          </motion.div>
        </div>
      </BaseModal>
    </div>
  );
};

export default UserManagement;
