package com.docuflow.servlet;

import com.docuflow.util.DatabaseConfig;
import com.docuflow.util.DocumentValidator;
import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.Part;

@WebServlet("/upload")
@MultipartConfig(fileSizeThreshold = 1024 * 1024 * 2, maxFileSize = 1024 * 1024 * 10)
public class DocumentUploadServlet extends HttpServlet {

    @Override
    public void init() throws ServletException {
        DatabaseConfig.initDatabase();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        String action = request.getParameter("action");
        if ("delete".equals(action)) {
            deleteDocument(request);
        }
        
        loadDocuments(request);
        request.getRequestDispatcher("dashboard.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        String currentUser = (session != null && session.getAttribute("user") != null) 
                             ? (String) session.getAttribute("user") : "Anonimo";

        try {
            Part filePart = request.getPart("document");
            String fileName = getFileName(filePart);

            if (fileName == null || fileName.isEmpty()) {
                request.setAttribute("uploadMessage", "Error: No se seleccionó ningún archivo.");
            } else if (!DocumentValidator.isValidExtension(fileName)) {
                request.setAttribute("uploadMessage", "Error: Formato no permitido. Solo PDF, PNG, JPG, JPEG.");
            } else {
                saveToDb(fileName, currentUser);
                request.setAttribute("uploadMessage", "Documento '" + fileName + "' guardado exitosamente.");
            }
        } catch (Exception e) {
            request.setAttribute("uploadMessage", "Error al procesar archivo: " + e.getMessage());
        }

        loadDocuments(request);
        request.getRequestDispatcher("dashboard.jsp").forward(request, response);
    }

    private void saveToDb(String fileName, String user) {
        String sql = "INSERT INTO documents(name, uploaded_by) VALUES(?, ?)";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, fileName);
            pstmt.setString(2, user);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void loadDocuments(HttpServletRequest request) {
        List<Map<String, String>> docs = new ArrayList<>();
        String sql = "SELECT * FROM documents ORDER BY id DESC";
        try (Connection conn = DatabaseConfig.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Map<String, String> doc = new HashMap<>();
                doc.put("id", String.valueOf(rs.getInt("id")));
                doc.put("name", rs.getString("name"));
                doc.put("user", rs.getString("uploaded_by"));
                docs.add(doc);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        request.setAttribute("documentsList", docs);
    }

    private void deleteDocument(HttpServletRequest request) {
        String id = request.getParameter("id");
        if (id != null) {
            String sql = "DELETE FROM documents WHERE id = ?";
            try (Connection conn = DatabaseConfig.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, Integer.parseInt(id));
                pstmt.executeUpdate();
                request.setAttribute("uploadMessage", "Documento eliminado correctamente.");
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    private String getFileName(Part part) {
        String contentDisp = part.getHeader("content-disposition");
        for (String token : contentDisp.split(";")) {
            if (token.trim().startsWith("filename")) {
                return token.substring(token.indexOf('=') + 2, token.length() - 1);
            }
        }
        return null;
    }
}