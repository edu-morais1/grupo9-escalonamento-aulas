#!/usr/bin/env bash
set -euo pipefail

DIR_PROJETO="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
DIR_ENTRADAS="${1:-$DIR_PROJETO/../Entradas para o Problema do Escalonamento de Aulas}"
CSV_SAIDA="${2:-$DIR_PROJETO/resultados/tempos.csv}"
# 0 (ou negativo) desabilita o orcamento de tempo: todas as execucoes rodam ate o fim.
TIMEOUT_SEGUNDOS="${3:-15}"
N_MAXIMO="${4:-}"

echo "Compilando..."
mkdir -p "$DIR_PROJETO/out"
javac -d "$DIR_PROJETO/out" "$DIR_PROJETO"/src/*.java

echo "Rodando benchmark sobre: $DIR_ENTRADAS"
mkdir -p "$(dirname "$CSV_SAIDA")"
if [ -n "$N_MAXIMO" ]; then
  java -cp "$DIR_PROJETO/out" BenchmarkRunner "$DIR_ENTRADAS" "$CSV_SAIDA" "$TIMEOUT_SEGUNDOS" "$N_MAXIMO"
else
  java -cp "$DIR_PROJETO/out" BenchmarkRunner "$DIR_ENTRADAS" "$CSV_SAIDA" "$TIMEOUT_SEGUNDOS"
fi
