<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<jsp:useBean id="now" class="java.util.Date" />
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Sistema de Gestión de Comisiones — Gobierno de Aragón</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.min.css">
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

    <div class="container mt-4">
        <div class="card">
            <div class="card-header">
                <h3>Menú principal</h3>
            </div>
            <div class="card-body">
                <div class="d-flex flex-column gap-2">

                    <%-- Opciones visibles para TODOS los roles autenticados --%>
                    <c:if test="${sessionScope.rolUsuario == 'LECTURA' || sessionScope.rolUsuario == 'GESTOR' || sessionScope.rolUsuario == 'ADMIN'}">
                        <a href="${pageContext.request.contextPath}/comisiones" class="btn btn-primary btn-lg text-start">
                            <i class="bi bi-people"></i> Ver Comisiones
                        </a>
                        <a href="${pageContext.request.contextPath}/comisiones/buscarPorDni" class="btn btn-outline-primary btn-lg text-start">
                            <i class="bi bi-search"></i> Buscar por DNI
                        </a>
                        <a href="${pageContext.request.contextPath}/comisiones/buscarComision" class="btn btn-outline-primary btn-lg text-start">
                            <i class="bi bi-building"></i> Buscar Comisión
                        </a>
                    </c:if>

                    <%-- Opciones para GESTOR y ADMIN --%>
                    <c:if test="${sessionScope.rolUsuario == 'GESTOR' || sessionScope.rolUsuario == 'ADMIN'}">
                        <a href="${pageContext.request.contextPath}/actas/new" class="btn btn-outline-secondary btn-lg text-start">
                            <i class="bi bi-file-earmark-plus"></i> Nueva Acta
                        </a>
                    </c:if>

                    <%-- Opciones solo para ADMIN --%>
                    <c:if test="${sessionScope.rolUsuario == 'ADMIN'}">
                        <a href="${pageContext.request.contextPath}/comisiones/new" class="btn btn-outline-secondary btn-lg text-start">
                            <i class="bi bi-plus-circle"></i> Nueva Comisión
                        </a>
                    </c:if>

                    <%-- Fallback: sin rol asignado --%>
                    <c:if test="${empty sessionScope.rolUsuario}">
                        <div class="alert alert-warning">
                            No tiene ningún rol asignado. Contacte con el administrador del sistema.
                        </div>
                    </c:if>

                </div>
            </div>
        </div>
        <a href="${pageContext.request.contextPath}/" class="btn btn-secondary mt-3">Volver al Inicio</a>
    </div>
</body>
</html>