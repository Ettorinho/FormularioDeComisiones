<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Iniciar Sesión — Formulario de Comisiones</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.10.0/font/bootstrap-icons.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
</head>
<body>

    <!-- Cabecera corporativa -->
    <header class="header-app">
        <div class="container">
            <h1 class="h3 mb-0">
                <i class="bi bi-file-earmark-text"></i>
                Formulario de Comisiones
            </h1>
            <p class="mb-0 mt-1 header-subtitle">Gobierno de Aragón</p>
        </div>
    </header>

    <div class="container mt-5">
        <div class="row justify-content-center">
            <div class="col-md-5 col-lg-4">
                <div class="card shadow-sm">
                    <div class="card-body p-4">

                        <!-- Título del formulario -->
                        <div class="text-center mb-4">
                            <i class="bi bi-shield-lock fs-1 text-primary"></i>
                            <h2 class="h4 mt-2 mb-1">Formulario de Comisiones</h2>
                            <p class="text-muted small">Acceso con credenciales corporativas</p>
                        </div>

                        <!-- Mensaje de error (si existe) -->
                        <c:if test="${not empty requestScope.error}">
                        <div class="alert alert-danger" role="alert">
                            <i class="bi bi-exclamation-triangle-fill me-2"></i>
                            <c:out value="${requestScope.error}"/>
                        </div>
                        </c:if>

                        <!-- Formulario de login -->
                        <form method="post" action="${pageContext.request.contextPath}/login">

                            <div class="mb-3">
                                <label for="username" class="form-label">Usuario</label>
                                <div class="input-group">
                                    <span class="input-group-text"><i class="bi bi-person"></i></span>
                                    <input type="text"
                                           id="username"
                                           name="username"
                                           class="form-control"
                                           placeholder="usuario o usuario@empresa.com"
                                           autofocus
                                           required>
                                </div>
                            </div>

                            <div class="mb-4">
                                <label for="password" class="form-label">Contraseña</label>
                                <div class="input-group">
                                    <span class="input-group-text"><i class="bi bi-lock"></i></span>
                                    <input type="password"
                                           id="password"
                                           name="password"
                                           class="form-control"
                                           required>
                                </div>
                            </div>

                            <div class="d-grid">
                                <button type="submit" class="btn btn-primary">
                                    <i class="bi bi-box-arrow-in-right me-1"></i>
                                    Iniciar Sesión
                                </button>
                            </div>

                        </form>
                    </div>
                </div>

                <p class="text-center text-muted small mt-3">
                    Use sus credenciales de red corporativa (Active Directory)
                </p>
            </div>
        </div>
    </div>

</body>
</html>
