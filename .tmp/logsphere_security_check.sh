#!/usr/bin/env bash
set -euo pipefail

AUTH=http://localhost:8080
ING=http://localhost:8081
PROC=http://localhost:8082
PASS='Passw0rd!'

register_login() {
  local email="$1"
  local reg login token
  reg=$(curl -sS -X POST "$AUTH/auth/register" -H "Content-Type: application/json" -d "{\"email\":\"$email\",\"password\":\"$PASS\",\"role\":\"USER\"}")
  login=$(curl -sS -X POST "$AUTH/auth/login" -H "Content-Type: application/json" -d "{\"email\":\"$email\",\"password\":\"$PASS\"}")
  token=$(echo "$login" | jq -r '.accessToken // .token // empty')
  [ -n "$token" ] || { echo "TOKEN_FAIL $email $login"; exit 1; }
  echo "$token"
}

EMAIL1="sec1$(date +%s)@example.com"
EMAIL2="sec2$(date +%s)@example.com"
TOKEN1=$(register_login "$EMAIL1")
TOKEN2=$(register_login "$EMAIL2")

SVC_RESP=$(curl -sS -X POST "$ING/api/services" -H "Authorization: Bearer $TOKEN1" -H "Content-Type: application/json" -d '{"name":"tenant-a-service"}')
SERVICE1=$(echo "$SVC_RESP" | jq -r '.serviceId // .id // empty')
APIKEY1=$(echo "$SVC_RESP" | jq -r '.apiKey // empty')
[ -n "$SERVICE1" ] && [ -n "$APIKEY1" ] || { echo "SERVICE1_FAIL $SVC_RESP"; exit 1; }

# 1) missing JWT
C1=$(curl -sS -o /tmp/sec1.json -w "%{http_code}" -X POST "$ING/api/v1/logs" -H "X-API-KEY: $APIKEY1" -H "Content-Type: application/json" -d '{"level":"ERROR","message":"x","traceId":"t1"}')
[ "$C1" != "200" ] || { echo "SEC_FAIL missing_jwt accepted"; exit 1; }

# 2) missing API key
C2=$(curl -sS -o /tmp/sec2.json -w "%{http_code}" -X POST "$ING/api/v1/logs" -H "Authorization: Bearer $TOKEN1" -H "Content-Type: application/json" -d '{"level":"ERROR","message":"x","traceId":"t2"}')
[ "$C2" != "200" ] || { echo "SEC_FAIL missing_api_key accepted"; exit 1; }

# 3) wrong API key
C3=$(curl -sS -o /tmp/sec3.json -w "%{http_code}" -X POST "$ING/api/v1/logs" -H "Authorization: Bearer $TOKEN1" -H "X-API-KEY: wrong-key" -H "Content-Type: application/json" -d '{"level":"ERROR","message":"x","traceId":"t3"}')
[ "$C3" != "200" ] || { echo "SEC_FAIL wrong_api_key accepted"; exit 1; }

# 4) create one real log for tenant A
C4=$(curl -sS -o /tmp/sec4.json -w "%{http_code}" -X POST "$ING/api/v1/logs" -H "Authorization: Bearer $TOKEN1" -H "X-API-KEY: $APIKEY1" -H "Content-Type: application/json" -d '{"level":"ERROR","message":"tenant-a-log","traceId":"ta1"}')
[[ "$C4" = "200" || "$C4" = "202" ]] || { echo "SEC_FAIL valid_ingest failed code=$C4"; exit 1; }
sleep 3

# 5) tenant B query tenant A service
Q2=$(curl -sS "$PROC/api/v1/logs?serviceId=$SERVICE1&page=0&size=10" -H "Authorization: Bearer $TOKEN2")
T2=$(echo "$Q2" | jq -r '.totalElements // 0')
[ "$T2" = "0" ] || { echo "SEC_FAIL cross_tenant_query_leak total=$T2 body=$Q2"; exit 1; }

# 6) tenant B create alert for tenant A service should fail
A2_CODE=$(curl -sS -o /tmp/sec_alert2.json -w "%{http_code}" -X POST "$PROC/api/v1/alerts" -H "Authorization: Bearer $TOKEN2" -H "Content-Type: application/json" -d "{\"serviceId\":$SERVICE1,\"level\":\"ERROR\",\"threshold\":1,\"windowMinutes\":5}")
[ "$A2_CODE" != "200" ] && [ "$A2_CODE" != "201" ] || { echo "SEC_FAIL cross_tenant_alert_created body=$(cat /tmp/sec_alert2.json)"; exit 1; }

echo "SECURITY_RESULT missingJwt=$C1 missingApiKey=$C2 wrongApiKey=$C3 crossTenantQueryTotal=$T2 crossTenantAlertCode=$A2_CODE"
