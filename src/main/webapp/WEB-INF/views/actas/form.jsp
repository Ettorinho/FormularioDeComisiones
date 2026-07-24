
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<c:set var="pageTitle" value="Nueva Acta de Reunión" />
<c:set var="headerSubtitle" value="Crear Nueva Acta de Reunión" />
<c:set var="headerClass" value="no-print" />
<%@ include file="/WEB-INF/views/common/header.jspf" %>
    <!-- Contenido Principal -->
    <div class="container mt-4 mb-5">
        
        <!-- Breadcrumb -->
        <nav aria-label="breadcrumb">
            <ol class="breadcrumb">
                <li class="breadcrumb-item"><a href="${pageContext.request.contextPath}/">Inicio</a></li>
                <li class="breadcrumb-item active">Nueva Acta</li>
            </ol>
        </nav>

        <div class="card shadow-sm">
            <div class="card-header bg-primary text-white">
                <h3 class="mb-0">
                    <i class="bi bi-file-earmark-plus"></i> Crear Acta de Reunión
                </h3>
            </div>
            <div class="card-body">
                
                <!-- Formulario con enctype para archivos -->
                <form id="formActa" method="post" action="${pageContext.request.contextPath}/actas/save" enctype="multipart/form-data">
                    <input type="hidden" name="csrfToken" value="${csrfToken}" />
                    
                    <!-- Selección de Comisión -->
                    <div class="mb-3">
                        <label for="comisionId" class="form-label">
                            <i class="bi bi-building"></i> Comisión / Grupo <span class="text-danger">*</span>
                        </label>
                        <c:choose>
                            <%-- Si venimos desde la vista de una comisión concreta, el acta queda
                                 bloqueada a esa comisión: se muestra deshabilitado (solo lectura)
                                 y se envía el ID real mediante un campo oculto, ya que los
                                 elementos <select disabled> no se envían en el submit del formulario. --%>
                            <c:when test="${not empty comisionPreseleccionada}">
                                <c:forEach var="comision" items="${comisiones}">
                                    <c:if test="${comisionPreseleccionada == comision.id}">
                                        <input type="text" class="form-control" value="<c:out value='${comision.nombre}'/><c:if test='${not empty comision.area}'> - <c:out value="${comision.area.descripcion}"/></c:if>" disabled />
                                    </c:if>
                                </c:forEach>
                                <input type="hidden" id="comisionId" name="comisionId" value="${comisionPreseleccionada}" />
                                <div class="form-text">
                                    <i class="bi bi-lock"></i> El acta se creará para esta comisión. Para elegir otra, acceda desde "Nueva Acta" en el menú principal.
                                </div>
                            </c:when>
                            <c:otherwise>
                                <select class="form-select" id="comisionId" name="comisionId" required>
                                    <option value="">Seleccione una comisión...</option>
                                    <c:forEach var="comision" items="${comisiones}">
                                        <option value="${comision.id}">
                                            <c:out value="${comision.nombre}"/>
                                            <c:if test="${not empty comision.area}">
                                                - <c:out value="${comision.area.descripcion}"/>
                                            </c:if>
                                        </option>
                                    </c:forEach>
                                </select>
                            </c:otherwise>
                        </c:choose>
                    </div>

                    <!-- Título del Acta -->
                    <div class="mb-3">
                        <label for="titulo" class="form-label">
                            <i class="bi bi-card-heading"></i> Título del Acta <span class="text-danger">*</span>
                        </label>
                        <input type="text" 
                               class="form-control" 
                               id="titulo" 
                               name="titulo" 
                               placeholder="Ej: Reunión ordinaria de enero 2026"
                               maxlength="200"
                               required>
                        <div class="form-text">
                            Título descriptivo para identificar el acta (5-200 caracteres)
                        </div>
                    </div>

                    <!-- Fecha de Reunión -->
                    <div class="mb-3">
                        <label for="fechaReunion" class="form-label">
                            <i class="bi bi-calendar-event"></i> Fecha de la Reunión <span class="text-danger">*</span>
                        </label>
                        <input type="date" class="form-control" id="fechaReunion" name="fechaReunion" required>
                    </div>

                    <!-- Lista de Miembros (carga dinámica) -->
                    <div class="mb-4">
                        <h5 class="border-bottom pb-2 mb-3">
                            <i class="bi bi-people-fill"></i> Registro de Asistencia <span class="text-danger">*</span>
                        </h5>
                        <div id="miembrosContainer">
                            <div class="alert alert-info">
                                <i class="bi bi-info-circle"></i>
                                Seleccione una comisión para cargar los miembros
                            </div>
                        </div>
                    </div>

                    <!-- Observaciones -->
                    <div class="mb-3">
                        <label for="observaciones" class="form-label">
                            <i class="bi bi-journal-text"></i> Observaciones / Notas de la Reunión
                        </label>
                        <textarea class="form-control" id="observaciones" name="observaciones" rows="4" 
                                  placeholder="Escriba aquí las observaciones, acuerdos tomados, temas tratados, etc."></textarea>
                        <small class="text-muted">Campo opcional</small>
                    </div>

                    <!-- NUEVO: Campo para adjuntar PDF -->
                    <div class="mb-4">
                        <label for="pdfFile" class="form-label">
                            <i class="bi bi-file-pdf text-danger"></i> Adjuntar documento PDF (opcional)
                        </label>
                        <input class="form-control" type="file" id="pdfFile" name="pdfFile" accept="application/pdf" />
                        <div class="form-text">
                            <i class="bi bi-info-circle"></i> Tamaño máximo: 5MB. Solo archivos PDF (.pdf)
                        </div>
                        <div id="pdfInfo" class="mt-2" style="display: none;">
                            <div class="alert alert-success mb-0">
                                <i class="bi bi-file-earmark-pdf-fill"></i> 
                                <strong>Archivo seleccionado:</strong> <span id="pdfFileName"></span> 
                                (<span id="pdfFileSize"></span>)
                            </div>
                        </div>
                    </div>

                    <!-- Botones de acción -->
                    <div class="d-flex justify-content-between">
                        <a href="${pageContext.request.contextPath}/" class="btn btn-secondary">
                            <i class="bi bi-x-circle"></i> Cancelar
                        </a>
                        <button type="submit" class="btn btn-primary" id="btnGuardar">
                            <i class="bi bi-save"></i> Guardar Acta
                        </button>
                    </div>
                </form>
            </div>
        </div>
    </div>

    <script src="${pageContext.request.contextPath}/resources/js/actas.js"></script>
<%@ include file="/WEB-INF/views/common/footer.jspf" %>
