import request from './request';

interface ApiResponse<T> {
  code: number;
  message: string;
  data?: T;
}

export const followApi = {
  follow: (userId: number): Promise<ApiResponse<any>> => {
    return request.post(`/follow/${userId}`);
  },

  unfollow: (userId: number): Promise<ApiResponse<any>> => {
    return request.delete(`/follow/${userId}`);
  },

  isFollowing: (userId: number): Promise<ApiResponse<boolean>> => {
    return request.get(`/follow/status/${userId}`);
  },

  getFollowingList: (page: number = 0, size: number = 20): Promise<ApiResponse<any>> => {
    return request.get('/follow/following', { params: { page, size } });
  },

  getFollowerList: (page: number = 0, size: number = 20): Promise<ApiResponse<any>> => {
    return request.get('/follow/followers', { params: { page, size } });
  },

  getFollowCount: (userId: number): Promise<ApiResponse<any>> => {
    return request.get(`/follow/count/${userId}`);
  }
};
