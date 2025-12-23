import http from 'k6/http';
import { check, sleep } from 'k6';

export const options = {
    vus: 50,
    duration: '30s',
};

export default function () {
    // 1. Swagger에서 쓰고 있는 'Bearer ' 제외한 순수 토큰값을 여기에 붙여넣으세요
    const token = 'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiIxIiwidG9rZW5fdHlwZSI6ImFjY2Vzc1Rva2VuIiwiZmFtIjoiZTIzNGFlYjgtMDg4ZS00ODI3LWI1NTctNzRlNWYzN2RjMTU2Iiwicm9sZSI6IlJPTEVfQURNSU4iLCJpYXQiOjE3NjY0NTAwNzYsImV4cCI6MTc2NjQ1MzY3Nn0.o_Zzh5LxImAP4s3HM5Z6t-L9w-A6b4kSdB970bQwML4';

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