package com.docuflow.servlet;

import com.docuflow.util.DatabaseConfig;
import com.docuflow.util.DocumentValidator;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
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
            loadDocuments(request);
            request.getRequestDispatcher("dashboard.jsp").forward(request, response);
        } else if ("download".equals(action)) {
            downloadDocument(request, response);
        } else {
            loadDocuments(request);
            request.getRequestDispatcher("dashboard.jsp").forward(request, response);
        }
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
            String contentType = filePart.getContentType();

            if (fileName == null || fileName.isEmpty()) {
                request.setAttribute("uploadMessage", "Error: No se seleccionó ningún archivo.");
            } else if (!DocumentValidator.isValidExtension(fileName)) {
                request.setAttribute("uploadMessage", "Error: Formato no permitido. Solo PDF, PNG, JPG, JPEG.");
            } else {
                try (InputStream inputStream = filePart.getInputStream()) {
                    byte[] fileBytes = inputStream.readAllBytes();
                    saveToDb(fileName, currentUser, contentType, fileBytes);
                    request.setAttribute("uploadMessage", "Documento '" + fileName + "' guardado exitosamente en SQLite.");
                }
            }
        } catch (Exception e) {
            request.setAttribute("uploadMessage", "Error al procesar archivo: " + e.getMessage());
        }

        loadDocuments(request);
        request.getRequestDispatcher("dashboard.jsp").forward(request, response);
    }

    private void saveToDb(String fileName, String user, String contentType, byte[] fileBytes) {
        String sql = "INSERT INTO documents(name, uploaded_by, file_type, file_data) VALUES(?, ?, ?, ?)";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, fileName);
            pstmt.setString(2, user);
            pstmt.setString(3, contentType != null ? contentType : "application/octet-stream");
            pstmt.setBytes(4, fileBytes);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void downloadDocument(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String id = request.getParameter("id");
        if (id == null) return;

        String sql = "SELECT name, file_type, file_data FROM documents WHERE id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, Integer.parseInt(id));
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    String fileName = rs.getString("name");
                    String contentType = rs.getString("file_type");
                    byte[] fileBytes = rs.getBytes("file_data");

                    response.setContentType(contentType);
                    response.setHeader("Content-Disposition", "attachment; filename=\"" + fileName + "\"");
                    response.setContentLength(fileBytes.length);

                    try (OutputStream out = response.getOutputStream()) {
                        out.write(fileBytes);
                        out.flush();
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void loadDocuments(HttpServletRequest request) {
        List<Map<String, String>> docs = new ArrayList<>();
        String sql = "SELECT id, name, uploaded_by, file_type FROM documents ORDER BY id DESC";
        try (Connection conn = DatabaseConfig.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Map<String, String> doc = new HashMap<>();
                doc.put("id", String.valueOf(rs.getInt("id")));
                doc.put("name", rs.getString("name"));
                doc.put("user", rs.getString("uploaded_by"));
                doc.put("type", rs.getString("file_type"));
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