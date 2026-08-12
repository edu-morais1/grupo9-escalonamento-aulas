public class SolucaoTCGreedy {

    public static EscalonadorGuloso.Resultado resolver(Aula[] aulas) {
        Aula[] ordenadas = aulas.clone();
        mergeSort(ordenadas, 0, ordenadas.length - 1);
        return EscalonadorGuloso.alocarSalas(ordenadas);
    }

    private static void mergeSort(Aula[] vet, int comeco, int fim) {
        if (comeco >= fim) {
            return;
        }
        int meio = (comeco + fim) / 2;
        mergeSort(vet, comeco, meio);
        mergeSort(vet, meio + 1, fim);
        merge(vet, comeco, meio, fim);
    }

    private static void merge(Aula[] vet, int comeco, int meio, int fim) {
        int tamEsquerda = meio - comeco + 1;
        int tamDireita = fim - meio;
        Aula[] esquerda = new Aula[tamEsquerda];
        Aula[] direita = new Aula[tamDireita];

        for (int i = 0; i < tamEsquerda; i++) {
            esquerda[i] = vet[comeco + i];
        }
        for (int j = 0; j < tamDireita; j++) {
            direita[j] = vet[meio + 1 + j];
        }

        int i = 0, j = 0, k = comeco;
        while (i < tamEsquerda && j < tamDireita) {
            if (esquerda[i].inicio() <= direita[j].inicio()) {
                vet[k++] = esquerda[i++];
            } else {
                vet[k++] = direita[j++];
            }
        }
        while (i < tamEsquerda) {
            vet[k++] = esquerda[i++];
        }
        while (j < tamDireita) {
            vet[k++] = direita[j++];
        }
    }
}
