package Main;

import Requests.*;
import Respuestas.*;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;

public class PruebaCliente {
    //Servidor
    private PrintWriter salida;
    private BufferedReader entrada;
    private Socket socket;
    private final ObjectMapper objectMapper = new ObjectMapper(); // Agregar al inicio de la clase

    //datos de prueba
    private String nombre="Hola";
    private String telefono="1234567891";
    private String contrasena="julieta123";

    private void conectarAServidor() {
        try {
            // ip publica:puerto
            socket = new Socket("127.0.0.1", 1234); // <- actualiza si cambia el túnel
            salida = new PrintWriter(socket.getOutputStream(), true);
            entrada = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        } catch (Exception e) {
            System.out.println("Error al conectar al servidor: " + e.getMessage());
        }
    }

    private void login(){
        try{
            // Convertir a JSON y enviar
            String jsonRequest = objectMapper.writeValueAsString(new Login(telefono,contrasena));
            System.out.println(jsonRequest);
            salida.println(jsonRequest);

            // Recibir y procesar respuesta
            String respuesta = entrada.readLine();
            System.out.println(respuesta);
        }catch(Exception ex){
            System.out.println("Hubo un problema con: "+ex.getMessage());
        }
    }

    private void registrar(){
        try{
            String jsonRequest = objectMapper.writeValueAsString(new Registrarse(nombre,telefono,contrasena));
            System.out.println(jsonRequest);
            salida.println(jsonRequest);

            String jsonResponse = entrada.readLine();
            System.out.println(jsonResponse);
        }catch (Exception e){
            System.out.println("Error al registrar: " + e.getMessage());
        }
    }

    private void getConversaciones(){
        try{
            String jsonRequest = objectMapper.writeValueAsString(new GetConversaciones());
            System.out.println(jsonRequest);
            salida.println(jsonRequest);

            String jsonResponse = entrada.readLine();
            System.out.println(jsonResponse);
        }catch (Exception e){
            System.out.println("Error al obtener conversación: " + e.getMessage());
        }
    }

    private void getMensajes(){
        try{
            String jsonRequest = objectMapper.writeValueAsString(new GetMensajes(1));
            System.out.println(jsonRequest);
            salida.println(jsonRequest);

            String jsonResponse = entrada.readLine();
            System.out.println(jsonResponse);
        }catch (Exception e){
            System.out.println("Error al obtener mensajes: " + e.getMessage());
        }
    }

    private void enviarMensaje(){
        try{
            String jsonRequest = objectMapper.writeValueAsString(new EnviarMensaje("ZZZZZZ",1));
            System.out.println(jsonRequest);
            salida.println(jsonRequest);

            String jsonResponse = entrada.readLine();
            System.out.println(jsonResponse);

        }catch (Exception e){
            System.out.println("Error al enviar mensaje: " + e.getMessage());
        }
    }

    private void crearConvPriv(){
        try{
            String jsonRequest = objectMapper.writeValueAsString(new CrearConversacionIndividual("Hola Mundo","1234567890", "1234567891"));
            System.out.println(jsonRequest);
            salida.println(jsonRequest);

            String jsonResponse = entrada.readLine();
            System.out.println(jsonResponse);

        }catch (Exception e){
            System.out.println("Error al crear chat: " + e.getMessage());
        }
    }
/*
    private void crearGrupo(){
        try{
            ArrayList<String> telefonos = new ArrayList<>();
            telefonos.add("1234567890");
            telefonos.add("567");
            String jsonRequest = objectMapper.writeValueAsString(new CrearGrupo("Hola6",telefonos));
            System.out.println(jsonRequest);
            salida.println(jsonRequest);

            String jsonResponse = entrada.readLine();
            System.out.println(jsonResponse);

        }catch (Exception e){
            System.out.println("Error al crear grupo: " + e.getMessage());
        }
    }
*/
    private void marcarLeido(){
        try{
            String jsonRequest = objectMapper.writeValueAsString(new MarcarLeido(1));
            System.out.println(jsonRequest);
            salida.println(jsonRequest);

            String jsonResponse = entrada.readLine();
            System.out.println(jsonResponse);

        }catch (Exception e){
            System.out.println("Error al enviar mensaje: " + e.getMessage());
        }
    }

    private void getEstadoMensaje(){
        try{
            String jsonRequest = objectMapper.writeValueAsString(new GetEstadoMensaje(1));
            System.out.println(jsonRequest);
            salida.println(jsonRequest);

            String jsonResponse = entrada.readLine();
            System.out.println(jsonResponse);

        }catch (Exception e){
            System.out.println("Error al enviar mensaje: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        PruebaCliente app = new PruebaCliente();
        try {
            Conexion.connect("127.0.0.1",1234);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        CreateRequests.RequestLogin("1234567891","julieta123");
        CreateRequests.RequestGetUsusEnLinea();
    }
}

