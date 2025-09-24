<%-- 
    Document   : buscarPorDni
    Created on : 22 sept. 2025, 11:04:09
    Author     : hlisa-admin
--%>

<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<jsp:useBean id="now" class="java.util.Date" />
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Buscar Comisiones por DNI</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
</head>
<body>
<div class="container mt-4">
    <h2>Buscar Miembro</h2>
    <form action="${pageContext.request.contextPath}/comisiones/buscarPorDni" method="post" class="row g-3 mb-4">
        <div class="col-auto">
            <input type="text" name="dni" class="form-control" placeholder="Introduce DNI" required value="${dniBuscado != null ? dniBuscado : ''}">
        </div>
        <div class="col-auto">
            <button type="submit" class="btn btn-primary mb-3">Buscar</button>
        </div>
    </form>
    <c:if test="${not empty dniBuscado}">
        <c:choose>
            <c:when test="${empty miembro}">
                <div class="alert alert-warning">No se encontró ningún miembro con ese DNI.</div>
            </c:when>
            <c:otherwise>
                <div class="mb-3">
                    <h5>Miembro: ${miembro.nombreApellidos} (${miembro.dniNif})</h5>
                </div>
                <c:if test="${empty comisiones}">
                    <div class="alert alert-info">Este miembro no pertenece a ninguna comisión.</div>
                </c:if>
                <c:if test="${not empty comisiones}">
                    <table class="table table-striped">
                        <thead>
                            <tr>
                                <th>Comisión</th>
                                <th>Fecha Constitución</th>
                                <th>Fecha Fin</th>
                                <th>Cargo</th>
                                <th>Estado</th>
                            </tr>
                        </thead>
                        <tbody>
                            <c:forEach var="cm" items="${comisiones}">
                                <tr>
                                    <td>${cm.comision.nombre}</td>
                                    <td><fmt:formatDate value="${cm.comision.fechaConstitucion}" pattern="dd/MM/yyyy" /></td>
                                    <td>
                                        <c:if test="${not empty cm.comision.fechaFin}">
                                            <fmt:formatDate value="${cm.comision.fechaFin}" pattern="dd/MM/yyyy" />
                                        </c:if>
                                        <c:if test="${empty cm.comision.fechaFin}">
                                            -
                                        </c:if>
                                    </td>
                                    <td>${cm.cargo}</td>
                                    <td>
                                        <c:choose>
                                            <c:when test="${empty cm.comision.fechaFin}">
                                                <span class="badge bg-success">Activa</span>
                                            </c:when>
                                            <c:when test="${cm.comision.fechaFin > now}">
                                                <span class="badge bg-success">Activa</span>
                                            </c:when>
                                            <c:otherwise>
                                                <span class="badge bg-secondary">Finalizada</span>
                                            </c:otherwise>
                                        </c:choose>
                                    </td>
                                </tr>
                            </c:forEach>
                        </tbody>
                    </table>
                </c:if>
            </c:otherwise>
        </c:choose>
    </c:if>
    <a href="${pageContext.request.contextPath}/" class="btn btn-secondary mt-3">Volver al Inicio</a>
</div>
</body>
</html>
