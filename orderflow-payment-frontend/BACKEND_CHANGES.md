# 백엔드 수정 필요 사항

## 1. PaymentService.java 수정

OrderEventConsumer에서 `cancelPayment(String orderId)` 를 호출하는데, 
기존 메서드는 `cancelPayment(String orderId, String cancelReason)` 입니다.

### 추가할 메서드:

```java
/**
 * 결제 취소 (이벤트 기반 - 기본 취소 사유 사용)
 */
@Transactional
public void cancelPayment(String orderId) {
    cancelPayment(orderId, "주문 취소에 의한 결제 취소");
}
```

## 2. 환경 변수 설정 (.env 파일)

```bash
# Toss Payments API 키 (테스트용)
TOSS_CLIENT_KEY=test_ck_xxxxxxxxxxxxxxxxxxxxxxxx
TOSS_SECRET_KEY=test_sk_xxxxxxxxxxxxxxxxxxxxxxxx
```

## 3. Toss 개발자센터에서 웹훅 URL 등록

실제 운영 시 다음 URL을 웹훅으로 등록해야 합니다:
- `https://your-domain.com/payment-service/api/payments/webhook/toss`

## 4. CORS 설정 (API Gateway 또는 SecurityConfig)

프론트엔드에서 API 호출을 위해 CORS 설정이 필요합니다.

```java
// SecurityConfig 또는 WebMvcConfig에 추가
@Bean
public CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration configuration = new CorsConfiguration();
    configuration.setAllowedOrigins(Arrays.asList("http://localhost:3000"));
    configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
    configuration.setAllowedHeaders(Arrays.asList("*"));
    configuration.setAllowCredentials(true);
    
    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", configuration);
    return source;
}
```
