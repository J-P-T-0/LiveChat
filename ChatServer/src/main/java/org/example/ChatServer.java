package org.example;

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

    //telefonos como keys
    public static Map<String, BufferedReader> readers = new HashMap<String, BufferedReader>();
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
                Socket socketCliente = servidor.accept();
                System.out.println("Cliente conectado");

                ClientHandler manejador = new ClientHandler(socketCliente, poolConn);
                manejador.start();
            }
        } catch (IOException e) {
            System.err.println("Error al iniciar el servidor: " + e.getMessage());
        }
    }

    @Override
    public void close() throws Exception {//Cerrar correctamente el server
        ejecutando = false;
        if (servidor != null) {
            servidor.close();
        }
        if (poolConn != null) {
            poolConn.close();
        }
    }

    public static void main(String[] args) {//Try-with-resources con el metodo close de autocloseable sobreescrito para que se cierre el servidor al finalizar start();
        try (ChatServer servidor = new ChatServer(1234)) {
            servidor.start();
        } catch (Exception e) {
            System.err.println("Error fatal del servidor: " + e.getMessage());
        }
    }
}