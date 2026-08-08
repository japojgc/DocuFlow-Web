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
        String dropSql = "DROP TABLE IF EXISTS requests;";
        String createSql = "CREATE TABLE requests ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "concepto TEXT NOT NULL, "
                + "monto REAL NOT NULL, "
                + "fecha TEXT NOT NULL, "
                + "observaciones TEXT, "
                + "file_name TEXT, "
                + "file_type TEXT, "
                + "file_data BLOB, "
                + "uploaded_by TEXT NOT NULL, "
                + "status TEXT DEFAULT 'PENDIENTE', "
                + "feedback TEXT"
                + ");";
        try (Connection conn = getConnection(); Statement stmt = conn.createStatement()) {
            stmt.execute(dropSql);
            stmt.execute(createSql);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}