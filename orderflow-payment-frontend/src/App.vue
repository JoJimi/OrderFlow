<template>
  <div id="app">
    <header class="app-header">
      <h1>OrderFlow</h1>
    </header>
    <main class="app-main">
      <router-view />
    </main>
  </div>
</template>

<script setup>
</script>

<style>
.app-header {
  background: linear-gradient(135deg, #3182f6 0%, #1a5dc8 100%);
  color: white;
  padding: 16px 24px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
}

.app-header h1 {
  font-size: 24px;
  font-weight: 600;
}

.app-main {
  max-width: 640px;
  margin: 0 auto;
  padding: 24px 16px;
}
</style>
