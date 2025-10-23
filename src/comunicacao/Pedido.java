package comunicacao;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Classe que representa um pedido de contagem
 * Contém o vetor de números e o número a ser procurado
 */
public class Pedido extends Comunicado {
    private static final long serialVersionUID = 1L;
    
    private final int[] numeros;
    private final int procurado;
    
    public Pedido(int[] numeros, int procurado) {
        this.numeros = numeros;
        this.procurado = procurado;
    }
    
    public int[] getNumeros() {
        return numeros;
    }
    
    public int getProcurado() {
        return procurado;
    }
    
    /**
     * Conta quantas vezes o número procurado aparece no vetor
     * Usa threads para paralelizar a contagem
     * @return número de ocorrências
     */
    public int contar() {
        int numProcessadores = Runtime.getRuntime().availableProcessors();
        int tamanhoVetor = numeros.length;
        
        // Se o vetor for pequeno, conta sequencialmente
        if (tamanhoVetor < 1000) {
            return contarSequencial(0, tamanhoVetor);
        }
        
        // Divide o trabalho entre as threads
        int tamanhoBloco = tamanhoVetor / numProcessadores;
        AtomicInteger contagemTotal = new AtomicInteger(0);
        ExecutorService executor = Executors.newFixedThreadPool(numProcessadores);
        CountDownLatch latch = new CountDownLatch(numProcessadores);
        
        for (int i = 0; i < numProcessadores; i++) {
            final int inicio = i * tamanhoBloco;
            final int fim = (i == numProcessadores - 1) ? tamanhoVetor : (i + 1) * tamanhoBloco;
            
            executor.submit(() -> {
                try {
                    int contagemParcial = contarSequencial(inicio, fim);
                    contagemTotal.addAndGet(contagemParcial);
                } finally {
                    latch.countDown();
                }
            });
        }
        
        try {
            latch.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.err.println("Erro ao aguardar threads: " + e.getMessage());
        } finally {
            executor.shutdown();
        }
        
        return contagemTotal.get();
    }
    
    /**
     * Conta sequencialmente em um intervalo do vetor
     */
    private int contarSequencial(int inicio, int fim) {
        int contagem = 0;
        for (int i = inicio; i < fim; i++) {
            if (numeros[i] == procurado) {
                contagem++;
            }
        }
        return contagem;
    }
}
