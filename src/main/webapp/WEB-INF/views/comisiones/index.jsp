<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Gestión de Comisiones</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.min.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
</head>
<body>
    <!-- Header -->
    <header class="header-app">
        <div class="container">
            <div class="row align-items-center">
                <div class="col-md-8">
                    <h1 class="h3 mb-0">
                        <i class="bi bi-people"></i>
                        Sistema de Gestión de Comisiones
                    </h1>
                    <p class="mb-0 mt-1 header-subtitle">Gobierno de Aragón</p>
                </div>
                <div class="col-md-4 text-end">
                    <span class="text-white me-3 small">
                        <i class="bi bi-person-circle me-1"></i>
                        <c:out value="${sessionScope.usuarioLogueado.nombreCompleto}"/>
                    </span>
                    <a href="${pageContext.request.contextPath}/logout" class="btn btn-outline-light btn-sm">
                        <i class="bi bi-box-arrow-right me-1"></i> Cerrar sesión
                    </a>
                </div>
            </div>
        </div>
    </header>

    <div class="container mt-4">
        <div class="card shadow-sm">
            <div class="card-header bg-primary text-white">
                <h3 class="mb-0"><i class="bi bi-people"></i> Gestión de Comisiones</h3>
            </div>
            <div class="card-body p-4">
                <p class="text-muted mb-4">Selecciona una opción para gestionar las comisiones y grupos de trabajo:</p>

                <div class="row g-4">
                    <div class="col-md-4">
                        <a href="${pageContext.request.contextPath}/comisiones/list"
                           class="btn btn-primary btn-lg w-100 h-100 d-flex flex-column justify-content-center align-items-center p-4 text-decoration-none">
                            <i class="bi bi-list-ul display-4 mb-3"></i>
                            <h5 class="mb-2">Ver Todas las Comisiones</h5>
                            <p class="mb-0 small">Listado completo de comisiones y grupos</p>
                        </a>
                    </div>
                    <div class="col-md-4">
                        <a href="${pageContext.request.contextPath}/comisiones/buscarPorDni"
                           class="btn btn-info btn-lg w-100 h-100 d-flex flex-column justify-content-center align-items-center p-4 text-decoration-none">
                            <i class="bi bi-person-search display-4 mb-3"></i>
                            <h5 class="mb-2">Buscar por DNI</h5>
                            <p class="mb-0 small">Encuentra las comisiones de un miembro</p>
                        </a>
                    </div>
                    <div class="col-md-4">
                        <a href="${pageContext.request.contextPath}/comisiones/buscarComision"
                           class="btn btn-info btn-lg w-100 h-100 d-flex flex-column justify-content-center align-items-center p-4 text-decoration-none">
                            <i class="bi bi-search display-4 mb-3"></i>
                            <h5 class="mb-2">Buscar Comisión</h5>
                            <p class="mb-0 small">Busca comisiones por nombre</p>
                        </a>
                    </div>
                </div>
            </div>
        </div>

        <a href="${pageContext.request.contextPath}/" class="btn btn-secondary mt-3">
            <i class="bi bi-house"></i> Volver al Inicio
        </a>
    </div>

    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>
