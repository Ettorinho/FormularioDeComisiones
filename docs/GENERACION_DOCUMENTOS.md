# Generación Dinámica de Documentos para Actas

## Descripción de la Funcionalidad

El sistema de gestión de comisiones ahora cuenta con la capacidad de generar automáticamente documentos de actas en formato PDF y Word (.docx). Esta funcionalidad permite crear documentos profesionales y autocontenidos con toda la información de un acta, incluyendo:

- Información de la comisión
- Fecha de reunión
- Observaciones
- Tabla completa de asistencias con justificaciones
- Fecha de generación del documento

Los documentos generados son independientes de la funcionalidad existente de adjuntar PDFs a las actas. Ambas opciones coexisten y pueden usarse según las necesidades.

Además, el servicio genera una **plantilla de acta en blanco** en PDF y `.docx` mediante los métodos `generarPlantillaVaciaPdf()` y `generarPlantillaVaciaWord()`. Desde septiembre de 2026 esta plantilla ya no se basa en una simple aproximación visual a una captura, sino en el **análisis directo del archivo oficial** `/home/runner/work/FormularioDeComisiones/FormularioDeComisiones/docs/Modelo acta.doc`.

## Dependencias Utilizadas

### Apache PDFBox 2.0.30

**Propósito:** Generación de documentos PDF

Apache PDFBox es una biblioteca Java de código abierto para trabajar con documentos PDF. Se utiliza para:
- Crear documentos PDF desde cero
- Añadir texto con diferentes fuentes y tamaños
- Crear tablas con bordes
- Controlar el diseño y formato del documento

**Licencia:** Apache License 2.0

**Documentación:** https://pdfbox.apache.org/

### Apache POI 5.3.0

**Propósito:** Generación de documentos Word (.docx)

Apache POI es una biblioteca Java para leer y escribir archivos de Microsoft Office. Específicamente, se usa el módulo `poi-ooxml` para:
- Crear documentos Word en formato .docx (Office Open XML)
- Añadir párrafos con formato
- Crear tablas estructuradas
- Aplicar estilos profesionales

**Licencia:** Apache License 2.0

**Documentación:** https://poi.apache.org/

## Cómo Funciona la Generación

### Arquitectura

```
Usuario → Vista JSP → ActaController → ActaGeneratorService → Documento (bytes)
                                              ↓
                                         ActaDAO (datos)
```

### Flujo de Generación

1. **Solicitud del Usuario:** El usuario hace clic en "Generar PDF" o "Generar Word" en la vista de un acta
2. **Petición HTTP:** Se envía una petición GET a `/actas/generate-pdf?id={actaId}` o `/actas/generate-word?id={actaId}`
3. **Procesamiento en el Controller:**
   - Valida el ID del acta
   - Carga el acta desde la base de datos
   - Carga las asistencias asociadas
   - Instancia `ActaGeneratorService`
   - Llama al método correspondiente (`generarPdf` o `generarWord`)
4. **Generación del Documento:**
   - El servicio crea el documento en memoria
   - Formatea la información según el tipo de documento
   - Retorna el documento como array de bytes
5. **Descarga:**
   - El controller establece los headers HTTP apropiados
   - Envía el documento al navegador para descarga

## Plantilla en blanco basada en `docs/Modelo acta.doc`

### Cómo se analizó el modelo oficial

Para reconstruir fielmente la plantilla se analizó directamente `docs/Modelo acta.doc` con varias herramientas del entorno:

- `file`: confirmó que el origen es un documento Word 97-2003 OLE2 de una sola página.
- `antiword` y `catdoc`: permitieron recuperar el texto lineal y validar la distribución general de filas y columnas.
- `LibreOffice --headless --convert-to docx`: permitió inspeccionar el XML resultante (`word/header1.xml` y `word/document.xml`) para extraer anchos de columnas, `gridSpan`, `vMerge`, alturas de fila y presencia del logo.

### Estructura real extraída del `.doc`

El modelo oficial se compone de **dos tablas principales**:

1. **Cabecera de 1 fila y 3 columnas**
   - Columna izquierda: logo + texto `SECTOR DE BARBASTRO`
   - Columna central: `ACTA DE REUNIÓN`
   - Columna derecha: `Revisión A` y `Página 1 de 1`
   - Proporciones recuperadas: **3001 / 5137 / 2228**

2. **Tabla principal de 7 filas y 3 columnas base**
   - Fila 1: `COMISIÓN DE` ocupando las 3 columnas
   - Fila 2: `Fecha :` | `Hora inicio:` | `Hora fin:`
   - Fila 3: `ASISTENTES` (columna izquierda) y `EXCUSAN SU ASISTENCIA` ocupando las columnas 2-3
   - Filas 4-5: zona en blanco, con **merge vertical en la primera columna** y **merge horizontal en las columnas 2-3**
   - Fila 6: `ORDEN DEL DIA:` ocupando las 3 columnas
   - Fila 7: `RESUMEN DE LA REUNION:` ocupando las 3 columnas
   - Proporciones recuperadas: **4930 / 2553 / 2307**

### Implementación actual de la plantilla

- **PDF (`generarPlantillaVaciaPdf`)**
  - Reproduce la cabecera de 3 columnas y la tabla principal de 7 filas con sus merges equivalentes.
  - Conserva los nombres de campos AcroForm existentes: `nombreGrupo`, `fecha`, `horaInicio`, `horaFin`, `asistentes`, `excusaAsistencia`, `ordenDelDia` y `resumenReunion`.
  - Se eliminaron los antiguos campos de firma porque no forman parte del modelo oficial analizado.
  - El documento sigue generándose completamente en memoria mediante `ByteArrayOutputStream`.

- **Word (`generarPlantillaVaciaWord`)**
  - No usa `XWPFHeaderFooterPolicy`: la cabecera se construye como una **tabla normal en el cuerpo** para evitar problemas de renderizado de POI con tablas e imágenes en headers.
  - Todas las tablas creadas aplican bordes visibles explícitos en `CTTblBorders`.
  - La tabla principal usa `gridSpan` y `vMerge` para reproducir la estructura real del `.doc`.
  - El documento sigue generándose completamente en memoria mediante `ByteArrayOutputStream`.

### Fuentes, colores y sustituciones

Hallazgos principales del modelo original:

- `SECTOR DE BARBASTRO`: verde azulado aproximado `#009999`, texto pequeño en negrita.
- `ACTA DE REUNIÓN`: título centrado en negrita.
- `COMISIÓN DE`: banda sombreada en gris claro.
- Cabeceras y etiquetas del cuerpo: tipografía tipo Arial/Tahoma/Verdana en tamaños pequeños (aprox. 8pt-12pt).

Limitaciones conocidas:

- En **PDF** se usan fuentes estándar de PDFBox (`Helvetica`, `Helvetica Bold`, `Helvetica Oblique`) como sustitutas de Arial/Tahoma/Verdana, porque esas fuentes no forman parte del core estándar de PDFBox.
- En **`.docx`** la cabecera del modelo original estaba en el header del documento Word, pero se replica en el cuerpo por fiabilidad de Apache POI; visualmente mantiene la misma estructura de tabla.

### Verificación recomendada de la plantilla

La verificación estructural mínima debe confirmar:

- 1 página en el PDF.
- 8 campos AcroForm: `nombreGrupo`, `fecha`, `horaInicio`, `horaFin`, `asistentes`, `excusaAsistencia`, `ordenDelDia`, `resumenReunion`.
- 2 tablas en el `.docx`: una para la cabecera y otra para el cuerpo.
- 7 filas en la tabla principal del `.docx`.
- `gridSpan` en las filas `COMISIÓN DE`, `ORDEN DEL DIA` y `RESUMEN DE LA REUNION`.
- `vMerge` en la primera columna de las filas 4-5 del bloque de asistentes.

### Formato del PDF

- **Fuente:** Helvetica (estándar PDF)
- **Tamaños:**
  - Título: 18pt en negrita
  - Subtítulos: 14pt en negrita
  - Texto normal: 12pt
- **Márgenes:** 50 puntos en todos los lados
- **Tabla de Asistencias:**
  - 4 columnas: Nombre, DNI, Asistencia, Justificación
  - Bordes visibles
  - Encabezados en negrita
- **Pie de Página:** Fecha de generación en 10pt

> Nota: esta sección describe `generarPdf()` (actas con datos reales). La plantilla en blanco usa una maquetación distinta basada en `docs/Modelo acta.doc`.

### Formato del Word

- **Título:** Centrado, 18pt en negrita
- **Información:** Etiquetas en negrita (14pt), valores en texto normal (12pt)
- **Tabla de Asistencias:**
  - Encabezados en negrita
  - 4 columnas con ancho automático
  - Bordes visibles
- **Pie de Página:** Alineado a la derecha, itálica, 10pt

> Nota: esta sección describe `generarWord()` (actas con datos reales). La plantilla en blanco `.docx` replica la tabla del modelo oficial y no comparte esta estructura simple.

## Cómo Personalizar las Plantillas

### Modificar el Formato del PDF

Edita el archivo `src/main/java/com/comisiones/service/ActaGeneratorService.java`, método `generarPdf()`:

**Cambiar fuentes y tamaños:**
```java
// Constantes al inicio de la clase
private static final float TITLE_FONT_SIZE = 20; // Cambiar de 18 a 20
private static final float SUBTITLE_FONT_SIZE = 15; // Cambiar de 14 a 15
```

**Cambiar márgenes:**
```java
private static final float MARGIN = 60; // Cambiar de 50 a 60
```

**Añadir contenido adicional:**
```java
// Después de las observaciones, antes de la tabla
contentStream.beginText();
contentStream.setFont(PDType1Font.HELVETICA_BOLD, SUBTITLE_FONT_SIZE);
contentStream.newLineAtOffset(MARGIN, yPosition);
contentStream.showText("Nueva Sección:");
contentStream.endText();
yPosition -= 20;
```

### Modificar el Formato del Word

Edita el método `generarWord()` en el mismo archivo:

**Cambiar el título:**
```java
titleRun.setFontSize(20); // Cambiar de 18 a 20
titleRun.setColor("0000FF"); // Añadir color azul
```

**Añadir secciones:**
```java
// Antes de la tabla
XWPFParagraph nuevaSeccion = document.createParagraph();
XWPFRun nuevaRun = nuevaSeccion.createRun();
nuevaRun.setText("Nueva Información:");
nuevaRun.setBold(true);
nuevaRun.setFontSize(14);
```

**Modificar la tabla:**
```java
// En el método setTableCellText, cambiar el tamaño de fuente
run.setFontSize(11); // Cambiar de 12 a 11
```

### Cambiar el Formato de Fecha

Por defecto se usa `dd/MM/yyyy` (formato español). Para cambiarlo:

```java
// Al inicio de la clase ActaGeneratorService
private static final String DATE_FORMAT = "MM/dd/yyyy"; // Formato americano
// o
private static final String DATE_FORMAT = "yyyy-MM-dd"; // Formato ISO
```

## Ejemplos de Uso

### Caso 1: Generar PDF para Acta #123

1. Navega a la vista del acta: `http://localhost:8080/FormularioDeComisiones/actas/view?id=123`
2. En la sección "Generar y Descargar Acta", haz clic en "Generar PDF"
3. El navegador descargará automáticamente `Acta_123.pdf`

### Caso 2: Generar Word para Acta #456

1. Navega a la vista del acta: `http://localhost:8080/FormularioDeComisiones/actas/view?id=456`
2. En la sección "Generar y Descargar Acta", haz clic en "Generar Word"
3. El navegador descargará automáticamente `Acta_456.docx`

### Caso 3: Generar desde un enlace directo

Puedes generar documentos directamente con URLs:
```
http://localhost:8080/FormularioDeComisiones/actas/generate-pdf?id=123
http://localhost:8080/FormularioDeComisiones/actas/generate-word?id=456
```

## Pasos para Probar la Funcionalidad

### Prueba 1: Crear un Acta y Generar Documentos

1. **Crear una nueva acta:**
   - Ve a "Nueva Acta"
   - Selecciona una comisión
   - Ingresa la fecha de reunión
   - Añade observaciones (ej: "Reunión ordinaria para discutir el presupuesto del próximo año")
   - Marca las asistencias de los miembros
   - Añade justificaciones para los ausentes
   - Guarda el acta

2. **Generar PDF:**
   - En la vista del acta creada, haz clic en "Generar PDF"
   - Verifica que se descarga el archivo
   - Abre el PDF y verifica:
     - ✓ Título correcto con ID del acta
     - ✓ Nombre de la comisión visible
     - ✓ Fecha de reunión en formato dd/MM/yyyy
     - ✓ Observaciones completas
     - ✓ Tabla de asistencias con todos los miembros
     - ✓ Justificaciones visibles para ausentes
     - ✓ Fecha de generación en el pie de página

3. **Generar Word:**
   - Haz clic en "Generar Word"
   - Verifica que se descarga el archivo .docx
   - Abre el Word y verifica los mismos elementos que en el PDF

### Prueba 2: Verificar Independencia del PDF Adjunto

1. **Crear acta con PDF adjunto:**
   - Crea una nueva acta
   - Adjunta un PDF existente durante la creación
   - Guarda el acta

2. **Verificar coexistencia:**
   - En la vista del acta, debes ver DOS secciones:
     - "Generar y Descargar Acta" (nueva funcionalidad)
     - "Documento Adjunto" (funcionalidad existente)
   - Verifica que ambas funcionan independientemente:
     - "Generar PDF" crea un documento nuevo con la información del acta
     - "Descargar" (en documento adjunto) descarga el PDF que adjuntaste

3. **Verificar sin PDF adjunto:**
   - Crea una acta sin adjuntar PDF
   - Verifica que solo aparece la sección "Generar y Descargar Acta"
   - Los botones de generación deben funcionar normalmente

### Prueba 3: Casos Límite

1. **Acta sin observaciones:**
   - Crea un acta sin ingresar observaciones
   - Genera PDF y Word
   - Verifica que aparece "Sin observaciones"

2. **Acta con observaciones largas:**
   - Crea un acta con observaciones muy extensas (varios párrafos)
   - Genera PDF y Word
   - Verifica que el texto se muestra correctamente

3. **Muchos asistentes:**
   - Crea un acta con una comisión que tenga muchos miembros (10+)
   - Genera PDF
   - Verifica que la tabla se renderiza correctamente (puede requerir nueva página)

4. **Nombres largos:**
   - Si tienes miembros con nombres muy largos
   - Verifica que se truncan apropiadamente en la tabla del PDF

### Prueba 4: Verificar Logs

1. Activa el modo debug en `AppLogger.java`:
   ```java
   private static final boolean DEBUG_MODE = true;
   ```

2. Genera documentos y revisa los logs del servidor:
   ```
   [DEBUG] Generando PDF para acta ID: 123
   [DEBUG] PDF generado correctamente
   [DEBUG] PDF generado y descargado para acta ID: 123
   ```

3. Desactiva el debug cuando termines:
   ```java
   private static final boolean DEBUG_MODE = false;
   ```

## Troubleshooting

### Error: "El PDF se descarga pero está corrupto"

**Causa:** Problema en la generación o transmisión de bytes

**Solución:**
1. Verifica que no haya ningún log o print antes de escribir el OutputStream
2. Asegúrate de que la respuesta HTTP no tiene caracteres extras
3. Revisa los logs del servidor para excepciones durante la generación

### Error: "El Word no se puede abrir"

**Causa:** El documento .docx puede estar mal formado

**Solución:**
1. Verifica que las dependencias de Apache POI estén correctamente instaladas
2. Ejecuta `mvn clean install` para reconstruir el proyecto
3. Revisa que no haya excepciones en los logs al crear tablas o párrafos

### Error: "OutOfMemoryError al generar documentos"

**Causa:** Acta con demasiados datos o sistema con poca memoria

**Solución:**
1. Aumenta la memoria de la JVM: `-Xmx512m` o mayor
2. Optimiza las imágenes o contenido si es muy grande
3. Considera paginar el PDF si tiene muchas asistencias

### Error: "FileNotFoundException o ClassNotFoundException"

**Causa:** Dependencias de Maven no descargadas correctamente

**Solución:**
```bash
mvn clean install -U
```

Esto forzará la descarga de todas las dependencias.

### Los botones no aparecen en la vista

**Causa:** Posible error en el JSP o caché del navegador

**Solución:**
1. Limpia la caché del navegador (Ctrl + F5)
2. Reinicia el servidor Tomcat
3. Verifica que el archivo `view.jsp` tenga el nuevo código
4. Revisa los logs del servidor por errores de compilación JSP

### El texto en el PDF aparece con caracteres raros

**Causa:** Problema de codificación de caracteres

**Solución:**
1. Asegúrate de que el proyecto use UTF-8:
   ```xml
   <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
   ```
2. PDFBox con Helvetica soporta caracteres latinos básicos
3. Para caracteres especiales, considera usar fuentes Unicode embebidas

### El PDF se genera pero la tabla no se ve completa

**Causa:** La tabla es más ancha que la página o tiene muchas filas

**Solución:**
1. Ajusta el ancho de las columnas en `generarPdf()`:
   ```java
   float col1Width = tableWidth * 0.30f; // Reducir columna 1
   float col2Width = tableWidth * 0.15f; // Reducir columna 2
   // ...
   ```
2. Para muchas filas que exceden una página, se necesitará implementar paginación
3. Una solución temporal es limitar el número de asistencias mostradas o usar un tamaño de fuente menor

## Activar/Desactivar la Funcionalidad

### Desactivar solo en la UI

Para ocultar los botones sin eliminar el código del backend:

En `view.jsp`, comenta el bloque:
```jsp
<!-- DESACTIVADO TEMPORALMENTE
<div class="row mb-3">
    <div class="col-12">
        <div class="card border-info">
            ...
        </div>
    </div>
</div>
-->
```

Los endpoints seguirán funcionando si alguien los llama directamente.

### Desactivar completamente

1. **En el Controller:** Comenta los endpoints en `doGet()`:
   ```java
   // } else if (pathInfo.equals("/generate-pdf")) {
   //     generatePdfActa(request, response);
   // } else if (pathInfo.equals("/generate-word")) {
   //     generateWordActa(request, response);
   ```

2. **En la Vista:** Comenta los botones como se indicó arriba

3. **Opcional - Remover dependencias:** Si quieres reducir el tamaño del WAR, comenta en `pom.xml`:
   ```xml
   <!-- DESACTIVADO TEMPORALMENTE
   <dependency>
       <groupId>org.apache.pdfbox</groupId>
       ...
   </dependency>
   -->
   ```

## Notas de Implementación

- Los documentos se generan completamente en memoria (ByteArrayOutputStream)
- No se almacenan archivos temporales en el sistema de archivos
- La generación es síncrona, el usuario espera mientras se crea el documento
- Para actas muy grandes, considera implementar generación asíncrona
- Los documentos son independientes del estado de la aplicación una vez generados

## Consideraciones de Seguridad

- Se valida que el ID del acta exista antes de generar
- Se utiliza prepared statements en el DAO (protección contra SQL injection)
- Los archivos se generan en memoria, no en disco (menos riesgo de exposición)
- Se recomienda añadir control de acceso si hay actas privadas
- Considera limitar la frecuencia de generación para prevenir abuso (rate limiting)

## Mejoras Futuras Sugeridas

1. **Plantillas personalizables:** Permitir al usuario elegir entre diferentes estilos
2. **Logo de la organización:** Añadir imagen del logo en el encabezado
3. **Firma digital:** Incluir firmas digitales de los participantes
4. **Generación asíncrona:** Para actas muy grandes, generar en background
5. **Caché de documentos:** Guardar documentos generados para evitar regeneración
6. **Más formatos:** Soporte para ODT, HTML, RTF
7. **Exportación por lotes:** Generar múltiples actas en un ZIP
8. **Personalización de contenido:** Permitir seleccionar qué secciones incluir

## Plantilla de Acta Vacía

### Qué es

La plantilla de acta vacía se ofrece en **dos formatos**:
- **PDF rellenable (AcroForm)** de 1 página A4.
- **Word (.docx)** con la misma estructura visual.

No contiene datos de ninguna acta concreta almacenada en la base de datos.

#### Estructura visual (1 página)

**Encabezado** de 3 columnas con bordes:
- Columna izquierda: **logo Salud** y subtítulo **"SECTOR DE BARBASTRO"**.
- Columna central: título **"ACTA DE REUNIÓN"**.
- Columna derecha: **"Revisión A"** y **"Página 1 de 1"**.

**Cuerpo del documento**:
1. Banda de título **"COMISIÓN DE"**.
2. Fila con **"Fecha"**, **"Hora inicio"** y **"Hora fin"**.
3. Dos columnas: **"ASISTENTES (Nombre y Cargo)"** y **"EXCUSAN SU ASISTENCIA (Nombre, Cargo y Razón de la no asistencia)"**.
4. Bloque **"ORDEN DEL DÍA"**.
5. Bloque **"RESUMEN DE LA REUNIÓN"**.
6. Bloque de firma (Nombre, Cargo, Fecha/Hora cierre, Firma).

En PDF, el bloque **Resumen** sigue siendo un `PDTextField` multilínea con scroll interno.

#### Campos AcroForm del formulario

| Campo                         | Tipo        | Descripción                                    |
|-------------------------------|-------------|------------------------------------------------|
| `nombreGrupo`                 | TextField   | Nombre del grupo/comisión                       |
| `fecha`                       | TextField   | Fecha de la reunión                            |
| `horaInicio`                  | TextField   | Hora de inicio                                 |
| `horaFin`                     | TextField   | Hora de fin                                    |
| `asistentes`                  | TextField   | Lista de asistentes (multilínea)               |
| `excusaAsistencia`            | TextField   | Excusan su asistencia (multilínea)             |
| `ordenDelDia`                 | TextField   | Orden del día (multilínea)                     |
| `resumenReunion`              | TextField   | Resumen de la reunión (multilínea, con scroll) |
| `firmaNombre`                 | TextField   | Nombre del firmante                            |
| `firmaCargo`                  | TextField   | Cargo del firmante                             |
| `firmaFechaCierre`            | TextField   | Fecha/hora de cierre                           |

### Cómo se genera

Los métodos responsables son:
- `generarPlantillaVaciaPdf()` en `ActaGeneratorService.java`.
- `generarPlantillaVaciaWord()` en `ActaGeneratorService.java`.

La versión PDF usa `PDAcroForm` y `PDTextField` de PDFBox, dibuja recuadros con `PDPageContentStream` e inserta el logo con `PDImageXObject.createFromByteArray()`.  
La versión Word usa Apache POI (`XWPFDocument`, `XWPFTable`, `XWPFHeader`) para replicar el mismo encabezado y bloques.

### Verificación de campos

El PDF generado puede verificarse mediante la API de PDFBox:
```java
PDDocument doc = PDDocument.load(new File("Plantilla_Acta_Vacia.pdf"));
PDAcroForm form = doc.getDocumentCatalog().getAcroForm();
// form.getFields().size() → 10 campos
for (PDField f : form.getFields()) {
    System.out.println(f.getClass().getSimpleName() + ": " + f.getFullyQualifiedName());
}
// doc.getNumberOfPages() → 1 página
doc.close();
```

El Word generado puede verificarse con Apache POI:
```java
XWPFDocument docx = new XWPFDocument(new FileInputStream("Plantilla_Acta_Vacia.docx"));
System.out.println("Body tables: " + docx.getTables().size());
XWPFHeader header = docx.getHeaderList().isEmpty() ? null : docx.getHeaderList().get(0);
System.out.println("Header tables: " + (header != null ? header.getTables().size() : 0));
docx.close();
```

### Verificación visual

Al abrir PDF/Word:
- El encabezado muestra logo + "SECTOR DE BARBASTRO", "ACTA DE REUNIÓN" y "Revisión A / Página 1 de 1".
- Se visualizan los bloques de COMISIÓN, Fecha/Horas, Asistentes/Excusan, Orden del día, Resumen y Firma.
- En PDF, todos los campos definidos son rellenables y `resumenReunion` mantiene scroll interno.

### Cómo se accede desde la UI

- **Desde la vista de una comisión** (`/comisiones/view?id=...`): aparecen botones para descargar la plantilla en PDF y Word. Solo visibles para usuarios con rol ADMIN o GESTOR en comisiones activas.
- **URL PDF:** `GET /actas/generate-blank-template` — `Content-Type: application/pdf`, `filename="Plantilla_Acta_Vacia.pdf"`.
- **URL Word:** `GET /actas/generate-blank-template-word` — `Content-Type: application/vnd.openxmlformats-officedocument.wordprocessingml.document`, `filename="Plantilla_Acta_Vacia.docx"`.

### Ejemplo

```
GET http://localhost:8080/FormularioDeComisiones/actas/generate-blank-template
→ descarga Plantilla_Acta_Vacia.pdf

GET http://localhost:8080/FormularioDeComisiones/actas/generate-blank-template-word
→ descarga Plantilla_Acta_Vacia.docx
```

## Soporte y Contacto

Para problemas o preguntas sobre esta funcionalidad:
- Revisa primero esta documentación
- Consulta los logs del servidor con DEBUG_MODE activado
- Revisa el código fuente en `ActaGeneratorService.java` y `ActaController.java`
- Contacta al equipo de desarrollo con información detallada del error
