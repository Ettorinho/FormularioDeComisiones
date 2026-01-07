<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Buscar Comisión o Grupo de Trabajo</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
</head>
<body>
<div class="container mt-4">
    <h2>Buscar Comisión o Grupo de Trabajo</h2>
    <form action="${pageContext.request.contextPath}/comisiones/buscarComision" method="post" class="row g-3 mb-4">
        <div class="col-auto">
            <input type="text" name="nombre" class="form-control" placeholder="Nombre de comisión o grupo" required value="${nombreBuscado != null ? nombreBuscado : ''}">
        </div>
        <div class="col-auto">
            <button type="submit" class="btn btn-primary mb-3">Buscar</button>
        </div>
    </form>
    <c:if test="${not empty nombreBuscado}">
        <c:choose>
            <c:when test="${empty comisiones}">
                <div class="alert alert-warning">No se encontraron comisiones o grupos con ese nombre.</div>
            </c:when>
            <c:otherwise>
                <c:forEach var="comision" items="${comisiones}">
                    <div class="card mb-4">
                        <div class="card-header">
                            <strong>${comision.nombre}</strong>
                        </div>
                        <div class="card-body">
                            <p>
                                <strong>Fecha de Constitución:</strong>
                                <fmt:formatDate value="${comision.fechaConstitucion}" pattern="dd/MM/yyyy"/>
                                <br/>
                                <strong>Fecha de Fin:</strong>
                                <c:if test="${not empty comision.fechaFin}">
                                    <fmt:formatDate value="${comision.fechaFin}" pattern="dd/MM/yyyy"/>
                                </c:if>
                                <c:if test="${empty comision.fechaFin}">
                                    -
                                </c:if>
                            </p>
                            <h5>Miembros:</h5>
                            <c:if test="${empty comision.miembros}">
                                <div class="alert alert-info mb-0">No hay miembros registrados.</div>
                            </c:if>
                            <c:if test="${not empty comision.miembros}">
                                <table class="table table-sm">
                                    <thead>
                                        <tr>
                                            <th>Nombre y Apellidos</th>
                                            <th>DNI/NIF</th>
                                            <th>Email</th>
                                            <th>Cargo</th>
                                            <th>Fecha Incorporación</th>
                                            <th>Fecha de Baja</th>
                                        </tr>
                                    </thead>
                                    <tbody>
                                        <c:forEach var="cm" items="${comision.miembros}">
                                            <tr>
                                                <td>${cm.miembro.nombreApellidos}</td>
                                                <td>${cm.miembro.dniNif}</td>
                                                <td>${cm.miembro.email}</td>
                                                <td>${cm.cargo}</td>
                                                <td><fmt:formatDate value="${cm.fechaIncorporacion}" pattern="dd/MM/yyyy"/></td>
                                                <td>
                                                    <c:if test ="${not empty cm.fechaBaja}">
                                                        <fmt:formatDate value ="${cm.fechaBaja}" pattern="dd/MM/yyyy"/>
                                                    </c:if>
                                                    <c:if test="{empty cm.fechaBaja}">
                                                        -
                                                    </c:if>    
                                                </td>
                                            </tr>
                                        </c:forEach>
                                    </tbody>
                                </table>
                            </c:if>
                        </div>
                    </div>
                </c:forEach>
            </c:otherwise>
        </c:choose>
    </c:if>
    <a href="${pageContext.request.contextPath}/" class="btn btn-secondary mt-3">Volver al Inicio</a>
</div>
<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>