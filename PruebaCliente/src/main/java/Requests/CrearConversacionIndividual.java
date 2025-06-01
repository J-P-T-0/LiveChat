package Requests;

import com.fasterxml.jackson.annotation.JsonProperty;

public class CrearConversacionIndividual extends Request {
    private String nombreConv;
    private String telefonoDestino;

    public CrearConversacionIndividual(@JsonProperty("nombreConv") String nombreConv,
                                       @JsonProperty("telDestino") String telefonoDestino) {
        super("CREAR_CONV_PRIV");
        this.nombreConv = nombreConv;
        this.telefonoDestino = telefonoDestino;
    }

    public String getNombreConv() {
        return nombreConv;
    }

    public String getTelefonoDestino() {
        return telefonoDestino;
    }
}
