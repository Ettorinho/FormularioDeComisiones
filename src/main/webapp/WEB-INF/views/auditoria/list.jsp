<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<c:set var="pageTitle" value="Historial de Auditoría" />
<c:set var="headerIcon" value="bi-shield-check" />
<%@ include file="/WEB-INF/views/common/header.jspf" %>

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
                    <div class="col-md-3">
                        <label for="usuario" class="form-label">Usuario</label>
                        <input type="text" id="usuario" name="usuario" class="form-control"
                               placeholder="Nombre de usuario AD"
                               value="<c:out value='${filtroUsuario}'/>">
                    </div>
                    <div class="col-md-2">
                        <label for="resultado" class="form-label">Resultado</label>
                        <select id="resultado" name="resultado" class="form-select">
                            <option value="">Todos</option>
                            <option value="EXITOSO"          ${filtroResultado == 'EXITOSO'          ? 'selected' : ''}>Exitoso</option>
                            <option value="FALLIDO"          ${filtroResultado == 'FALLIDO'          ? 'selected' : ''}>Fallido</option>
                            <option value="DENEGADO"         ${filtroResultado == 'DENEGADO'         ? 'selected' : ''}>Denegado</option>
                            <option value="VALIDACION_ERROR" ${filtroResultado == 'VALIDACION_ERROR' ? 'selected' : ''}>Error validación</option>
                        </select>
                    </div>
                    <div class="col-md-2">
                        <label for="fechaDesde" class="form-label">Desde</label>
                        <input type="date" id="fechaDesde" name="fechaDesde" class="form-control"
                               value="<c:out value='${fechaDesde}'/>">
                    </div>
                    <div class="col-md-2">
                        <label for="fechaHasta" class="form-label">Hasta</label>
                        <input type="date" id="fechaHasta" name="fechaHasta" class="form-control"
                               value="<c:out value='${fechaHasta}'/>">
                    </div>
                    <div class="col-md-1">
                        <button type="submit" class="btn btn-primary w-100">
                            <i class="bi bi-search"></i>
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
                                <th>Resultado</th>
                                <th>Duración</th>
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
                                            <c:when test="${accion.accion == 'LOGOUT'}">
                                                <span class="badge bg-secondary"><c:out value="${accion.accion}"/></span>
                                            </c:when>
                                            <c:when test="${accion.accion == 'LOGIN_FALLIDO' || accion.accion == 'ACCESS_DENIED'}">
                                                <span class="badge bg-danger"><c:out value="${accion.accion}"/></span>
                                            </c:when>
                                            <c:when test="${accion.accion == 'GENERAR'}">
                                                <span class="badge bg-info text-dark"><c:out value="${accion.accion}"/></span>
                                            </c:when>
                                            <c:when test="${accion.accion == 'DESCARGAR'}">
                                                <span class="badge bg-info text-dark"><c:out value="${accion.accion}"/></span>
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
                                    <td>
                                        <c:choose>
                                            <c:when test="${accion.resultado == 'EXITOSO'}">
                                                <span class="badge bg-success"><c:out value="${accion.resultado}"/></span>
                                            </c:when>
                                            <c:when test="${accion.resultado == 'FALLIDO' || accion.resultado == 'DENEGADO'}">
                                                <span class="badge bg-danger"><c:out value="${accion.resultado}"/></span>
                                            </c:when>
                                            <c:when test="${accion.resultado == 'VALIDACION_ERROR'}">
                                                <span class="badge bg-warning text-dark"><c:out value="${accion.resultado}"/></span>
                                            </c:when>
                                            <c:otherwise>
                                                <c:out value="${accion.resultado}"/>
                                            </c:otherwise>
                                        </c:choose>
                                    </td>
                                    <td class="text-nowrap">
                                        <c:if test="${accion.duracionMs != null}">
                                            <c:out value="${accion.duracionMs}"/> ms
                                        </c:if>
                                    </td>
                                </tr>
                            </c:forEach>
                        </tbody>
                    </table>
                </div>

                <!-- Paginación -->
                <div class="d-flex justify-content-between align-items-center mt-2">
                    <p class="text-muted small mb-0">
                        <i class="bi bi-info-circle me-1"></i>
                        Mostrando <strong>${acciones.size()}</strong> de <strong>${totalRegistros}</strong> registro(s).
                        Página ${paginaActual} de ${totalPaginas}.
                    </p>
                    <ul class="pagination pagination-sm mb-0">
                        <li class="page-item ${paginaActual <= 1 ? 'disabled' : ''}">
                            <a class="page-link" href="${pageContext.request.contextPath}/auditoria?page=${paginaActual - 1}&usuario=${filtroUsuario}&resultado=${filtroResultado}&fechaDesde=${fechaDesde}&fechaHasta=${fechaHasta}">
                                <i class="bi bi-chevron-left"></i>
                            </a>
                        </li>
                        <li class="page-item disabled">
                            <span class="page-link">Página ${paginaActual} / ${totalPaginas}</span>
                        </li>
                        <li class="page-item ${paginaActual >= totalPaginas ? 'disabled' : ''}">
                            <a class="page-link" href="${pageContext.request.contextPath}/auditoria?page=${paginaActual + 1}&usuario=${filtroUsuario}&resultado=${filtroResultado}&fechaDesde=${fechaDesde}&fechaHasta=${fechaHasta}">
                                <i class="bi bi-chevron-right"></i>
                            </a>
                        </li>
                    </ul>
                </div>
            </c:when>
            <c:otherwise>
                <div class="alert alert-info">
                    <i class="bi bi-info-circle me-1"></i>No se encontraron registros de auditoría.
                </div>
            </c:otherwise>
        </c:choose>
    </div>

<%@ include file="/WEB-INF/views/common/footer.jspf" %>
