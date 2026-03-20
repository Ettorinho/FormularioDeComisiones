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
        <a href="${pageContext.request.contextPath}/" class="btn btn-secondary mt-3">Volver al Inicio</a>
    </div>
</body>
</html>