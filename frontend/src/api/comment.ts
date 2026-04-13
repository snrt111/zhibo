import request from './request';

interface Comment {
  id?: number;
  liveId: number;
  userId?: number;
  content: string;
  parentId?: number;
  likeCount?: number;
  createdAt?: string;
}

interface ApiResponse<T> {
  code: number;
  message: string;
  data?: T;
}

export const commentApi = {
  createComment: (comment: Comment): Promise<ApiResponse<Comment>> => {
    return request.post('/comment/create', comment);
  },

  deleteComment: (commentId: number): Promise<ApiResponse<any>> => {
    return request.delete(`/comment/${commentId}`);
  },

  likeComment: (commentId: number): Promise<ApiResponse<any>> => {
    return request.post(`/comment/like/${commentId}`);
  },

  unlikeComment: (commentId: number): Promise<ApiResponse<any>> => {
    return request.post(`/comment/unlike/${commentId}`);
  },

  getCommentList: (liveId: number, page: number = 0, size: number = 20): Promise<ApiResponse<any>> => {
    return request.get(`/comment/list/${liveId}`, { params: { page, size } });
  },

  getReplyList: (commentId: number, page: number = 0, size: number = 20): Promise<ApiResponse<any>> => {
    return request.get(`/comment/replies/${commentId}`, { params: { page, size } });
  }
};
