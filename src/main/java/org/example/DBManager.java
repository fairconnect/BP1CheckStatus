package org.example;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBManager {

    private static Connection connection = null;

    private static final String PAYSATDBNEW_URL = "jdbc:mysql://192.168.22.41:3306/paysatdb_new";
    private static final String PAYSATDBNEW_USER = "usrim055";
    private static final String PAYSATDBNEW_PASSWORD = "p5JE9e7RpX";

    private static final String PAYSATDBNEW_URL_CERT = "jdbc:mysql://192.168.123.2:3306/paysatdb_new";
    private static final String PAYSATDBNEW_USER_CERT= "root";
    private static final String PAYSATDBNEW_PASSWORD_CERT = "cctf,47A";

    private static final String SIMDB_URL = "jdbc:mysql://192.168.2.240:3306/simdwh";
    private static final String SIMDB_USER = "root";
    private static final String SIMDB_PASSWORD = "dB#bIg;j2A*>";


    private DBManager() {}


    public static Connection getConnectionReplica() {
        try {
            if (connection == null || connection.isClosed()) {
                Class.forName("com.mysql.cj.jdbc.Driver");
                connection = DriverManager.getConnection(PAYSATDBNEW_URL, PAYSATDBNEW_USER, PAYSATDBNEW_PASSWORD);
            }
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        }
        return connection;
    }

    public static void closeConnection() {
        if (connection != null) {
            try {
                connection.close();
                connection = null;
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public static Connection getConnectionPaysatDBNewReplicaOld() {
        Connection connection = null;
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");

            connection = DriverManager.getConnection(PAYSATDBNEW_URL, PAYSATDBNEW_USER, PAYSATDBNEW_PASSWORD);
            //System.out.println("Connessione al database riuscita.");
        } catch (ClassNotFoundException e) {
            System.err.println("Driver JDBC non trovato: " + e.getMessage());
        } catch (SQLException e) {
            System.err.println("Errore di connessione al database: " + e.getMessage());
        }
        return connection;
    }

    public static Connection getConnectionPaysatDBNewCert() {
        Connection connection = null;
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");

            connection = DriverManager.getConnection(PAYSATDBNEW_URL_CERT, PAYSATDBNEW_USER_CERT, PAYSATDBNEW_PASSWORD_CERT);
            //System.out.println("Connessione al database riuscita.");
        } catch (ClassNotFoundException e) {
            System.err.println("Driver JDBC non trovato: " + e.getMessage());
        } catch (SQLException e) {
            System.err.println("Errore di connessione al database: " + e.getMessage());
        }
        return connection;
    }

    public static Connection getConnectionDBSim() {
        Connection connection = null;
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");

            connection = DriverManager.getConnection(SIMDB_URL, SIMDB_USER, SIMDB_PASSWORD);
            //System.out.println("Connessione al database riuscita.");
        } catch (ClassNotFoundException e) {
            System.err.println("Driver JDBC non trovato: " + e.getMessage());
        } catch (SQLException e) {
            System.err.println("Errore di connessione al database: " + e.getMessage());
        }
        return connection;
    }
}