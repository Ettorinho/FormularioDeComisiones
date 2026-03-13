<%-- 
    Document   : buscarPorDni
    Created on : 22 sept. 2025, 11:04:09
    Author     : hlisa-admin
--%>

<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<jsp:useBean id="now" class="java.util.Date" />
<! DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Buscar Comisiones por DNI</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
    <style>
        .badge-activa {
            background-color:  #28a745 !important;
        }
        .badge-finalizada {
            background-color:  #6c757d !important;
        }
        .row-inactive {
            opacity: 0.6;
            background-color: #f8f9fa;
        }
        .oculto {
            display: none ! important;
        }
    </style>
</head>
<body>
<div class="container mt-4">
    <h2>Buscar Miembro</h2>
    
    <form action="${pageContext.request.contextPath}/comisiones/buscarPorDni" method="post" class="mb-4">
        <div class="row g-3 align-items-end">
            <div class="col-auto">
                <label for="dni" class="form-label">DNI del miembro</label>
                <input type="text" name="dni" id="dni" class="form-control" placeholder="Introduce DNI" required value="${dniBuscado != null ? dniBuscado : ''}" />
            </div>
            <div class="col-auto">
                <button type="submit" class="btn btn-primary">🔍 Buscar</button>
            </div>
        </div>
        
        <c:if test="${not empty dniBuscado && not empty comisiones}">
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
    
    <c:if test="${not empty dniBuscado}">
        <c:choose>
            <c:when test="${empty miembro}">
                <div class="alert alert-warning">No se encontró ningún miembro con ese DNI.</div>
            </c:when>
            <c:otherwise>
                <div class="card mb-4">
                    <div class="card-header bg-primary text-white">
                        <h5 class="mb-0">
                            👤 ${miembro.nombreApellidos} 
                            <span class="badge bg-light text-dark">${miembro. dniNif}</span>
                        </h5>
                        <c:if test="${not empty miembro.email}">
                            <small>📧 ${miembro.email}</small>
                        </c:if>
                    </div>
                </div>
                
                <c:if test="${empty comisiones}">
                    <div class="alert alert-info">Este miembro no pertenece a ninguna comisión.</div>
                </c:if>
                
                <c:if test="${not empty comisiones}">
                    <div class="card">
                        <div class="card-header">
                            <h5 class="mb-0">Comisiones y Grupos</h5>
                        </div>
                        <div class="card-body p-0">
                            <table class="table table-striped table-hover mb-0" id="tablaComisiones">
                                <thead class="table-light">
                                    <tr>
                                        <th>Comision/Grupo</th>
                                        <th>Area</th>
                                        <th>Tipo</th>
                                        <th>Cargo actual</th>
                                        <th>Incorporacion</th>
                                        <th>Baja</th>
                                        <th>Constitucion</th>
                                        <th>Fin</th>
                                        <th>Estado</th>
                                        <th>Estado Miembro</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    <c:forEach var="cm" items="${comisiones}">
                                        <c:set var="esActiva" value="${empty cm.comision. fechaFin || cm.comision.fechaFin > now}" />
                                        <c:set var="miembroActivo" value="${empty cm.fechaBaja}" />
                                        
                                        <tr class="fila-comision" data-estado="${esActiva ? 'activa' : 'finalizada'}">
                                            <td>
                                                <a href="#" class="text-decoration-none text-dark fw-bold toggle-historial"
                                                   data-target="historial-${cm.comision.id}"
                                                   onclick="toggleHistorial('historial-${cm.comision.id}'); return false;">
                                                    ${cm.comision.nombre}
                                                    <small class="text-muted ms-1">[+]</small>
                                                </a>
                                            </td>
                                            <td>
                                                <c:choose>
                                                    <c:when test="${cm.comision.area == 'ATENCION_ESPECIALIZADA'}">
                                                        <span class="badge bg-info">Atención Especializada</span>
                                                    </c:when>
                                                    <c:when test="${cm.comision.area == 'ATENCION_PRIMARIA'}">
                                                        <span class="badge bg-success">Atención Primaria</span>
                                                    </c:when>
                                                    <c:when test="${cm.comision.area == 'MIXTA'}">
                                                        <span class="badge bg-warning text-dark">Mixta</span>
                                                    </c:when>
                                                    <c:otherwise>
                                                        ${cm.comision.area}
                                                    </c:otherwise>
                                                </c:choose>
                                            </td>
                                            <td>
                                                <c:choose>
                                                    <c:when test="${cm.comision.tipo == 'COMISION'}">
                                                        Comisión
                                                    </c:when>
                                                    <c:when test="${cm.comision.tipo == 'GRUPO_TRABAJO'}">
                                                        Grupo de Trabajo
                                                    </c:when>
                                                    <c:when test="${cm.comision.tipo == 'GRUPO_MEJORA'}">
                                                        Grupo de Mejora
                                                    </c:when>
                                                    <c:otherwise>
                                                        ${cm.comision.tipo}
                                                    </c:otherwise>
                                                </c:choose>
                                            </td>
                                            <td>
                                                <c:choose>
                                                    <c:when test="${cm.cargo == 'PRESIDENTE'}">
                                                        <span class="badge bg-danger">Presidente</span>
                                                    </c:when>
                                                    <c:when test="${cm.cargo == 'SECRETARIO'}">
                                                        <span class="badge bg-warning text-dark">Secretario</span>
                                                    </c:when>
                                                    <c:when test="${cm. cargo == 'RESPONSABLE'}">
                                                        <span class="badge bg-primary">Responsable</span>
                                                    </c:when>
                                                    <c:when test="${cm.cargo == 'REFERENTE'}">
                                                        <span class="badge bg-secondary">Referente</span>
                                                    </c:when>
                                                    <c:when test="${cm.cargo == 'INVESTIGADOR_PRINCIPAL'}">
                                                        <span class="badge bg-dark">Investigador Principal</span>
                                                    </c:when>
                                                    <c:when test="${cm.cargo == 'INVESTIGADOR_COLABORADOR'}">
                                                        <span class="badge bg-dark">Investigador Colaborador</span>
                                                    </c:when>
                                                    <c:when test="${cm.cargo == 'PARTICIPANTE'}">
                                                        <span class="badge bg-light text-dark">Participante</span>
                                                    </c:when>
                                                    <c:otherwise>
                                                        ${cm.cargo}
                                                    </c:otherwise>
                                                </c:choose>
                                            </td>
                                            <td>
                                                <fmt:formatDate value="${cm.fechaIncorporacion}" pattern="dd/MM/yyyy" />
                                            </td>
                                            <td>
                                                <c:choose>
                                                    <c:when test="${not empty cm.fechaBaja}">
                                                        <fmt:formatDate value="${cm.fechaBaja}" pattern="dd/MM/yyyy" />
                                                    </c:when>
                                                    <c:otherwise>-</c:otherwise>
                                                </c:choose>
                                            </td>
                                            <td>
                                                <fmt:formatDate value="${cm.comision.fechaConstitucion}" pattern="dd/MM/yyyy" />
                                            </td>
                                            <td>
                                                <c:choose>
                                                    <c:when test="${not empty cm.comision.fechaFin}">
                                                        <fmt:formatDate value="${cm. comision.fechaFin}" pattern="dd/MM/yyyy" />
                                                    </c:when>
                                                    <c:otherwise>
                                                        -
                                                    </c:otherwise>
                                                </c:choose>
                                            </td>
                                            <td>
                                                <c:choose>
                                                    <c:when test="${esActiva}">
                                                        <span class="badge badge-activa">Activa</span>
                                                    </c:when>
                                                    <c:otherwise>
                                                        <span class="badge badge-finalizada">Finalizada</span>
                                                    </c:otherwise>
                                                </c:choose>
                                            </td>
                                            <td>
                                                <c:choose>
                                                    <c:when test="${miembroActivo}">
                                                        <span class="badge bg-success">Activo</span>
                                                    </c:when>
                                                    <c:otherwise>
                                                        <span class="badge bg-secondary">
                                                            Baja:  <fmt:formatDate value="${cm.fechaBaja}" pattern="dd/MM/yyyy" />
                                                        </span>
                                                    </c:otherwise>
                                                </c:choose>
                                            </td>
                                        </tr>
                                        <%-- Fila de detalle expandible con historial de cargos --%>
                                        <tr class="fila-detalle-historial oculto" id="historial-${cm.comision.id}">
                                            <td colspan="10" class="bg-light p-3">
                                                <div class="row">
                                                    <div class="col-md-4">
                                                        <strong>Fechas de participacion:</strong>
                                                        <ul class="list-unstyled mt-1 mb-0">
                                                            <li><small>Incorporacion: <fmt:formatDate value="${cm.fechaIncorporacion}" pattern="dd/MM/yyyy" /></small></li>
                                                            <li>
                                                                <small>Baja: 
                                                                    <c:choose>
                                                                        <c:when test="${not empty cm.fechaBaja}">
                                                                            <fmt:formatDate value="${cm.fechaBaja}" pattern="dd/MM/yyyy" />
                                                                        </c:when>
                                                                        <c:otherwise>Activo</c:otherwise>
                                                                    </c:choose>
                                                                </small>
                                                            </li>
                                                        </ul>
                                                    </div>
                                                    <div class="col-md-8">
                                                        <strong>Historial de cargos:</strong>
                                                        <c:set var="historialComision" value="${historialPorComision[cm.comision.id.toString()]}" />
                                                        <c:choose>
                                                            <c:when test="${empty historialComision}">
                                                                <p class="text-muted mt-1 mb-0"><small>Sin cambios de cargo registrados.</small></p>
                                                            </c:when>
                                                            <c:otherwise>
                                                                <table class="table table-sm table-bordered mt-1 mb-0">
                                                                    <thead class="table-secondary">
                                                                        <tr>
                                                                            <th>Cargo</th>
                                                                            <th>Desde</th>
                                                                            <th>Hasta</th>
                                                                        </tr>
                                                                    </thead>
                                                                    <tbody>
                                                                        <%-- Fila para el cargo actual (cargoNuevo del elemento más reciente, índice 0) --%>
                                                                        <c:set var="primerCambio" value="${historialComision[0]}" />
                                                                        <tr>
                                                                            <td><small><strong>${primerCambio.cargoNuevo}</strong></small></td>
                                                                            <td><small><fmt:formatDate value="${primerCambio.fechaCambio}" pattern="dd/MM/yyyy" /></small></td>
                                                                            <td>
                                                                                <small>
                                                                                    <c:choose>
                                                                                        <c:when test="${not empty cm.fechaBaja}">
                                                                                            <fmt:formatDate value="${cm.fechaBaja}" pattern="dd/MM/yyyy" />
                                                                                        </c:when>
                                                                                        <c:otherwise>
                                                                                            <span class="text-success fw-bold">Activo</span>
                                                                                        </c:otherwise>
                                                                                    </c:choose>
                                                                                </small>
                                                                            </td>
                                                                        </tr>
                                                                        <%-- Filas para los cargos intermedios (índice 1 en adelante) --%>
                                                                        <c:forEach var="cambio" items="${historialComision}" varStatus="status">
                                                                            <c:if test="${status.index > 0}">
                                                                                <c:set var="cambioAnterior" value="${historialComision[status.index - 1]}" />
                                                                                <tr>
                                                                                    <td><small>${cambio.cargoNuevo}</small></td>
                                                                                    <td><small><fmt:formatDate value="${cambio.fechaCambio}" pattern="dd/MM/yyyy" /></small></td>
                                                                                    <td><small><fmt:formatDate value="${cambioAnterior.fechaCambio}" pattern="dd/MM/yyyy" /></small></td>
                                                                                </tr>
                                                                            </c:if>
                                                                        </c:forEach>
                                                                        <%-- Fila adicional para el primer cargo histórico (cargoAnterior del último elemento) --%>
                                                                        <c:set var="ultimoCambio" value="${historialComision[fn:length(historialComision) - 1]}" />
                                                                        <c:if test="${not empty ultimoCambio.cargoAnterior}">
                                                                            <tr class="table-light">
                                                                                <td><small>${ultimoCambio.cargoAnterior}</small></td>
                                                                                <td>
                                                                                    <small>
                                                                                        <c:choose>
                                                                                            <c:when test="${not empty cm.fechaIncorporacion}">
                                                                                                <fmt:formatDate value="${cm.fechaIncorporacion}" pattern="dd/MM/yyyy" />
                                                                                            </c:when>
                                                                                            <c:otherwise>-</c:otherwise>
                                                                                        </c:choose>
                                                                                    </small>
                                                                                </td>
                                                                                <td><small><fmt:formatDate value="${ultimoCambio.fechaCambio}" pattern="dd/MM/yyyy" /></small></td>
                                                                            </tr>
                                                                        </c:if>
                                                                    </tbody>
                                                                </table>
                                                            </c:otherwise>
                                                        </c:choose>
                                                    </div>
                                                </div>
                                            </td>
                                        </tr>
                                    </c:forEach>
                                </tbody>
                            </table>
                        </div>
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
                </c:if>
            </c:otherwise>
        </c:choose>
    </c:if>
    
    <a href="${pageContext.request.contextPath}/" class="btn btn-secondary mt-3">🏠 Volver al Inicio</a>
</div>

<script>
function toggleHistorial(id) {
    const fila = document.getElementById(id);
    if (fila) {
        fila.classList.toggle('oculto');
        // Cambiar el icono +/-
        const link = document.querySelector('[data-target="' + id + '"]');
        if (link) {
            const icono = link.querySelector('small');
            if (icono) {
                icono.textContent = fila.classList.contains('oculto') ? '[+]' : '[-]';
            }
        }
    }
}

document.addEventListener('DOMContentLoaded', function() {
    const checkActivas = document.getElementById('mostrarActivas');
    const checkFinalizadas = document.getElementById('mostrarFinalizadas');
    const filas = document.querySelectorAll('.fila-comision');
    
    console.log('Checkboxes encontrados:', {
        activas: checkActivas !== null,
        finalizadas:  checkFinalizadas !== null,
        totalFilas: filas.length
    });
    
    // Función para actualizar contadores
    function actualizarContadores() {
        let totalActivas = 0;
        let totalFinalizadas = 0;
        let totalVisibles = 0;
        
        filas.forEach(fila => {
            const estado = fila.getAttribute('data-estado');
            const visible = ! fila.classList.contains('oculto');
            
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
        
        console.log('Contadores actualizados:', {
            activas: totalActivas,
            finalizadas: totalFinalizadas,
            visibles: totalVisibles
        });
    }
    
    // Función para filtrar filas
    function filtrarComisiones() {
        if (!checkActivas || !checkFinalizadas) {
            console.log('Checkboxes no encontrados, saliendo...');
            return;
        }
        
        const mostrarActivas = checkActivas. checked;
        const mostrarFinalizadas = checkFinalizadas. checked;
        
        console.log('Filtrando:', {
            mostrarActivas:  mostrarActivas,
            mostrarFinalizadas: mostrarFinalizadas
        });
        
        let ocultadas = 0;
        let mostradas = 0;
        
        filas.forEach(fila => {
            const estado = fila.getAttribute('data-estado');
            let mostrar = false;
            
            if (estado === 'activa' && mostrarActivas) {
                mostrar = true;
            } else if (estado === 'finalizada' && mostrarFinalizadas) {
                mostrar = true;
            }
            
            if (mostrar) {
                fila.classList.remove('oculto');
                mostradas++;
            } else {
                fila.classList.add('oculto');
                ocultadas++;
            }
        });
        
        console.log('Resultado filtrado:', {
            mostradas: mostradas,
            ocultadas: ocultadas
        });
        
        actualizarContadores();
    }
    
    // Event listeners
    if (checkActivas) {
        checkActivas.addEventListener('change', function() {
            console.log('Checkbox Activas cambiado a:', this.checked);
            filtrarComisiones();
        });
    }
    
    if (checkFinalizadas) {
        checkFinalizadas.addEventListener('change', function() {
            console.log('Checkbox Finalizadas cambiado a:', this. checked);
            filtrarComisiones();
        });
    }
    
    // Inicializar contadores al cargar la página
    if (filas.length > 0) {
        console.log('Inicializando contadores...');
        actualizarContadores();
    }
});
</script>

<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>