package org.example;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class ChatServer {
    private int puerto;
    private ServerSocket servidor;

    public ChatServer(int puerto) {
        this.puerto = puerto;
    }

    public void start() {
        try {
            servidor = new ServerSocket(puerto);
            System.out.println("Server escuchando en puerto " + puerto);

            while (true) {
                Socket socketCliente = servidor.accept();
                System.out.println("Cliente conectado");

                ClientHandler manejador = new ClientHandler(socketCliente);
                manejador.start(); // Hilo por cliente
            }
        } catch (IOException e) {
            System.err.println("Error al iniciar el servidor: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        ChatServer servidor = new ChatServer(1234);//puerto al que escucha el server
        servidor.start();
    }
}