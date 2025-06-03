package Requests;

import com.fasterxml.jackson.annotation.JsonProperty;

public class EnviarMensaje extends Request {
    private String mensaje;
    private int conversacionID;

    public EnviarMensaje(@JsonProperty("mensaje") String mensaje, @JsonProperty("conversacionID") int conversacionID) {
        super("ENVIAR_MENSAJE");
        this.mensaje = mensaje;
        this.conversacionID = conversacionID;
    }

    public String getMensaje() {
        return mensaje;
    }

    public int getConversacionID() {
        return conversacionID;
    }
}
