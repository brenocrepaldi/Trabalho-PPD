package teste;

import comunicacao.*;
import java.io.*;
import java.net.*;

/**
 * Teste simples para validar a comunicação entre cliente e servidor
 */
public class TesteSimples {
    public static void main(String[] args) {
        System.out.println("=== Teste Simples de Comunicação ===\n");
        
        try {
            // Conecta ao servidor
            System.out.println("Conectando ao servidor localhost:12345...");
            Socket socket = new Socket("localhost", 12345);
            System.out.println("✓ Conectado com sucesso!");
            
            // Cria streams
            ObjectOutputStream transmissor = new ObjectOutputStream(socket.getOutputStream());
            ObjectInputStream receptor = new ObjectInputStream(socket.getInputStream());
            transmissor.flush();
            System.out.println("✓ Streams criados!");
            
            // Cria vetor pequeno de teste
            int[] vetorTeste = {5, 10, 5, 20, 5, 30, 5};
            int numeroProcurado = 5;
            
            System.out.println("\nEnviando pedido:");
            System.out.println("  Vetor: [5, 10, 5, 20, 5, 30, 5]");
            System.out.println("  Número procurado: " + numeroProcurado);
            System.out.println("  Ocorrências esperadas: 4");
            
            // Envia pedido
            Pedido pedido = new Pedido(vetorTeste, numeroProcurado);
            transmissor.writeObject(pedido);
            transmissor.flush();
            System.out.println("✓ Pedido enviado!");
            
            // Recebe resposta
            System.out.println("\nAguardando resposta...");
            Object objeto = receptor.readObject();
            
            if (objeto instanceof Resposta) {
                Resposta resposta = (Resposta) objeto;
                int contagem = resposta.getContagem();
                
                System.out.println("✓ Resposta recebida!");
                System.out.println("  Ocorrências encontradas: " + contagem);
                
                if (contagem == 4) {
                    System.out.println("\n✅ TESTE PASSOU! Contagem correta.");
                } else {
                    System.out.println("\n❌ TESTE FALHOU! Contagem incorreta.");
                }
            }
            
            // Envia comunicado de encerramento
            System.out.println("\nEnviando ComunicadoEncerramento...");
            transmissor.writeObject(new ComunicadoEncerramento());
            transmissor.flush();
            System.out.println("✓ ComunicadoEncerramento enviado!");
            
            // Fecha conexões
            transmissor.close();
            receptor.close();
            socket.close();
            System.out.println("✓ Conexões fechadas!");
            
            System.out.println("\n=== Teste concluído com sucesso! ===");
            
        } catch (Exception e) {
            System.err.println("❌ Erro no teste: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
