import React, { useState, useEffect } from 'react';
import axios from 'axios';
import { Link } from 'react-router-dom';

const AdminUserManagement = () => {
  const [users, setUsers] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [searchTerm, setSearchTerm] = useState('');
  const [filteredUsers, setFilteredUsers] = useState([]);
  const [selectedRole, setSelectedRole] = useState('All');

  useEffect(() => {
    const fetchUsers = async () => {
      try {
        setLoading(true);
        const token = localStorage.getItem('authToken') || sessionStorage.getItem('authToken');
        
        if (!token) {
          throw new Error('Authentication token not found');
        }
        
        const headers = { 'Authorization': `Bearer ${token}` };
        const response = await axios.get('/api/user-auth/getAll', { headers });
        
        setUsers(response.data);
        setFilteredUsers(response.data);
        setLoading(false);
      } catch (err) {
        setError('Failed to load users');
        setLoading(false);
        console.error('Error fetching users:', err);
      }
    };

    fetchUsers();
  }, []);

  // Filter users based on search term and role
  useEffect(() => {
    const results = users.filter(user => {
      const matchesSearch = user.userName.toLowerCase().includes(searchTerm.toLowerCase()) || 
                        user.email.toLowerCase().includes(searchTerm.toLowerCase());
      const matchesRole = selectedRole === 'All' || user.role === selectedRole;
      return matchesSearch && matchesRole;
    });
    
    setFilteredUsers(results);
  }, [searchTerm, selectedRole, users]);

  const handleDeleteUser = async (userId) => {
    if (window.confirm('Are you sure you want to delete this user? This action cannot be undone.')) {
      try {
        const token = localStorage.getItem('authToken') || sessionStorage.getItem('authToken');
        const headers = { 'Authorization': `Bearer ${token}` };
        
        await axios.delete(`/api/user-auth/${userId}`, { headers });
        
        // Remove user from state
        setUsers(users.filter(user => user.userId !== userId));
        setFilteredUsers(filteredUsers.filter(user => user.userId !== userId));
        
        alert('User deleted successfully');
      } catch (err) {
        console.error('Error deleting user:', err);
        alert('Failed to delete user');
      }
    }
  };

  return (
    <div className="min-h-screen bg-gray-100 py-8">
      <div className="container mx-auto px-4">
        <div className="flex justify-between items-center mb-6">
          <h1 className="text-3xl font-bold text-[#495E57]">User Management</h1>
          <Link to="/admin" className="bg-gray-500 text-white px-4 py-2 rounded hover:bg-gray-600 transition-colors">
            Back to Dashboard
          </Link>
        </div>
        
        <div className="bg-white rounded-lg shadow-md p-6 mb-6">
          <div className="flex flex-col md:flex-row justify-between mb-4">
            <div className="mb-4 md:mb-0 md:w-1/3">
              <input
                type="text"
                placeholder="Search users by name or email"
                className="w-full px-4 py-2 border rounded-lg focus:outline-none focus:ring-2 focus:ring-[#495E57]"
                value={searchTerm}
                onChange={(e) => setSearchTerm(e.target.value)}
              />
            </div>
            
            <div className="md:w-1/4">
              <select 
                className="w-full px-4 py-2 border rounded-lg focus:outline-none focus:ring-2 focus:ring-[#495E57]"
                value={selectedRole}
                onChange={(e) => setSelectedRole(e.target.value)}
              >
                <option value="All">All Roles</option>
                <option value="Customer">Customers</option>
                <option value="ServiceProvider">Service Providers</option>
                <option value="Admin">Administrators</option>
              </select>
            </div>
          </div>
          
          {loading ? (
            <div className="text-center py-4">Loading users...</div>
          ) : error ? (
            <div className="text-center text-red-500 py-4">{error}</div>
          ) : (
            <div className="overflow-x-auto">
              <table className="min-w-full bg-white">
                <thead className="bg-gray-100">
                  <tr>
                    <th className="py-3 px-4 text-left">User ID</th>
                    <th className="py-3 px-4 text-left">Username</th>
                    <th className="py-3 px-4 text-left">Email</th>
                    <th className="py-3 px-4 text-left">Role</th>
                    <th className="py-3 px-4 text-left">Created</th>
                    <th className="py-3 px-4 text-left">Actions</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-gray-200">
                  {filteredUsers.map(user => (
                    <tr key={user.userId} className="hover:bg-gray-50">
                      <td className="py-3 px-4">{user.userId}</td>
                      <td className="py-3 px-4">{user.userName}</td>
                      <td className="py-3 px-4">{user.email}</td>
                      <td className="py-3 px-4">
                        <span className={`px-2 py-1 rounded-full text-xs ${
                          user.role === 'Admin' ? 'bg-purple-100 text-purple-800' :
                          user.role === 'ServiceProvider' ? 'bg-blue-100 text-blue-800' :
                          'bg-green-100 text-green-800'
                        }`}>
                          {user.role}
                        </span>
                      </td>
                      <td className="py-3 px-4">{user.createdAt ? new Date(user.createdAt).toLocaleDateString() : 'N/A'}</td>
                      <td className="py-3 px-4">
                        <div className="flex space-x-2">
                          <Link to={`/admin/users/edit/${user.userId}`} className="text-blue-600 hover:text-blue-800">
                            <svg xmlns="http://www.w3.org/2000/svg" className="h-5 w-5" viewBox="0 0 20 20" fill="currentColor">
                              <path d="M13.586 3.586a2 2 0 112.828 2.828l-.793.793-2.828-2.828.793-.793zM11.379 5.793L3 14.172V17h2.828l8.38-8.379-2.83-2.828z" />
                            </svg>
                          </Link>
                          <button onClick={() => handleDeleteUser(user.userId)} className="text-red-600 hover:text-red-800">
                            <svg xmlns="http://www.w3.org/2000/svg" className="h-5 w-5" viewBox="0 0 20 20" fill="currentColor">
                              <path fillRule="evenodd" d="M9 2a1 1 0 00-.894.553L7.382 4H4a1 1 0 000 2v10a2 2 0 002 2h8a2 2 0 002-2V6a1 1 0 100-2h-3.382l-.724-1.447A1 1 0 0011 2H9zM7 8a1 1 0 012 0v6a1 1 0 11-2 0V8zm5-1a1 1 0 00-1 1v6a1 1 0 102 0V8a1 1 0 00-1-1z" clipRule="evenodd" />
                            </svg>
                          </button>
                        </div>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
              
              {filteredUsers.length === 0 && (
                <div className="text-center py-4 text-gray-500">No users found matching your criteria.</div>
              )}
            </div>
          )}
        </div>
      </div>
    </div>
  );
};

export default AdminUserManagement;
