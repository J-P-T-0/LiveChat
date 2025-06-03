package Respuestas;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ReturnEstadoMensaje extends Respuesta {
    private String estado;
    private String fechaEnvio;
    private String fechaLectura;

    public ReturnEstadoMensaje(@JsonProperty("estado") String estado,
                            @JsonProperty("fechaEnvío") String fechaEnvio,
                            @JsonProperty("fechaLectura") String fechaLectura) {
        super("RETURN_ESTADO_MENSAJE");
        this.estado = estado;
        this.fechaEnvio = fechaEnvio;
        this.fechaLectura = fechaLectura;
    }

    public String getEstado() {
        return estado;
    }

    public String getFechaEnvio() {
        return fechaEnvio;
    }

    public String getFechaLectura() {
        return fechaLectura;
    }
}
