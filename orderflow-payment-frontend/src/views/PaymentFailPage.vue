<template>
  <div class="payment-fail-page">
    <PaymentResult 
      :payment="null"
      status="failed"
      :error-message="errorMessage"
    />
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import PaymentResult from '@/components/payment/PaymentResult.vue'

const route = useRoute()

/**
 * Toss 결제 실패 시 URL에 다음 파라미터가 포함됨:
 * - code: 에러 코드
 * - message: 에러 메시지
 * - orderId: 주문 ID
 */
const errorMessage = computed(() => {
  const { code, message } = route.query
  
  if (code === 'USER_CANCEL') {
    return '결제가 취소되었습니다.'
  }
  
  return message || '결제 처리 중 오류가 발생했습니다.'
})
</script>

<style scoped>
.payment-fail-page {
  padding-top: 40px;
}
</style>
