package Requests;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;

public class CrearGrupo extends Request{
    private String nombreGrupo;
    private ArrayList<String> usuarios;

    public CrearGrupo(@JsonProperty("nombreGrupo") String nombreGrupo,
                      @JsonProperty("usuarios") ArrayList<String>  usuarios) {
        super("CREAR_GRUPO");
        this.nombreGrupo = nombreGrupo;
        this.usuarios = usuarios;
    }

    public String getNombreGrupo() {
        return nombreGrupo;
    }

    public ArrayList<String> getUsuarios() {
        return usuarios;
    }
}
