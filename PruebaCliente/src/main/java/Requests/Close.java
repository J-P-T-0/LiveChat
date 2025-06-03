package Requests;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Close extends Request {
    private String telefono;
    public Close(@JsonProperty("telDestino") String telefono) {
        super("CLOSE_CONNECTION");
        this.telefono = telefono;
    }

    public String getTelefono() {
        return telefono;
    }
}
