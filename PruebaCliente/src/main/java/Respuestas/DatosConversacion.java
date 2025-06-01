package Respuestas;

import com.fasterxml.jackson.annotation.JsonProperty;

public class DatosConversacion {
    private int id;
    private String nombre;
    private boolean esGrupo;

    public DatosConversacion(@JsonProperty("id") int id,
                             @JsonProperty("nombreConv") String nombre,
                             @JsonProperty("Grupo?:") boolean esGrupo) {
        this.id = id;
        this.nombre = nombre;
        this.esGrupo = esGrupo;
    }

    public int getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public boolean isEsGrupo() {
        return esGrupo;
    }
}
