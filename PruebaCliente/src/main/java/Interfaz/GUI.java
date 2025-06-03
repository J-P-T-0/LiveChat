package Interfaz;

import Respuestas.*;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.util.ArrayList;
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
        setTitle("Conversaciones de " + loginInfo.getNombre());

// === PANEL PRINCIPAL ===
        JPanel panelPrincipal = new JPanel();
        panelPrincipal.setLayout(new BorderLayout()); // Layout base para poder colocar paneles Norte, Centro, Este

// === PANEL CENTRAL (conversaciones a la izquierda, mensajes a la derecha) ===
        JPanel panelCentro = new JPanel();
        panelCentro.setLayout(new BoxLayout(panelCentro, BoxLayout.X_AXIS)); // Horizontal

// === PANEL DE CONVERSACIONES ===
        modeloConversaciones = new DefaultTableModel();
        modeloConversaciones.setColumnIdentifiers(new String[]{"Nombre"});
        tablaConversaciones = new JTable(modeloConversaciones) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tablaConversaciones.getSelectionModel().addListSelectionListener(_ -> cargarMensajes());

        JScrollPane scrollConversaciones = new JScrollPane(tablaConversaciones);
        JPanel panelConversaciones = new JPanel();
        panelConversaciones.setLayout(new BorderLayout());
        panelConversaciones.setBorder(BorderFactory.createTitledBorder("Conversaciones"));
        panelConversaciones.setPreferredSize(new Dimension(250, 0));
        panelConversaciones.add(scrollConversaciones, BorderLayout.CENTER);

// === PANEL DE MENSAJES ===
        modeloMensajes = new DefaultTableModel();
        modeloMensajes.setColumnIdentifiers(new String[]{"Remitente", "Mensaje", "Fecha"});
        JTable tablaMensajes = new JTable(modeloMensajes);
        tablaMensajes.setEnabled(false);

        JScrollPane scrollMensajes = new JScrollPane(tablaMensajes);
        JPanel panelMensajes = new JPanel();
        panelMensajes.setLayout(new BorderLayout());
        panelMensajes.setBorder(BorderFactory.createTitledBorder("Mensajes"));
        panelMensajes.add(scrollMensajes, BorderLayout.CENTER);

// === AGREGAR CONVERSACIONES Y MENSAJES A PANEL CENTRAL ===
        panelCentro.add(panelConversaciones);
        panelCentro.add(Box.createRigidArea(new Dimension(10, 0))); // Separación entre paneles
        panelCentro.add(panelMensajes);

// === PANEL INFERIOR: MENSAJE Y BOTÓN ENVIAR ===
        JPanel panelMensaje = new JPanel();
        panelMensaje.setLayout(new BoxLayout(panelMensaje, BoxLayout.X_AXIS));
        panelMensaje.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        txtMensaje = new JTextField();
        JButton btnEnviar = new JButton("Enviar");
        panelMensaje.add(txtMensaje);
        panelMensaje.add(Box.createRigidArea(new Dimension(10, 0)));
        panelMensaje.add(btnEnviar);
        btnEnviar.addActionListener(_ -> enviarMensaje());

// === PANEL DERECHO: NUEVO CHAT Y NUEVO GRUPO ===
        JPanel panelDerecho = new JPanel();
        panelDerecho.setLayout(new BoxLayout(panelDerecho, BoxLayout.Y_AXIS));
        panelDerecho.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        panelDerecho.setPreferredSize(new Dimension(150, 0));

        JButton btnNuevoChat = new JButton("Nuevo Chat ");
        btnNuevoChat.setPreferredSize(new Dimension(150, 0));
        btnNuevoChat.addActionListener(_ -> crearDM());
        JButton btnNuevoGrupo = new JButton("Nuevo Grupo");
        btnNuevoGrupo.addActionListener(_ -> crearGrupo());


        panelDerecho.add(btnNuevoChat);
        panelDerecho.add(Box.createRigidArea(new Dimension(0, 10)));
        panelDerecho.add(btnNuevoGrupo);

    // === ENSAMBLADO FINAL ===
        panelPrincipal.add(panelCentro, BorderLayout.CENTER);
        panelPrincipal.add(panelMensaje, BorderLayout.SOUTH);
        panelPrincipal.add(panelDerecho, BorderLayout.EAST);

        add(panelPrincipal);
        pack();

    // === CERRAR PROGRAMA ===
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent windowEvent) {
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
            //Aqui ya con un else nomas y cargas el nombre del grupo
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

    private void crearGrupo() {
        ArrayList<String> usuarios = new ArrayList<>();
        usuarios.add(loginInfo.getNombre());

        JFrame grupo = new JFrame();
        grupo.setTitle("Crear Grupo");
        grupo.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        grupo.setSize(400, 300);
        grupo.setLocationRelativeTo(null); // Centra la ventana

        grupo.setLayout(new BorderLayout(10, 10));

        // Parte superior: nombre del grupo
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.add(new JLabel("Nombre del grupo:"), BorderLayout.WEST);
        JTextField nombreGrupoField = new JTextField();
        topPanel.add(nombreGrupoField, BorderLayout.CENTER);
        grupo.add(topPanel, BorderLayout.NORTH);


        JTable nombresTable = new JTable(modeloConversaciones);
        nombresTable.setRowSelectionAllowed(true);
        nombresTable.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

        JScrollPane scrollPane = new JScrollPane(nombresTable);
        grupo.add(scrollPane, BorderLayout.CENTER);

        // Parte inferior: botones
        JPanel bottomPanel = new JPanel();
        JButton confirmarButton = new JButton("Confirmar");
        JButton cancelarButton = new JButton("Cancelar");
        bottomPanel.add(confirmarButton);
        bottomPanel.add(cancelarButton);
        grupo.add(bottomPanel, BorderLayout.SOUTH);

        grupo.setVisible(true);

        confirmarButton.addActionListener((ActionEvent _) -> {
            int[] filasSeleccionadas = nombresTable.getSelectedRows();
            String nombreGrupo = nombreGrupoField.getText();

            for (int fila : filasSeleccionadas) {
                String nombre = nombresTable.getValueAt(fila, 0).toString();
                usuarios.add(nombre);
            }

            if (nombreGrupo.isBlank()) {
                JOptionPane.showMessageDialog(this, "Ingresa un nombre");
                return;
            }

            if (usuarios.size() < 2) {
                JOptionPane.showMessageDialog(this, "Selecciona usuarios");
                return;
            }

            RequestNuevoGrupo(nombreGrupo, usuarios);
            grupo.dispose();
        });

        cancelarButton.addActionListener((ActionEvent _) -> {
           grupo.dispose();
        });

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
