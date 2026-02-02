<template>
  <div class="payment-success-page">
    <!-- 승인 처리 중 -->
    <div v-if="isProcessing" class="processing">
      <div class="spinner-large"></div>
      <h2>결제 승인 중...</h2>
      <p>잠시만 기다려주세요.</p>
    </div>

    <!-- 결제 결과 -->
    <PaymentResult 
      v-else
      :payment="payment"
      :status="resultStatus"
      :error-message="errorMessage"
    />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { usePaymentStore } from '@/stores/payment'
import PaymentResult from '@/components/payment/PaymentResult.vue'

const route = useRoute()
const router = useRouter()
const paymentStore = usePaymentStore()

const isProcessing = ref(true)
const payment = ref(null)
const resultStatus = ref('pending')
const errorMessage = ref('')

/**
 * Toss 결제 완료 후 리다이렉트되면 URL에 다음 파라미터가 포함됨:
 * - paymentKey: Toss 결제 키
 * - orderId: 주문 ID (우리가 전달한 tossOrderId)
 * - amount: 결제 금액
 */
const processPayment = async () => {
  const { paymentKey, orderId, amount } = route.query

  // 필수 파라미터 확인
  if (!paymentKey || !orderId || !amount) {
    console.error('필수 파라미터 누락:', route.query)
    resultStatus.value = 'failed'
    errorMessage.value = '결제 정보가 올바르지 않습니다.'
    isProcessing.value = false
    return
  }

  try {
    console.log('결제 승인 요청:', { paymentKey, orderId, amount })

    // 백엔드에 결제 승인 요청
    const result = await paymentStore.confirmPayment(
      paymentKey,
      orderId,
      parseInt(amount, 10)
    )

    payment.value = result
    resultStatus.value = 'success'

    console.log('결제 승인 성공:', result)

  } catch (error) {
    console.error('결제 승인 실패:', error)
    
    resultStatus.value = 'failed'
    errorMessage.value = error.response?.data?.message 
      || error.message 
      || '결제 승인에 실패했습니다.'

    // 실패해도 결제 정보는 표시 (취소/재시도 위해)
    if (error.response?.data) {
      payment.value = error.response.data
    }
  } finally {
    isProcessing.value = false
  }
}

onMounted(() => {
  processPayment()
})
</script>

<style scoped>
.payment-success-page {
  padding-top: 40px;
}

.processing {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 80px 20px;
  text-align: center;
}

.spinner-large {
  width: 60px;
  height: 60px;
  border: 4px solid #f0f0f0;
  border-top-color: #3182f6;
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin-bottom: 24px;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

.processing h2 {
  font-size: 24px;
  color: #333;
  margin-bottom: 8px;
}

.processing p {
  color: #666;
}
</style>
