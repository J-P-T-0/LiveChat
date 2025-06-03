package org.example;

import Respuestas.Aviso;
import Respuestas.Respuesta;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class ChatServer implements AutoCloseable {
    private int puerto;
    private ServerSocket servidor;
    private volatile boolean ejecutando = true;
    protected poolConexiones poolConn;
    // Es la clase principa de JSon que transforma objetos de java en JSON y viceversa
    public static final ObjectMapper traductorJson = new ObjectMapper();

    //telefonos como keys
    public static Map<String, PrintWriter> writers = new HashMap<String, PrintWriter>();

    public ChatServer(int puerto) throws SQLException {
        this.puerto = puerto;
        this.poolConn = new poolConexiones();
    }

    public void start() {
        try {
            servidor = new ServerSocket(puerto);
            System.out.println("Server escuchando en puerto " + puerto);

            while (ejecutando) {
                try {
                    Socket socketCliente = servidor.accept();
                    System.out.println("Cliente conectado: " + socketCliente);

                    ClientHandler manejador = new ClientHandler(socketCliente, poolConn);
                    manejador.start();
                } catch (IOException e) {
                    if (ejecutando) {
                        System.err.println("Error al aceptar cliente: " + e.getMessage());
                    }
                }
            }

            System.out.println("Servidor detenido");
        } catch (IOException e) {
            System.err.println("Error al iniciar el servidor: " + e.getMessage());
        }
    }

    @Override
    public void close() throws Exception {//Cerrar correctamente el server
        ejecutando = false;
        // Crear una solicitud de cierre para cada cliente conectado
        for (Map.Entry<String, PrintWriter> entry : writers.entrySet()) {
            try {
                enviarRespuesta(new Aviso("close", "El servidor se está cerrando"), entry.getValue());
            } catch (Exception e) {
                System.err.println("Error al enviar aviso de cierre a cliente: " + e.getMessage());
            }
        }
        Thread.sleep(1000);

        // Cerrar el servidor y las conexiones
        if (servidor != null) {
            servidor.close();
        }
        if (poolConn != null) {
            poolConn.close();
        }

        // Limpiar el mapa de writers
        writers.clear();
    }

    //Metodo de enviar mensaje de clientHandler
    private void enviarRespuesta(Respuesta respuesta, PrintWriter output) throws JsonProcessingException {
        System.out.println("Respuesta a otro");
        String jsonRespuesta = traductorJson.writeValueAsString(respuesta);
        output.println(jsonRespuesta);
    }

    public static void main(String[] args) {//Try-with-resources con el metodo close de autocloseable sobreescrito para que se cierre el servidor al finalizar start();
        try (ChatServer servidor = new ChatServer(1234)) {
            servidor.start();
        } catch (Exception e) {
            System.err.println("Error fatal del servidor: " + e.getMessage());
        }
    }
}