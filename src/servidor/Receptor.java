package servidor;

import comunicacao.*;
import java.io.*;
import java.net.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class Receptor {
    private static final SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss.SSS");
    
    public static void main(String[] args) {
        int[] portas;
        if (args.length == 0) {
            portas = new int[] {12345, 12346, 12347};
        } else {
            portas = new int[args.length];
            for (int i = 0; i < args.length; i++) {
                try {
                    portas[i] = Integer.parseInt(args[i]);
                } catch (NumberFormatException e) {
                    System.err.println("Porta inválida '" + args[i] + "'. Ignorando.");
                    portas[i] = -1;
                }
            }
        }

        log("Receptor iniciado nas portas: " + Arrays.toString(portas));
        log("Processadores: " + Runtime.getRuntime().availableProcessors());
        
        for (int porta : portas) {
            if (porta <= 0) continue;

            Thread listener = new Thread(() -> {
                try (ServerSocket serverSocket = new ServerSocket(porta)) {
                    log("Escutando porta " + porta);
                    while (true) {
                        try {
                            Socket socket = serverSocket.accept();
                            String clienteIP = socket.getInetAddress().getHostAddress();
                            log("[" + porta + "] Conexão: " + clienteIP);
                            new Thread(() -> processarConexao(socket, clienteIP)).start();
                        } catch (IOException e) {
                            log("[" + porta + "] Erro: " + e.getMessage());
                        }
                    }
                } catch (IOException e) {
                    log("[" + porta + "] Erro fatal: " + e.getMessage());
                }
            });
            listener.setName("Listener-" + porta);
            listener.setDaemon(true);
            listener.start();
        }

        try {
            Thread.currentThread().join();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
    
    private static void processarConexao(Socket socket, String clienteIP) {
        try (
            ObjectOutputStream transmissor = new ObjectOutputStream(socket.getOutputStream());
            ObjectInputStream receptor = new ObjectInputStream(socket.getInputStream())
        ) {
            transmissor.flush();
            
            boolean continuar = true;
            int pedidosProcessados = 0;
            
            while (continuar) {
                try {
                    Object objeto = receptor.readObject();
                    
                    if (objeto instanceof Pedido pedido) {
                        pedidosProcessados++;
                        long inicio = System.currentTimeMillis();
                        int resultado = contarEmParalelo(pedido);
                        long tempoDecorrido = System.currentTimeMillis() - inicio;
                        
                        log("[" + clienteIP + "] Pedido #" + pedidosProcessados + " -> " + 
                            resultado + " ocorrências (" + tempoDecorrido + "ms)");
                        
                        transmissor.writeObject(new Resposta(resultado));
                        transmissor.flush();
                        
                    } else if (objeto instanceof ComunicadoEncerramento) {
                        log("[" + clienteIP + "] Encerrando (" + pedidosProcessados + " pedidos)");
                        continuar = false;
                    }
                    
                } catch (EOFException e) {
                    continuar = false;
                } catch (ClassNotFoundException e) {
                    log("[" + clienteIP + "] Erro: " + e.getMessage());
                }
            }
            
        } catch (IOException e) {
            log("[" + clienteIP + "] Erro: " + e.getMessage());
        }
    }
    
    private static int contarEmParalelo(Pedido pedido) {
        byte[] numeros = pedido.getNumeros();
        byte procurado = pedido.getProcurado();
        int tamanhoVetor = numeros.length;
        
        int numThreads = Math.min(Runtime.getRuntime().availableProcessors(), tamanhoVetor);
        int tamanhoParte = tamanhoVetor / numThreads;
        int resto = tamanhoVetor % numThreads;
        
        List<ThreadContadora> threads = new ArrayList<>();
        int inicio = 0;
        
        for (int i = 0; i < numThreads; i++) {
            int tamanho = tamanhoParte + (i == numThreads - 1 ? resto : 0);
            int fim = inicio + tamanho;
            
            ThreadContadora thread = new ThreadContadora(numeros, procurado, inicio, fim, i + 1);
            threads.add(thread);
            thread.start();
            
            inicio = fim;
        }
        
        int contagemTotal = 0;
        for (ThreadContadora thread : threads) {
            try {
                thread.join();
                contagemTotal += thread.getContagem();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        
        return contagemTotal;
    }
    
    private static class ThreadContadora extends Thread {
        private final byte[] numeros;
        private final byte procurado;
        private final int inicio;
        private final int fim;
        private int contagem;
        
        public ThreadContadora(byte[] numeros, byte procurado, int inicio, int fim, int threadId) {
            this.numeros = numeros;
            this.procurado = procurado;
            this.inicio = inicio;
            this.fim = fim;
            this.contagem = 0;
        }
        
        @Override
        public void run() {
            for (int i = inicio; i < fim; i++) {
                if (numeros[i] == procurado) {
                    contagem++;
                }
            }
        }
        
        public int getContagem() {
            return contagem;
        }
    }
    
    private static void log(String mensagem) {
        System.out.println("[R " + sdf.format(new Date()) + "] " + mensagem);
    }
}
