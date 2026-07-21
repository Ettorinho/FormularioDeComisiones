<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<c:set var="pageTitle" value="Añadir Miembro" />
<c:set var="headerSubtitle" value="Gobierno de Aragón" />
<%@ include file="/WEB-INF/views/common/header.jspf" %>

    <div class="container">
        <div class="card">
            <div class="card-header">
                <h2>Añadir Miembro a <c:out value="${comision.nombre}"/></h2>
            </div>
            <div class="card-body">
                <c:if test="${not empty error}">
                    <div class="alert alert-danger"><c:out value="${error}"/></div>
                </c:if>
                
                <form id="addMemberForm" action="${pageContext.request.contextPath}/comisiones/addMember/${comision.id}" method="POST">
                    
                    <div class="form-group mb-3">
                        <label for="dni">DNI/NIF *</label>
                        <div class="input-group">
                            <input type="text" class="form-control" id="dni" name="dni" 
                                   placeholder="12345678A" 
                                   maxlength="9"
                                   required />
                            <button type="button" id="buscarDniBtn" class="btn btn-outline-secondary">
                                <span id="btnText">🔍 Buscar</span>
                                <span id="btnSpinner" class="spinner-border spinner-border-sm d-none" role="status"></span>
                            </button>
                        </div>
                        <small id="dniHelp" class="form-text text-muted">
                            Introduce el DNI/NIE y pulsa "Buscar" para rellenar automáticamente los datos desde LDAP.
                        </small>
                        <div id="ldapMessage"></div>
                        <div id="debugInfo" class="debug-info"></div>
                    </div>
                    
                    <div class="form-group mb-3">
                        <label for="nombreApellidos">Nombre y apellidos *</label>
                        <input type="text" class="form-control" id="nombreApellidos" name="nombreApellidos" 
                               placeholder="Nombre completo" required />
                    </div>
                    
                    <div class="form-group mb-3">
                        <label for="email">Correo electrónico *</label>
                        <input type="email" class="form-control" id="email" name="email" 
                               placeholder="usuario@example.com" required />
                    </div>
                    
                    <div class="form-group mb-3">
                        <label for="fechaIncorporacion">Fecha de incorporación *</label>
                        <input type="date" class="form-control" id="fechaIncorporacion" name="fechaIncorporacion" required />
                    </div>
                    
                    <div class="form-group mb-3">
                        <label for="cargo">Cargo *</label>
                        <select class="form-control" id="cargo" name="cargo" required>
                            <option value="">-- Selecciona un cargo --</option>
                            <c:forEach var="cargo" items="${cargos}">
                                <option value="${cargo}"><c:out value="${cargo.descripcion}"/></option>
                            </c:forEach>
                        </select>
                    </div>
                    
                    <div class="mt-4">
                        <button type="submit" class="btn btn-primary">
                            ✅ Añadir miembro
                        </button>
                        <a href="${pageContext.request.contextPath}/comisiones/view/${comision.id}" class="btn btn-secondary">
                            ❌ Cancelar
                        </a>
                    </div>
                </form>
            </div>
        </div>
    </div>

    <script src="${pageContext.request.contextPath}/resources/js/comisiones.js"></script>
<%@ include file="/WEB-INF/views/common/footer.jspf" %>
