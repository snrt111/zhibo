import request from './request';

interface Live {
  id?: number;
  userId: number;
  title: string;
  description: string;
  cover: string;
  categoryId: number;
  status: number;
  viewCount?: number;
  startTime?: string;
  endTime?: string;
  createdAt?: string;
  updatedAt?: string;
}

interface ApiResponse<T> {
  code: number;
  message: string;
  data?: T;
}

export const liveApi = {
  /**
   * 获取直播列表
   */
  getLiveList: (params?: any): Promise<ApiResponse<Live[]>> => {
    return request.get('/live/list', { params });
  },

  /**
   * 获取直播详情
   */
  getLiveDetail: (id: number): Promise<ApiResponse<Live>> => {
    return request.get(`/live/detail/${id}`);
  },

  /**
   * 创建直播
   */
  createLive: (live: Live): Promise<ApiResponse<Live>> => {
    return request.post('/live/create', live);
  },

  /**
   * 更新直播
   */
  updateLive: (live: Live): Promise<ApiResponse<Live>> => {
    return request.put('/live/update', live);
  },

  /**
   * 开始直播
   */
  startLive: (id: number): Promise<ApiResponse<any>> => {
    return request.post(`/live/start/${id}`);
  },

  /**
   * 结束直播
   */
  endLive: (id: number): Promise<ApiResponse<any>> => {
    return request.post(`/live/end/${id}`);
  },

  /**
   * 获取直播统计数据
   */
  getLiveStats: (id: number): Promise<ApiResponse<any>> => {
    return request.get(`/live/stats/${id}`);
  },

  /**
   * 获取我的直播（主播）
   */
  getMyLives: (): Promise<ApiResponse<Live[]>> => {
    return request.get('/live/my-list');
  },

  /**
   * 创建直播
   */
  create: (live: Partial<Live>): Promise<ApiResponse<Live>> => {
    return request.post('/live/create', live);
  },

  /**
   * 开始直播
   */
  start: (id: number): Promise<ApiResponse<any>> => {
    return request.post(`/live/start/${id}`);
  },

  /**
   * 结束直播
   */
  end: (id: number): Promise<ApiResponse<any>> => {
    return request.post(`/live/end/${id}`);
  },

  /**
   * 删除直播
   */
  delete: (id: number): Promise<ApiResponse<any>> => {
    return request.delete(`/live/delete/${id}`);
  },

  /**
   * 获取推流地址
   */
  getPushUrl: (id: number): Promise<ApiResponse<string>> => {
    return request.get(`/live/push-url/${id}`);
  },

  /**
   * 获取播放地址
   */
  getPlayUrl: (id: number): Promise<ApiResponse<string>> => {
    return request.get(`/live/play-url/${id}`);
  }
};
