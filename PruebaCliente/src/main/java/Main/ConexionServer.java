package Main;

import Interfaz.*;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ConexionServer {
    //Cuestiones del server
    private Socket socket;
    private PrintWriter salida;
    private BufferedReader entrada;
    private ObjectMapper objectMapper;

    public ConexionServer(String ip, int puerto) throws Exception {
        socket = new Socket(ip, puerto);
        salida = new PrintWriter(socket.getOutputStream(), true);
        entrada = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        objectMapper = new ObjectMapper();
    }

    public PrintWriter getSalida() {
        return salida;
    }

    public BufferedReader getEntrada() {
        return entrada;
    }

    public ObjectMapper getObjectMapper() {
        return objectMapper;
    }
}
