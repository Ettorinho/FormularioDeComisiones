<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Auditoría de Seguridad</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.10.0/font/bootstrap-icons.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
</head>
<body>
    <header class="header-app">
        <div class="container">
            <div class="row align-items-center">
                <div class="col-md-8">
                    <h1 class="h3 mb-0"><i class="bi bi-shield-check"></i> Panel de Auditoría</h1>
                    <p class="mb-0 mt-1 header-subtitle">Monitoreo de seguridad y rendimiento</p>
                </div>
                <div class="col-md-4 text-end">
                    <fmt:formatDate value="<%= new java.util.Date() %>" pattern="dd/MM/yyyy" />
                </div>
            </div>
        </div>
    </header>

    <div class="container mt-4">
        <div class="d-flex justify-content-between align-items-center mb-3">
            <h2><i class="bi bi-journal-text me-2"></i>Registros de auditoría</h2>
            <a href="${pageContext.request.contextPath}/comisiones" class="btn btn-secondary">
                <i class="bi bi-arrow-left me-1"></i>Volver
            </a>
        </div>

        <div class="card mb-4">
            <div class="card-body">
                <form method="get" action="${pageContext.request.contextPath}/auditoria" class="row g-2 align-items-end">
                    <div class="col-md-3">
                        <label for="usuario" class="form-label">Usuario</label>
                        <input type="text" id="usuario" name="usuario" class="form-control" value="<c:out value='${filtroUsuario}'/>">
                    </div>
                    <div class="col-md-2">
                        <label for="resultado" class="form-label">Resultado</label>
                        <select id="resultado" name="resultado" class="form-select">
                            <option value="">Todos</option>
                            <option value="EXITOSO" ${filtroResultado == 'EXITOSO' ? 'selected' : ''}>EXITOSO</option>
                            <option value="FALLIDO" ${filtroResultado == 'FALLIDO' ? 'selected' : ''}>FALLIDO</option>
                            <option value="DENEGADO" ${filtroResultado == 'DENEGADO' ? 'selected' : ''}>DENEGADO</option>
                        </select>
                    </div>
                    <div class="col-md-2">
                        <label for="fechaDesde" class="form-label">Desde</label>
                        <input type="date" id="fechaDesde" name="fechaDesde" class="form-control" value="<c:out value='${filtroFechaDesde}'/>">
                    </div>
                    <div class="col-md-2">
                        <label for="fechaHasta" class="form-label">Hasta</label>
                        <input type="date" id="fechaHasta" name="fechaHasta" class="form-control" value="<c:out value='${filtroFechaHasta}'/>">
                    </div>
                    <div class="col-md-2">
                        <label for="ip" class="form-label">IP</label>
                        <input type="text" id="ip" name="ip" class="form-control" value="<c:out value='${filtroIp}'/>">
                    </div>
                    <div class="col-md-1 d-grid">
                        <button type="submit" class="btn btn-primary"><i class="bi bi-search"></i></button>
                    </div>
                </form>
                <div class="mt-2">
                    <a href="${pageContext.request.contextPath}/auditoria" class="btn btn-outline-secondary btn-sm">
                        <i class="bi bi-x-circle me-1"></i>Limpiar filtros
                    </a>
                </div>
            </div>
        </div>

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
                                <th>ID</th>
                                <th>Resultado</th>
                                <th>Duración (ms)</th>
                                <th>IP</th>
                                <th>User-Agent</th>
                                <th>Error</th>
                            </tr>
                        </thead>
                        <tbody>
                            <c:forEach items="${acciones}" var="accion">
                                <tr>
                                    <td class="text-nowrap"><c:out value="${accion.fechaHora}"/></td>
                                    <td><c:out value="${accion.usuario}"/></td>
                                    <td><c:out value="${accion.accion}"/></td>
                                    <td><c:out value="${accion.entidad}"/></td>
                                    <td><c:out value="${accion.entidadId}"/></td>
                                    <td>
                                        <c:choose>
                                            <c:when test="${accion.resultado == 'EXITOSO'}">
                                                <span class="badge bg-success">EXITOSO</span>
                                            </c:when>
                                            <c:when test="${accion.resultado == 'FALLIDO'}">
                                                <span class="badge bg-danger">FALLIDO</span>
                                            </c:when>
                                            <c:when test="${accion.resultado == 'DENEGADO'}">
                                                <span class="badge bg-warning text-dark">DENEGADO</span>
                                            </c:when>
                                            <c:otherwise>
                                                <span class="badge bg-secondary"><c:out value="${accion.resultado}"/></span>
                                            </c:otherwise>
                                        </c:choose>
                                    </td>
                                    <td><c:out value="${accion.duracionMs}"/></td>
                                    <td class="text-nowrap"><c:out value="${accion.ipOrigen}"/></td>
                                    <td><c:out value="${accion.userAgent}"/></td>
                                    <td><c:out value="${accion.mensajeError}"/></td>
                                </tr>
                            </c:forEach>
                        </tbody>
                    </table>
                </div>
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
