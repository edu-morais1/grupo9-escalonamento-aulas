#!/usr/bin/env python3
"""Le resultados/tempos.csv e gera os graficos Tamanho vs Tempo (Greedy vs TCGreedy)."""
import csv
import sys
from pathlib import Path

import matplotlib
matplotlib.use("Agg")
import matplotlib.pyplot as plt

PROJETO = Path(__file__).resolve().parent.parent


def ler_csv(caminho):
    dados = {"Greedy": {"n": [], "tempo_ms": []}, "TCGreedy": {"n": [], "tempo_ms": []}}
    with open(caminho, newline="") as f:
        leitor = csv.DictReader(f)
        for linha in leitor:
            if linha["status"] != "ok":
                continue
            n = int(linha["n"])
            algoritmo = linha["algoritmo"]
            tempo_ms = float(linha["tempo_medio_ns"]) / 1_000_000.0
            dados[algoritmo]["n"].append(n)
            dados[algoritmo]["tempo_ms"].append(tempo_ms)
    return dados


def plotar(dados, caminho_saida, escala_log):
    fig, ax = plt.subplots(figsize=(9, 6))
    for algoritmo, marcador in (("Greedy", "o"), ("TCGreedy", "s")):
        ax.plot(dados[algoritmo]["n"], dados[algoritmo]["tempo_ms"], marker=marcador, label=algoritmo)

    ax.set_xlabel("Tamanho da entrada (n)")
    ax.set_ylabel("Tempo medio de execucao (ms)")
    ax.set_title("Tamanho vs Tempo -- Escalonamento de Aulas (Greedy vs TCGreedy)")
    if escala_log:
        ax.set_xscale("log")
        ax.set_yscale("log")
    ax.legend()
    ax.grid(True, which="both", linestyle="--", alpha=0.4)
    fig.tight_layout()
    fig.savefig(caminho_saida, dpi=150)
    print(f"Grafico salvo em {caminho_saida}")


def main():
    csv_entrada = Path(sys.argv[1]) if len(sys.argv) > 1 else PROJETO / "resultados" / "tempos.csv"
    pasta_saida = PROJETO / "resultados" / "graficos"
    pasta_saida.mkdir(parents=True, exist_ok=True)

    dados = ler_csv(csv_entrada)
    plotar(dados, pasta_saida / "tamanho_vs_tempo_linear.png", escala_log=False)
    plotar(dados, pasta_saida / "tamanho_vs_tempo_log_log.png", escala_log=True)


if __name__ == "__main__":
    main()
