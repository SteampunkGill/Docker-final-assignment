<template>
  <div id="app">
    <app-header />
    <main class="container">
      <router-view />
    </main>
    <app-footer />
  </div>
</template>
<script setup>
import AppHeader from './components/AppHeader.vue';
import AppFooter from './components/AppFooter.vue';
</script>
<style scoped>
#app {
  display: flex;
  flex-direction: column;
  min-height: 100vh;
}
.container {
  flex: 1;
}
</style>
