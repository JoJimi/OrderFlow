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
          <label>
            테스트 주문 ID
            <input v-model="testOrderId" placeholder="ORDER-xxxxx" />
          </label>
          <button @click="startTestPayment" class="btn btn-primary">
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
import { ref, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import PaymentWidget from '@/components/payment/PaymentWidget.vue'

const route = useRoute()
const router = useRouter()

// URL 파라미터에서 orderId 추출
const orderId = computed(() => route.params.orderId)

// 테스트 모드용
const testOrderId = ref('')

const startTestPayment = () => {
  if (testOrderId.value) {
    router.push(`/payment/${testOrderId.value}`)
  }
}
</script>

<style scoped>
.payment-page {
  padding-bottom: 40px;
}

.page-header {
  text-align: center;
  margin-bottom: 32px;
}

.page-header h2 {
  font-size: 28px;
  font-weight: 700;
  color: #333;
  margin-bottom: 8px;
}

.page-header p {
  color: #666;
  font-size: 15px;
}

.demo-mode {
  max-width: 480px;
  margin: 0 auto;
}

.demo-card {
  background: white;
  border-radius: 12px;
  padding: 32px 24px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
}

.demo-card h3 {
  font-size: 20px;
  margin-bottom: 8px;
  color: #333;
}

.demo-card > p {
  color: #666;
  margin-bottom: 24px;
}

.demo-form {
  margin-bottom: 32px;
}

.demo-form label {
  display: block;
  font-size: 14px;
  color: #333;
  margin-bottom: 8px;
}

.demo-form input {
  width: 100%;
  padding: 14px;
  border: 1px solid #ddd;
  border-radius: 8px;
  font-size: 16px;
  margin-bottom: 16px;
}

.demo-form input:focus {
  outline: none;
  border-color: #3182f6;
}

.btn {
  width: 100%;
  padding: 16px;
  border: none;
  border-radius: 8px;
  font-size: 16px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s;
}

.btn-primary {
  background: #3182f6;
  color: white;
}

.btn-primary:hover {
  background: #1a5dc8;
}

.demo-info {
  background: #f8f9fa;
  border-radius: 8px;
  padding: 20px;
}

.demo-info h4 {
  font-size: 14px;
  color: #333;
  margin-bottom: 12px;
}

.demo-info ul {
  list-style: none;
  padding: 0;
  margin: 0;
}

.demo-info li {
  font-size: 13px;
  color: #666;
  padding: 6px 0;
  font-family: 'Monaco', 'Menlo', monospace;
}
</style>
