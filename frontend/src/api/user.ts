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
  },

  /**
   * 获取所有用户列表（管理员专用）
   */
  getUserList: (): Promise<ApiResponse<User[]>> => {
    return request.get('/user/list');
  },

  /**
   * 删除用户（管理员专用）
   */
  deleteUser: (userId: number): Promise<ApiResponse<any>> => {
    return request.delete(`/user/delete/${userId}`);
  },

  /**
   * 更新用户状态（管理员专用）
   */
  updateUserStatus: (userId: number, status: number): Promise<ApiResponse<any>> => {
    return request.put(`/user/update-status/${userId}`, { status });
  },

  /**
   * 更新用户头像
   */
  updateAvatar: (avatar: string): Promise<ApiResponse<User>> => {
    return request.post('/user/update-avatar', { avatar });
  },

  /**
   * 更新用户基本信息
   */
  updateProfile: (data: { nickname?: string; email?: string; phone?: string; gender?: number; avatar?: string }): Promise<ApiResponse<User>> => {
    return request.post('/user/update-profile', data);
  },

  /**
   * 修改密码
   */
  changePassword: (oldPassword: string, newPassword: string): Promise<ApiResponse<any>> => {
    return request.post('/user/change-password', { oldPassword, newPassword });
  },

  /**
   * 获取指定用户信息
   */
  getUserById: (userId: number): Promise<ApiResponse<User>> => {
    return request.get(`/user/${userId}`);
  },

  /**
   * 发送短信验证码
   */
  sendSmsCode: (phone: string): Promise<ApiResponse<any>> => {
    return request.post('/user/send-sms-code', { phone });
  },

  /**
   * 手机号验证码登录
   */
  loginWithPhone: (phone: string, code: string): Promise<ApiResponse<any>> => {
    return request.post('/user/login-with-phone', { phone, code });
  },

  /**
   * 邮箱验证码登录
   */
  loginWithEmail: (email: string, code: string): Promise<ApiResponse<any>> => {
    return request.post('/user/login-with-email', { email, code });
  },

  /**
   * 手机号验证码注册
   */
  registerWithPhone: (phone: string, code: string, password: string, nickname?: string): Promise<ApiResponse<any>> => {
    return request.post('/user/register-with-phone', { phone, code, password, nickname });
  },

  /**
   * 发送邮箱验证码
   */
  sendEmailCode: (email: string): Promise<ApiResponse<any>> => {
    return request.post('/user/send-email-code', { email });
  },

  /**
   * 邮箱验证码注册
   */
  registerWithEmail: (email: string, code: string, password: string, nickname?: string): Promise<ApiResponse<any>> => {
    return request.post('/user/register-with-email', { email, code, password, nickname });
  },

  /**
   * 获取OAuth2授权URL
   */
  getOAuth2AuthorizeUrl: (provider: string): Promise<ApiResponse<string>> => {
    return request.get(`/user/oauth/authorize/${provider}`);
  },

  /**
   * GitHub登录
   */
  loginWithGithub: (code: string, state?: string): Promise<ApiResponse<any>> => {
    return request.get(`/user/oauth/callback/github?code=${code}&state=${state || ''}`);
  },

  /**
   * 微信登录
   */
  loginWithWechat: (code: string, state?: string): Promise<ApiResponse<any>> => {
    return request.get(`/user/oauth/callback/wechat?code=${code}&state=${state || ''}`);
  },

  /**
   * QQ登录
   */
  loginWithQQ: (code: string, state?: string): Promise<ApiResponse<any>> => {
    return request.get(`/user/oauth/callback/qq?code=${code}&state=${state || ''}`);
  },

  /**
   * 微博登录
   */
  loginWithWeibo: (code: string, state?: string): Promise<ApiResponse<any>> => {
    return request.get(`/user/oauth/callback/weibo?code=${code}&state=${state || ''}`);
  },

  /**
   * 获取账号安全信息
   */
  getSecurityInfo: (): Promise<ApiResponse<any>> => {
    return request.get('/user/security-info');
  },

  /**
   * 绑定手机号
   */
  bindPhone: (phone: string, code: string): Promise<ApiResponse<any>> => {
    return request.post('/user/bind-phone', { phone, code });
  },

  /**
   * 绑定邮箱
   */
  bindEmail: (email: string, code: string): Promise<ApiResponse<any>> => {
    return request.post('/user/bind-email', { email, code });
  },

  /**
   * 解绑手机号
   */
  unbindPhone: (): Promise<ApiResponse<any>> => {
    return request.post('/user/unbind-phone');
  },

  /**
   * 解绑邮箱
   */
  unbindEmail: (): Promise<ApiResponse<any>> => {
    return request.post('/user/unbind-email');
  }
};
