package Respuestas;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;

public class ReturnMensajes extends Respuesta {
    private ArrayList<DatosMensajes> datosMensajes;
    private int convID;

    public ReturnMensajes(@JsonProperty("datosMensajes") ArrayList<DatosMensajes> datosMensajes, @JsonProperty("convID") int convID) {
        super("RETURN_MENSAJES");
        this.datosMensajes = datosMensajes;
        this.convID = convID;
    }

    public ArrayList<DatosMensajes> getDatosMensajes() {
        return datosMensajes;
    }

    public int getConvID() {return convID;}
}
