<template>
  <div class="not-found-container">
    <h1>404</h1>
    <p>抱歉，您访问的页面不存在。</p>
    <router-link to="/">返回首页</router-link>
  </div>
</template>

<style scoped>
.not-found-container {
  text-align: center;
  padding-top: 100px;
}
h1 {
  font-size: 6rem;
  color: #666;
}
p {
  font-size: 1.5rem;
  margin-bottom: 20px;
}
</style>