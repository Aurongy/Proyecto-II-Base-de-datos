# Sistema de Gestión de Préstamo de Equipo Audiovisual
Java (Swing + JDBC) + MySQL

## 1. Base de datos (MySQL Workbench)
1. Abra MySQL Workbench y conéctese a su servidor (MySQL 8.0.16 o superior).
2. File > Open SQL Script... > `script_mysql.sql`.
3. Ejecute todo el script (icono del rayo). Crea la base `prestamo_audiovisual`, las tablas y los datos de prueba.

## 2. Conexión
Edite usuario y contraseña en `src/prestamo/Db.java` (constantes `USER` y `PASS`),
o defina las variables de entorno `DB_USER`, `DB_PASS` y `DB_URL`.

## 3. Driver JDBC
Descargue **MySQL Connector/J** (https://dev.mysql.com/downloads/connector/j/, opción "Platform Independent")
y use el archivo `mysql-connector-j-X.X.X.jar`.

## 4. Ejecutar
### Desde NetBeans / Eclipse / IntelliJ
Cree un proyecto Java, copie la carpeta `prestamo` dentro de `src`, agregue el .jar del conector
a las librerías del proyecto y ejecute `prestamo.Main`.

### Desde la terminal (JDK 11 o superior)
    javac -encoding UTF-8 -cp mysql-connector-j.jar -d out src/prestamo/*.java
    java  -cp out:mysql-connector-j.jar prestamo.Main        (Linux/macOS)
    java  -cp "out;mysql-connector-j.jar" prestamo.Main      (Windows)
