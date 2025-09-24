<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Añadir Miembro</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/resources/css/style.css">
</head>
<body>
    <div class="container">
        <div class="card">
            <div class="card-header">
                <h2>Añadir Miembro a ${comision.nombre}</h2>
            </div>
            <div class="card-body">
                <c:if test="${not empty error}">
                    <div class="alert alert-danger">${error}</div>
                </c:if>
                
                <form action="${pageContext.request.contextPath}/comisiones/addMember/${comision.id}" method="POST">
                    <div class="form-group mb-3">
                        <label for="nombreApellidos">Nombre y apellidos *</label>
                        <input type="text" class="form-control" id="nombreApellidos" name="nombreApellidos" required>
                    </div>
                    
                    <div class="form-group mb-3">
                        <label for="dni">DNI/NIF *</label>
                        <input type="text" class="form-control" id="dni" name="dni" required>
                    </div>
                    
                    <div class="form-group mb-3">
                        <label for="email">Correo electrónico *</label>
                        <input type="email" class="form-control" id="email" name="email" required>
                    </div>
                    
                    <div class="form-group mb-3">
                        <label for="fechaIncorporacion">Fecha de incorporación *</label>
                        <input type="date" class="form-control" id="fechaIncorporacion" name="fechaIncorporacion" required>
                    </div>               
                    <div class="form-group mb-3">
                        <label for="cargo">Cargo *</label>
                        <select class="form-control" id="cargo" name="cargo" required>
                            <option value="REFERENTE">REFERENTE</option>
                            <option value="RESPONSABLE">RESPONSABLE</option>
                            <option value="PRESIDENTE">PRESIDENTE</option>
                            <option value="MIEMBRO">MIEMBRO</option>
                            <option value="SECRETARIO">SECRETARIO</option>
                        </select>
                        <small class="form-text text-muted">
                           <%-- REFERENTE/RESPONSABLE/PRESIDENTE para responsables o 
                            MIEMBRO para miembros colaboradores y secretarios. --%>
                        </small>
                    </div>
                    
                    <div class="mt-3">
                        <button type="submit" class="btn btn-primary">Añadir miembro</button>
                        <a href="${pageContext.request.contextPath}/comisiones/view/${comision.id}" class="btn btn-secondary">Cancelar</a>
                    </div>
                </form>
            </div>
        </div>
    </div>
    
    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>