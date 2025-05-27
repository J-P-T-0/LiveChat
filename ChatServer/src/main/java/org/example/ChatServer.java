package org.example;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.SQLException;

public class ChatServer {
    private int puerto;
    private ServerSocket servidor;
    //pool de conexiones
    protected poolConexiones poolConn;
    public ChatServer(int puerto) throws SQLException {
        this.puerto = puerto;
        this.poolConn = new poolConexiones();
    }

    public void start() {
        try {
            servidor = new ServerSocket(puerto);
            System.out.println("Server escuchando en puerto " + puerto);

            while (true) {
                Socket socketCliente = servidor.accept();
                System.out.println("Cliente conectado");

                ClientHandler manejador = new ClientHandler(socketCliente, poolConn);
                manejador.start(); // Hilo por cliente
            }
        } catch (IOException e) {
            System.err.println("Error al iniciar el servidor: " + e.getMessage());
        }
    }

    public static void main(String[] args) throws SQLException {
        ChatServer servidor = new ChatServer(1234);//puerto al que escucha el server
        servidor.start();
    }
}