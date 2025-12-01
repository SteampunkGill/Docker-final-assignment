<template>
  <div class="order-confirm-page">
    <h2>确认订单信息</h2>
    <!-- 地址选择 -->
    <div class="section">
      <h3>选择收货地址</h3>
      <div v-if="addresses.length > 0" class="address-list">
        <div v-for="address in addresses" :key="address.id"
             :class="['address-card', { selected: selectedAddressId === address.id }]"
             @click="selectedAddressId = address.id">
          <p><strong>{{ address.receiverName }}</strong> ({{ address.receiverPhone }})</p>
          <p>{{ `${address.province} ${address.city} ${address.district} ${address.detail}` }}</p>
        </div>
      </div>
      <p v-else>请先去个人中心添加收货地址</p>
    </div>

    <!-- 商品信息 -->
    <div class="section">
      <h3>商品列表</h3>
      <div v-for="item in cartItems" :key="item.id" class="cart-item">
        <!-- 模板部分已经正确使用了嵌套结构，现在数据对了，就能正常显示了 -->
        <img :src="item.product?.imageUrl || 'https://via.placeholder.com/80'" :alt="item.product?.name">
        <div class="item-name">{{ item.product?.name }}</div>
        <div class="item-quantity">x {{ item.quantity }}</div>
        <div class="item-price">¥{{ (item.product?.price * item.quantity).toFixed(2) }}</div>
      </div>
    </div>

    <!-- 结算 -->
    <div class="summary">
      <p>总计: <span>¥{{ totalPrice.toFixed(2) }}</span></p>
      <button @click="handleCreateOrder" :disabled="!selectedAddressId">提交订单</button>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue';
import { useRouter } from 'vue-router';
import { getAddressList } from '@/api/user';
import { getCartList } from '@/api/cart';
import { createOrder } from '@/api/order';

const router = useRouter();
const addresses = ref([]);
const selectedAddressId = ref(null);
const cartItems = ref([]);

const totalPrice = computed(() => {
  return cartItems.value.reduce((sum, item) => sum + (item.product?.price || 0) * item.quantity, 0);
});

const handleCreateOrder = async () => {
  if (!selectedAddressId.value) {
    alert("请选择一个收货地址！");
    return;
  }
  const cartIds = cartItems.value.map(item => item.id);
  if (cartIds.length === 0) {
    alert("购物车为空！");
    return;
  }
  
  try {
    const orderData = {
      cartIds: cartIds,
      addressId: selectedAddressId.value
    };
    const response = await createOrder(orderData);
    alert('订单创建成功！');
    router.push(`/orders/${response.orderNo}`);
  } catch (error) {
    console.error("创建订单失败:", error);
  }
};

onMounted(async () => {
  try {
    const addressRes = await getAddressList();
    addresses.value = addressRes;
    if (addresses.value.length > 0) {
      // 默认选中标记为 is_default 的地址，否则选中第一个
      const defaultAddress = addresses.value.find(addr => addr.isDefault);
      selectedAddressId.value = defaultAddress ? defaultAddress.id : addresses.value[0].id;
    }

    const cartRes = await getCartList();
    
    cartItems.value = cartRes;

  } catch (error) {
    console.error("加载确认页信息失败:", error);
  }
});
</script>

<style scoped>
.section { margin-bottom: 20px; }
.address-card {
  background: #fff; padding: 15px; margin-bottom: 10px;
  border: 1px solid #eee; border-radius: 4px; cursor: pointer;
}
.address-card.selected { border-color: #007bff; box-shadow: 0 0 5px rgba(0,123,255,0.5); }
.cart-item { display: flex; align-items: center; justify-content: space-between; padding: 10px 0; border-bottom: 1px solid #eee; }
.cart-item img { width: 80px; height: 80px; }
.item-name { flex-grow: 1; margin-left: 15px; }
.summary { text-align: right; }
.summary span { color: #e44d26; font-size: 24px; font-weight: bold; }
</style>