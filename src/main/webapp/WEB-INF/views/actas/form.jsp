<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="pageTitle" value="Nueva Acta de Reunión" />
<c:set var="headerClass" value="no-print" />
<%@ include file="/WEB-INF/views/common/header.jspf" %>

<div class="container mt-4 mb-5">
    <nav aria-label="breadcrumb" class="no-print">
        <ol class="breadcrumb">
            <li class="breadcrumb-item"><a href="${pageContext.request.contextPath}/">Inicio</a></li>
            <li class="breadcrumb-item active">Nueva Acta</li>
        </ol>
    </nav>

    <div class="card shadow-sm">
        <div class="card-header bg-primary text-white no-print">
            <h3 class="mb-0">
                <i class="bi bi-file-earmark-plus"></i> Crear Acta de Reunión
            </h3>
        </div>
        <div class="card-body">
            <c:if test="${not empty error}">
                <div class="alert alert-danger">
                    <i class="bi bi-exclamation-triangle"></i> <c:out value="${error}"/>
                </div>
            </c:if>
            <form id="formActa" method="post" action="${pageContext.request.contextPath}/actas/save" enctype="multipart/form-data">
                <input type="hidden" name="csrfToken" value="${csrfToken}" />

                <!-- Encabezado estilo documento oficial: logo | ACTA DE REUNIÓN + comisión | código/revisión -->
                <div class="border rounded mb-4 overflow-hidden acta-doc-header">
                    <div class="row g-0 align-items-stretch">
                        <div class="col-3 border-end d-flex flex-column align-items-center justify-content-center text-center p-2">
                            <img src="${pageContext.request.contextPath}/resources/images/logo_salud.png"
                                 alt="Servicio Aragonés de Salud" style="max-height:46px; width:auto;">
                            <div class="fw-bold small mt-1">SECTOR DE BARBASTRO</div>
                        </div>
                        <div class="col-6 border-end d-flex flex-column align-items-center justify-content-center text-center p-2">
                            <div class="fw-bold">ACTA DE REUNIÓN:</div>
                            <div class="fw-bold" id="comisionNombreHeader">
                                <c:choose>
                                    <c:when test="${not empty comisionPreseleccionada}">
                                        <c:forEach var="comision" items="${comisiones}">
                                            <c:if test="${comisionPreseleccionada == comision.id}">
                                                <c:out value="${comision.nombre}"/>
                                            </c:if>
                                        </c:forEach>
                                    </c:when>
                                    <c:otherwise>Seleccione una comisión</c:otherwise>
                                </c:choose>
                            </div>
                        </div>
                        <div class="col-3 d-flex flex-column align-items-center justify-content-center text-center p-2 small">
                            <div class="fw-bold">MC-2_SA(P)E</div>
                            <div class="fw-bold">Revisión: A</div>
                        </div>
                    </div>
                </div>

                <div class="border rounded mb-4">
                    <div class="bg-light border-bottom px-3 py-2 text-center fw-bold">
                        DATOS DE LA REUNIÓN
                    </div>
                    <div class="p-3">
                        <div class="mb-3">
                            <label for="comisionId" class="form-label fw-bold">
                                Comisión / Grupo de trabajo <span class="text-danger">*</span>
                            </label>
                            <c:choose>
                                <c:when test="${not empty comisionPreseleccionada}">
                                    <c:forEach var="comision" items="${comisiones}">
                                        <c:if test="${comisionPreseleccionada == comision.id}">
                                            <input type="text" class="form-control" value="<c:out value='${comision.nombre}'/>" disabled />
                                        </c:if>
                                    </c:forEach>
                                    <input type="hidden" id="comisionId" name="comisionId" value="${comisionPreseleccionada}" />
                                </c:when>
                                <c:otherwise>
                                    <select class="form-select" id="comisionId" name="comisionId" required>
                                        <option value="">Seleccione una comisión...</option>
                                        <c:forEach var="comision" items="${comisiones}">
                                            <option value="${comision.id}"><c:out value="${comision.nombre}"/></option>
                                        </c:forEach>
                                    </select>
                                </c:otherwise>
                            </c:choose>
                        </div>

                        <div class="row g-3">
                            <div class="col-md-4">
                                <label for="fechaReunion" class="form-label fw-bold">Fecha <span class="text-danger">*</span></label>
                                <input type="date" class="form-control" id="fechaReunion" name="fechaReunion" required>
                            </div>
                            <div class="col-md-4">
                                <label for="horaInicio" class="form-label fw-bold">Hora inicio</label>
                                <input type="time" class="form-control" id="horaInicio" name="horaInicio">
                            </div>
                            <div class="col-md-4">
                                <label for="horaFin" class="form-label fw-bold">Hora fin</label>
                                <input type="time" class="form-control" id="horaFin" name="horaFin">
                            </div>
                        </div>
                    </div>
                </div>

                <div class="row g-4 mb-4">
                    <div class="col-lg-7">
                        <div class="border rounded h-100">
                            <div class="bg-light border-bottom px-3 py-2 text-center fw-bold">
                                ASISTENTES
                            </div>
                            <div class="px-3 py-1 text-center text-muted small border-bottom">
                                (Nombre y cargo)
                            </div>
                            <div class="p-3">
                                <div id="miembrosContainer">
                                    <div class="alert alert-info mb-0">
                                        <i class="bi bi-info-circle"></i>
                                        Seleccione una comisión para cargar los miembros
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>

                    <div class="col-lg-5">
                        <div class="border rounded h-100">
                            <div class="p-3 border-bottom">
                                <label for="duracion" class="form-label fw-bold">Duración</label>
                                <input type="text" class="form-control" id="duracion" name="duracion" placeholder="Ej: 1h 30 min">
                            </div>
                            <div class="p-3 border-bottom">
                                <label class="form-label fw-bold d-block">TIPO REUNIÓN</label>
                                <div class="form-check">
                                    <input class="form-check-input" type="radio" name="tipoReunion" id="tipoReunionCalidad" value="CALIDAD">
                                    <label class="form-check-label" for="tipoReunionCalidad">Revisión del Sist. Calidad</label>
                                </div>
                                <div class="form-check">
                                    <input class="form-check-input" type="radio" name="tipoReunion" id="tipoReunionInterna" value="INTERNA">
                                    <label class="form-check-label" for="tipoReunionInterna">Reunión Interna</label>
                                </div>
                                <div class="form-check">
                                    <input class="form-check-input" type="radio" name="tipoReunion" id="tipoReunionOtros" value="OTROS">
                                    <label class="form-check-label" for="tipoReunionOtros">Otros</label>
                                </div>
                                <div class="mt-2" id="tipoReunionOtrosDetalleContainer" style="display: none;">
                                    <label for="tipoReunionOtrosDetalle" class="form-label">Especificar</label>
                                    <textarea class="form-control" id="tipoReunionOtrosDetalle" name="tipoReunionOtrosDetalle" rows="3"></textarea>
                                </div>
                            </div>
                            <div class="p-3">
                                <label for="excusaAsistencia" class="form-label fw-bold">Excusa asistencia</label>
                                <textarea class="form-control bg-light" id="excusaAsistencia" name="excusaAsistencia" rows="6" readonly
                                          placeholder="Se rellena automáticamente al justificar la ausencia de un miembro en la lista de asistentes"></textarea>
                                <div class="form-text">Este campo se genera automáticamente a partir de las justificaciones indicadas en "Excusa asistencia" de la lista de asistentes.</div>
                            </div>
                        </div>
                    </div>
                </div>

                <div class="border rounded mb-4">
                    <div class="bg-light border-bottom px-3 py-2 fw-bold">
                        ORDEN DEL DÍA
                    </div>
                    <div class="p-3">
                        <textarea class="form-control" id="ordenDia" name="ordenDia" rows="5" placeholder="Indique los puntos tratados en la reunión"></textarea>
                    </div>
                </div>

                <div class="border rounded mb-4">
                    <div class="bg-light border-bottom px-3 py-2 fw-bold">
                        RESUMEN DE LA REUNIÓN
                    </div>
                    <div class="p-3">
                        <textarea class="form-control" id="observaciones" name="observaciones" rows="10" placeholder="Detalle aquí el resumen de la reunión, acuerdos y observaciones"></textarea>
                        <div class="form-text">Máximo 20.000 caracteres.</div>
                    </div>
                </div>

                <div class="mb-4 no-print">
                    <label for="pdfFile" class="form-label fw-bold">
                        <i class="bi bi-file-pdf text-danger"></i> Adjuntar documento PDF (opcional)
                    </label>
                    <input class="form-control" type="file" id="pdfFile" name="pdfFile" accept="application/pdf" />
                    <div class="form-text">Tamaño máximo: 5MB. Solo archivos PDF (.pdf)</div>
                    <div id="pdfInfo" class="mt-2" style="display: none;"></div>
                </div>

                <div class="d-flex justify-content-between no-print">
                    <a href="${pageContext.request.contextPath}/" class="btn btn-secondary">
                        <i class="bi bi-x-circle"></i> Cancelar
                    </a>
                    <div>
                        <button type="button" class="btn btn-outline-secondary me-2" id="btnLimpiarActa">
                            <i class="bi bi-arrow-clockwise"></i> Limpiar
                        </button>
                        <button type="submit" class="btn btn-primary" id="btnGuardar">
                            <i class="bi bi-save"></i> Guardar Acta
                        </button>
                    </div>
                </div>
            </form>
        </div>
    </div>
</div>

<script src="${pageContext.request.contextPath}/resources/js/actas.js"></script>
<%@ include file="/WEB-INF/views/common/footer.jspf" %>
