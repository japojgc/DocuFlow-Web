<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.List, java.util.Map" %>
<%
    if (session.getAttribute("user") == null) {
        response.sendRedirect("login.jsp");
        return;
    }
    String role = (String) session.getAttribute("role");
%>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <title>DocuFlow - Gestión de Solicitudes</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/css/bootstrap.min.css" rel="stylesheet">
</head>
<body class="bg-light">
    <nav class="navbar navbar-expand-lg navbar-dark bg-dark">
        <div class="container-fluid">
            <a class="navbar-brand" href="#">DocuFlow Web</a>
            <div class="d-flex align-items-center">
                <span class="navbar-text text-white me-3">
                    Usuario: <strong><%= session.getAttribute("user") %></strong> (<%= role %>)
                </span>
                <a href="logout" class="btn btn-outline-danger btn-sm">Cerrar Sesión</a>
            </div>
        </div>
    </nav>

    <div class="container-fluid mt-4 px-4">
        <div class="row">
            <!-- Formulario RF-02: Registrar Solicitud -->
            <div class="col-lg-4 mb-4">
                <div class="card shadow-sm border-0">
                    <div class="card-header bg-primary text-white">
                        <h5 class="card-title mb-0">Registrar Solicitud</h5>
                    </div>
                    <div class="card-body">
                        <% if (request.getAttribute("uploadMessage") != null) { %>
                            <div class="alert alert-info py-2" role="alert">
                                <%= request.getAttribute("uploadMessage") %>
                            </div>
                        <% } %>

                        <form action="upload" method="post" enctype="multipart/form-data">
                            <div class="mb-2">
                                <label class="form-label mb-1">Concepto</label>
                                <input type="text" class="form-control" name="concepto" placeholder="Ej. Compra de Viáticos" required>
                            </div>
                            <div class="row mb-2">
                                <div class="col-6">
                                    <label class="form-label mb-1">Monto ($)</label>
                                    <input type="number" step="0.01" class="form-control" name="monto" required>
                                </div>
                                <div class="col-6">
                                    <label class="form-label mb-1">Fecha</label>
                                    <input type="date" class="form-control" name="fecha" required>
                                </div>
                            </div>
                            <div class="mb-2">
                                <label class="form-label mb-1">Observaciones</label>
                                <textarea class="form-control" name="observaciones" rows="2"></textarea>
                            </div>
                            <div class="mb-3">
                                <label class="form-label mb-1">Adjunto (PDF, PNG, JPG)</label>
                                <input class="form-control" type="file" name="document">
                            </div>
                            <button type="submit" class="btn btn-success w-100">Enviar Solicitud</button>
                        </form>
                    </div>
                </div>
            </div>

            <!-- Tabla RF-04 y RF-05: Historial y Evaluación -->
            <div class="col-lg-8">
                <div class="card shadow-sm border-0">
                    <div class="card-header bg-secondary text-white">
                        <h5 class="card-title mb-0">Historial y Evaluación</h5>
                    </div>
                    <div class="card-body p-0">
                        <div class="table-responsive">
                            <table class="table table-hover align-middle mb-0">
                                <thead class="table-light">
                                    <tr>
                                        <th>ID</th>
                                        <th>Concepto / Detalle</th>
                                        <th>Monto</th>
                                        <th>Fecha</th>
                                        <th>Usuario</th>
                                        <th>Estatus</th>
                                        <th>Acciones</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    <%
                                        List<Map<String, String>> docs = (List<Map<String, String>>) request.getAttribute("documentsList");
                                        if (docs != null && !docs.isEmpty()) {
                                            for (Map<String, String> reqItem : docs) {
                                                String st = reqItem.get("status");
                                                String badgeClass = "PENDIENTE".equals(st) ? "bg-warning text-dark" : ("APROBADO".equals(st) ? "bg-success" : "bg-danger");
                                    %>
                                    <tr>
                                        <td><%= reqItem.get("id") %></td>
                                        <td>
                                            <strong><%= reqItem.get("concepto") %></strong><br>
                                            <small class="text-muted"><%= reqItem.get("observaciones") %></small>
                                            <% if (reqItem.get("feedback") != null && !reqItem.get("feedback").isEmpty()) { %>
                                                <br><small class="text-primary"><em>Notas de evaluación: <%= reqItem.get("feedback") %></em></small>
                                            <% } %>
                                        </td>
                                        <td>$<%= reqItem.get("monto") %></td>
                                        <td><%= reqItem.get("fecha") %></td>
                                        <td><span class="badge bg-secondary"><%= reqItem.get("user") %></span></td>
                                        <td><span class="badge <%= badgeClass %>"><%= st %></span></td>
                                        <td>
                                            <% if (reqItem.get("file_name") != null && !reqItem.get("file_name").isEmpty()) { %>
                                                <a href="upload?action=download&id=<%= reqItem.get("id") %>" class="btn btn-sm btn-outline-primary mb-1">Ver Archivo</a>
                                            <% } %>
                                            
                                            <% if ("ADMIN".equals(role) && "PENDIENTE".equals(st)) { %>
                                                <button type="button" 
                                                        class="btn btn-sm btn-success mb-1" 
                                                        data-bs-toggle="modal" 
                                                        data-bs-target="#evaluateModal" 
                                                        onclick="setupModal('<%= reqItem.get("id") %>', 'APROBADO')">
                                                    Aprobar
                                                </button>
                                                <button type="button" 
                                                        class="btn btn-sm btn-danger mb-1" 
                                                        data-bs-toggle="modal" 
                                                        data-bs-target="#evaluateModal" 
                                                        onclick="setupModal('<%= reqItem.get("id") %>', 'RECHAZADO')">
                                                    Rechazar
                                                </button>
                                            <% } %>
                                            <a href="upload?action=delete&id=<%= reqItem.get("id") %>" class="btn btn-sm btn-outline-secondary mb-1" onclick="return confirm('¿Eliminar?')">🗑</a>
                                        </td>
                                    </tr>
                                    <% 
                                            }
                                        } else {
                                    %>
                                    <tr>
                                        <td colspan="7" class="text-center text-muted py-3">No hay solicitudes registradas.</td>
                                    </tr>
                                    <% } %>
                                </tbody>
                            </table>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>

    <!-- Modal para Agregar Notas de Retroalimentación (RF-04) -->
    <div class="modal fade" id="evaluateModal" tabindex="-1" aria-labelledby="evaluateModalLabel" aria-hidden="true">
        <div class="modal-dialog">
            <div class="modal-content">
                <form action="upload" method="get">
                    <input type="hidden" name="action" value="evaluate">
                    <input type="hidden" name="id" id="modalRequestId">
                    <input type="hidden" name="status" id="modalRequestStatus">
                    
                    <div class="modal-header">
                        <h5 class="modal-title" id="evaluateModalLabel">Evaluación de Solicitud</h5>
                        <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
                    </div>
                    <div class="modal-body">
                        <div class="mb-3">
                            <label for="feedback" class="form-label">Notas / Comentarios de retroalimentación:</label>
                            <textarea class="form-control" name="feedback" id="feedback" rows="3" placeholder="Escribe las observaciones o justificación..." required></textarea>
                        </div>
                    </div>
                    <div class="modal-footer">
                        <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">Cancelar</button>
                        <button type="submit" class="btn btn-primary" id="modalSubmitBtn">Confirmar</button>
                    </div>
                </form>
            </div>
        </div>
    </div>

    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/js/bootstrap.bundle.min.js"></script>
    <script>
        function setupModal(id, status) {
            document.getElementById('modalRequestId').value = id;
            document.getElementById('modalRequestStatus').value = status;
            document.getElementById('feedback').value = '';
            
            const title = document.getElementById('evaluateModalLabel');
            const submitBtn = document.getElementById('modalSubmitBtn');
            
            if (status === 'APROBADO') {
                title.innerText = 'Aprobar Solicitud #' + id;
                submitBtn.className = 'btn btn-success';
                submitBtn.innerText = 'Aprobar Solicitud';
            } else {
                title.innerText = 'Rechazar Solicitud #' + id;
                submitBtn.className = 'btn btn-danger';
                submitBtn.innerText = 'Rechazar Solicitud';
            }
        }
    </script>
</body>
</html>