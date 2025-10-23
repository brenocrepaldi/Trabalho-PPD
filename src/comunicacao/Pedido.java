package comunicacao;

import java.io.Serializable;

/**
 * Classe Pedido - contém o vetor de números e o valor a ser procurado
 */
public class Pedido extends Comunicado implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private byte[] numeros;
    private byte procurado;
    
    /**
     * Construtor
     */
    public Pedido(byte[] numeros, byte procurado) {
        this.numeros = numeros;
        this.procurado = procurado;
    }
    
    /**
     * Método contar - percorre o vetor e conta ocorrências
     * (mantido para compatibilidade, mas não será usado com paralelismo)
     */
    public int contar() {
        int contagem = 0;
        for (byte numero : numeros) {
            if (numero == procurado) {
                contagem++;
            }
        }
        return contagem;
    }
    
    /**
     * Getter para o vetor de números
     * NECESSÁRIO para o processamento paralelo no Receptor
     */
    public byte[] getNumeros() {
        return numeros;
    }
    
    /**
     * Getter para o número procurado
     * NECESSÁRIO para o processamento paralelo no Receptor
     */
    public byte getProcurado() {
        return procurado;
    }
}