package Requests;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Login extends Request{
     private String telefono;
     private String password;

    @JsonCreator
    public Login(@JsonProperty("telefono") String telefono, @JsonProperty("password") String password) {
        super("LOGIN"); // Set the command type for this request
        this.telefono = telefono;
        this.password = password;
    }

    public String getTelefono() {
        return telefono;
    }

    public String getPassword() {
        return password;
    }
}
