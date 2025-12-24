#!/bin/bash

# Feign Client 병목 테스트 결과 분석 스크립트

echo "🔍 Feign Client 동기 통신 병목 테스트 분석"
echo "================================================"
echo ""

# Circuit Breaker 상태 확인
echo "📊 Circuit Breaker 상태 확인..."
CIRCUIT_STATE=$(curl -s http://localhost:9083/actuator/health | jq -r '.components.circuitBreakers.details.ProductClient.state' 2>/dev/null)

if [ "$CIRCUIT_STATE" == "OPEN" ]; then
    echo "   ⚠️  Circuit Breaker: OPEN (장애 감지 - Fallback 작동 중)"
elif [ "$CIRCUIT_STATE" == "HALF_OPEN" ]; then
    echo "   🔄 Circuit Breaker: HALF_OPEN (복구 시도 중)"
elif [ "$CIRCUIT_STATE" == "CLOSED" ]; then
    echo "   ✅ Circuit Breaker: CLOSED (정상)"
else
    echo "   ❌ Circuit Breaker 상태 확인 불가"
fi

echo ""

# Thread Pool 상태
echo "🧵 Thread Pool 상태..."
THREADS_BUSY=$(curl -s http://localhost:9083/actuator/metrics/tomcat.threads.busy | jq -r '.measurements[0].value' 2>/dev/null)
THREADS_MAX=$(curl -s http://localhost:9083/actuator/metrics/tomcat.threads.config.max | jq -r '.measurements[0].value' 2>/dev/null)

if [ ! -z "$THREADS_BUSY" ] && [ ! -z "$THREADS_MAX" ]; then
    USAGE=$(echo "scale=2; ($THREADS_BUSY / $THREADS_MAX) * 100" | bc)
    echo "   - 사용 중인 스레드: $THREADS_BUSY / $THREADS_MAX (${USAGE}%)"

    if (( $(echo "$USAGE > 90" | bc -l) )); then
        echo "   ⚠️  Thread Pool 고갈 위험! (90% 이상 사용)"
    elif (( $(echo "$USAGE > 70" | bc -l) )); then
        echo "   ⚠️  Thread Pool 사용률 높음 (70% 이상)"
    else
        echo "   ✅ Thread Pool 여유 있음"
    fi
else
    echo "   ❌ Thread Pool 메트릭 확인 불가"
fi

echo ""

# Resilience4j 메트릭
echo "🔧 Resilience4j 메트릭..."
FAILURE_RATE=$(curl -s http://localhost:9083/actuator/metrics/resilience4j.circuitbreaker.failure.rate?tag=name:ProductClient | jq -r '.measurements[0].value' 2>/dev/null)
SLOW_CALL_RATE=$(curl -s http://localhost:9083/actuator/metrics/resilience4j.circuitbreaker.slow.call.rate?tag=name:ProductClient | jq -r '.measurements[0].value' 2>/dev/null)

if [ ! -z "$FAILURE_RATE" ]; then
    FAILURE_PCT=$(echo "scale=2; $FAILURE_RATE * 100" | bc)
    echo "   - 실패율: ${FAILURE_PCT}%"

    if (( $(echo "$FAILURE_RATE > 0.5" | bc -l) )); then
        echo "     ⚠️  실패율 50% 초과 - Circuit Breaker OPEN 예상"
    fi
fi

if [ ! -z "$SLOW_CALL_RATE" ]; then
    SLOW_PCT=$(echo "scale=2; $SLOW_CALL_RATE * 100" | bc)
    echo "   - 지연 호출 비율: ${SLOW_PCT}%"
fi

echo ""

# K6 테스트 결과 파일이 있다면 분석
if [ -f "summary.json" ]; then
    echo "📈 K6 테스트 결과 요약..."

    # Baseline 결과
    BASELINE_P95=$(cat summary.json | jq -r '.metrics."order_response_time{scenario:baseline}".values."p(95)"' 2>/dev/null)
    if [ ! -z "$BASELINE_P95" ] && [ "$BASELINE_P95" != "null" ]; then
        echo "   정상 상태 P95: ${BASELINE_P95}ms"
    fi

    # Delayed 결과
    DELAYED_P95=$(cat summary.json | jq -r '.metrics."order_response_time{scenario:delayed}".values."p(95)"' 2>/dev/null)
    if [ ! -z "$DELAYED_P95" ] && [ "$DELAYED_P95" != "null" ]; then
        echo "   지연 상태 P95: ${DELAYED_P95}ms"

        if [ ! -z "$BASELINE_P95" ] && [ "$BASELINE_P95" != "null" ]; then
            INCREASE=$(echo "scale=2; ($DELAYED_P95 / $BASELINE_P95)" | bc)
            echo "   📊 응답시간 증가율: ${INCREASE}배"

            if (( $(echo "$INCREASE > 2" | bc -l) )); then
                echo "   ⚠️  동기 호출 병목 현상 확인!"
            fi
        fi
    fi

    # Circuit Breaker 결과
    CB_P95=$(cat summary.json | jq -r '.metrics."order_response_time{scenario:circuit_breaker}".values."p(95)"' 2>/dev/null)
    if [ ! -z "$CB_P95" ] && [ "$CB_P95" != "null" ]; then
        echo "   Circuit Breaker 상태 P95: ${CB_P95}ms"

        if [ ! -z "$DELAYED_P95" ] && [ "$DELAYED_P95" != "null" ]; then
            IMPROVEMENT=$(echo "scale=2; (($DELAYED_P95 - $CB_P95) / $DELAYED_P95) * 100" | bc)
            echo "   📊 응답시간 개선: ${IMPROVEMENT}%"

            if (( $(echo "$IMPROVEMENT > 30" | bc -l) )); then
                echo "   ✅ Circuit Breaker 효과 확인!"
            fi
        fi
    fi

    # 에러율
    ERROR_RATE=$(cat summary.json | jq -r '.metrics.error_rate.values.rate' 2>/dev/null)
    if [ ! -z "$ERROR_RATE" ] && [ "$ERROR_RATE" != "null" ]; then
        ERROR_PCT=$(echo "scale=2; $ERROR_RATE * 100" | bc)
        echo "   전체 에러율: ${ERROR_PCT}%"

        if (( $(echo "$ERROR_RATE > 0.5" | bc -l) )); then
            echo "   ❌ 에러율 50% 초과 - 시스템 불안정"
        elif (( $(echo "$ERROR_RATE > 0.2" | bc -l) )); then
            echo "   ⚠️  에러율 20% 초과 - 개선 필요"
        else
            echo "   ✅ 에러율 양호"
        fi
    fi
fi

echo ""
echo "================================================"
echo "✅ 분석 완료"
echo ""
echo "💡 팁:"
echo "   - Circuit Breaker가 OPEN이면 Product Service 복구 후 자동으로 CLOSED로 전환됩니다"
echo "   - Thread Pool 고갈 시 Bulkhead 설정으로 동시 호출 수를 제한하세요"
echo "   - 상세 로그는 logs/order-service.log를 확인하세요"