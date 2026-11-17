#!/usr/bin/env bash
# =====================================================================
#  Démarrage complet de l'architecture Scolarité (Linux / macOS / Git Bash)
#  Usage : ./start-all.sh                 (build + démarrage, profil H2)
#          ./start-all.sh --no-build      (démarrage sans recompiler)
#          SPRING_PROFILE=mysql ./start-all.sh   (bases MySQL, cf. docker-compose.yml)
# =====================================================================
set -e
cd "$(dirname "$0")"
mkdir -p logs

PROFILE="${SPRING_PROFILE:-h2}"
JAVA_OPTS="${JAVA_OPTS:--Xms128m -Xmx384m -Dfile.encoding=UTF-8}"

wait_for() { # $1=url  $2=timeout(s)  $3=libellé
  local url=$1 max=$2 label=$3 i=0
  printf "    attente de %s " "$label"
  until curl -s -o /dev/null -w "%{http_code}" "$url" 2>/dev/null | grep -q "200"; do
    i=$((i + 2)); [ $i -ge "$max" ] && { echo " (délai dépassé, on continue)"; return 0; }
    printf "."; sleep 2
  done
  echo " OK"
}

start_service() { # $1=nom du module
  local name=$1
  nohup java $JAVA_OPTS -jar "$name/target/$name-1.0.0.jar" --spring.profiles.active="$PROFILE" \
    > "logs/$name.log" 2>&1 &
  echo "  -> $name démarré (pid $!) - logs/$name.log"
}

if [ "$1" != "--no-build" ]; then
  echo "==> Build Maven (mvn clean package -DskipTests)"
  mvn -q clean package -DskipTests
fi

echo "==> 1/4 discovery-server (Eureka, port 8761)"
start_service discovery-server
sleep 15
wait_for "http://localhost:8761/actuator/health" 60 "Eureka"

echo "==> 2/4 microservices métier (profil $PROFILE)"
start_service etudiant-service
start_service cours-service
start_service inscription-service
wait_for "http://localhost:8081/actuator/health" 120 "etudiant-service"
wait_for "http://localhost:8082/actuator/health" 120 "cours-service"
wait_for "http://localhost:8083/actuator/health" 120 "inscription-service"

echo "==> 3/4 api-gateway (port 8888)"
start_service api-gateway
wait_for "http://localhost:8888/actuator/health" 90 "api-gateway"

echo "==> 4/4 frontend (port 5173)"
if command -v npm > /dev/null 2>&1 && [ -d frontend ]; then
  ( cd frontend
    [ -d node_modules ] || npm install --no-audit --no-fund > ../logs/frontend-install.log 2>&1
    nohup npm run dev > ../logs/frontend.log 2>&1 & )
  echo "  -> frontend démarré - logs/frontend.log"
else
  echo "  (npm introuvable : frontend ignoré)"
fi

cat <<EOF

=====================================================================
  Eureka            : http://localhost:8761
  API Gateway       : http://localhost:8888/api/inscriptions/1
  Swagger agrégé    : http://localhost:8888/swagger-ui.html
  Swagger services  : http://localhost:8081/swagger-ui.html (8082, 8083)
  Circuit breakers  : http://localhost:8083/actuator/circuitbreakers
  Frontend          : http://localhost:5173
  Panne simulée     : ./stop-service.sh etudiant
=====================================================================
EOF
