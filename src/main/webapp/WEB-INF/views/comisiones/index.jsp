<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="pageTitle" value="Gestión de Comisiones" />
<c:set var="headerSubtitle" value="Gobierno de Aragón" />
<c:set var="headerIcon" value="bi-people" />
<%@ include file="/WEB-INF/views/common/header.jspf" %>

    <div class="container mt-4">
        <div class="card shadow-sm">
            <div class="card-header bg-primary text-white">
                <h3 class="mb-0"><i class="bi bi-people"></i> Gestión de Comisiones</h3>
            </div>
            <div class="card-body p-4">
                <p class="text-muted mb-4">Selecciona una opción para gestionar las comisiones y grupos de trabajo:</p>

                <div class="row g-4">
                    <div class="col-md-4">
                        <a href="${pageContext.request.contextPath}/comisiones/list"
                           class="btn btn-primary btn-lg w-100 h-100 d-flex flex-column justify-content-center align-items-center p-4 text-decoration-none">
                            <i class="bi bi-list-ul display-4 mb-3"></i>
                            <h5 class="mb-2">Ver Todas las Comisiones</h5>
                            <p class="mb-0 small">Listado completo de comisiones y grupos</p>
                        </a>
                    </div>
                    <div class="col-md-4">
                        <a href="${pageContext.request.contextPath}/comisiones/buscarPorDni"
                           class="btn btn-info btn-lg w-100 h-100 d-flex flex-column justify-content-center align-items-center p-4 text-decoration-none">
                            <i class="bi bi-person-search display-4 mb-3"></i>
                            <h5 class="mb-2">Buscar por DNI</h5>
                            <p class="mb-0 small">Encuentra las comisiones de un miembro</p>
                        </a>
                    </div>
                    <div class="col-md-4">
                        <a href="${pageContext.request.contextPath}/comisiones/buscarComision"
                           class="btn btn-info btn-lg w-100 h-100 d-flex flex-column justify-content-center align-items-center p-4 text-decoration-none">
                            <i class="bi bi-search display-4 mb-3"></i>
                            <h5 class="mb-2">Buscar Comisión</h5>
                            <p class="mb-0 small">Busca comisiones por nombre</p>
                        </a>
                    </div>
                </div>
            </div>
        </div>

        <a href="${pageContext.request.contextPath}/" class="btn btn-secondary mt-3">
            <i class="bi bi-house"></i> Volver al Inicio
        </a>
    </div>

<%@ include file="/WEB-INF/views/common/footer.jspf" %>
