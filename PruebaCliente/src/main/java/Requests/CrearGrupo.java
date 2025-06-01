package Requests;

import com.fasterxml.jackson.annotation.JsonProperty;

public class CrearGrupo extends Request{
    private String nombreGrupo;
    private String numsTelefono;

    public CrearGrupo(@JsonProperty("nombreGrupo") String nombreGrupo,
                      @JsonProperty("numsTelefono") String numsTelefono) {
        super("CREAR_GRUPO");
        this.nombreGrupo = nombreGrupo;
        this.numsTelefono = numsTelefono;
    }

    public String getNombreGrupo() {
        return nombreGrupo;
    }

    public String getNumsTelefono() {
        return numsTelefono;
    }
}
