import api from './axios';

export const login = async (username, password) => {
  const response = await api.post('/auth/', { username, password });
  return response.data;
};

export const register = async (username, password) => {
  const response = await api.post('/user/', { username, password });
  return response.data;
};

export const refreshTokens = async (refreshToken) => {
  const response = await api.post('/auth/refresh', { refreshToken });
  return response.data;
};

export default {
  login,
  register,
  refreshTokens,
};