-- database/migrations/003_fix_historial_cargo_constraints.sql
-- Añadir CHECK constraints a comision_miembro_historial_cargos para garantizar
-- que cargo_anterior y cargo_nuevo contengan solo valores válidos del ENUM cargo_type.
-- Idempotente: usa DROP CONSTRAINT IF EXISTS antes de ADD CONSTRAINT.

ALTER TABLE comision_miembro_historial_cargos
    DROP CONSTRAINT IF EXISTS check_cargo_nuevo;

ALTER TABLE comision_miembro_historial_cargos
    ADD CONSTRAINT check_cargo_nuevo
    CHECK (cargo_nuevo IN (
        'REFERENTE', 'RESPONSABLE', 'PRESIDENTE', 'PARTICIPANTE',
        'SECRETARIO', 'INVESTIGADOR_PRINCIPAL', 'INVESTIGADOR_COLABORADOR'
    ));

ALTER TABLE comision_miembro_historial_cargos
    DROP CONSTRAINT IF EXISTS check_cargo_anterior;

ALTER TABLE comision_miembro_historial_cargos
    ADD CONSTRAINT check_cargo_anterior
    CHECK (cargo_anterior IS NULL OR cargo_anterior IN (
        'REFERENTE', 'RESPONSABLE', 'PRESIDENTE', 'PARTICIPANTE',
        'SECRETARIO', 'INVESTIGADOR_PRINCIPAL', 'INVESTIGADOR_COLABORADOR'
    ));
