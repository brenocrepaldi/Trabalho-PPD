package comunicacao;

/**
 * Classe que representa a resposta do Receptor
 * Contém a contagem de ocorrências encontradas
 */
public class Resposta extends Comunicado {
    private final Integer contagem;
    
    public Resposta(int contagem) {
        this.contagem = contagem;
    }
    
    public Integer getContagem() {
        return contagem;
    }
}
