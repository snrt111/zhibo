import request from './request';

interface UserNotificationSetting {
  id?: number;
  userId?: number;
  followLiveStart: number;
  systemNotification: number;
  giftNotification: number;
  createdAt?: string;
  updatedAt?: string;
}

interface LiveStartNotification {
  id: number;
  userId: number;
  anchorId: number;
  liveId?: number;
  liveTitle: string;
  liveCover?: string;
  status: number;
  sentAt?: string;
  readAt?: string;
  createdAt: string;
}

interface ApiResponse<T> {
  code: number;
  message: string;
  data?: T;
}

export const notificationApi = {
  /**
   * 获取用户通知设置
   */
  getUserSetting: (): Promise<ApiResponse<UserNotificationSetting>> => {
    return request.get('/notification/setting');
  },

  /**
   * 更新用户通知设置
   */
  updateUserSetting: (setting: Partial<UserNotificationSetting>): Promise<ApiResponse<UserNotificationSetting>> => {
    return request.put('/notification/setting', setting);
  },

  /**
   * 获取开播提醒列表
   */
  getNotificationList: (limit: number = 20): Promise<ApiResponse<LiveStartNotification[]>> => {
    return request.get(`/notification/list?limit=${limit}`);
  },

  /**
   * 获取未读通知数量
   */
  getUnreadCount: (): Promise<ApiResponse<number>> => {
    return request.get('/notification/unread-count');
  },

  /**
   * 标记通知为已读
   */
  markAsRead: (notificationId: number): Promise<ApiResponse<void>> => {
    return request.post(`/notification/read/${notificationId}`);
  },

  /**
   * 标记所有通知为已读
   */
  markAllAsRead: (): Promise<ApiResponse<number>> => {
    return request.post('/notification/read-all');
  },

  /**
   * 删除通知
   */
  deleteNotification: (notificationId: number): Promise<ApiResponse<void>> => {
    return request.delete(`/notification/${notificationId}`);
  }
};
