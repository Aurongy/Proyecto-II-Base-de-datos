package prestamo;

import javax.swing.*;

/** Sistema de Gestión de Préstamo de Equipo Audiovisual - Java Swing + JDBC + MySQL. */
public class Main {
    static final String Q_EMPLEADOS =
        "SELECT id_empleado AS ID, nombre AS Nombre, apellido AS Apellido, departamento AS Departamento, "
      + "correo AS Correo FROM empleado ORDER BY id_empleado";
    static final String Q_CATEGORIAS =
        "SELECT id_categoria AS ID, nombre AS Nombre FROM categoria ORDER BY id_categoria";
    static final String Q_EQUIPOS =
        "SELECT e.id_equipo AS ID, e.codigo AS Código, e.nombre AS Nombre, e.marca AS Marca, e.modelo AS Modelo, "
      + "e.numero_serie AS Serie, e.estado AS Estado, c.nombre AS Categoría "
      + "FROM equipo e JOIN categoria c ON c.id_categoria = e.id_categoria ORDER BY e.id_equipo";
    static final String Q_EQUIPOS_DISPONIBLES =
        "SELECT e.id_equipo AS ID, e.codigo AS Código, e.nombre AS Nombre, e.marca AS Marca, e.modelo AS Modelo, "
      + "c.nombre AS Categoría FROM equipo e JOIN categoria c ON c.id_categoria = e.id_categoria "
      + "WHERE e.estado = 'Disponible' ORDER BY e.id_equipo";

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); } catch (Exception ignored) { }
            try (java.sql.Connection c = Db.conectar()) {
                // conexión de prueba
            } catch (java.sql.SQLException ex) {
                JOptionPane.showMessageDialog(null,
                    "No se pudo conectar a MySQL:\n" + ex.getMessage()
                    + "\n\nRevise usuario/contraseña en Db.java y que haya ejecutado script_mysql.sql.",
                    "Error de conexión", JOptionPane.ERROR_MESSAGE);
                return;
            }

            JTabbedPane tabs = new JTabbedPane();
            tabs.addTab("Empleados", new CrudPanel("empleado", "id_empleado", new Campo[]{
                Campo.texto("nombre", "Nombre"), Campo.texto("apellido", "Apellido"),
                Campo.texto("departamento", "Departamento"), Campo.texto("correo", "Correo electrónico")},
                Q_EMPLEADOS));
            tabs.addTab("Categorías", new CrudPanel("categoria", "id_categoria", new Campo[]{
                Campo.texto("nombre", "Nombre")}, Q_CATEGORIAS));
            tabs.addTab("Equipos", new CrudPanel("equipo", "id_equipo", new Campo[]{
                Campo.texto("codigo", "Código"), Campo.texto("nombre", "Nombre"),
                Campo.texto("marca", "Marca"), Campo.texto("modelo", "Modelo"),
                Campo.texto("numero_serie", "Número de serie"),
                Campo.opcion("estado", "Estado", Util.ESTADOS_EQUIPO),
                Campo.combo("id_categoria", "Categoría", "SELECT id_categoria, nombre FROM categoria ORDER BY nombre")},
                Q_EQUIPOS));
            tabs.addTab("Solicitudes", new SolicitudPanel());
            tabs.addTab("Consultas", new ConsultaPanel());
            // al cambiar de pestaña se actualizan combos y listados
            tabs.addChangeListener(e -> ((Refrescable) tabs.getSelectedComponent()).refrescar());

            JFrame f = new JFrame("Gestión de Préstamo de Equipo Audiovisual");
            f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            f.add(tabs);
            f.setSize(1050, 720);
            f.setLocationRelativeTo(null);
            f.setVisible(true);
        });
    }
}
