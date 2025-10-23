package cliente;

import comunicacao.*;
import java.io.*;
import java.net.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Programa Distribuidor (D) - Cliente
 * Gera vetor de números, divide em partes e distribui para os Receptores
 */
public class Distribuidor {
    private static final SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss.SSS");
    private static final AtomicInteger contagemTotal = new AtomicInteger(0);
    
    // IPs dos servidores Receptores (modifique conforme necessário)
    private static final String[] IPS_SERVIDORES = {
        "127.0.0.1",
        "127.0.0.1",
        "127.0.0.1",
    };
    
    // Portas dos servidores (para testes locais com múltiplas portas)
    private static final int[] PORTAS_SERVIDORES = {
        12345,
        12346,
        12347,
    };
    
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        
        log("=== Sistema Distribuído de Contagem ===");
        log("Processadores disponíveis: " + Runtime.getRuntime().availableProcessors());
        log("Número de servidores configurados: " + IPS_SERVIDORES.length);
        
        // Pergunta o tamanho do vetor
        System.out.print("\nDigite o tamanho do vetor (ex: 10000000 para 10 milhões): ");
        int tamanhoVetor = scanner.nextInt();
        
        // Pergunta se quer buscar um número existente ou inexistente
        System.out.print("Buscar número existente (1) ou inexistente/111 (2)? ");
        int opcao = scanner.nextInt();
        
        log("\n=== Gerando vetor de " + String.format("%,d", tamanhoVetor) + " elementos ===");
        long inicioGeracao = System.currentTimeMillis();
        
        // ✅ MUDANÇA: Usar byte[] em vez de int[]
        byte[] vetorCompleto = new byte[tamanhoVetor];
        Random random = new Random();
        
        // Gera números aleatórios entre -100 e 100
        for (int i = 0; i < tamanhoVetor; i++) {
            vetorCompleto[i] = (byte) (random.nextInt(201) - 100);
        }
        
        long fimGeracao = System.currentTimeMillis();
        log("Vetor gerado em " + (fimGeracao - inicioGeracao) + "ms");
        
        // Escolhe o número a procurar
        byte numeroProcurado;  // ✅ MUDANÇA: byte em vez de int
        if (opcao == 2) {
            numeroProcurado = 111; // Número que não existe no vetor
            log("Número a procurar: " + numeroProcurado + " (não existe no vetor)");
        } else {
            int posicaoAleatoria = random.nextInt(tamanhoVetor);
            numeroProcurado = vetorCompleto[posicaoAleatoria];
            log("Número a procurar: " + numeroProcurado + " (escolhido da posição " + posicaoAleatoria + ")");
        }
        
        // Pergunta se quer exibir o vetor
        System.out.print("\nDeseja exibir o vetor completo? (s/n): ");
        String exibir = scanner.next().toLowerCase();
        if (exibir.equals("s")) {
            exibirVetor(vetorCompleto);
        }
        
        // Inicia a contagem distribuída
        log("\n=== Iniciando contagem distribuída ===");
        long inicioContagem = System.currentTimeMillis();
        
        int numServidores = IPS_SERVIDORES.length;
        Thread[] threads = new Thread[numServidores];
        
        // Divide o vetor em partes
        int tamanhoParte = tamanhoVetor / numServidores;
        
        for (int i = 0; i < numServidores; i++) {
            final int indice = i;
            final int inicio = i * tamanhoParte;
            final int fim = (i == numServidores - 1) ? tamanhoVetor : (i + 1) * tamanhoParte;
            final int tamanhoParteAtual = fim - inicio;
            
            // ✅ MUDANÇA: Cria cópia da parte do vetor como byte[]
            byte[] parte = Arrays.copyOfRange(vetorCompleto, inicio, fim);
            
            log("Thread " + i + " -> Servidor " + IPS_SERVIDORES[i] + ":" + PORTAS_SERVIDORES[i] + 
                " (elementos " + inicio + " a " + (fim-1) + ", total: " + String.format("%,d", tamanhoParteAtual) + ")");
            
            threads[i] = new Thread(() -> {
                processarParte(IPS_SERVIDORES[indice], PORTAS_SERVIDORES[indice], parte, numeroProcurado, indice);
            });
            
            threads[i].start();
        }
        
        // Aguarda todas as threads terminarem
        for (int i = 0; i < numServidores; i++) {
            try {
                threads[i].join();
            } catch (InterruptedException e) {
                log("Erro ao aguardar thread " + i + ": " + e.getMessage());
                Thread.currentThread().interrupt();
            }
        }
        
        long fimContagem = System.currentTimeMillis();
        long tempoTotal = fimContagem - inicioContagem;
        
        // Exibe resultado final
        log("\n=== Resultado Final ===");
        log("Número procurado: " + numeroProcurado);
        log("Ocorrências encontradas: " + contagemTotal.get());
        log("Tempo de contagem distribuída: " + tempoTotal + "ms (" + String.format("%.2f", tempoTotal/1000.0) + "s)");
        
        scanner.close();
    }
    
    /**
     * Processa uma parte do vetor em um servidor específico
     */
    private static void processarParte(String ip, int porta, byte[] parte, byte procurado, int indiceThread) {
        try (
            Socket socket = new Socket(ip, porta);
            ObjectOutputStream transmissor = new ObjectOutputStream(socket.getOutputStream());
            ObjectInputStream receptor = new ObjectInputStream(socket.getInputStream())
        ) {
            transmissor.flush();
            log("Thread " + indiceThread + ": Conectado a " + ip + ":" + porta);
            
            // Cria e envia o pedido
            Pedido pedido = new Pedido(parte, procurado);
            log("Thread " + indiceThread + ": Enviando pedido para " + ip + ":" + porta);
            transmissor.writeObject(pedido);
            transmissor.flush();
            
            // Recebe a resposta
            Object objeto = receptor.readObject();
            
            if (objeto instanceof Resposta resposta) {
                int contagem = resposta.getContagem();
                contagemTotal.addAndGet(contagem);
                log("Thread " + indiceThread + ": Resposta de " + ip + ":" + porta + " -> " + contagem + " ocorrências");
            }
            
            // Envia comunicado de encerramento
            log("Thread " + indiceThread + ": Enviando ComunicadoEncerramento para " + ip + ":" + porta);
            transmissor.writeObject(new ComunicadoEncerramento());
            transmissor.flush();
            
        } catch (UnknownHostException e) {
            log("Thread " + indiceThread + ": Host desconhecido - " + ip + ":" + porta);
        } catch (IOException e) {
            log("Thread " + indiceThread + ": Erro de IO com " + ip + ":" + porta + " - " + e.getMessage());
        } catch (ClassNotFoundException e) {
            log("Thread " + indiceThread + ": Classe não encontrada - " + e.getMessage());
        }
    }
    
    /**
     * Exibe o vetor completo (útil para testes com vetores pequenos)
     */
    private static void exibirVetor(byte[] vetor) {
        log("\n=== Vetor Completo ===");
        System.out.print("[");
        for (int i = 0; i < vetor.length; i++) {
            System.out.print(vetor[i]);
            if (i < vetor.length - 1) {
                System.out.print(", ");
            }
            // Quebra linha a cada 20 elementos para melhor visualização
            if ((i + 1) % 20 == 0) {
                System.out.println();
            }
        }
        System.out.println("]");
        log("===================\n");
    }
    
    /**
     * Registra mensagem de log com timestamp
     */
    private static void log(String mensagem) {
        System.out.println("[D " + sdf.format(new Date()) + "] " + mensagem);
    }
}
