<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<jsp:useBean id="now" class="java.util.Date" />
<c:set var="pageTitle" value="Baja de miembros de comisión" />
<%@ include file="/WEB-INF/views/common/header.jspf" %>

<div class="container mt-4">
    <div class="card">
        <div class="card-header d-flex justify-content-between align-items-center">
            <h3>Baja de miembros en la comisión: <c:out value="${comision.nombre}"/></h3>
            <a href="${pageContext.request.contextPath}/comisiones/view/${comision.id}" class="btn btn-secondary">Volver a la comisión</a>
        </div>
        <div class="card-body">
            <c:if test="${empty miembros}">
                <div class="alert alert-info">No hay miembros activos en esta comisión.</div>
            </c:if>
            <c:if test="${not empty miembros}">
                <table class="table table-hover">
                    <thead>
                        <tr>
                            <th>Nombre</th>
                            <th>DNI/NIF</th>
                            <th>Email</th>
                            <th>Cargo</th>
                            <th>Incorporación</th>
                            <th>Dar de baja</th>
                        </tr>
                    </thead>
                    <tbody>
                        <c:forEach var="cm" items="${miembros}">
                            <tr>
                                <td><c:out value="${cm.miembro.nombreApellidos}"/></td>
                                <td><c:out value="${cm.miembro.dniNif}"/></td>
                                <td><c:out value="${cm.miembro.email}"/></td>
                                <td>${cm.cargo}</td>
                                <td><fmt:formatDate value="${cm.fechaIncorporacion}" pattern="dd/MM/yyyy" /></td>
                                <td>
                                    <form action="${pageContext.request.contextPath}/comisiones/bajaMiembro/${comision.id}/${cm.miembro.id}" method="post" class="form-inline-action">
                                        <input type="hidden" name="csrfToken" value="${csrfToken}" />
                                        <input type="date" name="fechaBaja" 
                                               value="<fmt:formatDate value='${now}' pattern='yyyy-MM-dd'/>" 
                                               max="<fmt:formatDate value='${now}' pattern='yyyy-MM-dd'/>" required />
                                        <button type="submit" class="btn btn-danger btn-sm" onclick="return confirm('¿Seguro que quieres dar de baja a este miembro?');">Dar de baja</button>
                                    </form>
                                </td>
                            </tr>
                        </c:forEach>
                    </tbody>
                </table>
            </c:if>
        </div>
    </div>
</div>
<%@ include file="/WEB-INF/views/common/footer.jspf" %>
