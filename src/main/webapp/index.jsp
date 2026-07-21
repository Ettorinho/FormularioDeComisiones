<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<jsp:useBean id="now" class="java.util.Date" />
<c:set var="pageTitle" value="Sistema de Gestión de Comisiones — Gobierno de Aragón" />
<c:set var="headerSubtitle" value="Gobierno de Aragón" />
<%@ include file="/WEB-INF/views/common/header.jspf" %>

    <div class="container mt-4">
        <div class="card">
            <div class="card-header">
                <h3>Menú principal</h3>
            </div>
            <div class="card-body">
                <div class="d-flex flex-column gap-2">

                    <%-- Opciones visibles para TODOS los roles autenticados --%>
                    <c:if test="${sessionScope.rolUsuario == 'LECTURA' || sessionScope.rolUsuario == 'GESTOR' || sessionScope.rolUsuario == 'ADMIN'}">
                        <a href="${pageContext.request.contextPath}/comisiones" class="btn btn-primary btn-lg text-start">
                            <i class="bi bi-people"></i> Ver Comisiones
                        </a>
                    </c:if>

                    <%-- Fallback: sin rol asignado --%>
                    <c:if test="${empty sessionScope.rolUsuario}">
                        <div class="alert alert-warning">
                            No tiene ningún rol asignado. Contacte con el administrador del sistema.
                        </div>
                    </c:if>

                </div>
            </div>
        </div>
    </div>
<%@ include file="/WEB-INF/views/common/footer.jspf" %>
