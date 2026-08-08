<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>DocuFlow - Dashboard</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/css/bootstrap.min.css" rel="stylesheet">
</head>
<body class="bg-light">
    <nav class="navbar navbar-expand-lg navbar-dark bg-dark">
        <div class="container-fluid">
            <a class="navbar-brand" href="#">DocuFlow Web</a>
            <span class="navbar-text text-white">
                Usuario: <strong><%= session.getAttribute("user") != null ? session.getAttribute("user") : "Invitado" %></strong>
            </span>
        </div>
    </nav>

    <div class="container mt-5">
        <div class="row justify-content-center">
            <div class="col-md-8">
                <div class="card shadow-sm border-0">
                    <div class="card-header bg-primary text-white">
                        <h5 class="card-title mb-0">Gestión y Carga de Documentos</h5>
                    </div>
                    <div class="card-body">
                        <% if (request.getAttribute("uploadMessage") != null) { %>
                            <div class="alert alert-info" role="alert">
                                <%= request.getAttribute("uploadMessage") %>
                            </div>
                        <% } %>

                        <form action="upload" method="post" enctype="multipart/form-data">
                            <div class="mb-3">
                                <label for="document" class="form-label">Seleccionar documento (PDF, PNG, JPG)</label>
                                <input class="form-control" type="file" id="document" name="document" required>
                            </div>
                            <button type="submit" class="btn btn-success">Subir Archivo</button>
                        </form>
                    </div>
                </div>
            </div>
        </div>
    </div>
</body>
</html>