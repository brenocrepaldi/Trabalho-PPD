package cliente;


import comunicacao.*;
import java.io.*;
import java.net.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class Distribuidor {
    private static final SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss.SSS");
    private static final AtomicInteger contagemTotal = new AtomicInteger(0);
    
    private static final String[] IPS_SERVIDORES = {
        "127.0.0.1",
        "127.0.0.1",
        "127.0.0.1",
    };
    
    private static final int[] PORTAS_SERVIDORES = {
        12345,
        12346,
        12347,
    };
    
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        
        log("=== Sistema Distribuído de Contagem ===");
        log("Processadores: " + Runtime.getRuntime().availableProcessors());
        log("Servidores: " + IPS_SERVIDORES.length);
        
        System.out.print("\nTamanho do vetor: ");
        int tamanhoVetor = scanner.nextInt();
        
        System.out.print("Buscar número existente (1) ou inexistente/111 (2)? ");
        int opcao = scanner.nextInt();
        
        log("Gerando vetor de " + String.format("%,d", tamanhoVetor) + " elementos...");
        long inicioGeracao = System.currentTimeMillis();
        
        byte[] vetorCompleto = new byte[tamanhoVetor];
        Random random = new Random();
        
        for (int i = 0; i < tamanhoVetor; i++) {
            vetorCompleto[i] = (byte) (random.nextInt(201) - 100);
        }
        
        long fimGeracao = System.currentTimeMillis();
        log("Vetor gerado em " + (fimGeracao - inicioGeracao) + "ms");
        
        byte numeroProcurado;
        if (opcao == 2) {
            numeroProcurado = 111;
            log("Procurando: " + numeroProcurado + " (inexistente)");
        } else {
            int posicaoAleatoria = random.nextInt(tamanhoVetor);
            numeroProcurado = vetorCompleto[posicaoAleatoria];
            log("Procurando: " + numeroProcurado);
        }
        
        System.out.print("\nExibir vetor completo? (s/n): ");
        String exibir = scanner.next().toLowerCase();
        if (exibir.equals("s")) {
            exibirVetor(vetorCompleto);
        }
        
        log("Iniciando contagem distribuída...");
        long inicioContagem = System.currentTimeMillis();
        
        int numServidores = IPS_SERVIDORES.length;
        Thread[] threads = new Thread[numServidores];
        int tamanhoParte = tamanhoVetor / numServidores;
        
        for (int i = 0; i < numServidores; i++) {
            final int indice = i;
            final int inicio = i * tamanhoParte;
            final int fim = (i == numServidores - 1) ? tamanhoVetor : (i + 1) * tamanhoParte;
            
            byte[] parte = Arrays.copyOfRange(vetorCompleto, inicio, fim);
            
            threads[i] = new Thread(() -> {
                processarParte(IPS_SERVIDORES[indice], PORTAS_SERVIDORES[indice], parte, numeroProcurado, indice);
            });
            
            threads[i].start();
        }
        
        for (int i = 0; i < numServidores; i++) {
            try {
                threads[i].join();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        
        long tempoTotal = System.currentTimeMillis() - inicioContagem;
        
        log("\n=== Resultado ===");
        log("Número: " + numeroProcurado);
        log("Ocorrências: " + contagemTotal.get());
        log("Tempo: " + tempoTotal + "ms");
        
        scanner.close();
    }
    
    private static void processarParte(String ip, int porta, byte[] parte, byte procurado, int indiceThread) {
        try (
            Socket socket = new Socket(ip, porta);
            ObjectOutputStream transmissor = new ObjectOutputStream(socket.getOutputStream());
            ObjectInputStream receptor = new ObjectInputStream(socket.getInputStream())
        ) {
            transmissor.flush();
            log("T" + indiceThread + ": Conectado a " + ip + ":" + porta);
            
            transmissor.writeObject(new Pedido(parte, procurado));
            transmissor.flush();
            
            Object objeto = receptor.readObject();
            
            if (objeto instanceof Resposta resposta) {
                int contagem = resposta.getContagem();
                contagemTotal.addAndGet(contagem);
                log("T" + indiceThread + ": " + contagem + " ocorrências");
            }
            
            transmissor.writeObject(new ComunicadoEncerramento());
            transmissor.flush();
            
        } catch (UnknownHostException e) {
            log("T" + indiceThread + ": Host desconhecido");
        } catch (IOException e) {
            log("T" + indiceThread + ": Erro IO - " + e.getMessage());
        } catch (ClassNotFoundException e) {
            log("T" + indiceThread + ": Erro - " + e.getMessage());
        }
    }
    
    private static void exibirVetor(byte[] vetor) {
        System.out.println("\n=== Vetor ===");
        System.out.print("[");
        for (int i = 0; i < vetor.length; i++) {
            System.out.print(vetor[i]);
            if (i < vetor.length - 1) {
                System.out.print(", ");
            }
            if ((i + 1) % 20 == 0) {
                System.out.println();
            }
        }
        System.out.println("]\n");
    }
    
    private static void log(String mensagem) {
        System.out.println("[D " + sdf.format(new Date()) + "] " + mensagem);
    }
}
