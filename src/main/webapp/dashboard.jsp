<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.List, java.util.Map" %>
<%
    if (session.getAttribute("user") == null) {
        response.sendRedirect("login.jsp");
        return;
    }
%>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <title>DocuFlow - Dashboard</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/css/bootstrap.min.css" rel="stylesheet">
</head>
<body class="bg-light">
    <nav class="navbar navbar-expand-lg navbar-dark bg-dark">
        <div class="container-fluid">
            <a class="navbar-brand" href="#">DocuFlow Web</a>
            <div class="d-flex align-items-center">
                <span class="navbar-text text-white me-3">
                    Usuario: <strong><%= session.getAttribute("user") %></strong> (<%= session.getAttribute("role") %>)
                </span>
                <a href="logout" class="btn btn-outline-danger btn-sm">Cerrar Sesión</a>
            </div>
        </div>
    </nav>

    <div class="container mt-4">
        <div class="row">
            <!-- Formulario de Carga -->
            <div class="col-md-4 mb-4">
                <div class="card shadow-sm">
                    <div class="card-header bg-primary text-white">
                        <h5 class="card-title mb-0">Subir Documento</h5>
                    </div>
                    <div class="card-body">
                        <% if (request.getAttribute("uploadMessage") != null) { %>
                            <div class="alert alert-info py-2" role="alert">
                                <%= request.getAttribute("uploadMessage") %>
                            </div>
                        <% } %>

                        <form action="upload" method="post" enctype="multipart/form-data">
                            <div class="mb-3">
                                <label for="document" class="form-label">Seleccionar archivo</label>
                                <input class="form-control" type="file" id="document" name="document" required>
                            </div>
                            <button type="submit" class="btn btn-success w-100">Subir Archivo</button>
                        </form>
                    </div>
                </div>
            </div>

            <!-- Tabla de Documentos Persistidos -->
            <div class="col-md-8">
                <div class="card shadow-sm">
                    <div class="card-header bg-secondary text-white">
                        <h5 class="card-title mb-0">Documentos Registrados</h5>
                    </div>
                    <div class="card-body">
                        <table class="table table-hover align-middle">
                            <thead class="table-light">
                                <tr>
                                    <th>ID</th>
                                    <th>Nombre del Archivo</th>
                                    <th>Subido por</th>
                                    <th>Acciones</th>
                                </tr>
                            </thead>
                            <tbody>
                                <%
                                    List<Map<String, String>> docs = (List<Map<String, String>>) request.getAttribute("documentsList");
                                    if (docs != null && !docs.isEmpty()) {
                                        for (Map<String, String> doc : docs) {
                                %>
                                <tr>
                                    <td><%= doc.get("id") %></td>
                                    <td><%= doc.get("name") %></td>
                                    <td><span class="badge bg-info text-dark"><%= doc.get("user") %></span></td>
                                    <td>
                                        <a href="#" onclick="alert('Descargando <%= doc.get("name") %>...')" class="btn btn-sm btn-outline-primary">Descargar</a>
                                        <a href="upload?action=delete&id=<%= doc.get("id") %>" class="btn btn-sm btn-outline-danger" onclick="return confirm('¿Eliminar este documento?')">Eliminar</a>
                                    </td>
                                </tr>
                                <% 
                                        }
                                    } else {
                                %>
                                <tr>
                                    <td colspan="4" class="text-center text-muted">No hay documentos cargados en el sistema.</td>
                                </tr>
                                <% } %>
                            </tbody>
                        </table>
                    </div>
                </div>
            </div>
        </div>
    </div>
</body>
</html>