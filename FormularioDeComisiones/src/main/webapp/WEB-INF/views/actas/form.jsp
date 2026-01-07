<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<! DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Crear Acta de Reunión</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
    <style>
        .miembro-row {
            border-bottom: 1px solid #dee2e6;
            padding:  10px 0;
        }
        .miembro-row:last-child {
            border-bottom: none;
        }
        .cargo-badge {
            font-size: 0.75rem;
        }
        .spinner-border-custom {
            width: 2rem;
            height: 2rem;
        }
    </style>
</head>
<body>
<div class="container mt-4">
    <div class="card">
        <div class="card-header bg-primary text-white">
            <h3 class="mb-0">📝 Crear Acta de Reunión</h3>
        </div>
        <div class="card-body">
            
            <c:if test="${not empty error}">
                <div class="alert alert-danger alert-dismissible fade show" role="alert">
                    <strong>Error: </strong> ${error}
                    <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
                </div>
            </c:if>
            
            <form action="${pageContext.request.contextPath}/actas/save" method="post" id="formActa">
                
                <!-- Selección de Comisión -->
                <div class="mb-4">
                    <label for="comisionId" class="form-label fw-bold">Comisión / Grupo *</label>
                    <select class="form-select" id="comisionId" name="comisionId" required>
                        <option value="">-- Seleccione una comisión activa --</option>
                        <c:forEach var="comision" items="${comisiones}">
                            <option value="${comision.id}">
                                ${comision. nombre}
                                <c:if test="${not empty comision.area}">
                                    - ${comision.area == 'ATENCION_ESPECIALIZADA' ? 'At. Especializada' : 'At.  Primaria'}
                                </c:if>
                                <c:if test="${not empty comision.tipo}">
                                    (${comision.tipo == 'COMISION' ? 'Comisión' : comision.tipo == 'GRUPO_TRABAJO' ? 'Grupo de Trabajo' : 'Grupo de Mejora'})
                                </c:if>
                            </option>
                        </c:forEach>
                    </select>
                    <small class="text-muted">Solo se muestran comisiones activas</small>
                </div>
                
                <!-- Fecha de Reunión -->
                <div class="mb-4">
                    <label for="fechaReunion" class="form-label fw-bold">Fecha de Reunión *</label>
                    <input type="date" class="form-control" id="fechaReunion" name="fechaReunion" required>
                </div>
                
                <!-- Lista de Miembros (se carga dinámicamente) -->
                <div class="mb-4" id="divMiembros" style="display:  none;">
                    <label class="form-label fw-bold">Asistencia de Miembros</label>
                    <div class="card">
                        <div class="card-body" id="listaMiembros">
                            <p class="text-muted text-center">Seleccione una comisión para ver los miembros</p>
                        </div>
                    </div>
                    <small class="text-muted">Marque los miembros que asistieron a la reunión</small>
                </div>
                
                <!-- Observaciones -->
                <div class="mb-4">
                    <label for="observaciones" class="form-label fw-bold">Observaciones / Notas</label>
                    <textarea class="form-control" id="observaciones" name="observaciones" 
                              rows="6" 
                              placeholder="Escriba aquí el resumen de la reunión, acuerdos tomados, temas tratados, etc."></textarea>
                    <small class="text-muted">Opcional - Puede agregar notas, resumen o puntos tratados en la reunión</small>
                </div>
                
                <!-- Botones -->
                <div class="mt-4">
                    <button type="submit" class="btn btn-success btn-lg" id="btnGuardar">
                        💾 Guardar Acta
                    </button>
                    <a href="${pageContext. request.contextPath}/" class="btn btn-secondary btn-lg">
                        ❌ Cancelar
                    </a>
                </div>
            </form>
        </div>
    </div>
</div>

<script>
// Establecer fecha actual como valor predeterminado
document.addEventListener('DOMContentLoaded', function() {
    const today = new Date().toISOString().split('T')[0];
    document.getElementById('fechaReunion').value = today;
    document.getElementById('fechaReunion').max = today;
});

// Cargar miembros al seleccionar comisión
document.getElementById('comisionId').addEventListener('change', function() {
    const comisionId = this.value;
    const divMiembros = document.getElementById('divMiembros');
    const listaMiembros = document. getElementById('listaMiembros');
    
    console.log('Comisión seleccionada:', comisionId);
    
    if (! comisionId) {
        divMiembros.style.display = 'none';
        listaMiembros.innerHTML = '<p class="text-muted text-center">Seleccione una comisión para ver los miembros</p>';
        return;
    }
    
    // Mostrar loading
    listaMiembros.innerHTML = '<div class="text-center py-4"><div class="spinner-border spinner-border-custom text-primary" role="status"><span class="visually-hidden">Cargando... </span></div><p class="mt-2 text-muted">Cargando miembros...</p></div>';
    divMiembros.style.display = 'block';
    
    // Cargar miembros via AJAX
    const url = '${pageContext.request.contextPath}/actas/loadMiembros?comisionId=' + encodeURIComponent(comisionId);
    console.log('URL de carga:', url);
    
    fetch(url)
        .then(response => {
            console.log('Response status:', response.status);
            if (!response.ok) {
                throw new Error('HTTP error ' + response.status);
            }
            return response.text();
        })
        .then(html => {
            console.log('HTML recibido, longitud:', html.length);
            listaMiembros.innerHTML = html;
        })
        .catch(error => {
            console.error('Error:', error);
            listaMiembros.innerHTML = '<div class="alert alert-danger">Error al cargar los miembros:  ' + error.message + '</div>';
        });
});

// Validación antes de enviar
document.getElementById('formActa').addEventListener('submit', function(e) {
    const comisionId = document.getElementById('comisionId').value;
    const fechaReunion = document.getElementById('fechaReunion').value;
    
    if (!comisionId || ! fechaReunion) {
        e.preventDefault();
        alert('Por favor, complete los campos obligatorios (Comisión y Fecha de Reunión)');
        return false;
    }
    
    const checkboxes = document.querySelectorAll('input[name="asistio"]:checked');
    if (checkboxes.length === 0) {
        if (! confirm('No ha marcado ninguna asistencia.  ¿Desea continuar?')) {
            e.preventDefault();
            return false;
        }
    }
    
    console.log('Formulario enviado:', {
        comisionId: comisionId,
        fechaReunion: fechaReunion,
        asistencias: checkboxes.length
    });
});
</script>

<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>
