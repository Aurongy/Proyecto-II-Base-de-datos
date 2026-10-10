-- =====================================================================
-- SISTEMA DE GESTION DE PRESTAMO DE EQUIPO AUDIOVISUAL
-- Script MySQL (ejecutar completo en MySQL Workbench). Requiere MySQL 8.0.16+
-- Normalizado hasta 3FN
-- =====================================================================
DROP DATABASE IF EXISTS prestamo_audiovisual;
CREATE DATABASE prestamo_audiovisual CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE prestamo_audiovisual;

-- ---------------------------------------------------------------------
-- EMPLEADO
-- ---------------------------------------------------------------------
CREATE TABLE empleado (
    id_empleado   INT AUTO_INCREMENT PRIMARY KEY,
    nombre        VARCHAR(60)  NOT NULL,
    apellido      VARCHAR(60)  NOT NULL,
    departamento  VARCHAR(80)  NOT NULL,
    correo        VARCHAR(120) NOT NULL,
    CONSTRAINT uq_empleado_correo UNIQUE (correo),
    CONSTRAINT ck_empleado_nombre CHECK (CHAR_LENGTH(TRIM(nombre)) > 0),
    CONSTRAINT ck_empleado_apellido CHECK (CHAR_LENGTH(TRIM(apellido)) > 0),
    CONSTRAINT ck_empleado_depto CHECK (CHAR_LENGTH(TRIM(departamento)) > 0),
    CONSTRAINT ck_empleado_correo CHECK (correo LIKE '%_@_%._%')
) ENGINE=InnoDB;

-- ---------------------------------------------------------------------
-- CATEGORIA
-- ---------------------------------------------------------------------
CREATE TABLE categoria (
    id_categoria  INT AUTO_INCREMENT PRIMARY KEY,
    nombre        VARCHAR(60) NOT NULL,
    CONSTRAINT uq_categoria_nombre UNIQUE (nombre),
    CONSTRAINT ck_categoria_nombre CHECK (CHAR_LENGTH(TRIM(nombre)) > 0)
) ENGINE=InnoDB;

-- ---------------------------------------------------------------------
-- EQUIPO
-- ---------------------------------------------------------------------
CREATE TABLE equipo (
    id_equipo     INT AUTO_INCREMENT PRIMARY KEY,
    codigo        VARCHAR(20)  NOT NULL,
    nombre        VARCHAR(80)  NOT NULL,
    marca         VARCHAR(60)  NOT NULL,
    modelo        VARCHAR(60)  NOT NULL,
    numero_serie  VARCHAR(60)  NOT NULL,
    estado        VARCHAR(15)  NOT NULL DEFAULT 'Disponible',
    id_categoria  INT          NOT NULL,
    CONSTRAINT uq_equipo_codigo UNIQUE (codigo),
    CONSTRAINT uq_equipo_serie  UNIQUE (numero_serie),
    CONSTRAINT ck_equipo_estado CHECK (estado IN ('Disponible','Prestado','Mantenimiento','Dañado')),
    CONSTRAINT fk_equipo_categoria FOREIGN KEY (id_categoria)
        REFERENCES categoria(id_categoria) ON UPDATE CASCADE ON DELETE RESTRICT
) ENGINE=InnoDB;

-- ---------------------------------------------------------------------
-- SOLICITUD (MAESTRO)
-- ---------------------------------------------------------------------
CREATE TABLE solicitud (
    id_solicitud               INT AUTO_INCREMENT PRIMARY KEY,
    fecha_solicitud            DATE         NOT NULL,
    fecha_uso                  DATE         NOT NULL,
    fecha_devolucion_prevista  DATE         NOT NULL,
    motivo                     VARCHAR(200) NOT NULL,
    estado                     VARCHAR(15)  NOT NULL DEFAULT 'Pendiente',
    id_empleado                INT          NOT NULL,
    CONSTRAINT ck_solicitud_estado CHECK (estado IN ('Pendiente','Aprobada','Entregada','Devuelta','Cancelada')),
    CONSTRAINT ck_solicitud_uso CHECK (fecha_uso >= fecha_solicitud),
    CONSTRAINT ck_solicitud_dev CHECK (fecha_devolucion_prevista >= fecha_uso),
    CONSTRAINT ck_solicitud_motivo CHECK (CHAR_LENGTH(TRIM(motivo)) > 0),
    CONSTRAINT fk_solicitud_empleado FOREIGN KEY (id_empleado)
        REFERENCES empleado(id_empleado) ON UPDATE CASCADE ON DELETE RESTRICT
) ENGINE=InnoDB;

-- ---------------------------------------------------------------------
-- DETALLE_SOLICITUD (DETALLE)
-- ---------------------------------------------------------------------
CREATE TABLE detalle_solicitud (
    id_detalle    INT AUTO_INCREMENT PRIMARY KEY,
    id_solicitud  INT NOT NULL,
    id_equipo     INT NOT NULL,
    CONSTRAINT uq_detalle UNIQUE (id_solicitud, id_equipo),
    CONSTRAINT fk_detalle_solicitud FOREIGN KEY (id_solicitud)
        REFERENCES solicitud(id_solicitud) ON UPDATE CASCADE ON DELETE CASCADE,
    CONSTRAINT fk_detalle_equipo FOREIGN KEY (id_equipo)
        REFERENCES equipo(id_equipo) ON UPDATE CASCADE ON DELETE RESTRICT
) ENGINE=InnoDB;

-- =====================================================================
-- DATOS DE PRUEBA
-- =====================================================================
INSERT INTO empleado (nombre, apellido, departamento, correo) VALUES
('María',  'López',     'Recursos Humanos', 'maria.lopez@institucion.gt'),
('Carlos', 'Pérez',     'Finanzas',         'carlos.perez@institucion.gt'),
('Ana',    'Gómez',     'Comunicaciones',   'ana.gomez@institucion.gt'),
('Luis',   'Ramírez',   'Tecnología',       'luis.ramirez@institucion.gt'),
('Sofía',  'Hernández', 'Dirección',        'sofia.hernandez@institucion.gt');

INSERT INTO categoria (nombre) VALUES ('Audio'), ('Video'), ('Computación'), ('Accesorios');

INSERT INTO equipo (codigo, nombre, marca, modelo, numero_serie, estado, id_categoria) VALUES
('PRY-001','Proyector',            'Epson',      'EB-X06',        'SN-EP-0001','Disponible',    2),
('PRY-002','Proyector',            'BenQ',       'MS550',         'SN-BQ-0002','Disponible',    2),
('MIC-001','Micrófono inalámbrico','Shure',      'SM58',          'SN-SH-0003','Disponible',    1),
('MIC-002','Micrófono de solapa',  'Sennheiser', 'EW112',         'SN-SE-0004','Prestado',      1),
('BOC-001','Bocina portátil',      'JBL',        'PartyBox 110',  'SN-JB-0005','Disponible',    1),
('BOC-002','Bocina',               'Bose',       'S1 Pro',        'SN-BO-0006','Disponible',    1),
('CAM-001','Cámara',               'Canon',      'EOS R50',       'SN-CA-0007','Disponible',    2),
('CAM-002','Cámara web',           'Logitech',   'C920',          'SN-LG-0008','Disponible',    2),
('LAP-001','Computadora portátil', 'Dell',       'Latitude 5420', 'SN-DL-0009','Prestado',      3),
('LAP-002','Computadora portátil', 'HP',         'ProBook 450',   'SN-HP-0010','Disponible',    3),
('PAN-001','Pantalla',             'Samsung',    'QN55',          'SN-SM-0011','Disponible',    2),
('TRI-001','Trípode',              'Manfrotto',  'MT190',         'SN-MF-0012','Disponible',    4),
('TRI-002','Trípode',              'Neewer',     'NW-190',        'SN-NW-0013','Disponible',    4),
('CAB-001','Cable HDMI 10 m',      'Steren',     'HDMI-10',       'SN-ST-0014','Disponible',    4),
('CAB-002','Cable VGA 5 m',        'Steren',     'VGA-5',         'SN-ST-0015','Mantenimiento', 4);

INSERT INTO solicitud (fecha_solicitud, fecha_uso, fecha_devolucion_prevista, motivo, estado, id_empleado) VALUES
('2026-09-28','2026-10-01','2026-10-02','Capacitación de inducción', 'Devuelta',  1),
('2026-09-30','2026-10-02','2026-10-03','Presentación trimestral',   'Devuelta',  2),
('2026-10-01','2026-10-05','2026-10-06','Evento institucional',      'Devuelta',  3),
('2026-10-02','2026-10-06','2026-10-07','Grabación de video',        'Devuelta',  4),
('2026-10-05','2026-10-08','2026-10-09','Reunión con proveedores',   'Devuelta',  5),
('2026-10-06','2026-10-09','2026-10-12','Taller de liderazgo',       'Entregada', 1),
('2026-10-07','2026-10-14','2026-10-15','Conferencia anual',         'Aprobada',  2),
('2026-10-08','2026-10-16','2026-10-16','Capacitación de seguridad', 'Pendiente', 3),
('2026-10-08','2026-10-20','2026-10-21','Reunión de directivos',     'Pendiente', 4),
('2026-10-09','2026-10-12','2026-10-13','Actividad de integración',  'Cancelada', 5);

INSERT INTO detalle_solicitud (id_solicitud, id_equipo) VALUES
(1,1),(1,3),(2,10),(2,14),(3,5),(3,6),(4,7),(4,12),(5,2),
(6,9),(6,4),(7,1),(7,5),(7,3),(8,11),(9,10),(10,6);

-- =====================================================================
-- CONSULTAS (SELECT) requeridas
-- =====================================================================
-- 1. Todos los empleados
SELECT * FROM empleado;
-- 2. Todos los equipos
SELECT e.*, c.nombre AS categoria FROM equipo e JOIN categoria c ON c.id_categoria = e.id_categoria;
-- 3. Equipos disponibles
SELECT * FROM equipo WHERE estado = 'Disponible';
-- 4. Solicitudes registradas
SELECT s.*, CONCAT(e.nombre,' ',e.apellido) AS empleado
FROM solicitud s JOIN empleado e ON e.id_empleado = s.id_empleado;
