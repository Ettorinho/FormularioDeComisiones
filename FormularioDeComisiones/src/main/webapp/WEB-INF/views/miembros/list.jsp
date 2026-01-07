<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Listado de Miembros</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
</head>
<body>
    <div class="container mt-4">
        <div class="d-flex justify-content-between align-items-center mb-3">
            <h2>Listado de Miembros</h2>
            <%-- Opcional: Añadir un botón para crear un nuevo miembro si tienes esa funcionalidad --%>
            <%-- <a href="${pageContext.request.contextPath}/miembros/new" class="btn btn-primary">Nuevo Miembro</a> --%>
        </div>

        <c:if test="${not empty miembros}">
            <table class="table table-striped">
                <thead>
                    <tr>
                        <th>Nombre y Apellidos</th>
                        <th>DNI/NIF</th>
                        <th>Email</th>
                        <th>Acciones</th>
                    </tr>
                </thead>
                <tbody>
                    <%-- Iteramos sobre la lista "miembros" que nos envió el controlador --%>
                    <c:forEach items="${miembros}" var="miembro">
                        <tr>
                            <%-- Usamos las propiedades del objeto Miembro --%>
                            <td><c:out value="${miembro.nombreApellidos}"/></td>
                            <td><c:out value="${miembro.dniNif}"/></td>
                            <td><c:out value="${miembro.email}"/></td>
                            <td>
                                <%-- Opcional: Botones de acción para cada miembro --%>
                                <%-- <a href="${pageContext.request.contextPath}/miembros/view/${miembro.id}" class="btn btn-info btn-sm">Ver</a> --%>
                            </td>
                        </tr>
                    </c:forEach>
                </tbody>
            </table>
        </c:if>

        <c:if test="${empty miembros}">
            <div class="alert alert-info">No hay miembros registrados.</div>
        </c:if>

        <a href="${pageContext.request.contextPath}/" class="btn btn-secondary mt-3">Volver al Inicio</a>
    </div>
    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>