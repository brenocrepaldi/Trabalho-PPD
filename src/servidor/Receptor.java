package servidor;

import comunicacao.*;
import java.io.*;
import java.net.*;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Programa Receptor (R) - Servidor
 * Recebe pedidos de contagem, processa-os em paralelo e retorna respostas
 */
public class Receptor {
    private static final int PORTA = 12345;
    private static final SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss.SSS");
    
    public static void main(String[] args) {
        log("Receptor iniciado. Aguardando conexões na porta " + PORTA);
        log("Processadores disponíveis: " + Runtime.getRuntime().availableProcessors());
        
        try (ServerSocket serverSocket = new ServerSocket(PORTA)) {
            while (true) {
                try {
                    log("Aguardando nova conexão...");
                    Socket socket = serverSocket.accept();
                    String clienteIP = socket.getInetAddress().getHostAddress();
                    log("Conexão aceita de: " + clienteIP);
                    
                    // Processa a conexão
                    processarConexao(socket, clienteIP);
                    
                } catch (IOException e) {
                    log("Erro ao aceitar conexão: " + e.getMessage());
                }
            }
        } catch (IOException e) {
            log("Erro fatal ao criar ServerSocket: " + e.getMessage());
            e.printStackTrace();
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
                        
                        // Realiza a contagem
                        int resultado = pedido.contar();
                        
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
     * Registra mensagem de log com timestamp
     */
    private static void log(String mensagem) {
        System.out.println("[R " + sdf.format(new Date()) + "] " + mensagem);
    }
}
