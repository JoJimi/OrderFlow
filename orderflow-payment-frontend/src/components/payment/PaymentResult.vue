<template>
  <div class="payment-result" :class="statusClass">
    <div class="result-icon">
      <span v-if="isSuccess">✓</span>
      <span v-else-if="isFailed">✕</span>
      <span v-else>⏳</span>
    </div>
    
    <h2 class="result-title">{{ title }}</h2>
    <p class="result-message">{{ message }}</p>

    <!-- 결제 상세 정보 -->
    <div v-if="payment" class="payment-details">
      <div class="detail-row">
        <span class="label">주문번호</span>
        <span class="value">{{ payment.orderId }}</span>
      </div>
      <div class="detail-row">
        <span class="label">결제금액</span>
        <span class="value amount">{{ formatCurrency(payment.amount) }}</span>
      </div>
      <div v-if="payment.paymentMethod" class="detail-row">
        <span class="label">결제수단</span>
        <span class="value">{{ formatPaymentMethod(payment.paymentMethod) }}</span>
      </div>
      <div v-if="payment.cardNumber" class="detail-row">
        <span class="label">카드정보</span>
        <span class="value">{{ payment.cardCompany }} {{ maskCardNumber(payment.cardNumber) }}</span>
      </div>
      <div v-if="payment.approvedAt" class="detail-row">
        <span class="label">승인시간</span>
        <span class="value">{{ formatDate(payment.approvedAt) }}</span>
      </div>
      <div v-if="payment.receiptUrl" class="detail-row">
        <span class="label">영수증</span>
        <a :href="payment.receiptUrl" target="_blank" class="receipt-link">
          영수증 보기 →
        </a>
      </div>
    </div>

    <!-- 에러 정보 (실패 시) -->
    <div v-if="isFailed && payment?.failureReason" class="error-info">
      <p>{{ payment.failureReason }}</p>
    </div>

    <!-- 액션 버튼 -->
    <div class="action-buttons">
      <button v-if="isSuccess" @click="goToOrders" class="btn btn-primary">
        주문 내역 보기
      </button>
      <button v-if="isFailed" @click="retryPayment" class="btn btn-primary">
        다시 결제하기
      </button>
      <button @click="goHome" class="btn btn-secondary">
        홈으로
      </button>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import { 
  formatCurrency, 
  formatDate, 
  formatPaymentMethod,
  maskCardNumber 
} from '@/utils/format'

const props = defineProps({
  payment: {
    type: Object,
    default: null
  },
  status: {
    type: String,
    default: 'pending' // 'success' | 'failed' | 'pending'
  },
  errorMessage: {
    type: String,
    default: ''
  }
})

const router = useRouter()

const isSuccess = computed(() => props.status === 'success')
const isFailed = computed(() => props.status === 'failed')

const statusClass = computed(() => ({
  'status-success': isSuccess.value,
  'status-failed': isFailed.value,
  'status-pending': props.status === 'pending'
}))

const title = computed(() => {
  if (isSuccess.value) return '결제가 완료되었습니다'
  if (isFailed.value) return '결제에 실패했습니다'
  return '결제 처리 중...'
})

const message = computed(() => {
  if (isSuccess.value) return '주문이 정상적으로 접수되었습니다.'
  if (isFailed.value) return props.errorMessage || '결제 처리 중 문제가 발생했습니다.'
  return '잠시만 기다려주세요.'
})

const goToOrders = () => {
  router.push('/orders')
}

const goHome = () => {
  router.push('/')
}

const retryPayment = () => {
  if (props.payment?.orderId) {
    router.push(`/payment/${props.payment.orderId}`)
  } else {
    router.push('/')
  }
}
</script>

<style scoped>
.payment-result {
  background: white;
  border-radius: 12px;
  padding: 40px 24px;
  text-align: center;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
}

.result-icon {
  width: 80px;
  height: 80px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto 24px;
  font-size: 40px;
}

.status-success .result-icon {
  background: #e8f5e9;
  color: #4caf50;
}

.status-failed .result-icon {
  background: #ffebee;
  color: #e53935;
}

.status-pending .result-icon {
  background: #fff3e0;
  color: #ff9800;
  animation: pulse 1.5s infinite;
}

@keyframes pulse {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.5; }
}

.result-title {
  font-size: 24px;
  font-weight: 700;
  margin-bottom: 8px;
  color: #333;
}

.result-message {
  font-size: 16px;
  color: #666;
  margin-bottom: 32px;
}

.payment-details {
  background: #f8f9fa;
  border-radius: 8px;
  padding: 20px;
  margin-bottom: 24px;
  text-align: left;
}

.detail-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10px 0;
  border-bottom: 1px solid #e9ecef;
}

.detail-row:last-child {
  border-bottom: none;
}

.detail-row .label {
  color: #666;
  font-size: 14px;
}

.detail-row .value {
  color: #333;
  font-weight: 500;
}

.detail-row .value.amount {
  color: #3182f6;
  font-size: 18px;
  font-weight: 700;
}

.receipt-link {
  color: #3182f6;
  text-decoration: none;
}

.receipt-link:hover {
  text-decoration: underline;
}

.error-info {
  background: #ffebee;
  color: #c62828;
  padding: 16px;
  border-radius: 8px;
  margin-bottom: 24px;
  font-size: 14px;
}

.action-buttons {
  display: flex;
  gap: 12px;
  justify-content: center;
}

.btn {
  padding: 14px 28px;
  border-radius: 8px;
  font-size: 16px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s;
  border: none;
}

.btn-primary {
  background: #3182f6;
  color: white;
}

.btn-primary:hover {
  background: #1a5dc8;
}

.btn-secondary {
  background: #f0f0f0;
  color: #333;
}

.btn-secondary:hover {
  background: #e0e0e0;
}
</style>
