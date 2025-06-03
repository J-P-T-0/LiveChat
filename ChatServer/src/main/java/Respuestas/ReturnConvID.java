package Respuestas;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ReturnConvID extends Respuesta {
    private int convID;
    private String destinatario;
    private String telDestinatario;

    public ReturnConvID(@JsonProperty("convID") int convID,
                        @JsonProperty("destinatario") String destinatario,
                        @JsonProperty("telDestinatario") String telDestinatario) {
        super("RETURN_CONV_ID");
        this.convID = convID;
        this.destinatario = destinatario;
        this.telDestinatario = telDestinatario;
    }

    public int getConvID() {
        return convID;
    }

    public String getDestinatario() {
        return destinatario;
    }

    public String getTelDestinatario() {return telDestinatario;}
}
