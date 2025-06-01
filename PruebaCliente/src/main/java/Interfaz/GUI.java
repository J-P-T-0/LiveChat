package Interfaz;

import Requests.CrearConversacionIndividual;
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

public class GUI extends JFrame implements Runnable {
    //Tabla de conversaciones
    private LoginAuth loginInfo;
    private JTable tablaConversaciones;
    private DefaultTableModel modeloConversaciones;
    //tabla de mensajes
    private Thread hiloMensajes; // Este hilo estará revisando nuevos mensajes solo de la conversación activa

    private JTable tablaMensajes;
    private DefaultTableModel modeloMensajes;
    //Mensaje
    private JTextField txtMensaje;

    //Cuestiones del server
    private Socket socket;
    private PrintWriter salida;
    private BufferedReader entrada;
    private ObjectMapper objectMapper = new ObjectMapper();

    //constructor
    public GUI(LoginAuth loginInfo, Socket socket) {
        this.socket = socket;
        this.loginInfo = loginInfo;
        try {
            salida = new PrintWriter(socket.getOutputStream(), true);
            entrada = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        } catch (Exception e) {
            mostrarError("Error al conectar con el servidor.");
        }
        initComponents();
        cargarConversaciones();
    }

    //GUI
    private void initComponents() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 600);
        setLayout(new BorderLayout());

        setTitle("Conversaciones de " + loginInfo.getNombre());

        // Conversaciones
        modeloConversaciones = new DefaultTableModel();
        modeloConversaciones.setColumnIdentifiers(new String[]{"ID", "Nombre"});
        tablaConversaciones = new JTable(modeloConversaciones){
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tablaConversaciones.getSelectionModel().addListSelectionListener(e -> cargarMensajes());

        // Mensajes
        modeloMensajes = new DefaultTableModel();
        modeloMensajes.setColumnIdentifiers(new String[]{"Remitente", "Mensaje", "Fecha"});
        tablaMensajes = new JTable(modeloMensajes);
        tablaMensajes.setEnabled(false);

        // Entrada de mensaje
        JPanel panelMensaje = new JPanel(new BorderLayout());
        txtMensaje = new JTextField();
        JButton btnEnviar = new JButton("Enviar");
        panelMensaje.add(txtMensaje, BorderLayout.CENTER);
        panelMensaje.add(btnEnviar, BorderLayout.EAST);
        JButton btnNuevoChat = new JButton("Nuevo Chat");
        btnNuevoChat.addActionListener(e -> crearNuevoChat());

        JPanel panelDerecho = new JPanel(new BorderLayout());
        panelDerecho.add(btnNuevoChat, BorderLayout.NORTH);
        add(panelDerecho, BorderLayout.EAST);
        btnEnviar.addActionListener(e -> enviarMensaje());

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, new JScrollPane(tablaConversaciones), new JScrollPane(tablaMensajes));// como en el whats app real!
        splitPane.setDividerLocation(250);

        add(splitPane, BorderLayout.CENTER);
        add(panelMensaje, BorderLayout.SOUTH);
    }

    private void mostrarError(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Error", JOptionPane.ERROR_MESSAGE);
    }

    //funciones ya del chat
    private void cargarConversaciones() {
        try {
            String jsonRequest = objectMapper.writeValueAsString(new GetConversaciones());
            System.out.println(jsonRequest);
            salida.println(jsonRequest);

            String jsonResponse = entrada.readLine();
            System.out.println(jsonResponse);
            Respuesta respuesta = objectMapper.readValue(jsonResponse, Respuesta.class);

            if (respuesta instanceof ReturnConversaciones returnConversaciones) {
                modeloConversaciones.setRowCount(0); // Limpia por si ya había algo
                for (DatosConversacion conv : returnConversaciones.getDatosConversacion()) {
                    // Validación: no mostrar conversación contigo mismo

                    if (!conv.getNombre().equals(loginInfo.getTelefono())) {
                        modeloConversaciones.addRow(new Object[]{
                                conv.getId(),
                                conv.getNombre()
                        });
                    }else{ //Obtener nombre del destinatario
                        String destRequest = objectMapper.writeValueAsString(new GetMensajes(conv.getId()));
                        salida.println(destRequest);

                        String destResponse = entrada.readLine();

                        Respuesta destAnswer = objectMapper.readValue(destResponse, Respuesta.class);

                        if (destAnswer instanceof ReturnMensajes returnMensajes) {
                            for (DatosMensajes m : returnMensajes.getDatosMensajes()) {
                                if (!m.getNombre().equals(loginInfo.getNombre())) {
                                    modeloConversaciones.addRow(new Object[]{
                                            conv.getId(),
                                            m.getNombre()
                                    });
                                    break;
                                }

                            }
                        } else if (destAnswer instanceof Aviso aviso) {
                            SwingUtilities.invokeLater(() -> mostrarError(aviso.getDescripcion()));
                        }
                    }
                }
            } else if (respuesta instanceof Aviso aviso) {
                mostrarError(aviso.getDescripcion());
            }
        } catch (Exception e) {
            System.out.println("Error al obtener conversación: " + e.getMessage());
        }
    }

    private void cargarMensajes() {
        int fila = tablaConversaciones.getSelectedRow();
        if (fila == -1) return;

        int conversationId = Integer.parseInt(modeloConversaciones.getValueAt(fila, 0).toString());


        // Antes de crear un nuevo hilo, detenemos el anterior si sigue vivo
        if (hiloMensajes != null && hiloMensajes.isAlive()) {
            hiloMensajes.interrupt(); // Le decimos al hilo anterior que se detenga
        }

        // Creamos un nuevo hilo que estará escuchando mensajes para esta conversación
        hiloMensajes = new Thread(() -> {
            try {
                String jsonPrevResponse = null;
                while (!Thread.currentThread().isInterrupted()) {
                    String jsonRequest = objectMapper.writeValueAsString(new GetMensajes(conversationId));
                    salida.println(jsonRequest);

                    String jsonResponse = entrada.readLine();

                    // No hay nada nuevo, así que seguimos

                    if (!jsonResponse.equals(jsonPrevResponse)) {
                        jsonPrevResponse = jsonResponse; // Guardamos la última respuesta

                        Respuesta respuesta = objectMapper.readValue(jsonResponse, Respuesta.class);

                        if (respuesta instanceof ReturnMensajes returnMensajes) {
                            SwingUtilities.invokeLater(() -> {
                                modeloMensajes.setRowCount(0); // Limpia la tabla de mensajes
                                for (DatosMensajes e : returnMensajes.getDatosMensajes()) {
                                    modeloMensajes.addRow(new Object[]{
                                            e.getNombre(),
                                            e.getMensaje(),
                                            e.getFecha()
                                    });
                                }
                            });
                        } else if (respuesta instanceof Aviso aviso) {
                            SwingUtilities.invokeLater(() -> mostrarError(aviso.getDescripcion()));
                        }
                    }

                }
            } catch (Exception e) {
                System.out.println("Error en hilo de mensajes: " + e.getMessage());
            }
        });

        hiloMensajes.start(); // Inicia el nuevo hilo para esta conversación
    }


    private void enviarMensaje() {
        int fila = tablaConversaciones.getSelectedRow();

        if (fila == -1 || txtMensaje.getText().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Seleccione una conversación y escriba un mensaje");
            return;
        }
        //se obtiene el valor de la fila seleccionada como el indice de la conversacion
        int conversationId = Integer.parseInt(modeloConversaciones.getValueAt(fila, 0).toString());

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
    //clase dedicada para la creacion de chats 1v1
    private void crearNuevoChat() {
        String telefonoDestino = JOptionPane.showInputDialog(this, "Número del usuario con quien quieres chatear:");

        if (telefonoDestino == null || telefonoDestino.isBlank() || telefonoDestino.equals(loginInfo.getTelefono())) {
            JOptionPane.showMessageDialog(this, "Número inválido o es tu propio número.");
            return;
        }

        try {
            CrearConversacionIndividual solicitud = new CrearConversacionIndividual(loginInfo.getTelefono(), telefonoDestino);
            String jsonRequest = objectMapper.writeValueAsString(solicitud);
            System.out.println(jsonRequest);
            salida.println(jsonRequest);

            String jsonResponse = entrada.readLine();
            System.out.println(jsonResponse);
            Respuesta respuesta = objectMapper.readValue(jsonResponse, Respuesta.class);

            if (respuesta instanceof ReturnConvID convID) {
                // Mostrar mensaje de éxito
                JOptionPane.showMessageDialog(this, "¡Conversación creada!");

                // Agregar manualmente la conversación a la tabla
                modeloConversaciones.addRow(new Object[]{
                        convID.getConvID(),
                        telefonoDestino
                });
                txtMensaje.setText("Hola!");
                enviarMensaje();

            } else if (respuesta instanceof Aviso aviso) {
                JOptionPane.showMessageDialog(this, aviso.getDescripcion(), aviso.getEstado(), JOptionPane.ERROR_MESSAGE);
            }

        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "Error al crear la conversación: " + ex.getMessage());
            ex.printStackTrace();
        }
    }
    @Override
    public void run() {
        cargarMensajes();
    }
}
