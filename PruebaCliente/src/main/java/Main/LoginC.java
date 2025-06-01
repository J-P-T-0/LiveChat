package Main;

import Interfaz.ChatUi;
import Interfaz.GUI;
import Requests.*;
import Respuestas.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import javax.swing.*;
import java.awt.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class LoginC extends JFrame {
    private JTextField txtTelefono;
    private JPasswordField txtPassword;
    private JButton btnLogin;
    private JButton btnRegistrar;

    //Servidor
    private PrintWriter salida;
    private BufferedReader entrada;
    private Socket socket;
    private final ObjectMapper objectMapper = new ObjectMapper(); // Agregar al inicio de la clase

    public LoginC() {
        initComponents();
        conectarAServidor();
    }

    private void initComponents(){
        setTitle("Login");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setSize(400, 250);
        setLayout(new GridLayout(4,2));

        add(new JLabel("Telefono"));
        txtTelefono = new JTextField();
        add(txtTelefono);

        add(new JLabel("Password"));
        txtPassword = new JPasswordField();
        add(txtPassword);

        add(new JLabel("Ingresar: "));
        btnLogin = new JButton("Login");
        btnLogin.addActionListener(e -> login());
        add(btnLogin);

        add(new JLabel("No te has registrado?"));
        btnRegistrar = new JButton("Registrar");
        btnRegistrar.addActionListener(e -> {});
        add(btnRegistrar);
    }

    private boolean validarTelefono(String telefono) {
        return (!telefono.isBlank() && telefono.length()==10 && telefono.matches("^[0-9]+$"));
    }

    private boolean validarPassword(String password) {
        return (!password.isBlank());
    }

    private void login(){
        try{
            String telefono = txtTelefono.getText().trim();
            String password = txtPassword.getText().trim();

            if(!(validarPassword(password) && validarTelefono(telefono))){
                JOptionPane.showMessageDialog(this,"Error al ingresar el teléfono o la contraseña","Error",JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Convertir a JSON y enviar
            String jsonRequest = objectMapper.writeValueAsString(new Login(telefono,password));
            salida.println(jsonRequest);

            // Recibir y procesar respuesta
            String respuesta = entrada.readLine();
            System.out.println("Recibido: " + respuesta);
            Respuesta respuestaObj = objectMapper.readValue(respuesta, Respuesta.class);
            if(respuestaObj instanceof LoginAuth){
                LoginAuth loginAuth = (LoginAuth) respuestaObj;
                this.dispose();
                //new ChatUi(loginAuth, socket).setVisible(true);
                new GUI(loginAuth, socket).setVisible(true);
            }else if (respuestaObj instanceof Aviso){
                Aviso aviso = (Aviso) respuestaObj;
                JOptionPane.showMessageDialog(this,aviso.getDescripcion(),aviso.getEstado(),JOptionPane.ERROR_MESSAGE);
                this.txtPassword.setText("");
                this.txtTelefono.setText("");
            }
        }catch(Exception ex){
            System.out.println("Hubo un problema con: "+ex.getMessage());
        }
    }

    private void conectarAServidor() {
        try {
            // ip publica:puerto
            socket = new Socket("147.185.221.28", 37296); // <- actualiza si cambia el túnel
            salida = new PrintWriter(socket.getOutputStream(), true);
            entrada = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error al conectar al servidor: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new LoginC().setVisible(true));
    }
}
