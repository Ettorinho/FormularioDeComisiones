(function () {
    const contextPath = document.body ? (document.body.dataset.contextPath || '') : '';

    function clearNode(node) {
        if (node) {
            node.replaceChildren();
        }
    }

    function createElement(tag, text, className) {
        const element = document.createElement(tag);
        if (className) {
            element.className = className;
        }
        if (text !== undefined && text !== null) {
            element.textContent = text;
        }
        return element;
    }

    window.toggleJustificacion = function (index) {
        const noAsistioRadio = document.getElementById('radio_no_asistio_' + index);
        const justificacionRow = document.getElementById('justificacion_row_' + index);
        const justificacionTextarea = document.getElementById('justificacion_' + index);

        if (!justificacionRow) {
            return;
        }

        if (noAsistioRadio && noAsistioRadio.checked) {
            justificacionRow.style.display = 'table-row';
        } else {
            justificacionRow.style.display = 'none';
            if (justificacionTextarea) {
                justificacionTextarea.value = '';
            }
        }
    };

    window.marcarTodos = function (asistio) {
        const selector = asistio ? '[id^="radio_asistio_"]' : '[id^="radio_no_asistio_"]';
        document.querySelectorAll(selector).forEach(function (radio) {
            radio.checked = true;
            const index = radio.getAttribute('data-index');
            if (index !== null) {
                window.toggleJustificacion(index);
            }
        });
    };

    window.limpiarTodo = function () {
        document.querySelectorAll('input[type="radio"][name^="asistencia_"]').forEach(function (radio) {
            radio.checked = false;
        });
        document.querySelectorAll('[id^="justificacion_row_"]').forEach(function (row) {
            row.style.display = 'none';
        });
        document.querySelectorAll('textarea[id^="justificacion_"]').forEach(function (textarea) {
            textarea.value = '';
        });
    };

    function renderMessage(container, title, message) {
        clearNode(container);
        const alertDiv = createElement('div', null, 'alert alert-danger');
        const icon = createElement('i', null, 'bi bi-exclamation-triangle');
        const strong = createElement('strong', title);
        const small = createElement('small', 'Revise la respuesta del servidor o la consola del navegador para más detalles.');
        alertDiv.appendChild(icon);
        alertDiv.appendChild(document.createTextNode(' '));
        alertDiv.appendChild(strong);
        alertDiv.appendChild(document.createTextNode(' '));
        alertDiv.appendChild(document.createTextNode(message));
        alertDiv.appendChild(document.createElement('br'));
        alertDiv.appendChild(small);
        container.appendChild(alertDiv);
    }

    function renderLoading(container) {
        clearNode(container);
        const wrapper = createElement('div', null, 'text-center py-4');
        const spinner = createElement('div', null, 'spinner-border text-primary');
        spinner.setAttribute('role', 'status');
        spinner.appendChild(createElement('span', 'Cargando...', 'visually-hidden'));
        wrapper.appendChild(spinner);
        wrapper.appendChild(createElement('p', 'Cargando miembros de la comisión...', 'mt-2'));
        container.appendChild(wrapper);
    }

    function renderInfo(container, message) {
        clearNode(container);
        const alert = createElement('div', null, 'alert alert-info');
        alert.appendChild(createElement('i', null, 'bi bi-info-circle'));
        alert.appendChild(document.createTextNode(' ' + message));
        container.appendChild(alert);
    }

    function initActaForm() {
        const form = document.getElementById('formActa');
        if (!form) {
            return;
        }

        const comisionSelect = document.getElementById('comisionId');
        const miembrosContainer = document.getElementById('miembrosContainer');
        const fechaInput = document.getElementById('fechaReunion');
        const pdfInput = document.getElementById('pdfFile');
        const pdfInfo = document.getElementById('pdfInfo');
        const btnGuardar = document.getElementById('btnGuardar');

        function cargarMiembros(comisionId) {
            if (!comisionId) {
                renderInfo(miembrosContainer, 'Seleccione una comisión para cargar los miembros');
                return;
            }

            renderLoading(miembrosContainer);
            fetch(contextPath + '/actas/loadMiembros?comisionId=' + encodeURIComponent(comisionId))
                .then(function (response) {
                    if (!response.ok) {
                        throw new Error('HTTP ' + response.status);
                    }
                    return response.text();
                })
                .then(function (html) {
                    if (!html.trim()) {
                        throw new Error('Respuesta vacía del servidor');
                    }
                    miembrosContainer.innerHTML = html;
                })
                .catch(function (error) {
                    console.error('Error al cargar miembros', error);
                    renderMessage(miembrosContainer, 'Error al cargar los miembros:', error.message);
                });
        }

        function actualizarPdfInfo(file) {
            if (!file) {
                pdfInfo.style.display = 'none';
                return;
            }

            const fileName = file.name;
            const fileSize = (file.size / 1024).toFixed(2) + ' KB';
            pdfInfo.style.display = 'block';
            pdfInfo.className = 'mt-2';
            clearNode(pdfInfo);

            const alertDiv = createElement('div', null, 'alert mb-0');
            const icon = createElement('i');
            alertDiv.appendChild(icon);
            alertDiv.appendChild(document.createTextNode(' '));

            if (file.size > 5 * 1024 * 1024) {
                alertDiv.classList.add('alert-danger');
                icon.className = 'bi bi-exclamation-triangle';
                alertDiv.appendChild(document.createTextNode('El archivo es demasiado grande (' + fileSize + '). Máximo: 5MB'));
            } else if (!fileName.toLowerCase().endsWith('.pdf')) {
                alertDiv.classList.add('alert-danger');
                icon.className = 'bi bi-exclamation-triangle';
                alertDiv.appendChild(document.createTextNode('Solo se permiten archivos PDF'));
            } else {
                alertDiv.classList.add('alert-success');
                icon.className = 'bi bi-file-earmark-pdf-fill';
                const strong = createElement('strong', 'Archivo seleccionado: ');
                alertDiv.appendChild(strong);
                alertDiv.appendChild(document.createTextNode(fileName + ' (' + fileSize + ')'));
            }

            pdfInfo.appendChild(alertDiv);
        }

        comisionSelect.addEventListener('change', function () {
            cargarMiembros(comisionSelect.value);
        });

        form.addEventListener('submit', function (event) {
            const comisionId = comisionSelect.value;
            const fechaReunion = fechaInput.value;
            const titulo = document.getElementById('titulo').value.trim();
            const radiosChecked = document.querySelectorAll('input[type="radio"][name^="asistencia_"]:checked');
            const pdfFile = pdfInput.files[0];

            if (!comisionId) {
                event.preventDefault();
                window.alert('Por favor, seleccione una comisión');
                return;
            }

            if (!titulo || titulo.length < 5) {
                event.preventDefault();
                window.alert('Por favor, introduzca un título para el acta (mínimo 5 caracteres)');
                return;
            }

            if (!fechaReunion) {
                event.preventDefault();
                window.alert('Por favor, seleccione la fecha de reunión');
                return;
            }

            if (radiosChecked.length === 0) {
                event.preventDefault();
                window.alert('Por favor, marque la asistencia de al menos un miembro');
                return;
            }

            if (pdfFile) {
                if (pdfFile.size > 5 * 1024 * 1024) {
                    event.preventDefault();
                    window.alert('El archivo PDF es demasiado grande. Tamaño máximo: 5MB');
                    return;
                }

                if (!pdfFile.name.toLowerCase().endsWith('.pdf')) {
                    event.preventDefault();
                    window.alert('Solo se permiten archivos PDF (.pdf)');
                    return;
                }
            }

            const totalMiembros = document.querySelectorAll('input[name="miembroId"]').length;
            if (radiosChecked.length < totalMiembros &&
                !window.confirm('Ha marcado ' + radiosChecked.length + ' de ' + totalMiembros + ' miembros.\n\n¿Está seguro de que desea continuar?')) {
                event.preventDefault();
                return;
            }

            btnGuardar.disabled = true;
            btnGuardar.replaceChildren();
            const icon = createElement('i', null, 'bi bi-hourglass-split');
            btnGuardar.appendChild(icon);
            btnGuardar.appendChild(document.createTextNode(' Guardando...'));
        });

        pdfInput.addEventListener('change', function (event) {
            actualizarPdfInfo(event.target.files[0]);
        });

        const today = new Date().toISOString().split('T')[0];
        fechaInput.value = today;
        fechaInput.setAttribute('max', today);

        if (comisionSelect.value) {
            cargarMiembros(comisionSelect.value);
        }
    }

    document.addEventListener('DOMContentLoaded', initActaForm);
})();
