package Requests;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;

public class CrearGrupo extends Request{
    private String nombreGrupo;
    private ArrayList<String> telefonos;

    public CrearGrupo(@JsonProperty("nombreGrupo") String nombreGrupo,
                      @JsonProperty("telefonos") ArrayList<String> telefonos) {
        super("CREAR_GRUPO");
        this.nombreGrupo = nombreGrupo;
        this.telefonos = telefonos;
    }

    public String getNombreGrupo() {
        return nombreGrupo;
    }

    public ArrayList<String> getTelefonos() {
        return telefonos;
    }
}
