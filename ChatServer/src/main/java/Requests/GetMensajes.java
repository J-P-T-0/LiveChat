package Requests;

import com.fasterxml.jackson.annotation.JsonProperty;

public class GetMensajes extends Request{
    private int conversacionId;

    public GetMensajes(@JsonProperty("conversacionId") int conversacionId){
        super("GET_MENSAJES");
        this.conversacionId = conversacionId;
    }

    public int getConversacionId() {
        return conversacionId;
    }
}
