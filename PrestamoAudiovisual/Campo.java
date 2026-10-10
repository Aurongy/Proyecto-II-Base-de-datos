package prestamo;

/** Describe un campo del formulario CRUD genérico. */
public class Campo {
    public enum Tipo { TEXTO, FECHA, OPCION, COMBO }

    public final String clave, etiqueta;
    public final Tipo tipo;
    public final String[] opciones;   // para OPCION
    public final String sqlItems;     // para COMBO: SELECT id, texto ...

    private Campo(String clave, String etiqueta, Tipo tipo, String[] opciones, String sqlItems) {
        this.clave = clave; this.etiqueta = etiqueta; this.tipo = tipo;
        this.opciones = opciones; this.sqlItems = sqlItems;
    }
    public static Campo texto(String c, String e) { return new Campo(c, e, Tipo.TEXTO, null, null); }
    public static Campo fecha(String c, String e) { return new Campo(c, e, Tipo.FECHA, null, null); }
    public static Campo opcion(String c, String e, String... o) { return new Campo(c, e, Tipo.OPCION, o, null); }
    public static Campo combo(String c, String e, String sql) { return new Campo(c, e, Tipo.COMBO, null, sql); }
}
