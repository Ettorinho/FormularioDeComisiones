<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<jsp:useBean id="now" class="java.util.Date" />
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Listado de Comisiones</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.min.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
</head>
<body>
    <!-- Header -->
    <header class="header-app">
        <div class="container">
            <div class="d-flex align-items-center">
                <div class="d-flex align-items-center" style="flex:1 1 0;">
                    <img src="${pageContext.request.contextPath}/resources/images/logo_salud.png"
                         alt="Gobierno de Aragón"
                         style="height:44px; width:auto;">
                </div>
                <div class="text-center" style="flex:1 1 0;">
                    <h1 class="h3 mb-0 text-white text-nowrap">
                        <i class="bi bi-file-earmark-text"></i>
                        Gestión de Comisiones
                    </h1>
                </div>
                <div class="text-end" style="flex:1 1 0;">
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
        <div class="d-flex justify-content-between align-items-center mb-3">
            <h2>Comisiones y Grupos</h2>
            <c:if test="${sessionScope.rolUsuario == 'ADMIN'}">
                <a href="${pageContext.request.contextPath}/comisiones/new" class="btn btn-primary">Nueva Comisión</a>
            </c:if>
        </div>
        <c:if test="${not empty comisiones}">
            <table class="table table-striped">
                <thead>
                    <tr>
                        <th>Nombre</th>
                        <th>Fecha Constitución</th>
                        <th>Estado</th>
                        <th>Acciones</th>
                    </tr>
                </thead>
                 <tbody>
                    <c:forEach items="${comisiones}" var="comision">
                        <tr>
                            <td><c:out value="${comision.nombre}"/></td>
                            <td><fmt:formatDate pattern="dd/MM/yyyy" value="${comision.fechaConstitucion}" /></td>
                            <td>
                                <c:choose>
                                    <c:when test="${empty comision.fechaFin}">
                                        <span class="badge bg-success">Activa</span>
                                    </c:when>
                                    <c:when test="${comision.fechaFin > now}">
                                        <span class="badge bg-success">Activa</span>
                                    </c:when>
                                    <c:otherwise>
                                        <span class="badge bg-secondary">Finalizada</span>
                                    </c:otherwise>
                                </c:choose>
                            </td>
                            <td>
                                <a href="${pageContext.request.contextPath}/comisiones/view/${comision.id}" class="btn btn-info btn-sm">Ver</a>
                            </td>
                        </tr>
                    </c:forEach>
                </tbody>
            </table>
        </c:if>
        <c:if test="${empty comisiones}">
            <div class="alert alert-info">No hay comisiones registradas.</div>
        </c:if>
        <c:if test="${totalPaginas > 1}">
            <nav aria-label="Paginación de comisiones">
                <ul class="pagination justify-content-center mt-3">
                    <li class="page-item ${paginaActual <= 1 ? 'disabled' : ''}">
                        <a class="page-link" href="${pageContext.request.contextPath}/comisiones/list?page=${paginaActual - 1}">
                            <i class="bi bi-chevron-left"></i> Anterior
                        </a>
                    </li>
                    <li class="page-item disabled">
                        <span class="page-link">Página ${paginaActual} de ${totalPaginas}</span>
                    </li>
                    <li class="page-item ${paginaActual >= totalPaginas ? 'disabled' : ''}">
                        <a class="page-link" href="${pageContext.request.contextPath}/comisiones/list?page=${paginaActual + 1}">
                            Siguiente <i class="bi bi-chevron-right"></i>
                        </a>
                    </li>
                </ul>
            </nav>
        </c:if>
        <a href="${pageContext.request.contextPath}/" class="btn btn-secondary mt-3">Volver al Inicio</a>
    </div>
</body>
</html>
