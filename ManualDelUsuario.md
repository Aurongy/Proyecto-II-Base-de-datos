# Manual del Usuario
## Sistema de Gestión de Préstamo de Equipo Audiovisual

### 1. Introducción

El Sistema de Gestión de Préstamo de Equipo Audiovisual es una aplicación de escritorio desarrollada en Java Swing, conectada a una base de datos MySQL mediante JDBC. Su propósito es facilitar la administración de empleados, categorías, equipos audiovisuales y solicitudes de préstamo dentro de una institución.

El sistema permite registrar, consultar, modificar y eliminar información, además de controlar los equipos solicitados y mantener la información organizada mediante una base de datos relacional.

### 2. Objetivo del manual

Orientar al usuario en el manejo correcto del sistema, explicando las funciones disponibles, el registro de información y los procedimientos necesarios para administrar los préstamos de equipos audiovisuales.

### 3. Requisitos para utilizar el sistema

- Computadora con Windows, Linux u otro sistema compatible con Java.
- Java instalado y configurado.
- Servidor MySQL en funcionamiento.
- Base de datos `prestamo_audiovisual` creada y preparada mediante el script SQL.
- Controlador JDBC de MySQL incorporado al proyecto.
- Credenciales válidas para conectarse a la base de datos.

### 4. Inicio del sistema

1. Iniciar el servidor MySQL.
2. Verificar que exista la base de datos `prestamo_audiovisual`.
3. Revisar la configuración de conexión en la clase `Db.java`.
4. Ejecutar la clase `Main.java` desde IntelliJ IDEA o desde el entorno de desarrollo utilizado.
5. Si la conexión es correcta, se abrirá la ventana principal del sistema.

Si la conexión falla, aparecerá un mensaje de error que indica que se debe revisar el usuario, la contraseña, el servidor y la ejecución del script SQL.

### 5. Descripción de las opciones del sistema

#### 5.1. Módulo Empleados

Permite administrar la información de las personas que pueden solicitar equipos.

Los campos disponibles son:

- Nombre.
- Apellido.
- Departamento.
- Correo electrónico.

**Procedimiento para registrar un empleado:**

1. Ingresar a la pestaña Empleados.
2. Completar todos los campos.
3. Presionar **Guardar (Insertar / Actualizar)**.
4. Verificar que aparezca el mensaje de confirmación.
5. Comprobar que el registro se muestre en la tabla.

Para modificar un empleado, seleccionar su registro en la tabla, cambiar los datos y presionar Guardar. Para eliminarlo, seleccionarlo y presionar Eliminar.

El sistema impide guardar campos vacíos y la base de datos controla que el correo no se repita.

#### 5.2. Módulo Categorías

Permite administrar las categorías utilizadas para clasificar los equipos audiovisuales.

El campo disponible es Nombre.

Para registrar una categoría:

1. Ingresar a la pestaña Categorías.
2. Escribir el nombre de la categoría.
3. Presionar Guardar.
4. Verificar el registro en la tabla.

Se pueden utilizar las opciones Nuevo / Limpiar, Guardar y Eliminar. No se puede eliminar una categoría que esté relacionada con equipos.

#### 5.3. Módulo Equipos

Permite registrar y administrar los recursos audiovisuales de la institución.

Los campos disponibles son:

- Código.
- Nombre.
- Marca.
- Modelo.
- Número de serie.
- Estado.
- Categoría.

Los estados disponibles son:

- **Disponible:** equipo que puede seleccionarse para una nueva solicitud.
- **Prestado:** equipo que se encuentra registrado como entregado.
- **Mantenimiento:** equipo que requiere revisión o reparación.
- **Dañado:** equipo que no se encuentra en condiciones normales de uso.

Para registrar un equipo, completar los campos, seleccionar su estado y categoría y presionar Guardar.

El código y el número de serie deben ser únicos. La categoría seleccionada debe existir previamente en la base de datos.

#### 5.4. Módulo Solicitudes

Este módulo permite registrar las solicitudes de préstamo y los equipos que forman parte de cada solicitud.

**Datos principales de la solicitud:**

- Empleado solicitante.
- Fecha de solicitud.
- Fecha de uso.
- Fecha de devolución prevista.
- Motivo.
- Estado.

**Equipos solicitados:**

El usuario puede seleccionar equipos disponibles y agregarlos al detalle de la solicitud mediante el botón Agregar equipo. También puede seleccionar un equipo del detalle y retirarlo utilizando Quitar equipo seleccionado.

**Procedimiento para registrar una solicitud:**

1. Ingresar a la pestaña Solicitudes.
2. Seleccionar al empleado.
3. Introducir las fechas en formato `AAAA-MM-DD`.
4. Escribir el motivo del préstamo.
5. Seleccionar el estado de la solicitud.
6. Elegir un equipo disponible y presionar Agregar equipo.
7. Repetir el procedimiento para los demás equipos necesarios.
8. Presionar Guardar solicitud.
9. Verificar el mensaje de confirmación.

El sistema requiere un empleado, un motivo, fechas válidas y al menos un equipo.

La fecha de uso no puede ser anterior a la fecha de solicitud, y la devolución prevista no puede ser anterior a la fecha de uso.

Al guardar una solicitud en estado Entregada, el código actual cambia el estado de los equipos incluidos a Prestado.

Para modificar una solicitud, seleccionarla en la tabla, editar los datos y guardar los cambios.

La opción Eliminar solicitud permite eliminar únicamente solicitudes cuyo estado sea Cancelada.

#### 5.5. Módulo Consultas

Este módulo contiene botones para consultar información almacenada en la base de datos:

- **Todos los empleados:** muestra los empleados registrados.
- **Todos los equipos:** muestra los equipos y sus categorías.
- **Equipos disponibles:** muestra los equipos cuyo estado es Disponible.
- **Solicitudes registradas:** muestra las solicitudes, el empleado asociado y la cantidad de equipos incluidos.

Para ejecutar una consulta, presionar el botón correspondiente. El resultado aparecerá en la tabla central.

### 6. Mensajes y validaciones

El sistema presenta mensajes para orientar al usuario cuando ocurre alguna situación:

- **Campos obligatorios:** se debe completar la información faltante.
- **Formato de fecha incorrecto:** utilizar el formato `AAAA-MM-DD`.
- **Registro duplicado:** utilizar un código, correo o número de serie que no exista.
- **Referencia inválida:** seleccionar un empleado, categoría o equipo existente.
- **Registro relacionado:** no es posible eliminar ciertos datos mientras otros registros dependan de ellos.
- **Error de conexión:** revisar el servidor MySQL y las credenciales configuradas.

### 7. Recomendaciones de uso

- Verificar los datos antes de guardar.
- Utilizar códigos y números de serie únicos.
- Registrar primero las categorías y los empleados necesarios.
- Comprobar la disponibilidad de los equipos antes de crear solicitudes.
- No cerrar el servidor MySQL mientras se utiliza la aplicación.
- Realizar copias de seguridad periódicas de la base de datos.

### 8. Conclusión

El sistema facilita la administración de equipos audiovisuales y sus solicitudes de préstamo mediante una interfaz gráfica sencilla. El uso correcto de sus módulos permite mantener organizados los datos, reducir errores de registro y consultar la información necesaria para controlar los recursos de la institución.