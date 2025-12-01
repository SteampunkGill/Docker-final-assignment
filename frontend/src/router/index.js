import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '../store/user'
import Home from '../views/Home.vue'

const routes = [
  { path: '/', name: 'Home', component: Home },
  { path: '/login', name: 'Login', component: () => import('../views/Login.vue') },
  { 
    path: '/products/:id', 
    name: 'ProductDetail', 
    component: () => import('../views/product/ProductDetail.vue') 
  },
  { 
    path: '/cart', 
    name: 'Cart', 
    component: () => import('../views/Cart.vue'),
    meta: { requiresAuth: true }
  },
  { 
    path: '/orders', 
    name: 'OrderList', 
    component: () => import('../views/order/OrderList.vue'),
    meta: { requiresAuth: true }
  },
  { 
    path: '/orders/confirm', 
    name: 'OrderConfirm', 
    component: () => import('../views/order/OrderConfirm.vue'),
    meta: { requiresAuth: true }
  },
  { 
    path: '/orders/:orderNo', 
    name: 'OrderDetail', 
    component: () => import('../views/order/OrderDetail.vue'),
    meta: { requiresAuth: true }
  },
  // [新增] 用户中心路由
  {
    path: '/profile',
    name: 'Profile',
    component: () => import('../views/user/Profile.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/address',
    name: 'Address',
    component: () => import('../views/user/Address.vue'),
    meta: { requiresAuth: true }
  },
  // ...
  // [新增] 管理员商品管理路由
  {
    path: '/admin/products',
    name: 'AdminProduct',
    component: () => import('../views/admin/Product.vue'),
    meta: { requiresAuth: true } // 实际项目中还应有管理员权限校验
  },
// ...
  // 404 Page
  { path: '/:pathMatch(.*)*', name: 'NotFound', component: () => import('../views/NotFound.vue') }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 全局前置守卫 (保持不变)
router.beforeEach((to, from, next) => {
  const userStore = useUserStore()
  if (to.meta.requiresAuth && !userStore.token) {
    next({ name: 'Login' })
  } else {
    next()
  }
})

export default router