import { createRouter, createWebHistory } from 'vue-router';

const router = createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: '/login',
      name: 'Login',
      component: () => import('../components/Login.vue'),
      meta: { requiresAuth: false }
    },
    {
      path: '/register',
      name: 'Register',
      component: () => import('../components/Register.vue'),
      meta: { requiresAuth: false }
    },
    {
      path: '/',
      name: 'Home',
      component: () => import('../components/LiveList.vue'),
      meta: { requiresAuth: false }
    },
    {
      path: '/live/:id',
      name: 'LiveRoom',
      component: () => import('../components/LiveRoom.vue'),
      meta: { requiresAuth: false }
    },
    {
      path: '/admin',
      name: 'AdminDashboard',
      component: () => import('../components/admin/AdminDashboard.vue'),
      meta: { requiresAuth: true, requiresAdmin: true }
    },
    {
      path: '/admin/analytics',
      name: 'AnalyticsDashboard',
      component: () => import('../components/admin/AnalyticsDashboard.vue'),
      meta: { requiresAuth: true, requiresAdmin: true }
    },
    {
      path: '/admin/live',
      name: 'LiveManagement',
      component: () => import('../components/admin/LiveManagement.vue'),
      meta: { requiresAuth: true, requiresAdmin: true }
    },
    {
      path: '/admin/user',
      name: 'UserManagement',
      component: () => import('../components/admin/UserManagement.vue'),
      meta: { requiresAuth: true, requiresAdmin: true }
    },
    {
      path: '/anchor',
      name: 'AnchorDashboard',
      component: () => import('../components/anchor/AnchorDashboard.vue'),
      meta: { requiresAuth: true, requiresAnchor: true }
    }
  ]
});

// 路由守卫
router.beforeEach((to, _from, next) => {
  const token = localStorage.getItem('token');
  const userType = localStorage.getItem('userType');

  if (to.meta.requiresAuth) {
    if (!token) {
      // 未登录，跳转到登录页
      next({ path: '/login' });
    } else if (to.meta.requiresAdmin && userType !== '2') {
      // 非管理员，跳转到首页
      next({ path: '/' });
    } else if (to.meta.requiresAnchor && userType !== '1' && userType !== '2') {
      // 非主播或管理员，跳转到首页
      next({ path: '/' });
    } else {
      next();
    }
  } else {
    next();
  }
});

export default router;
