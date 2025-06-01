package Respuestas;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ReturnConvID extends Respuesta {
    private int convID;
    private String destinatario;

    public ReturnConvID(@JsonProperty("convID") int convID,
                        @JsonProperty("destinatario") String destinatario) {
        super("RETURN_CONV_ID");
        this.convID = convID;
        this.destinatario = destinatario;
    }

    public int getConvID() {
        return convID;
    }

    public String getDestinatario() {
        return destinatario;
    }
}
