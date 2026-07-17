import axios from 'axios'
import { ElMessage, ElNotification } from 'element-plus'
import router from '@/router'
import { useUserStore } from '@/store/modules/user'

const service = axios.create({
  baseURL: import.meta.env.VITE_APP_BASE_API || '/api',
  timeout: 30000,
  headers: { 'Content-Type': 'application/json;charset=utf-8' }
})

service.interceptors.request.use(
  config => {
    const token = localStorage.getItem('shenji_token')
    if (token) {
      config.headers['shenji-token'] = token
    }
    return config
  },
  error => Promise.reject(error)
)

service.interceptors.response.use(
  response => {
    const res = response.data
    if (res.code !== 200 && res.code !== 0) {
      ElMessage.error(res.msg || '请求失败')
      if (res.code === 401 || res.code === 403) {
        useUserStore().resetState()
        localStorage.removeItem('shenji_token')
        router.push('/login')
      }
      return Promise.reject(new Error(res.msg || '请求失败'))
    }
    return res.data
  },
  error => {
    if (error.response) {
      const { status } = error.response
      switch (status) {
        case 401:
          ElNotification.error({ title: '未授权', message: '登录已过期，请重新登录' })
          useUserStore().resetState()
          localStorage.removeItem('shenji_token')
          router.push('/login')
          break
        case 403:
          ElNotification.error({ title: '访问被拒绝', message: '权限不足' })
          break
        case 404:
          ElNotification.warning({ title: '未找到', message: '接口不存在' })
          break
        case 500:
          ElNotification.error({ title: '服务器错误', message: (error.response.data && error.response.data.msg) || '请联系管理员' })
          break
        default:
          ElMessage.error(error.message)
      }
    } else {
      ElMessage.error('网络错误，请检查连接')
    }
    return Promise.reject(error)
  }
)

export default service
