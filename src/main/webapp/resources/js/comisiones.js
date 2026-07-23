(function () {
    const contextPath = document.body ? (document.body.dataset.contextPath || '') : '';

    function crearElemento(tag, text, className) {
        const element = document.createElement(tag);
        if (className) {
            element.className = className;
        }
        if (text !== undefined && text !== null) {
            element.textContent = text;
        }
        return element;
    }

    function limpiarNodo(node) {
        if (node) {
            node.replaceChildren();
        }
    }

    function validarDni(dni) {
        const normalizado = String(dni || '').trim().toUpperCase();
        return /^[0-9]{8}[A-Z]$/.test(normalizado) || /^[XYZ][0-9]{7}[A-Z]$/.test(normalizado);
    }

    function obtenerEtiquetaCargo(valor) {
        const option = document.querySelector(
            '#cargoLDAP option[value="' + valor + '"], #miembroRol option[value="' + valor + '"], #cargo option[value="' + valor + '"]'
        );
        return option ? option.textContent : valor;
    }

    function seleccionarCargoPorDefecto(select) {
        if (!select) {
            return;
        }
        if (select.querySelector('option[value="PARTICIPANTE"]')) {
            select.value = 'PARTICIPANTE';
        } else if (select.options.length > 0) {
            select.selectedIndex = 0;
        }
    }

    async function buscarUsuarioLdap(dni) {
        const response = await fetch(contextPath + '/ldapLookup?dni=' + encodeURIComponent(dni), {
            method: 'GET',
            credentials: 'same-origin',
            headers: {
                Accept: 'application/json',
                'Content-Type': 'application/json; charset=UTF-8'
            }
        });

        if (!response.ok) {
            const message = await response.text();
            throw new Error(message || ('HTTP ' + response.status));
        }

        return response.json();
    }

    function initComisionForm() {
        const form = document.getElementById('formComision');
        if (!form) {
            return;
        }

        const areaSelect = document.getElementById('area');
        const tipoSelect = document.getElementById('tipo');
        const divTipo = document.getElementById('divTipo');
        const divSeleccion = document.getElementById('divSeleccion');
        const divMiembros = document.getElementById('divMiembros');
        const selectExistente = document.getElementById('comisionExistente');
        const miembrosJson = document.getElementById('miembrosJSON');
        const listaMiembros = document.getElementById('listaMiembros');
        const resultadoLdap = document.getElementById('resultadoLDAP');
        const cargoLdap = document.getElementById('cargoLDAP');
        const cargoManual = document.getElementById('miembroRol');
        const miembros = [];

        function renderSinMiembros() {
            const row = document.createElement('tr');
            row.id = 'sinMiembros';
            const cell = crearElemento('td', 'No hay miembros agregados', 'text-center text-muted');
            cell.colSpan = 4;
            row.appendChild(cell);
            listaMiembros.appendChild(row);
        }

        function actualizarCampoJson() {
            miembrosJson.value = JSON.stringify(miembros);
        }

        function actualizarTablaMiembros() {
            limpiarNodo(listaMiembros);

            if (miembros.length === 0) {
                renderSinMiembros();
                actualizarCampoJson();
                return;
            }

            miembros.forEach((miembro, index) => {
                const row = document.createElement('tr');
                row.appendChild(crearElemento('td', miembro.dni));
                row.appendChild(crearElemento('td', miembro.nombre));

                const cargoCell = document.createElement('td');
                cargoCell.appendChild(crearElemento('span', obtenerEtiquetaCargo(miembro.rol), 'badge bg-primary'));
                row.appendChild(cargoCell);

                const actionCell = document.createElement('td');
                const button = crearElemento('button', '🗑️ Eliminar', 'btn btn-sm btn-danger');
                button.type = 'button';
                button.addEventListener('click', function () {
                    if (window.confirm('¿Seguro que desea eliminar este miembro?')) {
                        miembros.splice(index, 1);
                        actualizarTablaMiembros();
                    }
                });
                actionCell.appendChild(button);
                row.appendChild(actionCell);
                listaMiembros.appendChild(row);
            });

            actualizarCampoJson();
        }

        function agregarMiembro(dni, nombre, rol, email) {
            const miembro = {
                dni: String(dni || '').trim(),
                nombre: String(nombre || '').trim(),
                rol: String(rol || 'PARTICIPANTE').trim(),
                email: String(email || '').trim()
            };

            if (!miembro.dni || !miembro.nombre) {
                window.alert('Error: DNI y nombre son obligatorios');
                return;
            }

            if (miembros.some(function (actual) { return actual.dni === miembro.dni; })) {
                window.alert('Este miembro ya está en la lista y no se puede agregar dos veces.');
                return;
            }

            miembros.push(miembro);
            actualizarTablaMiembros();
        }

        function cargarComisionesExistentes(area, tipo) {
            limpiarNodo(selectExistente);
            selectExistente.appendChild(new Option('-- Seleccione --', ''));

            return fetch(contextPath + '/comisiones/existentes?area=' + encodeURIComponent(area) + '&tipo=' + encodeURIComponent(tipo))
                .then(function (response) { return response.json(); })
                .then(function (data) {
                    if (data.error) {
                        window.alert(data.error);
                        return;
                    }

                    if (data.length === 0) {
                        const option = new Option('(No hay registros)', '');
                        option.disabled = true;
                        selectExistente.appendChild(option);
                        return;
                    }

                    data.forEach(function (item) {
                        selectExistente.appendChild(new Option(item.nombre, item.id));
                    });
                })
                .catch(function (error) {
                    console.error('Error al cargar comisiones existentes', error);
                    window.alert('Error al cargar las opciones existentes');
                });
        }

        areaSelect.addEventListener('change', function () {
            if (areaSelect.value) {
                divTipo.classList.remove('hidden');
                tipoSelect.required = true;
            } else {
                divTipo.classList.add('hidden');
                tipoSelect.required = false;
                divSeleccion.classList.add('hidden');
                divMiembros.classList.add('hidden');
            }
            tipoSelect.value = '';
        });

        tipoSelect.addEventListener('change', function () {
            if (areaSelect.value && tipoSelect.value) {
                divSeleccion.classList.remove('hidden');
                divMiembros.classList.remove('hidden');
                cargarComisionesExistentes(areaSelect.value, tipoSelect.value);
            } else {
                divSeleccion.classList.add('hidden');
                divMiembros.classList.add('hidden');
            }
        });

        document.querySelectorAll('input[name="opcionCreacion"]').forEach(function (radio) {
            radio.addEventListener('change', function () {
                const existente = document.getElementById('divExistentes');
                const nueva = document.getElementById('divNueva');
                const nombre = document.getElementById('nombre');
                const fechaConstitucion = document.getElementById('fechaConstitucion');

                if (radio.value === 'existente' && radio.checked) {
                    existente.classList.remove('hidden');
                    nueva.classList.add('hidden');
                    selectExistente.required = true;
                    nombre.required = false;
                    fechaConstitucion.required = false;
                } else if (radio.checked) {
                    existente.classList.add('hidden');
                    nueva.classList.remove('hidden');
                    selectExistente.required = false;
                    nombre.required = true;
                    fechaConstitucion.required = true;
                }
            });
        });

        document.getElementById('btnBuscarLDAP').addEventListener('click', function () {
            const dni = document.getElementById('dniBusqueda').value.trim().toUpperCase();
            if (!dni) {
                window.alert('Por favor, introduce un DNI');
                return;
            }

            buscarUsuarioLdap(dni)
                .then(function (data) {
                    if (!data.found) {
                        resultadoLdap.classList.add('hidden');
                        window.alert('Usuario no encontrado en LDAP. Puede agregarlo manualmente.');
                        return;
                    }

                    document.getElementById('ldapNombre').textContent = data.nombreApellidos || '';
                    document.getElementById('ldapEmail').textContent = data.email || 'No disponible';
                    document.getElementById('ldapDepartamento').textContent = data.department || 'No disponible';
                    resultadoLdap.dataset.dni = dni;
                    resultadoLdap.dataset.nombre = data.nombreApellidos || '';
                    resultadoLdap.dataset.email = data.email || '';
                    resultadoLdap.classList.remove('hidden');
                    seleccionarCargoPorDefecto(cargoLdap);
                })
                .catch(function (error) {
                    console.error('Error al consultar LDAP', error);
                    window.alert('Error al consultar el LDAP: ' + error.message);
                });
        });

        document.getElementById('btnAgregarDesdeLDAP').addEventListener('click', function () {
            if (!resultadoLdap.dataset.dni || !resultadoLdap.dataset.nombre) {
                window.alert('No hay datos disponibles para agregar. Intente buscar de nuevo.');
                return;
            }

            agregarMiembro(
                resultadoLdap.dataset.dni,
                resultadoLdap.dataset.nombre,
                cargoLdap.value,
                resultadoLdap.dataset.email || ''
            );

            document.getElementById('dniBusqueda').value = '';
            seleccionarCargoPorDefecto(cargoLdap);
            resultadoLdap.classList.add('hidden');
        });

        document.getElementById('btnAgregarMiembro').addEventListener('click', function () {
            const dni = document.getElementById('miembroDNI').value.trim().toUpperCase();
            const nombre = document.getElementById('miembroNombre').value.trim();

            if (!dni || !nombre) {
                window.alert('Por favor, complete DNI y Nombre');
                return;
            }

            agregarMiembro(dni, nombre, cargoManual.value, '');
            document.getElementById('miembroDNI').value = '';
            document.getElementById('miembroNombre').value = '';
            seleccionarCargoPorDefecto(cargoManual);
        });

        form.addEventListener('submit', function (event) {
            const opcionSeleccionada = document.querySelector('input[name="opcionCreacion"]:checked');
            const opcion = opcionSeleccionada ? opcionSeleccionada.value : '';

            if (opcion === 'existente') {
                if (!selectExistente.value) {
                    event.preventDefault();
                    window.alert('Por favor, seleccione una opción existente');
                    return;
                }
            } else {
                const nombre = document.getElementById('nombre').value.trim();
                const fechaConstitucion = document.getElementById('fechaConstitucion').value;
                if (!nombre || !fechaConstitucion) {
                    event.preventDefault();
                    window.alert('Por favor, complete nombre y fecha de constitución');
                    return;
                }
            }

            if (miembros.length === 0 && !window.confirm('No ha agregado ningún miembro. ¿Desea continuar?')) {
                event.preventDefault();
            }
        });

        seleccionarCargoPorDefecto(cargoLdap);
        seleccionarCargoPorDefecto(cargoManual);
        actualizarTablaMiembros();
    }

    function initAddMemberForm() {
        const form = document.getElementById('addMemberForm');
        if (!form) {
            return;
        }

        const dniInput = document.getElementById('dni');
        const button = document.getElementById('buscarDniBtn');
        const buttonText = document.getElementById('btnText');
        const spinner = document.getElementById('btnSpinner');
        const debugInfo = document.getElementById('debugInfo');
        const messageContainer = document.getElementById('ldapMessage');
        const fechaIncorporacion = document.getElementById('fechaIncorporacion');

        function mostrarMensaje(mensaje, tipo) {
            limpiarNodo(messageContainer);
            const message = crearElemento('div', mensaje, 'ldap-' + tipo);
            messageContainer.appendChild(message);
            if (tipo === 'info') {
                window.setTimeout(function () {
                    limpiarNodo(messageContainer);
                }, 5000);
            }
        }

        function mostrarDebug(mensaje) {
            debugInfo.textContent = mensaje;
        }

        function restaurarBoton() {
            button.disabled = false;
            buttonText.classList.remove('d-none');
            spinner.classList.add('d-none');
        }

        dniInput.addEventListener('input', function () {
            dniInput.value = dniInput.value.toUpperCase();
        });

        button.addEventListener('click', function () {
            const dni = dniInput.value.trim().toUpperCase();
            limpiarNodo(messageContainer);
            debugInfo.textContent = '';

            if (!dni) {
                mostrarMensaje('Por favor, introduce un DNI antes de buscar', 'error');
                dniInput.focus();
                return;
            }

            if (!validarDni(dni)) {
                mostrarMensaje('Formato de DNI/NIE inválido. Debe ser 12345678A o X1234567A', 'error');
                dniInput.focus();
                return;
            }

            button.disabled = true;
            buttonText.classList.add('d-none');
            spinner.classList.remove('d-none');
            mostrarDebug('Consultando usuario en LDAP...');

            buscarUsuarioLdap(dni)
                .then(function (json) {
                    restaurarBoton();

                    if (json.error) {
                        mostrarMensaje('Error: ' + json.error, 'error');
                        mostrarDebug('Error del servidor: ' + json.error);
                        return;
                    }

                    if (!json.found) {
                        mostrarMensaje('No se encontró la persona en LDAP. Puedes completar los datos manualmente.', 'error');
                        mostrarDebug('Usuario no encontrado en LDAP');
                        return;
                    }

                    if (json.nombreApellidos) {
                        document.getElementById('nombreApellidos').value = json.nombreApellidos;
                    }
                    if (json.email) {
                        document.getElementById('email').value = json.email;
                    }

                    mostrarMensaje(
                        '✓ Usuario encontrado: ' + json.nombreApellidos + (json.department ? ' (' + json.department + ')' : ''),
                        'info'
                    );
                    mostrarDebug('Búsqueda LDAP completada correctamente');

                    [document.getElementById('nombreApellidos'), document.getElementById('email')].forEach(function (input) {
                        if (input.value) {
                            input.style.backgroundColor = '#d4edda';
                            window.setTimeout(function () {
                                input.style.backgroundColor = '';
                            }, 2000);
                        }
                    });
                })
                .catch(function (error) {
                    restaurarBoton();
                    console.error('Error en búsqueda LDAP', error);
                    mostrarMensaje('Error al consultar el LDAP: ' + error.message, 'error');
                    mostrarDebug('Error: ' + error.message);
                });
        });

        if (fechaIncorporacion) {
            fechaIncorporacion.value = new Date().toISOString().split('T')[0];
        }
    }

    window.toggleHistorial = function (id) {
        const fila = document.getElementById(id);
        if (!fila) {
            return;
        }

        fila.classList.toggle('oculto');
        const link = document.querySelector('[data-target="' + id + '"]');
        if (!link) {
            return;
        }

        const icono = link.querySelector('small');
        if (icono) {
            icono.textContent = fila.classList.contains('oculto') ? '[+]' : '[-]';
        }
    };

    function initBuscarPorDni() {
        const filas = document.querySelectorAll('.fila-comision');
        if (filas.length === 0) {
            return;
        }

        const checkActivas = document.getElementById('mostrarActivas');
        const checkFinalizadas = document.getElementById('mostrarFinalizadas');
        const totalActivas = document.getElementById('totalActivas');
        const totalFinalizadas = document.getElementById('totalFinalizadas');
        const totalVisibles = document.getElementById('totalVisibles');

        function actualizarContadores() {
            let activas = 0;
            let finalizadas = 0;
            let visibles = 0;

            filas.forEach(function (fila) {
                const estado = fila.getAttribute('data-estado');
                const visible = !fila.classList.contains('oculto');
                if (estado === 'activa') {
                    activas += 1;
                } else if (estado === 'finalizada') {
                    finalizadas += 1;
                }
                if (visible) {
                    visibles += 1;
                }
            });

            if (totalActivas) {
                totalActivas.textContent = activas;
            }
            if (totalFinalizadas) {
                totalFinalizadas.textContent = finalizadas;
            }
            if (totalVisibles) {
                totalVisibles.textContent = visibles;
            }
        }

        function filtrar() {
            const mostrarActivas = !checkActivas || checkActivas.checked;
            const mostrarFinalizadas = !checkFinalizadas || checkFinalizadas.checked;

            filas.forEach(function (fila) {
                const estado = fila.getAttribute('data-estado');
                const mostrar = (estado === 'activa' && mostrarActivas) || (estado === 'finalizada' && mostrarFinalizadas);
                fila.classList.toggle('oculto', !mostrar);
            });

            actualizarContadores();
        }

        if (checkActivas) {
            checkActivas.addEventListener('change', filtrar);
        }
        if (checkFinalizadas) {
            checkFinalizadas.addEventListener('change', filtrar);
        }

        actualizarContadores();
    }

    document.addEventListener('DOMContentLoaded', function () {
        initComisionForm();
        initAddMemberForm();
        initBuscarPorDni();
    });
})();
