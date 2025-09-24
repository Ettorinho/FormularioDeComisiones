<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html>
<head>
    <title>Nueva Comisión</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
</head>
<body>
    <div class="container mt-4">
        <h2>Crear Nueva Comisión</h2>
        <c:if test="${not empty error}">
            <div class="alert alert-danger">${error}</div>
        </c:if>
        <form action="${pageContext.request.contextPath}/comisiones" method="post">
            <div class="mb-3">
                <label for="nombre" class="form-label">Nombre de la Comisión</label>
                <input type="text" class="form-control" id="nombre" name="nombre" required>
            </div>
            <div class="mb-3">
                <label for="fechaConstitucion" class="form-label">Fecha de Constitución</label>
                <input type="date" class="form-control" id="fechaConstitucion" name="fechaConstitucion" required>
            </div>
            <div class="mb-3">
                <label for="fechaFin" class="form-label">Fecha de Disolución (opcional)</label>
                <input type="date" class="form-control" id="fechaFin" name="fechaFin">
            </div>
            <button type="submit" class="btn btn-primary">Guardar</button>
            <a href="${pageContext.request.contextPath}/comisiones" class="btn btn-secondary">Cancelar</a>
        </form>
    </div>
</body>
</html>