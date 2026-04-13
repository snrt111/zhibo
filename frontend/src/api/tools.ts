import request from './request';
import type { ApiResponse } from './types';

export interface GenerateImageRequest {
  prompt: string;
  modelId: number;
}

export interface GeneratedImage {
  id: number;
  userId?: number;
  prompt: string;
  imageUrl: string;
  modelId?: number;
  createdAt: string;
}

export interface AiModel {
  id: number;
  name: string;
  type: string;
}

export const toolsApi = {
  getImageModels: (type?: string): Promise<ApiResponse<AiModel[]>> => {
    const url = type ? `/tools/image/models?type=${type}` : '/tools/image/models';
    return request.get(url);
  },

  generateImage: (data: GenerateImageRequest): Promise<ApiResponse<GeneratedImage>> => {
    return request.post('/tools/image/generate', data);
  },

  getImageHistory: (): Promise<ApiResponse<GeneratedImage[]>> => {
    return request.get('/tools/image/history');
  }
};
