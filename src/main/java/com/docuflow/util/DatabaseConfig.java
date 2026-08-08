package com.docuflow.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseConfig {

    private static final String DB_URL = "jdbc:sqlite:docuflow.db";

    public static Connection getConnection() throws SQLException {
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return DriverManager.getConnection(DB_URL);
    }

    public static void initDatabase() {
        // Reiniciamos la tabla para aplicar la nueva estructura con BLOB y tipo de archivo
        String dropSql = "DROP TABLE IF EXISTS documents;";
        String createSql = "CREATE TABLE documents ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "name TEXT NOT NULL, "
                + "uploaded_by TEXT NOT NULL, "
                + "file_type TEXT NOT NULL, "
                + "file_data BLOB NOT NULL"
                + ");";
        try (Connection conn = getConnection(); Statement stmt = conn.createStatement()) {
            stmt.execute(dropSql);
            stmt.execute(createSql);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}