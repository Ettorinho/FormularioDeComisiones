<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<c:choose>
    <c:when test="${empty miembros}">
        <div class="alert alert-warning">
            <i class="bi bi-exclamation-triangle"></i>
            No hay miembros asignados a esta comisión
        </div>
    </c:when>
    <c:otherwise>
        <div class="d-flex justify-content-between align-items-center mb-3">
            <p class="mb-0">
                <strong>Total de miembros:</strong> ${miembros.size()}
            </p>
            <div>
                <button type="button" class="btn btn-sm btn-success me-2" onclick="marcarTodos(true)">
                    <i class="bi bi-check-all"></i> Todos asistieron
                </button>
                <button type="button" class="btn btn-sm btn-danger me-2" onclick="marcarTodos(false)">
                    <i class="bi bi-x-circle"></i> Ninguno asistió
                </button>
                <button type="button" class="btn btn-sm btn-secondary" onclick="limpiarTodo()">
                    <i class="bi bi-arrow-clockwise"></i> Limpiar
                </button>
            </div>
        </div>

        <div class="table-responsive">
            <table class="table table-hover">
                <thead class="table-light">
                    <tr>
                        <th style="width: 50%">Miembro</th>
                        <th style="width: 20%" class="text-center">Asistió</th>
                        <th style="width: 20%" class="text-center">No asistió</th>
                        <th style="width: 10%"></th>
                    </tr>
                </thead>
                <tbody>
                    <c:forEach var="miembro" items="${miembros}" varStatus="loop">
                        <tr>
                            <td>
                                <input type="hidden" name="miembroId_${loop.index}" value="${miembro.id}" />
                                <strong>${miembro.nombreApellidos}</strong>
                                <br>
                                <small class="text-muted">
                                    <i class="bi bi-card-text"></i> ${miembro.dniNif}
                                </small>
                            </td>
                            <td class="text-center">
                                <input type="radio" 
                                       class="form-check-input" 
                                       id="radio_asistio_${loop.index}" 
                                       name="asistencia_${miembro.id}" 
                                       value="ASISTIO"
                                       data-index="${loop.index}"
                                       onchange="toggleJustificacion(${loop.index})" />
                            </td>
                            <td class="text-center">
                                <input type="radio" 
                                       class="form-check-input" 
                                       id="radio_no_asistio_${loop.index}" 
                                       name="asistencia_${miembro.id}" 
                                       value="NO_ASISTIO"
                                       data-index="${loop.index}"
                                       onchange="toggleJustificacion(${loop.index})" />
                            </td>
                            <td></td>
                        </tr>
                        <tr id="justificacion_row_${loop.index}" style="display: none;">
                            <td colspan="4" class="bg-light">
                                <div class="ms-4">
                                    <label for="justificacion_${loop.index}" class="form-label">
                                        <i class="bi bi-chat-left-text"></i> Justificación (opcional):
                                    </label>
                                    <textarea class="form-control" 
                                              id="justificacion_${loop.index}" 
                                              name="justificacion_${miembro.id}" 
                                              rows="2" 
                                              placeholder="Ej: Visita médica, permiso laboral, etc."></textarea>
                                </div>
                            </td>
                        </tr>
                    </c:forEach>
                </tbody>
            </table>
        </div>
    </c:otherwise>
</c:choose>