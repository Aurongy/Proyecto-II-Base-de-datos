package prestamo;

import javax.swing.*;
import java.awt.*;
import java.sql.SQLException;
import java.util.*;
import java.util.List;

/** Panel CRUD genérico: Empleados, Categorías y Equipos. */
public class CrudPanel extends JPanel implements Refrescable {
    private final String tabla, pk, sqlListado;
    private final Campo[] campos;
    private final Map<String, JComponent> comps = new LinkedHashMap<>();
    private final JTable grid = Util.crearTabla();
    private Integer actualId = null;

    public CrudPanel(String tabla, String pk, Campo[] campos, String sqlListado) {
        this.tabla = tabla; this.pk = pk; this.campos = campos; this.sqlListado = sqlListado;
        setLayout(new BorderLayout(8, 8));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // ---- formulario
        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(BorderFactory.createTitledBorder("Datos"));
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(4, 6, 4, 6);
        g.anchor = GridBagConstraints.WEST;
        for (int i = 0; i < campos.length; i++) {
            Campo c = campos[i];
            JComponent w = (c.tipo == Campo.Tipo.OPCION || c.tipo == Campo.Tipo.COMBO)
                    ? new JComboBox<Object>() : new JTextField(22);
            if (w instanceof JComboBox) w.setPreferredSize(new Dimension(250, 26));
            comps.put(c.clave, w);
            g.gridx = (i % 2) * 2; g.gridy = i / 2;
            form.add(new JLabel(c.etiqueta + " *"), g);
            g.gridx++;
            form.add(w, g);
        }
        add(form, BorderLayout.NORTH);

        // ---- listado
        add(new JScrollPane(grid), BorderLayout.CENTER);
        grid.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && grid.getSelectedRow() >= 0) cargarSeleccion();
        });

        // ---- botones
        JPanel b = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton nuevo = new JButton("Nuevo / Limpiar");
        JButton guardar = new JButton("Guardar (Insertar / Actualizar)");
        JButton eliminar = new JButton("Eliminar");
        nuevo.addActionListener(e -> limpiar());
        guardar.addActionListener(e -> guardar());
        eliminar.addActionListener(e -> eliminar());
        b.add(nuevo); b.add(guardar); b.add(eliminar);
        add(b, BorderLayout.SOUTH);

        refrescar();
        limpiar();
    }

    @SuppressWarnings("unchecked")
    @Override public void refrescar() {
        try {
            for (Campo c : campos) {
                if (c.tipo == Campo.Tipo.COMBO) {
                    JComboBox<Object> cb = (JComboBox<Object>) comps.get(c.clave);
                    Object sel = cb.getSelectedItem();
                    cb.removeAllItems();
                    for (Db.Item it : Db.items(c.sqlItems)) cb.addItem(it);
                    if (sel instanceof Db.Item) Util.seleccionar(cb, ((Db.Item) sel).id);
                } else if (c.tipo == Campo.Tipo.OPCION) {
                    JComboBox<Object> cb = (JComboBox<Object>) comps.get(c.clave);
                    if (cb.getItemCount() == 0) for (String o : c.opciones) cb.addItem(o);
                }
            }
            Util.llenar(grid, Db.consultar(sqlListado));
        } catch (SQLException ex) {
            Util.error(this, "Error de base de datos:\n" + Db.mensaje(ex));
        }
    }

    @SuppressWarnings("unchecked")
    private void limpiar() {
        actualId = null;
        for (Campo c : campos) {
            JComponent w = comps.get(c.clave);
            if (w instanceof JTextField) {
                ((JTextField) w).setText(c.tipo == Campo.Tipo.FECHA ? java.time.LocalDate.now().toString() : "");
            } else {
                ((JComboBox<Object>) w).setSelectedIndex(-1);
            }
        }
        JComponent est = comps.get("estado");
        if (est instanceof JComboBox) ((JComboBox<Object>) est).setSelectedIndex(0);
        grid.clearSelection();
    }

    @SuppressWarnings("unchecked")
    private void cargarSeleccion() {
        try {
            actualId = ((Number) grid.getValueAt(grid.getSelectedRow(), 0)).intValue();
            Db.Tabla t = Db.consultar("SELECT * FROM " + tabla + " WHERE " + pk + "=?", actualId);
            if (t.filas.isEmpty()) return;
            Object[] fila = t.filas.get(0);
            for (Campo c : campos) {
                int idx = Arrays.asList(t.columnas).indexOf(c.clave);
                Object val = fila[idx];
                JComponent w = comps.get(c.clave);
                if (c.tipo == Campo.Tipo.COMBO) Util.seleccionar((JComboBox<Object>) w, ((Number) val).intValue());
                else if (c.tipo == Campo.Tipo.OPCION) ((JComboBox<Object>) w).setSelectedItem(String.valueOf(val));
                else ((JTextField) w).setText(String.valueOf(val));
            }
        } catch (SQLException ex) {
            Util.error(this, Db.mensaje(ex));
        }
    }

    private void guardar() {
        List<Object> valores = new ArrayList<>();
        for (Campo c : campos) {
            JComponent w = comps.get(c.clave);
            Object v;
            if (w instanceof JTextField) {
                String s = ((JTextField) w).getText().trim();
                if (s.isEmpty()) { Util.aviso(this, "El campo '" + c.etiqueta + "' es obligatorio."); return; }
                if (c.tipo == Campo.Tipo.FECHA) {
                    if (!Util.fechaValida(s)) { Util.aviso(this, "'" + c.etiqueta + "' debe tener formato AAAA-MM-DD."); return; }
                    v = java.sql.Date.valueOf(s);
                } else v = s;
            } else {
                Object sel = ((JComboBox<?>) w).getSelectedItem();
                if (sel == null) { Util.aviso(this, "Seleccione un valor en '" + c.etiqueta + "'."); return; }
                v = (sel instanceof Db.Item) ? (Object) ((Db.Item) sel).id : sel.toString();
            }
            valores.add(v);
        }
        try {
            StringBuilder sql = new StringBuilder();
            if (actualId == null) {
                StringJoiner cols = new StringJoiner(", "), ph = new StringJoiner(", ");
                for (Campo c : campos) { cols.add(c.clave); ph.add("?"); }
                sql.append("INSERT INTO ").append(tabla).append(" (").append(cols).append(") VALUES (").append(ph).append(")");
            } else {
                StringJoiner sets = new StringJoiner(", ");
                for (Campo c : campos) sets.add(c.clave + "=?");
                sql.append("UPDATE ").append(tabla).append(" SET ").append(sets).append(" WHERE ").append(pk).append("=?");
                valores.add(actualId);
            }
            boolean nuevo = actualId == null;
            Db.actualizar(sql.toString(), valores.toArray());
            Util.info(this, nuevo ? "Registro insertado." : "Registro actualizado.");
            limpiar();
            refrescar();
        } catch (SQLException ex) {
            Util.error(this, Db.mensaje(ex));
        }
    }

    private void eliminar() {
        if (actualId == null) { Util.aviso(this, "Seleccione un registro de la tabla."); return; }
        if (!Util.confirmar(this, "¿Eliminar el registro seleccionado?")) return;
        try {
            Db.actualizar("DELETE FROM " + tabla + " WHERE " + pk + "=?", actualId);
            limpiar();
            refrescar();
        } catch (SQLException ex) {
            Util.error(this, Db.mensaje(ex));
        }
    }
}
