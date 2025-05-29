package Respuestas;

import com.fasterxml.jackson.annotation.JsonProperty;

public class LoginAuth extends Respuesta {
    private String nombre;
    private String telefono;

    public LoginAuth(@JsonProperty("nombre") String nombre,
                     @JsonProperty("telefono") String telefono) {
        super("LOGIN_AUTH");
        this.nombre = nombre;
        this.telefono = telefono;
    }

    public String getNombre() {
        return nombre;
    }

    public String getTelefono() {
        return telefono;
    }
}
