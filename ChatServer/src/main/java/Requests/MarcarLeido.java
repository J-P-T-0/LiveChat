package Requests;

import com.fasterxml.jackson.annotation.JsonProperty;

public class MarcarLeido extends Request {
    private int mensajeID;

    public MarcarLeido(@JsonProperty("mensajeID") int mensajeID) {
        super(("MARCAR_LEIDO"));
        this.mensajeID = mensajeID;
    }

    public int getMensajeID() {
        return mensajeID;
    }
}
