# Configuración de credenciales de BD vía JNDI (`context.xml`)

## Descripción

La aplicación obtiene sus conexiones a la base de datos PostgreSQL a través de
**HikariCP** (pool de conexiones interno). Las credenciales se leen desde
**JNDI** (`java:comp/env`), configuradas en el `context.xml` de Tomcat, que
está excluido del repositorio mediante `.gitignore` para no exponer secretos.

---

## Archivo `context.xml`

El archivo `src/main/webapp/META-INF/context.xml` **no está incluido en el
repositorio** (excluido por `.gitignore`). Cada desarrollador/operador debe
crearlo localmente con sus credenciales reales.

### Parámetros JNDI requeridos

| Nombre JNDI       | Descripción                | Ejemplo                                         |
|-------------------|----------------------------|-------------------------------------------------|
| `db/url`          | URL JDBC de conexión       | `jdbc:postgresql://db-host:5432/comisiones`     |
| `db/username`     | Usuario de la base de datos| `comisiones_app`                                |
| `db/password`     | Contraseña del usuario     | *(secreto; no commitear)*                       |

---

## Dónde colocar `context.xml` en Tomcat

Hay dos opciones equivalentes:

### Opción A — Dentro del WAR (recomendado para desarrollo)

Crea el archivo en `src/main/webapp/META-INF/context.xml` (excluido del repo).
Tomcat lo carga automáticamente al desplegar el WAR.

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

## Formato completo de `context.xml`

```xml
<?xml version="1.0" encoding="UTF-8"?>
<Context path="/FormularioDeComisiones">

    <!-- Contraseña del LDAP -->
    <Environment name="ldap/bindPassword" type="java.lang.String"
                 value="PASSWORD_LDAP" override="false"/>

    <!-- Credenciales de la BD (leídas por HikariCP vía JNDI) -->
    <Environment name="db/url"      type="java.lang.String"
                 value="jdbc:postgresql://HOST:5432/NOMBRE_BD" override="false"/>
    <Environment name="db/username" type="java.lang.String"
                 value="USUARIO_BD" override="false"/>
    <Environment name="db/password" type="java.lang.String"
                 value="PASSWORD_BD" override="false"/>

</Context>
```
