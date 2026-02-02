import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  {
    path: '/',
    name: 'Home',
    component: () => import('@/views/PaymentPage.vue')
  },
  {
    path: '/payment/:orderId',
    name: 'Payment',
    component: () => import('@/views/PaymentPage.vue'),
    props: true
  },
  {
    path: '/payment/success',
    name: 'PaymentSuccess',
    component: () => import('@/views/PaymentSuccessPage.vue')
  },
  {
    path: '/payment/fail',
    name: 'PaymentFail',
    component: () => import('@/views/PaymentFailPage.vue')
  },
  {
    path: '/payment/history',
    name: 'PaymentHistory',
    component: () => import('@/views/PaymentHistoryPage.vue')
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

export default router
