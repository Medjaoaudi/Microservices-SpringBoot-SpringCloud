#!/usr/bin/env bash
# =====================================================================
#  Arrête UN service (simulation de panne pendant la démo)
#  Usage : ./stop-service.sh etudiant|cours|inscription|gateway|discovery|frontend
# =====================================================================
case "$1" in
  etudiant|etudiant-service)       PORT=8081 ;;
  cours|cours-service)             PORT=8082 ;;
  inscription|inscription-service) PORT=8083 ;;
  gateway|api-gateway)             PORT=8888 ;;
  discovery|eureka|discovery-server) PORT=8761 ;;
  frontend)                        PORT=5173 ;;
  *) echo "Usage : $0 etudiant|cours|inscription|gateway|discovery|frontend"; exit 1 ;;
esac

kill_port() {
  local port=$1 pids=""
  if command -v netstat > /dev/null 2>&1 && [[ "$(uname -s)" =~ MINGW|MSYS|CYGWIN ]]; then
    # Windows (Git Bash) : PID Windows via netstat, arrêt via taskkill
    pids=$(netstat -ano | grep -E "[:.]$port[[:space:]].*LISTENING" | awk '{print $NF}' | sort -u)
    for p in $pids; do taskkill //PID "$p" //F > /dev/null 2>&1 && echo "  processus $p arrêté (port $port)"; done
  else
    pids=$(lsof -ti "tcp:$port" -sTCP:LISTEN 2>/dev/null)
    for p in $pids; do kill -9 "$p" && echo "  processus $p arrêté (port $port)"; done
  fi
  [ -z "$pids" ] && echo "  aucun processus n'écoute sur le port $port"
  return 0
}

echo "==> Arrêt de $1 (port $PORT)"
kill_port "$PORT"
