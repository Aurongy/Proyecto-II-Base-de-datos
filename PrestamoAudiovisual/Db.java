package prestamo;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/** Conexión JDBC y utilidades de acceso a datos. */
public class Db {
    // ====== CONFIGURE AQUÍ su conexión a MySQL (o use variables de entorno) ======
    static final String URL = env("DB_URL",
        "jdbc:mysql://localhost:3306/prestamo_audiovisual"
        + "?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC&characterEncoding=UTF-8");
    static final String USER = env("DB_USER", "root");
    static final String PASS = env("DB_PASS", "auro");
    // ==============================================================================

    private static String env(String k, String def) {
        String v = System.getenv(k);
        return v == null ? def : v;
    }

    public static Connection conectar() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASS);
    }

    /** Resultado tabular de un SELECT. */
    public static class Tabla {
        public String[] columnas;
        public List<Object[]> filas = new ArrayList<>();
    }

    /** Elemento de combobox: muestra "id - texto". */
    public static class Item {
        public final int id;
        public final String texto;
        public Item(int id, String texto) { this.id = id; this.texto = texto; }
        @Override public String toString() { return id + " - " + texto; }
    }

    public static Tabla consultar(String sql, Object... p) throws SQLException {
        try (Connection c = conectar(); PreparedStatement ps = c.prepareStatement(sql)) {
            for (int i = 0; i < p.length; i++) ps.setObject(i + 1, p[i]);
            try (ResultSet rs = ps.executeQuery()) {
                ResultSetMetaData md = rs.getMetaData();
                int n = md.getColumnCount();
                Tabla t = new Tabla();
                t.columnas = new String[n];
                for (int i = 0; i < n; i++) t.columnas[i] = md.getColumnLabel(i + 1);
                while (rs.next()) {
                    Object[] f = new Object[n];
                    for (int i = 0; i < n; i++) f[i] = rs.getObject(i + 1);
                    t.filas.add(f);
                }
                return t;
            }
        }
    }

    public static int actualizar(String sql, Object... p) throws SQLException {
        try (Connection c = conectar()) {
            return ejecutar(c, sql, p);
        }
    }

    /** Ejecuta INSERT/UPDATE/DELETE sobre una conexión existente (para transacciones). */
    public static int ejecutar(Connection c, String sql, Object... p) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            for (int i = 0; i < p.length; i++) ps.setObject(i + 1, p[i]);
            return ps.executeUpdate();
        }
    }

    /** SELECT id, texto -> lista de Items para combobox. */
    public static List<Item> items(String sql) throws SQLException {
        List<Item> l = new ArrayList<>();
        for (Object[] f : consultar(sql).filas)
            l.add(new Item(((Number) f[0]).intValue(), String.valueOf(f[1])));
        return l;
    }

    /** Mensaje amigable según el código de error de MySQL. */
    public static String mensaje(SQLException e) {
        switch (e.getErrorCode()) {
            case 1062: return "Valor repetido: ya existe un registro con ese dato único (código, serie, correo, nombre...).";
            case 1451: return "No se puede eliminar: el registro está relacionado con otros datos.";
            case 1452: return "Referencia inválida: el registro relacionado no existe.";
            case 3819: case 4025: return "Un dato no cumple las reglas de validación (estado, fechas o formato).";
            default:   return e.getMessage();
        }
    }
}
