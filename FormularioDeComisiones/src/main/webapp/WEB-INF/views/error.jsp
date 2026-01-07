<%@ page isErrorPage="true" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Error</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
</head>
<body>
<div class="container mt-5">
    <div class="alert alert-danger">
        <h4 class="alert-heading">Ha ocurrido un error</h4>
        <p>Se ha producido un error inesperado en la aplicación.</p>
        <hr>
        <p class="mb-0">
            <strong>Mensaje:</strong>
            ${error != null ? error : "No se proporcionó un mensaje de error específico."}
        </p>
    </div>
    <a href="${pageContext.request.contextPath}/" class="btn btn-primary">Volver al inicio</a>
</div>
<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>