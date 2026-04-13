import request from './request';

interface Recharge {
  id?: number;
  userId?: number;
  amount: number;
  paymentMethod: string;
  transactionId?: string;
  status?: number;
  createdAt?: string;
  updatedAt?: string;
  payTime?: string;
}

interface ApiResponse<T> {
  code: number;
  message: string;
  data?: T;
}

export const rechargeApi = {
  /**
   * 创建充值订单
   */
  createRecharge: (amount: number, paymentMethod: string): Promise<ApiResponse<Recharge>> => {
    return request.post('/recharge/create', {
      amount,
      paymentMethod
    });
  },

  /**
   * 完成充值
   */
  completeRecharge: (rechargeId: number): Promise<ApiResponse<void>> => {
    return request.post(`/recharge/complete/${rechargeId}`);
  },

  /**
   * 获取充值记录
   */
  getRechargeList: (page: number = 1, size: number = 10): Promise<ApiResponse<any>> => {
    return request.get('/recharge/list', {
      params: { page, size }
    });
  }
};
