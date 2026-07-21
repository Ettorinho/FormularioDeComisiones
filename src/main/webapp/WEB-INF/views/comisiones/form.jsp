<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<c:set var="pageTitle" value="Formulario de Comisiones" />
<c:set var="headerSubtitle" value="Gobierno de Aragón" />
<%@ include file="/WEB-INF/views/common/header.jspf" %>

    <div class="container mt-4">
        <h2 class="mb-4">📋 Formulario de Comisiones / Grupos</h2>
        
        <c:if test="${not empty error}">
            <div class="alert alert-danger alert-dismissible fade show" role="alert">
                <strong>Error: </strong> <c:out value="${error}"/>
                <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
            </div>
        </c:if>
        
        <form action="${pageContext.request.contextPath}/comisiones" method="post" id="formComision">
            
            <!-- Sección 1: Selección de Área y Tipo -->
            <div class="card card-custom">
                <div class="card-body">
                    <h5 class="section-header">1. Clasificación</h5>
                    
                    <div class="row">
                        <div class="col-md-6 mb-3">
                            <label for="area" class="form-label fw-bold">Área *</label>
                            <select class="form-select" id="area" name="area" required>
                                <option value="">-- Seleccione un área --</option>
                                <c:forEach var="area" items="${areas}">
                                    <option value="${area}"><c:out value="${area.descripcion}"/></option>
                                </c:forEach>
                            </select>
                        </div>
                        
                        <div class="col-md-6 mb-3 hidden" id="divTipo">
                            <label for="tipo" class="form-label fw-bold">Tipo *</label>
                            <select class="form-select" id="tipo" name="tipo">
                                <option value="">-- Seleccione un tipo --</option>
                                <c:forEach var="tipo" items="${tipos}">
                                    <option value="${tipo}"><c:out value="${tipo.descripcion}"/></option>
                                </c:forEach>
                            </select>
                        </div>
                    </div>
                </div>
            </div>
            
            <!-- Sección 2: Seleccionar existente o crear nueva -->
            <div class="card card-custom hidden" id="divSeleccion">
                <div class="card-body">
                    <h5 class="section-header">2. Opción</h5>
                    
                    <div class="mb-3">
                        <div class="form-check">
                            <input class="form-check-input" type="radio" name="opcionCreacion" id="opcionExistente" value="existente" checked />
                            <label class="form-check-label fw-bold" for="opcionExistente">
                                Agregar miembros a una existente
                            </label>
                        </div>
                        <div class="form-check">
                            <input class="form-check-input" type="radio" name="opcionCreacion" id="opcionNueva" value="nueva" />
                            <label class="form-check-label fw-bold" for="opcionNueva">
                                Crear nueva
                            </label>
                        </div>
                    </div>
                    
                    <!-- Seleccionar existente -->
                    <div id="divExistentes">
                        <label for="comisionExistente" class="form-label">Seleccione una opción</label>
                        <select class="form-select" id="comisionExistente" name="comisionExistente">
                            <option value="">-- Seleccione --</option>
                        </select>
                        <small class="text-muted">Seleccione para agregar nuevos miembros</small>
                    </div>
                    
                    <!-- Formulario para crear nueva -->
                    <div id="divNueva" class="hidden">
                        <div class="row">
                            <div class="col-md-12 mb-3">
                                <label for="nombre" class="form-label">Nombre *</label>
                                <input type="text" class="form-control" id="nombre" name="nombre" 
                                       placeholder="Ej: Comisión de Calidad Asistencial" />
                            </div>
                        </div>
                        <div class="row">
                            <div class="col-md-6 mb-3">
                                <label for="fechaConstitucion" class="form-label">Fecha de Constitución *</label>
                                <input type="date" class="form-control" id="fechaConstitucion" name="fechaConstitucion" />
                            </div>
                            <div class="col-md-6 mb-3">
                                <label for="fechaFin" class="form-label">Fecha de Disolución (Opcional)</label>
                                <input type="date" class="form-control" id="fechaFin" name="fechaFin" />
                            </div>
                        </div>
                    </div>
                </div>
            </div>
            
            <!-- Sección 3: Agregar Miembros -->
            <div class="card card-custom hidden" id="divMiembros">
                <div class="card-body">
                    <h5 class="section-header">3. Miembros</h5>
                    
                    <!-- Búsqueda por DNI -->
                    <div class="row mb-4">
                        <div class="col-md-9">
                            <label for="dniBusqueda" class="form-label fw-bold">Buscar en LDAP por DNI</label>
                            <input type="text" class="form-control" id="dniBusqueda" 
                                   placeholder="Ej: 12345678A" maxlength="10" />
                        </div>
                        <div class="col-md-3 d-flex align-items-end">
                            <button type="button" class="btn btn-info w-100" id="btnBuscarLDAP">
                                🔍 Buscar
                            </button>
                        </div>
                    </div>
                    
                    <!-- Resultado LDAP -->
                    <div id="resultadoLDAP" class="alert alert-success hidden" role="alert">
                        <h6 class="alert-heading">✅ Usuario encontrado en LDAP</h6>
                        <p class="mb-1"><strong>Nombre:</strong> <span id="ldapNombre"></span></p>
                        <p class="mb-1"><strong>Email:</strong> <span id="ldapEmail"></span></p>
                        <p class="mb-1"><strong>Departamento:</strong> <span id="ldapDepartamento"></span></p>
                        <hr>
                        <div class="row align-items-end">
                            <div class="col-md-6 mb-2">
                                <label for="cargoLDAP" class="form-label fw-bold">Seleccione el cargo</label>
                                <select class="form-select" id="cargoLDAP">
                                    <c:forEach var="cargo" items="${cargos}">
                                        <option value="${cargo}"><c:out value="${cargo.descripcion}"/></option>
                                    </c:forEach>
                                </select>
                            </div>
                            <div class="col-md-6 mb-2">
                                <button type="button" class="btn btn-success w-100" id="btnAgregarDesdeLDAP">
                                    ➕ Agregar este usuario
                                </button>
                            </div>
                        </div>
                    </div>
                    
                    <hr>
                    
                    <!-- Agregar manualmente -->
                    <h6 class="mb-3">O agregar manualmente</h6>
                    <div class="row">
                        <div class="col-md-3 mb-2">
                            <label for="miembroDNI" class="form-label">DNI</label>
                            <input type="text" class="form-control" id="miembroDNI" 
                                   placeholder="12345678A" maxlength="10" />
                        </div>
                        <div class="col-md-4 mb-2">
                            <label for="miembroNombre" class="form-label">Nombre y Apellidos</label>
                            <input type="text" class="form-control" id="miembroNombre" 
                                   placeholder="Juan Pérez García" />
                        </div>
                        <div class="col-md-3 mb-2">
                            <label for="miembroRol" class="form-label">Cargo</label>
                            <select class="form-select" id="miembroRol">
                                <c:forEach var="cargo" items="${cargos}">
                                    <option value="${cargo}"><c:out value="${cargo.descripcion}"/></option>
                                </c:forEach>
                            </select>
                        </div>
                        <div class="col-md-2 d-flex align-items-end mb-2">
                            <button type="button" class="btn btn-primary w-100" id="btnAgregarMiembro">
                                ➕ Agregar
                            </button>
                        </div>
                    </div>
                    
                    <hr>
                    
                    <!-- Tabla de miembros -->
                    <h6 class="mb-3">Miembros agregados</h6>
                    <div class="table-responsive">
                        <table class="table table-striped table-hover">
                            <thead class="table-light">
                                <tr>
                                    <th>DNI</th>
                                    <th>Nombre</th>
                                    <th>Cargo</th>
                                    <th>Acciones</th>
                                </tr>
                            </thead>
                            <tbody id="listaMiembros">
                                <tr id="sinMiembros">
                                    <td colspan="4" class="text-center text-muted">No hay miembros agregados</td>
                                </tr>
                            </tbody>
                        </table>
                    </div>
                    
                    <!-- Campo oculto JSON -->
                    <input type="hidden" id="miembrosJSON" name="miembrosJSON" value="[]">
                </div>
            </div>
            
            <!-- Botones de acción -->
            <div class="mt-4 mb-5">
                <button type="submit" class="btn btn-success btn-lg" id="btnGuardar">
                    💾 Guardar
                </button>
                <a href="${pageContext.request.contextPath}/comisiones/list" class="btn btn-secondary btn-lg">
                    ❌ Cancelar
                </a>
            </div>
        </form>
    </div>
    <script src="${pageContext.request.contextPath}/resources/js/comisiones.js"></script>
<%@ include file="/WEB-INF/views/common/footer.jspf" %>
