<template>
  <div class="payment-widget-container">
    <!-- 로딩 상태 -->
    <div v-if="loading" class="loading-overlay">
      <div class="spinner"></div>
      <p>결제 정보를 불러오는 중...</p>
    </div>

    <!-- 에러 상태 -->
    <div v-if="error" class="error-message">
      <p>{{ error }}</p>
      <button @click="initPaymentWidget" class="retry-btn">다시 시도</button>
    </div>

    <!-- 결제 위젯 영역 -->
    <div v-show="!loading && !error" class="payment-content">
      <!-- 결제 정보 요약 -->
      <div class="payment-summary" v-if="widgetInfo">
        <h3>결제 정보</h3>
        <div class="summary-row">
          <span>주문번호</span>
          <span>{{ widgetInfo.orderId }}</span>
        </div>
        <div class="summary-row">
          <span>주문명</span>
          <span>{{ widgetInfo.orderName }}</span>
        </div>
        <div class="summary-row total">
          <span>결제 금액</span>
          <span>{{ formatPrice(widgetInfo.amount) }}원</span>
        </div>
      </div>

      <!-- Toss 결제 수단 위젯이 렌더링될 영역 -->
      <div id="payment-method" class="payment-method-area"></div>

      <!-- Toss 이용약관 위젯이 렌더링될 영역 -->
      <div id="agreement" class="agreement-area"></div>

      <!-- 위젯 로딩 상태 표시 -->
      <div v-if="!isWidgetReady && !loading" class="widget-loading">
        <div class="spinner small"></div>
        <p>결제 위젯 로딩 중...</p>
      </div>

      <!-- 결제 버튼 -->
      <button
          @click="requestPayment"
          :disabled="isProcessing || !isWidgetReady"
          class="payment-btn"
      >
        <span v-if="isProcessing">결제 처리 중...</span>
        <span v-else-if="!isWidgetReady">결제 위젯 준비 중...</span>
        <span v-else>{{ formatPrice(widgetInfo?.amount || 0) }}원 결제하기</span>
      </button>
    </div>
  </div>
</template>

<script setup>
import {ref, onMounted, onUnmounted} from 'vue'
import {usePaymentStore} from '@/stores/payment'

const props = defineProps({
  orderId: {
    type: String,
    required: true
  }
})

const emit = defineEmits(['payment-success', 'payment-fail'])

const paymentStore = usePaymentStore()

// 상태 관리
const loading = ref(true)
const error = ref(null)
const isProcessing = ref(false)
const widgetInfo = ref(null)
const isWidgetReady = ref(false)

// Toss 결제 위젯 인스턴스
let paymentWidget = null
let paymentMethodWidget = null
let agreementWidget = null

// Toss SDK 로드 대기 함수
const waitForTossSDK = () => {
  return new Promise((resolve, reject) => {
    let attempts = 0
    const maxAttempts = 50 // 5초 (100ms * 50)

    const checkSDK = () => {
      if (typeof PaymentWidget !== 'undefined') {
        console.log('✅ Toss SDK 로드 완료')
        resolve()
      } else if (attempts >= maxAttempts) {
        reject(new Error('Toss 결제 SDK 로드 시간 초과. 페이지를 새로고침해주세요.'))
      } else {
        attempts++
        setTimeout(checkSDK, 100)
      }
    }
    checkSDK()
  })
}

// 결제 위젯 초기화
const initPaymentWidget = async () => {
  loading.value = true
  error.value = null
  isWidgetReady.value = false

  try {
    // 1. Toss SDK 로드 대기
    await waitForTossSDK()

    // 2. 백엔드에서 결제 위젯 정보 조회
    const data = await paymentStore.fetchWidgetInfo(props.orderId)
    widgetInfo.value = data

    // 3. Toss 결제 위젯 초기화
    const clientKey = import.meta.env.VITE_TOSS_CLIENT_KEY || data.clientKey

    // PaymentWidget 인스턴스 생성
    paymentWidget = PaymentWidget(clientKey, PaymentWidget.ANONYMOUS)

    // 4. DOM이 준비될 때까지 대기
    await new Promise(resolve => setTimeout(resolve, 300))

    // 5. 결제 수단 위젯 렌더링
    paymentMethodWidget = paymentWidget.renderPaymentMethods(
        '#payment-method',
        {value: data.amount},
        {variantKey: 'DEFAULT'}
    )

    // 6. 이용약관 위젯 렌더링
    agreementWidget = paymentWidget.renderAgreement(
        '#agreement',
        {variantKey: 'AGREEMENT'}
    )

    // ⭐ 핵심: ready 이벤트로 위젯 렌더링 완료 감지
    // 결제 수단 위젯의 ready 이벤트 리스너 등록
    paymentMethodWidget.on('ready', () => {
      console.log('✅ 결제 수단 위젯 렌더링 완료 (ready 이벤트)')
      isWidgetReady.value = true
    })

    console.log('✅ 결제 위젯 초기화 완료, ready 이벤트 대기 중...')

  } catch (e) {
    console.error('결제 위젯 초기화 실패:', e)
    error.value = e.message || '결제 정보를 불러올 수 없습니다.'
  } finally {
    loading.value = false
  }
}

// 결제 요청
const requestPayment = async () => {
  // 위젯이 준비되지 않았으면 요청 거부
  if (!isWidgetReady.value) {
    console.warn('⚠️ 결제 위젯이 아직 준비되지 않았습니다.')
    alert('결제 위젯이 아직 로딩 중입니다. 잠시 후 다시 시도해주세요.')
    return
  }

  if (!paymentWidget || !widgetInfo.value) {
    error.value = '결제 위젯이 초기화되지 않았습니다.'
    return
  }

  isProcessing.value = true

  try {
    const successUrl = `${window.location.origin}/payment/success`
    const failUrl = `${window.location.origin}/payment/fail`

    console.log('🔄 결제 요청 시작...')

    // Toss 결제 요청
    await paymentWidget.requestPayment({
      orderId: widgetInfo.value.orderId,
      orderName: widgetInfo.value.orderName,
      customerEmail: widgetInfo.value.customerEmail || undefined,
      customerName: widgetInfo.value.customerName || undefined,
      successUrl: successUrl,
      failUrl: failUrl
    })

  } catch (e) {
    console.error('결제 요청 실패:', e)

    if (e.code === 'USER_CANCEL') {
      // 사용자가 결제를 취소한 경우
      console.log('사용자가 결제를 취소했습니다.')
    } else {
      error.value = e.message || '결제 요청 중 오류가 발생했습니다.'
      emit('payment-fail', {error: e})
    }
  } finally {
    isProcessing.value = false
  }
}

// 금액 포맷팅
const formatPrice = (price) => {
  return new Intl.NumberFormat('ko-KR').format(price)
}

// 컴포넌트 마운트 시 위젯 초기화
onMounted(() => {
  initPaymentWidget()
})

// 컴포넌트 언마운트 시 정리
onUnmounted(() => {
  paymentWidget = null
  paymentMethodWidget = null
  agreementWidget = null
})
</script>

<style scoped>
.payment-widget-container {
  max-width: 600px;
  margin: 0 auto;
  padding: 20px;
}

.loading-overlay {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60px 20px;
}

.spinner {
  width: 40px;
  height: 40px;
  border: 3px solid #f3f3f3;
  border-top: 3px solid #3182f6;
  border-radius: 50%;
  animation: spin 1s linear infinite;
}

.spinner.small {
  width: 24px;
  height: 24px;
  border-width: 2px;
}

@keyframes spin {
  0% {
    transform: rotate(0deg);
  }
  100% {
    transform: rotate(360deg);
  }
}

.error-message {
  text-align: center;
  padding: 40px 20px;
  color: #dc3545;
}

.retry-btn {
  margin-top: 16px;
  padding: 10px 24px;
  background: #3182f6;
  color: white;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  font-size: 14px;
}

.retry-btn:hover {
  background: #1b64da;
}

.payment-summary {
  background: #f8f9fa;
  border-radius: 12px;
  padding: 20px;
  margin-bottom: 24px;
}

.payment-summary h3 {
  margin: 0 0 16px 0;
  font-size: 16px;
  color: #333;
}

.summary-row {
  display: flex;
  justify-content: space-between;
  padding: 8px 0;
  font-size: 14px;
  color: #666;
}

.summary-row.total {
  border-top: 1px solid #e0e0e0;
  margin-top: 8px;
  padding-top: 16px;
  font-weight: bold;
  color: #333;
  font-size: 16px;
}

.payment-method-area {
  min-height: 200px;
  margin-bottom: 16px;
}

.agreement-area {
  margin-bottom: 24px;
}

.widget-loading {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 20px;
  color: #666;
  font-size: 14px;
}

.widget-loading p {
  margin-top: 12px;
}

.payment-btn {
  width: 100%;
  padding: 16px;
  background: #3182f6;
  color: white;
  border: none;
  border-radius: 12px;
  font-size: 16px;
  font-weight: bold;
  cursor: pointer;
  transition: background 0.2s;
}

.payment-btn:hover:not(:disabled) {
  background: #1b64da;
}

.payment-btn:disabled {
  background: #ccc;
  cursor: not-allowed;
}
</style>