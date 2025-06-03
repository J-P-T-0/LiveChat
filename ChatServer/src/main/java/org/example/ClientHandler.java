package org.example;

//Importar los requests
import Requests.*;
import Respuestas.*;

import java.io.*;
import java.net.Socket;
import java.sql.*;
import java.util.ArrayList;

//importar librerias para JSON
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;


import static org.example.ChatServer.traductorJson;
import static org.example.ChatServer.writers;

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

    // Constructor que recibe el socket del cliente
    public ClientHandler(Socket socket, poolConexiones poolConexiones) {
        this.socket = socket;
        this.poolConexiones = poolConexiones;
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
                return;
            }

            // Confirmar conexion
            //enviarRespuesta(new Aviso("éxito","conexión establecida con el servidor"));
            
            String linea;
            while ((linea = entrada.readLine()) != null) {
                try {
                    // migrar_Todo a JSON :,,,D
                    //Convierte el string que recibe los mensajes de clientes y los convierte al formato Json (un arbol de objetos)

                    //Almacena el request pedido de lado de cliente
                    Request newRequest = traductorJson.readValue(linea, Request.class);

                    System.out.println("traductor funciona");

                    // Verificación de autenticación
                    if (!(newRequest instanceof Close) && !(newRequest instanceof Login) && !(newRequest instanceof Registrarse)
                            && usuarioActualID == null) {
                        enviarRespuesta(new Aviso("error", "Debes autenticarte primero"));
                        continue;
                    }

                    switch (newRequest) {
                        case Login loginRequest->login(loginRequest);

                        case Registrarse registroRequest ->registrar(registroRequest);

                        case GetConversaciones _ -> cargarConversaciones();

                        case GetMensajes mensajesRequest -> getMensajes(mensajesRequest);

                        case EnviarMensaje enviarMensajeRequest ->enviarMensaje(enviarMensajeRequest);

                        case CrearConversacionIndividual crearConvPrivRequest -> crearConversacionIndividual(crearConvPrivRequest);

                        case CrearGrupo crearGrupoRequest -> crearGrupo(crearGrupoRequest);

                        case GetEstadoMensaje getEstadoMensajeRequest -> obtenerEstadoMensaje(getEstadoMensajeRequest);

                        case MarcarLeido marcarLeidoRequest -> marcarMensajeComoLeido(marcarLeidoRequest);

                        case Close closeConn -> closeConn(closeConn);

                        case Reconnect reconnectRequest -> reconnect(reconnectRequest);

                        default-> enviarRespuesta(new Aviso("error", "Comando no reconocido"));
                    }
                } catch (Exception e) {
                    System.err.println(e.getMessage());
                    enviarRespuesta(new Aviso("error", "Error al procesar comando: " + e.getMessage()));
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
            System.out.println("Cliente finalizado");
        }
    }

    // metoodo login con json
    private void login(Login request) throws JsonProcessingException {
        try {
            if (autenticarUsuario(request.getTelefono(), request.getPassword())) {
                String nombre = getNombreUsu(request.getTelefono());
                enviarRespuesta(new LoginAuth(nombre, request.getTelefono()));
                writers.put(request.getTelefono(), salida);
            } else {
                enviarRespuesta(new Aviso("éxito","Contraseña o teléfono incorrectos"));
            }
        } catch (Exception e) {
            enviarRespuesta(new Aviso("error", "Error en login: " + e.getMessage()));
        }
    }

    // metodo para registrar un nuevo usuario con JSON
    private void registrar(Registrarse request) throws JsonProcessingException {
        try {
            if(existeTelefono(request.getTelefono())){
                throw new Exception("El número ya está registrado, pedir número nuevamente.");
            }
            usuarioRegistrado(request.getNombre(), request.getTelefono(), request.getContrasena());
            enviarRespuesta(new Aviso("éxito","Se registró el usuario"));
        } catch (Exception e) {
            enviarRespuesta(new Aviso("error", "Error al registrar usuario: " + e.getMessage()));
        }
    }

    private ArrayList<String> getParticipantes(int conv_id)  throws SQLException{
        Connection conn = poolConexiones.obtenerConexion();
        ArrayList<String> participantes = new ArrayList<>();

        String query = """               
            SELECT u.nombre AS participante
            FROM conversaciones c
            INNER JOIN conversacion_usuario cu1 ON c.id = cu1.conversacion_id
            INNER JOIN conversacion_usuario cu2 ON c.id = cu2.conversacion_id
            INNER JOIN usuarios u ON cu2.usuario_id = u.id
            WHERE cu1.usuario_id = ? AND cu1.conversacion_id = ?
            LIMIT 0, 50;
            """;

        //Ejecutar la query
        PreparedStatement stmt = conn.prepareStatement(query);
        stmt.setInt(1, usuarioActualID);
        stmt.setInt(2, conv_id);
        ResultSet rs = stmt.executeQuery();
        rs.getFetchSize();

        while (rs.next()) {
            participantes.add(rs.getString("participante"));
        }


        return participantes;
    }

private void cargarConversaciones() throws SQLException, JsonProcessingException {
    Connection conn = poolConexiones.obtenerConexion();
    try {
        String sql = """
                SELECT c.id, c.nombre, c.isGrupo
                FROM conversaciones c
                INNER JOIN conversacion_usuario p ON c.id = p.conversacion_id
                WHERE p.usuario_id = ?
                LIMIT 0,50
                """;
        try{
            //Ejecutar la query
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, usuarioActualID);
            ResultSet rs = stmt.executeQuery();

            ArrayList<DatosConversacion> datosConv = new ArrayList<>();

            while (rs.next()) {
                ArrayList<String> participantes = getParticipantes(rs.getInt("id"));
                //conv es como crear un objeto de tipo conversacion
                datosConv.add(new DatosConversacion(rs.getInt("id"), rs.getString("nombre"), rs.getBoolean("isGrupo"), participantes));
            }
            enviarRespuesta(new ReturnConversaciones(datosConv));
        }catch (SQLException e) {
            enviarRespuesta(new Aviso("error", "Error al recuperar conversaciones: " + e.getMessage()));
        }
    }
    finally {
        if (conn != null){
            poolConexiones.liberarConexion(conn);
        }
    }
}

    private void getMensajes(GetMensajes request) throws SQLException, JsonProcessingException {
        Connection conn = poolConexiones.obtenerConexion();
        try {
        String sql = """
                SELECT u.nombre, m.mensaje, m.fecha_envio
                FROM mensajes m
                JOIN usuarios u ON m.remitente_id = u.id
                WHERE m.conversacion_id = ?
                ORDER BY m.fecha_envio
                LIMIT 0,50
                """;
        try{
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, request.getConversacionId());
            ResultSet rs = stmt.executeQuery();

            ArrayList<DatosMensajes> datosMensajes = new ArrayList<>();
            while (rs.next()) {
                datosMensajes.add(new DatosMensajes(rs.getString("nombre"),rs.getString("mensaje"),rs.getTimestamp("fecha_envio").toString()));
            }
            enviarRespuesta(new ReturnMensajes(datosMensajes, request.getConversacionId()));
        }catch(Exception e){
            enviarRespuesta(new Aviso("error", "Error al recuperar mensajes: " + e.getMessage()));
        }
        }finally {
            if (conn != null){
                poolConexiones.liberarConexion(conn);
            }
        }

    }

    private void getMensajes(GetMensajes request, String telDestinatario) throws SQLException, JsonProcessingException {
        Connection conn = poolConexiones.obtenerConexion();
        try {
            String sql = """
                SELECT u.nombre, m.mensaje, m.fecha_envio
                FROM mensajes m
                JOIN usuarios u ON m.remitente_id = u.id
                WHERE m.conversacion_id = ?
                ORDER BY m.fecha_envio
                LIMIT 0,50
                """;
            try{
                PreparedStatement stmt = conn.prepareStatement(sql);
                stmt.setInt(1, request.getConversacionId());
                ResultSet rs = stmt.executeQuery();

                ArrayList<DatosMensajes> datosMensajes = new ArrayList<>();
                while (rs.next()) {
                    datosMensajes.add(new DatosMensajes(rs.getString("nombre"),rs.getString("mensaje"),rs.getTimestamp("fecha_envio").toString()));
                }
                enviarRespuesta(new ReturnMensajes(datosMensajes, request.getConversacionId()), writers.get(telDestinatario));
            }catch(Exception e){
                enviarRespuesta(new Aviso("error", "Error al recuperar mensajes: " + e.getMessage()), writers.get(telDestinatario));
            }
        }finally {
            if (conn != null){
                poolConexiones.liberarConexion(conn);
            }
        }

    }

    private void enviarMensaje(EnviarMensaje request) throws JsonProcessingException, SQLException {
        Connection conn = poolConexiones.obtenerConexion();
        try {
            conn.setAutoCommit(false);
            String sql = "INSERT INTO mensajes (conversacion_id, remitente_id, mensaje, fecha_envio) VALUES (?, ?, ?, NOW())";
            try {
                PreparedStatement stmt = conn.prepareStatement(sql);
                stmt.setInt(1, request.getConversacionID());
                stmt.setInt(2, usuarioActualID);
                stmt.setString(3, request.getMensaje());
                stmt.executeUpdate();



                conn.commit();
            } catch (Exception e) {
                conn.rollback();
                enviarRespuesta(new Aviso("error", "Error al enviar mensajes: " + e.getMessage()));
            }
        } finally {
            conn.setAutoCommit(true);
            if (conn != null){
                poolConexiones.liberarConexion(conn);
            }

            ArrayList<String> participantes = getParticipantes(request.getConversacionID());

            for(String s : writers.keySet()){
                System.out.println(s + ": " + writers.get(s));
            }

            for(String p : participantes){
                System.out.println(p);
                String tel = getTelFromNombre(p);
                //Se asegura de que el destinatario esté en linea para evitar errores
                if(writers.containsKey(tel)) {
                    getMensajes(new GetMensajes(request.getConversacionID()), tel);
                }
            }
        }
    }

    private void crearConversacionIndividual(CrearConversacionIndividual request) throws SQLException, JsonProcessingException {
        Connection conn = poolConexiones.obtenerConexion();
        try {
            conn.setAutoCommit(false); // Se inicia la transacción manualmente

            try {
                // Validar si el número destino existe
                Integer idDestino = validarTelefono(request.getTelefonoDestino());
                if (idDestino == null) {
                    enviarRespuesta(new Aviso("error", "No existe usuario con el número: " + request.getTelefonoDestino()));
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
                        enviarRespuesta(new Aviso("error", "Este teléfono ya fue añadido a tus conversaciones"));
                        return;
                    }
                }

                // Si no existe, crear conversación
                String sql = "INSERT INTO conversaciones (nombre, isGrupo) VALUES (?, ?)";
                try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                    stmt.setString(1, request.getNombreConv());
                    stmt.setBoolean(2, false); // conversación individual
                    stmt.executeUpdate();

                    ResultSet claves = stmt.getGeneratedKeys(); // Obtener el id de la inserción que se acaba de realizar
                    if (claves.next()) {
                        int nuevoIdConversacion = claves.getInt(1);

                        String remitente = request.getRemitente();
                        String destinatario = getNombreUsu(request.getTelefonoDestino());

                        // Hace 2 inserciones, una para cada usuario en la tabla de unión entre usuario y conversación
                        String insertUsuarios = "INSERT INTO conversacion_usuario (conversacion_id, usuario_id) VALUES (?, ?), (?, ?)";
                        try (PreparedStatement insertStmt = conn.prepareStatement(insertUsuarios)) {
                            insertStmt.setInt(1, nuevoIdConversacion);
                            insertStmt.setInt(2, usuarioActualID);
                            insertStmt.setInt(3, nuevoIdConversacion);
                            insertStmt.setInt(4, idDestino);
                            insertStmt.executeUpdate();

                            enviarRespuesta(new ReturnConvID(nuevoIdConversacion, destinatario));

                            //Se asegura de que el destinatario esté en linea para evitar errores
                            if(writers.containsKey(request.getTelefonoDestino())) {
                                enviarRespuesta(new ReturnConvID(nuevoIdConversacion, remitente), writers.get(request.getTelefonoDestino()));
                            }
                        }
                    }
                }

                conn.commit(); // Finaliza la transacción exitosamente

            } catch (SQLException e) {
                System.out.println("Error al crear conversacion individual: " + e.getMessage());
                conn.rollback(); // Revierte todos los cambios si hubo error
                throw e;
            }
        }finally {
            conn.setAutoCommit(true);
            if (conn != null){
                poolConexiones.liberarConexion(conn);
            }
        }
    }

    private void crearGrupo(CrearGrupo request) throws SQLException, JsonProcessingException {
        Connection conn = poolConexiones.obtenerConexion();
        try {
            conn.setAutoCommit(false); // Inicia transacción para el grupo

            try {
                // Lista de IDs válidos (inicia con el creador)
                ArrayList<Integer> participantesValidos = new ArrayList<>();
                participantesValidos.add(usuarioActualID);

                ArrayList<String> telefonosInvalidos = new ArrayList<>();


                // Validar y recolectar IDs por teléfono
                for (String telefono : request.getNumsTelefono()) {
                    String tel = telefono.trim();
                    Integer id = validarTelefono(tel);
                    if (id != null) {
                        participantesValidos.add(id);
                    } else if (!tel.isBlank()){
                        telefonosInvalidos.add(tel);
                    }
                }

                // Si no hay más participantes que el creador, aborta
                if (participantesValidos.size() < 2) {
                    enviarRespuesta(new Aviso("error", "No se puede crear un grupo sin participantes válidos"));
                    return;
                }

                // Notifica advertencia si hay errores
                if (!telefonosInvalidos.isEmpty()) {
                    enviarRespuesta(new NumsInvalidos(telefonosInvalidos));
                }

                // Crea conversación tipo grupo
                String crearGrupo = "INSERT INTO conversaciones (nombre, isGrupo) VALUES (?, ?)";
                try (PreparedStatement stmt = conn.prepareStatement(crearGrupo, Statement.RETURN_GENERATED_KEYS)) {
                    stmt.setString(1, request.getNombreGrupo());
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
                            ArrayList<String> ID_telefonosValidos = new ArrayList<>();
                            int idx = 1;
                            for (Integer idUsuario : participantesValidos) {
                                insertar.setInt(idx++, idConversacion);
                                insertar.setInt(idx++, idUsuario);

                                String telID = getTelFromID(idUsuario);
                                if(telID != null) {
                                    ID_telefonosValidos.add(telID);
                                }
                            }
                            insertar.executeUpdate();

                            enviarRespuesta(new GroupParticipants(idConversacion, ID_telefonosValidos, request.getNombreGrupo()));
                        }
                    }
                }

                conn.commit(); // Finaliza transacción

            } catch (SQLException e) {
                conn.rollback();
                enviarRespuesta(new Aviso("error", "Error al crear grupo: " + e.getMessage()));
                throw e;
            }
        }finally {
            conn.setAutoCommit(true);
            if (conn != null){
                poolConexiones.liberarConexion(conn);
            }
        }
    }

    private void closeConn(Close closeConn) {
        try {
            enviarRespuesta(new CloseClient());
            writers.remove(closeConn.getTelefono());
            salida.close();
            entrada.close();
            socket.close();
        }catch (Exception e) {
            System.err.println("Error al cerrar conexión: " + e.getMessage());
        }finally {
            for(String s : writers.keySet()){
                System.out.println(s + ": " + writers.get(s));
            }
        }
    }

    /*
    *
    * Sección de métodos adicionales
    *
    * */

    private String getNombreUsu(String telefono) throws SQLException {
        Connection conn = poolConexiones.obtenerConexion();
        try {
        String sql = "SELECT nombre FROM usuarios WHERE telefono = ?";
        PreparedStatement stmt = conn.prepareStatement(sql);
        stmt.setString(1, telefono);
        ResultSet rs = stmt.executeQuery();
        rs.next();
        return rs.getString("nombre");
    }
        finally {
        if (conn != null){
            poolConexiones.liberarConexion(conn);
        }
    }
    }

    // metodo para enviar respuestas con campos personalizados como id_conversacion o fehca_envio con JSON
    // métod0 fue modificado para que mande todos los tipos de mensajes
    private void enviarRespuesta(Respuesta respuesta) throws JsonProcessingException {
        String jsonRespuesta = traductorJson.writeValueAsString(respuesta);
        salida.println(jsonRespuesta);
    }

    //metodo para mandar respuesta a usuario especifico
    private void enviarRespuesta(Respuesta respuesta, PrintWriter output) throws JsonProcessingException {
        System.out.println("Respuesta a otro");
        String jsonRespuesta = traductorJson.writeValueAsString(respuesta);
        output.println(jsonRespuesta);
    }

    // metodo para marcar mensaje como leído
    private void marcarMensajeComoLeido(MarcarLeido request) throws SQLException, JsonProcessingException {
        Connection conn = poolConexiones.obtenerConexion();
        try {
            // verificar que el mensaje pertenezca a una conversación del usuario logeado
            String checkSql = """
            SELECT m.id
            FROM mensajes m
            JOIN conversacion_usuario cu ON m.conversacion_id = cu.conversacion_id
            WHERE m.id = ? AND cu.usuario_id = ?
            """;

            try (PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {
                checkStmt.setInt(1, request.getMensajeID());
                checkStmt.setInt(2, usuarioActualID);

                if (!checkStmt.executeQuery().next()) {
                    enviarRespuesta(new Aviso("error", "mensaje no pertenece a ninguna de tus conversaciones"));
                    return;
                }

                // Si pertenece a alguna conversacion del usuario
                String updateSql = "UPDATE mensajes SET fecha_lectura = NOW() WHERE id = ?";
                try (PreparedStatement updateStmt = conn.prepareStatement(updateSql)) {
                    updateStmt.setInt(1, request.getMensajeID());
                    int filasActualizadas = updateStmt.executeUpdate();

                    if (filasActualizadas > 0) {
                        enviarRespuesta(new Aviso("exito", "Mensaje marcado como leído"));
                    } else {
                        enviarRespuesta(new Aviso("error", "No se pudo marcar el mensaje como leído"));
                    }
                }
            }
        }finally {
            if (conn != null){
                poolConexiones.liberarConexion(conn);
            }
        }
    }

    // metodo para obtener el estado del mensaje
    private void obtenerEstadoMensaje(GetEstadoMensaje request) throws SQLException, JsonProcessingException {
        Connection conn = poolConexiones.obtenerConexion();
        try {
            String sql = """
            SELECT m.id, m.isEntregado, m.fecha_lectura, m.fecha_envio
            FROM mensajes m
            JOIN conversacion_usuario cu ON m.conversacion_id = cu.conversacion_id
            WHERE m.id = ? AND cu.usuario_id = ?
            """;

            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, request.getMensajeID());
                stmt.setInt(2, usuarioActualID);
                ResultSet rs = stmt.executeQuery();

                if (rs.next()) {
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

                    enviarRespuesta(new ReturnEstadoMensaje(estadoMensaje,fechaLectura,rs.getTimestamp("fecha_envio").toString() ));
                } else {
                    enviarRespuesta(new Aviso("error", "Este mensaje o no existe"));
                }
            }
        }finally {
            if (conn != null){
                poolConexiones.liberarConexion(conn);
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
        Connection conn = poolConexiones.obtenerConexion();
        try {
            conn.setAutoCommit(false);
        String sql = "INSERT INTO usuarios (nombre, telefono, contrasenia) VALUES (?, ?, ?)";
        PreparedStatement stmt = conn.prepareStatement(sql);
        stmt.setString(1, nombre);
        stmt.setString(2, telefono);
        stmt.setString(3, contrasenia);
        stmt.executeUpdate();
        conn.commit();
        }catch (SQLException e){
            conn.rollback();
        }
        finally {
            conn.setAutoCommit(true);
            if (conn != null){
                poolConexiones.liberarConexion(conn);
            }
        }
    }

    //Valida si ya existe algún usuario con el mismo teléfono
    private boolean existeTelefono (String telefono) throws SQLException{
        Connection conn = poolConexiones.obtenerConexion();
        try {
        String checkTel = "SELECT id FROM usuarios WHERE telefono = ?";
        PreparedStatement checkStmt = conn.prepareStatement(checkTel);
        checkStmt.setString(1, telefono);
        ResultSet rs = checkStmt.executeQuery();
        return rs.next();
        }finally {
            if (conn != null){
                poolConexiones.liberarConexion(conn);
            }
        }
    }


    // metodo para autenticar usuario retorna true si encuentra al usuario
    private boolean autenticarUsuario(String telefono, String password) throws SQLException {
        Connection conn = poolConexiones.obtenerConexion();
        try {
        String sql = "SELECT id FROM usuarios WHERE telefono = ? AND contrasenia = ?";
        PreparedStatement pstmt = conn.prepareStatement(sql);
        pstmt.setString(1, telefono);
        pstmt.setString(2, password);
        ResultSet rs = pstmt.executeQuery();
        // Si se encuentra un usuario con esos parametros, retorna verdadero y actualiza el parametro usuarioActualID
        if (rs.next()) {
            usuarioActualID = rs.getInt("id");
            return true;
        }
        return false;
        }finally {
            if (conn != null){
                poolConexiones.liberarConexion(conn);
            }
        }
    }

    private Integer validarTelefono(String telefono) throws SQLException, JsonProcessingException {
        Connection conn = poolConexiones.obtenerConexion();
        try {
            String sql = "SELECT id FROM usuarios WHERE telefono = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, telefono);
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    return rs.getInt("id");
                }
            } catch (SQLException e) {
                enviarRespuesta(new Aviso("error", "Error al validar teléfono: " + e.getMessage()));
                throw e;
            }
            return null;
        }finally {
            if (conn != null){
                poolConexiones.liberarConexion(conn);
            }
        }
    }

    private String getTelFromID(Integer id) throws SQLException, JsonProcessingException {
        Connection conn = poolConexiones.obtenerConexion();
        try {
            String sql = "SELECT telefono FROM usuarios WHERE id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, id.toString());
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    return rs.getString("telefono");
                }
            } catch (SQLException e) {
                enviarRespuesta(new Aviso("error", "Error al validar teléfono: " + e.getMessage()));
                throw e;
            }
            return null;
        }finally {
            if (conn != null){
                poolConexiones.liberarConexion(conn);
            }
        }
    }

    private String getTelFromNombre(String nombre) throws SQLException, JsonProcessingException {
        Connection conn = poolConexiones.obtenerConexion();
        try {
            String sql = "SELECT telefono FROM usuarios WHERE nombre = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, nombre);
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    return rs.getString("telefono");
                }
            } catch (SQLException e) {
                enviarRespuesta(new Aviso("error", "Error al validar teléfono: " + e.getMessage()));
                throw e;
            }
            return null;
        }finally {
            if (conn != null){
                poolConexiones.liberarConexion(conn);
            }
        }
    }
    
private void reconnect(Reconnect request) throws JsonProcessingException {
    try {
        // Validar que el usuario exista
        if (!existeTelefono(request.getTelefono())) {
            enviarRespuesta(new Aviso("error", "Usuario no encontrado"));
            return;
        }

        // Remover conexión anterior si existe
        writers.remove(request.getTelefono());
        
        // Reinicializar la conexión
        try {
            entrada = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            salida = new PrintWriter(socket.getOutputStream(), true);
        } catch (IOException e) {
            throw new IOException("Error al reiniciar streams de datos: " + e.getMessage());
        }
        enviarRespuesta(new Aviso("exito", "Necesitas Reinciar sesion"));
        // Usuario actual se hace null de modo que no se puede acceder a ninguna funcion hasta volver a logearse.
        usuarioActualID=null;

    } catch (Exception e) {
        enviarRespuesta(new Aviso("error", "Error en la reconexión: " + e.getMessage()));
    }
}
}