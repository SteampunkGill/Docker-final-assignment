import request from '@/utils/request'

/**
 * 获取商品列表
 * @param {object} params - 包含分页、搜索名称、分类ID等参数
 * @returns {Promise}
 */
export function getProductList(params) {
  console.log('【数据发出】即将向后端发送[获取商品列表]请求:', { params });
  return request({
    url: '/products',
    method: 'get',
    params
  })
}

/**
 * 获取商品详情
 * @param {string | number} id - 商品ID
 * @returns {Promise}
 */
export function getProductDetail(id) {
  console.log('【数据发出】即将向后端发送[获取商品详情]请求:', { id });
  return request({
    url: `/products/${id}`,
    method: 'get'
  })
}

/**
 * 创建新商品 (管理员接口)
 * @param {object} data - 商品数据
 * @returns {Promise}
 */
export function createProduct(data) {
  console.log('【数据发出】即将向后端发送[创建商品]请求:', { data });
  return request({ 
    url: '/products/admin', 
    method: 'post', 
    data 
  })
}

/**
 * 更新商品信息 (管理员接口)
 * @param {string | number} id - 商品ID
 * @param {object} data - 更新的商品数据
 * @returns {Promise}
 */
export function updateProduct(id, data) {
  console.log('【数据发出】即将向后端发送[更新商品]请求:', { id, data });
  return request({ 
    url: `/products/admin/${id}`, 
    method: 'put', 
    data 
  })
}

/**
 * 删除商品 (管理员接口)
 * @param {string | number} id - 商品ID
 * @returns {Promise}
 */
export function deleteProduct(id) {
  console.log('【数据发出】即将向后端发送[删除商品]请求:', { id });
  return request({ 
    url: `/products/admin/${id}`, 
    method: 'delete' 
  })
}