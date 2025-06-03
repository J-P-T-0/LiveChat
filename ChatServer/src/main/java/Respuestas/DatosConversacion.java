package Respuestas;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;

public class DatosConversacion {
    private int id;
    private String nombre;
    private boolean esGrupo;
    private ArrayList<String> participantes;

    public DatosConversacion(@JsonProperty("id") int id,
                             @JsonProperty("nombreConv") String nombre,
                             @JsonProperty("Grupo?:") boolean esGrupo,
                             @JsonProperty("participantes") ArrayList<String> participantes) {
        this.id = id;
        this.nombre = nombre;
        this.esGrupo = esGrupo;
        this.participantes = participantes;
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

    public ArrayList<String> getParticipantes() {
        return participantes;
    }
}
