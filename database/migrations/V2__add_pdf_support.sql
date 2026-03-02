-- ========================================
-- MIGRACIÓN: Añadir soporte para PDFs en actas
-- ========================================

-- 1. Añadir columnas para PDF en la tabla actas
ALTER TABLE actas 
ADD COLUMN IF NOT EXISTS pdf_nombre VARCHAR(255),
ADD COLUMN IF NOT EXISTS pdf_contenido BYTEA,
ADD COLUMN IF NOT EXISTS pdf_tipo_mime VARCHAR(100);

-- 2. Verificar estructura actualizada
SELECT 
    column_name,
    data_type,
    character_maximum_length,
    is_nullable
FROM information_schema.columns
WHERE table_name = 'actas'
ORDER BY ordinal_position;

-- 3. Ver actas con PDF (para testing después)
SELECT 
    id,
    comision_id,
    fecha_reunion,
    pdf_nombre,
    LENGTH(pdf_contenido) as pdf_size_bytes,
    pdf_tipo_mime
FROM actas
WHERE pdf_nombre IS NOT NULL
ORDER BY id DESC;