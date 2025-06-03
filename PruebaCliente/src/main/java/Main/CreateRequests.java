package Main;

import Requests.*;

import java.util.ArrayList;

import static Main.Conexion.*;

public class CreateRequests {
    public static Request lastRequest = null;

    private static void CallRequest(Request request){
        try{
            lastRequest = request;
            String jsonRequest = objectMapper.writeValueAsString(request);
            System.out.println(jsonRequest);
            salida.println(jsonRequest);
        }catch (Exception e){
            System.out.println("Error al pedir request: " + e.getMessage());
        }
    }

    public static void RequestConversaciones(){
        CallRequest(new GetConversaciones());
    }

    public static void RequestLogin(String telefono, String password){
        CallRequest(new Login(telefono, password));
    }

    public static void RequestRegistro(String nombre, String telefono, String contrasena){
        CallRequest(new Registrarse(nombre,telefono,contrasena));
    }

    public static void RequestMensajes(int conversationId){
        CallRequest(new GetMensajes(conversationId));
    }

    public static void RequestEnviarMsg(String msg, int conversationId){
        CallRequest(new EnviarMensaje(msg,conversationId));
    }

    public static void RequestNuevoDM(String telefonoDestino, String Remitente){
        CallRequest(new CrearConversacionIndividual("DM", telefonoDestino, Remitente));
    }

    public static void RequestNuevoGrupo(String nombre, ArrayList<String> telefonos){
        CallRequest(new CrearGrupo(nombre, telefonos));
    }

    public static void RequestClose(String telefono){
        CallRequest(new Close(telefono));
    }
}
