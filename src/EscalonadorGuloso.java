/**
 * Nucleo guloso compartilhado pelas duas solucoes do Grupo 9 (Greedy e TCGreedy).
 * A unica diferenca entre as solucoes esta na ordem das aulas passadas para alocarSalas:
 * - Greedy: ordem original do arquivo de entrada.
 * - TCGreedy: ordem crescente por horario de inicio (etapa de "Transformar").
 */
public class EscalonadorGuloso {

    public record Resultado(int numSalas, long operacoes) {
    }

    public static Resultado alocarSalas(Aula[] aulas) {
        int[] terminoDaSala = new int[aulas.length];
        int numSalas = 0;
        long operacoes = 0;

        for (Aula aula : aulas) {
            int salaEscolhida = -1;

            for (int s = 0; s < numSalas; s++) {
                operacoes++;
                if (terminoDaSala[s] <= aula.inicio()) {
                    salaEscolhida = s;
                    break;
                }
            }

            if (salaEscolhida == -1) {
                salaEscolhida = numSalas;
                numSalas++;
            }
            terminoDaSala[salaEscolhida] = aula.termino();
        }

        return new Resultado(numSalas, operacoes);
    }
}
