

<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<c:set var="pageTitle" value="Acta de Reunión - Gobierno de Aragón" />
<c:set var="headerClass" value="no-print" />
<%@ include file="/WEB-INF/views/common/header.jspf" %>

    <!-- Contenido Principal -->
    <div class="container mt-4 mb-5">

        <c:if test="${not empty error}">
            <div class="alert alert-danger alert-dismissible fade show" role="alert">
                <i class="bi bi-exclamation-triangle"></i> <strong>Error:</strong> <c:out value="${error}"/>
                <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
            </div>
        </c:if>

        <c:if test="${not empty acta}">
            <div class="card shadow-sm">
                <div class="card-header bg-primary text-white d-flex justify-content-between align-items-center">
                    <div>
                        <h3 class="mb-0">
                            <i class="bi bi-file-earmark-text"></i> <c:out value="${acta.titulo}"/>
                        </h3>
                    </div>
                    <div class="no-print">
                        <button onclick="window.print()" class="btn btn-light btn-sm me-2">
                            <i class="bi bi-printer"></i> Imprimir
                        </button>
                    </div>
                </div>
                
                <div class="card-body">
                    
                    <div class="border rounded mb-4">
                        <div class="bg-light border-bottom px-3 py-2 fw-bold text-center">
                            ACTA DE REUNIÓN · <c:out value="${acta.comision.nombre}"/>
                        </div>
                        <div class="p-3">
                            <div class="row g-3">
                                <div class="col-md-4">
                                    <strong>Fecha y hora:</strong><br>
                                    <span><c:out value="${acta.fechaReunionFormateada}"/></span>
                                    <c:if test="${not empty acta.horaInicio}">
                                        <span> <c:out value="${acta.horaInicio}"/>h</span>
                                    </c:if>
                                    <c:if test="${not empty acta.horaFin}">
                                        <span> - <c:out value="${acta.horaFin}"/>h</span>
                                    </c:if>
                                </div>
                                <div class="col-md-4">
                                    <strong>Duración:</strong><br>
                                    <c:out value="${empty acta.duracion ? '—' : acta.duracion}"/>
                                </div>
                                <div class="col-md-4">
                                    <strong>Tipo de reunión:</strong><br>
                                    <c:choose>
                                        <c:when test="${acta.tipoReunion == 'CALIDAD'}">Revisión del Sist. Calidad</c:when>
                                        <c:when test="${acta.tipoReunion == 'INTERNA'}">Reunión Interna</c:when>
                                        <c:when test="${acta.tipoReunion == 'OTROS'}">
                                            Otros<c:if test="${not empty acta.tipoReunionOtrosDetalle}">: <c:out value="${acta.tipoReunionOtrosDetalle}"/></c:if>
                                        </c:when>
                                        <c:otherwise>—</c:otherwise>
                                    </c:choose>
                                </div>
                            </div>
                            <div class="mt-3 text-muted">
                                <small><i class="bi bi-clock-history"></i> Acta creada el: <c:out value="${acta.fechaCreacionFormateada}"/></small>
                            </div>
                        </div>
                    </div>

                    <!--
                        Estadísticas de Asistencia.
                        Se distinguen 3 estados excluyentes entre sí (asistencia.estadoAsistencia):
                          - ASISTIO    -> contadorAsistieron
                          - EXCUSA     -> contadorExcusaAsistencia (NO cuenta como "No Asistieron")
                          - NO_ASISTIO -> contadorNoAsistieron (sin justificar)
                        Antes "Excusa asistencia" se contaba también dentro de "No Asistieron"
                        (isAsistio() == false), inflando ese contador y esa lista con miembros
                        que sí habían justificado su ausencia.
                    -->
                    <c:set var="contadorAsistieron" value="0"/>
                    <c:set var="contadorNoAsistieron" value="0"/>
                    <c:set var="contadorConJustificacion" value="0"/>
                    <c:forEach var="asistencia" items="${asistencias}">
                        <c:choose>
                            <c:when test="${asistencia.asistio}">
                                <c:set var="contadorAsistieron" value="${contadorAsistieron + 1}"/>
                            </c:when>
                            <c:when test="${asistencia.excusa}">
                                <c:set var="contadorConJustificacion" value="${contadorConJustificacion + 1}"/>
                            </c:when>
                            <c:otherwise>
                                <c:set var="contadorNoAsistieron" value="${contadorNoAsistieron + 1}"/>
                            </c:otherwise>
                        </c:choose>
                    </c:forEach>

                    <!-- Cálculo del porcentaje de asistencia -->
                    <c:set var="totalMiembrosActa" value="${contadorAsistieron + contadorNoAsistieron + contadorConJustificacion}"/>
                    <c:choose>
                        <c:when test="${totalMiembrosActa > 0}">
                            <c:set var="porcentajeAsistencia" value="${(contadorAsistieron * 100.0) / totalMiembrosActa}"/>
                        </c:when>
                        <c:otherwise>
                            <c:set var="porcentajeAsistencia" value="0"/>
                        </c:otherwise>
                    </c:choose>

                    <div class="row row-cols-1 g-3 mb-3 no-print">
                        <div class="col">
                            <div class="stats-box">
                                <h3>${totalMiembrosActa}</h3>
                                <p>Total Miembros</p>
                            </div>
                        </div>
                        <div class="col">
                            <div class="stats-box stats-box-green">
                                <h3>${contadorAsistieron}</h3>
                                <p>Asistieron</p>
                            </div>
                        </div>
                        <div class="col">
                            <div class="stats-box stats-box-red">
                                <h3>${contadorNoAsistieron}</h3>
                                <p>No Asistieron</p>
                            </div>
                        </div>
                        <div class="col">
                            <div class="stats-box stats-box-yellow">
                                <h3>${contadorConJustificacion}</h3>
                                <p>Excusa Asistencia</p>
                            </div>
                        </div>
                    </div>

                    <!-- Porcentaje de Asistencia -->
                    <div class="row mb-4 no-print">
                        <div class="col-12">
                            <div class="stats-box stats-box-blue">
                                <h3>
                                    <fmt:formatNumber value="${porcentajeAsistencia}" maxFractionDigits="1" minFractionDigits="0"/>%
                                </h3>
                                <p class="mb-2">Porcentaje de Asistencia</p>
                                <div class="progress" style="height: 10px;">
                                    <div class="progress-bar bg-success" role="progressbar"
                                         style="width: ${porcentajeAsistencia}%;"
                                         aria-valuenow="${porcentajeAsistencia}" aria-valuemin="0" aria-valuemax="100">
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>

                    <!-- Lista de Asistencia -->
                    <div class="mb-4">
                        <h5 class="border-bottom pb-2 mb-3">
                            <i class="bi bi-person-check"></i> Registro de Asistencia
                        </h5>
                        <div class="row">
                            <div class="col-md-4">
                                <h6 class="text-success">
                                    <i class="bi bi-check-circle"></i> Asistieron (${contadorAsistieron})
                                </h6>
                                <ul class="asistencia-lista">
                                    <c:set var="hayAsistentes" value="false"/>
                                    <c:forEach var="asistencia" items="${asistencias}">
                                        <c:if test="${asistencia.asistio}">
                                            <c:set var="hayAsistentes" value="true"/>
                                            <li class="asistio">
                                                <i class="bi bi-person-fill text-success"></i>
                                                <strong><c:out value="${asistencia.miembro.nombreApellidos}"/></strong>
                                                <br>
                                                <small class="text-muted ms-3">
                                                    <i class="bi bi-card-text"></i> <c:out value="${asistencia.miembro.dniNif}"/>
                                                </small>
                                            </li>
                                        </c:if>
                                    </c:forEach>
                                    <c:if test="${!hayAsistentes}">
                                        <li class="text-muted fst-italic">
                                            <i class="bi bi-info-circle"></i> Ningún miembro asistió
                                        </li>
                                    </c:if>
                                </ul>
                            </div>
                            <div class="col-md-4">
                                <h6 class="text-warning">
                                    <i class="bi bi-person-x"></i> Excusa Asistencia (${contadorConJustificacion})
                                </h6>
                                <ul class="asistencia-lista">
                                    <c:set var="hayExcusas" value="false"/>
                                    <c:forEach var="asistencia" items="${asistencias}">
                                        <c:if test="${asistencia.excusa}">
                                            <c:set var="hayExcusas" value="true"/>
                                            <li class="no-asistio">
                                                <i class="bi bi-person text-warning"></i>
                                                <strong><c:out value="${asistencia.miembro.nombreApellidos}"/></strong>
                                                <br>
                                                <small class="text-muted ms-3">
                                                    <i class="bi bi-card-text"></i> <c:out value="${asistencia.miembro.dniNif}"/>
                                                </small>

                                                <!-- MOSTRAR JUSTIFICACIÓN -->
                                                <c:if test="${not empty asistencia.justificacion}">
                                                    <div class="justificacion-box">
                                                        <div class="justificacion-label">
                                                            <i class="bi bi-file-earmark-text"></i> Justificación:
                                                        </div>
                                                        <div class="justificacion-text">
                                                            "<c:out value="${asistencia.justificacion}"/>"
                                                        </div>
                                                    </div>
                                                </c:if>

                                            </li>
                                        </c:if>
                                    </c:forEach>
                                    <c:if test="${!hayExcusas}">
                                        <li class="text-muted fst-italic">
                                            <i class="bi bi-info-circle"></i> Ningún miembro justificó su ausencia
                                        </li>
                                    </c:if>
                                </ul>
                            </div>
                            <div class="col-md-4">
                                <h6 class="text-danger">
                                    <i class="bi bi-x-circle"></i> No Asistieron (${contadorNoAsistieron})
                                </h6>
                                <ul class="asistencia-lista">
                                    <c:set var="hayAusentes" value="false"/>
                                    <c:forEach var="asistencia" items="${asistencias}">
                                        <c:if test="${asistencia.noAsistio}">
                                            <c:set var="hayAusentes" value="true"/>
                                            <li class="no-asistio">
                                                <i class="bi bi-person text-danger"></i>
                                                <strong><c:out value="${asistencia.miembro.nombreApellidos}"/></strong>
                                                <br>
                                                <small class="text-muted ms-3">
                                                    <i class="bi bi-card-text"></i> <c:out value="${asistencia.miembro.dniNif}"/>
                                                </small>
                                            </li>
                                        </c:if>
                                    </c:forEach>
                                    <c:if test="${!hayAusentes}">
                                        <li class="text-muted fst-italic">
                                            <i class="bi bi-info-circle"></i> Todos los miembros asistieron o justificaron su ausencia
                                        </li>
                                    </c:if>
                                </ul>
                            </div>
                        </div>
                    </div>

                    <div class="mb-4">
                        <h5 class="border-bottom pb-2 mb-3">
                            <i class="bi bi-list-ul"></i> Orden del día
                        </h5>
                        <c:choose>
                            <c:when test="${not empty acta.ordenDia}">
                                <div class="observaciones-box">
                                    <p class="mb-0" style="white-space: pre-wrap; line-height: 1.6;"><c:out value="${acta.ordenDia}"/></p>
                                </div>
                            </c:when>
                            <c:otherwise>
                                <p class="text-muted fst-italic">
                                    <i class="bi bi-info-circle"></i> No se registró orden del día
                                </p>
                            </c:otherwise>
                        </c:choose>
                    </div>

                    <div class="mb-4">
                        <h5 class="border-bottom pb-2 mb-3">
                            <i class="bi bi-person-x"></i> Excusa asistencia
                        </h5>
                        <c:choose>
                            <c:when test="${not empty acta.excusaAsistencia}">
                                <div class="observaciones-box">
                                    <p class="mb-0" style="white-space: pre-wrap; line-height: 1.6;"><c:out value="${acta.excusaAsistencia}"/></p>
                                </div>
                            </c:when>
                            <c:otherwise>
                                <p class="text-muted fst-italic">
                                    <i class="bi bi-info-circle"></i> No se registró excusa de asistencia general
                                </p>
                            </c:otherwise>
                        </c:choose>
                    </div>

                    <!-- Observaciones -->
                    <div class="mb-4">
                        <h5 class="border-bottom pb-2 mb-3">
                            <i class="bi bi-file-text"></i> Resumen de la reunión
                        </h5>
                        <c:choose>
                            <c:when test="${not empty acta.observaciones}">
                                <div class="observaciones-box">
                                    <p class="mb-0" style="white-space: pre-wrap; line-height: 1.6;"><c:out value="${acta.observaciones}"/></p>
                                </div>
                            </c:when>
                            <c:otherwise>
                                <p class="text-muted fst-italic">
                                    <i class="bi bi-info-circle"></i> No se registraron observaciones
                                </p>
                            </c:otherwise>
                        </c:choose>
                    </div>

                    <!-- Opciones de descarga generada -->
                    <div class="row mb-3">
                        <div class="col-12">
                            <div class="card border-info">
                                <div class="card-header bg-info text-white">
                                    <i class="bi bi-download"></i> Generar y Descargar Acta
                                </div>
                                <div class="card-body">
                                    <p class="card-text">Genera el acta automáticamente en el formato deseado:</p>
                                    <div class="btn-group" role="group">
                                        <a href="${pageContext.request.contextPath}/actas/generate-pdf?id=${acta.id}" 
                                           class="btn btn-danger" title="Generar PDF">
                                            <i class="bi bi-file-pdf"></i> Generar PDF
                                        </a>
                                        <a href="${pageContext.request.contextPath}/actas/generate-word?id=${acta.id}" 
                                           class="btn btn-primary" title="Generar Word">
                                            <i class="bi bi-file-word"></i> Generar Word
                                        </a>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>

                    <!-- NUEVO: Documento PDF adjunto -->
                    <c:if test="${acta.tienePdf()}">
                        <div class="mb-4">
                            <h5 class="border-bottom pb-2 mb-3">
                                <i class="bi bi-file-earmark-pdf text-danger"></i> Documento Adjunto
                            </h5>
                            <div class="card pdf-card">
                                <div class="card-body">
                                    <div class="row align-items-center">
                                        <div class="col-md-8">
                                            <div class="d-flex align-items-center">
                                                <i class="bi bi-file-earmark-pdf-fill fs-1 text-danger me-3"></i>
                                                <div>
                                                    <h6 class="mb-1">
                                                        <strong><i class="bi bi-paperclip"></i> <c:out value="${acta.pdfNombre}"/></strong>
                                                    </h6>
                                                    <small class="text-muted">
                                                        <i class="bi bi-filetype-pdf"></i> Documento PDF adjunto a esta acta
                                                    </small>
                                                </div>
                                            </div>
                                        </div>
                                        <div class="col-md-4 text-end">
                                            <a href="${pageContext.request.contextPath}/actas/view-pdf?id=${acta.id}" 
                                               class="btn btn-outline-primary btn-sm me-2" target="_blank" 
                                               title="Abrir PDF en nueva pestaña">
                                                <i class="bi bi-eye"></i> Ver
                                            </a>
                                            <a href="${pageContext.request.contextPath}/actas/download-pdf?id=${acta.id}" 
                                               class="btn btn-danger btn-sm" title="Descargar PDF">
                                                <i class="bi bi-download"></i> Descargar
                                            </a>
                                        </div>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </c:if>

                </div>
                
                <div class="card-footer bg-light no-print">
                    <div class="d-flex justify-content-between align-items-center">
                        <div>
                            <a href="${pageContext.request.contextPath}/" class="btn btn-secondary">
                                <i class="bi bi-house"></i> Inicio
                            </a>
                        </div>
                        <button onclick="window.print()" class="btn btn-outline-primary">
                            <i class="bi bi-printer"></i> Imprimir
                        </button>
                    </div>
                </div>
            </div>
        </c:if>
        
    </div>

<%@ include file="/WEB-INF/views/common/footer.jspf" %>
