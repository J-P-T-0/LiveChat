package Respuestas;

import com.fasterxml.jackson.annotation.JsonProperty;

public class LoginAuth extends Respuesta {
    private boolean autenticado;
    private String nombre;
    private String telefono;

    public LoginAuth(@JsonProperty("") boolean autenticado, String nombre, String telefono) {

    }
}
