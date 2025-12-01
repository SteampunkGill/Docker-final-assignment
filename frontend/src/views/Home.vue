<template>
  <div class="product-list-page">
    <h2>商品列表</h2>
    <div class="search-bar">
      <input type="text" v-model="searchParams.name" placeholder="搜索商品名称..." @keyup.enter="fetchProducts">

      <select v-model="searchParams.categoryId" @change="fetchProducts">
        <option value="">所有分类</option>
        <option v-for="category in categories" :key="category.id" :value="category.id">
          {{ category.name }}
        </option>
      </select>
      <button @click="fetchProducts">搜索</button>
    </div>

    <div class="product-grid">
      <!-- @click 事件让整个卡片都可以点击跳转 -->
      <div v-for="product in productList" :key="product.id" class="product-card" @click="goToDetail(product.id)">
        <!-- 图片已被删除 -->
        <h3>{{ product.name }}</h3>
        <p class="price">¥{{ product.price }}</p>
      </div>
    </div>

    <div class="pagination">
      <button @click="changePage(searchParams.page - 1)" :disabled="searchParams.page <= 1">上一页</button>
      <span>第 {{ searchParams.page }} / {{ totalPages }} 页</span>
      <button @click="changePage(searchParams.page + 1)" :disabled="searchParams.page >= totalPages">下一页</button>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue';
import { useRouter } from 'vue-router';
import { getProductList } from '@/api/product';
import { getCategoryList } from '@/api/category';

const router = useRouter();
const productList = ref([]);
const categories = ref([]);
const total = ref(0);
const searchParams = ref({
  page: 1,
  size: 8,
  name: '',
  categoryId: '' // 分类ID
});

const totalPages = computed(() => {
  // 避免 total.value 为0时出现除以0的情况
  if (total.value === 0) return 1;
  return Math.ceil(total.value / searchParams.value.size)
});

const fetchProducts = async () => {
  try {
    const response = await getProductList(searchParams.value);
    console.log('【数据接收】收到后端商品列表响应:', response);


    productList.value = response.records;
    total.value = response.total;
  } catch (error) {
    console.error("获取商品列表失败:", error);

  }
};

const fetchCategories = async () => {
  try {
    const response = await getCategoryList();
    categories.value = response;
  } catch (error) {
    console.error("获取分类列表失败:", error);

  }
};

const changePage = (newPage) => {
  if (newPage > 0 && newPage <= totalPages.value) {
    searchParams.value.page = newPage;
    fetchProducts();
  }
};

const goToDetail = (id) => {
  router.push(`/products/${id}`);
};

onMounted(() => {
  fetchProducts();
  fetchCategories(); // 页面加载时同时获取分类
});
</script>

<style scoped>
.product-list-page {
  padding: 20px;
  max-width: 1200px;
  margin: 0 auto;
}

h2 {
  text-align: center;
  margin-bottom: 30px;
  color: #333;
}

.search-bar {
  margin-bottom: 20px;
  display: flex;
  gap: 10px;
  align-items: center; /* 垂直居中对齐 */
}
.search-bar input {
  flex-grow: 1;
  padding: 10px;
  border: 1px solid #ccc;
  border-radius: 4px;
  font-size: 16px;
}
.search-bar button {
  padding: 10px 15px;
  background-color: #007bff;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 16px;
  transition: background-color 0.2s;
}
.search-bar button:hover {
  background-color: #0056b3;
}

/* 新增：下拉框样式 */
.search-bar select {
  padding: 10px;
  border: 1px solid #ccc;
  border-radius: 4px;
  font-size: 16px;
  background-color: white;
  cursor: pointer;
}

.product-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(250px, 1fr));
  gap: 20px;
}
.product-card {
  background-color: #fff;
  border: 1px solid #eee;
  border-radius: 8px;
  padding: 15px;
  text-align: center;
  cursor: pointer;
  transition: box-shadow 0.3s, transform 0.2s;
  display: flex;
  flex-direction: column;
  justify-content: space-between; /* 使内容垂直分布 */
  height: 120px; /* 给一个固定的高度，让卡片更整齐 */
}
.product-card:hover {
  box-shadow: 0 4px 12px rgba(0,0,0,0.15);
  transform: translateY(-5px);
}
/* 图片样式已被安全删除 */
/*
.product-card img {
  max-width: 100%;
  height: 200px;
  object-fit: cover;
  margin-bottom: 10px;
  border-radius: 4px;
}
*/
.product-card h3 {
  font-size: 1.2em;
  margin-bottom: 8px;
  color: #333;
  overflow: hidden;
  white-space: nowrap;
  text-overflow: ellipsis;
}
.price {
  color: #e44d26;
  font-weight: bold;
  font-size: 18px;
  margin-top: auto; /* 将价格推到底部 */
}
.pagination {
  margin-top: 30px;
  text-align: center;
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 15px;
}
.pagination button {
  padding: 8px 15px;
  background-color: #007bff;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 15px;
  transition: background-color 0.2s;
}
.pagination button:hover:not(:disabled) {
  background-color: #0056b3;
}
.pagination button:disabled {
  background-color: #cccccc;
  cursor: not-allowed;
}
.pagination span {
  display: inline-block;
  width: 150px;
  font-size: 16px;
  color: #555;
}
</style>