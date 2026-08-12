# Escalonamento de Aulas — Transformar para Conquistar + Estratégia Gulosa

Trabalho da disciplina de Projeto e Análise de Algoritmos (Csc2152, UNIOESTE,
2026) — **Grupo 9**.

**Autores:** Gabriel Jared de Barros Amorim, Jonathan Santos Tadei, Lucas
Ivanov Costa, Vinicius Eduardo Morais Oliveira.

## O problema

Dadas `n` aulas, cada uma com um horário de início e término, alocar o menor
número possível de salas de forma que nenhuma sala tenha duas aulas
sobrepostas ao mesmo tempo (aulas consecutivas, onde o término de uma coincide
com o início da próxima, podem usar a mesma sala).

Duas soluções são implementadas, compartilhando o mesmo núcleo guloso e
diferindo apenas na ordenação da entrada antes de rodá-lo:

- **Greedy**: processa as aulas na ordem original do arquivo.
- **TCGreedy**: ordena as aulas por horário de início (a etapa
  "Transformar" do paradigma Transformar para Conquistar) antes de rodar o
  mesmo núcleo guloso.

O relatório completo (fundamentação teórica, análise de complexidade linha a
linha e resultados experimentais) está em
[`relatorio/relatorio-grupo9.pdf`](relatorio/relatorio-grupo9.pdf).

## Estrutura

```
src/            código-fonte Java
scripts/        automação do benchmark e geração de gráficos
resultados/     CSV com os tempos medidos e os gráficos gerados
relatorio/      relatório final em PDF
```

## Como compilar e rodar

Sem build tool (Maven/Gradle) — apenas `javac`/`java`.

```bash
# Compilar
javac -d out src/*.java

# Rodar o benchmark completo sobre um diretório de entradas, escrevendo um CSV,
# com um orçamento de tempo por execução (em segundos; 0 desativa o limite)
./scripts/run_benchmark.sh "<pasta-de-entradas>" "resultados/tempos.csv" <timeout-segundos>

# Regenerar os gráficos (tempo x tamanho, linear e log-log) a partir do CSV
python3 scripts/plot_resultados.py resultados/tempos.csv
```

Os arquivos de entrada esperados têm duas linhas de inteiros
separados por espaço: a primeira com os horários de início de cada aula, a
segunda com os horários de término (mesmo índice = mesma aula). Este
repositório não inclui os datasets de entrada usados na coleta do relatório
(fornecidos pela disciplina).

## Arquitetura

O núcleo (`EscalonadorGuloso.alocarSalas`) é uma varredura *first-fit*: para
cada aula, percorre as salas já abertas e reutiliza a primeira cuja última
aula já tenha terminado; abre uma sala nova se nenhuma servir. Ele é O(n²) no
pior caso, já que a lista de salas pode crescer até O(n) e cada aula faz uma
varredura linear nela.

- `SolucaoGreedy` chama o núcleo direto, sem ordenar.
- `SolucaoTCGreedy` ordena as aulas por início (Merge Sort implementado
  manualmente, sem usar `Arrays.sort`) e então chama o mesmo núcleo — o que
  adiciona um custo O(n log n) sem mudar a classe de pior caso final (O(n²),
  dominada pelo núcleo).

`BenchmarkRunner` mede cada estratégia sobre cada arquivo de entrada com 6
execuções (descarta a primeira como aquecimento de JIT e tira a média das 5
restantes), registrando tempo médio, número de salas usadas e número de
operações de comparação do núcleo.

## Licença / origem do material

Este repositório contém apenas o código e o relatório produzidos pelo grupo.
Os datasets de entrada e o material de aula usados como referência teórica
não são redistribuídos aqui.
