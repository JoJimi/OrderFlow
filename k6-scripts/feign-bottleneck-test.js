import http from 'k6/http';
import { check, sleep } from 'k6';
import { Counter, Trend, Rate } from 'k6/metrics';

// 커스텀 메트릭
const orderSuccess = new Counter('order_success');
const orderFailure = new Counter('order_failure');
const responseTime = new Trend('response_time');
const errorRate = new Rate('error_rate');
const circuitBreakerOpen = new Counter('circuit_breaker_open');

// 테스트 설정
export const options = {
    scenarios: {
        // Product Service 지연 상태에서 부하 테스트
        delay_stress_test: {
            executor: 'ramping-vus',
            startVUs: 5,
            stages: [
                { duration: '30s', target: 10 },   // 0-30초: 점진적 증가
                { duration: '1m', target: 20 },    // 30-90초: 더 증가
                { duration: '2m', target: 20 },    // 90-210초: 부하 유지 (Circuit Breaker 발동 대기)
                { duration: '1m', target: 30 },    // 210-270초: 최대 부하 (Circuit OPEN 후 빠른 실패 확인)
                { duration: '30s', target: 5 },    // 270-300초: 점진적 감소
            ],
            gracefulRampDown: '30s',
        },
    },

    thresholds: {
        'error_rate': ['rate<0.7'],                     // 에러율 70% 미만 (Circuit 열리면 높아짐)
        'response_time': ['p(95)<5000', 'p(99)<8000'],  // P95 5초, P99 8초 이내
        'http_req_duration': ['p(95)<5000'],
    },
};

const JWT_TOKEN = '';
const BASE_URL = 'http://localhost:8083';

const headers = {
    'Content-Type': 'application/json',
    'Authorization': `Bearer ${JWT_TOKEN}`,
};

function getRandomProducts() {
    const productCount = Math.floor(Math.random() * 2) + 1;
    const products = [];

    for (let i = 0; i < productCount; i++) {
        const productId = `SEED-1-${String(Math.floor(Math.random() * 100000) + 1).padStart(6, '0')}`;
        products.push({
            productId: productId,
            quantity: Math.floor(Math.random() * 2) + 1,
        });
    }

    return products;
}

function getOrderPayload() {
    return {
        items: getRandomProducts(),
        shippingAddress: '서울시 강남구 테헤란로 123',
        shippingCity: '서울',
        shippingPostalCode: '06234',
    };
}

export default function () {
    const iteration = __ITER;
    const startTime = Date.now();

    const payload = JSON.stringify(getOrderPayload());
    const response = http.post(
        `${BASE_URL}/api/orders`,
        payload,
        {
            headers,
            timeout: '8s',
            tags: { name: 'create_order' },
        }
    );

    const duration = Date.now() - startTime;
    responseTime.add(duration);

    // 응답 검증
    const isSuccess = check(response, {
        'status is 201': (r) => r.status === 201,
        'status is not 5xx': (r) => r.status < 500,
        'has orderId': (r) => {
            try {
                const body = JSON.parse(r.body);
                return body.orderId !== undefined;
            } catch {
                return false;
            }
        },
        'response time < 8s': () => duration < 8000,
    });

    if (isSuccess) {
        orderSuccess.add(1);
        errorRate.add(0);

        if (duration > 2500) {
            console.log(`⏱️ 느린 응답 - Status: ${response.status}, Duration: ${duration}ms (Product Service 지연 영향)`);
        }
    } else {
        orderFailure.add(1);
        errorRate.add(1);

        // 에러 상세 로깅
        if (response.status === 503) {
            circuitBreakerOpen.add(1);
            console.log(`🔌 Circuit Breaker OPEN - Duration: ${duration}ms (빠른 실패)`);
        } else if (response.status >= 500) {
            console.log(`❌ Server Error ${response.status} - Duration: ${duration}ms`);
        } else if (response.status === 0) {
            console.log(`🔥 Timeout - Duration: ${duration}ms`);
        }
    }

    sleep(Math.random() * 0.5 + 0.3);
}

export function handleSummary(data) {
    console.log('\n' + '='.repeat(80));
    console.log('📊 Product Service 지연 상태 병목 테스트 결과');
    console.log('='.repeat(80));

    // 응답시간 분석
    const respTime = data.metrics.response_time;
    if (respTime) {
        console.log('\n⏱️ 응답시간 분석');
        console.log(`   - 평균: ${respTime.values.avg.toFixed(2)}ms`);
        console.log(`   - 중앙값: ${respTime.values.med.toFixed(2)}ms`);
        console.log(`   - P90: ${respTime.values['p(90)'].toFixed(2)}ms`);
        console.log(`   - P95: ${respTime.values['p(95)'].toFixed(2)}ms`);
        console.log(`   - P99: ${respTime.values['p(99)'].toFixed(2)}ms`);
        console.log(`   - 최대: ${respTime.values.max.toFixed(2)}ms`);

        if (respTime.values['p(95)'] > 2000) {
            console.log('\n   ⚠️ P95 응답시간이 2초 초과!');
            console.log('   → Product Service 2초 지연이 Order Service로 전파됨');
            console.log('   → 동기 호출(Feign Client) 병목 현상 확인!');
        }
    }

    const totalRequests = data.metrics.http_reqs.values.count;
    const successCount = data.metrics.order_success?.values.count || 0;
    const failureCount = data.metrics.order_failure?.values.count || 0;
    const cbOpenCount = data.metrics.circuit_breaker_open?.values.count || 0;

    console.log('\n📈 요청 처리 결과');
    console.log(`   - 총 요청: ${totalRequests}개`);
    console.log(`   - 성공: ${successCount}개 (${((successCount/totalRequests)*100).toFixed(2)}%)`);
    console.log(`   - 실패: ${failureCount}개 (${((failureCount/totalRequests)*100).toFixed(2)}%)`);
    console.log(`   - Circuit Breaker OPEN: ${cbOpenCount}개`);

    if (cbOpenCount > 0) {
        console.log('\n🔌 Circuit Breaker 동작 확인');
        console.log(`   - OPEN 횟수: ${cbOpenCount}회`);
        console.log(`   - 전체 실패의 ${((cbOpenCount/failureCount)*100).toFixed(2)}%가 Circuit Breaker에 의한 빠른 실패`);
        console.log('   ✅ Circuit Breaker가 느린 응답을 차단하고 빠르게 실패 처리');
    }

    const errRate = data.metrics.error_rate;
    if (errRate) {
        console.log('\n📉 에러율');
        console.log(`   - 전체: ${(errRate.values.rate * 100).toFixed(2)}%`);

        if (errRate.values.rate > 0.5) {
            console.log('   ⚠️ 에러율 50% 초과');
            console.log('   → Circuit Breaker가 활성화되어 많은 요청을 빠르게 실패 처리 중');
            console.log('   → 이는 시스템 전체가 느려지는 것을 방지하는 정상적인 동작입니다');
        } else {
            console.log('   ✅ 에러율 양호 - 시스템이 지연을 견디고 있음');
        }
    }

    const httpDuration = data.metrics.http_req_duration;
    if (httpDuration) {
        console.log('\n🌐 HTTP 요청 Duration (Gateway → Order → Product)');
        console.log(`   - P95: ${httpDuration.values['p(95)'].toFixed(2)}ms`);
        console.log(`   - P99: ${httpDuration.values['p(99)'].toFixed(2)}ms`);
    }

    console.log('\n📅 예상 타임라인');
    console.log('   0-30초:    VUs 5→10, 부하 증가 시작');
    console.log('   30-90초:   VUs 10→20, 지연 영향 축적');
    console.log('   90-210초:  VUs 20 유지, Circuit Breaker 임계값 도달');
    console.log('   210-270초: VUs 20→30, Circuit OPEN 후 빠른 실패');
    console.log('   270-300초: VUs 30→5, 부하 감소');

    console.log('\n💡 핵심 인사이트');
    console.log('   1. Product Service 2초 지연이 Order Service 응답시간에 직접 반영됨');
    console.log('   2. Feign Client 동기 호출로 인한 Thread Pool 점유 시간 증가');
    console.log('   3. Circuit Breaker가 일정 실패율 도달 시 빠른 실패로 시스템 보호');
    console.log('   4. 비동기 처리(Kafka) 도입 시 이러한 병목 현상 해소 가능');

    console.log('\n' + '='.repeat(80));

    return {
        'stdout': JSON.stringify(data, null, 2),
        'summary.json': JSON.stringify(data, null, 2),
    };
}

export function setup() {
    console.log('\n🚀 테스트 시작 준비...');
    console.log('⚠️ 주의: Product Service에 2초 지연이 주입되어 있어야 합니다!');
    console.log('   application-dev.yml에서 test.delay.enabled=true 확인');
    console.log('\n테스트 시나리오:');
    console.log('   - 5분 동안 점진적으로 부하 증가 (5 → 30 VUs)');
    console.log('   - Product Service 지연에 따른 응답시간 증가 관찰');
    console.log('   - Circuit Breaker 동작 및 빠른 실패 확인\n');
}

export function teardown(data) {
    console.log('\n✅ 테스트 완료!');
    console.log('📁 결과 파일: summary.json\n');
}