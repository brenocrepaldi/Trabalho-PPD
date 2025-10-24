package comunicacao;

public class Pedido extends Comunicado {
    private static final long serialVersionUID = 1L;
    
    private final byte[] numeros;
    private final byte procurado;
    
    public Pedido(byte[] numeros, byte procurado) {
        this.numeros = numeros;
        this.procurado = procurado;
    }
    
    public byte[] getNumeros() {
        return numeros;
    }
    
    public byte getProcurado() {
        return procurado;
    }
}