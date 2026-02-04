<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Acta de Reunión</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.10.0/font/bootstrap-icons.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
</head>
<body>
    <!-- Header -->
    <header class="header-aragon no-print">
        <div class="container">
            <div class="row align-items-center">
                <div class="col-md-8">
                    <h1 class="h3 mb-0">
                        <i class="bi bi-file-earmark-text"></i>
                        Sistema de Gestión de Comisiones
                    </h1>
                    <p class="mb-0 mt-1" style="opacity: 0.9;">Gestión de Actas</p>
                </div>
                <div class="col-md-4 text-end">
                    <fmt:formatDate value="<%= new java.util.Date() %>" pattern="dd/MM/yyyy" />
                </div>
            </div>
        </div>
    </header>

    <!-- Navbar -->
    <nav class="navbar navbar-expand-lg navbar-custom no-print">
        <div class="container">
            <button class="navbar-toggler" type="button" data-bs-toggle="collapse" data-bs-target="#navbarNav">
                <span class="navbar-toggler-icon"></span>
            </button>
            <div class="collapse navbar-collapse" id="navbarNav">
                <ul class="navbar-nav">
                    <li class="nav-item">
                        <a class="nav-link" href="${pageContext.request.contextPath}/">
                            <i class="bi bi-house-door"></i> Inicio
                        </a>
                    </li>
                    <li class="nav-item">
                        <a class="nav-link" href="${pageContext.request.contextPath}/comisiones">
                            <i class="bi bi-people"></i> Comisiones
                        </a>
                    </li>
                    <li class="nav-item">
                        <a class="nav-link active" href="${pageContext.request. contextPath}/actas/new">
                            <i class="bi bi-file-earmark-plus"></i> Actas
                        </a>
                    </li>
                    <li class="nav-item">
                        <a class="nav-link" href="${pageContext. request.contextPath}/comisiones/buscarPorDni">
                            <i class="bi bi-search"></i> Buscar Miembros
                        </a>
                    </li>
                </ul>
            </div>
        </div>
    </nav>

    <!-- Contenido Principal -->
    <div class="container mt-4 mb-5">
        
        <!-- Breadcrumb -->
        <nav aria-label="breadcrumb" class="no-print">
            <ol class="breadcrumb">
                <li class="breadcrumb-item"><a href="${pageContext. request.contextPath}/">Inicio</a></li>
                <li class="breadcrumb-item"><a href="${pageContext.request. contextPath}/actas/new">Actas</a></li>
                <li class="breadcrumb-item active">Ver Acta #${acta.id}</li>
            </ol>
        </nav>

        <c:if test="${not empty error}">
            <div class="alert alert-danger alert-dismissible fade show" role="alert">
                <i class="bi bi-exclamation-triangle"></i> <strong>Error:</strong> ${error}
                <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
            </div>
        </c:if>

        <c:if test="${not empty acta}">
            <div class="card shadow-sm">
                <div class="card-header bg-primary text-white d-flex justify-content-between align-items-center">
                    <h3 class="mb-0">
                        <i class="bi bi-file-earmark-text"></i> Acta de Reunión #${acta. id}
                    </h3>
                    <div class="no-print">
                        <button onclick="window.print()" class="btn btn-light btn-sm me-2">
                            <i class="bi bi-printer"></i> Imprimir
                        </button>
                        <a href="${pageContext.request.contextPath}/actas/new" class="btn btn-light btn-sm">
                            <i class="bi bi-plus-circle"></i> Nueva Acta
                        </a>
                    </div>
                </div>
                
                <div class="card-body">
                    
                    <!-- Información General -->
                    <div class="acta-header mb-4">
                        <h4 class="text-primary mb-3">
                            <i class="bi bi-building"></i> ${acta.comision.nombre}
                        </h4>
                        <div class="row">
                            <div class="col-md-6">
                                <c:if test="${not empty acta.comision.area}">
                                    <p class="mb-2">
                                        <strong><i class="bi bi-diagram-3"></i> Área: </strong>
                                        <span class="badge bg-info ms-2">
                                            ${acta. comision.area == 'ATENCION_ESPECIALIZADA' ? 'Atención Especializada' : 'Atención Primaria'}
                                        </span>
                                    </p>
                                </c:if>
                                <p class="mb-2">
                                    <strong><i class="bi bi-calendar-event"></i> Fecha de Reunión:</strong>
                                    <span class="ms-2">
                                        <fmt:formatDate value="${acta.fechaReunion}" pattern="dd/MM/yyyy" />
                                    </span>
                                </p>
                            </div>
                            <div class="col-md-6">
                                <p class="mb-0 text-muted">
                                    <small>
                                        <i class="bi bi-clock-history"></i> Acta creada el: 
                                        <fmt:formatDate value="${acta.fechaCreacion}" pattern="dd/MM/yyyy 'a las' HH:mm" />
                                    </small>
                                </p>
                            </div>
                        </div>
                    </div>

                    <!-- Estadísticas de Asistencia -->
                    <c:set var="contadorAsistieron" value="0"/>
                    <c:set var="contadorNoAsistieron" value="0"/>
                    <c:forEach var="asistencia" items="${asistencias}">
                        <c:if test="${asistencia. asistio}">
                            <c:set var="contadorAsistieron" value="${contadorAsistieron + 1}"/>
                        </c:if>
                        <c:if test="${! asistencia.asistio}">
                            <c:set var="contadorNoAsistieron" value="${contadorNoAsistieron + 1}"/>
                        </c:if>
                    </c:forEach>

                    <div class="row mb-4 no-print">
                        <div class="col-md-4">
                            <div class="stats-box" style="background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);">
                                <h3>${contadorAsistieron + contadorNoAsistieron}</h3>
                                <p>Total Miembros</p>
                            </div>
                        </div>
                        <div class="col-md-4">
                            <div class="stats-box" style="background: linear-gradient(135deg, #28a745 0%, #20c997 100%);">
                                <h3>${contadorAsistieron}</h3>
                                <p>Asistieron</p>
                            </div>
                        </div>
                        <div class="col-md-4">
                            <div class="stats-box" style="background: linear-gradient(135deg, #dc3545 0%, #e83e8c 100%);">
                                <h3>${contadorNoAsistieron}</h3>
                                <p>No Asistieron</p>
                            </div>
                        </div>
                    </div>

                    <!-- Lista de Asistencia -->
                    <div class="mb-4">
                        <h5 class="border-bottom pb-2 mb-3">
                            <i class="bi bi-person-check"></i> Registro de Asistencia
                        </h5>
                        <div class="row">
                            <div class="col-md-6">
                                <h6 class="text-success">
                                    <i class="bi bi-check-circle"></i> Asistieron (${contadorAsistieron})
                                </h6>
                                <ul class="asistencia-lista">
                                    <c:set var="hayAsistentes" value="false"/>
                                    <c:forEach var="asistencia" items="${asistencias}">
                                        <c:if test="${asistencia. asistio}">
                                            <c:set var="hayAsistentes" value="true"/>
                                            <li class="asistio">
                                                <i class="bi bi-person-fill text-success"></i>
                                                <strong>${asistencia.miembro. nombreApellidos}</strong>
                                                <br>
                                                <small class="text-muted ms-3">
                                                    <i class="bi bi-card-text"></i> ${asistencia. miembro. dniNif}
                                                </small>
                                            </li>
                                        </c:if>
                                    </c:forEach>
                                    <c:if test="${!hayAsistentes}">
                                        <li class="text-muted fst-italic">
                                            <i class="bi bi-info-circle"></i> Ningún miembro asistió
                                        </li>
                                    </c:if>
                                </ul>
                            </div>
                            <div class="col-md-6">
                                <h6 class="text-danger">
                                    <i class="bi bi-x-circle"></i> No Asistieron (${contadorNoAsistieron})
                                </h6>
                                <ul class="asistencia-lista">
                                    <c:set var="hayAusentes" value="false"/>
                                    <c:forEach var="asistencia" items="${asistencias}">
                                        <c:if test="${! asistencia.asistio}">
                                            <c:set var="hayAusentes" value="true"/>
                                            <li class="no-asistio">
                                                <i class="bi bi-person text-danger"></i>
                                                <strong>${asistencia. miembro.nombreApellidos}</strong>
                                                <br>
                                                <small class="text-muted ms-3">
                                                    <i class="bi bi-card-text"></i> ${asistencia. miembro.dniNif}
                                                </small>
                                            </li>
                                        </c:if>
                                    </c:forEach>
                                    <c:if test="${!hayAusentes}">
                                        <li class="text-muted fst-italic">
                                            <i class="bi bi-info-circle"></i> Todos los miembros asistieron
                                        </li>
                                    </c:if>
                                </ul>
                            </div>
                        </div>
                    </div>

                    <!-- Observaciones -->
                    <div class="mb-4">
                        <h5 class="border-bottom pb-2 mb-3">
                            <i class="bi bi-file-text"></i> Observaciones / Notas de la Reunión
                        </h5>
                        <c:choose>
                            <c:when test="${not empty acta.observaciones}">
                                <div class="observaciones-box">
                                    <p class="mb-0" style="white-space: pre-wrap; line-height: 1.6;">${acta.observaciones}</p>
                                </div>
                            </c:when>
                            <c:otherwise>
                                <p class="text-muted fst-italic">
                                    <i class="bi bi-info-circle"></i> No se registraron observaciones
                                </p>
                            </c:otherwise>
                        </c:choose>
                    </div>

                </div>
                
                <div class="card-footer bg-light no-print">
                    <div class="d-flex justify-content-between align-items-center">
                        <div>
                            <a href="${pageContext.request.contextPath}/" class="btn btn-secondary">
                                <i class="bi bi-house"></i> Inicio
                            </a>
                            <a href="${pageContext.request.contextPath}/actas/new" class="btn btn-primary">
                                <i class="bi bi-plus-circle"></i> Nueva Acta
                            </a>
                        </div>
                        <button onclick="window.print()" class="btn btn-outline-primary">
                            <i class="bi bi-printer"></i> Imprimir
                        </button>
                    </div>
                </div>
            </div>
        </c:if>
        
    </div>

    <!-- Footer -->
    <footer class="bg-dark text-white py-3 mt-5 no-print">
        <div class="container text-center">
            <p class="mb-0">
                &copy; <fmt:formatDate value="<%= new java.util.Date() %>" pattern="yyyy" /> 
                Actas de Reunión
            </p>
        </div>
    </footer>

    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>