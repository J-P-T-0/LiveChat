package Respuestas;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class Respuesta {
    private String estado;
    private String descripcion;

    public Respuesta(@JsonProperty("estado") String estado, @JsonProperty("descipcion") String descripcion) {
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
