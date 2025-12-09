import axios from 'axios'
import type { AxiosInstance, InternalAxiosRequestConfig } from 'axios'
import { useUserStore } from "@/stores/userStore.ts"


const request: AxiosInstance = axios.create({
  baseURL: 'http://localhost:8080/api',
  withCredentials: true,
  timeout: 10000,
  headers: {
    'Content-Type': 'application/json'
  },
  responseType: 'json'
})

const whiteList: string[] = [
  '/auth/register',
  '/auth/login',
  '/auth/refresh'
]

let isRefreshing = false

interface QueueItem {
  resolve: (token: string) => void
  reject: (error: any) => void
}
let failedQueue: QueueItem[] = []

const processQueue = (error: any, token?: string) => {
  failedQueue.forEach(promise => {
    if (error) {
      promise.reject(error)
    } else if (token) {
      promise.resolve(token)
    } else {
      promise.reject(new Error('No token available'))
    }
  })
  failedQueue = []
}

// 请求拦截器
request.interceptors.request.use(
  async (config: InternalAxiosRequestConfig) => {
    const url = config.url || ''
    const userStore = useUserStore()

    if (!whiteList.some(path => url.startsWith(path))) {
      const accessToken = userStore.accessToken

      if (accessToken) {
        config.headers.Authorization = `Bearer ${accessToken}`
      } else {
        console.warn('No access token found in store')
      }
    }

    return config
  },
  (error) => {
    return Promise.reject(error)
  }
)

// 响应拦截器
request.interceptors.response.use(
  (response) => {
    const result = response.data

    if (result && typeof result === 'object' && 'code' in result) {
      if (result.code === 200) {
        return result
      }
      const error: any = new Error(result.msg || 'Request failed')
      error.response = { data: result, status: result.code }
      error.config = response.config
      return Promise.reject(error)
    }

    return result
  },
  async (error) => {
    const originRequest = error.config
    const userStore = useUserStore()

    if (originRequest.url === '/auth/refresh') {
      userStore.clearToken()
      return Promise.reject(error)
    }

    const isTokenExpired =
      error.response?.data?.code === 401 ||
      error.response?.status === 401

    if (isTokenExpired && !originRequest._retry) {
      originRequest._retry = true

      if (isRefreshing) {
        return new Promise((resolve, reject) => {
          failedQueue.push({ resolve, reject })
        }).then(async (token) => {
          originRequest.headers.Authorization = `Bearer ${token}`
          return request(originRequest)
        }).catch(err => Promise.reject(err))
      } else {
        isRefreshing = true

        try {
          const refreshResult = await request.get('/auth/refresh')
          const newAccessToken = refreshResult.data
          userStore.setAccessToken(newAccessToken)

          originRequest.headers.Authorization = `Bearer ${newAccessToken}`
          processQueue(null, newAccessToken)
          return request(originRequest)
        } catch (refreshError) {
          processQueue(refreshError)
          userStore.clearToken()

          window.location.href = '/login'
          return Promise.reject(refreshError)
        } finally {
          isRefreshing = false
        }
      }
    }
    return Promise.reject(error)
  }
)

export default request
