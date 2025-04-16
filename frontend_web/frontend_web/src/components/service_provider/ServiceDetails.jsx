import React, { useEffect, useState } from 'react';
import { useParams } from 'react-router-dom';
import axios from 'axios';

function ServiceDetails() {
  const { serviceId } = useParams();
  const [service, setService] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    const fetchService = async () => {
      try {
        const token = localStorage.getItem('authToken') || sessionStorage.getItem('authToken');
        const response = await axios.get(`/api/services/${serviceId}`, {
          headers: { Authorization: `Bearer ${token}` },
        });
        setService(response.data);
        setLoading(false);
      } catch (err) {
        console.error('Error fetching service details:', err);
        setError('Failed to load service details.');
        setLoading(false);
      }
    };

    fetchService();
  }, [serviceId]);

  if (loading) {
    return <div>Loading...</div>;
  }

  if (error) {
    return <div>{error}</div>;
  }

  return (
    <div className="max-w-4xl mx-auto p-6 bg-white rounded-lg shadow">
      <h1 className="text-2xl font-bold text-gray-800">{service.serviceName}</h1>
      <p className="text-gray-600 mt-2">{service.serviceDescription}</p>
      <div className="mt-4">
        <p><strong>Price Range:</strong> {service.priceRange}</p>
        <p><strong>Duration:</strong> {service.durationEstimate}</p>
      </div>
    </div>
  );
}

export default ServiceDetails;