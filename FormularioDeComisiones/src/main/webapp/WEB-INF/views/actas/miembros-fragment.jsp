<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<c:if test="${empty miembros}">
    <p class="text-warning text-center py-3">
        ⚠️ Esta comisión no tiene miembros activos
    </p>
</c:if>

<c:if test="${not empty miembros}">
    <div class="row mb-2 fw-bold border-bottom pb-2">
        <div class="col-1 text-center">Asistió</div>
        <div class="col-4">Nombre</div>
        <div class="col-2">DNI</div>
        <div class="col-3">Email</div>
        <div class="col-2">Cargo</div>
    </div>
    
    <c:forEach var="cm" items="${miembros}">
        <div class="miembro-row">
            <div class="row align-items-center">
                <div class="col-1 text-center">
                    <input type="checkbox" 
                           class="form-check-input" 
                           name="asistio" 
                           value="${cm.miembro.id}"
                           id="asistio_${cm.miembro.id}">
                </div>
                <div class="col-4">
                    <label for="asistio_${cm.miembro.id} "class="form-check-label">
                        <strong>${cm.miembro.nombreApellidos}</strong>
                    </label>
                </div>
                <div class="col-2">
                    <small class="text-muted">${cm.miembro. dniNif}</small>
                </div>
                <div class="col-3">
                    <small class="text-muted">${cm.miembro.email}</small>
                </div>
                <div class="col-2">
                    <c:choose>
                        <c:when test="${cm.cargo == 'PRESIDENTE'}">
                            <span class="badge bg-danger cargo-badge">Presidente</span>
                        </c:when>
                        <c:when test="${cm.cargo == 'SECRETARIO'}">
                            <span class="badge bg-warning text-dark cargo-badge">Secretario</span>
                        </c:when>
                        <c:when test="${cm.cargo == 'RESPONSABLE'}">
                            <span class="badge bg-primary cargo-badge">Responsable</span>
                        </c:when>
                        <c:when test="${cm.cargo == 'REFERENTE'}">
                            <span class="badge bg-secondary cargo-badge">Referente</span>
                        </c:when>
                        <c:when test="${cm.cargo == 'INVESTIGADOR_PRINCIPAL'}">
                            <span class="badge bg-dark cargo-badge">Inv. Principal</span>
                        </c:when>
                        <c:when test="${cm.cargo == 'INVESTIGADOR_COLABORADOR'}">
                            <span class="badge bg-dark cargo-badge">Inv.  Colaborador</span>
                        </c:when>
                        <c:when test="${cm. cargo == 'PARTICIPANTE'}">
                            <span class="badge bg-light text-dark cargo-badge">Participante</span>
                        </c:when>
                        <c:otherwise>
                            <span class="badge bg-secondary cargo-badge">${cm.cargo}</span>
                        </c:otherwise>
                    </c:choose>
                </div>
            </div>
        </div>
    </c:forEach>
    
    <div class="mt-3 d-flex justify-content-between align-items-center">
        <small class="text-muted">Total de miembros: ${miembros.size()}</small>
        <div>
            <button type="button" class="btn btn-sm btn-outline-primary" onclick="marcarTodos(true)">
                ✓ Marcar Todos
            </button>
            <button type="button" class="btn btn-sm btn-outline-secondary" onclick="marcarTodos(false)">
                ✗ Desmarcar Todos
            </button>
        </div>
    </div>
</c:if>

<script>
function marcarTodos(marcar) {
    const checkboxes = document.querySelectorAll('input[name="asistio"]');
    checkboxes.forEach(cb => cb.checked = marcar);
    console.log('Marcados:', marcar ?  'todos' : 'ninguno');
}
</script>