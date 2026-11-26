#!/usr/bin/env bash
# 部署 WAR 到 Tomcat 9，上下文路径可外置（默认 class-score）
#
# 用法：
#   ./deploy_war.sh target/class-score.war               # 部署为 /class-score
#   ./deploy_war.sh target/class-score.war grade         # 部署为 /grade
#   PORT=8080 CTX=grade ./deploy_war.sh target/class-score.war grade
#
# 说明：上下文路径由 WAR 文件名决定（Tomcat 默认按文件名映射），
#      所以这里通过重命名 WAR 实现上下文路径外置，不需要改 pom.xml 重新打包。
set -uo pipefail

WAR="${1:-}"
CTX="${2:-${CTX:-class-score}}"
CATALINA="${3:-${CATALINA:-/opt/tomcat}}"
PORT="${PORT:-8080}"

if [ -z "$WAR" ] || [ ! -f "$WAR" ]; then
    echo "用法: $0 <war包路径> [上下文路径] [Tomcat目录]"
    echo "例:   $0 target/class-score.war class-score /opt/tomcat"
    exit 1
fi

echo "== 1. 检查目标环境 =="
echo "Tomcat 目录 : $CATALINA"
echo "上下文路径  : /$CTX"
echo "WAR 包      : $WAR ($(du -h "$WAR" | cut -f1))"

if [ ! -d "$CATALINA/webapps" ]; then
    echo "[FAIL] 找不到 $CATALINA/webapps，确认 Tomcat 目录是否正确"
    exit 1
fi
if pgrep -f 'org.apache.catalina.startup.Bootstrap' >/dev/null; then
    echo "[ OK ] Tomcat 进程运行中，依赖 autoDeploy 自动解压（约 10 秒）"
else
    echo "[WARN] Tomcat 未运行，部署后请执行 $CATALINA/bin/startup.sh"
fi

echo "== 2. 复制 WAR（先清理同名旧目录，避免残留旧版本） =="
# 常见问题：上一次用 root 部署，webapps/<ctx> 属主是 root，普通用户删不掉，
# 会导致 Tomcat 解压失败、JSP scratchDir 不可用（访问 404）。这里自动降级为 sudo。
SUDO=""
if [ -d "$CATALINA/webapps/$CTX" ] && [ ! -w "$CATALINA/webapps/$CTX" ]; then
    SUDO="sudo -n"
    echo "[WARN] $CATALINA/webapps/$CTX 属主不是当前用户，使用 sudo 清理与复制"
fi
$SUDO rm -rf "${CATALINA:?}/webapps/$CTX"
$SUDO rm -f "$CATALINA/webapps/$CTX.war"
$SUDO rm -rf "$CATALINA/work/Catalina/localhost/$CTX"
$SUDO cp "$WAR" "$CATALINA/webapps/$CTX.war"
$SUDO chown "$(id -un):$(id -gn)" "$CATALINA/webapps/$CTX.war" 2>/dev/null || true
echo "[ OK ] 已复制到 $CATALINA/webapps/$CTX.war"

echo "== 3. 等待解压 =="
for i in $(seq 1 30); do
    if [ -d "$CATALINA/webapps/$CTX/WEB-INF" ]; then
        echo "[ OK ] 已解压到 $CATALINA/webapps/$CTX（${i}s）"
        break
    fi
    sleep 1
done
if [ ! -d "$CATALINA/webapps/$CTX/WEB-INF" ]; then
    echo "[FAIL] 30 秒内未解压，检查 $CATALINA/logs/catalina.out"
    exit 1
fi

echo "== 4. 健康检查 =="
sleep 2
CODE=$(curl -s -o /tmp/health.json -w '%{http_code}' -m 10 "http://localhost:$PORT/$CTX/health")
echo "HTTP $CODE"
cat /tmp/health.json 2>/dev/null
echo ""
if [ "$CODE" = "200" ]; then
    echo "[ OK ] 访问地址：http://<服务器IP>:$PORT/$CTX/scores"
else
    echo "[FAIL] 健康检查未通过，执行 scripts/check_deploy.sh 查看分项结果"
    exit 2
fi
