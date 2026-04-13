import { message } from 'ant-design-vue';

/**
 * API统一响应格式
 */
export interface ApiResponse<T = any> {
  code: number;
  message: string;
  data?: T;
}

/**
 * API错误类
 * 用于封装接口返回的错误信息
 */
export class ApiError extends Error {
  code: number;
  data?: any;

  constructor(code: number, message: string, data?: any) {
    super(message);
    this.name = 'ApiError';
    this.code = code;
    this.data = data;
  }
}

/**
 * 网络错误类
 * 用于封装网络连接失败等错误
 */
export class NetworkError extends Error {
  constructor(message: string = '网络连接失败，请检查网络') {
    super(message);
    this.name = 'NetworkError';
  }
}

/**
 * 认证错误类
 * 用于封装登录过期、未授权等错误
 */
export class AuthError extends Error {
  constructor(message: string = '登录已过期，请重新登录') {
    super(message);
    this.name = 'AuthError';
  }
}

/**
 * 错误处理器
 * 统一处理各类错误，包括显示错误提示、跳转登录等
 */
export const errorHandler = {
  /**
   * 处理错误
   * @param error 错误对象
   * @param showMessage 是否显示错误提示
   */
  handle(error: any, showMessage: boolean = true): void {
    if (error instanceof ApiError) {
      // API错误
      if (showMessage) {
        message.error(error.message);
      }
    } else if (error instanceof AuthError) {
      // 认证错误 - 清除登录信息并跳转登录页
      if (showMessage) {
        message.error(error.message);
      }
      localStorage.removeItem('token');
      localStorage.removeItem('userType');
      localStorage.removeItem('username');
      localStorage.removeItem('avatar');
      setTimeout(() => {
        window.location.href = '/login';
      }, 1500);
    } else if (error instanceof NetworkError) {
      // 网络错误
      if (showMessage) {
        message.error(error.message);
      }
    } else if (error instanceof Error) {
      // 其他系统错误
      console.error('系统错误:', error);
      if (showMessage) {
        message.error(error.message || '操作失败');
      }
    } else {
      // 未知错误
      console.error('未知错误:', error);
      if (showMessage) {
        message.error('操作失败');
      }
    }
  },

  /**
   * 处理错误并执行回调
   * @param error 错误对象
   * @param callback 错误回调
   */
  handleWithCallback(error: any, callback: (error: any) => void): void {
    this.handle(error, false);
    callback(error);
  }
};

/**
 * 处理API响应（同步）
 * @param response API响应
 * @param successCallback 成功回调
 * @param errorCallback 错误回调
 */
export const handleResponse = <T = any>(
  response: ApiResponse<T>,
  successCallback: (data: T | undefined) => void,
  errorCallback?: (message: string) => void
): void => {
  if (response.code === 200) {
    successCallback(response.data);
  } else {
    const errorMsg = response.message || '操作失败';
    if (errorCallback) {
      errorCallback(errorMsg);
    } else {
      message.error(errorMsg);
    }
  }
};

/**
 * 处理API响应（异步）
 * @param response API响应
 * @param successCallback 成功回调
 * @param errorCallback 错误回调
 */
export const handleResponseAsync = async <T = any>(
  response: ApiResponse<T>,
  successCallback: (data: T | undefined) => Promise<void> | void,
  errorCallback?: (message: string) => Promise<void> | void
): Promise<void> => {
  if (response.code === 200) {
    await successCallback(response.data);
  } else {
    const errorMsg = response.message || '操作失败';
    if (errorCallback) {
      await errorCallback(errorMsg);
    } else {
      message.error(errorMsg);
    }
  }
};

/**
 * 创建请求处理器
 * 用于统一处理API请求和错误
 * @param defaultErrorHandler 默认错误处理器
 */
export const createRequestHandler = <T = any>(
  defaultErrorHandler?: (error: any) => void
) => {
  return {
    /**
     * 执行请求
     * @param request 请求Promise
     * @param successCallback 成功回调
     * @param options 选项
     */
    async execute(
      request: Promise<ApiResponse<T>>,
      successCallback: (data: T | undefined) => Promise<void> | void,
      options?: {
        errorCallback?: (message: string) => Promise<void> | void;
        showError?: boolean;
      }
    ): Promise<void> {
      try {
        const response = await request;
        await handleResponseAsync(response, successCallback, options?.errorCallback);
      } catch (error: any) {
        if (defaultErrorHandler) {
          defaultErrorHandler(error);
        } else if (options?.showError !== false) {
          errorHandler.handle(error);
        }
      }
    }
  };
};
