package Main;

import Interfaz.GUI;
import Respuestas.*;

import javax.swing.*;
import java.awt.*;
import java.net.URL;

import static Main.CreateRequests.RequestLogin;

public class ChatLogin extends JFrame {
    private JTextField txtTelefono;
    private JPasswordField txtPassword;
    private JButton btnLogin;
    private JButton btnRegistrar;
    private static ChatLogin frame;
    private Image imagenFondo;

    public ChatLogin() {
        conectarAServidor();
        initComponents();
        frame = this;
    }

    private void conectarAServidor() {
        try {
            Conexion.connect("147.185.221.28", 37296);
        } catch (Exception e) {
            GUI.MostrarAviso(new Aviso("Error", "Error al conectar con el servidor."));
        }
    }

    private void initComponents(){
        setTitle("Login");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1080, 720);
        setResizable(false);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        try {
            URL imgURL = getClass().getResource("/icons/tetopear.jpg");
            if (imgURL != null) {
                ImageIcon icon = new ImageIcon(imgURL);
                imagenFondo = icon.getImage().getScaledInstance(540, 720, Image.SCALE_SMOOTH);
            }
        } catch (Exception e) {
            System.err.println("No se cargo la fokin imagen " + e.getMessage());
        }

        JPanel panelImagen = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (imagenFondo != null) {
                    g.drawImage(imagenFondo, 0, 0, this);
                }
            }
        };
        panelImagen.setPreferredSize(new Dimension(540, 720));
        add(panelImagen, BorderLayout.WEST);

        JPanel panelDerecho = new JPanel(new BorderLayout());
        panelDerecho.setBackground(Color.decode("#1f2421"));

        JPanel panelFormulario = new JPanel(new GridLayout(5, 2, 10, 10));
        panelFormulario.setOpaque(false);
        panelFormulario.setBorder(BorderFactory.createEmptyBorder(100, 50, 100, 50));

        JLabel lbltelefono = new JLabel("Telefono");
        txtTelefono = new JTextField();
        txtTelefono.setPreferredSize(new Dimension(200, 30));
        lbltelefono.setForeground(Color.WHITE);
        panelFormulario.add(lbltelefono);
        panelFormulario.add(txtTelefono);
        lbltelefono.setFont(new Font("Verdana", Font.BOLD, 18));

        JLabel lblpass = new JLabel("Contraseña");
        txtPassword = new JPasswordField();
        txtPassword.setPreferredSize(new Dimension(200, 30));
        lblpass.setForeground(Color.WHITE);
        lblpass.setFont(new Font("Verdana", Font.BOLD, 18));
        panelFormulario.add(lblpass);
        panelFormulario.add(txtPassword);




        btnLogin = new JButton("Login");
        btnLogin.setFont(new Font("Verdana", Font.BOLD, 18));
        btnLogin.setBackground(Color.decode("#07436b"));
        btnLogin.setForeground(Color.WHITE);
        btnLogin.setOpaque(true);
        btnLogin.setBorderPainted(false);
        panelFormulario.add(btnLogin);
        // Panel separado para la parte de registro
        JPanel panelRegistro = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        panelRegistro.setBackground(Color.decode("#2e2e2e"));
        JLabel newregist = new JLabel("¿No tienes cuenta?");
        newregist.setForeground(Color.WHITE);
        btnRegistrar = new JButton("Registrar");
        btnRegistrar.setBackground(Color.decode("#5b0e73"));
        btnRegistrar.setForeground(Color.WHITE);
        btnRegistrar.setOpaque(true);
        btnRegistrar.setBorderPainted(false);
        btnRegistrar.addActionListener(_ -> {
            frame.setVisible(false);
            new Registro().setVisible(true);
        });
        panelRegistro.add(newregist);
        panelRegistro.add(btnRegistrar);

        // Se agrega al fondo del panel derecho
        panelDerecho.add(panelFormulario, BorderLayout.CENTER);
        panelDerecho.add(panelRegistro, BorderLayout.SOUTH);


        panelDerecho.add(panelFormulario, BorderLayout.CENTER);
        add(panelDerecho, BorderLayout.CENTER);

        setVisible(true);

        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                Conexion.disconnect();
                System.exit(0);
            }
        });
    }

    private boolean validarTelefono(String telefono) {
        return (!telefono.isBlank() && telefono.length() == 10 && telefono.matches("^[0-9]+$"));
    }

    private boolean validarPassword(String password) {
        return (!password.isBlank());
    }

    private void startLogin(){
        try {
            String telefono = txtTelefono.getText().trim();
            String password = new String(txtPassword.getPassword());

            if (!validarTelefono(telefono)) {
                JOptionPane.showMessageDialog(this, "El teléfono debe tener 10 caracteres", "Teléfono Inválido", JOptionPane.WARNING_MESSAGE);
                return;
            }

            if (!validarPassword(password)) {
                JOptionPane.showMessageDialog(this, "Ingrese una contraseña", "Contraseña Inválida", JOptionPane.ERROR_MESSAGE);
                return;
            }

            RequestLogin(telefono, password);
        } catch (Exception ex) {
            System.out.println("Hubo un problema con: " + ex.getMessage());
        }
    }

    public static void confirmLogin(LoginAuth loginAuth){
        frame.dispose();
        new GUI(loginAuth).setVisible(true);
    }

    public static void warningRegistro(Aviso aviso){
        if (aviso.getEstado().equals("éxito")) {
            JOptionPane.showMessageDialog(frame, aviso.getDescripcion(), "Error de Autenticación", JOptionPane.WARNING_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(frame, aviso.getDescripcion(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public static void main(String[] args) {
        new ChatLogin().setVisible(true);
    }
}
