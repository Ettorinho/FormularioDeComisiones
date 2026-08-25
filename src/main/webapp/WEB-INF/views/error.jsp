<%@ page isErrorPage="true" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="pageTitle" value="Error" />
<c:set var="headerIcon" value="bi-exclamation-triangle" />
<%@ include file="/WEB-INF/views/common/header.jspf" %>
<div class="container mt-5">
    <div class="alert alert-danger">
        <h4 class="alert-heading">Ha ocurrido un error</h4>
        <p>Se ha producido un error inesperado en la aplicación.</p>
        <hr>
        <p class="mb-0">
            <strong>Mensaje:</strong>
            <c:out value="${error != null ? error : 'No se proporcionó un mensaje de error específico.'}"/>
        </p>
    </div>
    <a href="${pageContext.request.contextPath}/" class="btn btn-primary">Volver al inicio</a>
</div>
<%@ include file="/WEB-INF/views/common/footer.jspf" %>
