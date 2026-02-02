<template>
  <div class="payment-history-page">
    <div class="page-header">
      <h2>결제 내역</h2>
      <p>지금까지의 결제 기록을 확인하세요</p>
    </div>

    <PaymentHistory />
  </div>
</template>

<script setup>
import PaymentHistory from '@/components/payment/PaymentHistory.vue'
</script>

<style scoped>
.payment-history-page {
  padding-bottom: 40px;
}

.page-header {
  margin-bottom: 24px;
}

.page-header h2 {
  font-size: 24px;
  font-weight: 700;
  color: #333;
  margin-bottom: 4px;
}

.page-header p {
  color: #666;
  font-size: 14px;
}
</style>
