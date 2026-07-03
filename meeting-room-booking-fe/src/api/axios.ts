import axios from 'axios';
import { useAuthStore } from '../stores/authStore';

let isRefreshing = false;
let failedQueue: any[] = [];

const processQueue = (error: any, token: string | null = null) => {
  failedQueue.forEach((prom) => {
    if (error) {
      prom.reject(error);
    } else {
      prom.resolve(token);
    }
  });
  failedQueue = [];
};

const apiClient = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080',
  headers: {
    'Content-Type': 'application/json',
  },
});

apiClient.interceptors.request.use((config) => {
  const authStore = useAuthStore();
  if (authStore.token) {
    config.headers.Authorization = `Bearer ${authStore.token}`;
  }
  return config;
});

apiClient.interceptors.response.use(
  (response) => response,
  async (error) => {
    const authStore = useAuthStore();
    const originalRequest = error.config;

    console.log('API Error Message:', error.message);
    console.log('API Error Response Status:', error.response?.status);
    console.log('API Error URL:', originalRequest?.url);

    const is401 = error.response?.status === 401;
    // Sometimes CORS blocks the 401 response status, resulting in a "Network Error"
    const isPotentialExpiredToken = is401 || (error.message === 'Network Error' && !error.response && authStore.token);

    if (isPotentialExpiredToken && !originalRequest?._retry) {
      console.log('Detected potential expired token, attempting refresh...');
      if (isRefreshing) {
        console.log('Refresh already in progress, queuing request...');
        return new Promise(function (resolve, reject) {
          failedQueue.push({ resolve, reject });
        })
          .then((token) => {
            originalRequest.headers.Authorization = 'Bearer ' + token;
            return apiClient(originalRequest);
          })
          .catch((err) => {
            return Promise.reject(err);
          });
      }

      originalRequest._retry = true;
      isRefreshing = true;

      return new Promise(function (resolve, reject) {
        authStore.refreshToken()
          .then((success) => {
            if (success) {
              const token = authStore.token;
              originalRequest.headers.Authorization = 'Bearer ' + token;
              processQueue(null, token);
              resolve(apiClient(originalRequest));
            } else {
              processQueue(new Error('Refresh failed'), null);
              authStore.logout('Session expired. Please login again.');
              reject(error);
            }
          })
          .catch((err) => {
            processQueue(err, null);
            authStore.logout('Session expired. Please login again.');
            reject(err);
          })
          .finally(() => {
            isRefreshing = false;
          });
      });
    }

    return Promise.reject(error);
  }
);

export default apiClient;
