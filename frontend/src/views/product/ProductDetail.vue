<template>
  <div class="product-detail-page">
    <div v-if="product" class="product-detail-container">
      <!-- 图片区域已被删除 -->
      <div class="product-info">
        <h1>{{ product.name }}</h1>
        <p class="description">{{ product.description }}</p>
        <p class="price">价格: <span>¥{{ product.price }}</span></p>
        <p class="stock">库存: {{ product.stock }}</p>
        <div class="actions">
          <label for="quantity">数量:</label>
          <input type="number" id="quantity" v-model.number="quantity" min="1" :max="product.stock">
          <button @click="handleAddToCart" :disabled="product.stock < 1">加入购物车</button>
        </div>
      </div>
    </div>
    <div v-else>
      <p>加载中...</p>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { getProductDetail } from '@/api/product';
import { addToCart } from '@/api/cart';

const route = useRoute();
const router = useRouter();
const product = ref(null);
const quantity = ref(1);

const fetchProduct = async () => {
  try {
    const response = await getProductDetail(route.params.id);
    console.log('【数据接收】收到后端商品详情响应:', response);
    product.value = response;
  } catch (error) {
    console.error("获取商品详情失败:", error);
    router.push('/not-found');
  }
};

const handleAddToCart = async () => {
  if (quantity.value < 1) {
    alert("数量不能小于1");
    return;
  }
  if (quantity.value > product.value.stock) {
    alert("超出库存数量");
    return;
  }
  try {
    const payload = {
      productId: product.value.id,
      quantity: quantity.value
    };
    await addToCart(payload);
    alert('商品已成功加入购物车！');

    router.push('/cart');
  } catch (error) {
    console.error("添加到购物车失败:", error);
    // 错误信息已由axios拦截器弹出
  }
};

onMounted(() => {
  fetchProduct();
});
</script>

<style scoped>
.product-detail-container {
  /* display: flex;  <-- 注释掉flex布局，使其变为上下排列 */
  gap: 40px;
  background: #fff;
  padding: 30px;
  border-radius: 8px;
}
.product-info {
  flex: 1;
}
h1 {
  font-size: 28px;
  margin-bottom: 15px;
}
.description {
  color: #666;
  margin-bottom: 20px;
  line-height: 1.6;
}
.price {
  font-size: 20px;
  margin-bottom: 10px;
}
.price span {
  color: #e44d26;
  font-size: 24px;
  font-weight: bold;
}
.stock {
  color: #888;
  margin-bottom: 20px;
}
.actions {
  display: flex;
  align-items: center;
  gap: 15px;
}
.actions input {
  width: 60px;
  text-align: center;
}
</style>