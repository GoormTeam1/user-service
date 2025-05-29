#!/bin/bash

# ✅ 설정값
APP_NAME="user-service"
JAR_NAME="user.jar"
APP_DIR="/home/ubuntu/backend/$APP_NAME"
LOG_FILE="$APP_DIR/$APP_NAME.log"
LOG_DIR="/home/ubuntu/backend/logs/service/$APP_NAME"

echo "▶ [$APP_NAME] 배포 시작"

# ✅ 로그 디렉토리 생성
echo "▶ 로그 디렉토리 생성 중..."
mkdir -p "$LOG_DIR"

# ✅ 기존 프로세스 종료
echo "▶ 기존 프로세스 종료 중..."
PID=$(pgrep -f "$JAR_NAME")
if [ -n "$PID" ]; then
  kill -9 $PID
  echo "✅ PID $PID 종료 완료"
else
  echo "ℹ️ 기존 프로세스 없음"
fi

# ✅ 새 jar 실행
echo "▶ 새 앱 실행 중..."
nohup java -jar "$APP_DIR/$JAR_NAME" > "$LOG_FILE" 2>&1 &

echo "✅ [$APP_NAME] 배포 완료. 로그: $LOG_FILE"
