package Interfaz;

import Respuestas.*;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.HashMap;

import static Main.CreateRequests.*;

public class GUI extends JFrame {
    //Tabla de conversaciones
    private static LoginAuth loginInfo;
    private static JTable tablaConversaciones;
    private static DefaultTableModel modeloConversaciones;

    //Modelo para modificar la tabla de mensajes
    private static JPanel mensajesPanel;
    private static JScrollPane scrollMensajes;
    private static JPanel panelMensaje;
    //Modelo para mostrar los usuarios conectados
    private static DefaultListModel<String> modeloUsuariosConectados;

    //Mensaje
    private JTextField txtMensaje;

    //Mapea nombres a ID de conversaciones para que se vea mas bonito
    private static HashMap<String, String> contactos;
    private static HashMap<Integer, String> conversaciones;

    private static GUI frame;

    //constructor
    public GUI(LoginAuth loginInfo) {
        GUI.loginInfo = loginInfo;
        contactos = new HashMap<>();
        conversaciones = new HashMap<>();
        initComponents();
        frame = this;
        frame.setExtendedState(JFrame.MAXIMIZED_BOTH);

        //evento para cargar conversaciones
        RequestConversaciones();
        RequestGetUsusEnLinea();
    }

    //GUI
    private void initComponents() {
        int heightOfFrame = 600;
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setSize(800, heightOfFrame);
        setTitle("Conversaciones de " + loginInfo.getNombre());

// === PANEL PRINCIPAL ===
        JPanel panelPrincipal = new JPanel();
        panelPrincipal.setLayout(new BorderLayout()); // Layout base para poder colocar paneles Norte, Centro, Este


// === PANEL DE CONVERSACIONES ===
        modeloConversaciones = new DefaultTableModel();
        modeloConversaciones.setColumnIdentifiers(new String[]{"ID","Nombre"});
        tablaConversaciones = new JTable(modeloConversaciones) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tablaConversaciones.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                cargarMensajes();
            }
        });
        TableColumnModel tcm = tablaConversaciones.getColumnModel();
        tcm.removeColumn( tcm.getColumn(0));

        JScrollPane scrollConversaciones = new JScrollPane(tablaConversaciones);
        JPanel panelConversaciones = new JPanel();
        panelConversaciones.setLayout(new BorderLayout());
        panelConversaciones.setBorder(BorderFactory.createTitledBorder("Conversaciones"));
        panelConversaciones.setPreferredSize(new Dimension(250, 0));
        panelConversaciones.add(scrollConversaciones, BorderLayout.CENTER);

// === PANEL INFERIOR: MENSAJE Y BOTÓN ENVIAR ===
        panelMensaje = new JPanel();
        panelMensaje.setVisible(false);
        panelMensaje.setLayout(new BoxLayout(panelMensaje, BoxLayout.X_AXIS));
        panelMensaje.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        txtMensaje = new JTextField();
        JButton btnEnviar = new JButton("Enviar");
        panelMensaje.add(txtMensaje);
        panelMensaje.add(Box.createRigidArea(new Dimension(10, 0)));
        panelMensaje.add(btnEnviar);
        btnEnviar.addActionListener(_ -> enviarMensaje());
        txtMensaje.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER && !e.isShiftDown()) {
                    e.consume(); // Evita que agregue un salto de línea
                    enviarMensaje();
                }
            }
        });

// === PANEL DE MENSAJES ===
        mensajesPanel = new JPanel();
        mensajesPanel.setLayout(new BoxLayout(mensajesPanel, BoxLayout.Y_AXIS));
        scrollMensajes = new JScrollPane(mensajesPanel);
        scrollMensajes.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        scrollMensajes.getVerticalScrollBar().setUnitIncrement(18);

        JPanel panelMensajes = new JPanel();
        panelMensajes.setLayout(new BorderLayout());
        panelMensajes.setBorder(BorderFactory.createTitledBorder("Mensajes"));
        panelMensajes.add(scrollMensajes, BorderLayout.CENTER);
        panelMensajes.add(panelMensaje, BorderLayout.SOUTH);

// === AGREGAR CONVERSACIONES Y MENSAJES A PANEL CENTRAL ===
        JSplitPane panelCentro = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, panelConversaciones, panelMensajes);
        panelCentro.setResizeWeight(0.025);



// === PANEL DERECHO: NUEVO CHAT Y NUEVO GRUPO ===
        int localwidth = 150;
        JPanel panelDerecho = new JPanel();
        panelDerecho.setLayout(new BoxLayout(panelDerecho, BoxLayout.Y_AXIS));
        panelDerecho.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        panelDerecho.setPreferredSize(new Dimension(localwidth, 0));

        JButton btnNuevoChat = new JButton("Nuevo Chat ");
        btnNuevoChat.setPreferredSize(new Dimension(localwidth, 30));
        btnNuevoChat.addActionListener(_ -> crearDM());
        JButton btnNuevoGrupo = new JButton("Nuevo Grupo");
        btnNuevoGrupo.addActionListener(_ -> crearGrupo());
        btnNuevoGrupo.setPreferredSize(new Dimension(localwidth, 30));

        //== LISTA QUE MUESTRA LOS USUARIOS CONECTADOS ==//
        JList<String> listaUsuarios = new JList<>();
        modeloUsuariosConectados= new DefaultListModel<>();
        listaUsuarios.setModel(modeloUsuariosConectados);
        listaUsuarios.setFixedCellWidth(localwidth);
        listaUsuarios.setFixedCellHeight(20);
        listaUsuarios.setPreferredSize(new Dimension(localwidth, 0));

        JScrollPane scrollUsuarios = new JScrollPane(listaUsuarios);
        scrollUsuarios.setPreferredSize(new Dimension(localwidth, 100));

        panelDerecho.add(btnNuevoChat);
        panelDerecho.add(Box.createRigidArea(new Dimension(0, 10)));
        panelDerecho.add(btnNuevoGrupo);
        panelDerecho.add(Box.createRigidArea(new Dimension(0, 10)));
        panelDerecho.add(scrollUsuarios);

    // === ENSAMBLADO FINAL ===
        panelPrincipal.add(panelCentro, BorderLayout.CENTER);
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

    //Funcion de actualizacion de los usuarios
    public static void RefreshUsuariosConectados(ReturnUsusEnLinea respuesta){
        modeloUsuariosConectados.clear();
        for(String u: respuesta.getUsuariosEnLinea().keySet()){
            if(u.equals(loginInfo.getTelefono())) continue;

            modeloUsuariosConectados.addElement(contactos.getOrDefault(u, u));
        }
    }

    /*Funciones ya del chat*/

    public static void cargarConversaciones(ReturnConversaciones respuesta) {

        modeloConversaciones.setRowCount(0); // Limpia por si ya había algo
        for (DatosConversacion conv : respuesta.getDatosConversacion()) {

            if (!conv.isEsGrupo()) {
                String destinatario = " ";
                String telefono = " ";
                for(String p: conv.getParticipantes()) {
                    if(!p.equals(loginInfo.getNombre())) {
                        destinatario = p;
                    }
                }

                for(String t: conv.getTelefonos()) {
                    if(!t.equals(loginInfo.getTelefono())) {
                        telefono = t;
                    }
                }

                modeloConversaciones.addRow(new Object[]{
                        conv.getId(),
                        destinatario
                });

                if(!contactos.containsKey(telefono))
                    contactos.put(telefono, destinatario);

                if(!conversaciones.containsKey(conv.getId()))
                    conversaciones.put(conv.getId(), telefono);

            }else{
                modeloConversaciones.addRow(new Object[]{
                        conv.getId(),
                        conv.getNombre()
                });
            }
        }

        for(String u: contactos.keySet()){
            System.out.println(u + ": " + contactos.get(u));
        }

    }

    private void cargarMensajes() {
        int fila = tablaConversaciones.getSelectedRow();
        if (fila == -1) return;

        int conversationId = Integer.parseInt(tablaConversaciones.getModel().getValueAt(fila, 0).toString());
        panelMensaje.setVisible(true);
        scrollMensajes.getVerticalScrollBar().setValue(scrollMensajes.getVerticalScrollBar().getMaximum());

        RequestMensajes(conversationId);
    }

    public static void RefreshMensajes(ReturnMensajes respuesta) {
        int fila = tablaConversaciones.getSelectedRow();
        if (fila == -1) return;

        int conversationId = Integer.parseInt(tablaConversaciones.getModel().getValueAt(fila, 0).toString());

        if(respuesta.getConvID() == conversationId) {
            mensajesPanel.removeAll(); // Limpia mensajes anteriores

            for (DatosMensajes e : respuesta.getDatosMensajes()) {
                JPanel burbuja = new JPanel();
                burbuja.setLayout(new BoxLayout(burbuja, BoxLayout.Y_AXIS));
                burbuja.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
                burbuja.setBackground(e.getNombre().equals(loginInfo.getNombre()) ? new Color(220, 248, 198) : Color.WHITE);

                JLabel remitente = new JLabel(e.getNombre());
                remitente.setFont(new Font("Arial", Font.BOLD, 12));

                JTextArea mensaje = new JTextArea(e.getMensaje());
                mensaje.setFont(new Font("Arial", Font.PLAIN, 14));
                mensaje.setLineWrap(true);
                mensaje.setWrapStyleWord(true);
                mensaje.setEditable(false);
                mensaje.setOpaque(false);

                JLabel fecha = new JLabel(e.getFecha());
                fecha.setFont(new Font("Arial", Font.ITALIC, 10));
                fecha.setHorizontalAlignment(SwingConstants.RIGHT);

                burbuja.add(remitente);
                burbuja.add(mensaje);
                burbuja.add(fecha);

                JPanel contenedor = new JPanel(new FlowLayout(e.getNombre().equals(loginInfo.getNombre()) ? FlowLayout.RIGHT : FlowLayout.LEFT));
                contenedor.add(burbuja);
                mensajesPanel.add(contenedor);
            }

            // Refresca la vista
            mensajesPanel.revalidate();
            mensajesPanel.repaint();
            SwingUtilities.invokeLater(() -> {
                JScrollBar vertical = scrollMensajes.getVerticalScrollBar();
                vertical.setValue(vertical.getMaximum());
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

        int conversationId = Integer.parseInt(tablaConversaciones.getModel().getValueAt(fila, 0).toString());

        RequestEnviarMsg(txtMensaje.getText(), conversationId);

        txtMensaje.setText("");
    }

    private void crearGrupo() {
        ArrayList<String> usuarios = new ArrayList<>();
        usuarios.add(loginInfo.getTelefono());

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

        TableModel tableModel = modeloConversaciones;

        JTable nombresTable = new JTable(tableModel);
        nombresTable.setRowSelectionAllowed(true);
        nombresTable.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        TableColumnModel tcm = nombresTable.getColumnModel();
        tcm.removeColumn( tcm.getColumn(0));

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
                Integer conversationId = Integer.parseInt(nombresTable.getModel().getValueAt(fila, 0).toString());
                String telefono = conversaciones.get(conversationId);

                usuarios.add(telefono);
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
        conversaciones.put(convID.getConvID(), convID.getTelDestinatario());
        contactos.put(convID.getTelDestinatario(), convID.getDestinatario());

        // Agregar manualmente la conversación a la tabla
        modeloConversaciones.addRow(new Object[]{
                convID.getConvID(),
                convID.getDestinatario()
        });

        for (String u : contactos.keySet()) {
            System.out.println(u + ": " + contactos.get(u));
        }

        RequestGetUsusEnLinea();
    }

    public static void RefreshConversaciones(GroupParticipants groupParticipants) {
        // Mostrar mensaje de éxito
        JOptionPane.showMessageDialog(frame, "¡Grupo " + groupParticipants.getGroupName() + " creado!");

        //Añade conversacion al mapa
        conversaciones.put(groupParticipants.getGroupID(), groupParticipants.getGroupName());

        // Agregar manualmente la conversación a la tabla
        modeloConversaciones.addRow(new Object[]{
                groupParticipants.getGroupID(),
                groupParticipants.getGroupName()
        });
    }

    public static void MostrarAviso(Aviso aviso) {
        JOptionPane.showMessageDialog(frame, aviso.getDescripcion(), aviso.getEstado(), JOptionPane.ERROR_MESSAGE);
    }

}
