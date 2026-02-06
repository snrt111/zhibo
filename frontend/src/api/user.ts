import request from './request';

interface User {
  id?: number;
  username: string;
  password: string;
  nickname: string;
  email?: string;
  phone?: string;
  avatar?: string;
  gender?: number;
  userType?: number;
  status?: number;
  createdAt?: string;
  updatedAt?: string;
}

interface LoginRequest {
  username: string;
  password: string;
}

interface ResetPasswordRequest {
  newPassword: string;
}

interface ApiResponse<T> {
  code: number;
  message: string;
  data?: T;
  token?: string;
  user?: User;
}

export const userApi = {
  /**
   * 用户注册
   */
  register: (user: User): Promise<ApiResponse<User>> => {
    return request.post('/user/register', user);
  },

  /**
   * 用户登录
   */
  login: (loginRequest: LoginRequest): Promise<ApiResponse<any>> => {
    return request.post('/user/login', loginRequest);
  },

  /**
   * 获取当前用户信息
   */
  getUserInfo: (): Promise<ApiResponse<User>> => {
    return request.get('/user/info');
  },

  /**
   * 更新用户信息
   */
  updateUserInfo: (user: Partial<User>): Promise<ApiResponse<User>> => {
    return request.put('/user/update', user);
  },

  /**
   * 重置密码
   */
  resetPassword: (resetRequest: ResetPasswordRequest): Promise<ApiResponse<any>> => {
    return request.post('/user/reset-password', resetRequest);
  }
};
