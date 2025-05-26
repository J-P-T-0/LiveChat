package Requests;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Registrarse extends Request{
    private String nombre;
    private String telefono;
    private String contrasena;

    public Registrarse(@JsonProperty("nombre") String nombre, @JsonProperty("telefono") String telefono, @JsonProperty("contrasena") String contrasena) {
        super("REGISTRARSE");
        this.nombre = nombre;
        this.telefono = telefono;
        this.contrasena = contrasena;
    }

    public String getNombre() {
        return nombre;
    }

    public String getTelefono() {
        return telefono;
    }

    public String getContrasena() {
        return contrasena;
    }
}
