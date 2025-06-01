package Respuestas;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ReturnConvID extends Respuesta {
    private int convID;

    public ReturnConvID(@JsonProperty("convID") int convID) {
        super("RETURN_CONV_ID");
        this.convID = convID;
    }

    public int getConvID() {
        return convID;
    }
}
