# 1º Trabalho de PAA — Grupo 9

**Tema:** Transformar para Conquistar (TC) + Estratégia Gulosa
**Problema:** Distribuição de aulas em salas (minimizar o número de salas usadas)

**Integrantes:**
- Gabriel Jared de Barros Amorim
- Jonathan Santos Tadei
- Lucas Ivanov Costa
- Vinicius Eduardo Morais Oliveira

> Este arquivo é um rascunho de conteúdo. Antes da entrega final, migrar para o
> template oficial de formatação indicado no link "download" da Seção 10 do
> enunciado (baixar do Microsoft Teams).

## 1. Introdução e descrição do problema

O problema consiste em alocar `n` aulas, cada uma com um horário de início e
término, ao menor número possível de salas, respeitando as restrições:
- Duas aulas não podem ocupar a mesma sala no mesmo instante;
- Não é necessário intervalo entre duas aulas consecutivas na mesma sala (uma
  aula pode começar exatamente no instante em que a anterior termina).

Foram implementadas duas soluções gulosas que compartilham o mesmo núcleo de
alocação, diferindo apenas na etapa de pré-processamento da entrada:

- **Greedy**: processa as aulas na ordem original do arquivo de entrada.
- **TCGreedy**: aplica a etapa de "Transformar" — ordena as aulas por horário
  de início, de forma crescente — e só então aplica o mesmo núcleo guloso.

Este problema é um caso clássico da literatura de algoritmos: corresponde
exatamente ao Exercício 16.1-4 de Cormen et al. (2009), apresentado logo após
o problema de seleção de atividades (Seção 16.1) como o problema de
*escalonar atividades usando o menor número possível de salas de
conferência*. O próprio livro observa que esse problema equivale à coloração
de um grafo de intervalos (cada aula é um vértice, e duas aulas incompatíveis
— que se sobrepõem — são ligadas por uma aresta): o número mínimo de salas
necessárias é igual ao número cromático desse grafo, que por sua vez é igual
à *profundidade máxima* de sobreposição simultânea de aulas (o maior número
de aulas ativas ao mesmo tempo, em qualquer instante). O mesmo problema — com
a mesma função de seleção usada pelo TCGreedy, "selecionar a aula com o menor
tempo de início" — também é apresentado no material da disciplina, na seção
"Escalonamento de Tarefas" (Brun, 2026).

## 2. Descrição das soluções

### 2.1 Núcleo guloso compartilhado (`alocarSalas`)

Mantém uma lista de salas abertas, cada uma com o horário de término da última
aula alocada nela. Para cada aula, na ordem em que é processada:
1. Percorre as salas já abertas em busca da primeira cujo horário de término
   seja menor ou igual ao horário de início da aula atual (*first-fit*);
2. Se encontrar, reutiliza essa sala (atualiza seu horário de término);
3. Caso contrário, abre uma nova sala para a aula atual.

```
alocarSalas(aulas):
    terminoDaSala = []
    para cada aula em aulas:
        sala = procurar em terminoDaSala uma sala com termino <= aula.inicio
        se encontrou:
            atualizar termino dessa sala para aula.termino
        senao:
            abrir nova sala com termino = aula.termino
    retornar numero de salas abertas
```

### 2.2 Greedy

Aplica `alocarSalas` diretamente, sem qualquer ordenação prévia.

### 2.3 TCGreedy

Ordena o vetor de aulas por horário de início (`Arrays.sort`, O(n log n)) e em
seguida aplica a **mesma** função `alocarSalas`. Essa combinação de
ordenação por início seguida de alocação gulosa corresponde exatamente ao
algoritmo de escalonamento apresentado em aula (Brun, 2026), cuja função de
seleção é "selecionar a tarefa com o menor tempo de início".

## 3. Análise de complexidade assintótica (teórica)

### 3.1 Modelo de custo

Seguindo o método de contagem de operações básicas apresentado em aula
(atribuição, teste lógico, incremento, indexação, soma e retorno como
operações de custo unitário; o tempo de uma sequência de comandos é o
maior tempo de qualquer comando da sequência; o tempo de comandos
aninhados é obtido pela soma/produto das complexidades), reescrevemos o
núcleo `alocarSalas` com laços indexados, equivalentes ao `for-each`
usado na implementação em Java:

```
1   terminoDaSala = novo vetor de tamanho n
2   numSalas = 0
3   operacoes = 0
4   for (i = 0; i < n; i++)
5       salaEscolhida = -1
6       for (s = 0; s < numSalas; s++)
7           operacoes = operacoes + 1
8           if (terminoDaSala[s] <= aulas[i].inicio)
9               salaEscolhida = s
10              break
11      if (salaEscolhida == -1)
12          salaEscolhida = numSalas
13          numSalas = numSalas + 1
14      terminoDaSala[salaEscolhida] = aulas[i].termino
15  retornar numSalas, operacoes
```

### 3.2 Núcleo guloso — pior caso (comum às duas soluções)

O pior caso ocorre quando a condição da linha 8 nunca é verdadeira — por
exemplo, quando as `n` aulas se sobrepõem todas mutuamente (compartilham
um mesmo instante em comum). Nesse cenário, o laço interno nunca encontra
sala livre: roda até o fim sem `break` (linhas 9-10 nunca executam) e o
`if` da linha 11 é sempre verdadeiro (sempre abre sala nova). Quando o
laço da linha 6 começa a executar para a i-ésima aula (i = 0, ..., n-1),
o valor de `numSalas` naquele momento é exatamente `i`, pois uma sala
nova foi aberta em cada uma das `i` iterações anteriores. Isso
caracteriza um **laço aninhado dependente**: o limite do laço interno
cresce junto com o índice do laço externo, de forma análoga ao exemplo
de laços dependentes visto em aula — e por isso seu custo total é obtido
somando uma progressão aritmética.

Custo de cada linha, no pior caso:

| Linha | Custo/execução | Nº de execuções | Total |
|---|---|---|---|
| 1 | n (alocação/zeragem) | 1 | n |
| 2 | 1 atrib. | 1 | 1 |
| 3 | 1 atrib. | 1 | 1 |
| 4 | 1 atrib. + n(tlog+incr) + tlog | 1 | 2n+2 |
| 5 | 1 atrib. | n vezes | n |
| 6 | 1 atrib. + i(tlog+incr) + tlog | Σ(i=0 a n-1), numSalas=i | n²+n |
| 7 | 1 atrib. | Σ(i=0 a n-1) i | (n²-n)/2 |
| 8 | 1 tlog | Σ(i=0 a n-1) i | (n²-n)/2 |
| 9-10 | — | pior caso: nunca executa | 0 |
| 11 | 1 tlog | n vezes | n |
| 12-13 | 2 atrib. | n vezes (sempre verdadeiro) | 2n |
| 14 | 1 index + 1 atrib. | n vezes | 2n |
| 15 | 1 retorno | 1 | O(1) |

Somando todas as linhas:

```
T_nucleo(n) = n + 1 + 1 + (2n+2) + n + (n²+n) + (n²-n)/2 + (n²-n)/2 + n + 2n + 2n + O(1)

T_nucleo(n) = 2n² + 9n + O(1) ∈ Θ(n²)
```

ou seja, **O(n²)** no pior caso.

### 3.3 Greedy

O Greedy aplica apenas o núcleo, diretamente sobre a entrada, sem custo
adicional:

```
T_Greedy(n) = T_nucleo(n) = 2n² + 9n + O(1) ∈ O(n²)
```

### 3.4 TCGreedy

O TCGreedy soma ao núcleo o custo da ordenação por horário de início,
tratada como uma chamada de função de complexidade conhecida (assim como
funções puras vistas em aula, ex. `raiz(n)`, `potencia(base,expoente)`).
O método `Arrays.sort` usado para ordenar um vetor de objetos (como
`Aula[]`) implementa uma variante estável de *merge sort* (TimSort), cujo
número de comparações no pior caso é `T_sort(n) = n·log2(n)`. Logo:

```
T_TCGreedy(n) = T_sort(n) + T_nucleo(n)
              = n·log2(n) + 2n² + 9n + O(1) ∈ O(n²)
```

pois o termo quadrático domina o termo n·log(n) para n suficientemente
grande.

### 3.5 Consequência teórica

**As duas soluções pertencem à mesma classe assintótica de pior caso,
O(n²)**, já que compartilham o mesmo núcleo de alocação — o termo
n·log(n) adicional do TCGreedy é absorvido pelo termo quadrático que
domina em ambos os polinômios. A diferença entre as soluções não aparece
na classe assintótica de pior caso, mas sim (a) no comportamento médio
prático e (b) na qualidade da solução (número de salas), como discutido
na Seção 5.

## 4. Metodologia experimental

### 4.1 Configuração da máquina de testes

- **SO:** Arch Linux, kernel `7.1.3-arch1-3`
- **Processador:** Intel(R) Core(TM) i5-8265U CPU @ 1.60GHz (4 núcleos / 8 threads)
- **Memória RAM:** 7,6 GiB
- **Linguagem/Runtime:** Java (OpenJDK 21.0.11)
- **IDE:** nenhuma — compilação e execução via linha de comando (terminal),
  sem uso de ambiente de desenvolvimento integrado
- **Compilação:** `javac`, sem flags de otimização especiais
- **Execução:** `java -cp out BenchmarkRunner <pasta-entradas> <csv-saida> <timeout>`

*(Ajustar esta seção se o grupo rodar os experimentos em outra máquina.)*

### 4.2 Processo de coleta dos tempos

Para cada um dos tamanhos de entrada disponibilizados e para cada estratégia:
1. A entrada é lida uma única vez (a leitura do arquivo **não** entra na
   medição de tempo);
2. O núcleo guloso é executado 6 vezes sobre os mesmos dados em memória,
   cronometrado com `System.nanoTime()`;
3. A primeira execução é descartada (aquecimento de JIT);
4. Calcula-se a média das 5 execuções restantes;
5. Também são registrados, por execução: o número de salas resultante e o
   número de operações de comparação do núcleo guloso (variáveis de
   interesse).
6. O `BenchmarkRunner` suporta um orçamento de tempo opcional por execução
   (parametrizável ou totalmente desligável), pensado para evitar que uma
   entrada muito grande trave o benchmark indefinidamente. A coleta final
   apresentada neste relatório foi feita com esse orçamento **desligado**,
   para obter tempos reais mesmo nos tamanhos maiores — o maior tempo médio
   observado foi o do Greedy em n=1.000.000, ~121,1s por execução.

Os dados brutos ficam em `resultados/tempos.csv`.

## 5. Resultados

**Figura 1.** `resultados/graficos/tamanho_vs_tempo_linear.png` — tempo médio de
execução (ms) em função do tamanho da entrada (n), escala linear.

**Figura 2.** `resultados/graficos/tamanho_vs_tempo_log_log.png` — mesmos dados
em escala log-log, que evidencia melhor o comportamento nas ordens de
grandeza menores.

Dados completos em `resultados/tempos.csv` — 28 tamanhos, de n=10 a
n=1.000.000. Todas as execuções completaram normalmente, sem necessidade de
interromper por tempo.

| n | Tempo médio Greedy (ms) | Tempo médio TCGreedy (ms) | Salas Greedy | Salas TCGreedy |
|---|---|---|---|---|
| 10 | 0,007 | 0,017 | 7 | 7 |
| 100 | 0,190 | 0,238 | 62 | 53 |
| 1.000 | 0,110 | 0,396 | 531 | 481 |
| 10.000 | 7,373 | 16,222 | 4.576 | 4.405 |
| 20.000 | 26,452 | 35,551 | 9.703 | 9.606 |
| 30.000 | 65,945 | 65,458 | 13.234 | 12.864 |
| 50.000 | 194,644 | 188,233 | 25.313 | 25.090 |
| 100.000 | 770,891 | 727,892 | 50.402 | 49.979 |
| 150.000 | 1.905,461 | 1.681,344 | 75.314 | 74.868 |
| 250.000 | 4.934,210 | 4.551,336 | 125.691 | 124.877 |
| 350.000 | 10.156,943 | 9.018,185 | 175.876 | 175.219 |
| 500.000 | 21.163,517 | 21.774,828 | 251.179 | 250.175 |
| 750.000 | 50.509,619 | 44.313,597 | 376.653 | 375.352 |
| 1.000.000 | 121.092,902 | 93.954,894 | 642.836 | 642.819 |

Principais observações:
- **Número de salas** cresce de forma aproximadamente linear com n em ambas as
  soluções (ex.: n=250.000 → 125.691 salas no Greedy, 124.877 no TCGreedy;
  n=1.000.000 → 642.836 no Greedy, 642.819 no TCGreedy), confirmando que o
  núcleo guloso de fato se comporta como O(n²) na prática (não apenas no
  pior caso teórico), já que o número de salas abertas `r` é proporcional a
  `n`.
- **TCGreedy realiza menos operações** de comparação no núcleo guloso do que
  Greedy, e resulta em **menos ou o mesmo número de salas** (qualidade de
  solução igual ou melhor), para todos os tamanhos testados — a ordenação
  por início ajuda o *first-fit* a encontrar salas livres mais cedo, em
  média, e evita alocações desnecessárias.
- **Ponto de cruzamento no tempo de parede:** para n pequeno (até ~n=20.000),
  o Greedy é mais rápido em tempo de parede, pois o custo constante do
  `Arrays.sort` supera a economia de operações do núcleo (ex.: n=20.000 →
  Greedy 26,5ms vs TCGreedy 35,6ms). A partir de n≈30.000, esse quadro se
  inverte — o TCGreedy passa a ser mais rápido na maioria dos tamanhos
  maiores (ex.: n=30.000 → Greedy 65,9ms vs TCGreedy 65,5ms; n=1.000.000 →
  Greedy 121.092,9ms vs TCGreedy 93.954,9ms), pois o custo O(n log n) do
  sort passa a ser irrelevante frente à economia de operações O(n²) do
  núcleo. Há uma inversão pontual em n=500.000 (Greedy 21.163,5ms vs
  TCGreedy 21.774,8ms), provavelmente causada por ruído de medição (pausas
  de *garbage collector* ou variação de carga do sistema) e não por uma
  mudança real de comportamento algorítmico, já que a tendência volta a
  favorecer o TCGreedy nos dois tamanhos seguintes.

## 6. Análise crítica

As duas soluções compartilham exatamente o mesmo núcleo guloso, e por isso
pertencem à **mesma classe assintótica de pior caso, O(n²)** — a etapa de
"Transformar" (ordenação, O(n log n)) não muda essa classe, pois o núcleo
domina o crescimento assintótico. Isso ficou evidente na prática: o número de
operações e o número de salas crescem de forma consistente com n² nas duas
soluções (a razão operações/n² permanece aproximadamente estável ao longo dos
tamanhos testados).

Ainda assim, o "Transformar" tem um efeito prático real e mensurável, mesmo
sem mudar a classe assintótica de pior caso:
- Ele **reduz a constante multiplicativa** do núcleo O(n²) (menos operações
  por aula, em média, porque salas livres são encontradas mais cedo);
- Ele **melhora a qualidade da solução** (menos salas usadas) — um benefício
  que nem chega a ser exigido nos critérios formais do Grupo 9, mas é um
  efeito colateral positivo interessante do pré-processamento;
- Ele **adiciona um custo fixo** (o sort em si), que só compensa a partir de
  um tamanho de entrada onde a economia de operações do núcleo supera esse
  custo — daí o ponto de cruzamento observado por volta de n≈20.000–30.000.

A melhora na qualidade da solução (item anterior) não é uma coincidência
empírica, mas consequência direta da teoria de coloração de grafos de
intervalos discutida na Seção 3 (Cormen et al., 2009): o número mínimo de
salas necessárias é igual à profundidade máxima de sobreposição simultânea
de aulas, e processar as aulas em ordem crescente de horário de início,
alocando cada uma à primeira sala livre disponível — exatamente o que o
TCGreedy faz — é conhecido por atingir esse mínimo teórico. Já o Greedy
processa as aulas na ordem arbitrária do arquivo de entrada, sem essa
garantia: uma aula que começa cedo mas aparece tarde no arquivo pode ser
processada depois de aulas que começam mais tarde, fazendo com que o
algoritmo abra salas que uma aula ainda não processada poderia ter
reaproveitado. É por isso que o TCGreedy iguala ou usa menos salas que o
Greedy em **todos** os tamanhos testados, nunca o contrário.

Esse resultado ilustra bem o princípio geral do paradigma Transformar para
Conquistar: pré-processar a entrada não necessariamente melhora a complexidade
assintótica de pior caso do algoritmo seguinte, mas pode melhorar
substancialmente o comportamento médio/prático — e o ganho só se manifesta
quando a etapa de "Conquistar" de fato tira proveito da transformação (aqui,
o first-fit tira proveito de estar processando aulas em ordem crescente de
início, mesmo sem ter sido redesenhado para isso).

Os tempos observados em n=1.000.000 (a maior entrada testada — Greedy
121,09s, TCGreedy 93,95s por execução) reforçam, na prática, por que O(n²) é
considerado proibitivo para entradas grandes: cada execução isolada já leva
mais de um minuto, mesmo em um processador moderno.

## 7. Conclusão

O experimento mostrou que, embora Greedy e TCGreedy compartilhem o mesmo
núcleo de alocação e por isso tenham a mesma classe assintótica de pior caso
(O(n²)), a etapa de pré-ordenação do TCGreedy tem impacto prático real: reduz
o número de operações do núcleo e iguala ou reduz o número de salas usadas
em todos os tamanhos testados, e passa a compensar seu próprio custo (o
sort) a partir de n≈20.000–30.000, tornando-se a solução mais rápida em
tempo de parede na maior parte dos tamanhos maiores testados (com uma única
inversão pontual em n=500.000, provavelmente ruído de medição). Isso
confirma que o valor do paradigma Transformar para Conquistar não está
necessariamente em mudar a ordem de complexidade assintótica, mas em explorar
uma estrutura na entrada (aqui, a ordem cronológica das aulas) que barateia o
comportamento médio do algoritmo que vem em seguida. Os tempos crescentes nas
entradas maiores testadas (chegando a mais de um minuto por execução em
n=1.000.000) também evidenciaram, de forma concreta, por que algoritmos O(n²)
se tornam inviáveis à medida que n cresce.

## 8. Código-fonte

O código-fonte completo está disponível na pasta `src/` deste projeto,
incluindo os comandos de medição de tempo (`System.nanoTime()`) e contagem de
operações (`EscalonadorGuloso.Resultado.operacoes()`), conforme exigido na
Seção 11 do enunciado.
