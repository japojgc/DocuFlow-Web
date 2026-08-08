package com.docuflow.servlet;

import java.io.IOException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet("/login")
public class AuthServlet extends HttpServlet {

    // Maneja la petición al escribir la URL directamente en el navegador
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.getRequestDispatcher("login.jsp").forward(request, response);
    }

    // Maneja el envío del formulario de inicio de sesión
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        String username = request.getParameter("username");
        String password = request.getParameter("password");

        if ("admin".equals(username) && "admin123".equals(password)) {
            HttpSession session = request.getSession();
            session.setAttribute("user", username);
            session.setAttribute("role", "ADMIN");
            response.sendRedirect("upload");
        } else if ("user".equals(username) && "user123".equals(password)) {
            HttpSession session = request.getSession();
            session.setAttribute("user", username);
            session.setAttribute("role", "EMPLOYEE");
            response.sendRedirect("upload");
        } else {
            request.setAttribute("errorMessage", "Usuario o contraseña incorrectos");
            request.getRequestDispatcher("login.jsp").forward(request, response);
        }
    }
}