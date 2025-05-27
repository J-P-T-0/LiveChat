package org.example;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.LinkedList;


public class poolConexiones implements AutoCloseable {
    
    private static final int CONEXIONES_SIMULTANEAS = 20;
    private final LinkedList<Connection> conexiones = new LinkedList<>();
    //Controla cuando hay conexiones disponibles para ser utilizadas por los hilos, cuando se cierra el pool de conexiones se marca el valor a true para que no se puedan utilizar las conexiones
    private boolean cerrado = false;
    private int conexionesCreadas = 0;
    
    //Variables de entorno para la conexion a la base de datos
    private static final String DB_URL = System.getenv("DB_URL");
    private static final String DB_USER = System.getenv("DB_USER");
    private static final String DB_PASS = System.getenv("DB_PASS");
    private static final String UBICACION_TRUSTSTORE = "/app/truststore/truststore.jks";
    private static final String CONTRASENIA_TRUSTORE = System.getenv("TRUSTSTORE_PASS");

    public poolConexiones() {
        configurarSSL();
        iniciarPool();
    }

    private void configurarSSL() {
        System.setProperty("javax.net.ssl.trustStore", UBICACION_TRUSTSTORE);
        System.setProperty("javax.net.ssl.trustStorePassword", CONTRASENIA_TRUSTORE);
    }

    private void iniciarPool() {
        for (int i = 0; i < CONEXIONES_SIMULTANEAS; i++) {
            try {
                Connection auxConn = crearConexion();
                conexiones.add(auxConn);
                conexionesCreadas++;
            } catch (SQLException e) {
                System.err.println("Error al inicializar conexión: " + e.getMessage());
            }
        }
    }

    private Connection crearConexion() throws SQLException {
        return DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
    }

    public synchronized Connection obtenerConexion() throws SQLException {
        while (!cerrado) {
            if (!conexiones.isEmpty()) {
                Connection conn = conexiones.removeFirst();
                try {
                    if (conn.isValid(1)) {
                        return conn;
                    } else {
                        conn.close(); // eliminar conexión inválida
                        conexionesCreadas--;
                    }
                } catch (SQLException e) {
                    conexionesCreadas--;
                }
            } else if (conexionesCreadas < CONEXIONES_SIMULTANEAS) {
                //Crear nueva si no llegamos al límite
                Connection nueva = crearConexion();
                conexionesCreadas++;
                return nueva;
            } else {
                // Esperar si no hay disponibles y no se pueden crear más
                try {
                    wait();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new SQLException("Interrupción mientras esperaba una conexión", e);
                }
            }
        }

        throw new SQLException("El pool de conexiones está cerrado");
    }

    public synchronized void liberarConexion(Connection conexion) throws SQLException {
        if (conexion == null || conexion.isClosed()) {
            conexionesCreadas--;
            return;
        }

        if (!cerrado) {
            conexiones.addLast(conexion);
            notifyAll(); //Notifica a hilos que están esperando
        } else {
            conexion.close();
            conexionesCreadas--;
        }
    }

    @Override
    public synchronized void close() throws SQLException {
        cerrado = true;
        SQLException errores = null;
        for (Connection c : conexiones) {
            try {
                c.close();
            } catch (SQLException e) {
                if (errores == null) errores = e;
                else errores.addSuppressed(e);
            }
        }
        conexiones.clear();
        conexionesCreadas = 0;
        notifyAll(); // Notificar a hilos esperando
        if (errores != null) throw errores;
    }
}