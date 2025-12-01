<template>
  <header class="app-header">
    <div class="header-content container">
      <router-link to="/" class="logo">Simple Mall</router-link>
      <nav>
        <router-link to="/">首页</router-link>
        <router-link to="/cart">购物车</router-link>
        <router-link to="/orders">我的订单</router-link>
        <router-link to="/admin/products">商品管理</router-link>
      </nav>
      <div class="user-actions">
        <template v-if="userStore.token">
          <span>欢迎您！</span>
          <button @click="handleLogout">退出</button>
        </template>
        <template v-else>
          <router-link to="/login">登录</router-link>
        </template>
      </div>
    </div>
  </header>
</template>
<script setup>
import { useUserStore } from '@/store/user';
import { useRouter } from 'vue-router';

const userStore = useUserStore();
const router = useRouter();

const handleLogout = () => {
  userStore.logout();
  router.push('/login');
};
</script>
<style scoped>
.app-header {
  background-color: #fff;
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
  padding: 0 20px;
}
.header-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
  height: 60px;
}
.logo {
  font-size: 24px;
  font-weight: bold;
  color: #333;
  text-decoration: none;
}
nav a {
  margin: 0 15px;
  color: #555;
  text-decoration: none;
  font-size: 16px;
}
nav a.router-link-active {
  color: #007bff;
  font-weight: bold;
}
.user-actions span, .user-actions a {
  margin-right: 15px;
}
.user-actions button {
    padding: 5px 10px;
    font-size: 14px;
}
</style>
