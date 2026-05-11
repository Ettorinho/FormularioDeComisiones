<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Monitoreo de Auditoría</title>
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
            <h2><i class="bi bi-shield-exclamation me-2"></i>Monitoreo de Auditoría</h2>
            <a href="${pageContext.request.contextPath}/comisiones" class="btn btn-secondary">
                <i class="bi bi-arrow-left me-1"></i>Volver
            </a>
        </div>

        <!-- Intentos de acceso denegados (últimas 24 h) -->
        <c:if test="${not empty intentosDenegados}">
        <div class="card mb-4 border-danger">
            <div class="card-header bg-danger text-white">
                <i class="bi bi-exclamation-triangle-fill me-1"></i>
                Intentos de Acceso Denegados (Últimas 24 h)
                <span class="badge bg-light text-danger ms-2">${intentosDenegados.size()}</span>
            </div>
            <div class="card-body p-0">
                <div class="table-responsive">
                    <table class="table table-sm table-striped mb-0">
                        <thead class="table-danger">
                            <tr>
                                <th>Fecha/Hora</th>
                                <th>Usuario</th>
                                <th>IP</th>
                                <th>URL</th>
                                <th>Detalle</th>
                            </tr>
                        </thead>
                        <tbody>
                            <c:forEach items="${intentosDenegados}" var="d">
                                <tr>
                                    <td class="text-nowrap"><c:out value="${d.fechaHora}"/></td>
                                    <td><c:out value="${d.usuario}"/></td>
                                    <td class="text-nowrap"><c:out value="${d.ipOrigen}"/></td>
                                    <td><c:out value="${d.urlSolicitada}"/></td>
                                    <td><c:out value="${d.descripcion}"/></td>
                                </tr>
                            </c:forEach>
                        </tbody>
                    </table>
                </div>
            </div>
        </div>
        </c:if>

        <!-- Actividad por IP (últimos 7 días) -->
        <c:if test="${not empty actividadPorIp}">
        <div class="card mb-4">
            <div class="card-header">
                <i class="bi bi-geo-alt me-1"></i>Actividad por Dirección IP (Últimos 7 días)
            </div>
            <div class="card-body p-0">
                <div class="table-responsive">
                    <table class="table table-sm table-striped mb-0">
                        <thead class="table-dark">
                            <tr>
                                <th>IP</th>
                                <th>Nº de Acciones</th>
                            </tr>
                        </thead>
                        <tbody>
                            <c:forEach items="${actividadPorIp}" var="entry">
                                <tr>
                                    <td><c:out value="${entry.key}"/></td>
                                    <td><c:out value="${entry.value}"/></td>
                                </tr>
                            </c:forEach>
                        </tbody>
                    </table>
                </div>
            </div>
        </div>
        </c:if>

        <!-- Formulario de filtro -->
        <div class="card mb-4">
            <div class="card-body">
                <form method="get" action="${pageContext.request.contextPath}/auditoria" class="row g-2 align-items-end">
                    <div class="col-md-3">
                        <label for="usuario" class="form-label">Usuario</label>
                        <input type="text" id="usuario" name="usuario" class="form-control"
                               placeholder="Nombre de usuario AD"
                               value="<c:out value='${filtroUsuario}'/>">
                    </div>
                    <div class="col-md-3">
                        <label for="resultado" class="form-label">Resultado</label>
                        <select id="resultado" name="resultado" class="form-select">
                            <option value="">-- Todos --</option>
                            <option value="EXITOSO"  ${filtroResultado == 'EXITOSO'  ? 'selected' : ''}>EXITOSO</option>
                            <option value="FALLIDO"  ${filtroResultado == 'FALLIDO'  ? 'selected' : ''}>FALLIDO</option>
                            <option value="DENEGADO" ${filtroResultado == 'DENEGADO' ? 'selected' : ''}>DENEGADO</option>
                        </select>
                    </div>
                    <div class="col-md-2">
                        <button type="submit" class="btn btn-primary w-100">
                            <i class="bi bi-search me-1"></i>Filtrar
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

        <!-- Alertas de filtro activo -->
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
        <c:if test="${not empty filtroResultado}">
            <div class="alert alert-info">
                <i class="bi bi-funnel me-1"></i>
                Filtrando por resultado: <strong><c:out value="${filtroResultado}"/></strong>
            </div>
        </c:if>

        <!-- Registro de auditoría -->
        <div class="card">
            <div class="card-header">
                <i class="bi bi-journal-text me-1"></i>Registro de Auditoría
            </div>
            <div class="card-body p-0">
                <c:choose>
                    <c:when test="${not empty acciones}">
                        <div class="table-responsive">
                            <table class="table table-striped table-sm mb-0">
                                <thead class="table-dark">
                                    <tr>
                                        <th>Fecha/Hora</th>
                                        <th>Usuario</th>
                                        <th>Acción</th>
                                        <th>Resultado</th>
                                        <th>IP</th>
                                        <th>URL</th>
                                        <th>Entidad</th>
                                        <th>Descripción</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    <c:forEach items="${acciones}" var="accion">
                                        <tr class="${accion.resultado == 'FALLIDO' || accion.resultado == 'DENEGADO' ? 'table-danger' : ''}">
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
                                                    <c:when test="${accion.accion == 'LOGIN_FALLIDO' || accion.accion == 'ACCESO_DENEGADO'}">
                                                        <span class="badge bg-danger"><c:out value="${accion.accion}"/></span>
                                                    </c:when>
                                                    <c:otherwise>
                                                        <span class="badge bg-secondary"><c:out value="${accion.accion}"/></span>
                                                    </c:otherwise>
                                                </c:choose>
                                            </td>
                                            <td>
                                                <c:choose>
                                                    <c:when test="${accion.resultado == 'EXITOSO'}">
                                                        <span class="badge bg-success"><c:out value="${accion.resultado}"/></span>
                                                    </c:when>
                                                    <c:when test="${accion.resultado == 'DENEGADO'}">
                                                        <span class="badge bg-danger"><c:out value="${accion.resultado}"/></span>
                                                    </c:when>
                                                    <c:when test="${accion.resultado == 'FALLIDO'}">
                                                        <span class="badge bg-warning text-dark"><c:out value="${accion.resultado}"/></span>
                                                    </c:when>
                                                    <c:otherwise>
                                                        <span class="badge bg-secondary"><c:out value="${accion.resultado}"/></span>
                                                    </c:otherwise>
                                                </c:choose>
                                            </td>
                                            <td class="text-nowrap"><c:out value="${accion.ipOrigen}"/></td>
                                            <td><c:out value="${accion.urlSolicitada}"/></td>
                                            <td><c:out value="${accion.entidad}"/></td>
                                            <td><c:out value="${accion.descripcion}"/></td>
                                        </tr>
                                    </c:forEach>
                                </tbody>
                            </table>
                        </div>
                        <div class="p-3 text-muted small">
                            <i class="bi bi-info-circle me-1"></i>
                            Mostrando <strong>${acciones.size()}</strong> registro(s).
                        </div>
                    </c:when>
                    <c:otherwise>
                        <div class="p-3">
                            <div class="alert alert-info mb-0">
                                <i class="bi bi-info-circle me-1"></i>No se encontraron registros de auditoría.
                            </div>
                        </div>
                    </c:otherwise>
                </c:choose>
            </div>
        </div>
    </div>

    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>
