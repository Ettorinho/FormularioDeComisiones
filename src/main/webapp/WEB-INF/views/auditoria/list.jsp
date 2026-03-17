<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Historial de Auditoría</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.10.0/font/bootstrap-icons.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
</head>
<body>
    <!-- Header -->
    <header class="header-app">
        <div class="container">
            <div class="row align-items-center">
                <div class="col-md-8">
                    <h1 class="h3 mb-0">
                        <i class="bi bi-shield-check"></i>
                        Sistema de Gestión de Comisiones
                    </h1>
                    <p class="mb-0 mt-1 header-subtitle">Gobierno de Aragón</p>
                </div>
                <div class="col-md-4 text-end">
                    <fmt:formatDate value="<%= new java.util.Date() %>" pattern="dd/MM/yyyy" />
                </div>
            </div>
        </div>
    </header>

    <div class="container mt-4">
        <div class="d-flex justify-content-between align-items-center mb-3">
            <h2><i class="bi bi-journal-text me-2"></i>Historial de Auditoría</h2>
            <a href="${pageContext.request.contextPath}/comisiones" class="btn btn-secondary">
                <i class="bi bi-arrow-left me-1"></i>Volver
            </a>
        </div>

        <!-- Formulario de filtro -->
        <div class="card mb-4">
            <div class="card-body">
                <form method="get" action="${pageContext.request.contextPath}/auditoria" class="row g-2 align-items-end">
                    <div class="col-md-4">
                        <label for="usuario" class="form-label">Filtrar por usuario</label>
                        <input type="text" id="usuario" name="usuario" class="form-control"
                               placeholder="Nombre de usuario AD"
                               value="<c:out value='${filtroUsuario}'/>">
                    </div>
                    <div class="col-md-2">
                        <button type="submit" class="btn btn-primary w-100">
                            <i class="bi bi-search me-1"></i>Buscar
                        </button>
                    </div>
                    <div class="col-md-2">
                        <a href="${pageContext.request.contextPath}/auditoria" class="btn btn-outline-secondary w-100">
                            <i class="bi bi-x-circle me-1"></i>Limpiar
                        </a>
                    </div>
                </form>
            </div>
        </div>

        <!-- Información de filtro activo -->
        <c:if test="${not empty filtroUsuario}">
            <div class="alert alert-info">
                <i class="bi bi-funnel me-1"></i>
                Mostrando acciones del usuario: <strong><c:out value="${filtroUsuario}"/></strong>
            </div>
        </c:if>
        <c:if test="${not empty filtroEntidad}">
            <div class="alert alert-info">
                <i class="bi bi-funnel me-1"></i>
                Mostrando acciones sobre: <strong><c:out value="${filtroEntidad}"/></strong>
                (ID: <c:out value="${filtroEntidadId}"/>)
            </div>
        </c:if>

        <!-- Tabla de auditoría -->
        <c:choose>
            <c:when test="${not empty acciones}">
                <div class="table-responsive">
                    <table class="table table-striped table-sm">
                        <thead class="table-dark">
                            <tr>
                                <th>Fecha/Hora</th>
                                <th>Usuario</th>
                                <th>Acción</th>
                                <th>Entidad</th>
                                <th>ID Entidad</th>
                                <th>Descripción</th>
                                <th>IP</th>
                            </tr>
                        </thead>
                        <tbody>
                            <c:forEach items="${acciones}" var="accion">
                                <tr>
                                    <td class="text-nowrap">
                                        <c:out value="${accion.fechaHora}"/>
                                    </td>
                                    <td><c:out value="${accion.usuario}"/></td>
                                    <td>
                                        <c:choose>
                                            <c:when test="${accion.accion == 'CREAR'}">
                                                <span class="badge bg-success"><c:out value="${accion.accion}"/></span>
                                            </c:when>
                                            <c:when test="${accion.accion == 'MODIFICAR'}">
                                                <span class="badge bg-warning text-dark"><c:out value="${accion.accion}"/></span>
                                            </c:when>
                                            <c:when test="${accion.accion == 'ELIMINAR' || accion.accion == 'BAJA'}">
                                                <span class="badge bg-danger"><c:out value="${accion.accion}"/></span>
                                            </c:when>
                                            <c:when test="${accion.accion == 'LOGIN'}">
                                                <span class="badge bg-primary"><c:out value="${accion.accion}"/></span>
                                            </c:when>
                                            <c:when test="${accion.accion == 'LOGIN_FALLIDO'}">
                                                <span class="badge bg-danger"><c:out value="${accion.accion}"/></span>
                                            </c:when>
                                            <c:otherwise>
                                                <span class="badge bg-secondary"><c:out value="${accion.accion}"/></span>
                                            </c:otherwise>
                                        </c:choose>
                                    </td>
                                    <td><c:out value="${accion.entidad}"/></td>
                                    <td><c:out value="${accion.entidadId}"/></td>
                                    <td><c:out value="${accion.descripcion}"/></td>
                                    <td class="text-nowrap"><c:out value="${accion.ipOrigen}"/></td>
                                </tr>
                            </c:forEach>
                        </tbody>
                    </table>
                </div>
                <p class="text-muted small">
                    <i class="bi bi-info-circle me-1"></i>
                    Mostrando <strong>${acciones.size()}</strong> registro(s).
                </p>
            </c:when>
            <c:otherwise>
                <div class="alert alert-info">
                    <i class="bi bi-info-circle me-1"></i>No se encontraron registros de auditoría.
                </div>
            </c:otherwise>
        </c:choose>
    </div>

    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>
