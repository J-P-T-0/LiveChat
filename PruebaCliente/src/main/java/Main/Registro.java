package Main;

import Interfaz.GUI;
import Requests.Registrarse;
import Respuestas.Aviso;
import Respuestas.Respuesta;
import com.fasterxml.jackson.databind.ObjectMapper;

import javax.swing.*;
import java.awt.*;
import java.io.BufferedReader;
import java.io.PrintWriter;

public class Registro extends JFrame {
    private JTextField txtNombre;
    private JTextField txtTelefono;
    private JPasswordField txtContrasena;
    private JButton btnRegistrarse;

    //Cuestiones del server
    private static PrintWriter salida;
    private static BufferedReader entrada;
    private static ObjectMapper objectMapper;

    public Registro() {
        setTitle("Registro de Usuario");
        setSize(300, 200);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        conectarAServidor();
        initUI();
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

    private void initUI() {
        JLabel lblTelefono = new JLabel("Teléfono:");
        JLabel lblContrasena = new JLabel("Contraseña:");

        txtNombre = new JTextField();
        txtTelefono = new JTextField();
        txtContrasena = new JPasswordField();
        btnRegistrarse = new JButton("Registrarse");

        btnRegistrarse.addActionListener(e -> registrar());

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
    }

    private void registrar(){
        String telefono = txtTelefono.getText().trim();
        String contrasena = new String(txtContrasena.getPassword());
        String nombre = txtNombre.getText().trim();

        if (telefono.isEmpty() || contrasena.isEmpty() || nombre.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Completa todos los campos.");
            return;
        }

        try{
            String jsonRequest = objectMapper.writeValueAsString(new Registrarse(nombre,telefono,contrasena));
            System.out.println(jsonRequest);
            salida.println(jsonRequest);

            String jsonResponse = entrada.readLine();
            System.out.println(jsonResponse);
            Respuesta respuesta = objectMapper.readValue(jsonResponse, Respuesta.class);
            if(respuesta instanceof Aviso){
                Aviso aviso = (Aviso) respuesta;
                JOptionPane.showMessageDialog(this,aviso.getDescripcion(),aviso.getEstado(),JOptionPane.INFORMATION_MESSAGE);
                if(aviso.getEstado().equals("éxito")){
                    this.dispose();
                    new LoginC().setVisible(true);
                }
            }
        }catch (Exception e){
            System.out.println("Error al registrar: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new Registro().setVisible(true));
    }
}
