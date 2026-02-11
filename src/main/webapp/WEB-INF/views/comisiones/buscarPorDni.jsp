<%-- 
    Document   : buscarPorDni
    Created on : 22 sept. 2025, 11:04:09
    Author     : hlisa-admin
--%>

<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
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
                                        <th>Comisión/Grupo</th>
                                        <th>Área</th>
                                        <th>Tipo</th>
                                        <th>Cargo</th>
                                        <th>Fecha Constitución</th>
                                        <th>Fecha Fin</th>
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
                                                <strong>${cm.comision.nombre}</strong>
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