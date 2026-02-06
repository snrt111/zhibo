import request from './request';

interface ApiResponse<T> {
  code: number;
  message: string;
  data?: T;
}

export const analyticsApi = {
  /**
   * 获取平台总览数据
   */
  getOverview: (): Promise<ApiResponse<any>> => {
    return request.get('/analytics/overview');
  },

  /**
   * 获取直播趋势数据
   */
  getLiveTrend: (params?: any): Promise<ApiResponse<any>> => {
    return request.get('/analytics/live-trend', { params });
  },

  /**
   * 获取用户趋势数据
   */
  getUserTrend: (params?: any): Promise<ApiResponse<any>> => {
    return request.get('/analytics/user-trend', { params });
  },

  /**
   * 获取礼物收入数据
   */
  getGiftIncome: (params?: any): Promise<ApiResponse<any>> => {
    return request.get('/analytics/gift-income', { params });
  },

  /**
   * 获取热门直播数据
   */
  getHotLives: (params?: any): Promise<ApiResponse<any>> => {
    return request.get('/analytics/hot-lives', { params });
  },

  /**
   * 获取热门主播数据
   */
  getHotAnchors: (params?: any): Promise<ApiResponse<any>> => {
    return request.get('/analytics/hot-anchors', { params });
  }
};
