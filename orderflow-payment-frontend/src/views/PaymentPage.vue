<template>
  <div class="payment-page">
    <div class="page-header">
      <h2>결제하기</h2>
      <p>안전한 토스페이먼츠로 결제가 진행됩니다</p>
    </div>

    <!-- 테스트용: 주문 ID가 없으면 데모 모드 -->
    <div v-if="!orderId" class="demo-mode">
      <div class="demo-card">
        <h3>🧪 테스트 모드</h3>
        <p>실제 주문 없이 결제 위젯을 테스트합니다.</p>

        <div class="demo-form">
          <!-- ✅ 토큰 입력 필드 추가 -->
          <label>
            Access Token (JWT)
            <input
                v-model="accessToken"
                placeholder="eyJhbGciOiJIUzI1NiJ9..."
                @blur="saveToken"
            />
          </label>
          <small class="token-hint">OAuth2 로그인 후 받은 accessToken을 입력하세요</small>

          <label>
            테스트 주문 ID
            <input v-model="testOrderId" placeholder="ORDER-xxxxx" />
          </label>
          <button @click="startTestPayment" class="btn btn-primary" :disabled="!accessToken">
            테스트 결제 시작
          </button>
        </div>

        <div class="demo-info">
          <h4>테스트 카드 정보</h4>
          <ul>
            <li>카드번호: 4330 0000 0000 0001</li>
            <li>유효기간: 12/25</li>
            <li>CVC: 123</li>
            <li>비밀번호: 00</li>
          </ul>
        </div>
      </div>
    </div>

    <!-- 실제 결제 위젯 -->
    <PaymentWidget v-else :order-id="orderId" />
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import PaymentWidget from '@/components/payment/PaymentWidget.vue'

const route = useRoute()
const router = useRouter()

// URL 파라미터에서 orderId 추출
const orderId = computed(() => route.params.orderId)

// 테스트 모드용
const testOrderId = ref('')
const accessToken = ref('')

// 페이지 로드 시 저장된 토큰 불러오기
onMounted(() => {
  accessToken.value = localStorage.getItem('accessToken') || ''
})

// 토큰 저장
const saveToken = () => {
  if (accessToken.value) {
    localStorage.setItem('accessToken', accessToken.value)
    console.log('✅ 토큰이 저장되었습니다')
  }
}

const startTestPayment = () => {
  if (!accessToken.value) {
    alert('Access Token을 먼저 입력하세요!')
    return
  }
  saveToken()
  if (testOrderId.value) {
    router.push(`/payment/${testOrderId.value}`)
  }
}
</script>

<style scoped>
/* 기존 스타일 유지하고 아래 추가 */

.token-hint {
  display: block;
  font-size: 12px;
  color: #888;
  margin-bottom: 16px;
}

.demo-form input {
  width: 100%;
  padding: 14px;
  border: 1px solid #ddd;
  border-radius: 8px;
  font-size: 14px;
  margin-bottom: 8px;
}

.btn:disabled {
  background: #ccc;
  cursor: not-allowed;
}
</style>