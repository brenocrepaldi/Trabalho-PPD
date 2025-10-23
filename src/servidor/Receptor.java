package servidor;

import comunicacao.*;
import java.io.*;
import java.net.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * Programa Receptor (R) - Servidor
 * Recebe pedidos de contagem, processa-os em paralelo e retorna respostas
 */
public class Receptor {
    private static final SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss.SSS");
    
    public static void main(String[] args) {
        // === DEBUG - REMOVER DEPOIS ===
        System.out.println("\n=== DEBUG DE ARGUMENTOS ===");
        System.out.println("Número de argumentos recebidos: " + args.length);
        for (int i = 0; i < args.length; i++) {
            System.out.println("  args[" + i + "] = '" + args[i] + "'");
        }
        System.out.println("===========================\n");
        // === FIM DO DEBUG ===
        
            // Se nenhum argumento for passado, usa três portas padrão para simulação local
            int[] portas;
            if (args.length == 0) {
                portas = new int[] {12345, 12346, 12347};
                System.out.println("✓ Nenhum argumento fornecido. Usando portas padrão: 12345, 12346, 12347");
            } else {
                // Converte argumentos em inteiros (portas)
                portas = new int[args.length];
                for (int i = 0; i < args.length; i++) {
                    try {
                        portas[i] = Integer.parseInt(args[i]);
                    } catch (NumberFormatException e) {
                        System.err.println("✗ Porta inválida '" + args[i] + "'. Ignorando.");
                        portas[i] = -1;
                    }
                }
            }

            log("Receptor iniciado. Aguardando conexões nas portas: " + Arrays.toString(portas));
        log("Processadores disponíveis: " + Runtime.getRuntime().availableProcessors());
        
            // Para cada porta, cria um ServerSocket e uma thread que aceita conexões
            for (int porta : portas) {
                if (porta <= 0) continue; // pula entradas inválidas

                Thread listener = new Thread(() -> {
                    try (ServerSocket serverSocket = new ServerSocket(porta)) {
                        log("Aguardando conexões na porta " + porta);
                        while (true) {
                            try {
                                Socket socket = serverSocket.accept();
                                String clienteIP = socket.getInetAddress().getHostAddress();
                                log("[porta:" + porta + "] Conexão aceita de: " + clienteIP);
                                // Processa a conexão em outra thread para não bloquear o accept
                                new Thread(() -> processarConexao(socket, clienteIP)).start();
                            } catch (IOException e) {
                                log("[porta:" + porta + "] Erro ao aceitar conexão: " + e.getMessage());
                            }
                        }
                    } catch (IOException e) {
                        log("[porta:" + porta + "] Erro fatal ao criar ServerSocket: " + e.getMessage());
                        e.printStackTrace();
                    }
                });
                listener.setName("Listener-Porta-" + porta);
                listener.setDaemon(true);
                listener.start();
            }

            // Mantém o processo principal vivo
            try {
                Thread.currentThread().join();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
    }
    
    /**
     * Processa uma conexão com um cliente
     */
    private static void processarConexao(Socket socket, String clienteIP) {
        try (
            ObjectOutputStream transmissor = new ObjectOutputStream(socket.getOutputStream());
            ObjectInputStream receptor = new ObjectInputStream(socket.getInputStream())
        ) {
            transmissor.flush();
            log("Streams criados para cliente " + clienteIP);
            
            boolean continuar = true;
            int pedidosProcessados = 0;
            
            while (continuar) {
                try {
                    // Lê o objeto enviado pelo cliente
                    Object objeto = receptor.readObject();
                    
                    if (objeto instanceof Pedido) {
                        pedidosProcessados++;
                        log("[" + clienteIP + "] Pedido #" + pedidosProcessados + " recebido");
                        
                        Pedido pedido = (Pedido) objeto;
                        long inicio = System.currentTimeMillis();
                        
                        // Realiza a contagem EM PARALELO
                        int resultado = contarEmParalelo(pedido, clienteIP, pedidosProcessados);
                        
                        long fim = System.currentTimeMillis();
                        long tempoDecorrido = fim - inicio;
                        
                        log("[" + clienteIP + "] Pedido #" + pedidosProcessados + 
                            " processado em " + tempoDecorrido + "ms. Resultado: " + resultado);
                        
                        // Envia a resposta
                        Resposta resposta = new Resposta(resultado);
                        transmissor.writeObject(resposta);
                        transmissor.flush();
                        
                    } else if (objeto instanceof ComunicadoEncerramento) {
                        log("[" + clienteIP + "] ComunicadoEncerramento recebido");
                        log("[" + clienteIP + "] Total de pedidos processados: " + pedidosProcessados);
                        continuar = false;
                        
                    } else {
                        log("[" + clienteIP + "] Objeto desconhecido recebido: " + objeto.getClass().getName());
                    }
                    
                } catch (EOFException e) {
                    log("[" + clienteIP + "] Conexão encerrada pelo cliente");
                    continuar = false;
                } catch (ClassNotFoundException e) {
                    log("[" + clienteIP + "] Erro: Classe não encontrada - " + e.getMessage());
                }
            }
            
            log("[" + clienteIP + "] Encerrando conexão");
            
        } catch (IOException e) {
            log("[" + clienteIP + "] Erro na comunicação: " + e.getMessage());
        }
    }
    
    /**
     * Realiza a contagem em paralelo usando múltiplas threads
     */
    private static int contarEmParalelo(Pedido pedido, String clienteIP, int numeroPedido) {
        byte[] numeros = pedido.getNumeros();
        byte procurado = pedido.getProcurado();
        int tamanhoVetor = numeros.length;
        
        // Determina quantas threads usar (no máximo o número de processadores)
        int numProcessadores = Runtime.getRuntime().availableProcessors();
        int numThreads = Math.min(numProcessadores, tamanhoVetor);
        
        log("[" + clienteIP + "] Pedido #" + numeroPedido + 
            " - Processando vetor de " + tamanhoVetor + " elementos usando " + numThreads + " threads");
        
        // Calcula o tamanho de cada parte
        int tamanhoParte = tamanhoVetor / numThreads;
        int resto = tamanhoVetor % numThreads;
        
        // Lista para armazenar as threads e os contadores
        List<ThreadContadora> threads = new ArrayList<>();
        
        // Cria e inicia as threads
        int inicio = 0;
        for (int i = 0; i < numThreads; i++) {
            // A última thread pega os elementos restantes
            int tamanho = tamanhoParte + (i == numThreads - 1 ? resto : 0);
            int fim = inicio + tamanho;
            
            ThreadContadora thread = new ThreadContadora(numeros, procurado, inicio, fim, i + 1);
            threads.add(thread);
            thread.start();
            
            log("[" + clienteIP + "] Thread #" + (i + 1) + " criada - processando índices [" + 
                inicio + " a " + (fim - 1) + "]");
            
            inicio = fim;
        }
        
        // Aguarda todas as threads terminarem e soma os resultados
        int contagemTotal = 0;
        for (ThreadContadora thread : threads) {
            try {
                thread.join(); // Espera a thread terminar
                contagemTotal += thread.getContagem();
                log("[" + clienteIP + "] Thread #" + thread.getThreadIdCustom() + 
                    " concluída - contagem parcial: " + thread.getContagem());
            } catch (InterruptedException e) {
                log("[" + clienteIP + "] Thread interrompida: " + e.getMessage());
            }
        }
        
        return contagemTotal;
    }
    
    /**
     * Thread que realiza a contagem em uma parte do vetor
     */
    private static class ThreadContadora extends Thread {
        private final byte[] numeros;
        private final byte procurado;
        private final int inicio;
        private final int fim;
        private final int threadId;
        private int contagem;
        
        public ThreadContadora(byte[] numeros, byte procurado, int inicio, int fim, int threadId) {
            this.numeros = numeros;
            this.procurado = procurado;
            this.inicio = inicio;
            this.fim = fim;
            this.threadId = threadId;
            this.contagem = 0;
        }
        
        @Override
        public void run() {
            // Log de início da thread
            log("    [Thread #" + threadId + "] INICIOU - processando índices [" + inicio + " a " + (fim - 1) + "]");
            
            long inicioTempo = System.currentTimeMillis();
            
            // Realiza a contagem na parte designada do vetor
            for (int i = inicio; i < fim; i++) {
                if (numeros[i] == procurado) {
                    contagem++;
                }
            }
            
            long fimTempo = System.currentTimeMillis();
            long duracao = fimTempo - inicioTempo;
            
            // Log de conclusão da thread
            log("    [Thread #" + threadId + "] CONCLUÍDA - encontrou " + contagem + 
                " ocorrências em " + duracao + "ms");
        }
        
        public int getContagem() {
            return contagem;
        }
        
        public int getThreadIdCustom() {
            return threadId;
        }
    }
    
    /**
     * Registra mensagem de log com timestamp
     */
    private static void log(String mensagem) {
        System.out.println("[R " + sdf.format(new Date()) + "] " + mensagem);
    }
}
