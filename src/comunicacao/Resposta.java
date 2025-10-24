package comunicacao;

public class Resposta extends Comunicado {
    private final Integer contagem;
    
    public Resposta(int contagem) {
        this.contagem = contagem;
    }
    
    public Integer getContagem() {
        return contagem;
    }
}
