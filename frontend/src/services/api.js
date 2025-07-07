import axios from 'axios';
import store from '@/store';
import router from '@/router';

const apiClient = axios.create({
  baseURL: 'http://localhost:8080/api',
  headers: {
    'Content-Type': 'application/json'
  }
});

apiClient.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('jwtToken');
    if (token) {
      config.headers['Authorization'] = `Bearer ${token}`;
    } else {
      delete config.headers['Authorization']; 
    }
    return config;
  }
);

apiClient.interceptors.response.use(
  (response) => {
    return response;
  },
  (error) => {
    if (error.response) {
      const { status } = error.response;
      if (status === 401) {
        console.error('Unauthorized request - 401. Logging out and redirecting.');
        store.dispatch('auth/logout', { root: true }); 
        router.push('/login'); 
      }
    }
    return Promise.reject(error);
  }
);

export default apiClient;