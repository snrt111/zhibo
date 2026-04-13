import request from './request';

export interface Withdraw {
  id?: number;
  userId?: number;
  amount: number;
  balanceBefore?: number;
  balanceAfter?: number;
  status?: number;
  bankCard: string;
  bankName: string;
  accountName: string;
  auditUserId?: number;
  auditTime?: string;
  auditRemark?: string;
  payTime?: string;
  createdAt?: string;
  updatedAt?: string;
}

export interface UserBalance {
  id?: number;
  userId?: number;
  totalIncome?: number;
  availableBalance?: number;
  frozenBalance?: number;
  totalWithdraw?: number;
}

export const withdrawApi = {
  getBalance: (): Promise<any> => {
    return request.get('/withdraw/balance');
  },

  applyWithdraw: (data: { amount: number; bankCard: string; bankName: string; accountName: string }): Promise<any> => {
    return request.post('/withdraw/apply', data);
  },

  getMyWithdrawList: (page: number = 0, size: number = 20): Promise<any> => {
    return request.get(`/withdraw/my-list?page=${page}&size=${size}`);
  },

  getPendingList: (page: number = 0, size: number = 20): Promise<any> => {
    return request.get(`/withdraw/pending?page=${page}&size=${size}`);
  },

  getWithdrawList: (page: number = 0, size: number = 20, status?: number | null): Promise<any> => {
    const params = new URLSearchParams({ page: page.toString(), size: size.toString() });
    if (status !== undefined && status !== null) params.append('status', status.toString());
    return request.get(`/withdraw/list?${params.toString()}`);
  },

  auditWithdraw: (withdrawId: number, status: number, remark: string): Promise<any> => {
    return request.post(`/withdraw/audit/${withdrawId}`, { status, remark });
  },

  confirmPay: (withdrawId: number): Promise<any> => {
    return request.post(`/withdraw/pay/${withdrawId}`);
  },

  getWithdrawDetail: (withdrawId: number): Promise<any> => {
    return request.get(`/withdraw/${withdrawId}`);
  }
};