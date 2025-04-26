import React, { useState, useEffect } from 'react';
import axios from 'axios';
import { Link } from 'react-router-dom';

const CategoryManagement = () => {
  const [newCategory, setNewCategory] = useState({ categoryName: '', description: '' });
  const [categories, setCategories] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [editingCategory, setEditingCategory] = useState(null);

  useEffect(() => {
    fetchCategories();
  }, []);

  const fetchCategories = async () => {
    try {
      setLoading(true);
      const token = localStorage.getItem('authToken') || sessionStorage.getItem('authToken');
      
      if (!token) {
        throw new Error('Authentication token not found');
      }
      
      const headers = { 'Authorization': `Bearer ${token}` };
      
      const response = await axios.get('/api/service-categories/getAll', { headers });
      setCategories(response.data || []);
      setLoading(false);
    } catch (err) {
      setError('Failed to load categories');
      setLoading(false);
      console.error('Error fetching categories:', err);
    }
  };

  const handleInputChange = (e) => {
    const { name, value } = e.target;
    setNewCategory({ ...newCategory, [name]: value });
  };

  const handleEditInputChange = (e) => {
    const { name, value } = e.target;
    setEditingCategory({ ...editingCategory, [name]: value });
  };

  const handleCreateCategory = async (e) => {
    e.preventDefault();
    try {
      const token = localStorage.getItem('authToken') || sessionStorage.getItem('authToken');
      const headers = { 'Authorization': `Bearer ${token}` };
      
      console.log("Creating category with data:", newCategory);
      
      await axios.post('/api/service-categories/create', newCategory, { headers });
      setNewCategory({ categoryName: '', description: '' });
      fetchCategories();
    } catch (err) {
      console.error('Error creating category:', err);
      alert('Failed to create category');
    }
  };

  const handleEditCategory = (category) => {
    setEditingCategory({ ...category });
  };

  const handleUpdateCategory = async () => {
    try {
      const token = localStorage.getItem('authToken') || sessionStorage.getItem('authToken');
      const headers = { 'Authorization': `Bearer ${token}` };
      
      await axios.put(`/api/service-categories/update/${editingCategory.categoryId}`, editingCategory, { headers });
      setEditingCategory(null);
      fetchCategories();
    } catch (err) {
      console.error('Error updating category:', err);
      alert('Failed to update category');
    }
  };

  const handleDeleteCategory = async (categoryId) => {
    if (window.confirm('Are you sure you want to delete this category?')) {
      try {
        const token = localStorage.getItem('authToken') || sessionStorage.getItem('authToken');
        const headers = { 'Authorization': `Bearer ${token}` };
        
        await axios.delete(`/api/service-categories/delete/${categoryId}`, { headers });
        fetchCategories();
      } catch (err) {
        console.error('Error deleting category:', err);
        alert('Failed to delete category');
      }
    }
  };

  if (loading && categories.length === 0) return <div className="text-center p-8">Loading categories...</div>;
  if (error) return <div className="text-center p-8 text-red-500">{error}</div>;

  return (
    <div className="min-h-screen bg-gray-100 p-4">
      <div className="container mx-auto">
        <div className="bg-white p-6 rounded-lg shadow-md mb-6">
          <div className="flex justify-between items-center mb-6">
            <h1 className="text-2xl font-bold text-[#495E57]">Service Category Management</h1>
            <Link to="/adminHomePage" className="text-[#495E57] hover:underline flex items-center">
              <svg xmlns="http://www.w3.org/2000/svg" className="h-5 w-5 mr-1" viewBox="0 0 20 20" fill="currentColor">
                <path fillRule="evenodd" d="M9.707 14.707a1 1 0 01-1.414 0l-4-4a1 1 0 010-1.414l4-4a1 1 0 011.414 1.414L7.414 9H15a1 1 0 110 2H7.414l2.293 2.293a1 1 0 010 1.414z" clipRule="evenodd" />
              </svg>
              Back to Dashboard
            </Link>
          </div>

          {/* Create New Category Form */}
          <div className="bg-gray-50 p-4 rounded mb-6">
            <h2 className="text-lg font-medium mb-4">Create New Category</h2>
            <form onSubmit={handleCreateCategory} className="space-y-4">
              <div>
                <label htmlFor="categoryName" className="block text-sm font-medium text-gray-700">Category Name</label>
                <input
                  type="text"
                  id="categoryName"
                  name="categoryName"
                  value={newCategory.categoryName}
                  onChange={handleInputChange}
                  className="mt-1 p-2 w-full border border-gray-300 rounded"
                  required
                />
              </div>
              <div>
                <label htmlFor="description" className="block text-sm font-medium text-gray-700">Description</label>
                <textarea
                  id="description"
                  name="description"
                  value={newCategory.description}
                  onChange={handleInputChange}
                  rows="2"
                  className="mt-1 p-2 w-full border border-gray-300 rounded"
                  required
                />
              </div>
              <button
                type="submit"
                className="bg-[#495E57] text-white px-4 py-2 rounded hover:bg-[#3a4a45]"
              >
                Create Category
              </button>
            </form>
          </div>

          {/* Categories List */}
          <div>
            <h2 className="text-lg font-medium mb-4">Service Categories</h2>
            <div className="overflow-x-auto">
              <table className="min-w-full divide-y divide-gray-200">
                <thead className="bg-gray-50">
                  <tr>
                    <th scope="col" className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">ID</th>
                    <th scope="col" className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Name</th>
                    <th scope="col" className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Description</th>
                    <th scope="col" className="px-6 py-3 text-right text-xs font-medium text-gray-500 uppercase tracking-wider">Actions</th>
                  </tr>
                </thead>
                <tbody className="bg-white divide-y divide-gray-200">
                  {categories.length > 0 ? (
                    categories.map((category) => (
                      <tr key={category.categoryId} className="hover:bg-gray-50">
                        <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-900">{category.categoryId}</td>
                        <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-900">{category.categoryName}</td>
                        <td className="px-6 py-4 text-sm text-gray-900">{category.description}</td>
                        <td className="px-6 py-4 whitespace-nowrap text-right text-sm font-medium">
                          <button 
                            className="text-indigo-600 hover:text-indigo-900 mr-3"
                            onClick={() => handleEditCategory(category)}
                          >
                            Edit
                          </button>
                          <button 
                            className="text-red-600 hover:text-red-900"
                            onClick={() => handleDeleteCategory(category.categoryId)}
                          >
                            Delete
                          </button>
                        </td>
                      </tr>
                    ))
                  ) : (
                    <tr>
                      <td colSpan="4" className="px-6 py-4 text-center text-sm text-gray-500">No categories found</td>
                    </tr>
                  )}
                </tbody>
              </table>
            </div>
          </div>
        </div>

        {/* Edit Category Modal */}
        {editingCategory && (
          <div className="fixed inset-0 flex items-center justify-center bg-black bg-opacity-50 z-50">
            <div className="bg-white p-6 rounded-lg shadow-xl max-w-lg w-full">
              <h2 className="text-xl font-bold mb-4">Edit Category</h2>
              <form className="space-y-4">
                <div>
                  <label htmlFor="edit-categoryName" className="block text-sm font-medium text-gray-700">Category Name</label>
                  <input
                    type="text"
                    id="edit-categoryName"
                    name="categoryName"
                    value={editingCategory.categoryName}
                    onChange={handleEditInputChange}
                    className="mt-1 p-2 w-full border border-gray-300 rounded"
                    required
                  />
                </div>
                <div>
                  <label htmlFor="edit-description" className="block text-sm font-medium text-gray-700">Description</label>
                  <textarea
                    id="edit-description"
                    name="description"
                    value={editingCategory.description}
                    onChange={handleEditInputChange}
                    rows="2"
                    className="mt-1 p-2 w-full border border-gray-300 rounded"
                    required
                  />
                </div>
                <div className="flex justify-end space-x-3">
                  <button
                    type="button"
                    onClick={() => setEditingCategory(null)}
                    className="bg-gray-300 text-gray-800 px-4 py-2 rounded hover:bg-gray-400"
                  >
                    Cancel
                  </button>
                  <button
                    type="button"
                    onClick={handleUpdateCategory}
                    className="bg-[#495E57] text-white px-4 py-2 rounded hover:bg-[#3a4a45]"
                  >
                    Update
                  </button>
                </div>
              </form>
            </div>
          </div>
        )}
      </div>
    </div>
  );
};

export default CategoryManagement;
