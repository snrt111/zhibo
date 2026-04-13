import request from './request';
import type { ApiResponse } from './types';

export interface AiModelConfig {
  id?: number;
  name: string;
  type: string;
  baseUrl: string;
  apiKey?: string;
  modelName: string;
  status?: number;
  priority?: number;
  createdAt?: string;
  updatedAt?: string;
}

export interface PageResult<T> {
  records: T[];
  total: number;
  size: number;
  current: number;
  pages: number;
}

export const aiConfigApi = {
  getList: (page: number, size: number): Promise<ApiResponse<PageResult<AiModelConfig>>> => {
    return request.get(`/admin/ai-config/list?page=${page}&size=${size}`);
  },

  getById: (id: number): Promise<ApiResponse<AiModelConfig>> => {
    return request.get(`/admin/ai-config/${id}`);
  },

  create: (data: AiModelConfig): Promise<ApiResponse<void>> => {
    return request.post('/admin/ai-config', data);
  },

  update: (data: AiModelConfig): Promise<ApiResponse<void>> => {
    return request.put('/admin/ai-config', data);
  },

  delete: (id: number): Promise<ApiResponse<void>> => {
    return request.delete(`/admin/ai-config/${id}`);
  },

  updateStatus: (id: number, status: number): Promise<ApiResponse<void>> => {
    return request.put(`/admin/ai-config/${id}/status`, { status });
  }
};
