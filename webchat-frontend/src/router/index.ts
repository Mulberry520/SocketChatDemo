import {createRouter, createWebHistory} from 'vue-router'
import type { RouteRecordRaw, Router } from 'vue-router'
import Home from "@/views/Home.vue";


const routes: RouteRecordRaw[] = [
  {
    path: '/',
    component: Home
  }

]

const router: Router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes
})


export { router }

export default router
