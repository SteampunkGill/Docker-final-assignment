import request from '@/utils/request'

export function addToCart(data) {
  console.log('【数据发出】即将向后端发送[添加购物车]请求:', data);
  return request({
    url: '/carts',
    method: 'post',
    data // { productId: ..., quantity: ... }
  })
}

export function getCartList() {
    console.log('【数据发出】即将向后端发送[获取购物车列表]请求');
    return request({
        url: '/carts',
        method: 'get'
    });
}

export function updateCartItem(id, data) {
    console.log(`【数据发出】即将向后端发送[更新购物车项 ${id}]请求:`, data);
    return request({
        url: `/carts/${id}`,
        method: 'put',
        data // { quantity: ... }
    });
}

export function deleteCartItem(id) {
    console.log(`【数据发出】即将向后端发送[删除购物车项 ${id}]请求`);
    return request({
        url: `/carts/${id}`,
        method: 'delete'
    });
}