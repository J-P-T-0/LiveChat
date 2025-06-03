package Main;

import Requests.Registrarse;
import Respuestas.Aviso;
import Respuestas.Respuesta;

import javax.swing.*;
import java.awt.*;

import static Main.Conexion.*;
import static Main.CreateRequests.RequestClose;
import static Main.CreateRequests.RequestRegistro;

public class Registro extends JFrame {
    private JTextField txtNombre;
    private JTextField txtTelefono;
    private JPasswordField txtContrasena;
    private JButton btnRegistrarse;
    private static Registro frame;

    public Registro() {
        setTitle("Registro de Usuario");
        setSize(300, 200);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        initUI();
        frame = this;
    }

    private void initUI() {
        JLabel lblTelefono = new JLabel("Teléfono:");
        JLabel lblContrasena = new JLabel("Contraseña:");

        txtNombre = new JTextField();
        txtTelefono = new JTextField();
        txtContrasena = new JPasswordField();
        btnRegistrarse = new JButton("Registrarse");

        btnRegistrarse.addActionListener(e -> startRegistro());

        JPanel panel = new JPanel(new GridLayout(4, 2));
        panel.add(new JLabel("Nombre:"));
        panel.add(txtNombre);
        panel.add(lblTelefono);
        panel.add(txtTelefono);
        panel.add(lblContrasena);
        panel.add(txtContrasena);
        panel.add(new JLabel());  // Espacio vacío
        panel.add(btnRegistrarse);

        add(panel);

        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                frame.dispose();
            }
        });
    }

    private boolean validarTelefono(String telefono) {
        return (!telefono.isBlank() && telefono.length()==10 && telefono.matches("^[0-9]+$"));
    }

    private void startRegistro(){
        String telefono = txtTelefono.getText().trim();
        String contrasena = new String(txtContrasena.getPassword());
        String nombre = txtNombre.getText().trim();

        if (telefono.isEmpty() || contrasena.isEmpty() || nombre.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Completa todos los campos.");
            return;
        }

        if(!(validarTelefono(telefono))){
            JOptionPane.showMessageDialog(this, "Error tel.");
            return;
        }

        RequestRegistro(nombre, telefono, contrasena);
    }

    public static void confirmRegistro(Aviso aviso){
        JOptionPane.showMessageDialog(frame, aviso.getDescripcion(), aviso.getEstado(), JOptionPane.INFORMATION_MESSAGE);
        if(aviso.getEstado().equals("éxito")){
            frame.dispose();
        }
    }
}
