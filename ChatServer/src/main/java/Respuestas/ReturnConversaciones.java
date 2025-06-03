package Respuestas;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;

public class ReturnConversaciones extends Respuesta {
    private ArrayList<DatosConversacion> datosConversacion;

    public ReturnConversaciones(@JsonProperty("datosConversacion") ArrayList<DatosConversacion> datosConversacion) {
        super("RETURN_CONVERSACIONES");
        this.datosConversacion = datosConversacion;
    }

    public ArrayList<DatosConversacion> getDatosConversacion() {
        return datosConversacion;
    }
}
