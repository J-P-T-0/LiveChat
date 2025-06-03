package Respuestas;

import com.fasterxml.jackson.annotation.JsonProperty;

public class DatosMensajes {
    private String nombre;
    private String mensaje;
    private String fecha;

    public DatosMensajes(@JsonProperty("nombre") String nombre,
                         @JsonProperty("mensaje") String mensaje,
                         @JsonProperty("fecha") String fecha) {
        this.nombre = nombre;
        this.mensaje = mensaje;
        this.fecha = fecha;
    }

    public String getNombre() {
        return nombre;
    }

    public String getMensaje() {
        return mensaje;
    }

    public String getFecha() {
        return fecha;
    }
}
