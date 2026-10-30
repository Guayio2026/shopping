import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '@/stores/user'

const routes = [
  {
    path: '/',
    redirect: '/user/home'
  },
  {
    path: '/login',
    name: 'UserLogin',
    component: () => import('@/views/UserLogin.vue'),
    meta: { public: true }
  },
  {
    path: '/register',
    name: 'UserRegister',
    component: () => import('@/views/UserRegister.vue'),
    meta: { public: true }
  },
  {
    path: '/user',
    component: () => import('@/layouts/UserLayout.vue'),
    meta: { public: true },
    redirect: '/user/home',
    children: [
      {
        path: 'home',
        name: 'UserHome',
        component: () => import('@/views/user/Home.vue'),
        meta: { public: true, title: '用户首页' }
      },
      // ======== 全部商品页（第六章新增）========
      // ⚠️ meta.public 必须有 —— 不加的话路由守卫会要求登录，游客打不开
      // ⚠️ path 不带开头斜杠（写成 '/goods' 会变成根路径，/user/goods 就访问不到了）
      // ⚠️ 组件在 views/user/ 目录下，别写成 @/views/Goods.vue（那是管理后台的商品管理）
      {
        path: 'goods',
        name: 'UserGoods',
        component: () => import('@/views/user/GoodsList.vue'),
        meta: { public: true, title: '全部商品' }
      },
      {
        path: 'goods/:id',
        name: 'UserGoodsDetail',
        component: () => import('@/views/user/GoodsDetail.vue'),
        meta: { public: true, title: '商品详情' }
      },
      // ======== 我的收藏（第八章新增）========
      // ⚠️ meta 里【不能】写 public: true —— 这页要登录才看得到自己的收藏
      // ⚠️ requiresUser: true 决定未登录时跳 /login（而不是后台的 /admin/login），登录后回来
      // ⚠️ path 不带开头斜杠（写成 '/collect' 就变成根路径 /collect，/user/collect 打不开了）
      {
        path: 'collect',
        name: 'UserMyCollect',
        component: () => import('@/views/user/MyCollect.vue'),
        meta: { requiresAuth: true, requiresUser: true, title: '我的收藏' }
      },

      // ======== 我的订单（第九章新增）========
      // ⚠️ meta 里【不能】写 public: true —— 订单是隐私数据，必须登录才能看
      // ⚠️ requiresUser: true 决定未登录时跳 /login（而不是后台的 /admin/login）
      // ⚠️ path 不带开头斜杠（写成 '/orders' 会变成根路径，/user/orders 就打不开了）
      // ⚠️ 组件在 views/user/ 目录下；后台那个订单页是 views/Orders.vue（没有 user/），别写混
      {
        path: 'orders',
        name: 'UserMyOrders',
        component: () => import('@/views/user/MyOrders.vue'),
        meta: { requiresAuth: true, requiresUser: true, title: '我的订单' }
      },

      {
        path: 'profile',
        name: 'UserProfile',
        component: () => import('@/views/user/Profile.vue'),
        meta: { requiresAuth: true, requiresUser: true, title: '个人中心' }
      }
    ]
  },
  {
    path: '/admin/login',
    name: 'AdminLogin',
    component: () => import('@/views/Login.vue'),
    meta: { public: true, admin: true }
  },
  {
    path: '/admin',
    component: () => import('@/layouts/MainLayout.vue'),
    redirect: '/admin/home',
    meta: { admin: true, requiresAuth: true, requiresAdmin: true },
    children: [
      {
        path: 'home',
        name: 'AdminHome',
        component: () => import('@/views/Home.vue'),
        meta: { title: '首页', admin: true }
      },
      {
        path: 'admins',
        name: 'AdminAdmins',
        component: () => import('@/views/Admins.vue'),
        meta: { title: '管理员管理', admin: true }
      },
      {
        path: 'users',
        name: 'AdminUsers',
        component: () => import('@/views/Users.vue'),
        meta: { title: '用户管理', admin: true }
      },
      {
        path: 'menus',
        name: 'AdminMenus',
        component: () => import('@/views/Menus.vue'),
        meta: { title: '菜单管理', admin: true }
      },
      // ======== 商品分类管理（新增的模块，仿照上面几条自己写的）========
      // path: 浏览器地址 /admin/types；component: 对应页面文件；meta.title: 标签页标题
      {
        path: 'types',
        name: 'AdminTypes',
        component: () => import('@/views/Types.vue'),
        meta: { title: '分类管理', admin: true }
      },
      // ======== 商品管理（goods 模块）========
      {
        path: 'goods',
        name: 'AdminGoods',
        component: () => import('@/views/Goods.vue'),
        meta: { title: '商品管理', admin: true }
      },
      // ======== 轮播图2（carousel 模块）========
      // ⚠️ path 子路由不带开头斜杠（写 '/carousels' 会变成根路径，页面打不开）
      //      注意和上面那条 'banners'（现有轮播图管理）是两个不同的页面，别搞混
      {
        path: 'carousels',
        name: 'AdminCarousels',
        component: () => import('@/views/Carousels.vue'),
        meta: { title: '轮播图2', admin: true }
      },
      // ======== 收藏管理（第八章新增）========
      // ⚠️ 组件是 views/Collects.vue（后台，有 s），前台那页是 views/user/MyCollect.vue —— 别写混
      // /admin 这层父路由已带 requiresAuth + requiresAdmin，子路由只写 title + admin 即可
      {
        path: 'collects',
        name: 'AdminCollects',
        component: () => import('@/views/Collects.vue'),
        meta: { title: '收藏管理', admin: true }
      },

      // ======== 订单管理（第九章新增）========
      // ⚠️ 组件是 views/Orders.vue（后台，没有 user/），前台那页是 views/user/MyOrders.vue —— 别写混
      // ⚠️ /admin 这层父路由已带 requiresAuth + requiresAdmin，子路由只写 title + admin 即可
      {
        path: 'orders',
        name: 'AdminOrders',
        component: () => import('@/views/Orders.vue'),
        meta: { title: '订单管理', admin: true }
      },
      // ======== 公告管理（第十一章新增）========
      // ⚠️ path 不带开头斜杠（写成 '/notices' 会变成根路径，/admin/notices 就打不开了）
      // ⚠️ 组件是 views/Notices.vue（后台页），前台首页是 views/user/Home.vue —— 别写混
      {
        path: 'notices',
        name: 'AdminNotices',
        component: () => import('@/views/Notices.vue'),
        meta: { title: '公告管理', admin: true }
      },

      {
        path: 'banners',
        name: 'AdminBanners',
        component: () => import('@/views/system/Banners.vue'),
        meta: { title: '轮播图管理', admin: true }
      },
      {
        path: 'logs',
        name: 'AdminLogs',
        component: () => import('@/views/Logs.vue'),
        meta: { title: '系统日志', admin: true }
      },
      {
        path: 'profile',
        name: 'AdminProfile',
        component: () => import('@/views/Profile.vue'),
        meta: { title: '个人中心', admin: true }
      },
      {
        path: 'settings',
        name: 'AdminSettings',
        component: () => import('@/views/Settings.vue'),
        meta: { title: '系统设置', admin: true }
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes,
  scrollBehavior(to, from, savedPosition) {
    if (to.path.startsWith('/admin') && from.path.startsWith('/admin')) {
      const main = document.querySelector('.layout .main')
      if (main) main.scrollTop = 0
      return false
    }
    if (savedPosition) return savedPosition
    return { top: 0 }
  }
})

function isAdminAccount(user) {
  return user?.accountType === 'ADMIN' || user?.role === 'ADMIN'
}

router.beforeEach(async (to) => {
  const userStore = useUserStore()
  const token = localStorage.getItem('token')
  const isAdminLogin = to.path === '/admin/login'
  const isUserLogin = to.path === '/login'
  const isUserRegister = to.path === '/register'
  const isAdminRoute = to.path.startsWith('/admin') && !isAdminLogin

  // 已登录访问登录/注册页时跳转
  if (to.meta.public && !to.meta.requiresAuth) {
    if (token && (isAdminLogin || isUserLogin || isUserRegister)) {
      if (!userStore.user) {
        try {
          await userStore.fetchUser()
        } catch {
          userStore.logout()
          return true
        }
      }
      if (isAdminLogin && isAdminAccount(userStore.user)) {
        return '/admin/home'
      }
      if ((isUserLogin || isUserRegister) && !isAdminAccount(userStore.user)) {
        return '/user/home'
      }
    }
    return true
  }

  // 管理后台 / 需要登录的用户页
  if (to.meta.requiresAuth || isAdminRoute) {
    if (!token) {
      const loginPath = to.meta.requiresUser ? '/login' : '/admin/login'
      return { path: loginPath, query: { redirect: to.fullPath } }
    }

    if (!userStore.user) {
      try {
        await userStore.fetchUser()
      } catch {
        userStore.logout()
        const loginPath = to.meta.requiresUser ? '/login' : '/admin/login'
        return { path: loginPath, query: { redirect: to.fullPath } }
      }
    }

    if (to.meta.requiresAdmin || isAdminRoute) {
      if (!isAdminAccount(userStore.user)) {
        userStore.logout()
        return { path: '/admin/login' }
      }
    }

    if (to.meta.requiresUser && isAdminAccount(userStore.user)) {
      return '/admin/home'
    }
  }

  return true
})

export default router
