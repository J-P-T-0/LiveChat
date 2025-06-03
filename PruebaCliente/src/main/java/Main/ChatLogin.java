package Main;

import Interfaz.GUI;
import Respuestas.*;
import org.w3c.dom.ls.LSOutput;

import javax.swing.*;
import java.awt.*;

import static Main.CreateRequests.RequestLogin;

public class ChatLogin extends JFrame {
    private JTextField txtTelefono;
    private JPasswordField txtPassword;
    private JButton btnLogin;
    private JButton btnRegistrar;
    private static ChatLogin frame;

    public ChatLogin() {
        conectarAServidor();
        initComponents();
        frame = this;
    }

    private void conectarAServidor() {
        try {
            Conexion.connect("147.185.221.28", 37296);
            //Conexion.connect("127.0.0.1", 1234);

        } catch (Exception e) {
            GUI.MostrarAviso(new Aviso("Error", "Error al conectar con el servidor."));
        }
    }

    private void initComponents(){
        setTitle("Login");
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        setSize(400, 250);
        setLayout(new GridLayout(4,2));
        setResizable(false);
        setLocationRelativeTo(null);

        add(new JLabel("Telefono"));
        txtTelefono = new JTextField();
        add(txtTelefono);

        add(new JLabel("Password"));
        txtPassword = new JPasswordField();
        add(txtPassword);

        add(new JLabel("Ingresar: "));
        btnLogin = new JButton("Login");
        btnLogin.addActionListener(_ -> startLogin());
        add(btnLogin);

        add(new JLabel("No te has registrado?"));
        btnRegistrar = new JButton("Registrar");
        btnRegistrar.addActionListener(_ -> new Registro().setVisible(true));
        add(btnRegistrar);

        setVisible(true);

        addWindowListener(new java.awt.event.WindowAdapter() {

            @Override
            public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                System.out.println("Closing window");
                Conexion.disconnect();
                System.exit(0);
            }
        });
    }

    private boolean validarTelefono(String telefono) {
        return (!telefono.isBlank() && telefono.length()==10 && telefono.matches("^[0-9]+$"));
    }

    private boolean validarPassword(String password) {
        return (!password.isBlank());
    }

    private void startLogin(){
        try{
            String telefono = txtTelefono.getText().trim();
            String password = new String(txtPassword.getPassword());

            if(!validarTelefono(telefono)){
                JOptionPane.showMessageDialog(this,"El teléfono debe tener 10 caracteres","Teléfono Inválido",JOptionPane.WARNING_MESSAGE);
                return;
            }

            if(!validarPassword(password)){
                JOptionPane.showMessageDialog(this,"Ingrese una contraseña","Contraseña Inválida",JOptionPane.ERROR_MESSAGE);
            }

            RequestLogin(telefono, password);

        }catch(Exception ex){
            System.out.println("Hubo un problema con: " + ex.getMessage());
        }
    }

    public static void confirmLogin(LoginAuth loginAuth){
        frame.dispose();
        new GUI(loginAuth).setVisible(true);
    }

    public static void warningRegistro(Aviso aviso){
        if(aviso.getEstado().equals("éxito")){
            JOptionPane.showMessageDialog(frame, aviso.getDescripcion(), "Error de Autenticación", JOptionPane.WARNING_MESSAGE);
        }else{
            JOptionPane.showMessageDialog(frame, aviso.getDescripcion(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public static void main(String[] args) {
        new ChatLogin().setVisible(true);
    }

}
