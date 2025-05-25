package org.example;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.*;
import java.net.Socket;
import java.security.KeyStore;

public class Cliente extends JFrame {
    private JTable tableMensajes;
    private DefaultTableModel modeloMensajes;
    private JTextField txtMensaje;
    private PrintWriter salida;
    private BufferedReader entrada;
    private Socket socket;

    public Cliente() {
        setTitle("Cliente");
        setSize(600, 400);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        initUI();
        conectarAServidor();
    }

    private void initUI() {
        modeloMensajes = new DefaultTableModel(new Object[]{"Mensajes"}, 0);
        tableMensajes = new JTable(modeloMensajes);
        txtMensaje = new JTextField();
        JButton btnEnviar = new JButton("Enviar");

        btnEnviar.addActionListener(e -> enviarMensaje());

        JPanel panelInferior = new JPanel(new BorderLayout());
        panelInferior.add(txtMensaje, BorderLayout.CENTER);
        panelInferior.add(btnEnviar, BorderLayout.EAST);

        add(new JScrollPane(tableMensajes), BorderLayout.CENTER);
        add(panelInferior, BorderLayout.SOUTH);
    }

    private void conectarAServidor() {
        try {
            // ip publica:puerto
            socket = new Socket("147.185.221.28", 37296); // <- actualizar si cambia el tunel (paso)
            salida = new PrintWriter(socket.getOutputStream(), true);
            entrada = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            new Thread(() -> {
                try {
                    String mensajeServer;
                    while ((mensajeServer = entrada.readLine()) != null) {
                        modeloMensajes.addRow(new Object[]{mensajeServer});//aniade nuevas filas a la tabla con la respuesta del server
                    }
                } catch (IOException ex) {
                    modeloMensajes.addRow(new Object[]{"Error al leer mensaje de server"+ ex.getMessage()});
                }
            }).start();

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error al conectar al servidor: " + e.getMessage());
        }
    }

    //metodo llamado por el boton enviar
    private void enviarMensaje() {
        String mensaje = txtMensaje.getText();
        if (!mensaje.isEmpty()) {
            salida.println(mensaje);
            txtMensaje.setText("");
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new Cliente().setVisible(true));
    }
}
