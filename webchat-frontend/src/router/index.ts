import {createRouter, createWebHistory} from 'vue-router'
import type { RouteRecordRaw, Router } from 'vue-router'
import LoginView from "@/views/LoginView.vue";
import TestMainView from "@/views/Main.vue";


const routes: RouteRecordRaw[] = [
  {
    path: '/login',
    component: LoginView
  },
  {
    path: '/',
    redirect: '/main'
  },
  {
    path: '/main',
    component: TestMainView
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
