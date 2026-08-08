package com.docuflow.servlet;

import com.docuflow.util.DocumentValidator;
import java.io.IOException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Part;

@WebServlet("/upload")
@MultipartConfig(
    fileSizeThreshold = 1024 * 1024 * 2, // 2MB
    maxFileSize = 1024 * 1024 * 10,      // 10MB
    maxRequestSize = 1024 * 1024 * 50    // 50MB
)
public class DocumentUploadServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        try {
            Part filePart = request.getPart("document");
            String fileName = getFileName(filePart);

            if (fileName == null || fileName.isEmpty()) {
                request.setAttribute("uploadMessage", "Error: No se seleccionó ningún archivo.");
            } else if (!DocumentValidator.isValidExtension(fileName)) {
                request.setAttribute("uploadMessage", "Error: Formato de archivo no permitido. Solo se aceptan PDF, PNG, JPG y JPEG.");
            } else {
                // Registro simulado de archivo subido exitosamente
                request.setAttribute("uploadMessage", "Documento '" + fileName + "' subido y validado exitosamente.");
            }
        } catch (Exception e) {
            request.setAttribute("uploadMessage", "Error durante el procesamiento del archivo: " + e.getMessage());
        }

        request.getRequestDispatcher("dashboard.jsp").forward(request, response);
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