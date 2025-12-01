<template>
  <div class="login-container">
    <div class="login-box">
      <h2>用户登录</h2>
      <form @submit.prevent="handleLogin">
        <div class="form-group">
          <label for="username">用户名</label>
          <input type="text" id="username" v-model="credentials.username" required>
        </div>
        <div class="form-group">
          <label for="password">密码</label>
          <input type="password" id="password" v-model="credentials.password" required>
        </div>
        <p v-if="error" class="error-message">{{ error }}</p>
        <button type="submit" class="login-btn">登录</button>
      </form>
    </div>
  </div>
</template>
<script setup>
import { ref } from 'vue';
import { useRouter } from 'vue-router';
import { useUserStore } from '@/store/user';

const router = useRouter();
const userStore = useUserStore();
const credentials = ref({
  username: '',
  password: ''
});
const error = ref('');

const handleLogin = async () => {
  error.value = '';
  try {
    await userStore.login(credentials.value);
    router.push('/');
  } catch (err) {
    error.value = '登录失败，请检查用户名或密码。';
    console.error(err);
  }
};
</script>
<style scoped>
.login-container {
  display: flex;
  justify-content: center;
  align-items: center;
  padding-top: 50px;
}
.login-box {
  width: 400px;
  padding: 40px;
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 4px 10px rgba(0,0,0,0.1);
}
h2 {
  text-align: center;
  margin-bottom: 20px;
}
.form-group {
  margin-bottom: 15px;
}
.form-group label {
  display: block;
  margin-bottom: 5px;
}
.login-btn {
  width: 100%;
  margin-top: 10px;
}
.error-message {
  color: red;
  text-align: center;
  margin-top: 10px;
}
</style>
