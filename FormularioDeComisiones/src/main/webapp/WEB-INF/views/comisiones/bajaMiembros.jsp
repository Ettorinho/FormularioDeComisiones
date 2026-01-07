<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<jsp:useBean id="now" class="java.util.Date" />
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Baja de miembros de comisión</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
</head>
<body>
<div class="container mt-4">
    <div class="card">
        <div class="card-header d-flex justify-content-between align-items-center">
            <h3>Baja de miembros en la comisión: ${comision.nombre}</h3>
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
                                <td>${cm.miembro.nombreApellidos}</td>
                                <td>${cm.miembro.dniNif}</td>
                                <td>${cm.miembro.email}</td>
                                <td>${cm.cargo}</td>
                                <td><fmt:formatDate value="${cm.fechaIncorporacion}" pattern="dd/MM/yyyy" /></td>
                                <td>
                                    <form action="${pageContext.request.contextPath}/comisiones/bajaMiembro/${comision.id}/${cm.miembro.id}" method="post" style="display:inline;">
                                        <input type="date" name="fechaBaja" 
                                               value="<fmt:formatDate value='${now}' pattern='yyyy-MM-dd'/>" 
                                               max="<fmt:formatDate value='${now}' pattern='yyyy-MM-dd'/>" required>
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
<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>