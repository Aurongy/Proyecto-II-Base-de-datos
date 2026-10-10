package prestamo;

import javax.swing.*;
import java.awt.*;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/** Solicitudes de préstamo: estructura MAESTRO (solicitud) - DETALLE (equipos). */
public class SolicitudPanel extends JPanel implements Refrescable {
    static final String Q_SOLICITUDES =
        "SELECT s.id_solicitud AS ID, s.fecha_solicitud AS 'Fecha solicitud', s.fecha_uso AS 'Fecha uso', "
      + "s.fecha_devolucion_prevista AS 'Devolución', s.motivo AS Motivo, s.estado AS Estado, "
      + "CONCAT(e.nombre,' ',e.apellido) AS Empleado, "
      + "(SELECT COUNT(*) FROM detalle_solicitud d WHERE d.id_solicitud = s.id_solicitud) AS 'No. equipos' "
      + "FROM solicitud s JOIN empleado e ON e.id_empleado = s.id_empleado ORDER BY s.id_solicitud";

    private final JComboBox<Object> cbEmp = new JComboBox<>();
    private final JComboBox<Object> cbEstado = new JComboBox<>();
    private final JComboBox<Object> cbEquipo = new JComboBox<>();
    private final JTextField tfSol = new JTextField(10), tfUso = new JTextField(10),
                             tfDev = new JTextField(10), tfMotivo = new JTextField(28);
    private final JTable gridMaestro = Util.crearTabla(), gridDetalle = Util.crearTabla();
    private final List<Integer> detalle = new ArrayList<>();   // equipos en memoria hasta guardar
    private Integer actualId = null;

    public SolicitudPanel() {
        setLayout(new BorderLayout(6, 6));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        for (String e : Util.ESTADOS_SOLICITUD) cbEstado.addItem(e);
        cbEmp.setPreferredSize(new Dimension(250, 26));
        cbEquipo.setPreferredSize(new Dimension(320, 26));

        // ---------- MAESTRO
        JPanel m = new JPanel(new GridBagLayout());
        m.setBorder(BorderFactory.createTitledBorder("Solicitud (Maestro)"));
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(3, 6, 3, 6);
        g.anchor = GridBagConstraints.WEST;
        fila(m, g, 0, "Empleado *", cbEmp, "Motivo *", tfMotivo);
        fila(m, g, 1, "Fecha solicitud *", tfSol, "Estado *", cbEstado);
        fila(m, g, 2, "Fecha de uso *", tfUso, "Devolución prevista *", tfDev);

        // ---------- DETALLE
        JPanel d = new JPanel(new BorderLayout(4, 4));
        d.setBorder(BorderFactory.createTitledBorder("Equipos solicitados (Detalle)"));
        JPanel dTop = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton bAdd = new JButton("Agregar equipo");
        JButton bDel = new JButton("Quitar equipo seleccionado");
        bAdd.addActionListener(e -> agregarDetalle());
        bDel.addActionListener(e -> quitarDetalle());
        dTop.add(new JLabel("Equipo disponible:")); dTop.add(cbEquipo); dTop.add(bAdd); dTop.add(bDel);
        d.add(dTop, BorderLayout.NORTH);
        JScrollPane spDet = new JScrollPane(gridDetalle);
        spDet.setPreferredSize(new Dimension(100, 100));
        d.add(spDet, BorderLayout.CENTER);

        JPanel arriba = new JPanel(new BorderLayout(4, 4));
        arriba.add(m, BorderLayout.NORTH);
        arriba.add(d, BorderLayout.CENTER);
        add(arriba, BorderLayout.NORTH);

        // ---------- listado de solicitudes
        add(new JScrollPane(gridMaestro), BorderLayout.CENTER);
        gridMaestro.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && gridMaestro.getSelectedRow() >= 0) cargarSeleccion();
        });

        // ---------- botones
        JPanel b = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton bNueva = new JButton("Nueva / Limpiar");
        JButton bGuardar = new JButton("Guardar solicitud");
        JButton bElim = new JButton("Eliminar solicitud (solo Canceladas)");
        bNueva.addActionListener(e -> limpiar());
        bGuardar.addActionListener(e -> guardar());
        bElim.addActionListener(e -> eliminar());
        b.add(bNueva); b.add(bGuardar); b.add(bElim);
        add(b, BorderLayout.SOUTH);

        refrescar();
        limpiar();
    }

    private void fila(JPanel p, GridBagConstraints g, int r, String l1, JComponent c1, String l2, JComponent c2) {
        g.gridy = r;
        g.gridx = 0; p.add(new JLabel(l1), g);
        g.gridx = 1; p.add(c1, g);
        g.gridx = 2; p.add(new JLabel(l2), g);
        g.gridx = 3; p.add(c2, g);
    }

    // ------------------------------------------------------------ datos
    @Override public void refrescar() {
        try {
            Object sel = cbEmp.getSelectedItem();
            cbEmp.removeAllItems();
            for (Db.Item it : Db.items("SELECT id_empleado, CONCAT(nombre,' ',apellido) FROM empleado ORDER BY nombre"))
                cbEmp.addItem(it);
            if (sel instanceof Db.Item) Util.seleccionar(cbEmp, ((Db.Item) sel).id);
            refrescarEquipos();
            Util.llenar(gridMaestro, Db.consultar(Q_SOLICITUDES));
        } catch (SQLException ex) {
            Util.error(this, "Error de base de datos:\n" + Db.mensaje(ex));
        }
    }

    /** Combo con equipos Disponibles que aún no están en el detalle. */
    private void refrescarEquipos() throws SQLException {
        cbEquipo.removeAllItems();
        for (Db.Item it : Db.items("SELECT id_equipo, CONCAT(codigo,' ',nombre,' ',marca) FROM equipo "
                                 + "WHERE estado='Disponible' ORDER BY codigo"))
            if (!detalle.contains(it.id)) cbEquipo.addItem(it);
        cbEquipo.setSelectedIndex(-1);
    }

    private void pintarDetalle() throws SQLException {
        Db.Tabla t = new Db.Tabla();
        t.columnas = new String[]{"ID", "Código", "Nombre", "Marca", "Modelo", "Estado"};
        for (int id : detalle)
            t.filas.addAll(Db.consultar("SELECT id_equipo, codigo, nombre, marca, modelo, estado "
                                      + "FROM equipo WHERE id_equipo=?", id).filas);
        Util.llenar(gridDetalle, t);
    }

    private void agregarDetalle() {
        Object o = cbEquipo.getSelectedItem();
        if (!(o instanceof Db.Item)) { Util.aviso(this, "Seleccione un equipo."); return; }
        try {
            detalle.add(((Db.Item) o).id);
            pintarDetalle();
            refrescarEquipos();
        } catch (SQLException ex) { Util.error(this, Db.mensaje(ex)); }
    }

    private void quitarDetalle() {
        int r = gridDetalle.getSelectedRow();
        if (r < 0) { Util.aviso(this, "Seleccione un equipo del detalle."); return; }
        try {
            detalle.remove(Integer.valueOf(((Number) gridDetalle.getValueAt(r, 0)).intValue()));
            pintarDetalle();
            refrescarEquipos();
        } catch (SQLException ex) { Util.error(this, Db.mensaje(ex)); }
    }

    // ------------------------------------------------------------ formulario
    private void limpiar() {
        actualId = null;
        detalle.clear();
        cbEmp.setSelectedIndex(-1);
        String hoy = LocalDate.now().toString();
        tfSol.setText(hoy); tfUso.setText(hoy); tfDev.setText(hoy); tfMotivo.setText("");
        cbEstado.setSelectedItem("Pendiente");
        gridMaestro.clearSelection();
        try { pintarDetalle(); refrescarEquipos(); } catch (SQLException ex) { Util.error(this, Db.mensaje(ex)); }
    }

    private void cargarSeleccion() {
        try {
            actualId = ((Number) gridMaestro.getValueAt(gridMaestro.getSelectedRow(), 0)).intValue();
            Object[] r = Db.consultar("SELECT id_empleado, fecha_solicitud, fecha_uso, fecha_devolucion_prevista, "
                    + "motivo, estado FROM solicitud WHERE id_solicitud=?", actualId).filas.get(0);
            Util.seleccionar(cbEmp, ((Number) r[0]).intValue());
            tfSol.setText(String.valueOf(r[1])); tfUso.setText(String.valueOf(r[2]));
            tfDev.setText(String.valueOf(r[3])); tfMotivo.setText(String.valueOf(r[4]));
            cbEstado.setSelectedItem(String.valueOf(r[5]));
            detalle.clear();
            for (Object[] f : Db.consultar("SELECT id_equipo FROM detalle_solicitud WHERE id_solicitud=? "
                                         + "ORDER BY id_detalle", actualId).filas)
                detalle.add(((Number) f[0]).intValue());
            pintarDetalle();
            refrescarEquipos();
        } catch (SQLException ex) { Util.error(this, Db.mensaje(ex)); }
    }

    // ------------------------------------------------------------ guardar (transacción maestro + detalle)
    private void guardar() {
        Object emp = cbEmp.getSelectedItem();
        String fs = tfSol.getText().trim(), fu = tfUso.getText().trim(), fd = tfDev.getText().trim();
        String motivo = tfMotivo.getText().trim();
        String estado = String.valueOf(cbEstado.getSelectedItem());
        if (!(emp instanceof Db.Item) || motivo.isEmpty()) { Util.aviso(this, "Empleado y motivo son obligatorios."); return; }
        if (!Util.fechaValida(fs) || !Util.fechaValida(fu) || !Util.fechaValida(fd)) {
            Util.aviso(this, "Las fechas deben tener formato AAAA-MM-DD."); return;
        }
        if (fu.compareTo(fs) < 0 || fd.compareTo(fu) < 0) {
            Util.aviso(this, "La fecha de uso no puede ser anterior a la solicitud, ni la devolución anterior al uso.");
            return;
        }
        if (detalle.isEmpty()) { Util.aviso(this, "La solicitud debe incluir al menos un equipo."); return; }

        int idEmp = ((Db.Item) emp).id;
        try (Connection c = Db.conectar()) {
            c.setAutoCommit(false);
            try {
                int sid;
                if (actualId == null) {
                    try (PreparedStatement ps = c.prepareStatement(
                            "INSERT INTO solicitud (fecha_solicitud, fecha_uso, fecha_devolucion_prevista, "
                          + "motivo, estado, id_empleado) VALUES (?,?,?,?,?,?)", Statement.RETURN_GENERATED_KEYS)) {
                        ps.setDate(1, Date.valueOf(fs)); ps.setDate(2, Date.valueOf(fu)); ps.setDate(3, Date.valueOf(fd));
                        ps.setString(4, motivo); ps.setString(5, estado); ps.setInt(6, idEmp);
                        ps.executeUpdate();
                        try (ResultSet k = ps.getGeneratedKeys()) { k.next(); sid = k.getInt(1); }
                    }
                } else {
                    sid = actualId;
                    Db.ejecutar(c, "UPDATE solicitud SET fecha_solicitud=?, fecha_uso=?, fecha_devolucion_prevista=?, "
                            + "motivo=?, estado=?, id_empleado=? WHERE id_solicitud=?",
                            Date.valueOf(fs), Date.valueOf(fu), Date.valueOf(fd), motivo, estado, idEmp, sid);
                    // liberar equipos que esta solicitud tenía prestados
                    Db.ejecutar(c, "UPDATE equipo SET estado='Disponible' WHERE estado='Prestado' AND id_equipo IN "
                            + "(SELECT id_equipo FROM detalle_solicitud WHERE id_solicitud=?)", sid);
                    Db.ejecutar(c, "DELETE FROM detalle_solicitud WHERE id_solicitud=?", sid);
                }
                for (int eid : detalle)
                    Db.ejecutar(c, "INSERT INTO detalle_solicitud (id_solicitud, id_equipo) VALUES (?,?)", sid, eid);
                if (estado.equals("Entregada"))
                    for (int eid : detalle) Db.ejecutar(c, "UPDATE equipo SET estado='Prestado' WHERE id_equipo=?", eid);
                c.commit();
                Util.info(this, "Solicitud #" + sid + " guardada con " + detalle.size() + " equipo(s).");
            } catch (SQLException ex) {
                c.rollback();
                throw ex;
            }
            limpiar();
            refrescar();
        } catch (SQLException ex) {
            Util.error(this, "No se pudo guardar:\n" + Db.mensaje(ex));
        }
    }

    private void eliminar() {
        if (actualId == null) { Util.aviso(this, "Seleccione una solicitud."); return; }
        try {
            String estado = String.valueOf(Db.consultar("SELECT estado FROM solicitud WHERE id_solicitud=?", actualId)
                    .filas.get(0)[0]);
            if (!estado.equals("Cancelada")) { Util.aviso(this, "Solo se pueden eliminar solicitudes en estado Cancelada."); return; }
            if (!Util.confirmar(this, "¿Eliminar la solicitud y sus detalles?")) return;
            Db.actualizar("DELETE FROM solicitud WHERE id_solicitud=?", actualId);   // detalles: ON DELETE CASCADE
            limpiar();
            refrescar();
        } catch (SQLException ex) { Util.error(this, Db.mensaje(ex)); }
    }
}
