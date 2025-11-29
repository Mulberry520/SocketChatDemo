import axios from "axios";
import type { AxiosInstance } from "axios";

const request: AxiosInstance = axios.create({
  baseURL: 'http://localhost:8080/api',
  withCredentials: true,
  timeout: 10000,
  headers: {
    'Content-Type': 'application/json'
  },
  responseType: 'json',
})

const whiteList: string[] = [
  '/auth/register',
  '/auth/login',
  '/auth/refresh'
]

let isRefreshing: boolean = false
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

request.interceptors.request.use(
  (config) => {
    const url = config.url || ''

    if (!whiteList.some((path) => url.startsWith(path))) {
      const accessToken = localStorage.getItem('accessToken')
      if (accessToken) {
        config.headers.Authorization = `Bearer ${accessToken}`
      } else {
        console.warn("No access token")
      }
    }

    return config
  },
  (error) => {
    return Promise.reject(error)
  }
)

request.interceptors.response.use(
  (response) => {
    let result = response.data

    if (result && typeof result === 'object' && 'code' in result) {
      if (result.code === 200) {
        return result
      }
      const error = new Error(result.msg || 'Request failed')
      ;(error as any).response = { data: result, status: result.code }
      ;(error as any).config = response.config
      return Promise.reject(error)
    }

    return result
  },
  async (error) => {
    const originRequest = error.config
    if (originRequest.url === '/auth/refresh') {
      localStorage.removeItem('accessToken')
      return Promise.reject(error)
    }

    const isTokenExpire =
      error.response?.data.code === 401 ||
      error.response?.status === 401

    if (isTokenExpire && !originRequest._retry) {
      originRequest._retry = true

      if (isRefreshing) {
        return new Promise((resolve, reject) => {
          failedQueue.push({resolve, reject})
        }).then(token => {
          originRequest.headers.Authorization = `Bearer ${token}`
          return request(originRequest)
        }).catch(error => Promise.reject(error))
      } else {
        isRefreshing = true

        try {
          const result = await request.get('/auth/refresh')
          const newAccessToken = result.data
          localStorage.setItem('accessToken', newAccessToken)

          originRequest.headers.Authorization = `Bearer ${newAccessToken}`
          processQueue(null, newAccessToken)
          return request(originRequest)
        } catch (error) {
          processQueue(error)
          localStorage.removeItem('accessToken')
          return Promise.reject(error)
        } finally {
          isRefreshing = false
        }
      }
    }

    return Promise.reject(error)
  }
)

export default request
