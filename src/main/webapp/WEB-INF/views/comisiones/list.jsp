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
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.10.0/font/bootstrap-icons.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
</head>
<body>
    <!-- Header -->
    <header style="background: linear-gradient(135deg, #004B87 0%, #003366 100%); color: white; padding: 1.5rem 0; box-shadow: 0 2px 4px rgba(0,0,0,0.1);">
        <div class="container">
            <div class="row align-items-center">
                <div class="col-md-8">
                    <h1 class="h3 mb-0">
                        <i class="bi bi-file-earmark-text"></i>
                        Sistema de Gestión de Comisiones
                    </h1>
                    <p class="mb-0 mt-1" style="opacity: 0.9;">Gobierno de Aragón</p>
                </div>
                <div class="col-md-4 text-end">
                    <fmt:formatDate value="<%= new java.util.Date() %>" pattern="dd/MM/yyyy" />
                </div>
            </div>
        </div>
    </header>

    <div class="container mt-4">
        <div class="d-flex justify-content-between align-items-center mb-3">
            <h2>Comisiones y Grupos</h2>
            <a href="${pageContext.request.contextPath}/comisiones/new" class="btn btn-primary">Nueva Comisión</a>
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