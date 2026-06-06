import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  {
    path: '/',
    component: () => import('../layouts/MainLayout.vue'),
    children: [
      { path: '', name: 'Home', component: () => import('../views/home/HomePage.vue') },
      { path: 'products', name: 'ProductList', component: () => import('../views/product/ProductList.vue') },
      { path: 'product/:id', name: 'ProductDetail', component: () => import('../views/product/ProductDetail.vue') },
      { path: 'cart', name: 'Cart', component: () => import('../views/cart/CartPage.vue'), meta: { auth: true } },
      { path: 'checkout', name: 'Checkout', component: () => import('../views/order/CheckoutPage.vue'), meta: { auth: true } },
      { path: 'thank-you', name: 'ThankYou', component: () => import('../views/order/ThankYouPage.vue') },
      { path: 'login', name: 'Login', component: () => import('../views/user/LoginPage.vue') },
      { path: 'register', name: 'Register', component: () => import('../views/user/RegisterPage.vue') },
      { path: 'user/profile', name: 'Profile', component: () => import('../views/user/ProfilePage.vue'), meta: { auth: true } },
      { path: 'user/orders', name: 'UserOrders', component: () => import('../views/user/OrderListPage.vue'), meta: { auth: true } },
      { path: 'user/aftersale', name: 'AfterSale', component: () => import('../views/user/AfterSalePage.vue'), meta: { auth: true } },
    ],
  },
  {
    path: '/admin',
    component: () => import('../layouts/AdminLayout.vue'),
    meta: { auth: true, admin: true },
    children: [
      { path: '', name: 'Dashboard', component: () => import('../views/admin/Dashboard.vue') },
      { path: 'products', name: 'AdminProducts', component: () => import('../views/admin/ProductManage.vue') },
      { path: 'products/add', name: 'AddProduct', component: () => import('../views/admin/ProductForm.vue') },
      { path: 'products/:id', name: 'EditProduct', component: () => import('../views/admin/ProductForm.vue') },
      { path: 'orders', name: 'AdminOrders', component: () => import('../views/admin/OrderManage.vue') },
      { path: 'users', name: 'AdminUsers', component: () => import('../views/admin/UserManage.vue') },
      { path: 'categories', name: 'AdminCategories', component: () => import('../views/admin/CategoryManage.vue') },
      { path: 'settings/site', name: 'SiteSettings', component: () => import('../views/admin/SiteSettings.vue') },
      { path: 'settings/pages', name: 'PageSettings', component: () => import('../views/admin/PageSettings.vue') },
      { path: 'settings/marketing', name: 'MarketingSettings', component: () => import('../views/admin/MarketingSettings.vue') },
      { path: 'service/shipping', name: 'ShippingService', component: () => import('../views/admin/ShippingService.vue') },
      { path: 'service/aftersales', name: 'AfterSalesService', component: () => import('../views/admin/AfterSalesService.vue') },
      { path: 'service/inquiries', name: 'InquiryManage', component: () => import('../views/admin/InquiryManage.vue') },
    ],
  },
]

const router = createRouter({
  history: createWebHistory(),
  routes,
})

router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('token')
  const user = JSON.parse(localStorage.getItem('user') || 'null')
  if (to.meta.auth && !token) {
    next('/login')
  } else if (to.meta.admin && user?.role !== 'ADMIN') {
    next('/')
  } else {
    next()
  }
})

export default router
