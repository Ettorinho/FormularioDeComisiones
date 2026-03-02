> ⚠️ **OBSOLETO**: Esta configuración ha sido reemplazada por HikariCP con variables de entorno. Ver `docs/DATASOURCE_JNDI.md` para la configuración actual.

```xml
<!-- src/main/webapp/META-INF/context.xml  (NO subir al repo) -->
<?xml version="1.0" encoding="UTF-8"?>
<Context path="/FormularioDeComisiones">

    <Environment
        name="ldap/bindPassword"
        type="java.lang.String"
        value="CAMBIAR_POR_PASSWORD_LDAP"
        override="false"/>

    <Resource
        name="jdbc/Comisiones"
        auth="Container"
        type="javax.sql.DataSource"
        factory="org.apache.tomcat.jdbc.pool.DataSourceFactory"
        driverClassName="org.postgresql.Driver"
        url="jdbc:postgresql://HOST:5432/NOMBRE_BD"
        username="USUARIO_BD"
        password="PASSWORD_BD"
        maxActive="10"
        maxIdle="5"
        minIdle="2"
        maxWaitMillis="30000"
        testOnBorrow="true"
        validationQuery="SELECT 1"
    />

</Context>
```
