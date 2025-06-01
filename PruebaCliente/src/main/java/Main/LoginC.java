package Main;

import Interfaz.GUI;
import Requests.*;
import Respuestas.*;
import com.fasterxml.jackson.databind.ObjectMapper;

import javax.swing.*;
import java.awt.*;
import java.io.BufferedReader;
import java.io.PrintWriter;

public class LoginC extends JFrame {
    private JTextField txtTelefono;
    private JPasswordField txtPassword;
    private JButton btnLogin;
    private JButton btnRegistrar;

    //Servidor
    private PrintWriter salida;
    private BufferedReader entrada;
    private ObjectMapper objectMapper;
    public static ConexionServer conexionServer;

    public LoginC() {
        conectarAServidor();
        initComponents();
    }

    private void conectarAServidor() {
        try {
            //conexionServer = new ConexionServer("147.185.221.28", 37296);
            conexionServer = new ConexionServer("127.0.0.1", 1234);
            this.entrada= conexionServer.getEntrada();
            this.salida= conexionServer.getSalida();
            this.objectMapper= conexionServer.getObjectMapper();
        } catch (Exception e) {
            GUI.mostrarError("Error al conectar con el servidor.");
        }
    }

    private void initComponents(){
        setTitle("Login");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setSize(400, 250);
        setLayout(new GridLayout(4,2));
        setResizable(false);

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
        btnRegistrar.addActionListener(e -> {new Registro().setVisible(true); this.dispose();});
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
                new GUI(loginAuth).setVisible(true);
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

    public static void main(String[] args) {
        SwingUtilities.invokeLater(()-> new LoginC().setVisible(true));
    }

}
