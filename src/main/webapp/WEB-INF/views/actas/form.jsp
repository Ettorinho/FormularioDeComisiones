<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Nueva Acta de Reunión - Gobierno de Aragón</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
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
                        <i class="bi bi-file-earmark-text"></i>
                        Sistema de Gestión de Comisiones
                    </h1>
                    <p class="mb-0 mt-1" style="opacity: 0.9;">Crear Nueva Acta de Reunión</p>
                </div>
                <div class="col-md-4 text-end">
                    <fmt:formatDate value="<%= new java.util.Date() %>" pattern="dd/MM/yyyy" />
                </div>
            </div>
        </div>
    </header>

    <!-- Navbar -->
    <nav class="navbar navbar-expand-lg navbar-custom no-print">
        <div class="container">
            <button class="navbar-toggler" type="button" data-bs-toggle="collapse" data-bs-target="#navbarNav">
                <span class="navbar-toggler-icon"></span>
            </button>
            <div class="collapse navbar-collapse" id="navbarNav">
                <ul class="navbar-nav">
                    <li class="nav-item">
                        <a class="nav-link" href="${pageContext.request.contextPath}/">
                            <i class="bi bi-house-door"></i> Inicio
                        </a>
                    </li>
                    <li class="nav-item">
                        <a class="nav-link" href="${pageContext.request.contextPath}/comisiones">
                            <i class="bi bi-people"></i> Comisiones
                        </a>
                    </li>
                    <li class="nav-item">
                        <a class="nav-link active" href="${pageContext.request.contextPath}/actas/new">
                            <i class="bi bi-file-earmark-plus"></i> Nueva Acta
                        </a>
                    </li>
                    <li class="nav-item">
                        <a class="nav-link" href="${pageContext.request.contextPath}/comisiones/buscarPorDni">
                            <i class="bi bi-search"></i> Buscar Miembros
                        </a>
                    </li>
                </ul>
            </div>
        </div>
    </nav>

    <!-- Contenido Principal -->
    <div class="container mt-4 mb-5">
        
        <!-- Breadcrumb -->
        <nav aria-label="breadcrumb">
            <ol class="breadcrumb">
                <li class="breadcrumb-item"><a href="${pageContext.request.contextPath}/">Inicio</a></li>
                <li class="breadcrumb-item active">Nueva Acta</li>
            </ol>
        </nav>

        <div class="card shadow-sm">
            <div class="card-header bg-primary text-white">
                <h3 class="mb-0">
                    <i class="bi bi-file-earmark-plus"></i> Crear Acta de Reunión
                </h3>
            </div>
            <div class="card-body">
                
                <!-- Formulario con enctype para archivos -->
                <form id="formActa" method="post" action="${pageContext.request.contextPath}/actas/save" enctype="multipart/form-data">
                    
                    <!-- Selección de Comisión -->
                    <div class="mb-3">
                        <label for="comisionId" class="form-label">
                            <i class="bi bi-building"></i> Comisión / Grupo <span class="text-danger">*</span>
                        </label>
                        <select class="form-select" id="comisionId" name="comisionId" required>
                            <option value="">Seleccione una comisión...</option>
                            <c:forEach var="comision" items="${comisiones}">
                                <option value="${comision.id}">
                                    ${comision.nombre}
                                    <c:if test="${not empty comision.area}">
                                        - ${comision.area == 'ATENCION_ESPECIALIZADA' ? 'Atención Especializada' : 'Atención Primaria'}
                                    </c:if>
                                </option>
                            </c:forEach>
                        </select>
                    </div>

                    <!-- Fecha de Reunión -->
                    <div class="mb-3">
                        <label for="fechaReunion" class="form-label">
                            <i class="bi bi-calendar-event"></i> Fecha de la Reunión <span class="text-danger">*</span>
                        </label>
                        <input type="date" class="form-control" id="fechaReunion" name="fechaReunion" required>
                    </div>

                    <!-- Lista de Miembros (carga dinámica) -->
                    <div class="mb-4">
                        <h5 class="border-bottom pb-2 mb-3">
                            <i class="bi bi-people-fill"></i> Registro de Asistencia <span class="text-danger">*</span>
                        </h5>
                        <div id="miembrosContainer">
                            <div class="alert alert-info">
                                <i class="bi bi-info-circle"></i>
                                Seleccione una comisión para cargar los miembros
                            </div>
                        </div>
                    </div>

                    <!-- Observaciones -->
                    <div class="mb-3">
                        <label for="observaciones" class="form-label">
                            <i class="bi bi-journal-text"></i> Observaciones / Notas de la Reunión
                        </label>
                        <textarea class="form-control" id="observaciones" name="observaciones" rows="4" 
                                  placeholder="Escriba aquí las observaciones, acuerdos tomados, temas tratados, etc."></textarea>
                        <small class="text-muted">Campo opcional</small>
                    </div>

                    <!-- NUEVO: Campo para adjuntar PDF -->
                    <div class="mb-4">
                        <label for="pdfFile" class="form-label">
                            <i class="bi bi-file-pdf text-danger"></i> Adjuntar documento PDF (opcional)
                        </label>
                        <input class="form-control" type="file" id="pdfFile" name="pdfFile" accept="application/pdf" />
                        <div class="form-text">
                            <i class="bi bi-info-circle"></i> Tamaño máximo: 5MB. Solo archivos PDF (.pdf)
                        </div>
                        <div id="pdfInfo" class="mt-2" style="display: none;">
                            <div class="alert alert-success mb-0">
                                <i class="bi bi-file-earmark-pdf-fill"></i> 
                                <strong>Archivo seleccionado:</strong> <span id="pdfFileName"></span> 
                                (<span id="pdfFileSize"></span>)
                            </div>
                        </div>
                    </div>

                    <!-- Botones de acción -->
                    <div class="d-flex justify-content-between">
                        <a href="${pageContext.request.contextPath}/" class="btn btn-secondary">
                            <i class="bi bi-x-circle"></i> Cancelar
                        </a>
                        <button type="submit" class="btn btn-primary" id="btnGuardar">
                            <i class="bi bi-save"></i> Guardar Acta
                        </button>
                    </div>
                </form>
            </div>
        </div>
    </div>

    <!-- Footer -->
    <footer class="bg-dark text-white py-3 mt-5">
        <div class="container text-center">
            <p class="mb-0">
                &copy; <fmt:formatDate value="<%= new java.util.Date() %>" pattern="yyyy" /> 
                Gobierno de Aragón - Departamento de Sanidad
            </p>
        </div>
    </footer>

    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
    
    <script>
        // ========================================
// FUNCIONES PARA JUSTIFICACIONES
// ========================================

function toggleJustificacion(index) {
    const noAsistioRadio = document.getElementById('radio_no_asistio_' + index);
    const justificacionRow = document.getElementById('justificacion_row_' + index);
    const justificacionTextarea = document.getElementById('justificacion_' + index);
    
    if (noAsistioRadio && noAsistioRadio.checked) {
        justificacionRow.style.display = 'table-row';
    } else {
        justificacionRow.style.display = 'none';
        if (justificacionTextarea) {
            justificacionTextarea.value = '';
        }
    }
}

function marcarTodos(asistio) {
    const radios = document.querySelectorAll(asistio ? '[id^="radio_asistio_"]' : '[id^="radio_no_asistio_"]');
    radios.forEach(radio => {
        radio.checked = true;
        const index = radio.getAttribute('data-index');
        if (index !== null) {
            toggleJustificacion(index);
        }
    });
}

function limpiarTodo() {
    const radios = document.querySelectorAll('input[type="radio"][name^="asistencia_"]');
    radios.forEach(radio => {
        radio.checked = false;
    });
    
    const justificacionRows = document.querySelectorAll('[id^="justificacion_row_"]');
    justificacionRows.forEach(row => {
        row.style.display = 'none';
    });
    
    const textareas = document.querySelectorAll('textarea[id^="justificacion_"]');
    textareas.forEach(textarea => {
        textarea.value = '';
    });
}

// ========================================
// CARGAR MIEMBROS AL SELECCIONAR COMISIÓN
// ========================================

document.getElementById('comisionId').addEventListener('change', function() {
    const comisionId = this.value;
    const container = document.getElementById('miembrosContainer');
    
    console.log('Comisión seleccionada:', comisionId);
    
    if (!comisionId) {
        container.innerHTML = `
            <div class="alert alert-info">
                <i class="bi bi-info-circle"></i>
                Seleccione una comisión para cargar los miembros
            </div>
        `;
        return;
    }
    
    // Mostrar loading
    container.innerHTML = `
        <div class="text-center py-4">
            <div class="spinner-border text-primary" role="status">
                <span class="visually-hidden">Cargando...</span>
            </div>
            <p class="mt-2">Cargando miembros de la comisión...</p>
        </div>
    `;
    
    // Construir URL
    const url = '${pageContext.request.contextPath}/actas/loadMiembros?comisionId=' + comisionId;
    console.log('URL de petición:', url);
    
    // Hacer petición AJAX
    fetch(url)
        .then(response => {
            console.log('Response status:', response.status);
            console.log('Response OK:', response.ok);
            
            if (!response.ok) {
                throw new Error('HTTP error! status: ' + response.status);
            }
            return response.text();
        })
        .then(html => {
            console.log('HTML recibido (primeros 200 chars):', html.substring(0, 200));
            console.log('Longitud del HTML:', html.length);
            
            if (html.trim().length === 0) {
                throw new Error('Respuesta vacía del servidor');
            }
            
            container.innerHTML = html;
            console.log('✅ Miembros cargados correctamente');
        })
        .catch(error => {
            console.error('❌ Error completo:', error);
            container.innerHTML = `
                <div class="alert alert-danger">
                    <i class="bi bi-exclamation-triangle"></i>
                    <strong>Error al cargar los miembros:</strong> ${error.message}
                    <br><small>Revise la consola del navegador (F12) para más detalles</small>
                </div>
            `;
        });
});

// ========================================
// VALIDACIÓN DEL FORMULARIO
// ========================================

document.getElementById('formActa').addEventListener('submit', function(e) {
    const comisionId = document.getElementById('comisionId').value;
    const fechaReunion = document.getElementById('fechaReunion').value;
    
    // Validar comisión
    if (!comisionId) {
        e.preventDefault();
        alert('Por favor, seleccione una comisión');
        return false;
    }
    
    // Validar fecha
    if (!fechaReunion) {
        e.preventDefault();
        alert('Por favor, seleccione la fecha de reunión');
        return false;
    }
    
    // Validar que haya al menos un miembro con asistencia marcada
    const radiosChecked = document.querySelectorAll('input[type="radio"][name^="asistencia_"]:checked');
    if (radiosChecked.length === 0) {
        e.preventDefault();
        alert('Por favor, marque la asistencia de al menos un miembro');
        return false;
    }
    
    // Validar archivo PDF
    const pdfFile = document.getElementById('pdfFile').files[0];
    
    if (pdfFile) {
        // Validar tamaño (5MB)
        if (pdfFile.size > 5 * 1024 * 1024) {
            e.preventDefault();
            alert('El archivo PDF es demasiado grande. Tamaño máximo: 5MB');
            return false;
        }
        
        // Validar extensión
        const fileName = pdfFile.name.toLowerCase();
        if (!fileName.endsWith('.pdf')) {
            e.preventDefault();
            alert('Solo se permiten archivos PDF (.pdf)');
            return false;
        }
        
        console.log('PDF válido:', fileName, '-', (pdfFile.size / 1024).toFixed(2), 'KB');
    }
    
    // Confirmar envío
    const totalMiembros = document.querySelectorAll('input[name="miembroId"]').length;
    const totalMarcados = radiosChecked.length;
    
    if (totalMarcados < totalMiembros) {
        const confirmar = confirm(
            `Ha marcado ${totalMarcados} de ${totalMiembros} miembros.\n\n` +
            `¿Está seguro de que desea continuar?`
        );
        
        if (!confirmar) {
            e.preventDefault();
            return false;
        }
    }
    
    // Deshabilitar botón para evitar doble envío
    const btnGuardar = document.getElementById('btnGuardar');
    btnGuardar.disabled = true;
    btnGuardar.innerHTML = '<i class="bi bi-hourglass-split"></i> Guardando...';
    
    return true;
});

// ========================================
// MOSTRAR INFO DEL PDF SELECCIONADO
// ========================================

document.getElementById('pdfFile').addEventListener('change', function(e) {
    const file = e.target.files[0];
    const pdfInfo = document.getElementById('pdfInfo');
    
    if (file) {
        const fileName = file.name;
        const fileSize = (file.size / 1024).toFixed(2) + ' KB';
        
        document.getElementById('pdfFileName').textContent = fileName;
        document.getElementById('pdfFileSize').textContent = fileSize;
        pdfInfo.style.display = 'block';
        
        // Validación en tiempo real
        if (file.size > 5 * 1024 * 1024) {
            pdfInfo.className = 'mt-2';
            pdfInfo.innerHTML = `
                <div class="alert alert-danger mb-0">
                    <i class="bi bi-exclamation-triangle"></i> 
                    El archivo es demasiado grande (${fileSize}). Máximo: 5MB
                </div>
            `;
        } else if (!fileName.toLowerCase().endsWith('.pdf')) {
            pdfInfo.className = 'mt-2';
            pdfInfo.innerHTML = `
                <div class="alert alert-danger mb-0">
                    <i class="bi bi-exclamation-triangle"></i> 
                    Solo se permiten archivos PDF
                </div>
            `;
        } else {
            pdfInfo.className = 'mt-2';
            pdfInfo.innerHTML = `
                <div class="alert alert-success mb-0">
                    <i class="bi bi-file-earmark-pdf-fill"></i> 
                    <strong>Archivo seleccionado:</strong> ${fileName} (${fileSize})
                </div>
            `;
        }
    } else {
        pdfInfo.style.display = 'none';
    }
});

// ========================================
// ESTABLECER FECHA ACTUAL POR DEFECTO
// ========================================

window.addEventListener('DOMContentLoaded', function() {
    const fechaInput = document.getElementById('fechaReunion');
    const today = new Date().toISOString().split('T')[0];
    
    // Establecer fecha actual como valor
    fechaInput.value = today;
    
    // Establecer fecha máxima (no se pueden seleccionar fechas futuras)
    fechaInput.setAttribute('max', today);
});
    </script>
</body>
</html>