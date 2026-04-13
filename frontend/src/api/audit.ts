import request from './request';

export interface ContentAudit {
  id?: number;
  contentType?: number;
  contentId?: number;
  content?: string;
  status?: number;
  auditorId?: number;
  auditTime?: string;
  auditResult?: string;
  autoAudit?: number;
  riskLevel?: number;
  createdAt?: string;
  updatedAt?: string;
}

export interface SensitiveWord {
  id?: number;
  word: string;
  category?: number;
  level?: number;
  enabled?: number;
  createdAt?: string;
}

export const auditApi = {
  getPendingList: (page: number = 0, size: number = 20): Promise<any> => {
    return request.get(`/audit/pending?page=${page}&size=${size}`);
  },

  getAuditList: (page: number = 0, size: number = 20, status?: number | null, contentType?: number | null, riskLevel?: number | null): Promise<any> => {
    const params = new URLSearchParams({ page: page.toString(), size: size.toString() });
    if (status !== undefined && status !== null) params.append('status', status.toString());
    if (contentType !== undefined && contentType !== null) params.append('contentType', contentType.toString());
    if (riskLevel !== undefined && riskLevel !== null) params.append('riskLevel', riskLevel.toString());
    return request.get(`/audit/list?${params.toString()}`);
  },

  manualAudit: (auditId: number, status: number, auditResult: string): Promise<any> => {
    return request.post(`/audit/audit/${auditId}`, { status, auditResult });
  },

  checkContent: (content: string): Promise<any> => {
    return request.post('/audit/check', { content });
  },

  getSensitiveWordList: (page: number = 0, size: number = 20, category?: number, enabled?: number): Promise<any> => {
    const params = new URLSearchParams({ page: page.toString(), size: size.toString() });
    if (category !== undefined) params.append('category', category.toString());
    if (enabled !== undefined) params.append('enabled', enabled.toString());
    return request.get(`/audit/sensitive-words?${params.toString()}`);
  },

  addSensitiveWord: (word: string, category?: number, level?: number): Promise<any> => {
    return request.post('/audit/sensitive-words', { word, category, level });
  },

  deleteSensitiveWord: (id: number): Promise<any> => {
    return request.delete(`/audit/sensitive-words/${id}`);
  },

  toggleSensitiveWord: (id: number, enabled: number): Promise<any> => {
    return request.put(`/audit/sensitive-words/${id}/toggle`, { enabled });
  },

  updateSensitiveWord: (id: number, word: string, category?: number, level?: number): Promise<any> => {
    return request.put(`/audit/sensitive-words/${id}`, { word, category, level });
  },

  reloadSensitiveWords: (): Promise<any> => {
    return request.post('/audit/sensitive-words/reload');
  }
};