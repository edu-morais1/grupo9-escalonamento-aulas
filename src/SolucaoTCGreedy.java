import java.util.Arrays;
import java.util.Comparator;

public class SolucaoTCGreedy {

    public static EscalonadorGuloso.Resultado resolver(Aula[] aulas) {
        Aula[] ordenadas = aulas.clone();
        Arrays.sort(ordenadas, Comparator.comparingInt(Aula::inicio));
        return EscalonadorGuloso.alocarSalas(ordenadas);
    }
}
