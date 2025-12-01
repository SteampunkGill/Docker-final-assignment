import request from '@/utils/request'

export function getCategoryList() {
  console.log('【数据发出】即将向后端发送[获取分类列表]请求');
  return request({
    url: '/categories',
    method: 'get'
  })
}