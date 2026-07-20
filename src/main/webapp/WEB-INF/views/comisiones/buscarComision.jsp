<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<jsp:useBean id="now" class="java.util.Date" />
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Buscar Comisión o Grupo de Trabajo</title>
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
    <h2>Buscar Comisión o Grupo de Trabajo</h2>
    <form action="${pageContext.request.contextPath}/comisiones/buscarComision" method="post" class="mb-4">
        <div class="row g-3 align-items-end">
            <div class="col-auto">
                <label for="nombre" class="form-label">Nombre de comisión o grupo</label>
                <input type="text" name="nombre" id="nombre" class="form-control"
                       placeholder="Introduce nombre" required
                       value="<c:out value='${nombreBuscado != null ? nombreBuscado : ""}'/>">
            </div>
            <div class="col-auto">
                <button type="submit" class="btn btn-primary">🔍 Buscar</button>
            </div>
        </div>

        <!-- Filtros de estado -->
        <c:if test="${not empty nombreBuscado && not empty comisiones}">
            <div class="mt-3">
                <label class="form-label fw-bold">Filtrar por estado:</label>
                <div class="form-check form-check-inline">
                    <input class="form-check-input" type="checkbox" id="mostrarActivas" checked />
                    <label class="form-check-label" for="mostrarActivas">
                        <span class="badge badge-activa">Activas</span>
                    </label>
                </div>
                <div class="form-check form-check-inline">
                    <input class="form-check-input" type="checkbox" id="mostrarFinalizadas" checked />
                    <label class="form-check-label" for="mostrarFinalizadas">
                        <span class="badge badge-finalizada">Finalizadas</span>
                    </label>
                </div>
            </div>
        </c:if>
    </form>

    <c:if test="${not empty nombreBuscado}">
        <c:choose>
            <c:when test="${empty comisiones}">
                <div class="alert alert-warning">No se encontraron comisiones o grupos con ese nombre.</div>
            </c:when>
            <c:otherwise>
                <!-- Contador -->
                <div class="card mb-3">
                    <div class="card-footer text-muted">
                        <small>
                            <strong>Total: </strong>
                            <span id="totalComisiones">${comisiones.size()}</span> comisión(es) |
                            <span id="totalActivas" class="text-success">0</span> activa(s) |
                            <span id="totalFinalizadas" class="text-secondary">0</span> finalizada(s) |
                            <span id="totalVisibles">0</span> visible(s)
                        </small>
                    </div>
                </div>

                <c:forEach var="comision" items="${comisiones}">
                    <c:set var="esActiva" value="${empty comision.fechaFin || comision.fechaFin gt now}" />

                    <div class="card mb-4 card-comision" data-estado="${esActiva ? 'activa' : 'finalizada'}">
                        <div class="card-header d-flex justify-content-between align-items-center">
                            <strong><c:out value="${comision.nombre}"/></strong>
                            <c:choose>
                                <c:when test="${esActiva}">
                                    <span class="badge badge-activa">Activa</span>
                                </c:when>
                                <c:otherwise>
                                    <span class="badge badge-finalizada">Finalizada</span>
                                </c:otherwise>
                            </c:choose>
                        </div>
                        <div class="card-body">
                            <div class="row mb-3">
                                <div class="col-md-6">
                                    <p class="mb-1">
                                        <strong>Fecha de Constitución:</strong>
                                        <fmt:formatDate value="${comision.fechaConstitucion}" pattern="dd/MM/yyyy"/>
                                    </p>
                                </div>
                                <div class="col-md-6">
                                    <p class="mb-1">
                                        <strong>Fecha de Fin:</strong>
                                        <c:choose>
                                            <c:when test="${not empty comision.fechaFin}">
                                                <fmt:formatDate value="${comision.fechaFin}" pattern="dd/MM/yyyy"/>
                                            </c:when>
                                            <c:otherwise>-</c:otherwise>
                                        </c:choose>
                                    </p>
                                </div>
                            </div>

                            <h5>Miembros:</h5>
                            <c:if test="${empty comision.miembros}">
                                <div class="alert alert-info mb-0">No hay miembros registrados.</div>
                            </c:if>
                            <c:if test="${not empty comision.miembros}">
                                <table class="table table-sm table-striped">
                                    <thead>
                                        <tr>
                                            <th>Nombre y Apellidos</th>
                                            <th>DNI/NIF</th>
                                            <th>Email</th>
                                            <th>Cargo</th>
                                            <th>Fecha Incorporación</th>
                                            <th>Fecha de Baja</th>
                                        </tr>
                                    </thead>
                                    <tbody>
                                        <c:forEach var="cm" items="${comision.miembros}">
                                            <tr>
                                                <td><c:out value="${cm.miembro.nombreApellidos}"/></td>
                                                <td><c:out value="${cm.miembro.dniNif}"/></td>
                                                <td><c:out value="${cm.miembro.email}"/></td>
                                                <td>${cm.cargo}</td>
                                                <td><fmt:formatDate value="${cm.fechaIncorporacion}" pattern="dd/MM/yyyy"/></td>
                                                <td>
                                                    <c:choose>
                                                        <c:when test="${not empty cm.fechaBaja}">
                                                            <fmt:formatDate value="${cm.fechaBaja}" pattern="dd/MM/yyyy"/>
                                                        </c:when>
                                                        <c:otherwise>-</c:otherwise>
                                                    </c:choose>
                                                </td>
                                            </tr>
                                        </c:forEach>
                                    </tbody>
                                </table>
                            </c:if>
                        </div>
                    </div>
                </c:forEach>
            </c:otherwise>
        </c:choose>
    </c:if>
    <a href="${pageContext.request.contextPath}/comisiones" class="btn btn-secondary mt-3">
        <i class="bi bi-arrow-left"></i> Volver a Comisiones
    </a>
</div>

<script>
document.addEventListener('DOMContentLoaded', function() {
    const checkActivas = document.getElementById('mostrarActivas');
    const checkFinalizadas = document.getElementById('mostrarFinalizadas');
    const cards = document.querySelectorAll('.card-comision');

    function actualizarContadores() {
        let totalActivas = 0;
        let totalFinalizadas = 0;
        let totalVisibles = 0;

        cards.forEach(function(card) {
            const estado = card.getAttribute('data-estado');
            const visible = !card.classList.contains('oculto');

            if (estado === 'activa') {
                totalActivas++;
                if (visible) totalVisibles++;
            } else {
                totalFinalizadas++;
                if (visible) totalVisibles++;
            }
        });

        const elemTotalActivas = document.getElementById('totalActivas');
        const elemTotalFinalizadas = document.getElementById('totalFinalizadas');
        const elemTotalVisibles = document.getElementById('totalVisibles');

        if (elemTotalActivas) elemTotalActivas.textContent = totalActivas;
        if (elemTotalFinalizadas) elemTotalFinalizadas.textContent = totalFinalizadas;
        if (elemTotalVisibles) elemTotalVisibles.textContent = totalVisibles;
    }

    function filtrarComisiones() {
        if (!checkActivas || !checkFinalizadas) return;

        const mostrarActivas = checkActivas.checked;
        const mostrarFinalizadas = checkFinalizadas.checked;

        cards.forEach(function(card) {
            const estado = card.getAttribute('data-estado');
            let mostrar = false;

            if (estado === 'activa' && mostrarActivas) {
                mostrar = true;
            } else if (estado === 'finalizada' && mostrarFinalizadas) {
                mostrar = true;
            }

            if (mostrar) {
                card.classList.remove('oculto');
            } else {
                card.classList.add('oculto');
            }
        });

        actualizarContadores();
    }

    if (checkActivas) {
        checkActivas.addEventListener('change', filtrarComisiones);
    }

    if (checkFinalizadas) {
        checkFinalizadas.addEventListener('change', filtrarComisiones);
    }

    if (cards.length > 0) {
        actualizarContadores();
    }
});
</script>
</body>
</html>
