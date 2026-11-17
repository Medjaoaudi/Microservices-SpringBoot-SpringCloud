#!/usr/bin/env bash
# Arrête tous les services (ordre inverse du démarrage).
cd "$(dirname "$0")"
for s in frontend gateway inscription cours etudiant discovery; do
  ./stop-service.sh "$s"
done
