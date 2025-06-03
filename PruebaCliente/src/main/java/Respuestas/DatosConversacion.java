package Respuestas;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;

public class DatosConversacion {
    private int id;
    private String nombre;
    private boolean esGrupo;
    private ArrayList<String> participantes;
    private ArrayList<String> telefonos;

    public DatosConversacion(@JsonProperty("id") int id,
                             @JsonProperty("nombreConv") String nombre,
                             @JsonProperty("Grupo?:") boolean esGrupo,
                             @JsonProperty("participantes") ArrayList<String> participantes,
                             @JsonProperty("telefonos") ArrayList<String> telefonos) {
        this.id = id;
        this.nombre = nombre;
        this.esGrupo = esGrupo;
        this.participantes = participantes;
        this.telefonos = telefonos;
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
    public ArrayList<String> getTelefonos() {
        return telefonos;
    }
}
