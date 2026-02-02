<template>
  <div class="payment-widget-container">
    <!-- 로딩 상태 -->
    <div v-if="loading" class="loading-overlay">
      <div class="spinner"></div>
      <p>결제 정보를 불러오는 중...</p>
    </div>

    <!-- 에러 상태 -->
    <div v-else-if="error" class="error-container">
      <div class="error-icon">⚠️</div>
      <p>{{ error }}</p>
      <button @click="retry" class="btn btn-primary">다시 시도</button>
    </div>

    <!-- 결제 위젯 -->
    <div v-else class="payment-content">
      <!-- 주문 정보 -->
      <div class="order-summary">
        <h3>주문 정보</h3>
        <div class="summary-row">
          <span>주문번호</span>
          <span>{{ widgetInfo?.orderId }}</span>
        </div>
        <div class="summary-row total">
          <span>결제 금액</span>
          <span class="amount">{{ formatCurrency(widgetInfo?.amount) }}</span>
        </div>
      </div>

      <!-- Toss 결제 위젯이 렌더링될 영역 -->
      <div id="payment-method" class="widget-area"></div>
      <div id="agreement" class="widget-area"></div>

      <!-- 결제 버튼 -->
      <button 
        @click="requestPayment" 
        :disabled="isProcessing"
        class="btn btn-payment"
      >
        <span v-if="isProcessing">
          <span class="btn-spinner"></span>
          결제 진행 중...
        </span>
        <span v-else>
          {{ formatCurrency(widgetInfo?.amount) }} 결제하기
        </span>
      </button>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { usePaymentStore } from '@/stores/payment'
import { formatCurrency } from '@/utils/format'

const props = defineProps({
  orderId: {
    type: String,
    required: true
  }
})

const router = useRouter()
const paymentStore = usePaymentStore()

// 상태
const loading = ref(true)
const error = ref(null)
const isProcessing = ref(false)
const widgetInfo = ref(null)

// Toss 결제 위젯 인스턴스
let paymentWidget = null
let paymentMethodWidget = null
let agreementWidget = null

// 결제 위젯 초기화
const initPaymentWidget = async () => {
  loading.value = true
  error.value = null

  try {
    // 1. Toss SDK 로드 확인
    if (typeof PaymentWidget === 'undefined') {
      console.error('Toss PaymentWidget SDK가 로드되지 않았습니다')
      error.value = 'Toss 결제 SDK 로드 실패. 페이지를 새로고침해주세요.'
      loading.value = false
      return
    }

    // 2. 백엔드에서 결제 위젯 정보 조회
    const data = await paymentStore.fetchWidgetInfo(props.orderId)
    widgetInfo.value = data

    // 3. Toss 결제 위젯 초기화
    const clientKey = import.meta.env.VITE_TOSS_CLIENT_KEY || data.clientKey

    // PaymentWidget 인스턴스 생성
    paymentWidget = PaymentWidget(clientKey, PaymentWidget.ANONYMOUS)

    // 4. DOM이 준비될 때까지 대기
    await new Promise(resolve => setTimeout(resolve, 100))

    // 5. 결제 수단 위젯 렌더링
    paymentMethodWidget = paymentWidget.renderPaymentMethods(
        '#payment-method',
        { value: data.amount },
        { variantKey: 'DEFAULT' }
    )

    // 6. 이용약관 위젯 렌더링
    agreementWidget = paymentWidget.renderAgreement(
        '#agreement',
        { variantKey: 'AGREEMENT' }
    )

    console.log('✅ 결제 위젯 초기화 완료')

  } catch (e) {
    console.error('결제 위젯 초기화 실패:', e)
    error.value = e.message || '결제 정보를 불러올 수 없습니다.'
  } finally {
    loading.value = false
  }
}

// 결제 요청
const requestPayment = async () => {
  if (!paymentWidget || !widgetInfo.value) {
    error.value = '결제 위젯이 초기화되지 않았습니다.'
    return
  }

  isProcessing.value = true

  try {
    // Toss 결제 요청
    // 결제 완료 시 successUrl로, 실패 시 failUrl로 리다이렉트됨
    await paymentWidget.requestPayment({
      orderId: widgetInfo.value.orderId,
      orderName: widgetInfo.value.orderName || 'OrderFlow 주문 결제',
      customerName: widgetInfo.value.customerName || '고객',
      // 결제 성공/실패 시 리다이렉트될 URL
      successUrl: `${window.location.origin}/payment/success`,
      failUrl: `${window.location.origin}/payment/fail`
    })

  } catch (e) {
    // 사용자가 결제를 취소한 경우
    if (e.code === 'USER_CANCEL') {
      console.log('사용자가 결제를 취소했습니다.')
    } else {
      console.error('결제 요청 실패:', e)
      error.value = e.message || '결제 요청에 실패했습니다.'
    }
    isProcessing.value = false
  }
}

// 재시도
const retry = () => {
  initPaymentWidget()
}

onMounted(() => {
  initPaymentWidget()
})

onUnmounted(() => {
  // 위젯 정리 (메모리 누수 방지)
  paymentWidget = null
  paymentMethodWidget = null
  agreementWidget = null
})
</script>

<style scoped>
.payment-widget-container {
  background: white;
  border-radius: 12px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
  overflow: hidden;
}

.loading-overlay {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60px 20px;
  color: #666;
}

.spinner {
  width: 40px;
  height: 40px;
  border: 3px solid #f0f0f0;
  border-top-color: #3182f6;
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin-bottom: 16px;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

.error-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 40px 20px;
  text-align: center;
}

.error-icon {
  font-size: 48px;
  margin-bottom: 16px;
}

.error-container p {
  color: #e53935;
  margin-bottom: 20px;
}

.payment-content {
  padding: 24px;
}

.order-summary {
  background: #f8f9fa;
  border-radius: 8px;
  padding: 20px;
  margin-bottom: 24px;
}

.order-summary h3 {
  font-size: 16px;
  font-weight: 600;
  margin-bottom: 16px;
  color: #333;
}

.summary-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px 0;
  font-size: 14px;
  color: #666;
}

.summary-row.total {
  border-top: 1px solid #e0e0e0;
  margin-top: 12px;
  padding-top: 16px;
  font-size: 16px;
  font-weight: 600;
  color: #333;
}

.summary-row .amount {
  color: #3182f6;
  font-size: 20px;
}

.widget-area {
  margin-bottom: 16px;
}

.btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  padding: 14px 24px;
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

.btn-payment {
  width: 100%;
  background: #3182f6;
  color: white;
  padding: 18px;
  font-size: 18px;
  margin-top: 20px;
}

.btn-payment:hover:not(:disabled) {
  background: #1a5dc8;
}

.btn-payment:disabled {
  background: #ccc;
  cursor: not-allowed;
}

.btn-spinner {
  display: inline-block;
  width: 16px;
  height: 16px;
  border: 2px solid rgba(255,255,255,0.3);
  border-top-color: white;
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin-right: 8px;
}
</style>
