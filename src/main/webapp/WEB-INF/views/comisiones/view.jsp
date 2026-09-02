<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<jsp:useBean id="now" class="java.util.Date" />
<c:set var="pageTitle" value="Detalles de ${comision.nombre}" />
<c:set var="headerIcon" value="bi-people" />
<%@ include file="/WEB-INF/views/common/header.jspf" %>
<div class="container mt-4">
    <div class="card">
        <div class="card-header d-flex justify-content-between align-items-center">
            <h3><c:out value="${comision.nombre}"/></h3>
            <a href="${pageContext.request.contextPath}/comisiones/" class="btn btn-secondary">Volver al listado</a>
        </div>
        <div class="card-body">
            <p><strong>Fecha de Constitución:</strong> <fmt:formatDate value="${comision.fechaConstitucion}" pattern="dd/MM/yyyy" /></p>
            <p><strong>Fecha de Fin:</strong> 
                <c:if test="${not empty comision.fechaFin}">
                    <fmt:formatDate value="${comision.fechaFin}" pattern="dd/MM/yyyy" />
                </c:if>
                <c:if test="${empty comision.fechaFin}">
                    <span class="badge bg-success">Activa</span>
                </c:if>
            </p>
            <p>
                <strong>Estado:</strong>
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
            </p>
            <hr/>
            <div class="d-flex justify-content-between align-items-center mb-3">
                <h4>Miembros</h4>
                <c:if test="${(empty comision.fechaFin || comision.fechaFin > now) && (rolUsuario == 'ADMIN' || rolUsuario == 'GESTOR')}">
                    <div class="d-flex flex-nowrap align-items-center gap-2 overflow-auto">
                        <a href="${pageContext.request.contextPath}/actas/new?comisionId=${comision.id}" class="btn btn-success text-nowrap">
                            <i class="bi bi-file-earmark-plus"></i> Crear Acta
                        </a>
                        <a href="${pageContext.request.contextPath}/comisiones/addMember/${comision.id}" class="btn btn-primary text-nowrap">Añadir Miembro</a>
                        <a href="${pageContext.request.contextPath}/comisiones/bajaMiembros/${comision.id}" class="btn btn-warning text-nowrap">Dar de baja a Miembros</a>
                        <a href="${pageContext.request.contextPath}/actas/generate-blank-template" class="btn btn-outline-secondary text-nowrap">
                            <i class="bi bi-file-earmark-arrow-down"></i> Plantilla Acta PDF
                        </a>
                        <a href="${pageContext.request.contextPath}/actas/generate-blank-template-word" class="btn btn-outline-secondary text-nowrap">
                            <i class="bi bi-file-earmark-word"></i> Plantilla Acta Word
                        </a>
                    </div>
                </c:if>
            </div>
            
            <c:if test="${empty miembros}">
                <div class="alert alert-info">No hay miembros en esta comisión.</div>
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
                            <th>Acciones</th>
                        </tr>
                    </thead>
                    <tbody>
                        <c:forEach var="cm" items="${miembros}">
                            <tr>
                                <td><c:out value="${cm.miembro.nombreApellidos}"/></td>
                                <td><c:out value="${cm.miembro.dniNif}"/></td>
                                <td><c:out value="${cm.miembro.email}"/></td>
                                <td><c:out value="${cm.cargo}"/></td>
                                <td><fmt:formatDate value="${cm.fechaIncorporacion}" pattern="dd/MM/yyyy" /></td>
                                <td>
                                    <c:choose>
                                        <c:when test="${empty cm.fechaBaja}">
                                            <c:if test="${rolUsuario == 'ADMIN' || rolUsuario == 'GESTOR'}">
                                                <a href="${pageContext.request.contextPath}/comisiones/cambiarCargo?comisionId=${comision.id}&miembroId=${cm.miembro.id}" 
                                                   class="btn btn-warning btn-sm" 
                                                   title="Cambiar cargo del miembro">
                                                    <i class="bi bi-arrow-left-right"></i> Cambiar Cargo
                                                </a>
                                            </c:if>
                                        </c:when>
                                        <c:otherwise>
                                            <span class="badge bg-secondary">Baja: <fmt:formatDate value="${cm.fechaBaja}" pattern="dd/MM/yyyy"/></span>
                                        </c:otherwise>
                                    </c:choose>
                                </td>
                            </tr>
                        </c:forEach>
                    </tbody>
                </table>
            </c:if>

            <hr/>
            <div class="mt-4">
                <h4>Actas de Reuniones</h4>
                <c:if test="${empty actas}">
                    <div class="alert alert-info">No hay actas registradas para esta comisión.</div>
                </c:if>
                <c:if test="${not empty actas}">
                    <table class="table table-hover">
                        <thead>
                            <tr>
                                <th>Título</th>
                                <th>Fecha de Reunión</th>
                                <th>Acciones</th>
                            </tr>
                        </thead>
                        <tbody>
                            <c:forEach var="acta" items="${actas}">
                                <tr>
                                    <td><strong><c:out value="${acta.titulo}"/></strong></td>
                                    <td>${acta.fechaReunionFormateada}</td>
                                    <td>
                                        <a href="${pageContext.request.contextPath}/actas/view?id=${acta.id}"
                                           class="btn btn-primary btn-sm">
                                            <i class="bi bi-eye"></i> Ver
                                        </a>
                                    </td>
                                </tr>
                            </c:forEach>
                        </tbody>
                    </table>
                </c:if>
            </div>
        </div>
    </div>
</div>
<%@ include file="/WEB-INF/views/common/footer.jspf" %>
