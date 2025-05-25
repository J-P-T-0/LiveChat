package org.example;

import java.sql.Connection;
import java.util.LinkedList;

public class poolConexiones {
    private static final int MAX_CONNECTIONS = 10;
    private final LinkedList<Connection> poolConn = new LinkedList<>();
}
