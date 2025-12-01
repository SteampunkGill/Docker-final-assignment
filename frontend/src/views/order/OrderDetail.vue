<template>
  <div class="order-detail-page">
    <h2>订单详情</h2>
    <div v-if="order" class="order-detail-container">
      <div class="section">
        <h3>订单信息</h3>
        <p><strong>订单号:</strong> {{ order.orderNo }}</p>
        <p><strong>状态:</strong> {{ formatStatus(order.status) }}</p>
        <p><strong>总金额:</strong> <span class="amount">¥{{ order.totalAmount }}</span></p>
        <p><strong>下单时间:</strong> {{ new Date(order.createTime).toLocaleString() }}</p>
    
        <div v-if="order.status === 0" class="pay-action">
          <button @click="handlePay" class="pay-btn">立即支付</button>
        </div>
      </div>
      <div class="section">
        <h3>收货信息</h3>
        <p><strong>收货人:</strong> {{ order.receiverInfo.receiverName }}</p>
        <p><strong>联系电话:</strong> {{ order.receiverInfo.receiverPhone }}</p>
        <p><strong>收货地址:</strong> {{ `${order.receiverInfo.province} ${order.receiverInfo.city} ${order.receiverInfo.district} ${order.receiverInfo.detail}` }}</p>
      </div>
      <div class="section">
        <h3>商品列表</h3>
        <div class="order-item" v-for="item in order.items" :key="item.id">
          <img :src="item.imageUrl || 'https://via.placeholder.com/80'" alt="">
          <div class="item-details">
            <p>{{ item.productName }}</p>
            <p>单价: ¥{{ item.price }}</p>
          </div>
          <div class="item-quantity">
            x {{ item.quantity }}
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { useRoute } from 'vue-router';

import { getOrderDetail, payOrder } from '@/api/order';

const route = useRoute();
const order = ref(null);

const fetchOrderDetail = async () => {
  try {
    const response = await getOrderDetail(route.params.orderNo);
    console.log('【数据接收】收到后端订单详情响应:', response);
    order.value = response;
  } catch (error) {
    console.error("获取订单详情失败:", error);
  }
};


const handlePay = async () => {
  try {
    await payOrder(order.value.orderNo);
    alert('支付成功！');
    // 更新当前页面的状态
    order.value.status = 1;
  } catch (error) {
    console.error("支付失败:", error);
  }
};

const formatStatus = (status) => {
  const statuses = { 0: '待付款', 1: '已付款', 2: '已发货', 3: '已完成', 4: '已取消' };
  return statuses[status] || '未知';
};

onMounted(fetchOrderDetail);
</script>

<style scoped>
.order-detail-container {
  background: #fff;
  padding: 20px;
  border-radius: 8px;
}
.section {
  margin-bottom: 20px;
  border-bottom: 1px solid #eee;
  padding-bottom: 20px;
}
.section:last-child {
  border-bottom: none;
}
h3 {
  margin-bottom: 15px;
}
p {
  line-height: 1.8;
  color: #555;
}
.amount {
  color: #e44d26;
  font-weight: bold;
}
.order-item {
  display: flex;
  align-items: center;
  gap: 15px;
  margin-bottom: 10px;
}
.order-item img {
  width: 80px;
  height: 80px;
}
.item-details {
  flex-grow: 1;
}
.item-quantity {
    font-weight: bold;
}

.pay-action {
  margin-top: 20px;
}
.pay-btn {
  background-color: #ff9800;
  padding: 10px 25px;
  font-size: 16px;
}
</style>