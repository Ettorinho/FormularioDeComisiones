<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Gestión de Comisiones</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
    <style>
        body {
            background-color: #f8f9fa;
        }
    </style>
</head>
<body>
    <div class="container mt-5">
        <div class="px-4 py-5 my-5 text-center">
            <img class="d-block mx-auto mb-4" src="https://getbootstrap.com/docs/5.1/assets/brand/bootstrap-logo.svg" alt="" width="72" height="57">
            <h1 class="display-5 fw-bold">Sistema de Gestión de Comisiones</h1>
            <div class="col-lg-6 mx-auto">
                <p class="lead mb-4">Bienvenido a la aplicación para la gestión de comisiones y grupos de trabajo. Utilice los botones a continuación para navegar.</p>
                <div class="d-grid gap-3">
                    <!-- Este botón te llevará a la lista de comisiones -->
                    <a href="${pageContext.request.contextPath}/comisiones" class="btn btn-primary btn-lg">Crear Comisión o Grupo de Trabajo</a>
                    <a href="${pageContext.request.contextPath}/comisiones/buscarPorDni" class="btn btn-primary btn-lg">Buscar Miembros</a>
                    <a href="${pageContext.request.contextPath}/comisiones/buscarComision" class="btn btn-primary btn-lg">Buscar Comisión o Grupo de Trabajo</a>
                    <a href="${pageContext.request.contextPath}/actas/new" class="btn btn-primary btn-lg">Crear Acta de Reunión</a>                    
                    <!-- Este botón te llevará a la lista de miembros -->
                    <%-- <a href="${pageContext.request.contextPath}/miembros" class="btn btn-outline-secondary btn-lg">Ver Miembros</a> --%>
                </div>
            </div>
        </div>
    </div>
</body>
</html>