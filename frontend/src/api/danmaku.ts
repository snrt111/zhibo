import request from './request';

interface Danmaku {
  id?: number;
  liveId: number;
  userId?: number;
  content: string;
  color?: string;
  createdAt?: string;
}

interface ApiResponse<T> {
  code: number;
  message: string;
  data?: T;
}

export const danmakuApi = {
  /**
   * 发送弹幕
   */
  sendDanmaku: (danmaku: Danmaku): Promise<ApiResponse<Danmaku>> => {
    return request.post('/danmaku/send', danmaku);
  },

  /**
   * 获取直播弹幕列表
   */
  getDanmakuList: (liveId: number, params?: any): Promise<ApiResponse<Danmaku[]>> => {
    return request.get(`/danmaku/list/${liveId}`, { params });
  },

  /**
   * 获取弹幕统计数据
   */
  getDanmakuStats: (liveId: number): Promise<ApiResponse<any>> => {
    return request.get(`/danmaku/stats/${liveId}`);
  }
};
