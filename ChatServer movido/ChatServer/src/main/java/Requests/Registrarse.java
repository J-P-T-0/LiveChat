package Requests;

public class Registrarse extends Request{
    private String usuario;
    private String contrasena;

    public Registrarse(String usuario, String contrasena) {
        super("REGISTRARSE");
        this.usuario = usuario;
        this.contrasena = contrasena;
    }
}
