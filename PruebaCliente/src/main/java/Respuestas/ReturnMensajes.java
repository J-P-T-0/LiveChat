package Respuestas;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;

public class ReturnMensajes extends Respuesta {
    private ArrayList<DatosMensajes> datosMensajes;

    public ReturnMensajes(@JsonProperty("datosMensajes") ArrayList<DatosMensajes> datosMensajes) {
        super("RETURN_MENSAJES");
        this.datosMensajes = datosMensajes;
    }

    public ArrayList<DatosMensajes> getDatosMensajes() {
        return datosMensajes;
    }
}
