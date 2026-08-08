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
            deleteRequest(request);
        } else if ("download".equals(action)) {
            downloadDocument(request, response);
            return;
        } else if ("evaluate".equals(action)) {
            evaluateRequest(request);
        }

        loadRequests(request);
        request.getRequestDispatcher("dashboard.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        String currentUser = (session != null && session.getAttribute("user") != null) 
                             ? (String) session.getAttribute("user") : "Anonimo";

        String concepto = request.getParameter("concepto");
        String montoStr = request.getParameter("monto");
        String fecha = request.getParameter("fecha");
        String observaciones = request.getParameter("observaciones");

        try {
            Part filePart = request.getPart("document");
            String fileName = getFileName(filePart);
            String contentType = filePart.getContentType();

            byte[] fileBytes = null;
            if (fileName != null && !fileName.isEmpty()) {
                if (!DocumentValidator.isValidExtension(fileName)) {
                    request.setAttribute("uploadMessage", "Error: Formato de archivo no permitido.");
                    loadRequests(request);
                    request.getRequestDispatcher("dashboard.jsp").forward(request, response);
                    return;
                }
                try (InputStream inputStream = filePart.getInputStream()) {
                    fileBytes = inputStream.readAllBytes();
                }
            }

            saveRequestToDb(concepto, Double.parseDouble(montoStr), fecha, observaciones, fileName, contentType, fileBytes, currentUser);
            request.setAttribute("uploadMessage", "Solicitud registrada con éxito.");
        } catch (Exception e) {
            request.setAttribute("uploadMessage", "Error al procesar solicitud: " + e.getMessage());
        }

        loadRequests(request);
        request.getRequestDispatcher("dashboard.jsp").forward(request, response);
    }

    private void saveRequestToDb(String concepto, double monto, String fecha, String obs, String fileName, String contentType, byte[] fileBytes, String user) {
        String sql = "INSERT INTO requests(concepto, monto, fecha, observaciones, file_name, file_type, file_data, uploaded_by, status) VALUES(?, ?, ?, ?, ?, ?, ?, ?, 'PENDIENTE')";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, concepto);
            pstmt.setDouble(2, monto);
            pstmt.setString(3, fecha);
            pstmt.setString(4, obs);
            pstmt.setString(5, fileName);
            pstmt.setString(6, contentType);
            pstmt.setBytes(7, fileBytes);
            pstmt.setString(8, user);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void evaluateRequest(HttpServletRequest request) {
        String id = request.getParameter("id");
        String status = request.getParameter("status"); // APROBADO o RECHAZADO
        String feedback = request.getParameter("feedback");

        if (id != null && status != null) {
            String sql = "UPDATE requests SET status = ?, feedback = ? WHERE id = ?";
            try (Connection conn = DatabaseConfig.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, status);
                pstmt.setString(2, feedback != null ? feedback : "");
                pstmt.setInt(3, Integer.parseInt(id));
                pstmt.executeUpdate();
                request.setAttribute("uploadMessage", "Solicitud " + status.toLowerCase() + " correctamente.");
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    private void loadRequests(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        String role = (session != null) ? (String) session.getAttribute("role") : "";
        String user = (session != null) ? (String) session.getAttribute("user") : "";

        List<Map<String, String>> requestsList = new ArrayList<>();
        
        // RF-05: El empleado solo ve sus solicitudes; el Administrador/Gerente ve todas
        String sql = "EMPLOYEE".equals(role)
                ? "SELECT * FROM requests WHERE uploaded_by = ? ORDER BY id DESC"
                : "SELECT * FROM requests ORDER BY id DESC";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            if ("EMPLOYEE".equals(role)) {
                pstmt.setString(1, user);
            }

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Map<String, String> req = new HashMap<>();
                    req.put("id", String.valueOf(rs.getInt("id")));
                    req.put("concepto", rs.getString("concepto"));
                    req.put("monto", String.valueOf(rs.getDouble("monto")));
                    req.put("fecha", rs.getString("fecha"));
                    req.put("observaciones", rs.getString("observaciones"));
                    req.put("file_name", rs.getString("file_name"));
                    req.put("user", rs.getString("uploaded_by"));
                    req.put("status", rs.getString("status"));
                    req.put("feedback", rs.getString("feedback"));
                    requestsList.add(req);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        request.setAttribute("documentsList", requestsList);
    }

    private void downloadDocument(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String id = request.getParameter("id");
        if (id == null) return;

        String sql = "SELECT file_name, file_type, file_data FROM requests WHERE id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, Integer.parseInt(id));
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    String fileName = rs.getString("file_name");
                    String contentType = rs.getString("file_type");
                    byte[] fileBytes = rs.getBytes("file_data");

                    if (fileBytes != null && fileName != null) {
                        response.setContentType(contentType != null ? contentType : "application/octet-stream");
                        response.setHeader("Content-Disposition", "attachment; filename=\"" + fileName + "\"");
                        response.setContentLength(fileBytes.length);

                        try (OutputStream out = response.getOutputStream()) {
                            out.write(fileBytes);
                            out.flush();
                        }
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void deleteRequest(HttpServletRequest request) {
        String id = request.getParameter("id");
        if (id != null) {
            String sql = "DELETE FROM requests WHERE id = ?";
            try (Connection conn = DatabaseConfig.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, Integer.parseInt(id));
                pstmt.executeUpdate();
                request.setAttribute("uploadMessage", "Solicitud eliminada.");
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