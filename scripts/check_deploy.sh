#!/usr/bin/env bash
# 部署后自检脚本：5 项检查，全部通过退出码为 0，任一失败退出码为 1
#
# 用法：
#   ./scripts/check_deploy.sh
#   PORT=8088 CTX=class-score ./scripts/check_deploy.sh
#   HOST=192.168.56.102 PORT=8080 ./scripts/check_deploy.sh
set -uo pipefail

HOST="${HOST:-localhost}"
PORT="${PORT:-8080}"
CTX="${CTX:-class-score}"
CATALINA="${CATALINA:-/opt/tomcat}"
BASE="http://$HOST:$PORT/$CTX"

pass=0
fail=0
ok() { echo "[PASS] $1"; pass=$((pass + 1)); }
no() { echo "[FAIL] $1"; fail=$((fail + 1)); }

echo "== 部署自检：$BASE =="
echo "Tomcat 目录：$CATALINA"
echo "----------------------------------------"

# 1. Tomcat 进程
if pgrep -f 'org.apache.catalina.startup.Bootstrap' >/dev/null 2>&1; then
    ok "Tomcat 进程存在"
else
    no "Tomcat 进程不存在（$CATALINA/bin/startup.sh；看 $CATALINA/logs/catalina.out）"
fi

# 2. 端口监听（端口冲突时这里就会失败）
if (ss -lnt 2>/dev/null || netstat -lnt 2>/dev/null) | grep -q ":$PORT "; then
    ok "端口 $PORT 已监听"
else
    no "端口 $PORT 未监听（ss -lntp | grep $PORT 查占用，或改 server 端口/端口转发）"
fi

# 3. WAR 是否解压
if [ -d "$CATALINA/webapps/$CTX/WEB-INF" ]; then
    ok "WAR 已解压：$CATALINA/webapps/$CTX"
else
    no "WAR 未解压（确认 webapps/$CTX.war 存在，看 catalina.out 是否 UnpackWARs=false）"
fi

# 4. 健康检查（含数据库连通性）
CODE=$(curl -s -o /tmp/health_check.json -m 8 -w '%{http_code}' "$BASE/health")
BODY=$(head -c 400 /tmp/health_check.json 2>/dev/null)
case "$CODE" in
    200) ok "健康检查 200：$BODY" ;;
    503) no "健康检查 503（数据库不可用）：$BODY" ;;
    404) no "健康检查 404（上下文路径不对，实际部署名可能不是 $CTX）" ;;
    000) no "健康检查无响应（端口/防火墙/转发问题）" ;;
    *)   no "健康检查返回 $CODE：$BODY" ;;
esac

# 5. 业务页面
CODE2=$(curl -s -o /dev/null -m 8 -w '%{http_code}' "$BASE/scores")
if [ "$CODE2" = "200" ]; then
    ok "业务页 /scores 200"
else
    no "业务页 /scores 返回 $CODE2（500 多为数据库账号/库名问题，看 catalina.out）"
fi

echo "----------------------------------------"
echo "结果：PASS=$pass  FAIL=$fail"
if [ "$fail" -eq 0 ]; then
    echo "结论：部署可用"
    exit 0
fi
echo "结论：部署有问题，按上面 [FAIL] 提示排查（详见 DEPLOY.md 第六节）"
exit 1
