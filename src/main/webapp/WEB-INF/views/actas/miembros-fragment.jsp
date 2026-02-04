<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<c:if test="${empty miembros}">
    <p class="text-warning text-center py-3">⚠️ Esta comisión no tiene miembros activos</p>
</c:if>

<c:if test="${not empty miembros}">
    <table class="table table-hover table-bordered">
        <thead class="table-light">
            <tr>
                <th style="width:25%; text-align:center;">Asistencia</th>
                <th style="width:40%;">Nombre</th>
                <th style="width:20%;">DNI</th>
                <th style="width:15%;">Cargo</th>
            </tr>
        </thead>
        <tbody>
            <c:forEach var="cm" items="${miembros}" varStatus="loop">
                <tr>
                    <td style="text-align:center;">
                        <div style="display:flex; flex-direction:column; gap:5px;">
                            <div>
                                <input type="radio" name="asistencia_${cm.miembro.id}" value="ASISTIO" id="si_${loop.index}" onchange="toggleJustificacion(${cm.miembro.id}, false)">
                                <label for="si_${loop.index}" style="margin-left:5px; cursor:pointer;">✅ Asistió</label>
                            </div>
                            <div>
                                <input type="radio" name="asistencia_${cm.miembro.id}" value="NO_ASISTIO" id="no_${loop.index}" onchange="toggleJustificacion(${cm.miembro.id}, true)">
                                <label for="no_${loop.index}" style="margin-left:5px; cursor:pointer;">❌ No asistió</label>
                            </div>
                        </div>
                        <input type="hidden" name="miembroId" value="${cm.miembro.id}">
                    </td>
                    <td><strong>${cm.miembro.nombreApellidos}</strong></td>
                    <td><small>${cm.miembro.dniNif}</small></td>
                    <td>
                        <c:choose>
                            <c:when test="${cm.cargo == 'PRESIDENTE'}"><span class="badge bg-danger">Presidente</span></c:when>
                            <c:when test="${cm.cargo == 'SECRETARIO'}"><span class="badge bg-warning text-dark">Secretario</span></c:when>
                            <c:when test="${cm.cargo == 'RESPONSABLE'}"><span class="badge bg-primary">Responsable</span></c:when>
                            <c:when test="${cm.cargo == 'REFERENTE'}"><span class="badge bg-secondary">Referente</span></c:when>
                            <c:when test="${cm.cargo == 'INVESTIGADOR_PRINCIPAL'}"><span class="badge bg-dark">Inv. Principal</span></c:when>
                            <c:when test="${cm.cargo == 'INVESTIGADOR_COLABORADOR'}"><span class="badge bg-dark">Inv. Colaborador</span></c:when>
                            <c:when test="${cm.cargo == 'PARTICIPANTE'}"><span class="badge bg-light text-dark">Participante</span></c:when>
                            <c:otherwise><span class="badge bg-secondary">${cm.cargo}</span></c:otherwise>
                        </c:choose>
                    </td>
                </tr>
                <tr id="justificacion_${cm.miembro.id}" style="display:none;">
                    <td colspan="4" style="background-color:#fff3cd; padding:15px;">
                        <label style="font-weight:bold; margin-bottom:5px;">📝 Justificación de ausencia para ${cm.miembro.nombreApellidos}:</label>
                        <textarea name="justificacion_${cm.miembro.id}" class="form-control" rows="2" placeholder="Escriba el motivo de la ausencia (ej: baja médica, permiso, vacaciones, etc.)"></textarea>
                        <small class="text-muted">Campo opcional - puede dejarse vacío si no hay justificación</small>
                    </td>
                </tr>
            </c:forEach>
        </tbody>
    </table>
    <div style="margin-top:15px; display:flex; justify-content:space-between; align-items:center;">
        <small style="color:#6c757d;">Total de miembros: ${miembros.size()}</small>
        <div>
            <button type="button" class="btn btn-sm btn-success" onclick="marcarTodos('ASISTIO')">✅ Todos Asistieron</button>
            <button type="button" class="btn btn-sm btn-danger" onclick="marcarTodos('NO_ASISTIO')">❌ Marcar Ausencias</button>
            <button type="button" class="btn btn-sm btn-secondary" onclick="limpiarTodo()">🔄 Limpiar Todo</button>
        </div>
    </div>
</c:if>
