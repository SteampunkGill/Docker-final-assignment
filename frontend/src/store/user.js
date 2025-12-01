import { defineStore } from 'pinia'
import { ref } from 'vue'
import request from '@/utils/request'

export const useUserStore = defineStore('user', () => {
  const token = ref(localStorage.getItem('token') || '')
  const userInfo = ref({})

  const login = async (credentials) => {
    console.log('【数据发出】即将向后端发送[用户登录]请求:', credentials);
    const data = await request({
      url: '/users/login',
      method: 'post',
      data: credentials
    })
    console.log('【数据接收】收到后端响应:', data);
    token.value = data.token
    localStorage.setItem('token', data.token)
  }

  const logout = () => {
    token.value = ''
    userInfo.value = {}
    localStorage.removeItem('token')
  }

  return { token, userInfo, login, logout }
})