import request from './request';

export interface Report {
  id?: number;
  reporterId?: number;
  targetType: number;
  targetId: number;
  reason: string;
  description?: string;
  evidence?: string;
  status?: number;
  handlerId?: number;
  handleTime?: string;
  handleResult?: string;
  createdAt?: string;
  updatedAt?: string;
}

export const reportApi = {
  createReport: (data: { targetType: number; targetId: number; reason: string; description?: string; evidence?: string }): Promise<any> => {
    return request.post('/report/create', data);
  },

  getPendingList: (page: number = 0, size: number = 20): Promise<any> => {
    return request.get(`/report/pending?page=${page}&size=${size}`);
  },

  getReportList: (page: number = 0, size: number = 20, status?: number | null, targetType?: number | null): Promise<any> => {
    const params = new URLSearchParams({ page: page.toString(), size: size.toString() });
    if (status !== undefined && status !== null) params.append('status', status.toString());
    if (targetType !== undefined && targetType !== null) params.append('targetType', targetType.toString());
    return request.get(`/report/list?${params.toString()}`);
  },

  getMyReportList: (page: number = 0, size: number = 20): Promise<any> => {
    return request.get(`/report/my-list?page=${page}&size=${size}`);
  },

  handleReport: (reportId: number, status: number, handleResult: string): Promise<any> => {
    return request.post(`/report/handle/${reportId}`, { status, handleResult });
  },

  getReportDetail: (reportId: number): Promise<any> => {
    return request.get(`/report/${reportId}`);
  }
};