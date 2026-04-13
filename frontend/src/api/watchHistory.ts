import request from './request';

interface WatchHistory {
  id: number;
  userId: number;
  liveId: number;
  liveTitle: string;
  liveCover: string;
  anchorId: number;
  anchorName: string;
  watchDuration: number;
  createdAt: string;
  updatedAt: string;
}

interface ApiResponse<T> {
  code: number;
  message: string;
  data?: T;
}

export const watchHistoryApi = {
  recordWatchHistory: (liveId: number, watchDuration: number): Promise<ApiResponse<any>> => {
    return request.post('/watch-history/record', { liveId, watchDuration });
  },

  getUserWatchHistory: (page: number = 1, size: number = 20): Promise<ApiResponse<any>> => {
    return request.get('/watch-history/list', { params: { page, size } });
  },

  clearUserWatchHistory: (): Promise<ApiResponse<any>> => {
    return request.delete('/watch-history/clear');
  },

  deleteWatchHistory: (historyId: number): Promise<ApiResponse<any>> => {
    return request.delete(`/watch-history/delete/${historyId}`);
  }
};
