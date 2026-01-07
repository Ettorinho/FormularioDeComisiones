<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<jsp:useBean id="now" class="java.util.Date" />
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Detalles de ${comision.nombre}</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
</head>
<body>
<div class="container mt-4">
    <div class="card">
        <div class="card-header d-flex justify-content-between align-items-center">
            <h3>${comision.nombre}</h3>
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
                <c:if test="${empty comision.fechaFin || comision.fechaFin > now}">
                    <div>
                        <a href="${pageContext.request.contextPath}/comisiones/addMember/${comision.id}" class="btn btn-primary me-2">Añadir Miembro</a>
                        <a href="${pageContext.request.contextPath}/comisiones/bajaMiembros/${comision.id}" class="btn btn-warning">Dar de baja a Miembros</a>
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
                                <td>${cm.miembro.nombreApellidos}</td>
                                <td>${cm.miembro.dniNif}</td>
                                <td>${cm.miembro.email}</td>
                                <td>${cm.cargo}</td>
                                <td><fmt:formatDate value="${cm.fechaIncorporacion}" pattern="dd/MM/yyyy" /></td>
                                <td>
                                    <c:choose>
                                        <c:when test="${empty cm.fechaBaja}">
                                            <!-- No mostrar botón de baja aquí, solo mostrarlo en bajaMiembros.jsp -->
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
        </div>
    </div>
</div>
<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>