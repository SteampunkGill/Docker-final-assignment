<template>
  <div class="cart-page">
    <h2>我的购物车</h2>
    <div v-if="cartItems.length > 0">
      <div class="cart-item" v-for="item in cartItems" :key="item.id">
        <div class="item-info">
          <!-- 图片已被删除 -->
          <div>
            <h4>{{ item.product?.name }}</h4>
            <p>单价: ¥{{ item.product?.price }}</p>
          </div>
        </div>
        <div class="item-actions">
          <input type="number" v-model.number="item.quantity" @change="updateQuantity(item.id, item.quantity)" min="1">
          <button @click="removeItem(item.id)" class="remove-btn">删除</button>
        </div>
        <div class="item-total">
          小计: ¥{{ (item.product?.price * item.quantity).toFixed(2) }}
        </div>
      </div>
      <div class="cart-summary">
        <h3>总计: <span>¥{{ totalPrice.toFixed(2) }}</span></h3>
        <button @click="handleCheckout" class="checkout-btn">去结算</button>
      </div>
    </div>
    <div v-else>
      <p>购物车是空的，快去逛逛吧！</p>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { getCartList, updateCartItem, deleteCartItem } from '@/api/cart';

const router = useRouter();
const cartItems = ref([]);

const fetchCartItems = async () => {
  try {
    const response = await getCartList();
    console.log('【数据接收】收到后端购物车列表响应:', response);

    // [最终决定性修正] 直接使用后端返回的完美数据结构，不再做任何处理！
    cartItems.value = response;

  } catch (error) {
    console.error("获取购物车列表失败:", error);
  }
};

const totalPrice = computed(() => {
  // 确保 item.product 存在
  return cartItems.value.reduce((sum, item) => sum + (item.product?.price || 0) * item.quantity, 0);
});

const updateQuantity = async (id, quantity) => {
  if (quantity < 1) {
    alert("数量不能小于1");
    fetchCartItems();
    return;
  }
  try {
    await updateCartItem(id, { quantity });
    const item = cartItems.value.find(i => i.id === id);
    if(item) item.quantity = quantity;
  } catch (error) {
    console.error("更新数量失败:", error);
    fetchCartItems();
  }
};

const removeItem = async (id) => {
  if (confirm("确定要删除这个商品吗？")) {
    try {
      await deleteCartItem(id);
      cartItems.value = cartItems.value.filter(item => item.id !== id);
    } catch (error) {
      console.error("删除失败:", error);
    }
  }
};

const handleCheckout = () => {
  if (cartItems.value.length === 0) {
    alert("购物车中没有商品");
    return;
  }
  router.push('/orders/confirm');
};

onMounted(fetchCartItems);
</script>

<style scoped>
.cart-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: #fff;
  padding: 15px;
  margin-bottom: 10px;
  border-radius: 4px;
}
.item-info {
  display: flex;
  align-items: center;
  gap: 15px;
  flex: 2;
}
/* 图片样式可以安全删除 */
/*
.item-info img {
  width: 100px;
  height: 100px;
  object-fit: cover;
}
*/
.item-actions {
  flex: 1;
  display: flex;
  align-items: center;
  gap: 10px;
}
.item-actions input {
  width: 60px;
}
.item-total {
  flex: 1;
  text-align: right;
  font-weight: bold;
}
.remove-btn {
  background-color: #dc3545;
}
.cart-summary {
  margin-top: 20px;
  padding: 20px;
  background: #fff;
  text-align: right;
}
.cart-summary h3 span {
  color: #e44d26;
  font-size: 24px;
}
.checkout-btn {
  margin-top: 10px;
  padding: 15px 30px;
}
</style>