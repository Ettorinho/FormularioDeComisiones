-- ========================================
-- V24: Actualizar CHECK constraints de historial de cargos para admitir PONENTE
-- ========================================
-- Motivo:
-- La migración V23 añadió el valor PONENTE al ENUM cargo_type, pero
-- PostgreSQL exige que un valor de ENUM recién creado esté commiteado
-- antes de poder usarse (p. ej. en un CHECK constraint). Esta migración,
-- ejecutada en una transacción posterior a V23, actualiza los CHECK
-- constraints de comision_miembro_historial_cargos para permitir PONENTE
-- como cargo_anterior / cargo_nuevo.
-- ========================================

ALTER TABLE comision_miembro_historial_cargos
    DROP CONSTRAINT IF EXISTS check_cargo_nuevo;

ALTER TABLE comision_miembro_historial_cargos
    ADD CONSTRAINT check_cargo_nuevo
    CHECK (cargo_nuevo IN (
        'REFERENTE', 'RESPONSABLE', 'PRESIDENTE', 'PARTICIPANTE',
        'SECRETARIO', 'INVESTIGADOR_PRINCIPAL', 'INVESTIGADOR_COLABORADOR', 'FIRMANTE', 'PONENTE'
    ));

ALTER TABLE comision_miembro_historial_cargos
    DROP CONSTRAINT IF EXISTS check_cargo_anterior;

ALTER TABLE comision_miembro_historial_cargos
    ADD CONSTRAINT check_cargo_anterior
    CHECK (cargo_anterior IS NULL OR cargo_anterior IN (
        'REFERENTE', 'RESPONSABLE', 'PRESIDENTE', 'PARTICIPANTE',
        'SECRETARIO', 'INVESTIGADOR_PRINCIPAL', 'INVESTIGADOR_COLABORADOR', 'FIRMANTE', 'PONENTE'
    ));

-- Verificación
DO $$
BEGIN
    IF EXISTS (
        SELECT 1 FROM pg_constraint WHERE conname = 'check_cargo_nuevo'
    ) AND EXISTS (
        SELECT 1 FROM pg_constraint WHERE conname = 'check_cargo_anterior'
    ) THEN
        RAISE NOTICE '✅ Constraints check_cargo_nuevo / check_cargo_anterior actualizados correctamente para admitir PONENTE';
    ELSE
        RAISE EXCEPTION '❌ Error: No se pudieron actualizar los constraints de cargo en comision_miembro_historial_cargos';
    END IF;
END $$;
