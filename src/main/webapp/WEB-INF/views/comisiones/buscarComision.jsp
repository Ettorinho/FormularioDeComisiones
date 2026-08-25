<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<jsp:useBean id="now" class="java.util.Date" />
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Buscar Comisión o Grupo de Trabajo</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.min.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
</head>
<body>
    <!-- Header -->
    <header class="header-app">
        <div class="container">
            <div class="d-flex align-items-center justify-content-between">
                <div class="d-flex align-items-center gap-3">
                    <img src="${pageContext.request.contextPath}/resources/images/logo_salud.png"
                         alt="Gobierno de Aragón"
                         style="height:44px; width:auto;">
                    <h1 class="h3 mb-0 text-white">
                        <i class="bi bi-file-earmark-text"></i>
                        Gestión de Comisiones
                    </h1>
                </div>
                <div class="text-end">
                    <span class="text-white me-3 small">
                        <i class="bi bi-person-circle me-1"></i>
                        <c:out value="${sessionScope.usuarioLogueado.nombreCompleto}"/>
                    </span>
                    <a href="${pageContext.request.contextPath}/logout" class="btn btn-outline-light btn-sm">
                        <i class="bi bi-box-arrow-right me-1"></i> Cerrar sesión
                    </a>
                </div>
            </div>
        </div>
    </header>

    <div class="container mt-4">
        <h2>Buscar Comisión o Grupo de Trabajo</h2>
        <form action="${pageContext.request.contextPath}/comisiones/buscarComision" method="post" class="mb-4">
            <input type="hidden" name="csrfToken" value="${csrfToken}" />
            <div class="row g-3 align-items-end">
                <div class="col-auto">
                    <label for="nombre" class="form-label">Nombre de comisión o grupo</label>
                    <input type="text" name="nombre" id="nombre" class="form-control"
                           placeholder="Introduce nombre" required
                           value="<c:out value='${nombreBuscado != null ? nombreBuscado : ""}'/>">
                </div>
                <div class="col-auto">
                    <button type="submit" class="btn btn-primary">
                        <i class="bi bi-search"></i> Buscar
                    </button>
                </div>
            </div>
        </form>

        <c:if test="${not empty nombreBuscado}">
            <c:choose>
                <c:when test="${empty comisiones}">
                    <div class="alert alert-warning">No se encontraron comisiones o grupos con ese nombre.</div>
                </c:when>
                <c:otherwise>
                    <p class="text-muted">
                        <strong>Resultados:</strong> ${comisiones.size()} comisión(es) encontrada(s)
                    </p>
                    <table class="table table-striped">
                        <thead>
                            <tr>
                                <th>Nombre</th>
                                <th>Fecha Constitución</th>
                                <th>Estado</th>
                                <th>Acciones</th>
                            </tr>
                        </thead>
                        <tbody>
                            <c:forEach items="${comisiones}" var="comision">
                                <tr>
                                    <td><c:out value="${comision.nombre}"/></td>
                                    <td><fmt:formatDate pattern="dd/MM/yyyy" value="${comision.fechaConstitucion}" /></td>
                                    <td>
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
                                    </td>
                                    <td>
                                        <a href="${pageContext.request.contextPath}/comisiones/view/${comision.id}" class="btn btn-info btn-sm">Ver</a>
                                    </td>
                                </tr>
                            </c:forEach>
                        </tbody>
                    </table>
                </c:otherwise>
            </c:choose>
        </c:if>

        <a href="${pageContext.request.contextPath}/comisiones" class="btn btn-secondary mt-3">
            <i class="bi bi-arrow-left"></i> Volver a Comisiones
        </a>
    </div>

    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>
