package Requests;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Reconnect extends Request {
    private String telefono;
    
    public Reconnect(@JsonProperty("telefono") String telefono) {
        super("RECONECTAR");
        this.telefono = telefono;
    }

    public String getTelefono() {
        return telefono;
    }
}