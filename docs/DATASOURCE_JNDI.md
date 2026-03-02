# Configuración del DataSource JNDI `jdbc/Comisiones`

## Descripción

La aplicación obtiene todas sus conexiones a la base de datos PostgreSQL a través
de un **DataSource JNDI** gestionado por Tomcat, con el nombre:

```
java:comp/env/jdbc/Comisiones
```

Esto delega al contenedor el pool de conexiones y la gestión de credenciales,
evitando credenciales en el código fuente.

---

## Archivo `context.xml`

El repositorio incluye `src/main/webapp/META-INF/context.xml` con valores de
ejemplo (placeholders). **Antes de desplegar en producción debes sustituir los
valores `CHANGE_ME`** con los datos reales de tu entorno.

### Ejemplo de configuración

```xml
<Context>
    <Resource
        name="jdbc/Comisiones"
        auth="Container"
        type="javax.sql.DataSource"
        factory="org.apache.tomcat.jdbc.pool.DataSourceFactory"

        driverClassName="org.postgresql.Driver"
        url="jdbc:postgresql://localhost:5432/comisiones"
        username="CHANGE_ME"
        password="CHANGE_ME"

        maxActive="10"
        maxIdle="5"
        minIdle="2"
        maxWaitMillis="30000"

        testOnBorrow="true"
        validationQuery="SELECT 1"
    />
</Context>
```

### Variables a configurar

| Propiedad        | Descripción                                      | Ejemplo                                        |
|------------------|--------------------------------------------------|------------------------------------------------|
| `url`            | URL JDBC de conexión                             | `jdbc:postgresql://db-host:5432/comisiones`    |
| `username`       | Usuario de la base de datos                      | `comisiones_app`                               |
| `password`       | Contraseña del usuario                           | *(secreto; no commitear)*                      |
| `maxActive`      | Nº máximo de conexiones activas en el pool       | `10`                                           |
| `maxIdle`        | Nº máximo de conexiones inactivas en el pool     | `5`                                            |
| `minIdle`        | Nº mínimo de conexiones inactivas en el pool     | `2`                                            |
| `maxWaitMillis`  | Tiempo máximo de espera para obtener conexión    | `30000` (30 s)                                 |
| `validationQuery`| Consulta de validación al tomar conexión del pool| `SELECT 1`                                     |

---

## Dónde colocar `context.xml` en Tomcat

Hay dos opciones equivalentes:

### Opción A — Dentro del WAR (recomendado para desarrollo)

El archivo `META-INF/context.xml` incluido en el WAR es cargado automáticamente
por Tomcat al desplegar la aplicación. Es la opción más sencilla pero almacena
la configuración dentro del artefacto desplegable.

### Opción B — Externo al WAR (recomendado para producción)

Crea el archivo en:

```
$CATALINA_HOME/conf/Catalina/localhost/FormularioDeComisiones.xml
```

Esto permite modificar credenciales sin reconstruir el WAR y es la práctica
habitual en entornos de producción.

---

## Requisito: driver PostgreSQL

El driver JDBC de PostgreSQL debe estar disponible para Tomcat **en el momento
del inicio**. Hay dos formas:

1. **En el WAR** (`WEB-INF/lib/`): la dependencia está declarada en `pom.xml`
   con scope `compile` (opción actual), por lo que el JAR se incluye
   automáticamente en el WAR al construir con Maven.

2. **En `$CATALINA_HOME/lib/`**: descarga el JAR desde
   [https://jdbc.postgresql.org/download/](https://jdbc.postgresql.org/download/)
   y cópialo allí. En este caso puedes cambiar el scope de la dependencia en
   `pom.xml` a `provided`.

---

## Referencia en `web.xml` (opcional pero recomendado)

Para declarar explícitamente la dependencia JNDI en el descriptor de la
aplicación, añade en `WEB-INF/web.xml`:

```xml
<resource-ref>
    <description>DataSource para la BD de Comisiones</description>
    <res-ref-name>jdbc/Comisiones</res-ref-name>
    <res-type>javax.sql.DataSource</res-type>
    <res-auth>Container</res-auth>
</resource-ref>
```
