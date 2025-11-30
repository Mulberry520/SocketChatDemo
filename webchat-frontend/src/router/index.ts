import {createRouter, createWebHistory} from 'vue-router'
import type { RouteRecordRaw, Router } from 'vue-router'
import LoginView from "@/views/LoginView.vue";
import DashboardView from "@/views/DashboardView.vue";
import WebSocketTest from "@/components/WebSocketTest.vue";


const routes: RouteRecordRaw[] = [
  {
    path: '/dashboard',
    component: DashboardView
  },
  {
    path: '/login',
    component: LoginView
  },
  {
    path: '/',
    redirect: '/login'
  },
  {
    path: '/websocket',
    component: WebSocketTest
  }
]

const router: Router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes
})

router.beforeEach((to, from, next) => {
  const publicPages = ['/login', '/register']
  const authRequired = !publicPages.includes(to.path)
  const token = localStorage.getItem('accessToken')

  if (authRequired && !token) {
    return next('/login')
  }
  next()
})

export default router
