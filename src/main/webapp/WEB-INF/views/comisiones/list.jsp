<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<jsp:useBean id="now" class="java.util.Date" />
<c:set var="pageTitle" value="Listado de Comisiones" />
<%@ include file="/WEB-INF/views/common/header.jspf" %>

    <div class="container mt-4">
        <div class="d-flex justify-content-between align-items-center mb-3">
            <h2>Comisiones y Grupos</h2>
            <c:if test="${sessionScope.rolUsuario == 'ADMIN'}">
                <a href="${pageContext.request.contextPath}/comisiones/new" class="btn btn-primary">Nueva Comisión</a>
            </c:if>
        </div>
        <c:if test="${not empty comisiones}">
            <table class="table table-striped">
                <thead>
                    <tr>
                        <th>Nombre</th>
                        <th>Fecha Constitución</th>
                        <th>Estado</th>
                        <th>Acciones</th>
                    </tr>
                </thead>
                 <tbody>
                    <c:forEach items="${comisiones}" var="comision">
                        <tr>
                            <td><c:out value="${comision.nombre}"/></td>
                            <td><fmt:formatDate pattern="dd/MM/yyyy" value="${comision.fechaConstitucion}" /></td>
                            <td>
                                <c:choose>
                                    <c:when test="${empty comision.fechaFin}">
                                        <span class="badge bg-success">Activa</span>
                                    </c:when>
                                    <c:when test="${comision.fechaFin > now}">
                                        <span class="badge bg-success">Activa</span>
                                    </c:when>
                                    <c:otherwise>
                                        <span class="badge bg-secondary">Finalizada</span>
                                    </c:otherwise>
                                </c:choose>
                            </td>
                            <td>
                                <a href="${pageContext.request.contextPath}/comisiones/view/${comision.id}" class="btn btn-info btn-sm">Ver</a>
                            </td>
                        </tr>
                    </c:forEach>
                </tbody>
            </table>
        </c:if>
        <c:if test="${empty comisiones}">
            <div class="alert alert-info">No hay comisiones registradas.</div>
        </c:if>
        <c:if test="${totalPaginas > 1}">
            <nav aria-label="Paginación de comisiones">
                <ul class="pagination justify-content-center mt-3">
                    <li class="page-item ${paginaActual <= 1 ? 'disabled' : ''}">
                        <a class="page-link" href="${pageContext.request.contextPath}/comisiones/list?page=${paginaActual - 1}">
                            <i class="bi bi-chevron-left"></i> Anterior
                        </a>
                    </li>
                    <li class="page-item disabled">
                        <span class="page-link">Página ${paginaActual} de ${totalPaginas}</span>
                    </li>
                    <li class="page-item ${paginaActual >= totalPaginas ? 'disabled' : ''}">
                        <a class="page-link" href="${pageContext.request.contextPath}/comisiones/list?page=${paginaActual + 1}">
                            Siguiente <i class="bi bi-chevron-right"></i>
                        </a>
                    </li>
                </ul>
            </nav>
        </c:if>
        <a href="${pageContext.request.contextPath}/" class="btn btn-secondary mt-3">Volver al Inicio</a>
    </div>
<%@ include file="/WEB-INF/views/common/footer.jspf" %>
