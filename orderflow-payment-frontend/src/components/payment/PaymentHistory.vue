<template>
  <div class="payment-history">
    <div v-if="loading" class="loading">
      <div class="spinner"></div>
      <p>결제 내역을 불러오는 중...</p>
    </div>

    <div v-else-if="payments.length === 0" class="empty">
      <p>결제 내역이 없습니다.</p>
    </div>

    <div v-else class="history-list">
      <div 
        v-for="payment in payments" 
        :key="payment.paymentId"
        class="history-item"
        @click="viewDetail(payment)"
      >
        <div class="item-main">
          <div class="item-info">
            <span class="order-id">{{ payment.orderId }}</span>
            <span :class="['status-badge', getStatusClass(payment.paymentStatus)]">
              {{ formatPaymentStatus(payment.paymentStatus) }}
            </span>
          </div>
          <div class="item-amount">
            {{ formatCurrency(payment.amount) }}
          </div>
        </div>
        <div class="item-sub">
          <span>{{ formatDate(payment.createdAt) }}</span>
          <span v-if="payment.paymentMethod">
            {{ formatPaymentMethod(payment.paymentMethod) }}
          </span>
        </div>
      </div>
    </div>

    <!-- 페이지네이션 -->
    <div v-if="totalPages > 1" class="pagination">
      <button 
        @click="prevPage" 
        :disabled="currentPage === 0"
        class="page-btn"
      >
        이전
      </button>
      <span class="page-info">{{ currentPage + 1 }} / {{ totalPages }}</span>
      <button 
        @click="nextPage" 
        :disabled="currentPage >= totalPages - 1"
        class="page-btn"
      >
        다음
      </button>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { usePaymentStore } from '@/stores/payment'
import { 
  formatCurrency, 
  formatDate, 
  formatPaymentStatus,
  formatPaymentMethod 
} from '@/utils/format'

const paymentStore = usePaymentStore()

const payments = ref([])
const loading = ref(true)
const currentPage = ref(0)
const totalPages = ref(0)
const pageSize = 10

const fetchPayments = async () => {
  loading.value = true
  try {
    const data = await paymentStore.fetchPaymentHistory({
      page: currentPage.value,
      size: pageSize
    })
    payments.value = data.content || []
    totalPages.value = data.totalPages || 0
  } catch (e) {
    console.error('결제 내역 조회 실패:', e)
  } finally {
    loading.value = false
  }
}

const getStatusClass = (status) => {
  const classMap = {
    'PAYMENT_COMPLETED': 'status-completed',
    'PAYMENT_PENDING': 'status-pending',
    'PAYMENT_FAILED': 'status-failed',
    'PAYMENT_CANCELLED': 'status-cancelled'
  }
  return classMap[status] || ''
}

const viewDetail = (payment) => {
  // 결제 상세 보기 (모달 또는 페이지 이동)
  console.log('결제 상세:', payment)
}

const prevPage = () => {
  if (currentPage.value > 0) {
    currentPage.value--
    fetchPayments()
  }
}

const nextPage = () => {
  if (currentPage.value < totalPages.value - 1) {
    currentPage.value++
    fetchPayments()
  }
}

onMounted(() => {
  fetchPayments()
})
</script>

<style scoped>
.payment-history {
  background: white;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
}

.loading, .empty {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60px 20px;
  color: #666;
}

.spinner {
  width: 32px;
  height: 32px;
  border: 3px solid #f0f0f0;
  border-top-color: #3182f6;
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin-bottom: 12px;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

.history-item {
  padding: 16px 20px;
  border-bottom: 1px solid #f0f0f0;
  cursor: pointer;
  transition: background 0.2s;
}

.history-item:hover {
  background: #f8f9fa;
}

.history-item:last-child {
  border-bottom: none;
}

.item-main {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.item-info {
  display: flex;
  align-items: center;
  gap: 10px;
}

.order-id {
  font-weight: 600;
  color: #333;
}

.status-badge {
  font-size: 12px;
  padding: 4px 8px;
  border-radius: 4px;
  font-weight: 500;
}

.status-completed {
  background: #e8f5e9;
  color: #2e7d32;
}

.status-pending {
  background: #fff3e0;
  color: #f57c00;
}

.status-failed {
  background: #ffebee;
  color: #c62828;
}

.status-cancelled {
  background: #f5f5f5;
  color: #757575;
}

.item-amount {
  font-size: 18px;
  font-weight: 700;
  color: #3182f6;
}

.item-sub {
  display: flex;
  gap: 16px;
  font-size: 13px;
  color: #888;
}

.pagination {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 16px;
  padding: 16px;
  border-top: 1px solid #f0f0f0;
}

.page-btn {
  padding: 8px 16px;
  border: 1px solid #ddd;
  background: white;
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.2s;
}

.page-btn:hover:not(:disabled) {
  background: #f5f5f5;
}

.page-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.page-info {
  font-size: 14px;
  color: #666;
}
</style>
