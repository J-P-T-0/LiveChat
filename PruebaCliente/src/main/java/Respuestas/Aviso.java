package Respuestas;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Aviso extends Respuesta {
    private String estado;
    private String descripcion;

    public Aviso(@JsonProperty("estado") String estado, @JsonProperty("descripcion") String descripcion) {
        super("AVISO");
        this.estado = estado;
        this.descripcion = descripcion;
    }

    public String getEstado() {
        return estado;
    }

    public String getDescripcion() {
        return descripcion;
    }
}
