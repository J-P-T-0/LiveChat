package Requests;

import com.fasterxml.jackson.annotation.JsonProperty;

public class GetEstadoMensaje extends Request {
    private int mensajeID;

    public GetEstadoMensaje(@JsonProperty("mensajeID") int mensajeID) {
        super("GET_ESTADO_MENSAJE");
        this.mensajeID = mensajeID;
    }

    public int getMensajeID() {
        return mensajeID;
    }
}
