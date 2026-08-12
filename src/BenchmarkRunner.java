import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Roda as duas estrategias (Greedy e TCGreedy) sobre todos os arquivos de entrada,
 * 6 execucoes cada, descarta a primeira (aquecimento de JIT) e grava a media das 5
 * restantes em um CSV, junto com o numero de salas e o numero de operacoes do nucleo guloso.
 *
 * Uso: java BenchmarkRunner <pasta-de-entradas> <arquivo-csv-saida> [timeout-segundos-por-execucao] [n-maximo]
 *
 * timeout-segundos-por-execucao: use 0 (ou qualquer valor <= 0) para desabilitar o
 * orcamento de tempo -- todas as execucoes rodam ate o fim, nao importa quanto demorem.
 */
public class BenchmarkRunner {

    private static final Pattern PADRAO_NOME = Pattern.compile("Aula(\\d+)\\.txt");
    private static final int EXECUCOES = 6;

    public static void main(String[] args) throws IOException {
        if (args.length < 2) {
            System.err.println("Uso: java BenchmarkRunner <pasta-de-entradas> <arquivo-csv-saida> [timeout-segundos-por-execucao] [n-maximo]");
            System.err.println("  timeout-segundos-por-execucao: 0 ou negativo desabilita o orcamento de tempo");
            System.exit(1);
        }

        File pastaEntradas = new File(args[0]);
        File arquivoSaida = new File(args[1]);
        double timeoutSegundos = args.length >= 3 ? Double.parseDouble(args[2]) : 15.0;
        long timeoutNs = timeoutSegundos <= 0
                ? Long.MAX_VALUE // timeout desabilitado: nenhuma execucao e interrompida por tempo
                : (long) (timeoutSegundos * 1_000_000_000L);
        int nMaximo = args.length >= 4 ? Integer.parseInt(args[3]) : Integer.MAX_VALUE;

        if (timeoutSegundos <= 0) {
            System.out.println("Orcamento de tempo desabilitado -- todas as execucoes vao ate o fim.");
        }

        List<File> arquivos = listarArquivosOrdenadosPorTamanho(pastaEntradas);
        arquivos.removeIf(f -> extrairTamanho(f.getName()) > nMaximo);
        if (arquivos.isEmpty()) {
            System.err.println("Nenhum arquivo 'AulaN.txt' encontrado em " + pastaEntradas);
            System.exit(1);
        }

        boolean greedyEstourouTempo = false;
        boolean tcGreedyEstourouTempo = false;

        try (PrintWriter csv = new PrintWriter(new FileWriter(arquivoSaida))) {
            csv.println("algoritmo,n,tempo_medio_ns,tempo_execucao_unica_ns,num_salas,operacoes_medias,status");

            for (File arquivo : arquivos) {
                int n = extrairTamanho(arquivo.getName());

                if (greedyEstourouTempo && tcGreedyEstourouTempo) {
                    // Nenhuma das duas estrategias sera executada neste tamanho: nao ha
                    // motivo para ler e fazer parse do arquivo de entrada correspondente.
                    escreverLinhaPulada(csv, "Greedy", n);
                    escreverLinhaPulada(csv, "TCGreedy", n);
                    csv.flush();
                    continue;
                }

                Aula[] aulas = LeitorEntrada.ler(arquivo.getAbsolutePath());
                System.out.println("== n=" + n + " (" + arquivo.getName() + ") ==");

                if (!greedyEstourouTempo) {
                    greedyEstourouTempo = executarEregistrar(csv, "Greedy", n, aulas,
                            SolucaoGreedy::resolver, timeoutNs);
                } else {
                    escreverLinhaPulada(csv, "Greedy", n);
                }

                if (!tcGreedyEstourouTempo) {
                    tcGreedyEstourouTempo = executarEregistrar(csv, "TCGreedy", n, aulas,
                            SolucaoTCGreedy::resolver, timeoutNs);
                } else {
                    escreverLinhaPulada(csv, "TCGreedy", n);
                }

                csv.flush();
            }
        }

        System.out.println("Resultados gravados em " + arquivoSaida.getAbsolutePath());
    }

    private interface Estrategia {
        EscalonadorGuloso.Resultado resolver(Aula[] aulas);
    }

    /**
     * Executa 6 vezes, descarta a primeira, grava a media das 5 restantes.
     * Retorna true se alguma execucao estourou o orcamento de tempo (sinal para
     * pular tamanhos maiores dessa estrategia).
     */
    private static boolean executarEregistrar(PrintWriter csv, String nomeEstrategia, int n,
            Aula[] aulas, Estrategia estrategia, long timeoutNs) {
        long[] tempos = new long[EXECUCOES];
        EscalonadorGuloso.Resultado ultimoResultado = null;

        for (int i = 0; i < EXECUCOES; i++) {
            long inicio = System.nanoTime();
            ultimoResultado = estrategia.resolver(aulas);
            long duracao = System.nanoTime() - inicio;
            tempos[i] = duracao;

            if (duracao > timeoutNs) {
                System.out.println("  " + nomeEstrategia + " excedeu o orcamento de tempo ("
                        + (duracao / 1_000_000_000.0) + "s) em n=" + n + " -> pulando tamanhos maiores");
                // "duracao" e o tempo de uma unica execucao (a que estourou o orcamento),
                // nao uma media de 5 -- por isso vai em uma coluna separada de
                // tempo_medio_ns, para nao misturar as duas estatisticas.
                escreverLinha(csv, nomeEstrategia, n, "", String.valueOf(duracao),
                        String.valueOf(ultimoResultado.numSalas()),
                        String.valueOf(ultimoResultado.operacoes()), "timeout_parcial");
                return true;
            }
        }

        long somaCinco = 0;
        for (int i = 1; i < EXECUCOES; i++) {
            somaCinco += tempos[i];
        }
        long mediaCinco = somaCinco / (EXECUCOES - 1);

        System.out.println("  " + nomeEstrategia + ": tempo_medio=" + mediaCinco
                + "ns numSalas=" + ultimoResultado.numSalas());
        escreverLinha(csv, nomeEstrategia, n, String.valueOf(mediaCinco), "",
                String.valueOf(ultimoResultado.numSalas()),
                String.valueOf(ultimoResultado.operacoes()), "ok");
        return false;
    }

    private static void escreverLinhaPulada(PrintWriter csv, String nomeEstrategia, int n) {
        escreverLinha(csv, nomeEstrategia, n, "", "", "", "", "timeout_pulado");
    }

    private static void escreverLinha(PrintWriter csv, String algoritmo, int n, String tempoMedioNs,
            String tempoExecucaoUnicaNs, String numSalas, String operacoesMedias, String status) {
        csv.println(String.join(",", algoritmo, String.valueOf(n), tempoMedioNs,
                tempoExecucaoUnicaNs, numSalas, operacoesMedias, status));
    }

    private static List<File> listarArquivosOrdenadosPorTamanho(File pasta) {
        File[] brutos = pasta.listFiles((dir, nome) -> PADRAO_NOME.matcher(nome).matches());
        List<File> arquivos = new ArrayList<>();
        if (brutos != null) {
            arquivos.addAll(Arrays.asList(brutos));
        }
        arquivos.sort((a, b) -> Integer.compare(extrairTamanho(a.getName()), extrairTamanho(b.getName())));
        return arquivos;
    }

    private static int extrairTamanho(String nomeArquivo) {
        Matcher m = PADRAO_NOME.matcher(nomeArquivo);
        if (m.matches()) {
            return Integer.parseInt(m.group(1));
        }
        throw new IllegalArgumentException("Nome de arquivo inesperado: " + nomeArquivo);
    }
}
