package Respuestas;

import com.fasterxml.jackson.annotation.JsonProperty;

public class LoginAuth extends Respuesta {
    private String nombre;
    private String telefono;
    private String token;
    private int idUsuario;

    public LoginAuth(
            @JsonProperty("nombre") String nombre,
            @JsonProperty("telefono") String telefono) {
        super("LOGIN_AUTH");
        this.nombre = nombre;
        this.telefono = telefono;
        this.token = token;
        this.idUsuario = idUsuario;
    }

    public String getNombre() {
        return nombre;
    }

    public String getTelefono() {
        return telefono;
    }

    public String getToken() {
        return token;
    }

    public int getIdUsuario() {
        return idUsuario;
    }
}
