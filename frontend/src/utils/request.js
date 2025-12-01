import axios from 'axios'
import { useUserStore } from '../store/user'
import router from '../router'

// 创建axios实例
const service = axios.create({
  baseURL: '/api', // Vite代理将/api转发到后端
  timeout: 10000 // 请求超时时间
})

// 请求拦截器
service.interceptors.request.use(
  config => {
    const userStore = useUserStore()
    // 如果存在token，则每个http header都加上token
    if (userStore.token) {
      config.headers['Authorization'] = `Bearer ${userStore.token}`
    }
    return config
  },
  error => {
    console.log(error) // for debug
    return Promise.reject(error)
  }
)

// 响应拦截器
service.interceptors.response.use(
  response => {
    const res = response.data
    // code不为200则视为错误
    if (res.code !== 200) {
      // 简单提示错误信息
      alert(res.message || 'Error')

      // 401: Token失效;
      if (res.code === 401) {
        const userStore = useUserStore()
        userStore.logout()
        router.push('/login')
      }
      return Promise.reject(new Error(res.message || 'Error'))
    } else {
      // code为200，直接返回data
      return res.data
    }
  },
  error => {
    console.error('err' + error) // for debug
    alert(error.message)
    return Promise.reject(error)
  }
)

export default service