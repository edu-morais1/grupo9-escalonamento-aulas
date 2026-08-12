import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.StringTokenizer;

public class LeitorEntrada {

    public static Aula[] ler(String caminhoArquivo) throws IOException {
        try (BufferedReader br = new BufferedReader(new FileReader(caminhoArquivo))) {
            String linhaInicios = br.readLine();
            if (linhaInicios == null) {
                throw new IOException("Arquivo " + caminhoArquivo
                        + " esta vazio ou nao tem a linha 1 (horarios de inicio)");
            }
            String linhaTerminos = br.readLine();
            if (linhaTerminos == null) {
                throw new IOException("Arquivo " + caminhoArquivo
                        + " nao tem a linha 2 (horarios de termino)");
            }

            int[] inicios = lerLinha(linhaInicios);
            int[] terminos = lerLinha(linhaTerminos);

            if (inicios.length != terminos.length) {
                throw new IOException("Arquivo " + caminhoArquivo
                        + " tem numero de inicios (" + inicios.length
                        + ") diferente de terminos (" + terminos.length + ")");
            }

            Aula[] aulas = new Aula[inicios.length];
            for (int i = 0; i < aulas.length; i++) {
                aulas[i] = new Aula(inicios[i], terminos[i]);
            }
            return aulas;
        }
    }

    private static int[] lerLinha(String linha) {
        StringTokenizer tok = new StringTokenizer(linha);
        int[] valores = new int[tok.countTokens()];
        for (int i = 0; i < valores.length; i++) {
            valores[i] = Integer.parseInt(tok.nextToken());
        }
        return valores;
    }
}
