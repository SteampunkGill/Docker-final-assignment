<template>
  <div class="order-list-page">
    <h2>我的订单</h2>
    <div class="order-card" v-for="order in orders" :key="order.id">
      <div class="order-header" @click="goToDetail(order.orderNo)">
        <span>订单号: {{ order.orderNo }}</span>
        <span :class="['status', getStatusClass(order.status)]">{{ formatStatus(order.status) }}</span>
      </div>
      <div class="order-body" @click="goToDetail(order.orderNo)">
        <p>下单时间: {{ new Date(order.createTime).toLocaleString() }}</p>
        <p>总金额: <span class="amount">¥{{ order.totalAmount }}</span></p>
      </div>

      <div class="order-footer" v-if="order.status === 0">
        <button @click.stop="handlePay(order)" class="pay-btn">去支付</button>
      </div>
    </div>
    <div v-if="orders.length === 0">
        <p>您还没有任何订单。</p>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';

import { getOrderList, payOrder } from '@/api/order';

const router = useRouter();
const orders = ref([]);

const fetchOrders = async () => {
  try {
    const response = await getOrderList({ page: 1, size: 20 });
    console.log('【数据接收】收到后端订单列表响应:', response);
    orders.value = response.records;
  } catch (error) {
    console.error("获取订单列表失败:", error);
  }
};


const handlePay = async (order) => {
  try {
    await payOrder(order.orderNo);
    alert('支付成功！');

    order.status = 1; 
  } catch (error) {
    console.error("支付失败:", error);
 
  }
};

const formatStatus = (status) => {
  const statuses = { 0: '待付款', 1: '已付款', 2: '已发货', 3: '已完成', 4: '已取消' };
  return statuses[status] || '未知';
};

const getStatusClass = (status) => {
    return `status-${status}`;
}

const goToDetail = (orderNo) => {
  router.push(`/orders/${orderNo}`);
};

onMounted(fetchOrders);
</script>

<style scoped>
.order-card {
  background: #fff;
  padding: 20px;
  margin-bottom: 15px;
  border-radius: 8px;
  transition: box-shadow .3s;
}
.order-header, .order-body {
  cursor: pointer;
}
.order-card:hover {
    box-shadow: 0 2px 8px rgba(0,0,0,0.1);
}
.order-header {
  display: flex;
  justify-content: space-between;
  padding-bottom: 10px;
  color: #666;
}
.order-body {
  border-top: 1px solid #eee;
  padding-top: 10px;
}
.amount {
  color: #e44d26;
  font-weight: bold;
}
.status {
    font-weight: bold;
}
.status-0 { color: #ff9800; } /* 待付款 */
.status-1 { color: #4caf50; } /* 已付款 */

.order-footer {
  border-top: 1px solid #eee;
  padding-top: 15px;
  margin-top: 15px;
  text-align: right;
}
.pay-btn {
  background-color: #ff9800;
  padding: 8px 20px;
}
</style>