<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<jsp:useBean id="now" class="java.util.Date" />
<c:set var="pageTitle" value="Buscar Comisión o Grupo de Trabajo" />
<%@ include file="/WEB-INF/views/common/header.jspf" %>

    <div class="container mt-4">
        <h2>Buscar Comisión o Grupo de Trabajo</h2>
        <form action="${pageContext.request.contextPath}/comisiones/buscarComision" method="post" class="mb-4">
            <input type="hidden" name="csrfToken" value="${csrfToken}" />
            <div class="row g-3 align-items-end">
                <div class="col-auto">
                    <label for="nombre" class="form-label">Nombre de comisión o grupo</label>
                    <input type="text" name="nombre" id="nombre" class="form-control"
                           placeholder="Introduce nombre" required
                           value="<c:out value='${nombreBuscado != null ? nombreBuscado : ""}'/>">
                </div>
                <div class="col-auto">
                    <button type="submit" class="btn btn-primary">
                        <i class="bi bi-search"></i> Buscar
                    </button>
                </div>
            </div>
        </form>

        <c:if test="${not empty nombreBuscado}">
            <c:choose>
                <c:when test="${empty comisiones}">
                    <div class="alert alert-warning">No se encontraron comisiones o grupos con ese nombre.</div>
                </c:when>
                <c:otherwise>
                    <p class="text-muted">
                        <strong>Resultados:</strong> ${comisiones.size()} comisión(es) encontrada(s)
                    </p>
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
                </c:otherwise>
            </c:choose>
        </c:if>

        <a href="${pageContext.request.contextPath}/comisiones" class="btn btn-secondary mt-3">
            <i class="bi bi-arrow-left"></i> Volver a Comisiones
        </a>
    </div>

<%@ include file="/WEB-INF/views/common/footer.jspf" %>
