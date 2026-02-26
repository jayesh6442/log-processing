#!/usr/bin/env bash
set -euo pipefail

AUTH=http://localhost:8080
ING=http://localhost:8081
PROC=http://localhost:8082

EMAIL="verify$(date +%s)@example.com"
PASS="Passw0rd!"

# 1) register
REG_BODY=$(jq -nc --arg e "$EMAIL" --arg p "$PASS" '{email:$e,password:$p,role:"USER"}')
REG_RESP=$(curl -sS -X POST "$AUTH/auth/register" -H "Content-Type: application/json" -d "$REG_BODY")
REG_ID=$(echo "$REG_RESP" | jq -r '.id // empty')
[ -n "$REG_ID" ] || { echo "REGISTER_FAIL $REG_RESP"; exit 1; }

# 2) login
LOGIN_BODY=$(jq -nc --arg e "$EMAIL" --arg p "$PASS" '{email:$e,password:$p}')
LOGIN_RESP=$(curl -sS -X POST "$AUTH/auth/login" -H "Content-Type: application/json" -d "$LOGIN_BODY")
TOKEN=$(echo "$LOGIN_RESP" | jq -r '.accessToken // .token // empty')
[ -n "$TOKEN" ] || { echo "LOGIN_FAIL $LOGIN_RESP"; exit 1; }

# 3) create service
SVC_NAME="orders-$(date +%s)"
SVC_BODY=$(jq -nc --arg n "$SVC_NAME" '{name:$n}')
SVC_RESP=$(curl -sS -X POST "$ING/api/services" -H "Authorization: Bearer $TOKEN" -H "Content-Type: application/json" -d "$SVC_BODY")
SERVICE_ID=$(echo "$SVC_RESP" | jq -r '.serviceId // .id // empty')
API_KEY=$(echo "$SVC_RESP" | jq -r '.apiKey // empty')
[ -n "$SERVICE_ID" ] && [ -n "$API_KEY" ] || { echo "SERVICE_FAIL $SVC_RESP"; exit 1; }

# 4) create alert rule (retry for async service-events sync)
ALERT_BODY=$(jq -nc --argjson sid "$SERVICE_ID" '{serviceId:$sid,level:"ERROR",threshold:2,windowMinutes:10}')
ALERT_RESP=""
ALERT_ID=""
for i in {1..12}; do
  ALERT_RESP=$(curl -sS -X POST "$PROC/api/v1/alerts" -H "Authorization: Bearer $TOKEN" -H "Content-Type: application/json" -d "$ALERT_BODY")
  ALERT_ID=$(echo "$ALERT_RESP" | jq -r '.id // .alertId // empty')
  if [ -n "$ALERT_ID" ]; then
    break
  fi
  sleep 1
done
[ -n "$ALERT_ID" ] || { echo "ALERT_RULE_FAIL $ALERT_RESP"; exit 1; }

# offsets before ingest
BEFORE_OFFSET=$(docker compose exec -T kafka kafka-get-offsets --bootstrap-server localhost:9092 --topic alerts 2>/dev/null | awk -F: '{sum+=$3} END{print sum+0}')

# 5) ingest 2 ERROR logs
for i in 1 2; do
  LOG_BODY=$(jq -nc --arg msg "Payment timeout $i" --arg tr "trace-$(date +%s)-$i" '{level:"ERROR",message:$msg,traceId:$tr}')
  CODE=$(curl -sS -o /tmp/ingest_resp.json -w "%{http_code}" -X POST "$ING/api/v1/logs" -H "Authorization: Bearer $TOKEN" -H "X-API-KEY: $API_KEY" -H "Content-Type: application/json" -d "$LOG_BODY")
  [ "$CODE" = "202" ] || { echo "INGEST_FAIL_$i code=$CODE body=$(cat /tmp/ingest_resp.json)"; exit 1; }
  sleep 1
done

sleep 5

# 6) query logs
Q_RESP=$(curl -sS "$PROC/api/v1/logs?serviceId=$SERVICE_ID&level=ERROR&page=0&size=10&sort=timestamp,desc" -H "Authorization: Bearer $TOKEN")
TOTAL=$(echo "$Q_RESP" | jq -r '.totalElements // 0')
[ "$TOTAL" -ge 2 ] || { echo "QUERY_FAIL $Q_RESP"; exit 1; }

# 7) alert_event persisted
ALERT_EVENTS=$(docker compose exec -T postgres psql -U postgres -d logsphere -tAc "select count(*) from alert_events where alert_id = $ALERT_ID;")
[ "${ALERT_EVENTS:-0}" -ge 1 ] || { echo "ALERT_EVENT_FAIL count=${ALERT_EVENTS:-0}"; exit 1; }

# 8) alerts topic moved
AFTER_OFFSET=$(docker compose exec -T kafka kafka-get-offsets --bootstrap-server localhost:9092 --topic alerts 2>/dev/null | awk -F: '{sum+=$3} END{print sum+0}')
DELTA=$((AFTER_OFFSET - BEFORE_OFFSET))
[ "$DELTA" -ge 1 ] || { echo "ALERT_TOPIC_FAIL before=$BEFORE_OFFSET after=$AFTER_OFFSET"; exit 1; }

echo "RESULT register=true login=true service=true ingest=true query=true alertRule=true alertEvent=true alertTopic=true serviceId=$SERVICE_ID alertId=$ALERT_ID total=$TOTAL delta=$DELTA"
