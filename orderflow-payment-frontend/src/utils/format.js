/**
 * 금액을 원화 형식으로 포맷팅
 * @param {number} amount - 금액
 * @returns {string} - "₩1,234,567" 형식
 */
export const formatCurrency = (amount) => {
  if (amount == null) return '₩0'
  return new Intl.NumberFormat('ko-KR', {
    style: 'currency',
    currency: 'KRW'
  }).format(amount)
}

/**
 * 날짜 포맷팅
 * @param {string} dateString - ISO 날짜 문자열
 * @returns {string} - "2024년 1월 15일 14:30" 형식
 */
export const formatDate = (dateString) => {
  if (!dateString) return '-'
  const date = new Date(dateString)
  return new Intl.DateTimeFormat('ko-KR', {
    year: 'numeric',
    month: 'long',
    day: 'numeric',
    hour: '2-digit',
    minute: '2-digit'
  }).format(date)
}

/**
 * 결제 상태를 한글로 변환
 * @param {string} status - 결제 상태 코드
 * @returns {string} - 한글 상태명
 */
export const formatPaymentStatus = (status) => {
  const statusMap = {
    'PAYMENT_PENDING': '결제 대기',
    'PAYMENT_COMPLETED': '결제 완료',
    'PAYMENT_FAILED': '결제 실패',
    'PAYMENT_CANCELLED': '결제 취소',
    'REFUND_REQUESTED': '환불 요청',
    'REFUND_COMPLETED': '환불 완료'
  }
  return statusMap[status] || status
}

/**
 * 결제 수단을 한글로 변환
 * @param {string} method - 결제 수단 코드
 * @returns {string} - 한글 결제 수단명
 */
export const formatPaymentMethod = (method) => {
  const methodMap = {
    'CREDIT_CARD': '신용카드',
    'BANK_TRANSFER': '계좌이체',
    'MOBILE_PAYMENT': '간편결제',
    'VIRTUAL_ACCOUNT': '가상계좌'
  }
  return methodMap[method] || method || '-'
}

/**
 * 카드 번호 마스킹
 * @param {string} cardNumber - 카드 번호
 * @returns {string} - "1234-****-****-5678" 형식
 */
export const maskCardNumber = (cardNumber) => {
  if (!cardNumber) return '-'
  // 이미 마스킹된 경우 그대로 반환
  if (cardNumber.includes('*')) return cardNumber
  // 마스킹 처리
  return cardNumber.replace(/(\d{4})(\d{4})(\d{4})(\d{4})/, '$1-****-****-$4')
}
