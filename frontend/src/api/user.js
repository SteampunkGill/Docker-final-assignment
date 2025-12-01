import request from '@/utils/request'

export function getUserProfile() {
  console.log('【数据发出】即将向后端发送[获取用户信息]请求');
  return request({
    url: '/users/profile',
    method: 'get'
  })
}

export function getAddressList() {
  console.log('【数据发出】即将向后端发送[获取地址列表]请求');
  return request({
    url: '/addresses',
    method: 'get'
  })
}

export function addAddress(data) {
  console.log('【数据发出】即将向后端发送[新增地址]请求:', data);
  return request({
    url: '/addresses',
    method: 'post',
    data
  })
}