package Interfaz;

import Main.ConexionServer;
import Main.LoginC;
import Requests.EnviarMensaje;
import Requests.GetConversaciones;
import Requests.GetMensajes;
import Respuestas.*;
import com.fasterxml.jackson.databind.ObjectMapper;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class GUI extends JFrame implements Runnable {
    //Tabla de conversaciones
    private JTable tablaConversaciones;
    private DefaultTableModel modeloConversaciones;
    //tabla de mensajes
    private JTable tablaMensajes;
    private DefaultTableModel modeloMensajes;
    //Mensaje
    private JTextField txtMensaje;

    //Cuestiones del server
    private static PrintWriter salida;
    private static BufferedReader entrada;
    private static ObjectMapper objectMapper;

    //constructor
    public GUI() {
        conectarAServidor();
        initComponents();
        cargarConversaciones();
    }

    public static void conectarAServidor() {
        ConexionServer conectarAServidor = LoginC.conexionServer;
        try {
            entrada= conectarAServidor.getEntrada();
            salida= conectarAServidor.getSalida();
            objectMapper= conectarAServidor.getObjectMapper();
        } catch (Exception e) {
            GUI.mostrarError("Error al conectar con el servidor.");
        }
    }

    //GUI
    private void initComponents() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 600);
        setLayout(new BorderLayout());

        // Conversaciones
        modeloConversaciones = new DefaultTableModel();
        modeloConversaciones.setColumnIdentifiers(new String[]{"ID", "Nombre"});
        tablaConversaciones = new JTable(modeloConversaciones);
        tablaConversaciones.getSelectionModel().addListSelectionListener(e -> cargarMensajes());

        // Mensajes
        modeloMensajes = new DefaultTableModel();
        modeloMensajes.setColumnIdentifiers(new String[]{"Remitente", "Mensaje", "Fecha"});
        tablaMensajes = new JTable(modeloMensajes);

        // Entrada de mensaje
        JPanel panelMensaje = new JPanel(new BorderLayout());
        txtMensaje = new JTextField();
        JButton btnEnviar = new JButton("Enviar");
        panelMensaje.add(txtMensaje, BorderLayout.CENTER);
        panelMensaje.add(btnEnviar, BorderLayout.EAST);

        btnEnviar.addActionListener(e -> enviarMensaje());

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, new JScrollPane(tablaConversaciones), new JScrollPane(tablaMensajes));// como en el whats app real!
        splitPane.setDividerLocation(250);

        add(splitPane, BorderLayout.CENTER);
        add(panelMensaje, BorderLayout.SOUTH);
    }

    //funciones ya del chat
    private void cargarConversaciones() {
        try{
            String jsonRequest = objectMapper.writeValueAsString(new GetConversaciones());
            System.out.println(jsonRequest);
            salida.println(jsonRequest);

            String jsonResponse = entrada.readLine();
            System.out.println(jsonResponse);
            Respuesta respuesta = objectMapper.readValue(jsonResponse, Respuesta.class);
            if(respuesta instanceof ReturnConversaciones){
                ReturnConversaciones returnConversaciones = (ReturnConversaciones) respuesta;
                for(DatosConversacion e: returnConversaciones.getDatosConversacion()){
                        modeloConversaciones.addRow(new Object[]{
                                e.getId(),
                                e.getNombre()
                        });
                }
            }else if (respuesta instanceof Aviso){
                Aviso aviso = (Aviso) respuesta;
                mostrarError(aviso.getDescripcion());
            }
        }catch (Exception e){
            System.out.println("Error al obtener conversación: " + e.getMessage());
        }
    }

    private void cargarMensajes() {
        int fila = tablaConversaciones.getSelectedRow();
        if (fila == -1) return;
        int conversationId = (int) modeloConversaciones.getValueAt(fila, 0);

        new Thread(() -> {
            try {
                String jsonPrevResponse = null;
                while (!Thread.currentThread().isInterrupted()) {
                    String jsonRequest = objectMapper.writeValueAsString(new GetMensajes(conversationId));
                    salida.println(jsonRequest);

                    String jsonResponse = entrada.readLine();

                    // Si la respuesta es igual a la anterior, esperar un momento y continuar
                    if (jsonResponse.equals(jsonPrevResponse)) {
                        Thread.sleep(500); // Esperar 500ms antes de la siguiente verificación
                        continue;
                    }

                    System.out.println("Nuevo mensaje recibido");
                    Respuesta respuesta = objectMapper.readValue(jsonResponse, Respuesta.class);

                    if (respuesta instanceof ReturnMensajes) {
                        ReturnMensajes returnMensajes = (ReturnMensajes) respuesta;

                        // Actualizar la UI en el hilo de eventos de Swing
                        SwingUtilities.invokeLater(() -> {
                            modeloMensajes.setRowCount(0);
                            for (DatosMensajes e : returnMensajes.getDatosMensajes()) {
                                modeloMensajes.addRow(new Object[]{
                                        e.getNombre(),
                                        e.getMensaje(),
                                        e.getFecha()
                                });
                            }
                        });
                    } else if (respuesta instanceof Aviso) {
                        Aviso aviso = (Aviso) respuesta;
                        SwingUtilities.invokeLater(() ->
                                mostrarError(aviso.getDescripcion())
                        );
                    }

                    jsonPrevResponse = jsonResponse;
                }
            } catch (Exception e) {
                System.out.println("Error al obtener mensajes: " + e.getMessage());
            }
        }).start();
    }

    private void enviarMensaje() {
        int fila = tablaConversaciones.getSelectedRow();
        if (fila == -1 || txtMensaje.getText().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Seleccione una conversación y escriba un mensaje");
            return;
        }
        //se obtiene el valor de la fila seleccionada como el indice de la conversacion
        int conversationId = (int) modeloConversaciones.getValueAt(fila, 0);
        try{
            String jsonRequest = objectMapper.writeValueAsString(new EnviarMensaje(txtMensaje.getText(),conversationId));
            System.out.println(jsonRequest);
            salida.println(jsonRequest);

            String jsonResponse = entrada.readLine();
            System.out.println(jsonResponse);
            Respuesta respuesta = objectMapper.readValue(jsonResponse, Respuesta.class);
            if(respuesta instanceof ReturnMensajes){
                cargarMensajes();
            }else if (respuesta instanceof Aviso){
                Aviso aviso = (Aviso) respuesta;
                mostrarError(aviso.getDescripcion());
            }
        }catch (Exception e){
            System.out.println("Error al enviar mensaje: " + e.getMessage());
        }
        txtMensaje.setText("");
        cargarMensajes();
    }

    public static void mostrarError(String msg) {
        JOptionPane.showMessageDialog(null, msg, "Error", JOptionPane.ERROR_MESSAGE);
    }

    @Override
    public void run() {
        cargarMensajes();
    }

}
