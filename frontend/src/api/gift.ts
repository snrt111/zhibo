import request from './request';

interface Gift {
  id?: number;
  name: string;
  price: number;
  icon?: string;
  image?: string;
  description?: string;
  isHot?: boolean;
  createdAt?: string;
  updatedAt?: string;
}

interface GiftRecord {
  id?: number;
  liveId: number;
  fromUserId?: number;
  toUserId?: number;
  giftId: number;
  count?: number;
  giftCount?: number;
  totalPrice?: number;
  totalAmount?: number;
  createdAt?: string;
}

interface ApiResponse<T> {
  code: number;
  message: string;
  data?: T;
}

export const giftApi = {
  /**
   * 获取礼物列表（按价格升序）
   */
  getGiftList: (): Promise<ApiResponse<Gift[]>> => {
    return request.get('/gift/list');
  },

  /**
   * 获取热门礼物列表
   */
  getHotGiftList: (): Promise<ApiResponse<Gift[]>> => {
    return request.get('/gift/hot');
  },

  /**
   * 发送礼物
   */
  sendGift: (giftRecord: GiftRecord): Promise<ApiResponse<GiftRecord>> => {
    return request.post('/gift/send', {
      liveId: giftRecord.liveId,
      giftId: giftRecord.giftId,
      count: giftRecord.count || giftRecord.giftCount || 1,
      toUserId: giftRecord.toUserId
    });
  },

  /**
   * 获取直播礼物记录
   */
  getGiftRecords: (liveId: number, params?: any): Promise<ApiResponse<GiftRecord[]>> => {
    return request.get(`/gift/records/${liveId}`, { params });
  },

  /**
   * 获取礼物统计数据
   */
  getGiftStats: (liveId: number): Promise<ApiResponse<any>> => {
    return request.get(`/gift/stats/${liveId}`);
  },

  /**
   * 获取直播间礼物统计
   */
  getLiveGiftStats: (liveId: number): Promise<ApiResponse<any>> => {
    return request.get(`/gift/stats/live/${liveId}`);
  },

  /**
   * 获取主播礼物统计
   */
  getAnchorGiftStats: (): Promise<ApiResponse<any>> => {
    return request.get('/gift/stats/anchor');
  }
};
