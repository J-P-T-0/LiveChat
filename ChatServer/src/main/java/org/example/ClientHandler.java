package org.example;

//Importar los requests
import Requests.*;

import java.io.*;
import java.net.Socket;
import java.sql.*;

//importar librerias para JSON
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.ArrayNode;

// Clase que maneja cada conexion de cliente en un hilo separado
public class ClientHandler extends Thread {
    // Socket para comunicación con el cliente
    private Socket socket;
    // Buffer para leer mensajes del cliente
    private BufferedReader entrada;
    // Writer para enviar mensajes al cliente
    private PrintWriter salida;
    // Conexion a la base de datos
    private Connection conn;
    // ID del usuario autenticado, null si no está autenticado
    private Integer usuarioAutenticadoId = null;

    // Al inicio de la clase
    private final ObjectMapper objectMapper = new ObjectMapper();

    // Constructor que recibe el socket del cliente
    public ClientHandler(Socket socket) {
        this.socket = socket;
    }


    // Metodo principal que se ejecuta en el hilo
    public void run() {
        try {
            //System.out.println("Local: has entrado a la validación");
            // Inicializa los streams de entrada/salida
            try {
                entrada = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                salida = new PrintWriter(socket.getOutputStream(), true);
            } catch (IOException e) {
                System.out.println("Entrada/salida no disponible: " + e.getMessage());
            }

            // Confirmar conexion
            try {
                ObjectNode datos = objectMapper.createObjectNode();
                datos.put("estado", "success");
                enviarRespuesta("exito", "Conexion al servidor establecida", datos);
            } catch (Exception e) {
                System.out.println("Object node error: " + e.getMessage());
            }


            try {
                // Configura propiedades SSL en el servidor para mysql (contenedor con la base de datos)
                System.setProperty("javax.net.ssl.trustStore", "/app/truststore/truststore.jks");

                /*
                System.setProperty("javax.net.ssl.trustStorePassword", System.getenv("TRUSTSTORE_PASS"));
                // Obtiene credenciales de base de datos de variables de entorno dentro del contenedor
                String url = System.getenv("DB_URL");
                String user = System.getenv("DB_USER");
                String password = System.getenv("DB_PASS");*/

                // Esta es la versión para la configuración local, comentar o descomentar según necesidad
                System.setProperty("javax.net.ssl.trustStorePassword", "cb.pp3_UPSLP");
                String url = "jdbc:mysql://127.0.0.1:3306/appdb";
                String user = "root";
                String password = "";

                // Establece conexión con la base de datos
                conn = DriverManager.getConnection(url, user, password);
            } catch (SQLException e) {
                System.out.println("Error al conectar con la base de datos: " + e.getMessage());
                System.exit(0);
            }

            // Ciclo principal de lectura de comandos
            String linea= null;
            while ((linea = entrada.readLine()) != null) {
                try {
                    // migrar_Todo a JSON :,,,D
                    //Convierte el string que recibe los mensajes de clientes y los convierte al formato Json (un arbol de objetos)

                    //Almacena el request pedido de lado de cliente
                    Request newRequest = objectMapper.readValue(linea, Request.class);

                    // Verificación de autenticación
                    if (!(newRequest instanceof Login) && !(newRequest instanceof Registrarse)
                            && usuarioAutenticadoId == null) {
                        enviarRespuesta("error", "Debes autenticarte primero");
                        continue;
                    }

                    switch (newRequest) {
                        case Login loginRequest->login(loginRequest);

                        case Registrarse registroRequest ->registrar(registroRequest);

                        case GetConversaciones _ -> cargarConversaciones();

                        case GetMensajes mensajesRequest -> getMensajes(mensajesRequest);
/*
                        case "ENVIAR_MENSAJE":
                            //enviarMensaje(
                            //        comando.get("conversacionId").asInt(),
                            //       comando.get("mensaje").asText()
                            //);
                            break;

                        case "CREAR_CONVERSACION": break;

                        case "CREAR_GRUPO":
                            break;
                        case "MENSAJE_PRIVADO":
                            break;
                        case "MARCAR_MENSAJE_COMO_LEIDO":
                            break;
                        case "OBTENER_ESTADO_MENSAJE":
                            break;*/
                        default-> enviarRespuesta("error", "comando invalido");
                    }
                } catch (Exception e) {
                    ObjectNode error = objectMapper.createObjectNode();
                    error.put("estado", "error");
                    error.put("descripcion", "Error al procesar comando: " + e.getMessage());
                    salida.println(error.toString());
                }
            }

            // Cierra conexiones al terminar
            conn.close();
            socket.close();
        } catch (Exception e) {
            System.err.println("Error con cliente: " + e.getMessage());
        }
    }

    // metoodo login con json
    private void login(Login request) {
        try {
            if (autenticarUsuario(request.getTelefono(), request.getPassword())) {
                ObjectNode datos = objectMapper.createObjectNode();
                datos.put("usuarioID", usuarioAutenticadoId).asText();
                enviarRespuesta("exito", "login realizado correctamente", datos);
            } else {
                enviarRespuesta("error", "nombre o contrasenia incorrectos");
            }
        } catch (Exception e) {
            enviarRespuesta("error", "Error en login: " + e.getMessage());
        }
    }

    // metodo para registrar un nuevo usuario con JSON
    private void registrar(Registrarse request) {
        try {
            if(existeTelefono(request.getTelefono())){
                throw new Exception("El número ya está registrado, pedir número nuevamente.");
            }
            usuarioRegistrado(request.getTelefono(), request.getNombre(), request.getContrasena());
        } catch (Exception e) {
            enviarRespuesta("error", "Error al registrar usuario: " + e.getMessage());
        }
    }

    // Ejemplo de cargarConversaciones modificado
    private void cargarConversaciones() throws SQLException {
        String sql = """
                SELECT c.id, c.nombre, c.isGrupo 
                FROM conversaciones c
                INNER JOIN conversacion_usuario p ON c.id = p.conversacion_id
                WHERE p.usuario_id = ?
                """;
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, usuarioAutenticadoId);
            ResultSet rs = stmt.executeQuery();

            ArrayNode conversaciones = objectMapper.createArrayNode();//array que contiene multiples objetos en este caso conversaciones
            while (rs.next()) {
                //conv es como crear un objeto de tipo conversacion
                ObjectNode conv = objectMapper.createObjectNode();
                conv.put("id", rs.getInt("id"));
                conv.put("nombre", rs.getString("nombre"));
                conv.put("tipo", rs.getBoolean("isGrupo") ? "Grupo" : "Individual");
                conversaciones.add(conv);
            }

            ObjectNode datos = objectMapper.createObjectNode();//Se vuelve a envolver en un objeto de tipo object node para enviarlo
            //se accede de manera similar a un arraylist  rootNode.get("conversaciones").get(0);
            //Json es un arbol de objetos 0.0
            datos.set("conversaciones", conversaciones);
            enviarRespuesta("success", "Conversaciones recuperadas con éxito", datos);
        }
    }
    // metodo que carga las conversaciones del usuario


    private void getMensajes(GetMensajes request) throws SQLException {
        String sql = """
                SELECT u.nombre, m.mensaje, m.fecha_envio 
                FROM mensajes m
                JOIN usuarios u ON m.remitente_id = u.id
                WHERE m.conversacion_id = ? 
                ORDER BY m.fecha_envio
                """;
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, request.getConversacionId());
            ResultSet rs = stmt.executeQuery();

            ArrayNode mensajesArray = objectMapper.createArrayNode();
            while (rs.next()) {
                ObjectNode mensaje = objectMapper.createObjectNode();
                mensaje.put("nombre", rs.getString("nombre"));
                mensaje.put("mensaje", rs.getString("mensaje"));
                mensaje.put("fecha_envio", rs.getTimestamp("fecha_envio").toString());
                mensajesArray.add(mensaje);
            }

            ObjectNode datos = objectMapper.createObjectNode();
            datos.set("mensajes", mensajesArray);
            enviarRespuesta("success", "Mensajes recuperados con éxito", datos);
        }
    }

    private void enviarMensaje(int conversacionId, String mensaje) throws SQLException {
        String sql = "INSERT INTO mensajes (conversacion_id, remitente_id, mensaje, fecha_envio) VALUES (?, ?, ?, NOW())";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, conversacionId);
            stmt.setInt(2, usuarioAutenticadoId);
            stmt.setString(3, mensaje);
            int filas = stmt.executeUpdate();

            if (filas > 0) {
                enviarRespuesta("success", "Mensaje enviado exitosamente");
            } else {
                enviarRespuesta("error", "Error al enviar mensaje");
            }
        }
    }

    /*
    *
    *     //SECCIÓN DE VALIDACIONES PARA LAS DEMÁS FUNCIONES
    *
    */

    //Valida si el usuario logró ser registrado o no
    private void usuarioRegistrado(String nombre, String telefono, String contrasenia) throws SQLException {
        String sql = "INSERT INTO usuarios (nombre, telefono, contrasenia) VALUES (?, ?, ?)";
        PreparedStatement stmt = conn.prepareStatement(sql);
        stmt.setString(1, nombre);
        stmt.setString(2, telefono);
        stmt.setString(3, contrasenia);
        stmt.executeUpdate();
    }

    //Valida si ya existe algún usuario con el mismo teléfono
    private boolean existeTelefono (String telefono) throws SQLException{
        String checkTel = "SELECT id FROM usuarios WHERE telefono = ?";
        PreparedStatement checkStmt = conn.prepareStatement(checkTel);
        checkStmt.setString(1, telefono);
        ResultSet rs = checkStmt.executeQuery();
        return rs.next();
    }


    // metodo para autenticar usuario
    private boolean autenticarUsuario(String telefono, String password) throws SQLException {
        String sql = "SELECT id FROM usuarios WHERE telefono = ? AND contrasenia = ?";
        PreparedStatement pstmt = conn.prepareStatement(sql);
        pstmt.setString(1, telefono);
        pstmt.setString(2, password);
        ResultSet rs = pstmt.executeQuery();
        // Si se encuentra un usuario con esos parametros, retorna verdadero y actualiza el parametro usuarioAutenticadoId
        if (rs.next()) {
            usuarioAutenticadoId = rs.getInt("id");
            return true;
        }
        return false;
    }




    // metodo para enviar respuestas simples como error o exito con JSON
    private void enviarRespuesta(String estado, String descripcion) {
        ObjectNode respuesta = objectMapper.createObjectNode();
        respuesta.put("estado", estado);
        respuesta.put("descripcion", descripcion);
        salida.println(respuesta.toString());
    }

    // metodo para enviar respuestas con campos personalizados como id_conversacion o fehca_envio con JSON
    private void enviarRespuesta(String estado, String descripcion, ObjectNode datosAdicionales) {
        ObjectNode respuesta = objectMapper.createObjectNode();
        respuesta.put("estado", estado);
        respuesta.put("descripcion", descripcion);
        respuesta.setAll(datosAdicionales); // Añade todos los datos adicionales
        salida.println(respuesta.toString());
    }

// metodo para marcar mensaje como leído
private void marcarMensajeComoLeido(int mensajeId) throws SQLException {
    // verificar que el mensaje pertenezca a una conversación del usuario logeado
    String checkSql = """
            SELECT m.id 
            FROM mensajes m
            JOIN conversacion_usuario cu ON m.conversacion_id = cu.conversacion_id
            WHERE m.id = ? AND cu.usuario_id = ?
            """;
            
    try (PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {
        checkStmt.setInt(1, mensajeId);
        checkStmt.setInt(2, usuarioAutenticadoId);
        
        if (!checkStmt.executeQuery().next()) {
            enviarRespuesta("error", "mensaje no pertenece a ninguna de tus conversaciones");
            return;
        }
        
        // Si pertenece a alguna conversacion del usuario
        String updateSql = "UPDATE mensajes SET fecha_lectura = NOW() WHERE id = ?";
        try (PreparedStatement updateStmt = conn.prepareStatement(updateSql)) {
            updateStmt.setInt(1, mensajeId);
            int filasActualizadas = updateStmt.executeUpdate();
            
            if (filasActualizadas > 0) {
                enviarRespuesta("success", "Mensaje marcado como leído");
            } else {
                enviarRespuesta("error", "No se pudo marcar el mensaje como leído");
            }
        }
    }
}

// metodo para obtener el estado del mensaje
private void obtenerEstadoMensaje(int mensajeId) throws SQLException {
    String sql = """
            SELECT m.id, m.isEntregado, m.fecha_lectura, m.fecha_envio
            FROM mensajes m
            JOIN conversacion_usuario cu ON m.conversacion_id = cu.conversacion_id
            WHERE m.id = ? AND cu.usuario_id = ?
            """;
            
    try (PreparedStatement stmt = conn.prepareStatement(sql)) {
        stmt.setInt(1, mensajeId);
        stmt.setInt(2, usuarioAutenticadoId);
        ResultSet rs = stmt.executeQuery();
        
        if (rs.next()) {
            ObjectNode datos = objectMapper.createObjectNode();
            String estadoMensaje;
            String fechaLectura = "";
            
            // Evaluar el estado
            if (rs.getTimestamp("fecha_lectura") != null) {
                estadoMensaje = "leido";
                fechaLectura = rs.getTimestamp("fecha_lectura").toString();
            } else if (rs.getBoolean("isEntregado")) {
                estadoMensaje = "entregado";
            } else {
                estadoMensaje = "no entregado";
            }
            
            datos.put("estadoMensaje", estadoMensaje);
            datos.put("fechaLectura", fechaLectura);
            datos.put("fechaEnvio", rs.getTimestamp("fecha_envio").toString());
            
            enviarRespuesta("success", "Estado del mensaje recuperado", datos);
        } else {
            enviarRespuesta("error", "Este mensaje o no existe");
        }
    }
}
}