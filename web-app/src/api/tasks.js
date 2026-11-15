import api from './axios';

const API_URL = '/';

export const getTasksByUser = async (userId) => {
  const response = await api.get(`${API_URL}tasks/user/${userId}`);
  return response.data;
};

export const getTaskById = async (taskId) => {
  const response = await api.get(`${API_URL}tasks/${taskId}`);
  return response.data;
};

export const createTask = async (templateId) => {
  const response = await api.post(`${API_URL}tasks/`, { templateId });
  return response.data;
};

export const createLocalTask = async (templateId, ipAddresses) => {
  const response = await api.post(`${API_URL}tasks/local`, { templateId, ipAddresses });
  return response.data;
};

export const runTask = async (taskId, userName, sshKey) => {
  const response = await api.post(`${API_URL}tasks/${taskId}/run`, { userName, sshKey });
  return response.data;
};

export const startVm = async (taskId) => {
  const response = await api.get(`${API_URL}tasks/${taskId}/start`);
  return response.data;
};

export const stopVm = async (taskId) => {
  const response = await api.get(`${API_URL}tasks/${taskId}/stop`);
  return response.data;
};

export const deleteVm = async (taskId) => {
  const response = await api.get(`${API_URL}tasks/${taskId}/delete-vm/`);
  return response.data;
};

export const checkTask = async (taskId) => {
  const response = await api.get(`${API_URL}tasks/${taskId}/check`);
  return response.data;
};

export const getTaskInfo = async (taskId) => {
  const response = await api.get(`${API_URL}tasks/${taskId}/info`);
  return response.data;
};

export const getTaskResults = async (taskId) => {
  const response = await api.get(`${API_URL}task/results/${taskId}`);
  return response.data;
};

export const getTaskChecks = async (taskId) => {
  const response = await api.get(`${API_URL}task/checks/${taskId}`);
  return response.data;
};

export const getResultsByCheckId = async (checkId) => {
  const response = await api.get(`${API_URL}results/${checkId}`);
  return response.data;
};

export const getResultById = async (resultId) => {
  const response = await api.get(`${API_URL}results/by-id/${resultId}`);
  return response.data;
};

export const getTemplateById = async (id) => {
  const response = await api.get(`${API_URL}templates/${id}`);
  return response.data;
};

export const getTemplates = async () => {
  const response = await api.get(`${API_URL}get-templates`);
  return response.data;
};

export const updateTemplates = async () => {
  const response = await api.get(`${API_URL}update-templates`);
  return response.data;
};

export const deleteTemplate = async (id) => {
  const response = await api.delete(`${API_URL}delete-template/${id}`);
  return response.data;
};

export const getTemplatesAnalytics = async () => {
  const response = await api.get(`${API_URL}get-templates`);
  return response.data;
};

export const getAnalyticsSummary = async () => {
  const response = await api.get(`${API_URL}api/analytics/summary`);
  return response.data;
};

export const getAnalyticsTasks = async () => {
  const response = await api.get(`${API_URL}api/analytics/task-stats`);
  return response.data;
};

export default {
  getTasksByUser,
  getTaskById,
  createTask,
  createLocalTask,
  runTask,
  startVm,
  stopVm,
  deleteVm,
  checkTask,
  getTaskInfo,
  getTaskResults,
  getTaskChecks,
  getResultsByCheckId,
  getResultById,
  getTemplates,
  getTemplateById,
  updateTemplates,
  deleteTemplate,
  getTemplatesAnalytics,
  getAnalyticsSummary,
  getAnalyticsTasks,
};
