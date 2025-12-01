<template>
  <div class="admin-product-page">
    <h2>商品管理</h2>

    <!-- 商品表单 (用于新增和编辑) -->
    <div class="product-form">
      <h3>{{ isEditing ? '编辑商品' : '新增商品' }}</h3>
      <form @submit.prevent="handleSave">
        <input v-model="productForm.name" placeholder="商品名称" required>
        <select v-model="productForm.categoryId" required>
          <option disabled value="">请选择分类</option>
          <option v-for="cat in categories" :key="cat.id" :value="cat.id">{{ cat.name }}</option>
        </select>
        <input v-model.number="productForm.price" type="number" step="0.01" placeholder="价格" required>
        <input v-model.number="productForm.stock" type="number" placeholder="库存" required>
        <input v-model="productForm.imageUrl" placeholder="图片URL">
        <textarea v-model="productForm.description" placeholder="商品描述"></textarea>
        <button type="submit">保存</button>
        <button v-if="isEditing" @click="resetForm" type="button">取消编辑</button>
      </form>
    </div>

    <!-- 商品列表 -->
    <div class="product-list">
      <h3>现有商品</h3>
      <table>
        <thead>
          <tr>
            <th>ID</th>
            <th>名称</th>
            <th>价格</th>
            <th>库存</th>
            <th>操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="product in products" :key="product.id">
            <td>{{ product.id }}</td>
            <td>{{ product.name }}</td>
            <td>¥{{ product.price }}</td>
            <td>{{ product.stock }}</td>
            <td>
              <button @click="handleEdit(product)" class="edit-btn">编辑</button>
              <button @click="handleDelete(product)" class="delete-btn">删除</button>
            </td>
          </tr>
        </tbody>
      </table>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { getProductList, createProduct, updateProduct, deleteProduct } from '@/api/product';
import { getCategoryList } from '@/api/category';

const products = ref([]);
const categories = ref([]);
const isEditing = ref(false);
const productForm = ref({
  id: null,
  name: '',
  categoryId: '',
  price: '',
  stock: '',
  imageUrl: '',
  description: ''
});

const fetchAllProducts = async () => {
  const res = await getProductList({ page: 1, size: 100 });
  products.value = res.records;
};

const fetchAllCategories = async () => {
  categories.value = await getCategoryList();
};

const resetForm = () => {
  isEditing.value = false;
  productForm.value = {
    id: null, name: '', categoryId: '', price: '', stock: '', imageUrl: '', description: ''
  };
};

const handleEdit = (product) => {
  isEditing.value = true;
  
  productForm.value = JSON.parse(JSON.stringify(product));
};

const handleDelete = async (product) => {
  if (confirm(`确定要删除商品 "${product.name}" 吗？`)) {
    try {
      await deleteProduct(product.id);
      alert('删除成功！');
      fetchAllProducts();
    } catch (error) {
      console.error('删除失败:', error);
    }
  }
};

const handleSave = async () => {
  try {
    if (isEditing.value) {
      await updateProduct(productForm.value.id, productForm.value);
      alert('更新成功！');
    } else {
      await createProduct(productForm.value);
      alert('新增成功！');
    }
    fetchAllProducts();
    resetForm();
  } catch (error) {
    console.error('保存失败:', error);
  }
};

onMounted(() => {
  fetchAllProducts();
  fetchAllCategories();
});
</script>

<style scoped>
.product-form { background: #fff; padding: 20px; border-radius: 8px; margin-bottom: 20px; }
.product-form input, .product-form select, .product-form textarea { width: 100%; padding: 8px; margin-bottom: 10px; border: 1px solid #ccc; border-radius: 4px; }
.product-form button { margin-right: 10px; }
.product-list table { width: 100%; border-collapse: collapse; background: #fff; }
.product-list th, .product-list td { border: 1px solid #eee; padding: 10px; text-align: left; }
.edit-btn { background-color: #007bff; margin-right: 5px; }
.delete-btn { background-color: #dc3545; }
</style>