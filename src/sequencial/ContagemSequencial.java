package sequencial;

import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Programa de contagem sequencial (sem paralelismo ou distribuição)
 * Usado para comparação de desempenho
 */
public class ContagemSequencial {
    private static final SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss.SSS");
    
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        
        log("=== Contagem Sequencial (Sem Paralelismo) ===");
        
        // Pergunta o tamanho do vetor
        System.out.print("\nDigite o tamanho do vetor (ex: 10000000 para 10 milhões): ");
        int tamanhoVetor = scanner.nextInt();
        
        // Pergunta se quer buscar um número existente ou inexistente
        System.out.print("Buscar número existente (1) ou inexistente/111 (2)? ");
        int opcao = scanner.nextInt();
        
        log("\n=== Gerando vetor de " + String.format("%,d", tamanhoVetor) + " elementos ===");
        long inicioGeracao = System.currentTimeMillis();
        
        int[] vetor = new int[tamanhoVetor];
        Random random = new Random();
        
        // Gera números aleatórios entre -100 e 100
        for (int i = 0; i < tamanhoVetor; i++) {
            vetor[i] = random.nextInt(201) - 100;
        }
        
        long fimGeracao = System.currentTimeMillis();
        log("Vetor gerado em " + (fimGeracao - inicioGeracao) + "ms");
        
        // Escolhe o número a procurar
        int numeroProcurado;
        if (opcao == 2) {
            numeroProcurado = 111; // Número que não existe no vetor
            log("Número a procurar: " + numeroProcurado + " (não existe no vetor)");
        } else {
            int posicaoAleatoria = random.nextInt(tamanhoVetor);
            numeroProcurado = vetor[posicaoAleatoria];
            log("Número a procurar: " + numeroProcurado + " (escolhido da posição " + posicaoAleatoria + ")");
        }
        
        // Inicia a contagem sequencial
        log("\n=== Iniciando contagem sequencial ===");
        long inicioContagem = System.currentTimeMillis();
        
        int contagem = 0;
        for (int i = 0; i < tamanhoVetor; i++) {
            if (vetor[i] == numeroProcurado) {
                contagem++;
            }
        }
        
        long fimContagem = System.currentTimeMillis();
        long tempoTotal = fimContagem - inicioContagem;
        
        // Exibe resultado final
        log("\n=== Resultado Final ===");
        log("Número procurado: " + numeroProcurado);
        log("Ocorrências encontradas: " + contagem);
        log("Tempo de contagem sequencial: " + tempoTotal + "ms (" + String.format("%.2f", tempoTotal/1000.0) + "s)");
        
        scanner.close();
    }
    
    /**
     * Registra mensagem de log com timestamp
     */
    private static void log(String mensagem) {
        System.out.println("[SEQ " + sdf.format(new Date()) + "] " + mensagem);
    }
}
