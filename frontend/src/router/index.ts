import { createRouter, createWebHistory } from 'vue-router';

export type UserType = '0' | '1';

export const UserTypeLabels: Record<UserType, string> = {
  '0': '普通用户',
  '1': '管理员'
};

const router = createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: '/login',
      name: 'Login',
      component: () => import('../components/Login.vue'),
      meta: { requiresAuth: false, title: '登录' }
    },
    {
      path: '/register',
      name: 'Register',
      component: () => import('../components/Register.vue'),
      meta: { requiresAuth: false, title: '注册' }
    },
    {
      path: '/oauth/callback',
      name: 'OAuth2Callback',
      component: () => import('../components/Login.vue'),
      meta: { requiresAuth: false, title: '登录中...' }
    },
    {
      path: '/',
      name: 'Home',
      component: () => import('../components/LiveList.vue'),
      meta: { requiresAuth: true, title: '直播列表' }
    },
    {
      path: '/live/:id',
      name: 'LiveRoom',
      component: () => import('../components/LiveRoom.vue'),
      meta: { requiresAuth: true, title: '直播间' }
    },
    {
      path: '/anchor',
      name: 'AnchorDashboard',
      component: () => import('../components/anchor/AnchorDashboard.vue'),
      meta: { requiresAuth: true, title: '主播中心' }
    },
    {
      path: '/anchor/withdraw',
      name: 'AnchorWithdraw',
      component: () => import('../components/anchor/WithdrawPage.vue'),
      meta: { requiresAuth: true, title: '提现管理' }
    },
    {
      path: '/anchor/live/:id',
      name: 'AnchorLiveRoom',
      component: () => import('../components/anchor/AnchorLiveRoom.vue'),
      meta: { requiresAuth: true, title: '主播直播间' }
    },
    {
      path: '/admin',
      name: 'AdminDashboard',
      component: () => import('../components/admin/AdminDashboard.vue'),
      meta: { requiresAuth: true, requiresAdmin: true, title: '管理后台' }
    },
    {
      path: '/admin/analytics',
      name: 'AnalyticsDashboard',
      component: () => import('../components/admin/AnalyticsDashboard.vue'),
      meta: { requiresAuth: true, requiresAdmin: true, title: '数据分析' }
    },
    {
      path: '/admin/monitor',
      name: 'MonitorDashboard',
      component: () => import('../components/admin/MonitorDashboard.vue'),
      meta: { requiresAuth: true, requiresAdmin: true, title: '系统监控' }
    },
    {
      path: '/admin/live',
      name: 'LiveManagement',
      component: () => import('../components/admin/LiveManagement.vue'),
      meta: { requiresAuth: true, requiresAdmin: true, title: '直播管理' }
    },
    {
      path: '/admin/user',
      name: 'UserManagement',
      component: () => import('../components/admin/UserManagement.vue'),
      meta: { requiresAuth: true, requiresAdmin: true, title: '用户管理' }
    },
    {
      path: '/admin/gift',
      name: 'GiftManagement',
      component: () => import('../components/admin/GiftManagement.vue'),
      meta: { requiresAuth: true, requiresAdmin: true, title: '礼物管理' }
    },
    {
      path: '/admin/ai-config',
      name: 'AiConfigManagement',
      component: () => import('../components/admin/AiConfigManagement.vue'),
      meta: { requiresAuth: true, requiresAdmin: true, title: 'AI配置管理' }
    },
    {
      path: '/admin/audit',
      name: 'ContentAuditManagement',
      component: () => import('../components/admin/ContentAuditManagement.vue'),
      meta: { requiresAuth: true, requiresAdmin: true, title: '内容审核' }
    },
    {
      path: '/admin/report',
      name: 'ReportManagement',
      component: () => import('../components/admin/ReportManagement.vue'),
      meta: { requiresAuth: true, requiresAdmin: true, title: '举报管理' }
    },
    {
      path: '/admin/withdraw',
      name: 'WithdrawManagement',
      component: () => import('../components/admin/WithdrawManagement.vue'),
      meta: { requiresAuth: true, requiresAdmin: true, title: '提现管理' }
    },
    {
      path: '/profile',
      name: 'Profile',
      component: () => import('../views/Profile.vue'),
      meta: { requiresAuth: true, title: '个人中心' }
    },
    {
      path: '/watch-history',
      name: 'WatchHistory',
      component: () => import('../components/WatchHistory.vue'),
      meta: { requiresAuth: true, title: '观看历史' }
    },
    {
      path: '/security',
      name: 'SecuritySettings',
      component: () => import('../components/SecuritySettings.vue'),
      meta: { requiresAuth: true, title: '账号安全' }
    },
    {
      path: '/notifications',
      name: 'NotificationSettings',
      component: () => import('../components/NotificationSettings.vue'),
      meta: { requiresAuth: true, title: '通知设置' }
    }
  ]
});

router.beforeEach((to, _from, next) => {
  const token = localStorage.getItem('token');
  const userType = localStorage.getItem('userType') as UserType | null;

  if (to.meta.requiresAuth && !token) {
    next({ path: '/login', query: { redirect: to.fullPath } });
    return;
  }

  if (to.meta.requiresAdmin && userType !== '1') {
    next({ path: '/' });
    return;
  }

  if ((to.path === '/login' || to.path === '/register') && token) {
    next({ path: '/' });
    return;
  }

  next();
});

export default router;
