package Main;

import Requests.Registrarse;
import Respuestas.Aviso;

import javax.swing.*;
import java.awt.*;
import java.net.URL;

import static Main.Conexion.*;
import static Main.CreateRequests.RequestRegistro;

public class Registro extends JFrame {
    private JTextField txtNombre;
    private JTextField txtTelefono;
    private JPasswordField txtContrasena;
    private JButton btnRegistrarse;
    private static Registro frame;
    private Image imagenFondo;

    public Registro() {
        setTitle("Registro de Usuario");
        setSize(300, 200);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        frame = this;
        initUI();
    }

    private void initUI() {
        setTitle("Registro");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(1080, 720);
        setResizable(false);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        try {
            URL imgURL = getClass().getResource("/icons/evilteto.jpg");
            if (imgURL != null) {
                ImageIcon icon = new ImageIcon(imgURL);
                imagenFondo = icon.getImage().getScaledInstance(540, 720, Image.SCALE_SMOOTH);
            }
        } catch (Exception e) {
            System.err.println("No se cargó la imagen: " + e.getMessage());
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
        add(panelImagen, BorderLayout.EAST);

        JPanel panelIzquierdo = new JPanel(new BorderLayout());
        panelIzquierdo.setBackground(Color.decode("#141c2b"));

        JButton btnVolver = new JButton("Volver al login");
        btnVolver.setFont(new Font("Verdana", Font.PLAIN, 14));
        btnVolver.setBackground(Color.decode("#333333"));
        btnVolver.setForeground(Color.WHITE);
        btnVolver.setBorderPainted(false);
        btnVolver.setFocusPainted(false);
        btnVolver.setOpaque(true);
        btnVolver.setPreferredSize(new Dimension(200, 40));
        btnVolver.addActionListener(e -> {
            frame.dispose();
            new ChatLogin().setVisible(true);
        });

        panelIzquierdo.add(btnVolver, BorderLayout.NORTH);

        JPanel panelFormulario = new JPanel(new GridLayout(4, 2, 10, 10));
        panelFormulario.setOpaque(false);
        panelFormulario.setBorder(BorderFactory.createEmptyBorder(100, 50, 100, 50));

        JLabel lblNombre = new JLabel("Nombre");
        lblNombre.setForeground(Color.WHITE);
        lblNombre.setFont(new Font("Verdana", Font.BOLD, 18));
        txtNombre = new JTextField();
        txtNombre.setPreferredSize(new Dimension(200, 30));
        panelFormulario.add(lblNombre);
        panelFormulario.add(txtNombre);

        JLabel lblTelefono = new JLabel("Teléfono");
        lblTelefono.setForeground(Color.WHITE);
        lblTelefono.setFont(new Font("Verdana", Font.BOLD, 18));
        txtTelefono = new JTextField();
        txtTelefono.setPreferredSize(new Dimension(200, 30));
        panelFormulario.add(lblTelefono);
        panelFormulario.add(txtTelefono);

        JLabel lblContrasena = new JLabel("Contraseña");
        lblContrasena.setForeground(Color.WHITE);
        lblContrasena.setFont(new Font("Verdana", Font.BOLD, 18));
        txtContrasena = new JPasswordField();
        txtContrasena.setPreferredSize(new Dimension(200, 30));
        panelFormulario.add(lblContrasena);
        panelFormulario.add(txtContrasena);

        btnRegistrarse = new JButton("Registrarse");
        btnRegistrarse.setFont(new Font("Verdana", Font.BOLD, 18));
        btnRegistrarse.setBackground(Color.decode("#4d1d35"));
        btnRegistrarse.setForeground(Color.WHITE);
        btnRegistrarse.setOpaque(true);
        btnRegistrarse.setBorderPainted(false);
        btnRegistrarse.setPreferredSize(new Dimension(200, 20));
        btnRegistrarse.addActionListener(e -> startRegistro());

        JPanel contenedorBoton = new JPanel(new BorderLayout());
        contenedorBoton.setOpaque(false);
        contenedorBoton.add(btnRegistrarse, BorderLayout.CENTER);

        panelFormulario.add(new JLabel());
        panelFormulario.add(contenedorBoton);

        panelIzquierdo.add(panelFormulario, BorderLayout.CENTER);
        add(panelIzquierdo, BorderLayout.CENTER);

        setVisible(true);

        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                dispose();
            }
        });
    }

    private boolean validarTelefono(String telefono) {
        return (!telefono.isBlank() && telefono.length() == 10 && telefono.matches("^[0-9]+$"));
    }

    private void startRegistro() {
        String telefono = txtTelefono.getText().trim();
        String contrasena = new String(txtContrasena.getPassword());
        String nombre = txtNombre.getText().trim();

        if (telefono.isEmpty() || contrasena.isEmpty() || nombre.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Completa todos los campos.");
            return;
        }

        if (!(validarTelefono(telefono))) {
            JOptionPane.showMessageDialog(this, "Error tel.");
            return;
        }

        RequestRegistro(nombre, telefono, contrasena);
    }

    public static void confirmRegistro(Aviso aviso) {
        JOptionPane.showMessageDialog(frame, aviso.getDescripcion(), aviso.getEstado(), JOptionPane.INFORMATION_MESSAGE);
        if (aviso.getEstado().equals("éxito")) {
            frame.dispose();
            new ChatLogin().setVisible(true);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new Registro().setVisible(true));
    }
}
