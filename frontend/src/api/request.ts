import axios from 'axios';
import { ApiError, AuthError, NetworkError } from '../utils/errorHandler';

/**
 * Axios请求实例
 * 统一配置请求拦截器和响应拦截器
 * 自动添加Token、处理错误响应
 */
const service = axios.create({
  baseURL: '/api',
  timeout: 10000,
  headers: {
    'Content-Type': 'application/json'
  }
});

/**
 * 请求拦截器
 * 自动添加Authorization头
 */
service.interceptors.request.use(
  config => {
    console.log('Request:', config.url);
    const token = localStorage.getItem('token');
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  error => {
    console.error('请求错误:', error);
    return Promise.reject(new NetworkError());
  }
);

/**
 * 响应拦截器
 * 统一处理响应错误，转换为自定义异常类型
 */
service.interceptors.response.use(
  response => {
    console.log('Response:', response.config.url, response.data);
    const res = response.data;
    return res;
  },
  error => {
    if (error.response) {
      const { status, data } = error.response;

      // 401 未授权 - 需要重新登录
      if (status === 401) {
        return Promise.reject(new AuthError());
      }

      // 403 禁止访问 - 权限不足
      if (status === 403) {
        return Promise.reject(new ApiError(status, data?.message || '没有权限执行此操作'));
      }

      // 404 资源不存在
      if (status === 404) {
        return Promise.reject(new ApiError(status, data?.message || '请求的资源不存在'));
      }

      // 5xx 服务器错误
      if (status >= 500) {
        return Promise.reject(new ApiError(status, data?.message || '服务器错误，请稍后重试'));
      }

      // 其他错误
      if (data) {
        return Promise.reject(new ApiError(status, data?.message || '请求失败'));
      }
    }

    // 请求发送失败（网络问题）
    if (error.request) {
      return Promise.reject(new NetworkError());
    }

    console.error('响应错误:', error);
    return Promise.reject(new NetworkError());
  }
);

export default service;
