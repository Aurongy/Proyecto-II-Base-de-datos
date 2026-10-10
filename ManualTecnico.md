# Manual Técnico
## Sistema de Gestión de Préstamo de Equipo Audiovisual

### 1. Introducción técnica

El sistema fue desarrollado utilizando Java, la biblioteca gráfica Swing, JDBC y MySQL. Su arquitectura separa la interfaz gráfica de la lógica de acceso a datos y de la estructura de almacenamiento.

La base de datos `prestamo_audiovisual` está organizada en cinco tablas principales: `empleado`, `categoria`, `equipo`, `solicitud` y `detalle_solicitud`.

El diseño utiliza claves primarias, claves foráneas, restricciones de unicidad, restricciones CHECK y reglas de integridad referencial para mantener la consistencia de la información.

### 2. Arquitectura del sistema

La aplicación se organiza en las siguientes clases:

| Clase | Responsabilidad |
|---|---|
| `Main` | Inicializa la aplicación y construye la ventana principal con pestañas. |
| `Campo` | Define los tipos de campos utilizados por los formularios CRUD. |
| `CrudPanel` | Administra las operaciones de inserción, consulta, actualización y eliminación de empleados, categorías y equipos. |
| `SolicitudPanel` | Administra las solicitudes y sus equipos asociados. |
| `ConsultaPanel` | Ejecuta las consultas SELECT disponibles. |
| `Db` | Gestiona la conexión JDBC y las operaciones SQL. |
| `Util` | Proporciona métodos para tablas, validaciones, mensajes y selección de elementos. |
| `Refrescable` | Define el método `refrescar()` para actualizar los datos de los paneles. |

La conexión a MySQL se realiza mediante el controlador MySQL Connector/J.

### 3. Modelo de datos

La base de datos se diseñó para representar empleados, categorías, equipos audiovisuales y solicitudes de préstamo. La tabla `detalle_solicitud` relaciona cada solicitud con los equipos que contiene.

#### 3.1. Identificación de entidades

**Entidad EMPLEADO**

Almacena la información de las personas que solicitan equipos.

**Entidad CATEGORIA**

Clasifica los equipos según su tipo, por ejemplo, audio, video, computación y accesorios.

**Entidad EQUIPO**

Contiene los datos de identificación, características y estado de cada recurso audiovisual.

**Entidad SOLICITUD**

Registra la información general de cada préstamo, como fechas, motivo, estado y empleado solicitante.

**Entidad DETALLE_SOLICITUD**

Representa los equipos asociados a cada solicitud y permite establecer una relación entre solicitudes y equipos.

### 4. Identificación de atributos

#### 4.1. Tabla EMPLEADO

| Atributo | Tipo de dato | Descripción |
|---|---|---|
| `id_empleado` | INT | Identificador único; clave primaria autoincremental. |
| `nombre` | VARCHAR(60) | Nombre del empleado. |
| `apellido` | VARCHAR(60) | Apellido del empleado. |
| `departamento` | VARCHAR(80) | Departamento al que pertenece. |
| `correo` | VARCHAR(120) | Correo electrónico único. |

#### 4.2. Tabla CATEGORIA

| Atributo | Tipo de dato | Descripción |
|---|---|---|
| `id_categoria` | INT | Identificador único; clave primaria. |
| `nombre` | VARCHAR(60) | Nombre único de la categoría. |

#### 4.3. Tabla EQUIPO

| Atributo | Tipo de dato | Descripción |
|---|---|---|
| `id_equipo` | INT | Identificador único; clave primaria. |
| `codigo` | VARCHAR(20) | Código único del equipo. |
| `nombre` | VARCHAR(80) | Nombre del recurso. |
| `marca` | VARCHAR(60) | Marca del equipo. |
| `modelo` | VARCHAR(60) | Modelo del equipo. |
| `numero_serie` | VARCHAR(60) | Número de serie único. |
| `estado` | VARCHAR(15) | Estado operativo del equipo. |
| `id_categoria` | INT | Clave foránea que identifica la categoría. |

#### 4.4. Tabla SOLICITUD

| Atributo | Tipo de dato | Descripción |
|---|---|---|
| `id_solicitud` | INT | Identificador único; clave primaria. |
| `fecha_solicitud` | DATE | Fecha de registro de la solicitud. |
| `fecha_uso` | DATE | Fecha prevista de utilización. |
| `fecha_devolucion_prevista` | DATE | Fecha prevista para devolver el equipo. |
| `motivo` | VARCHAR(200) | Motivo del préstamo. |
| `estado` | VARCHAR(15) | Estado de la solicitud. |
| `id_empleado` | INT | Clave foránea del empleado solicitante. |

#### 4.5. Tabla DETALLE_SOLICITUD

| Atributo | Tipo de dato | Descripción |
|---|---|---|
| `id_detalle` | INT | Identificador único; clave primaria. |
| `id_solicitud` | INT | Clave foránea de la solicitud. |
| `id_equipo` | INT | Clave foránea del equipo solicitado. |

La restricción UNIQUE sobre (`id_solicitud`, `id_equipo`) evita que un mismo equipo se agregue dos veces a una misma solicitud.

### 5. Relaciones entre entidades

Las relaciones del modelo son las siguientes:

1. **EMPLEADO — SOLICITUD:** un empleado puede registrar varias solicitudes, pero cada solicitud pertenece a un único empleado.
2. **CATEGORIA — EQUIPO:** una categoría puede agrupar varios equipos, pero cada equipo pertenece a una categoría.
3. **SOLICITUD — DETALLE_SOLICITUD:** una solicitud puede tener varios detalles y cada detalle pertenece a una solicitud.
4. **EQUIPO — DETALLE_SOLICITUD:** un equipo puede aparecer en los detalles de distintas solicitudes a lo largo del tiempo, y cada detalle identifica un solo equipo.

Las relaciones entre SOLICITUD y EQUIPO son de muchos a muchos y se resuelven mediante la entidad asociativa DETALLE_SOLICITUD.

### 6. Cardinalidades

| Relación | Cardinalidad | Explicación |
|---|---|---|
| EMPLEADO — SOLICITUD | 1:N | Un empleado puede tener varias solicitudes. |
| CATEGORIA — EQUIPO | 1:N | Una categoría puede contener varios equipos. |
| SOLICITUD — DETALLE_SOLICITUD | 1:N | Una solicitud puede contener varios detalles. |
| EQUIPO — DETALLE_SOLICITUD | 1:N | Un equipo puede figurar en varios detalles históricos. |
| SOLICITUD — EQUIPO | N:M | Una solicitud puede incluir varios equipos y un equipo puede participar en distintas solicitudes a lo largo del tiempo. |

Las claves foráneas hacen obligatoria la existencia del registro relacionado. Además, el sistema exige que cada solicitud tenga al menos un equipo al guardarse desde la interfaz.

### 7. Proceso de normalización hasta la Tercera Forma Normal (3FN)

La normalización busca reducir la duplicidad de información y prevenir anomalías al insertar, modificar o eliminar registros.

#### 7.1. Primera Forma Normal (1FN)

Una tabla está en 1FN cuando sus atributos contienen valores atómicos y no existen grupos repetitivos dentro de una misma fila.

En este sistema:

- Los datos del empleado se guardan en campos separados.
- Cada equipo se registra individualmente.
- Los equipos de una solicitud se almacenan en filas separadas de `detalle_solicitud`.
- No se guardan listas de equipos dentro de una columna de `solicitud`.

Por ello, el diseño cumple la 1FN.

#### 7.2. Segunda Forma Normal (2FN)

Una tabla está en 2FN cuando cumple la 1FN y todos los atributos no clave dependen de la clave primaria completa.

En el diseño propuesto, cada tabla tiene una clave primaria simple (`id_empleado`, `id_categoria`, `id_equipo`, `id_solicitud` o `id_detalle`). Por lo tanto, no existen dependencias parciales respecto de una clave primaria compuesta.

En `detalle_solicitud`, la combinación de `id_solicitud` y `id_equipo` también tiene una restricción UNIQUE que evita asociaciones duplicadas, mientras que `id_detalle` funciona como clave primaria.

#### 7.3. Tercera Forma Normal (3FN)

Una tabla está en 3FN cuando cumple la 2FN y no presenta dependencias transitivas de atributos no clave respecto de una clave candidata.

El sistema separa los datos en entidades independientes:

- Los datos personales se almacenan en `empleado`, no en cada solicitud.
- El nombre de una categoría se guarda en `categoria`, no se repite en cada equipo.
- Los datos descriptivos de los equipos se almacenan en `equipo`, no en `solicitud`.
- Los equipos asociados a cada préstamo se registran en `detalle_solicitud`.

Así, los atributos descriptivos dependen de la clave de su propia tabla y las relaciones se representan mediante claves foráneas.

**Conclusión de normalización:** el modelo propuesto cumple las formas normales 1FN, 2FN y 3FN, bajo las dependencias funcionales previstas en el diseño.

### 8. Modelo relacional

El modelo relacional queda representado de la siguiente manera:

- **EMPLEADO** (`id_empleado` PK, nombre, apellido, departamento, correo UNIQUE).
- **CATEGORIA** (`id_categoria` PK, nombre UNIQUE).
- **EQUIPO** (`id_equipo` PK, codigo UNIQUE, nombre, marca, modelo, numero_serie UNIQUE, estado, `id_categoria` FK).
- **SOLICITUD** (`id_solicitud` PK, fecha_solicitud, fecha_uso, fecha_devolucion_prevista, motivo, estado, `id_empleado` FK).
- **DETALLE_SOLICITUD** (`id_detalle` PK, `id_solicitud` FK, `id_equipo` FK, UNIQUE (`id_solicitud`, `id_equipo`)).

PK significa clave primaria, FK significa clave foránea y UNIQUE indica que el valor o combinación debe ser único.

### 9. Restricciones de integridad

#### 9.1. Integridad de entidad

Las claves primarias identifican de manera única cada registro. Todas las tablas cuentan con una clave primaria y los identificadores se generan automáticamente mediante `AUTO_INCREMENT`.

#### 9.2. Integridad referencial

Las claves foráneas aseguran que las referencias correspondan a registros existentes:

- `equipo.id_categoria` referencia `categoria.id_categoria`.
- `solicitud.id_empleado` referencia `empleado.id_empleado`.
- `detalle_solicitud.id_solicitud` referencia `solicitud.id_solicitud`.
- `detalle_solicitud.id_equipo` referencia `equipo.id_equipo`.

Las reglas `ON UPDATE CASCADE` permiten propagar cambios en las claves referenciadas. Las reglas `ON DELETE RESTRICT` impiden eliminar categorías, empleados o equipos que aún tengan referencias protegidas. En el detalle de solicitudes se utiliza `ON DELETE CASCADE`, por lo que eliminar una solicitud elimina sus detalles asociados.

#### 9.3. Integridad de dominio

Las restricciones CHECK establecen reglas para los valores permitidos:

- Los nombres, apellidos, departamentos, categorías y motivos no deben estar vacíos después de eliminar espacios en blanco.
- Los estados de los equipos deben pertenecer a `Disponible`, `Prestado`, `Mantenimiento` o `Dañado`.
- Los estados de las solicitudes deben pertenecer a `Pendiente`, `Aprobada`, `Entregada`, `Devuelta` o `Cancelada`.
- La fecha de uso debe ser igual o posterior a la fecha de solicitud.
- La fecha de devolución prevista debe ser igual o posterior a la fecha de uso.

El correo electrónico tiene una restricción CHECK definida mediante LIKE y una restricción UNIQUE. La validación exacta del formato depende del patrón SQL utilizado.

#### 9.4. Integridad de unicidad

Se aplican restricciones UNIQUE a:

- `empleado.correo`.
- `categoria.nombre`.
- `equipo.codigo`.
- `equipo.numero_serie`.
- La combinación `detalle_solicitud.id_solicitud` y `detalle_solicitud.id_equipo`.

Estas reglas reducen los registros duplicados.

#### 9.5. Integridad de los datos desde Java

La aplicación valida campos obligatorios, formato de fechas y selección de registros relacionados antes de ejecutar las operaciones SQL.

En el módulo de solicitudes, las operaciones de actualización del maestro y sus detalles se agrupan en una transacción JDBC. Si una operación SQL falla durante ese proceso, se ejecuta `rollback`; si todo resulta correcto, se ejecuta `commit`.

### 10. Consultas SQL principales

El sistema implementa consultas para recuperar:

- Todos los empleados.
- Todas las categorías.
- Todos los equipos con su categoría.
- Equipos disponibles.
- Solicitudes registradas con el nombre del empleado y la cantidad de equipos.

Las consultas de inserción, actualización y eliminación utilizan `PreparedStatement` y parámetros para los valores de los registros.

Los nombres de tablas y columnas construidos dinámicamente en el CRUD deben proceder exclusivamente de configuraciones internas confiables.

### 11. Configuración de la conexión

La clase `Db` utiliza los parámetros siguientes:

- URL: `jdbc:mysql://localhost:3306/prestamo_audiovisual`
- Usuario predeterminado: `root`
- Contraseña predeterminada: `auro`

La aplicación permite sobrescribir los parámetros mediante las variables de entorno `DB_URL`, `DB_USER` y `DB_PASS`.

Para un entorno real, se recomienda sustituir las credenciales de ejemplo por credenciales seguras, evitar el uso de la cuenta administrativa y no guardar contraseñas en el código fuente.

### 12. Conclusión técnica

El sistema presenta una estructura modular que separa la interfaz gráfica, las operaciones SQL y las utilidades comunes. La base de datos normalizada hasta 3FN permite organizar los datos de manera consistente, mientras que las claves foráneas y restricciones de integridad protegen las relaciones entre empleados, equipos, categorías y solicitudes.

Como mejora futura, se recomienda reforzar el control de disponibilidad de los equipos durante las transacciones, sincronizar todos los cambios de estado con las operaciones de entrega y devolución y ampliar las pruebas de integridad y concurrencia.