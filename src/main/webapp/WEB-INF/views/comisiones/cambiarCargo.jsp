<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Cambiar Cargo - ${comisionMiembro.miembro.nombreApellidos}</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.min.css">
    <style>
        .badge-cargo {
            font-size: 0.9rem;
            padding: 0.4rem 0.8rem;
        }
        .cargo-PRESIDENTE { background-color: #0d6efd; }
        .cargo-SECRETARIO { background-color: #6610f2; }
        .cargo-REFERENTE { background-color: #0dcaf0; }
        .cargo-RESPONSABLE { background-color: #198754; }
        .cargo-PARTICIPANTE { background-color: #6c757d; }
        .cargo-INVESTIGADOR_PRINCIPAL { background-color: #fd7e14; }
        .cargo-INVESTIGADOR_COLABORADOR { background-color: #ffc107; }
    </style>
</head>
<body>
<div class="container mt-4">
    <!-- Breadcrumb -->
    <nav aria-label="breadcrumb">
        <ol class="breadcrumb">
            <li class="breadcrumb-item"><a href="${pageContext.request.contextPath}/comisiones/">Comisiones</a></li>
            <li class="breadcrumb-item"><a href="${pageContext.request.contextPath}/comisiones/view/${comisionMiembro.comision.id}">${comisionMiembro.comision.nombre}</a></li>
            <li class="breadcrumb-item active">Cambiar Cargo</li>
        </ol>
    </nav>

    <!-- Mensajes de éxito/error -->
    <c:if test="${not empty success}">
        <div class="alert alert-success alert-dismissible fade show" role="alert">
            <i class="bi bi-check-circle-fill"></i> <c:out value="${success}"/>
            <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
        </div>
    </c:if>
    
    <c:if test="${not empty error}">
        <div class="alert alert-danger alert-dismissible fade show" role="alert">
            <i class="bi bi-exclamation-triangle-fill"></i> <c:out value="${error}"/>
            <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
        </div>
    </c:if>

    <!-- Información del miembro -->
    <div class="card mb-4">
        <div class="card-header">
            <h4><i class="bi bi-person-badge"></i> Información del Miembro</h4>
        </div>
        <div class="card-body">
            <div class="row">
                <div class="col-md-6">
                    <p><strong>Nombre:</strong> ${comisionMiembro.miembro.nombreApellidos}</p>
                    <p><strong>DNI/NIF:</strong> ${comisionMiembro.miembro.dniNif}</p>
                    <p><strong>Email:</strong> ${comisionMiembro.miembro.email}</p>
                </div>
                <div class="col-md-6">
                    <p><strong>Comisión:</strong> ${comisionMiembro.comision.nombre}</p>
                    <p><strong>Cargo Actual:</strong> 
                        <span class="badge cargo-${comisionMiembro.cargo} badge-cargo">${comisionMiembro.cargo}</span>
                    </p>
                    <p><strong>Fecha de Incorporación:</strong> 
                        <fmt:formatDate value="${comisionMiembro.fechaIncorporacion}" pattern="dd/MM/yyyy" />
                    </p>
                    <c:if test="${not empty comisionMiembro.fechaBaja}">
                        <p><strong>Fecha de Baja:</strong> 
                            <span class="badge bg-secondary">
                                <fmt:formatDate value="${comisionMiembro.fechaBaja}" pattern="dd/MM/yyyy" />
                            </span>
                        </p>
                    </c:if>
                </div>
            </div>
        </div>
    </div>

    <!-- Formulario de cambio de cargo -->
    <c:if test="${empty comisionMiembro.fechaBaja}">
        <div class="card mb-4">
            <div class="card-header">
                <h4><i class="bi bi-arrow-left-right"></i> Cambiar Cargo</h4>
            </div>
            <div class="card-body">
                <form method="post" action="${pageContext.request.contextPath}/comisiones/cambiarCargo">
                    <input type="hidden" name="comisionId" value="${comisionMiembro.comision.id}">
                    <input type="hidden" name="miembroId" value="${comisionMiembro.miembro.id}">
                    
                    <div class="row mb-3">
                        <div class="col-md-6">
                            <label for="nuevoCargo" class="form-label">Nuevo Cargo <span class="text-danger">*</span></label>
                            <select class="form-select" id="nuevoCargo" name="nuevoCargo" required>
                                <option value="">-- Seleccione un cargo --</option>
                                <option value="REFERENTE" ${comisionMiembro.cargo == 'REFERENTE' ? 'disabled' : ''}>REFERENTE</option>
                                <option value="RESPONSABLE" ${comisionMiembro.cargo == 'RESPONSABLE' ? 'disabled' : ''}>RESPONSABLE</option>
                                <option value="PRESIDENTE" ${comisionMiembro.cargo == 'PRESIDENTE' ? 'disabled' : ''}>PRESIDENTE</option>
                                <option value="SECRETARIO" ${comisionMiembro.cargo == 'SECRETARIO' ? 'disabled' : ''}>SECRETARIO</option>
                                <option value="PARTICIPANTE" ${comisionMiembro.cargo == 'PARTICIPANTE' ? 'disabled' : ''}>PARTICIPANTE</option>
                                <option value="INVESTIGADOR_PRINCIPAL" ${comisionMiembro.cargo == 'INVESTIGADOR_PRINCIPAL' ? 'disabled' : ''}>INVESTIGADOR PRINCIPAL</option>
                                <option value="INVESTIGADOR_COLABORADOR" ${comisionMiembro.cargo == 'INVESTIGADOR_COLABORADOR' ? 'disabled' : ''}>INVESTIGADOR COLABORADOR</option>
                            </select>
                            <div class="form-text">El cargo actual (${comisionMiembro.cargo}) está deshabilitado.</div>
                        </div>
                        
                        <div class="col-md-6">
                            <label for="motivo" class="form-label">Motivo del cambio</label>
                            <textarea class="form-control" id="motivo" name="motivo" rows="3" 
                                      placeholder="Ej: Votado en asamblea, Finalización de mandato, Promoción..."></textarea>
                            <div class="form-text">Opcional, pero recomendado para trazabilidad.</div>
                        </div>
                    </div>
                    
                    <div class="d-flex justify-content-between">
                        <a href="${pageContext.request.contextPath}/comisiones/view/${comisionMiembro.comision.id}" 
                           class="btn btn-secondary">
                            <i class="bi bi-arrow-left"></i> Cancelar
                        </a>
                        <button type="submit" class="btn btn-primary" onclick="return confirm('¿Está seguro de cambiar el cargo de este miembro?');">
                            <i class="bi bi-save"></i> Cambiar Cargo
                        </button>
                    </div>
                </form>
            </div>
        </div>
    </c:if>

    <!-- Mensaje para miembros dados de baja -->
    <c:if test="${not empty comisionMiembro.fechaBaja}">
        <div class="alert alert-warning">
            <i class="bi bi-exclamation-triangle-fill"></i>
            <strong>Miembro dado de baja.</strong> No se puede cambiar el cargo de un miembro que ya está dado de baja.
        </div>
        <a href="${pageContext.request.contextPath}/comisiones/view/${comisionMiembro.comision.id}" 
           class="btn btn-secondary">
            <i class="bi bi-arrow-left"></i> Volver a la comisión
        </a>
    </c:if>

    <!-- Historial de cambios -->
    <div class="card">
        <div class="card-header">
            <h4><i class="bi bi-clock-history"></i> Historial de Cambios de Cargo</h4>
        </div>
        <div class="card-body">
            <c:if test="${empty historial}">
                <div class="alert alert-info">
                    <i class="bi bi-info-circle"></i> No hay cambios de cargo registrados para este miembro.
                </div>
            </c:if>
            
            <c:if test="${not empty historial}">
                <div class="table-responsive">
                    <table class="table table-striped table-hover">
                        <thead class="table-dark">
                            <tr>
                                <th>Fecha y Hora</th>
                                <th>Cargo Anterior</th>
                                <th></th>
                                <th>Cargo Nuevo</th>
                                <th>Motivo</th>
                                <th>Usuario</th>
                            </tr>
                        </thead>
                        <tbody>
                            <c:forEach var="cambio" items="${historial}">
                                <tr>
                                    <td>
                                        <fmt:formatDate value="${cambio.fechaCambio}" 
                                                       pattern="dd/MM/yyyy HH:mm:ss" />
                                    </td>
                                    <td>
                                        <c:choose>
                                            <c:when test="${empty cambio.cargoAnterior}">
                                                <span class="text-muted fst-italic">-</span>
                                            </c:when>
                                            <c:otherwise>
                                                <span class="badge cargo-${cambio.cargoAnterior} badge-cargo">
                                                    ${cambio.cargoAnterior}
                                                </span>
                                            </c:otherwise>
                                        </c:choose>
                                    </td>
                                    <td class="text-center">
                                        <i class="bi bi-arrow-right text-primary"></i>
                                    </td>
                                    <td>
                                        <span class="badge cargo-${cambio.cargoNuevo} badge-cargo">
                                            ${cambio.cargoNuevo}
                                        </span>
                                    </td>
                                    <td>
                                        <c:choose>
                                            <c:when test="${empty cambio.motivo}">
                                                <span class="text-muted fst-italic">Sin motivo especificado</span>
                                            </c:when>
                                            <c:otherwise>
                                                ${cambio.motivo}
                                            </c:otherwise>
                                        </c:choose>
                                    </td>
                                    <td>
                                        <c:choose>
                                            <c:when test="${empty cambio.usuarioModificacion}">
                                                <span class="text-muted fst-italic">SYSTEM</span>
                                            </c:when>
                                            <c:otherwise>
                                                ${cambio.usuarioModificacion}
                                            </c:otherwise>
                                        </c:choose>
                                    </td>
                                </tr>
                            </c:forEach>
                        </tbody>
                    </table>
                </div>
                <div class="text-muted mt-2">
                    <small><i class="bi bi-info-circle"></i> Total de cambios: ${historial.size()}</small>
                </div>
            </c:if>
        </div>
    </div>
</div>

<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>
