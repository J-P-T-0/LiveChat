package Requests;

import com.fasterxml.jackson.annotation.JsonProperty;

public class CrearConversacionIndividual extends Request {
    private String nombreConv;
    private String telefonoDestino;
    private String Remitente;

    public CrearConversacionIndividual(@JsonProperty("nombreConv") String nombreConv,
                                       @JsonProperty("telDestino") String telefonoDestino,
                                       @JsonProperty("remitente") String Remitente) {
        super("CREAR_CONV_PRIV");
        this.nombreConv = nombreConv;
        this.telefonoDestino = telefonoDestino;
        this.Remitente = Remitente;
    }

    public String getNombreConv() {
        return nombreConv;
    }

    public String getTelefonoDestino() {
        return telefonoDestino;
    }

    public String getRemitente() {
        return Remitente;
    }
}
