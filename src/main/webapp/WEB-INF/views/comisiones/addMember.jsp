<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Añadir Miembro</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/resources/css/style.css">
    <style>
        .spinner-border-sm {
            width: 1rem;
            height: 1rem;
            border-width:  0.15em;
        }
        .ldap-info {
            font-size: 0.875rem;
            color: #28a745;
            margin-top: 0.5rem;
        }
        .ldap-error {
            font-size: 0.875rem;
            color: #dc3545;
            margin-top: 0.5rem;
        }
        .debug-info {
            font-size: 0.75rem;
            color: #6c757d;
            margin-top: 0.25rem;
            font-family: monospace;
        }
    </style>
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.10.0/font/bootstrap-icons.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
</head>
<body>
    <!-- Header -->
    <header class="header-aragon no-print">
        <div class="container">
            <div class="row align-items-center">
                <div class="col-md-8">
                    <h1 class="h3 mb-0">
                        <i class="bi bi-people"></i>
                        Sistema de Gestión de Comisiones
                    </h1>
                </div>
            </div>
        </div>
    </header>
    <div class="container">
        <div class="card">
            <div class="card-header">
                <h2>Añadir Miembro a ${comision.nombre}</h2>
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
                            <option value="PARTICIPANTE">Participante</option>
                            <option value="PRESIDENTE">Presidente</option>
                            <option value="SECRETARIO">Secretario</option>
                            <option value="REFERENTE">Referente</option>
                            <option value="RESPONSABLE">Responsable</option>
                            <option value="INVESTIGADOR_PRINCIPAL">Investigador Principal</option>
                            <option value="INVESTIGADOR_COLABORADOR">Investigador Colaborador</option>
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

<script>
// Función para validar formato de DNI/NIE español
function validarDNI(dni) {
    dni = dni.toUpperCase().trim();
    const dniRegex = /^[0-9]{8}[A-Z]$/;
    const nieRegex = /^[XYZ][0-9]{7}[A-Z]$/;
    return dniRegex.test(dni) || nieRegex.test(dni);
}

// Función para mostrar mensajes
function mostrarMensaje(mensaje, tipo) {
    const messageDiv = document.getElementById('ldapMessage');
    messageDiv.innerHTML = '<div class="ldap-' + tipo + '">' + mensaje + '</div>';
    
    if (tipo === 'info') {
        setTimeout(() => {
            messageDiv. innerHTML = '';
        }, 5000);
    }
}

// Función para mostrar información de debug
function mostrarDebug(mensaje) {
    const debugDiv = document.getElementById('debugInfo');
    debugDiv.innerHTML = mensaje;
    console.log('[DEBUG]', mensaje);
}

// Normalizar DNI automáticamente
document.getElementById('dni').addEventListener('input', function(e) {
    this.value = this.value.toUpperCase();
});

// Evento del botón de búsqueda
document.getElementById('buscarDniBtn').addEventListener('click', function () {
    const dniInput = document.getElementById('dni');
    const dni = dniInput.value.trim().toUpperCase();
    
    // Limpiar mensajes previos
    document.getElementById('ldapMessage').innerHTML = '';
    document.getElementById('debugInfo').innerHTML = '';
    
    console.log('=== Iniciando búsqueda LDAP ===');
    console.log('DNI introducido:', dni);
    console.log('Longitud DNI:', dni.length);
    
    if (!dni) {
        mostrarMensaje('Por favor, introduce un DNI antes de buscar', 'error');
        dniInput.focus();
        return;
    }
    
    if (!validarDNI(dni)) {
        mostrarMensaje('Formato de DNI/NIE inválido. Debe ser 12345678A o X1234567A', 'error');
        dniInput.focus();
        return;
    }
    
    const btn = this;
    const btnText = document.getElementById('btnText');
    const btnSpinner = document.getElementById('btnSpinner');
    
    // Mostrar spinner
    btn.disabled = true;
    btnText.classList.add('d-none');
    btnSpinner.classList.remove('d-none');

    const baseUrl = '${pageContext.request.contextPath}/ldapLookup';
    const url = baseUrl + '?dni=' + encodeURIComponent(dni);
    
    console.log('URL completa:', url);
    console.log('Base URL:', baseUrl);
    console.log('DNI codificado:', encodeURIComponent(dni));
    
    mostrarDebug('Conectando a:  ' + url);

    fetch(url, {
        method: 'GET',
        credentials: 'same-origin',
        headers: {
            'Accept': 'application/json',
            'Content-Type': 'application/json; charset=UTF-8'
        }
    })
    .then(response => {
        console.log('Respuesta recibida: ');
        console.log('  Status:', response.status);
        console.log('  Status Text:', response.statusText);
        console.log('  Headers:', Array.from(response.headers.entries()));
        
        mostrarDebug('Status HTTP:  ' + response.status + ' ' + response.statusText);
        
        if (!response. ok) {
            return response.text().then(text => {
                console.error('Respuesta de error:', text);
                throw new Error('HTTP ' + response.status + ': ' + (text || response.statusText));
            });
        }
        
        return response.json();
    })
    .then(json => {
        console.log('JSON recibido:', json);
        
        // Restaurar botón
        btn.disabled = false;
        btnText.classList. remove('d-none');
        btnSpinner.classList.add('d-none');
        
        if (json.error) {
            mostrarMensaje('Error:  ' + json.error, 'error');
            mostrarDebug('Error del servidor: ' + json.error);
            return;
        }
        
        if (! json.found) {
            mostrarMensaje('No se encontró la persona en LDAP.  Puedes completar los datos manualmente.', 'error');
            mostrarDebug('Usuario no encontrado en LDAP');
            return;
        }
        
        // Rellenar campos automáticamente
        console.log('Rellenando campos con:', json);
        
        if (json.nombreApellidos) {
            document.getElementById('nombreApellidos').value = json.nombreApellidos;
            console.log('Nombre rellenado:', json.nombreApellidos);
        }
        
        if (json.email) {
            document.getElementById('email').value = json.email;
            console.log('Email rellenado:', json.email);
        }
        
        // Mostrar mensaje de éxito
        const mensaje = '✓ Usuario encontrado:  ' + json.nombreApellidos + 
                       (json.department ? ' (' + json.department + ')' : '');
        mostrarMensaje(mensaje, 'info');
        mostrarDebug('Búsqueda exitosa');
        
        // Resaltar campos rellenados
        [document.getElementById('nombreApellidos'), document.getElementById('email')].forEach(input => {
            if (input. value) {
                input.style.backgroundColor = '#d4edda';
                setTimeout(() => {
                    input. style.backgroundColor = '';
                }, 2000);
            }
        });
    })
    .catch(err => {
        console.error('ERROR en búsqueda LDAP: ');
        console.error('  Tipo:', err.name);
        console.error('  Mensaje:', err.message);
        console.error('  Stack:', err.stack);
        
        btn.disabled = false;
        btnText.classList.remove('d-none');
        btnSpinner. classList.add('d-none');
        
        mostrarMensaje('Error al consultar el LDAP:  ' + err.message, 'error');
        mostrarDebug('Error:  ' + err.message);
    });
});

// Establecer fecha actual como valor predeterminado
document.addEventListener('DOMContentLoaded', function() {
    const today = new Date().toISOString().split('T')[0];
    document.getElementById('fechaIncorporacion').value = today;
    
    console.log('Página cargada');
    console.log('Context path:', '${pageContext.request.contextPath}');
    console.log('URL base LDAP:', '${pageContext. request.contextPath}/ldapLookup');
});
</script>

    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>