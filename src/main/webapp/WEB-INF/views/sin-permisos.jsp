<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="pageTitle" value="Acceso denegado" />
<c:set var="headerIcon" value="bi-shield-lock" />
<%@ include file="/WEB-INF/views/common/header.jspf" %>

<div class="container mt-5">
    <div class="row justify-content-center">
        <div class="col-md-6 text-center">
            <div class="alert alert-warning shadow-sm p-4">
                <i class="bi bi-shield-lock display-4 text-warning mb-3 d-block"></i>
                <h4 class="alert-heading">Acceso denegado</h4>
                <p>No tienes permisos suficientes para acceder a esta sección.</p>
                <hr>
                <p class="mb-0 text-muted small">
                    Contacta con el administrador del sistema para solicitar el acceso necesario.
                </p>
            </div>
            <a href="${pageContext.request.contextPath}/comisiones" class="btn btn-primary mt-3">
                <i class="bi bi-arrow-left"></i> Volver al inicio
            </a>
        </div>
    </div>
</div>
<%@ include file="/WEB-INF/views/common/footer.jspf" %>
