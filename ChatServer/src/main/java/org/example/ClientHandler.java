package org.example;


import java.io.*;
import java.net.Socket;
import java.sql.*;
import java.util.ArrayList;

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
    private poolConexiones poolConexiones;
    // ID del usuario autenticado, null si no esta autenticado
    private Integer usuarioActualID = null;

    // Es la clase principa de JSon que transforma objetos de java en JSON y viceversa
    private final ObjectMapper traductorJson = new ObjectMapper();

    // Constructor que recibe el socket del cliente
    public ClientHandler(Socket socket, poolConexiones poolConexiones) {
        this.socket = socket;
        this.poolConexiones = poolConexiones;
    }


    // Metodo principal que se ejecuta en el hilo
    public void run() {
        try {
            entrada = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            salida = new PrintWriter(socket.getOutputStream(), true);
            
            ObjectNode datos = traductorJson.createObjectNode();
            datos.put("comandos", "REGISTRARSE, LOGIN, GET_CONVERSACIONES, GET_MENSAJES, ENVIAR_MENSAJE, CREAR_CONVERSACION, CREAR_GRUPO, MENSAJE_PRIVADO");
            enviarRespuesta("exito", "Conexion al servidor establecida", datos);

            String linea;
            while ((linea = entrada.readLine()) != null) {
                try {
                    ObjectNode comando = (ObjectNode) traductorJson.readTree(linea);
                    String tipoComando = comando.get("comando").asText().toUpperCase();

                    // Verificación de autenticación
                    if (!tipoComando.equals("LOGIN") && !tipoComando.equals("REGISTRARSE")
                            && usuarioActualID == null) {
                        enviarRespuesta("error", "No puedes acceder al servidor sin estar autenticado");
                        continue;
                    }

                    switch (tipoComando) {
                        case "LOGIN":
                            login(comando.get("telefono").asText(), comando.get("password").asText());
                            break;

                        case "REGISTRARSE":
                            //Al registrar usuario debe volver a mostrarse LOGIN para acceder
                            registrar(comando.get("telefono").asText(), comando.get("nombre").asText(), comando.get("password").asText());
                            break;

                        case "GET_CONVERSACIONES":
                            cargarConversaciones();
                            break;

                        case "GET_MENSAJES":
                            getMensajes(comando.get("conversacionId").asInt());
                            break;

                        case "ENVIAR_MENSAJE":
                            enviarMensaje(
                                    comando.get("conversacionId").asInt(),
                                    comando.get("mensaje").asText()
                            );
                            break;

                        case "CREAR_CONVERSACION":
                            break;
                        case "CREAR_GRUPO":

                            break;
                        case "MENSAJE_PRIVADO":

                            break;
                        case "MARCAR_MENSAJE_COMO_LEIDO":
                            marcarMensajeComoLeido(comando.get("mensajeId").asInt());
                            break;
                        case "OBTENER_ESTADO_MENSAJE":
                            obtenerEstadoMensaje(comando.get("mensajeId").asInt());
                            break;
                        default:
                            enviarRespuesta("error", "comando invalido");
                    }
                } catch (Exception e) {
                    enviarRespuesta("error", "Error al procesar comando: " + e.getMessage());
                }
            }
        } catch (Exception e) {
            System.err.println("Error con cliente: " + e.getMessage());
        } finally {
            try {
                socket.close();
            } catch (IOException e) {
                System.err.println("Error al cerrar el socket: " + e.getMessage());
            }
        }
    }

    // metoodo login con json
    private void login(String telefono, String password) {
        try {
            if (autenticarUsuario(telefono, password)) {
                enviarRespuesta("exito", "login realizado correctamente");
            } else {
                enviarRespuesta("error", "nombre o contrasenia incorrectos");
            }
        } catch (Exception e) {
            enviarRespuesta("error", "Error en login: " + e.getMessage());
        }
    }

private void cargarConversaciones() throws SQLException {
    try (Connection conn = poolConexiones.obtenerConexion()) {
        String sql = """
                SELECT c.id, c.nombre, c.isGrupo FROM conversaciones c
                INNER JOIN conversacion_usuario p ON c.id = p.conversacion_id
                WHERE p.usuario_id = ?
                """;
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, usuarioActualID);
            ResultSet rs = stmt.executeQuery();

            ArrayNode conversaciones = traductorJson.createArrayNode();
            while (rs.next()) {
                ObjectNode conv = traductorJson.createObjectNode();
                conv.put("id", rs.getInt("id"));
                conv.put("nombre", rs.getString("nombre"));
                conv.put("tipo", rs.getBoolean("isGrupo") ? "Grupo" : "Individual");
                conversaciones.add(conv);
            }

            ObjectNode datos = traductorJson.createObjectNode();
            datos.set("conversaciones", conversaciones);
            enviarRespuesta("exito", "Conversaciones recuperadas con éxito", datos);
        }
    }//try-with-resources para cerrar automaticamente la conexion con la db despues de ejecutar el try, (PreparedStatement ya tiene implementada la interfaz AutoCloseable)
}

    private void getMensajes(int conversacionId) throws SQLException {
        try (Connection conn = poolConexiones.obtenerConexion()) {
            String sql = """
                SELECT u.nombre, m.mensaje, m.fecha_envio 
                FROM mensajes m
                JOIN usuarios u ON m.remitente_id = u.id
                WHERE m.conversacion_id = ? 
                ORDER BY m.fecha_envio
                """;
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, conversacionId);
                ResultSet rs = stmt.executeQuery();

                ArrayNode mensajesArray = traductorJson.createArrayNode();
                while (rs.next()) {
                    ObjectNode mensaje = traductorJson.createObjectNode();
                    mensaje.put("nombre", rs.getString("nombre"));
                    mensaje.put("mensaje", rs.getString("mensaje"));
                    mensaje.put("fecha_envio", rs.getTimestamp("fecha_envio").toString());
                    mensajesArray.add(mensaje);
                }

                ObjectNode datos = traductorJson.createObjectNode();
                datos.set("mensajes", mensajesArray);
                enviarRespuesta("exito", "Mensajes recuperados con éxito", datos);    
        } //try-with-resources para cerrar automaticamente la conexion con la db despues de ejecutar el try, (PreparedStatement ya tiene implementada la interfaz AutoCloseable)
        
        }
    }

    private void enviarMensaje(int conversacionId, String mensaje) throws SQLException {
        try (Connection conn = poolConexiones.obtenerConexion()) {
            conn.setAutoCommit(false);
            try {
                String sql = "INSERT INTO mensajes (conversacion_id, remitente_id, mensaje, fecha_envio) VALUES (?, ?, ?, NOW())";
                try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                    stmt.setInt(1, conversacionId);
                    stmt.setInt(2, usuarioActualID);
                    stmt.setString(3, mensaje);
                    int filas = stmt.executeUpdate();

                    if (filas > 0) {
                        enviarRespuesta("exito", "Mensaje enviado exitosamente");
                    } else {
                        enviarRespuesta("error", "Error al enviar mensaje");
                    }
                }
                conn.commit();
            }catch(SQLException e){
                System.out.println("Error al enviar mensaje: " + e.getMessage());
                conn.rollback();
                throw e;
            }
        }//try-with-resources para cerrar automaticamente la conexion con la db despues de ejecutar el try, (PreparedStatement ya tiene implementada la interfaz AutoCloseable)
    }

    private void registrarUsuario(String telefono, String nombre, String password) throws SQLException {
        try (Connection conn = poolConexiones.obtenerConexion()) {
            conn.setAutoCommit(false); //Antes de insertar se valida que tdo este correcto
            try {
                // Verifica teléfono e inserta usuario en la misma transacción
                String telefonoExiste = "SELECT id FROM usuarios WHERE telefono = ?";
                try (PreparedStatement checkStmt = conn.prepareStatement(telefonoExiste)) {//Try-with-resources para cerrar automaticamente la conexion con la db despues de ejecutar el try, (PreparedStatement ya tiene implementada la interfaz AutoCloseable)
                    checkStmt.setString(1, telefono);
                    ResultSet rs = checkStmt.executeQuery();// para SELECT
                    if (rs.next()) {//Si hay resultados, el telefono ya existe, lista enlazada
                        enviarRespuesta("error", "El número de teléfono ya está registrado");
                        return;
                    }
                }
                //Si no, el telefono se puede registrar
                String sql = "INSERT INTO usuarios (telefono, nombre, password) VALUES (?, ?, ?)";
                try (PreparedStatement stmt = conn.prepareStatement(sql)) {//try-with-resources para cerrar automaticamente la conexion con la db despues de ejecutar el try, (PreparedStatement ya tiene implementada la interfaz AutoCloseable)
                    stmt.setString(1, telefono);
                    stmt.setString(2, nombre);
                    stmt.setString(3, password);
                    int resultado = stmt.executeUpdate();// para: INSERT, UPDATE, DELETE
                    // resultado guarda el numero de filas afectadas con executeUpdate
                    if (resultado > 0) {
                        enviarRespuesta("exito", "Usuario registrado exitosamente");
                    } else {
                        enviarRespuesta("error", "Error al registrar usuario");
                    }
                }
                conn.commit();
            } catch (SQLException e) {
                System.out.println("Error al registrar usuario: " + e.getMessage());
                conn.rollback();//Si ocurre algun error se hace roolback para devolver el estado anterior
                throw e;
            }
        }
    }

    // metodo para autenticar usuario retorna true si encuentra al usuario
    private boolean autenticarUsuario(String telefono, String password) throws SQLException {
        try (Connection conn = poolConexiones.obtenerConexion()) {
            String sql = "SELECT id FROM usuarios WHERE telefono = ? AND password = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {//Try-with-resources para cerrar automaticamente la conexion con la db despues de ejecutar el try, (PreparedStatement ya tiene implementada la interfaz AutoCloseable)
                pstmt.setString(1, telefono);
                pstmt.setString(2, password);
                ResultSet rs = pstmt.executeQuery();
                // Si se encuentra un usuario con esos parametros, retorna verdadero y actualiza el parametro usuarioActualID
                if (rs.next()) {
                    usuarioActualID = rs.getInt("id");
                    return true;
                }
            }
            return false;
        }
    }

    // metodo para registrar un nuevo usuario con JSON
    private void registrar(String telefono, String nombre, String password) {
        try {
            registrarUsuario(telefono, nombre, password);
            enviarRespuesta("exito", "Usuario registrado correctamente");
        } catch (SQLException e) {
            enviarRespuesta("error", "Error al registrar usuario: " + e.getMessage());
        }
    }

    // metodo para validar número de teléfono
    private Integer validarTelefono(String telefono) throws SQLException {
        try (Connection conn = poolConexiones.obtenerConexion()) {
            String sql = "SELECT id FROM usuarios WHERE telefono = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, telefono);
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    return rs.getInt("id");
                }
            } catch (SQLException e) {
                enviarRespuesta("error", "Error al validar teléfono: " + e.getMessage());
                throw e;
            }
            return null;
        }
    }

    // metodo para enviar respuestas simples como error o exito con JSON
    private void enviarRespuesta(String estado, String descripcion) {
        ObjectNode respuesta = traductorJson.createObjectNode();//Se crea un Nodo de tipo Json vacio usando el traductorJson
        //Cada comunicacion va a tener almenos 2 campos: estado y descripcion para la validacion de consultas del cliente
        respuesta.put("estado", estado);
        //put es un metodo de ObjectNode que permite agregar pares de clave-valor como en un hashmap
        respuesta.put("descripcion", descripcion);
        salida.println(respuesta);
    }

    // metodo para enviar respuestas con campos personalizados como id_conversacion o fehca_envio con JSON
    private void enviarRespuesta(String estado, String descripcion, ObjectNode datosAdicionales) {
        ObjectNode respuesta = traductorJson.createObjectNode();//Se crea un Nodo de tipo Json vacio usando el traductorJson
        //Cada comunicacion va a tener almenos 2 campos: estado y descripcion para la validacion de consultas del cliente
        respuesta.put("estado", estado);
        //put es un metodo de ObjectNode que permite agregar pares de clave-valor como en un hashmap
        respuesta.put("descripcion", descripcion);
        respuesta.setAll(datosAdicionales); // Añade todos los datos adicionales
        salida.println(respuesta);
    }

    // metodo para marcar mensaje como leído
    private void marcarMensajeComoLeido(int mensajeId) throws SQLException {
        try (Connection conn = poolConexiones.obtenerConexion()) {
            // verificar que el mensaje pertenezca a una conversación del usuario logeado
            String checkSql = """
                SELECT m.id 
                FROM mensajes m
                JOIN conversacion_usuario cu ON m.conversacion_id = cu.conversacion_id
                WHERE m.id = ? AND cu.usuario_id = ?
                """;

            try (PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {
                checkStmt.setInt(1, mensajeId);
                checkStmt.setInt(2, usuarioActualID);

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
                        enviarRespuesta("exito", "Mensaje marcado como leído");
                    } else {
                        enviarRespuesta("error", "No se pudo marcar el mensaje como leído");
                    }
                }
            }
        }
    }

    // metodo para obtener el estado del mensaje
    private void obtenerEstadoMensaje(int mensajeId) throws SQLException {
        try (Connection conn = poolConexiones.obtenerConexion()) {
            String sql = """
                SELECT m.id, m.fue_entregado, m.fecha_lectura, m.fecha_envio
                FROM mensajes m
                JOIN conversacion_usuario cu ON m.conversacion_id = cu.conversacion_id
                WHERE m.id = ? AND cu.usuario_id = ?
                """;

            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, mensajeId);
                stmt.setInt(2, usuarioActualID);
                ResultSet rs = stmt.executeQuery();

                if (rs.next()) {
                    ObjectNode datos = traductorJson.createObjectNode();
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

                    enviarRespuesta("exito", "Estado del mensaje recuperado", datos);
                } else {
                    enviarRespuesta("error", "Este mensaje o no existe");
                }
            }
        }
    }
    private void crearConversacionIndividual(String telefonoDestino) throws SQLException {
        try (Connection conn = poolConexiones.obtenerConexion()) {
            conn.setAutoCommit(false); // Se inicia la transacción manualmente

            try {
                // Validar si el número destino existe
                Integer idDestino = validarTelefono(telefonoDestino);
                if (idDestino == null) {
                    enviarRespuesta("error", "No existe usuario con el número: " + telefonoDestino);
                    return;
                }

                // Verificar si ya existe una conversación individual entre ambos
                String checkSql = """
                SELECT c.id FROM conversaciones c
                JOIN conversacion_usuario cu1 ON c.id = cu1.conversacion_id
                JOIN conversacion_usuario cu2 ON c.id = cu2.conversacion_id
                WHERE cu1.usuario_id = ? AND cu2.usuario_id = ?
                AND (SELECT COUNT(*) FROM conversacion_usuario cu3 WHERE cu3.conversacion_id = c.id) = 2
            """;

                try (PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {
                    checkStmt.setInt(1, usuarioActualID);
                    checkStmt.setInt(2, idDestino);
                    ResultSet rs = checkStmt.executeQuery();
                    if (rs.next()) {
                        ObjectNode datos = traductorJson.createObjectNode();
                        datos.put("idConversacion", rs.getInt("id"));
                        enviarRespuesta("success", "Ya existe una conversación con este usuario", datos);
                        return;
                    }
                }

                // Si no existe, crear conversación
                String sql = "INSERT INTO conversaciones (nombre, isGrupo) VALUES (?, ?)";
                try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                    stmt.setString(1, "Chat privado");
                    stmt.setBoolean(2, false); // conversación individual
                    stmt.executeUpdate();

                    ResultSet claves = stmt.getGeneratedKeys(); // Obtener el id de la inserción que se acaba de realizar
                    if (claves.next()) {
                        int nuevoIdConversacion = claves.getInt(1);

                        // Hace 2 inserciones, una para cada usuario en la tabla de unión entre usuario y conversación
                        String insertUsuarios = "INSERT INTO conversacion_usuario (conversacion_id, usuario_id) VALUES (?, ?), (?, ?)";
                        try (PreparedStatement insertStmt = conn.prepareStatement(insertUsuarios)) {
                            insertStmt.setInt(1, nuevoIdConversacion);
                            insertStmt.setInt(2, usuarioActualID);
                            insertStmt.setInt(3, nuevoIdConversacion);
                            insertStmt.setInt(4, idDestino);
                            insertStmt.executeUpdate();

                            ObjectNode datos = traductorJson.createObjectNode();
                            datos.put("idConversacion", nuevoIdConversacion);
                            enviarRespuesta("success", "Conversación individual creada", datos);
                        }
                    }
                }

                conn.commit(); // Finaliza la transacción exitosamente

            } catch (SQLException e) {
                System.out.println("Error al crear conversacion individual: " + e.getMessage());
                conn.rollback(); // Revierte todos los cambios si hubo error
                throw e;
            }
        }
    }


    private void crearConversacionPrivada(int remitenteId) throws SQLException {
        try (Connection conn = poolConexiones.obtenerConexion()) {
            String sql = "SELECT telefono FROM usuarios WHERE id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, remitenteId);
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    String telefonoRemitente = rs.getString("telefono");
                    crearConversacionIndividual(telefonoRemitente); // Reutiliza la lógica validada
                } else {
                    enviarRespuesta("error", "No se encontró al usuario con ID " + remitenteId);
                }
            }
        }
    }
    private void crearGrupo(String nombreGrupo, String telefonos) throws SQLException {
        try (Connection conn = poolConexiones.obtenerConexion()) {
            conn.setAutoCommit(false); // Inicia transacción para el grupo

            try {
                // Lista de IDs válidos (inicia con el creador)
                ArrayList<Integer> participantesValidos = new ArrayList<>();
                participantesValidos.add(usuarioActualID);

                StringBuilder telefonosInvalidos = new StringBuilder();

                // Validar y recolectar IDs por teléfono
                for (String telefono : telefonos.split(",")) {
                    String tel = telefono.trim();
                    Integer id = validarTelefono(tel);
                    if (id != null) {
                        participantesValidos.add(id);
                    } else {
                        if (!tel.isEmpty()) {
                            if (telefonosInvalidos.length() > 0) telefonosInvalidos.append(", ");
                            telefonosInvalidos.append(tel);
                        }
                    }
                }

                // Si no hay más participantes que el creador, aborta
                if (participantesValidos.size() < 2) {
                    enviarRespuesta("error", "No se puede crear un grupo sin participantes válidos");
                    return;
                }

                // Notifica advertencia si hay errores
                if (telefonosInvalidos.length() > 0) {
                    ObjectNode advertencia = traductorJson.createObjectNode();
                    advertencia.put("telefonosInvalidos", telefonosInvalidos.toString());
                    enviarRespuesta("warning", "Algunos números no se pudieron agregar", advertencia);
                }

                // Crea conversación tipo grupo
                String crearGrupo = "INSERT INTO conversaciones (nombre, isGrupo) VALUES (?, ?)";
                try (PreparedStatement stmt = conn.prepareStatement(crearGrupo, Statement.RETURN_GENERATED_KEYS)) {
                    stmt.setString(1, nombreGrupo);
                    stmt.setBoolean(2, true);
                    stmt.executeUpdate();

                    ResultSet claves = stmt.getGeneratedKeys();
                    if (claves.next()) {
                        int idConversacion = claves.getInt(1);

                        // Inserta todos los participantes válidos
                        StringBuilder sb = new StringBuilder("INSERT INTO conversacion_usuario (conversacion_id, usuario_id) VALUES ");
                        for (int i = 0; i < participantesValidos.size(); i++) {
                            sb.append("(?, ?)");
                            if (i < participantesValidos.size() - 1) sb.append(", ");
                        }

                        try (PreparedStatement insertar = conn.prepareStatement(sb.toString())) {
                            int idx = 1;
                            for (Integer idUsuario : participantesValidos) {
                                insertar.setInt(idx++, idConversacion);
                                insertar.setInt(idx++, idUsuario);
                            }
                            insertar.executeUpdate();

                            ObjectNode datos = traductorJson.createObjectNode();
                            datos.put("idGrupo", idConversacion);
                            datos.put("miembros", participantesValidos.size());
                            enviarRespuesta("success", "Grupo creado exitosamente", datos);
                        }
                    }
                }

                conn.commit(); // Finaliza transacción

            } catch (SQLException e) {
                conn.rollback();
                enviarRespuesta("error", "Fallo al crear grupo: " + e.getMessage());
                throw e;
            }
        }
    }



}