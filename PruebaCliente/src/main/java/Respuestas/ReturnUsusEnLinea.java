package Respuestas;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;

public class ReturnUsusEnLinea extends Respuesta{
    private Map<String, String> usuariosEnLinea;

    public ReturnUsusEnLinea(@JsonProperty("usuariosEnLinea") Map<String, String> usuariosEnLinea) {
        super("RETURN_USUS_EN_LINEA");
        this.usuariosEnLinea = usuariosEnLinea;
    }

    public Map<String, String> getUsuariosEnLinea() {
        return usuariosEnLinea;
    }
}
