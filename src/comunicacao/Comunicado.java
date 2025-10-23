package comunicacao;

import java.io.Serializable;

/**
 * Classe base para comunicação entre Distribuidor e Receptor
 * Serve como superclasse para Pedido, Resposta e ComunicadoEncerramento
 */
public class Comunicado implements Serializable {
    private static final long serialVersionUID = 1L;
}
