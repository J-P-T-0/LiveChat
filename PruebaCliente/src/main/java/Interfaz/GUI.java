package Interfaz;

import Respuestas.*;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static Main.Conexion.*;
import static Main.CreateRequests.*;

public class GUI extends JFrame {
    //Tabla de conversaciones
    private static LoginAuth loginInfo;
    private static JTable tablaConversaciones;
    private static DefaultTableModel modeloConversaciones;

    //Modelo para modificar la tabla de mensajes
    private static DefaultTableModel modeloMensajes;

    //Mensaje
    private JTextField txtMensaje;

    //Mapea nombres a ID de conversaciones para que se vea mas bonito
    private static Map<String, Integer> conversaciones;

    private static GUI frame;

    //constructor
    public GUI(LoginAuth loginInfo) {
        GUI.loginInfo = loginInfo;
        conversaciones = new HashMap<>();
        initComponents();
        frame = this;

        //evento para cargar conversaciones
        RequestConversaciones();
    }


    //GUI
    private void initComponents() {
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setSize(800, 600);
        setLayout(new BorderLayout());

        setTitle("Conversaciones de " + loginInfo.getNombre());

        // Conversaciones
        modeloConversaciones = new DefaultTableModel();
        modeloConversaciones.setColumnIdentifiers(new String[]{"Nombre"});
        tablaConversaciones = new JTable(modeloConversaciones){
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        //evento para cargar mensajes - pasar a request
        tablaConversaciones.getSelectionModel().addListSelectionListener(_ -> cargarMensajes());

        // Mensajes
        modeloMensajes = new DefaultTableModel();
        modeloMensajes.setColumnIdentifiers(new String[]{"Remitente", "Mensaje", "Fecha"});
        JTable tablaMensajes = new JTable(modeloMensajes);
        tablaMensajes.setEnabled(false);

        // Entrada de mensaje
        JPanel panelMensaje = new JPanel(new BorderLayout());
        txtMensaje = new JTextField();
        JButton btnEnviar = new JButton("Enviar");
        panelMensaje.add(txtMensaje, BorderLayout.CENTER);
        panelMensaje.add(btnEnviar, BorderLayout.EAST);
        JButton btnNuevoChat = new JButton("Nuevo Chat");
        //evento para crear chat privado - pasar a request
        btnNuevoChat.addActionListener(_ -> crearDM());

        JPanel panelDerecho = new JPanel(new BorderLayout());
        panelDerecho.add(btnNuevoChat, BorderLayout.NORTH);
        add(panelDerecho, BorderLayout.EAST);
        //evento ára enviar mensaje - pasar a request
        btnEnviar.addActionListener(_ -> enviarMensaje());

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, new JScrollPane(tablaConversaciones), new JScrollPane(tablaMensajes));// como en el whats app real!
        splitPane.setDividerLocation(250);

        add(splitPane, BorderLayout.CENTER);
        add(panelMensaje, BorderLayout.SOUTH);

        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                System.out.println("Closing window");
                RequestClose(loginInfo.getTelefono());
                System.exit(0);
            }
        });
    }

    /*Funciones ya del chat*/

    public static void cargarConversaciones(ReturnConversaciones respuesta) {

        modeloConversaciones.setRowCount(0); // Limpia por si ya había algo
        for (DatosConversacion conv : respuesta.getDatosConversacion()) {
            String destinatario = " ";

            if (!conv.isEsGrupo()) {
                for(String p: conv.getParticipantes()) {
                    if(!p.equals(loginInfo.getNombre())) {
                        destinatario = p;
                        break;
                    }
                }
                conversaciones.put(destinatario, conv.getId());

                modeloConversaciones.addRow(new Object[]{
                        destinatario
                });
            }
            //Aqui ya con un else nomas y cargas el nomber del grupo
        }

    }


    private void cargarMensajes() {
        int fila = tablaConversaciones.getSelectedRow();
        if (fila == -1) return;

        String destinatario = modeloConversaciones.getValueAt(fila, 0).toString();

        int conversationId = conversaciones.get(destinatario);

        RequestMensajes(conversationId);
    }

    public static void RefreshMensajes(ReturnMensajes respuesta) {
        int fila = tablaConversaciones.getSelectedRow();
        if (fila == -1) return;

        String destinatario = modeloConversaciones.getValueAt(fila, 0).toString();

        int conversationId = conversaciones.get(destinatario);
        if(respuesta.getConvID() == conversationId) {
            SwingUtilities.invokeLater(() -> {
                modeloMensajes.setRowCount(0); // Limpia la tabla de mensajes
                for (DatosMensajes e : respuesta.getDatosMensajes()) {
                    modeloMensajes.addRow(new Object[]{
                            e.getNombre(),
                            e.getMensaje(),
                            e.getFecha()
                    });
                }
            });
        }

    }


    private void enviarMensaje() {
        int fila = tablaConversaciones.getSelectedRow();

        if (fila == -1 || txtMensaje.getText().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Seleccione una conversación y escriba un mensaje");
            return;
        }
        //se obtiene el valor de la fila seleccionada como el indice de la conversacion
        String destinatario = modeloConversaciones.getValueAt(fila, 0).toString();
        int conversationId = conversaciones.get(destinatario);

        RequestEnviarMsg(txtMensaje.getText(), conversationId);

        txtMensaje.setText("");
        cargarMensajes();
    }


    //clase dedicada para la creacion de chats 1v1
    private void crearDM() {
        String telefonoDestino = JOptionPane.showInputDialog(this, "Número del usuario con quien quieres chatear:");

        if(telefonoDestino == null) return;

        if (telefonoDestino.isBlank()) {
            JOptionPane.showMessageDialog(this, "Ingresa un teléfono");
            return;
        }

        if(telefonoDestino.equals(loginInfo.getTelefono())){
            JOptionPane.showMessageDialog(this, "No puedes añadir tu propio número.");
            return;
        }

        RequestNuevoDM(telefonoDestino, loginInfo.getNombre());
    }

    public static void RefreshConversaciones(ReturnConvID convID) {
        // Mostrar mensaje de éxito
        JOptionPane.showMessageDialog(frame, "¡Conversación con " + convID.getDestinatario() + " creada!");

        //Añade conversacion al mapa
        conversaciones.put(convID.getDestinatario(), convID.getConvID());

        // Agregar manualmente la conversación a la tabla
        modeloConversaciones.addRow(new Object[]{
                convID.getDestinatario()
        });
    }

    public static void MostrarAviso(Aviso aviso) {
        JOptionPane.showMessageDialog(frame, aviso.getDescripcion(), aviso.getEstado(), JOptionPane.ERROR_MESSAGE);
    }

}
