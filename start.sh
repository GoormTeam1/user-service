#!/bin/bash

# ✅ 설정값
APP_NAME="user-service"
JAR_NAME="user.jar"
APP_DIR="/home/ubuntu/backend/$APP_NAME"
LOG_DIR="/home/ubuntu/backend/logs/service/$APP_NAME"
LOG_FILE="$LOG_DIR/$APP_NAME.log"
BACKUP_JAR="$APP_DIR/${JAR_NAME}.bak"
ENV_FILE="$APP_DIR/../.env"
PORT=8081
HEALTH_URL="http://localhost:$PORT/actuator/health"

echo "▶ [$APP_NAME] 배포 시작"

# ✅ .env 로드
if [ -f "$ENV_FILE" ]; then
  echo "▶ .env 로드 중..."
  export $(grep -v '^#' "$ENV_FILE" | xargs)
else
  echo "⚠️ .env 파일이 존재하지 않습니다: $ENV_FILE"
fi

# ✅ 로그 디렉토리 생성
mkdir -p "$LOG_DIR"

# ✅ 기존 JAR 백업
if [ -f "$APP_DIR/$JAR_NAME" ]; then
  cp "$APP_DIR/$JAR_NAME" "$BACKUP_JAR"
  echo "✅ 기존 JAR 백업 완료"
fi

# ✅ 기존 프로세스 종료
PID=$(pgrep -f "$JAR_NAME")
if [ -n "$PID" ]; then
  kill -9 "$PID"
  echo "✅ 기존 프로세스 종료 완료 (PID: $PID)"
else
  echo "ℹ️ 실행 중인 프로세스 없음"
fi

# ✅ 새 JAR 실행
echo "▶ 새 버전 실행 중..."
nohup java -jar "$APP_DIR/$JAR_NAME" > "$LOG_FILE" 2>&1 &

# ✅ 헬스 체크 (최대 10초 대기)
echo "⏳ 헬스 체크 중..."
success=false
for i in {1..10}; do
  sleep 5
  STATUS=$(curl -s -o /dev/null -w "%{http_code}" "$HEALTH_URL")
  if [ "$STATUS" == "200" ]; then
    success=true
    break
  fi
done

# ✅ 헬스 체크 결과에 따라 처리
if [ "$success" = true ]; then
  echo "✅ 서버 실행 성공"
  echo "[RESULT] SUCCESS"
  exit 0
else
  echo "❌ 헬스 체크 실패. 롤백 시도 중..."

  # 실패한 프로세스 종료
  ROLLBACK_PID=$(pgrep -f "$JAR_NAME")
  [ -n "$ROLLBACK_PID" ] && kill -9 "$ROLLBACK_PID"

  # 롤백
  if [ -f "$BACKUP_JAR" ]; then
    mv "$BACKUP_JAR" "$APP_DIR/$JAR_NAME"
    echo "▶ 롤백 JAR 실행 중..."
    nohup java -jar "$APP_DIR/$JAR_NAME" > "$LOG_FILE" 2>&1 &

    # 롤백 후 헬스 체크
    for i in {1..10}; do
      sleep 5
      STATUS=$(curl -s -o /dev/null -w "%{http_code}" "$HEALTH_URL")
      if [ "$STATUS" == "200" ]; then
        echo "✅ 롤백 성공"
        echo "[RESULT] ROLLBACK_SUCCESS"
        exit 0
      fi
    done

    echo "❌ 롤백 후 헬스 체크도 실패"
    echo "[RESULT] ROLLBACK_FAILED"
    exit 1
  else
    echo "⚠️ 롤백할 백업 JAR 없음"
    echo "[RESULT] ROLLBACK_FAILED"
    exit 1
  fi
fi
