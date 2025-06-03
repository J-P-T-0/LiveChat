package Main;

import Respuestas.*;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

import static Interfaz.GUI.*;
import static Main.ChatLogin.confirmLogin;
import static Main.CreateRequests.RequestConversaciones;
import static Main.CreateRequests.lastRequest;

public class Conexion implements Runnable {
    static public PrintWriter salida;
    static public BufferedReader entrada;
    static public ObjectMapper objectMapper;
    static private Socket socket;

    public static void connect(String ip, int puerto) throws Exception {
        //Cuestiones del server

        socket = new Socket(ip, puerto);
        salida = new PrintWriter(socket.getOutputStream(), true);
        entrada = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        objectMapper = new ObjectMapper();

        Thread thread = new Thread(new Conexion());
        thread.start();
    }

    public static void disconnect(){
        try{
            salida.close();
            entrada.close();
            socket.close();
        }catch(Exception e){
            System.out.println("Error al cerrar el cliente" + e.getMessage());
        }
    }

    @Override
    public void run() {
        try {
            String lastLinea = "";
            Respuesta lastResponse = null;
            String linea;
            while ((linea = entrada.readLine()) != null) {
                try {
                    System.out.println(linea);

                    //Almacena la respuesta mandada por el server
                    Respuesta Response = objectMapper.readValue(linea, Respuesta.class);

                    if(!lastLinea.equals(linea) && !Response.equals(lastResponse)) {
                        if(lastRequest.getComando().equals("REGISTRARSE") && Response instanceof Aviso aviso) {
                            Registro.confirmRegistro(aviso);
                            continue;
                        }

                        if(lastRequest.getComando().equals("LOGIN") && Response instanceof Aviso aviso) {
                            ChatLogin.warningRegistro(aviso);
                            continue;
                        }

                        switch (Response) {
                            case LoginAuth R -> confirmLogin(R);

                            case ReturnConversaciones R -> cargarConversaciones(R);

                            case GroupParticipants R -> {
                                RefreshConversaciones(R);
                                RequestConversaciones();//Actualizar conversacionesUsu
                            }

                            case ReturnConvID R -> RefreshConversaciones(R);

                            case ReturnMensajes R -> RefreshMensajes(R);

                            case Aviso aviso -> MostrarAviso(aviso);

                            case CloseClient _ -> disconnect();

                            case ReturnUsusEnLinea R -> RefreshUsuariosConectados(R);

                            default -> System.out.println("lol");
                        }
                    }

                    lastLinea = linea;
                    lastResponse = Response;
                } catch (Exception e) {
                    System.out.println("El error en 'Conexion' es: "+e.getMessage());
                }
            }
        } catch (Exception e) {
            System.err.println("Error con cliente: " + e.getMessage());
            System.exit(1);
        }
    }
}
