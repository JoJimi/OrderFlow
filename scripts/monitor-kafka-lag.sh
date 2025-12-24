#!/bin/bash

# ================= 설정 =================
CONTAINER_NAME="orderFlow-kafka"
TOPIC_NAME="order-event"
GROUP_NAME="payment-service-group"
INTERVAL=5
# ========================================

echo "Kafka Consumer Lag 모니터링 시작..."
echo "======================================"
echo "Topic: $TOPIC_NAME"
echo "Consumer Group: $GROUP_NAME"
echo "Container: $CONTAINER_NAME"
echo "======================================"
echo ""

# 로그 파일 생성
LOG_FILE="kafka-lag-$(date +%Y%m%d-%H%M%S).log"
echo "Time,Partition,Current-Offset,Log-End-Offset,Lag,Consumer-Id" | tee -a "$LOG_FILE"

KAFKA_CMD="docker exec $CONTAINER_NAME kafka-consumer-groups"

echo "사용 명령어: $KAFKA_CMD"
echo "--------------------------------------"

# 모니터링 루프
while true; do
    TIMESTAMP=$(date +%H:%M:%S)

    # --bootstrap-server는 컨테이너 내부 통신이므로 localhost:9092 사용
    RESULT=$($KAFKA_CMD --bootstrap-server localhost:9092 --group "$GROUP_NAME" --describe --timeout 10000)

    # 결과가 비어있으면 스킵하지 않고 에러인지 확인
    if [ -z "$RESULT" ]; then
        echo "($TIMESTAMP) 데이터 조회 실패 (명령어 에러 또는 연결 지연)"
    else
        # 결과 필터링 및 출력
        echo "$RESULT" | grep "$TOPIC_NAME" | while read -r line; do
            PARTITION=$(echo "$line" | awk '{print $2}')
            CURRENT_OFFSET=$(echo "$line" | awk '{print $3}')
            LOG_END_OFFSET=$(echo "$line" | awk '{print $4}')
            LAG=$(echo "$line" | awk '{print $5}')
            CONSUMER_ID=$(echo "$line" | awk '{print $6}')

            OUTPUT="$TIMESTAMP,$PARTITION,$CURRENT_OFFSET,$LOG_END_OFFSET,$LAG,$CONSUMER_ID"
            echo "$OUTPUT" | tee -a "$LOG_FILE"
        done
    fi

    sleep $INTERVAL
done