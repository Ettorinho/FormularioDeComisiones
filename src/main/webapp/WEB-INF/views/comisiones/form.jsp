<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<! DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Formulario de Comisiones</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
    <style>
        .hidden {
            display: none;
        }
        .section-header {
            background-color: #f8f9fa;
            padding: 10px 15px;
            margin-bottom: 15px;
            border-left: 4px solid #0d6efd;
        }
        .card-custom {
            box-shadow: 0 0 10px rgba(0,0,0,0.1);
            margin-bottom: 20px;
        }
    </style>
</head>
<body>
    <div class="container mt-4">
        <h2 class="mb-4">📋 Formulario de Comisiones / Grupos</h2>
        
        <c:if test="${not empty error}">
            <div class="alert alert-danger alert-dismissible fade show" role="alert">
                <strong>Error: </strong> ${error}
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
                                <option value="ATENCION_ESPECIALIZADA">Atención Especializada</option>
                                <option value="ATENCION_PRIMARIA">Atención Primaria</option>
                                <option value="MIXTA">Mixta</option>
                            </select>
                        </div>
                        
                        <div class="col-md-6 mb-3 hidden" id="divTipo">
                            <label for="tipo" class="form-label fw-bold">Tipo *</label>
                            <select class="form-select" id="tipo" name="tipo">
                                <option value="">-- Seleccione un tipo --</option>
                                <option value="COMISION">Comisión</option>
                                <option value="GRUPO_TRABAJO">Grupo de Trabajo</option>
                                <option value="GRUPO_MEJORA">Grupo de Mejora</option>
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
                                    <option value="PARTICIPANTE">Participante</option>
                                    <option value="PRESIDENTE">Presidente</option>
                                    <option value="SECRETARIO">Secretario</option>
                                    <option value="REFERENTE">Referente</option>
                                    <option value="RESPONSABLE">Responsable</option>
                                    <option value="INVESTIGADOR_PRINCIPAL">Investigador Principal</option>
                                    <option value="INVESTIGADOR_COLABORADOR">Investigador Colaborador</option>
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
                                <option value="PARTICIPANTE">Participante</option>
                                <option value="PRESIDENTE">Presidente</option>
                                <option value="SECRETARIO">Secretario</option>
                                <option value="REFERENTE">Referente</option>
                                <option value="RESPONSABLE">Responsable</option>
                                <option value="INVESTIGADOR_PRINCIPAL">Investigador Principal</option>
                                <option value="INVESTIGADOR_COLABORADOR">Investigador Colaborador</option>
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
    
    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap. bundle.min.js"></script>
    <script>
        const contextPath = '${pageContext.request.contextPath}';
        let miembros = [];
        
        // Cambio de Área
        document.getElementById('area').addEventListener('change', function() {
            const area = this.value;
            const divTipo = document.getElementById('divTipo');
            const selectTipo = document.getElementById('tipo');
            
            if (area) {
                divTipo.classList.remove('hidden');
                selectTipo.required = true;
            } else {
                divTipo.classList.add('hidden');
                selectTipo.required = false;
                document.getElementById('divSeleccion').classList.add('hidden');
                document.getElementById('divMiembros').classList.add('hidden');
            }
            
            selectTipo.value = '';
        });
        
        // Cambio de Tipo
        document.getElementById('tipo').addEventListener('change', function() {
            const area = document.getElementById('area').value;
            const tipo = this.value;
            const divSeleccion = document.getElementById('divSeleccion');
            const selectExistente = document.getElementById('comisionExistente');
            
            if (tipo && area) {
                divSeleccion.classList.remove('hidden');
                document.getElementById('divMiembros').classList.remove('hidden');
                
                console.log('Cargando comisiones para area=' + area + ', tipo=' + tipo);
                
                fetch(contextPath + '/comisiones/existentes? area=' + area + '&tipo=' + tipo)
                    . then(response => response.json())
                    .then(data => {
                        console.log('Datos recibidos:', data);
                        selectExistente.innerHTML = '<option value="">-- Seleccione --</option>';
                        
                        if (data.error) {
                            console.error('Error:', data.error);
                            return;
                        }
                        
                        if (data.length === 0) {
                            const option = document.createElement('option');
                            option.value = '';
                            option.textContent = '(No hay registros)';
                            option.disabled = true;
                            selectExistente.appendChild(option);
                        } else {
                            data.forEach(item => {
                                const option = document.createElement('option');
                                option.value = item.id;
                                option.textContent = item.nombre;
                                selectExistente.appendChild(option);
                            });
                        }
                    })
                    .catch(error => {
                        console.error('Error:', error);
                        alert('Error al cargar las opciones existentes');
                    });
            } else {
                divSeleccion.classList.add('hidden');
                document.getElementById('divMiembros').classList.add('hidden');
            }
        });
        
        // Cambio entre Existente / Nueva
        document.querySelectorAll('input[name="opcionCreacion"]').forEach(radio => {
            radio.addEventListener('change', function() {
                const divExistentes = document.getElementById('divExistentes');
                const divNueva = document.getElementById('divNueva');
                const selectExistente = document.getElementById('comisionExistente');
                const inputNombre = document.getElementById('nombre');
                const inputFechaConst = document.getElementById('fechaConstitucion');
                
                if (this.value === 'existente') {
                    divExistentes.classList.remove('hidden');
                    divNueva.classList.add('hidden');
                    selectExistente.required = true;
                    inputNombre.required = false;
                    inputFechaConst.required = false;
                } else {
                    divExistentes.classList.add('hidden');
                    divNueva.classList.remove('hidden');
                    selectExistente.required = false;
                    inputNombre.required = true;
                    inputFechaConst. required = true;
                }
            });
        });
        
        // Buscar en LDAP
        document.getElementById('btnBuscarLDAP').addEventListener('click', function() {
            const dni = document.getElementById('dniBusqueda').value.trim().toUpperCase();
            
            if (! dni) {
                alert('Por favor, introduce un DNI');
                return;
            }
            
            console.log('Buscando DNI:', dni);
            
            fetch(contextPath + '/ldapLookup?dni=' + encodeURIComponent(dni))
                .then(response => {
                    console.log('Response status:', response.status);
                    return response.json();
                })
                .then(data => {
                    console.log('LDAP response:', data);
                    if (data.found) {
                        document.getElementById('ldapNombre').textContent = data.nombreApellidos;
                        document. getElementById('ldapEmail').textContent = data.email || 'No disponible';
                        document.getElementById('ldapDepartamento').textContent = data.department || 'No disponible';
                        document.getElementById('resultadoLDAP').classList.remove('hidden');
                        
                        const resultadoDiv = document.getElementById('resultadoLDAP');
                        resultadoDiv.dataset.dni = dni;
                        resultadoDiv.dataset.nombre = data.nombreApellidos;
                        resultadoDiv.dataset.email = data.email || '';
                        
                        // Resetear el selector de cargo a PARTICIPANTE
                        document.getElementById('cargoLDAP').value = 'PARTICIPANTE';
                        
                        console.log('Datos guardados en dataset:', {
                            dni: resultadoDiv.dataset.dni,
                            nombre: resultadoDiv.dataset.nombre,
                            email: resultadoDiv. dataset.email
                        });
                    } else {
                        alert('Usuario no encontrado en LDAP.  Puede agregarlo manualmente.');
                        document.getElementById('resultadoLDAP').classList.add('hidden');
                    }
                })
                .catch(error => {
                    console.error('Error:', error);
                    alert('Error al consultar el LDAP:  ' + error.message);
                });
        });
        
        // Agregar desde LDAP
        document.getElementById('btnAgregarDesdeLDAP').addEventListener('click', function() {
            const resultado = document.getElementById('resultadoLDAP');
            
            const dni = resultado.dataset.dni;
            const nombre = resultado.dataset. nombre;
            const email = resultado.dataset.email || '';
            const cargo = document.getElementById('cargoLDAP').value;
            
            console.log('=== Agregando desde LDAP ===');
            console.log('DNI del dataset:', dni);
            console.log('Nombre del dataset:', nombre);
            console.log('Email del dataset:', email);
            console. log('Cargo seleccionado:', cargo);
            
            if (!dni || !nombre) {
                alert('Error: No hay datos para agregar.  Intente buscar de nuevo.');
                return;
            }
            
            agregarMiembro(dni, nombre, cargo, email);
            
            // Limpiar
            document.getElementById('dniBusqueda').value = '';
            document.getElementById('cargoLDAP').value = 'PARTICIPANTE';
            resultado.classList.add('hidden');
        });
        
        // Agregar manualmente
        document.getElementById('btnAgregarMiembro').addEventListener('click', function() {
            const dni = document.getElementById('miembroDNI').value.trim().toUpperCase();
            const nombre = document.getElementById('miembroNombre').value.trim();
            const rol = document.getElementById('miembroRol').value;
            
            console.log('=== Agregando manualmente ===');
            console.log('DNI:', dni);
            console.log('Nombre:', nombre);
            console.log('Rol:', rol);
            
            if (!dni || !nombre) {
                alert('Por favor, complete DNI y Nombre');
                return;
            }
            
            agregarMiembro(dni, nombre, rol, '');
            
            document.getElementById('miembroDNI').value = '';
            document.getElementById('miembroNombre').value = '';
            document.getElementById('miembroRol').value = 'PARTICIPANTE';
        });
        
        function agregarMiembro(dni, nombre, rol, email) {
            console.log('=== agregarMiembro llamado ===');
            console.log('Parámetros recibidos: ');
            console.log('  dni:', dni, '(tipo:', typeof dni + ')');
            console.log('  nombre:', nombre, '(tipo:', typeof nombre + ')');
            console.log('  rol:', rol, '(tipo:', typeof rol + ')');
            console.log('  email:', email, '(tipo:', typeof email + ')');
            
            // Convertir a string y limpiar
            dni = String(dni || '').trim();
            nombre = String(nombre || '').trim();
            rol = String(rol || 'PARTICIPANTE').trim();  // ⭐ CAMBIADO
            email = String(email || '').trim();
            
            console.log('Valores después de limpiar: ');
            console.log('  dni:', dni);
            console.log('  nombre:', nombre);
            console.log('  rol:', rol);
            console.log('  email:', email);
            
            if (!dni || !nombre) {
                console.error('❌ Error: DNI o nombre vacíos');
                alert('Error: DNI y nombre son obligatorios');
                return;
            }
            
            // Verificar duplicados
            const existe = miembros.some(function(m) {
                return m.dni === dni;
            });
            
            if (existe) {
                alert('⚠️ Este miembro ya está en la lista.\n\n' + 
                      'DNI: ' + dni + '\n' +
                      'Nombre: ' + nombre + '\n\n' +
                      'No se puede agregar el mismo miembro dos veces a la misma comisión.');
                return;
            }
            
            // Crear objeto con propiedades explícitas
            const nuevoMiembro = {};
            nuevoMiembro. dni = dni;
            nuevoMiembro.nombre = nombre;
            nuevoMiembro.rol = rol;
            nuevoMiembro.email = email;
            
            console.log('✅ Objeto creado:', nuevoMiembro);
            console.log('   Keys:', Object.keys(nuevoMiembro));
            console.log('   nuevoMiembro. dni:', nuevoMiembro.dni);
            console.log('   nuevoMiembro.nombre:', nuevoMiembro.nombre);
            console.log('   nuevoMiembro.rol:', nuevoMiembro.rol);
            
            // Agregar al array
            miembros.push(nuevoMiembro);
            
            console.log('📋 Array de miembros:', miembros);
            console.log('   Longitud:', miembros.length);
            
            // Actualizar tabla
            actualizarTablaMiembros();
            
            // Actualizar JSON
            const jsonString = JSON.stringify(miembros);
            document.getElementById('miembrosJSON').value = jsonString;
            console.log('💾 JSON:', jsonString);
        }
        
        function actualizarTablaMiembros() {
            const tbody = document.getElementById('listaMiembros');
            
            console.log('========================================');
            console.log('🔄 ACTUALIZANDO TABLA');
            console.log('Total miembros:', miembros.length);
            console.log('Array completo:', miembros);
            
            // Limpiar completamente la tabla
            tbody.innerHTML = '';
            
            if (miembros.length === 0) {
                tbody.innerHTML = '<tr id="sinMiembros"><td colspan="4" class="text-center text-muted">No hay miembros agregados</td></tr>';
                console.log('Tabla vacía');
                return;
            }
            
            // Iterar sobre cada miembro
            for (let i = 0; i < miembros.length; i++) {
                const miembro = miembros[i];
                
                console.log('\n--- Procesando miembro', i, '---');
                console.log('Objeto:', miembro);
                console.log('Keys:', Object.keys(miembro));
                console.log('miembro.dni =', miembro.dni);
                console.log('miembro["dni"] =', miembro["dni"]);
                console.log('miembro.nombre =', miembro. nombre);
                console.log('miembro["nombre"] =', miembro["nombre"]);
                console.log('miembro.rol =', miembro.rol);
                console.log('miembro["rol"] =', miembro["rol"]);
                
                // Extraer valores de forma segura
                let dni = '';
                let nombre = '';
                let rol = '';
                
                if (miembro.dni !== undefined && miembro.dni !== null) {
                    dni = String(miembro.dni);
                }
                if (miembro.nombre !== undefined && miembro.nombre !== null) {
                    nombre = String(miembro. nombre);
                }
                if (miembro.rol !== undefined && miembro.rol !== null) {
                    rol = String(miembro.rol);
                }
                
                console.log('Valores extraídos: ');
                console.log('  dni:', dni, '(length:', dni.length + ')');
                console.log('  nombre:', nombre, '(length:', nombre.length + ')');
                console.log('  rol:', rol, '(length:', rol. length + ')');
                
                // Crear fila manualmente con innerHTML (más confiable)
                const tr = document.createElement('tr');
                
                // Método:  innerHTML directo
                tr.innerHTML = 
                    '<td>' + dni + '</td>' +
                    '<td>' + nombre + '</td>' +
                    '<td><span class="badge bg-primary">' + rol + '</span></td>' +
                    '<td><button type="button" class="btn btn-sm btn-danger" onclick="eliminarMiembro(' + i + ')">🗑️ Eliminar</button></td>';
                
                console.log('HTML generado:', tr.innerHTML);
                
                // Agregar al tbody
                tbody.appendChild(tr);
                console.log('Fila agregada al DOM');
            }
            
            console.log('\n========================================');
            console.log('✅ Tabla completada');
            console.log('Total filas en tbody:', tbody.children.length);
            console.log('innerHTML del tbody:', tbody.innerHTML);
            console.log('========================================\n');
        }
        
        function eliminarMiembro(index) {
            if (confirm('¿Seguro que desea eliminar este miembro?')) {
                console.log('Eliminando miembro en índice:', index);
                miembros.splice(index, 1);
                actualizarTablaMiembros();
                document.getElementById('miembrosJSON').value = JSON.stringify(miembros);
                console.log('Miembro eliminado.  Miembros restantes:', miembros);
            }
        }
        
        document.getElementById('formComision').addEventListener('submit', function(e) {
            const opcion = document.querySelector('input[name="opcionCreacion"]:checked').value;
            
            if (opcion === 'existente') {
                const selectExistente = document.getElementById('comisionExistente');
                if (! selectExistente.value) {
                    e.preventDefault();
                    alert('Por favor, seleccione una opción existente');
                    return;
                }
            } else {
                const nombre = document.getElementById('nombre').value. trim();
                const fechaConst = document.getElementById('fechaConstitucion').value;
                if (!nombre || !fechaConst) {
                    e.preventDefault();
                    alert('Por favor, complete nombre y fecha de constitución');
                    return;
                }
            }
            
            if (miembros.length === 0) {
                if (! confirm('No ha agregado ningún miembro. ¿Desea continuar?')) {
                    e.preventDefault();
                }
            }
        });
    </script>
</body>
</html>