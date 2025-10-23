package util;

/**
 * Programa para estimar o maior tamanho possível de vetor em Java
 * Baseado no código fornecido no enunciado
 */
public class MaiorVetorAproximado {
    public static void main(String[] args) {
        System.out.println("Estimando o maior tamanho possível de vetor em Java...");
        long inicio = System.currentTimeMillis();

        int tamanho = 1_000_000;  // começa com 1 milhão
        int ultimoBemSucedido = 0;

        while (true) {
            try {
                int[] vetor = new int[tamanho];
                ultimoBemSucedido = tamanho;
                vetor = null;  // libera
                System.gc();

                // aumenta o tamanho em 50% para a próxima tentativa
                if (tamanho > Integer.MAX_VALUE / 3 * 2) break;

                tamanho /= 2;
                tamanho *= 3;

                System.out.printf("Alocado com sucesso: %,d elementos%n", ultimoBemSucedido);
            } catch (OutOfMemoryError e) {
                System.out.printf("Falhou em %,d elementos%n", tamanho);
                break;
            }
        }

        long fim = System.currentTimeMillis();
        System.out.println("\nMaior vetor que coube (aproximadamente): " +
                String.format("%,d", ultimoBemSucedido));
        System.out.printf("Memória estimada: %.2f MB%n",
                ultimoBemSucedido * 4.0 / (1024 * 1024));  // 4 bytes por int
        System.out.printf("Tempo total: %.2f segundos%n", (fim - inicio) / 1000.0);
        
        System.out.println("\n=== INSTRUÇÕES ===");
        System.out.println("Para executar com mais memória, use:");
        System.out.println("java -Xmx8G util.MaiorVetorAproximado");
    }
}
