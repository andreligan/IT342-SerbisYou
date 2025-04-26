import React, { useState, useEffect } from 'react';
import axios from 'axios';
import { Link } from 'react-router-dom';
import { motion, AnimatePresence } from 'framer-motion';

const CategoryManagement = () => {
  const [newCategory, setNewCategory] = useState({ categoryName: '', description: '' });
  const [categories, setCategories] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [editingCategory, setEditingCategory] = useState(null);
  const [toast, setToast] = useState({ show: false, message: '', type: '' });
  const [expandedRows, setExpandedRows] = useState({});

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
      
      setToast({
        show: true,
        message: 'Category created successfully!',
        type: 'success'
      });
      
      setTimeout(() => setToast({ show: false, message: '', type: '' }), 3000);
    } catch (err) {
      console.error('Error creating category:', err);
      setToast({
        show: true,
        message: 'Failed to create category.',
        type: 'error'
      });
      setTimeout(() => setToast({ show: false, message: '', type: '' }), 3000);
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
      
      setToast({
        show: true,
        message: 'Category updated successfully!',
        type: 'success'
      });
      
      setTimeout(() => setToast({ show: false, message: '', type: '' }), 3000);
    } catch (err) {
      console.error('Error updating category:', err);
      setToast({
        show: true,
        message: 'Failed to update category.',
        type: 'error'
      });
      setTimeout(() => setToast({ show: false, message: '', type: '' }), 3000);
    }
  };

  const handleDeleteCategory = async (categoryId) => {
    if (window.confirm('Are you sure you want to delete this category?')) {
      try {
        const token = localStorage.getItem('authToken') || sessionStorage.getItem('authToken');
        const headers = { 'Authorization': `Bearer ${token}` };
        
        await axios.delete(`/api/service-categories/delete/${categoryId}`, { headers });
        fetchCategories();
        
        setToast({
          show: true,
          message: 'Category deleted successfully!',
          type: 'success'
        });
        
        setTimeout(() => setToast({ show: false, message: '', type: '' }), 3000);
      } catch (err) {
        console.error('Error deleting category:', err);
        setToast({
          show: true,
          message: 'Failed to delete category.',
          type: 'error'
        });
        setTimeout(() => setToast({ show: false, message: '', type: '' }), 3000);
      }
    }
  };

  const toggleRowExpand = (categoryId) => {
    setExpandedRows({
      ...expandedRows,
      [categoryId]: !expandedRows[categoryId]
    });
  };

  if (loading && categories.length === 0) {
    return (
      <div className="min-h-screen bg-gray-100 flex justify-center items-center p-4">
        <div className="animate-pulse flex flex-col items-center">
          <div className="h-32 w-32 rounded-full bg-[#F4CE14] opacity-70"></div>
          <div className="mt-4 h-8 w-56 bg-[#495E57] rounded opacity-70"></div>
          <div className="mt-2 h-6 w-32 bg-[#495E57] rounded opacity-50"></div>
        </div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="min-h-screen bg-gray-100 flex justify-center items-center p-4">
        <div className="max-w-md p-8 bg-white rounded-lg shadow-lg text-center">
          <div className="inline-flex items-center justify-center h-16 w-16 rounded-full bg-red-100 mb-6">
            <svg className="h-8 w-8 text-red-500" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z" />
            </svg>
          </div>
          <h3 className="text-xl font-medium text-gray-800">{error}</h3>
          <p className="text-gray-500 mt-2">Please try again later or contact support.</p>
          <button 
            onClick={() => window.location.reload()} 
            className="mt-6 px-4 py-2 bg-[#495E57] text-white rounded-md hover:bg-[#3a4a45]"
          >
            Retry
          </button>
        </div>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-gray-100 p-4">
      <div className="container mx-auto">
        <AnimatePresence>
          {toast.show && (
            <motion.div
              initial={{ opacity: 0, y: -50 }}
              animate={{ opacity: 1, y: 0 }}
              exit={{ opacity: 0, y: -50 }}
              className={`fixed top-5 right-5 z-50 p-4 rounded-lg shadow-lg ${
                toast.type === 'success' ? 'bg-green-100 text-green-800 border-l-4 border-green-500' : 
                toast.type === 'error' ? 'bg-red-100 text-red-800 border-l-4 border-red-500' : 
                'bg-blue-100 text-blue-800 border-l-4 border-blue-500'
              } flex items-center`}
            >
              {toast.type === 'success' ? (
                <svg className="w-5 h-5 mr-2" fill="currentColor" viewBox="0 0 20 20">
                  <path fillRule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zm3.707-9.293a1 1 0 00-1.414-1.414L9 10.586 7.707 9.293a1 1 0 00-1.414 1.414l2 2a1 1 0 001.414 0l4-4z" clipRule="evenodd" />
                </svg>
              ) : (
                <svg className="w-5 h-5 mr-2" fill="currentColor" viewBox="0 0 20 20">
                  <path fillRule="evenodd" d="M8.257 3.099c.765-1.36 2.722-1.36 3.486 0l5.58 9.92c.75 1.334-.213 2.98-1.742 2.98H4.42c-1.53 0-2.493-1.646-1.743-2.98l5.58-9.92zM11 13a1 1 0 11-2 0 1 1 0 012 0zm-1-8a1 1 0 00-1 1v3a1 1 0 002 0V6a1 1 0 00-1-1z" clipRule="evenodd" />
                </svg>
              )}
              <span>{toast.message}</span>
            </motion.div>
          )}
        </AnimatePresence>

        <motion.div 
          initial={{ opacity: 0, y: 20 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ duration: 0.5 }}
          className="mb-10"
        >
          {/* Header section with asymmetrical design */}
          <div className="relative overflow-hidden">
            {/* Background elements */}
            <div className="absolute top-0 right-0 w-1/3 h-64 bg-[#F4CE14]/10 rounded-bl-[80px] -z-10"></div>
            <div className="absolute top-20 left-40 w-16 h-16 bg-[#495E57]/5 rounded-full -z-10"></div>
            <div className="absolute bottom-10 right-20 w-24 h-24 bg-[#F4CE14]/10 rounded-full -z-10"></div>
            
            <div className="container mx-auto px-4 py-8">
              {/* Back button */}
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
              
              {/* Header content */}
              <div className="grid grid-cols-1 md:grid-cols-3 gap-8">
                <div className="md:col-span-2">
                  <h1 className="text-4xl font-bold text-[#495E57] mb-4">Service Categories</h1>
                  <p className="text-gray-600 text-lg mb-8 max-w-2xl">
                    Organize and manage your platform's service offerings through intuitive categorization.
                  </p>
                  
                  {/* Stats row */}
                  <div className="flex flex-wrap gap-8 items-center mt-4">
                    <div className="flex items-baseline">
                      <span className="text-5xl font-bold text-[#495E57]">{categories.length}</span>
                      <span className="text-xl text-gray-500 ml-2">Categories</span>
                    </div>
                    
                    <div className="h-12 w-px bg-gray-200"></div>
                    
                    <div className="flex items-center space-x-2">
                      <div className="flex -space-x-2">
                        {[...Array(Math.min(3, categories.length))].map((_, i) => (
                          <div key={i} className={`w-10 h-10 rounded-full flex items-center justify-center text-white text-xs font-bold shadow-sm border-2 border-white`} style={{ backgroundColor: i === 0 ? '#495E57' : i === 1 ? '#F4CE14' : '#EE9972' }}>
                            {categories[i]?.categoryName?.charAt(0) || '?'}
                          </div>
                        ))}
                        {categories.length > 3 && (
                          <div className="w-10 h-10 rounded-full bg-gray-200 flex items-center justify-center text-xs font-bold border-2 border-white">
                            +{categories.length - 3}
                          </div>
                        )}
                      </div>
                      <span className="text-gray-600">Active</span>
                    </div>
                  </div>
                </div>
                
                {/* Visual element */}
                <div className="flex justify-center md:justify-end">
                  <div className="w-32 h-32 bg-gradient-to-br from-[#F4CE14] to-[#f8db63] rounded-full shadow-lg flex items-center justify-center p-8">
                    <svg xmlns="http://www.w3.org/2000/svg" className="h-full w-full text-white" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                      <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={1.5} d="M19 11H5m14 0a2 2 0 012 2v6a2 2 0 01-2 2H5a2 2 0 01-2-2v-6a2 2 0 012-2m14 0V9a2 2 0 00-2-2M5 11V9a2 2 0 012-2m0 0V5a2 2 0 012-2h6a2 2 0 012 2v2M7 7h10" />
                    </svg>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </motion.div>

        <div className="grid md:grid-cols-3 gap-8 mb-12 px-4">
          {/* Form card with more white space */}
          <motion.div 
            initial={{ opacity: 0, x: -20 }}
            animate={{ opacity: 1, x: 0 }}
            transition={{ duration: 0.5, delay: 0.2 }}
            className="bg-white rounded-2xl shadow-md overflow-hidden"
          >
            <div className="bg-gradient-to-r from-[#F4CE14] to-[#f5d84a] p-6">
              <h2 className="text-2xl font-bold text-[#495E57]">Create Category</h2>
              <p className="text-sm text-[#495E57]/80 mt-1">Add a new service category</p>
            </div>
            <div className="p-8">
              <form onSubmit={handleCreateCategory} className="space-y-4">
                <div>
                  <label htmlFor="categoryName" className="block text-sm font-medium text-gray-700 mb-1">Category Name</label>
                  <input
                    type="text"
                    id="categoryName"
                    name="categoryName"
                    value={newCategory.categoryName}
                    onChange={handleInputChange}
                    className="w-full p-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-[#495E57] focus:border-[#495E57] transition-all"
                    placeholder="e.g. Plumbing, Electrical"
                    required
                  />
                </div>
                <div>
                  <label htmlFor="description" className="block text-sm font-medium text-gray-700 mb-1">Description</label>
                  <textarea
                    id="description"
                    name="description"
                    value={newCategory.description}
                    onChange={handleInputChange}
                    rows="4"
                    className="w-full p-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-[#495E57] focus:border-[#495E57] transition-all"
                    placeholder="Describe this service category..."
                    required
                  />
                </div>
                <button
                  type="submit"
                  className="w-full bg-[#495E57] text-white py-2 px-4 rounded-lg hover:bg-[#3a4a45] transition-colors flex items-center justify-center"
                >
                  <svg xmlns="http://www.w3.org/2000/svg" className="h-5 w-5 mr-2" viewBox="0 0 20 20" fill="currentColor">
                    <path fillRule="evenodd" d="M10 5a1 1 0 011 1v3h3a1 1 0 110 2h-3v3a1 1 0 11-2 0v-3H6a1 1 0 110-2h3V6a1 1 0 011-1z" clipRule="evenodd" />
                  </svg>
                  Create Category
                </button>
              </form>
            </div>
          </motion.div>

          {/* Categories list with more white space */}
          <motion.div 
            initial={{ opacity: 0, x: 20 }}
            animate={{ opacity: 1, x: 0 }}
            transition={{ duration: 0.5, delay: 0.2 }}
            className="md:col-span-2 bg-white rounded-2xl shadow-md overflow-hidden"
          >
            <div className="bg-[#495E57] p-6">
              <div className="flex justify-between items-center">
                <h2 className="text-xl font-bold text-white">Service Categories</h2>
                {/* <div className="bg-[#F4CE14] rounded-full h-8 w-8 flex items-center justify-center text-[#495E57] font-bold">
                  {categories.length}
                </div> */}
              </div>
              <p className="text-sm text-gray-200 mt-1">All service categories in your system</p>
            </div>
            <div className="p-6 overflow-x-auto">
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
                      <React.Fragment key={category.categoryId}>
                        <tr className={`group hover:bg-gray-50 cursor-pointer ${expandedRows[category.categoryId] ? 'bg-gray-50' : ''}`}>
                          <td className="px-6 py-4 whitespace-nowrap text-sm font-medium text-gray-900">{category.categoryId}</td>
                          <td className="px-6 py-4 whitespace-nowrap text-sm font-medium text-[#495E57]" onClick={() => toggleRowExpand(category.categoryId)}>
                            <div className="flex items-center">
                              <div className="h-2 w-2 bg-[#F4CE14] rounded-full mr-2"></div>
                              {category.categoryName}
                              <svg 
                                xmlns="http://www.w3.org/2000/svg" 
                                className={`h-4 w-4 ml-1 transition-transform ${expandedRows[category.categoryId] ? 'transform rotate-180' : ''}`} 
                                viewBox="0 0 20 20" 
                                fill="currentColor"
                              >
                                <path fillRule="evenodd" d="M5.293 7.293a1 1 0 011.414 0L10 10.586l3.293-3.293a1 1 0 111.414 1.414l-4 4a1 1 0 01-1.414 0l-4-4a1 1 0 010-1.414z" clipRule="evenodd" />
                              </svg>
                            </div>
                          </td>
                          <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                            {category.description?.length > 30
                              ? `${category.description.substring(0, 30)}...`
                              : category.description}
                          </td>
                          <td className="px-6 py-4 whitespace-nowrap text-right text-sm font-medium">
                            <button 
                              className="text-indigo-600 hover:text-indigo-900 bg-indigo-100 px-2 py-1 rounded-md mr-2 transition-colors"
                              onClick={() => handleEditCategory(category)}
                            >
                              Edit
                            </button>
                            <button 
                              className="text-red-600 hover:text-red-900 bg-red-100 px-2 py-1 rounded-md transition-colors"
                              onClick={() => handleDeleteCategory(category.categoryId)}
                            >
                              Delete
                            </button>
                          </td>
                        </tr>
                        {expandedRows[category.categoryId] && (
                          <tr className="bg-gray-50">
                            <td colSpan="4" className="px-6 py-4">
                              <div className="text-sm text-gray-700">
                                <strong>Full Description:</strong>
                                <p className="mt-1">{category.description}</p>
                              </div>
                            </td>
                          </tr>
                        )}
                      </React.Fragment>
                    ))
                  ) : (
                    <tr>
                      <td colSpan="4" className="px-6 py-8 text-center">
                        <div className="text-center">
                          <svg xmlns="http://www.w3.org/2000/svg" className="mx-auto h-12 w-12 text-gray-400" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={1} d="M9 5H7a2 2 0 00-2 2v12a2 2 0 002 2h10a2 2 0 002-2V7a2 2 0 00-2-2h-2M9 5a2 2 0 002 2h2a2 2 0 002-2M9 5a2 2 0 012-2h2a2 2 0 012 2" />
                          </svg>
                          <h3 className="mt-2 text-sm font-medium text-gray-900">No categories found</h3>
                          <p className="mt-1 text-sm text-gray-500">Get started by creating a new category.</p>
                        </div>
                      </td>
                    </tr>
                  )}
                </tbody>
              </table>
            </div>
          </motion.div>
        </div>

        <AnimatePresence>
          {editingCategory && (
            <motion.div
              initial={{ opacity: 0 }}
              animate={{ opacity: 1 }}
              exit={{ opacity: 0 }}
              className="fixed inset-0 flex items-center justify-center bg-black bg-opacity-50 z-50"
              onClick={() => setEditingCategory(null)}
            >
              <motion.div
                initial={{ scale: 0.9, opacity: 0 }}
                animate={{ scale: 1, opacity: 1 }}
                exit={{ scale: 0.9, opacity: 0 }}
                className="bg-white p-6 rounded-lg shadow-xl max-w-lg w-full m-4"
                onClick={e => e.stopPropagation()}
              >
                <div className="flex justify-between items-center mb-4">
                  <h2 className="text-xl font-bold text-[#495E57]">Edit Category</h2>
                  <button 
                    onClick={() => setEditingCategory(null)}
                    className="text-gray-500 hover:text-gray-700"
                  >
                    <svg xmlns="http://www.w3.org/2000/svg" className="h-6 w-6" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                      <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M6 18L18 6M6 6l12 12" />
                    </svg>
                  </button>
                </div>
                <form className="space-y-4">
                  <div>
                    <label htmlFor="edit-categoryName" className="block text-sm font-medium text-gray-700">Category Name</label>
                    <input
                      type="text"
                      id="edit-categoryName"
                      name="categoryName"
                      value={editingCategory.categoryName}
                      onChange={handleEditInputChange}
                      className="mt-1 p-2 w-full border border-gray-300 rounded-lg focus:ring-2 focus:ring-[#495E57] focus:border-[#495E57]"
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
                      rows="5"
                      className="mt-1 p-2 w-full border border-gray-300 rounded-lg focus:ring-2 focus:ring-[#495E57] focus:border-[#495E57]"
                      required
                    />
                  </div>
                  <div className="flex justify-end space-x-3 pt-4 border-t border-gray-200">
                    <button
                      type="button"
                      onClick={() => setEditingCategory(null)}
                      className="px-4 py-2 bg-gray-200 text-gray-800 rounded-lg hover:bg-gray-300 transition-colors"
                    >
                      Cancel
                    </button>
                    <button
                      type="button"
                      onClick={handleUpdateCategory}
                      className="px-4 py-2 bg-[#F4CE14] text-[#495E57] font-medium rounded-lg hover:bg-[#e6c013] transition-colors"
                    >
                      Update Category
                    </button>
                  </div>
                </form>
              </motion.div>
            </motion.div>
          )}
        </AnimatePresence>
      </div>
    </div>
  );
};

export default CategoryManagement;
