<template>
  <div class="profile-page">
    <h2>个人中心</h2>
    <div v-if="user" class="profile-info">
      <p><strong>用户名:</strong> {{ user.username }}</p>
      <p><strong>手机号:</strong> {{ user.phone || '未设置' }}</p>
      <p><strong>注册时间:</strong> {{ new Date(user.createTime).toLocaleString() }}</p>
    </div>
    <div v-else>
      加载中...
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { getUserProfile } from '@/api/user';

const user = ref(null);

onMounted(async () => {
  try {
    user.value = await getUserProfile();
  } catch (error) {
    console.error("获取用户信息失败:", error);
  }
});
</script>

<style scoped>
.profile-info {
  background: #fff;
  padding: 20px;
  border-radius: 4px;
}
.profile-info p {
  line-height: 2;
  font-size: 16px;
}
</style>