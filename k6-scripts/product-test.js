import http from 'k6-scripts/http';
import { check, sleep } from 'k6';

export const options = {
    vus: 50,
    duration: '30s',
};

export default function () {
    // 1. Swagger에서 쓰고 있는 'Bearer ' 제외한 순수 토큰값을 여기에 붙여넣으세요
    const token = '';

    // 2. 헤더 설정 (Authorization 추가)
    const params = {
        headers: {
            'Authorization': `Bearer ${token}`,
            'Content-Type': 'application/json',
        },
    };

    // 3. 요청 보낼 때 params(헤더)를 같이 전달
    const res = http.get('http://127.0.0.1:8082/api/products/SEED-1-000050', params);

    check(res, { 'status is 200': (r) => r.status === 200 });
    sleep(0.1);
}