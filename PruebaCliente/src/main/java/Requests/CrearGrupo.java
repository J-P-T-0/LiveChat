package Requests;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;

public class CrearGrupo extends Request{
    private String nombreGrupo;
    private ArrayList<String> numsTelefono;

    public CrearGrupo(@JsonProperty("nombreGrupo") String nombreGrupo,
                      @JsonProperty("numsTelefono") ArrayList<String>  numsTelefono) {
        super("CREAR_GRUPO");
        this.nombreGrupo = nombreGrupo;
        this.numsTelefono = numsTelefono;
    }

    public String getNombreGrupo() {
        return nombreGrupo;
    }

    public ArrayList<String> getNumsTelefono() {
        return numsTelefono;
    }
}
