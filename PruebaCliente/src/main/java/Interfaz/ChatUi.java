package Interfaz;

import Requests.*;
import Respuestas.*;
import com.fasterxml.jackson.databind.ObjectMapper;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.*;
import java.net.Socket;

public class ChatUi extends JFrame {
    private JTextField txtNumeroBuscar, txtMensaje;
    private JButton btnBuscar, btnEnviar;
    private JTable tablaMensajes;
    private DefaultTableModel modeloMensajes;

    private Socket socket;
    private PrintWriter salida;
    private BufferedReader entrada;
    private ObjectMapper mapper = new ObjectMapper();

    private String token;
    private int idUsuario;
    private int idConversacion;

    public ChatUi() {
        setTitle("Chat App");
        setSize(600, 500);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        initUI();
        conectarAServidor();
       // mostrarLoginEmergente();
    }

    public ChatUi(LoginAuth loginInfo, Socket socket) {
        this.socket = socket;

        setTitle("Chat App");
        setSize(600, 500);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        initUI();
        conectarAServidor();
       // mostrarLoginEmergente();

    }

    private void initUI() {
        setLayout(new BorderLayout(10, 10));

        // Panel superior
        JPanel panelTop = new JPanel(new BorderLayout(10, 10));
        txtNumeroBuscar = new JTextField();
        btnBuscar = new JButton("Buscar contacto");

        panelTop.add(new JLabel("Número de usuario:"), BorderLayout.WEST);
        panelTop.add(txtNumeroBuscar, BorderLayout.CENTER);
        panelTop.add(btnBuscar, BorderLayout.EAST);

        add(panelTop, BorderLayout.NORTH);

        // Tabla de mensajes
        modeloMensajes = new DefaultTableModel(new Object[]{"Mensajes"}, 0);
        tablaMensajes = new JTable(modeloMensajes);
        tablaMensajes.setFont(new Font("SansSerif", Font.PLAIN, 14));
        add(new JScrollPane(tablaMensajes), BorderLayout.CENTER);

        // Panel inferior - Enviar mensaje
        JPanel panelBottom = new JPanel(new BorderLayout(10, 10));
        txtMensaje = new JTextField();
        btnEnviar = new JButton("Enviar");

        panelBottom.add(txtMensaje, BorderLayout.CENTER);
        panelBottom.add(btnEnviar, BorderLayout.EAST);

        add(panelBottom, BorderLayout.SOUTH);

        // Listeners
        btnBuscar.addActionListener(e -> buscarUsuarioYCrearConversacion());
        btnEnviar.addActionListener(e -> enviarMensaje());
    }

    private void mostrarLoginEmergente() {
        JTextField txtTelefono = new JTextField();
        JPasswordField txtContrasena = new JPasswordField();
        Object[] campos = {
                "Teléfono:", txtTelefono,
                "Contraseña:", txtContrasena
        };

        int opcion;
        do {
            opcion = JOptionPane.showConfirmDialog(this, campos, "Login", JOptionPane.OK_CANCEL_OPTION);
            if (opcion == JOptionPane.OK_OPTION) {
                if (realizarLogin(txtTelefono.getText(), new String(txtContrasena.getPassword()))) {
                    break;
                }
            } else {
                System.exit(0);
            }
        } while (true);
    }

    private void conectarAServidor() {
        try {
            salida = new PrintWriter(socket.getOutputStream(), true);
            entrada = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        } catch (Exception e) {
            mostrarError("Error al conectar con el servidor.");
        }
    }

    private boolean realizarLogin(String telefono, String contrasena) {
        try {
            Login login = new Login(telefono, contrasena);
            salida.println(mapper.writeValueAsString(login));

            String respuesta = entrada.readLine();
            LoginAuth auth = mapper.readValue(respuesta, LoginAuth.class);
            if (auth.getNombre() != null) {
                this.token = auth.getToken();
                this.idUsuario = auth.getIdUsuario();
                mostrarMensaje("Bienvenido, " + auth.getNombre());
                return true;
            } else {
                mostrarError("Login fallido.");
                return false;
            }
        } catch (Exception e) {
            mostrarError("Error en login: " + e.getMessage());
            return false;
        }
    }

    private void buscarUsuarioYCrearConversacion() {
        try {
            CrearConversacionIndividual crear = new CrearConversacionIndividual(
                    txtNumeroBuscar.getText(), token, "1000000000");
            salida.println(mapper.writeValueAsString(crear));

            String respuesta = entrada.readLine();
            ReturnConvID conv = mapper.readValue(respuesta, ReturnConvID.class);
            this.idConversacion = conv.getConvID();
            mostrarMensaje("Conversación creada con ID: " + idConversacion);
        } catch (Exception e) {
            mostrarError("Error al crear conversación: " + e.getMessage());
        }
    }

    private void enviarMensaje() {
        try {
            String msg = txtMensaje.getText();
            if (msg.isEmpty()) return;

            EnviarMensaje mensaje = new EnviarMensaje(msg, idConversacion);
            salida.println(mapper.writeValueAsString(mensaje));

            String respuesta = entrada.readLine();
            Aviso aviso = mapper.readValue(respuesta, Aviso.class);

            modeloMensajes.insertRow(0, new Object[]{msg});
            txtMensaje.setText("");
        } catch (Exception e) {
            mostrarError("Error al enviar mensaje: " + e.getMessage());
        }
    }

    private void mostrarMensaje(String msg) {
        modeloMensajes.insertRow(0, new Object[]{msg});
    }

    private void mostrarError(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Error", JOptionPane.ERROR_MESSAGE);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new ChatUi().setVisible(true));
    }
}
