import { createRouter, createWebHistory } from 'vue-router'
import Layout from '@/views/Layout.vue'

const routes = [
  {
    path: '/',
    redirect: '/correct'
  },
  {
    path: '/',
    component: Layout,
    children: [
      {
        path: 'correct',
        name: 'Correct',
        component: () => import('@/views/Correct.vue')
      },
      {
        path: 'topic',
        name: 'Topic',
        component: () => import('@/views/Topic.vue')
      },
      {
        path: 'reference',
        name: 'Reference',
        component: () => import('@/views/Reference.vue')
      },
      {
        path: 'history',
        name: 'History',
        component: () => import('@/views/History.vue')
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

export default router