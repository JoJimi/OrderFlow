import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import paymentApi from '@/api/payment'

export const usePaymentStore = defineStore('payment', () => {
  // State
  const currentPayment = ref(null)
  const paymentHistory = ref([])
  const loading = ref(false)
  const error = ref(null)

  // Getters
  const isPending = computed(() => 
    currentPayment.value?.paymentStatus === 'PAYMENT_PENDING'
  )
  
  const isCompleted = computed(() => 
    currentPayment.value?.paymentStatus === 'PAYMENT_COMPLETED'
  )

  // Actions
  const fetchWidgetInfo = async (orderId) => {
    loading.value = true
    error.value = null
    try {
      const data = await paymentApi.getPaymentWidgetInfo(orderId)
      currentPayment.value = data
      return data
    } catch (e) {
      error.value = e.response?.data?.message || '결제 정보를 불러올 수 없습니다.'
      throw e
    } finally {
      loading.value = false
    }
  }

  const confirmPayment = async (paymentKey, orderId, amount) => {
    loading.value = true
    error.value = null
    try {
      const data = await paymentApi.confirmPayment({
        paymentKey,
        orderId,
        amount
      })
      currentPayment.value = data
      return data
    } catch (e) {
      error.value = e.response?.data?.message || '결제 승인에 실패했습니다.'
      throw e
    } finally {
      loading.value = false
    }
  }

  const fetchPaymentHistory = async (params = {}) => {
    loading.value = true
    error.value = null
    try {
      const data = await paymentApi.getPayments(params)
      paymentHistory.value = data.content || []
      return data
    } catch (e) {
      error.value = e.response?.data?.message || '결제 내역을 불러올 수 없습니다.'
      throw e
    } finally {
      loading.value = false
    }
  }

  const cancelPayment = async (orderId, reason) => {
    loading.value = true
    error.value = null
    try {
      const data = await paymentApi.cancelPayment(orderId, reason)
      currentPayment.value = data
      return data
    } catch (e) {
      error.value = e.response?.data?.message || '결제 취소에 실패했습니다.'
      throw e
    } finally {
      loading.value = false
    }
  }

  const clearError = () => {
    error.value = null
  }

  return {
    // State
    currentPayment,
    paymentHistory,
    loading,
    error,
    // Getters
    isPending,
    isCompleted,
    // Actions
    fetchWidgetInfo,
    confirmPayment,
    fetchPaymentHistory,
    cancelPayment,
    clearError
  }
})
