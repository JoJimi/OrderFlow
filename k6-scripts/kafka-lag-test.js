import http from 'k6/http';
import { check, sleep } from 'k6';

export const options = {
    stages: [
        { duration: '10s', target: 10 },    // 워밍업: 10 VUs
        { duration: '60s', target: 50 },    // 부하: 50 VUs
        { duration: '30s', target: 0 },     // 안정화
    ],
};

const ACCESS_TOKEN = 'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiIxIiwidG9rZW5fdHlwZSI6ImFjY2Vzc1Rva2VuIiwiZmFtIjoiZGNiNWM0NzQtMWQ4ZS00M2UwLTkzMmMtZjRkNzZkODEyYTBkIiwicm9sZSI6IlJPTEVfQURNSU4iLCJpYXQiOjE3NjY1MzY3ODIsImV4cCI6MTc2NjU0MDM4Mn0.0yOG9EKn_oDj8Eqvo_N7uXTS_9U2w4eSVWixRpttuu0';
const BASE_URL = 'http://127.0.0.1:8083';

// 자주 사용되는 상품 ID (캐시 HIT 확률 높음)
const PRODUCT_IDS = [
    'SEED-1-000001', 'SEED-1-000002', 'SEED-1-000003',
    'SEED-1-000004', 'SEED-1-000005', 'SEED-1-000006',
    'SEED-1-000007', 'SEED-1-000008', 'SEED-1-000009',
    'SEED-1-000010'
];

export default function () {
    const payload = JSON.stringify({
        items: [
            {
                productId: PRODUCT_IDS[Math.floor(Math.random() * PRODUCT_IDS.length)],
                quantity: Math.floor(Math.random() * 2) + 1
            }
        ],
        shippingAddress: `서울시 강남구 테헤란로 ${Math.floor(Math.random() * 100) + 1}번길`
    });

    const params = {
        headers: {
            'Content-Type': 'application/json',
            'Authorization': `Bearer ${ACCESS_TOKEN}`,
        },
        timeout: '30s', // 타임아웃 설정
    };

    const res = http.post(`${BASE_URL}/api/orders`, payload, params);

    check(res, {
        'success': (r) => r.status === 200 || r.status === 201,
    });

    sleep(0.5); // 더 긴 대기 시간 (부하 감소)
}

export function setup() {
    console.log('========================================');
    console.log('Kafka Lag 측정 테스트 (부하 감소 버전)');
    console.log('========================================');
    console.log('Phase 1: Warmup    (10s, 10 VUs)');
    console.log('Phase 2: Load      (60s, 50 VUs)');
    console.log('Phase 3: Cooldown  (30s, 0 VUs)');
    console.log('========================================');

    return { startTime: Date.now() };
}

export function teardown(data) {
    const duration = (Date.now() - data.startTime) / 1000;
    console.log('\n========================================');
    console.log(`✓ 테스트 완료 (${duration.toFixed(1)}초)`);
    console.log('========================================');
}