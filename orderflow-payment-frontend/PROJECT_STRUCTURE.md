# OrderFlow Payment Frontend 구조

```
orderflow-payment-frontend/
│
├── index.html                    # 진입점
├── package.json                  # 의존성 관리
├── vite.config.js               # Vite 설정
├── .env.development             # 개발 환경변수
├── .env.production              # 운영 환경변수
│
├── public/
│   └── favicon.ico
│
└── src/
    ├── main.js                  # Vue 앱 초기화
    ├── App.vue                  # 루트 컴포넌트
    │
    ├── api/                     # API 호출 모듈
    │   ├── index.js             # Axios 인스턴스
    │   └── payment.js           # 결제 API
    │
    ├── components/              # 컴포넌트
    │   └── payment/
    │       ├── PaymentWidget.vue      # Toss 결제 위젯
    │       ├── PaymentResult.vue      # 결제 결과 화면
    │       └── PaymentHistory.vue     # 결제 내역
    │
    ├── views/                   # 페이지 뷰
    │   ├── PaymentPage.vue      # 결제 페이지
    │   ├── PaymentSuccessPage.vue    # 결제 성공
    │   └── PaymentFailPage.vue       # 결제 실패
    │
    ├── router/                  # 라우터
    │   └── index.js
    │
    ├── stores/                  # Pinia 스토어
    │   └── payment.js
    │
    └── utils/                   # 유틸리티
        └── format.js            # 금액 포맷팅 등
```

## 핵심 결제 흐름

1. 사용자가 주문 → 백엔드에서 Payment 레코드 생성
2. 프론트에서 결제 위젯 정보 요청 (`GET /api/payments/{orderId}/widget`)
3. Toss 결제 위젯 초기화 및 결제 진행
4. 결제 완료 시 콜백 → 승인 요청 (`POST /api/payments/confirm`)
5. 결과 페이지로 리다이렉트
