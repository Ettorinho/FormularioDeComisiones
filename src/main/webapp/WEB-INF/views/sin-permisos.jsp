<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Acceso denegado</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.min.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
</head>
<body>
<header class="header-app">
    <div class="container">
        <div class="row align-items-center">
            <div class="col">
                <h1 class="h3 mb-0">Formulario de Comisiones</h1>
            </div>
            <div class="col-auto">
                <a href="${pageContext.request.contextPath}/logout" class="btn btn-outline-light btn-sm">
                    <i class="bi bi-box-arrow-right"></i> Cerrar sesión
                </a>
            </div>
        </div>
    </div>
</header>

<div class="container mt-5">
    <div class="row justify-content-center">
        <div class="col-md-6 text-center">
            <div class="alert alert-warning shadow-sm p-4">
                <i class="bi bi-shield-lock display-4 text-warning mb-3 d-block"></i>
                <h4 class="alert-heading">Acceso denegado</h4>
                <p>No tienes permisos suficientes para acceder a esta sección.</p>
                <hr>
                <p class="mb-0 text-muted small">
                    Contacta con el administrador del sistema para solicitar el acceso necesario.
                </p>
            </div>
            <a href="${pageContext.request.contextPath}/comisiones" class="btn btn-primary mt-3">
                <i class="bi bi-arrow-left"></i> Volver al inicio
            </a>
        </div>
    </div>
</div>
</body>
</html>
