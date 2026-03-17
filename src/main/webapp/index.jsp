<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Sistema de Gestión de Comisiones — Gobierno de Aragón</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.10.0/font/bootstrap-icons.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
</head>
<body>
    <header class="header-app">
        <div class="container">
            <div class="row align-items-center">
                <div class="col-md-8">
                    <h1 class="h3 mb-0">
                        <i class="bi bi-file-earmark-text"></i>
                        Sistema de Gestión de Comisiones
                    </h1>
                    <p class="mb-0 mt-1 header-subtitle">Gobierno de Aragón</p>
                </div>
                <div class="col-md-4 text-end">
                    <span class="text-white me-3 small">
                        <i class="bi bi-person-circle me-1"></i>
                        ${sessionScope.usuarioLogueado.nombreCompleto}
                    </span>
                    <a href="${pageContext.request.contextPath}/logout" class="btn btn-outline-light btn-sm">
                        <i class="bi bi-box-arrow-right me-1"></i> Cerrar sesión
                    </a>
                </div>
            </div>
        </div>
    </header>

    <div class="container mt-5">
        <div class="row justify-content-center">
            <div class="col-md-6 col-lg-5 d-flex flex-column gap-3">

                <c:if test="${rolUsuario == 'LECTURA' or rolUsuario == 'GESTOR' or rolUsuario == 'ADMIN'}">
                    <a href="${pageContext.request.contextPath}/comisiones"
                       class="btn btn-lg btn-outline-primary py-3">
                        <i class="bi bi-building me-2"></i> Ver Comisiones
                    </a>
                    <a href="${pageContext.request.contextPath}/comisiones/buscarPorDni"
                       class="btn btn-lg btn-outline-primary py-3">
                        <i class="bi bi-search me-2"></i> Buscar por DNI
                    </a>
                    <a href="${pageContext.request.contextPath}/comisiones/buscarComision"
                       class="btn btn-lg btn-outline-primary py-3">
                        <i class="bi bi-search me-2"></i> Buscar Comisión
                    </a>
                </c:if>

                <c:if test="${rolUsuario == 'GESTOR' or rolUsuario == 'ADMIN'}">
                    <a href="${pageContext.request.contextPath}/actas/new"
                       class="btn btn-lg btn-outline-secondary py-3">
                        <i class="bi bi-file-earmark-plus me-2"></i> Nueva Acta
                    </a>
                </c:if>

                <c:if test="${rolUsuario == 'ADMIN'}">
                    <a href="${pageContext.request.contextPath}/comisiones/new"
                       class="btn btn-lg btn-outline-success py-3">
                        <i class="bi bi-plus-circle me-2"></i> Nueva Comisión
                    </a>
                </c:if>

            </div>
        </div>
    </div>
</body>
</html>