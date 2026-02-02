import api from './index'

/**
 * 결제 위젯 초기화 정보 조회
 * @param {string} orderId - 주문 ID
 */
export const getPaymentWidgetInfo = async (orderId) => {
  const response = await api.get(`/api/payments/${orderId}/widget`)
  return response.data
}

/**
 * 결제 승인 요청 (Toss 결제 완료 후 호출)
 * @param {Object} confirmData - { paymentKey, orderId, amount }
 */
export const confirmPayment = async (confirmData) => {
  const response = await api.post('/api/payments/confirm', confirmData)
  return response.data
}

/**
 * 결제 취소
 * @param {string} orderId - 주문 ID
 * @param {string} cancelReason - 취소 사유
 */
export const cancelPayment = async (orderId, cancelReason) => {
  const response = await api.post(`/api/payments/${orderId}/cancel`, {
    cancelReason
  })
  return response.data
}

/**
 * 결제 상태 동기화
 * @param {string} orderId - 주문 ID
 */
export const syncPaymentStatus = async (orderId) => {
  const response = await api.post(`/api/payments/${orderId}/sync`)
  return response.data
}

/**
 * 결제 조회
 * @param {string} orderId - 주문 ID
 */
export const getPayment = async (orderId) => {
  const response = await api.get(`/api/payments/${orderId}`)
  return response.data
}

/**
 * 결제 목록 조회
 * @param {Object} params - { page, size, sort }
 */
export const getPayments = async (params = {}) => {
  const response = await api.get('/api/payments', { params })
  return response.data
}

export default {
  getPaymentWidgetInfo,
  confirmPayment,
  cancelPayment,
  syncPaymentStatus,
  getPayment,
  getPayments
}
