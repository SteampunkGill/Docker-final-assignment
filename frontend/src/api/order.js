import request from '@/utils/request'

export function createOrder(data) {
  console.log('【数据发出】即将向后端发送[创建订单]请求:', data);
  return request({
    url: '/orders/create',
    method: 'post',
    data
  })
}

export function getOrderList(params) {
  console.log('【数据发出】即将向后端发送[获取订单列表]请求:', { params });
  return request({
    url: '/orders',
    method: 'get',
    params
  })
}

export function getOrderDetail(orderNo) {
  console.log(`【数据发出】即将向后端发送[获取订单详情 ${orderNo}]请求`);
  return request({
    url: `/orders/${orderNo}`,
    method: 'get'
  })
}

// [新增] 添加模拟支付的API函数
export function payOrder(orderNo) {
  console.log(`【数据发出】即将向后端发送[模拟支付]请求, 订单号: ${orderNo}`);
  return request({
    url: `/orders/${orderNo}/pay`,
    method: 'post'
  })
}