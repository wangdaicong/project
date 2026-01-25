import axios from 'axios';

const api = axios.create({
  baseURL: '/api',
  timeout: 0,
  headers: {
    'Content-Type': 'application/json'
  }
});

export const userApi = {
  login: (data) => api.post('/user/login', data),
  register: (data) => api.post('/user/register', data),
  getUser: (id) => api.get(`/user/${id}`)
};

export const paperApi = {
  generateOutline: (data) => api.post('/paper/outline', data),
  generatePaper: (data) => api.post('/paper/generate', data),
  generateReferences: (title, subject, count = 40) => 
    api.post(`/paper/references?title=${encodeURIComponent(title)}&subject=${encodeURIComponent(subject)}&count=${count}`),
  exportPaper: (format, data) => api.post(`/paper/export?format=${encodeURIComponent(format)}`, data, {
    responseType: 'blob'
  }),
  savePaper: (data) => api.post('/paper/save', data),
  getPaper: (id) => api.get(`/paper/${id}`),
  getUserPapers: (userId) => api.get(`/paper/user/${userId}`),
  deletePaper: (id) => api.delete(`/paper/${id}`)
};

export const fileApi = {
  upload: (file) => {
    const formData = new FormData();
    formData.append('file', file);
    return api.post('/file/upload', formData, {
      headers: { 'Content-Type': 'multipart/form-data' }
    });
  },
  uploadMultiple: (files) => {
    const formData = new FormData();
    files.forEach(file => formData.append('files', file));
    return api.post('/file/upload/multiple', formData, {
      headers: { 'Content-Type': 'multipart/form-data' }
    });
  }
};

export default api;
