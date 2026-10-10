package prestamo;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;

public class Util {
    public static final String[] ESTADOS_EQUIPO = {"Disponible", "Prestado", "Mantenimiento", "Dañado"};
    public static final String[] ESTADOS_SOLICITUD = {"Pendiente", "Aprobada", "Entregada", "Devuelta", "Cancelada"};

    public static JTable crearTabla() {
        JTable t = new JTable(new DefaultTableModel());
        t.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        t.setFillsViewportHeight(true);
        t.setAutoCreateRowSorter(false);
        return t;
    }

    public static void llenar(JTable tabla, Db.Tabla datos) {
        DefaultTableModel m = new DefaultTableModel(datos.columnas, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        for (Object[] f : datos.filas) m.addRow(f);
        tabla.setModel(m);
    }

    public static boolean fechaValida(String s) {
        try { LocalDate.parse(s); return true; }
        catch (DateTimeParseException e) { return false; }
    }

    /** Selecciona en el combo el Item con ese id. */
    public static void seleccionar(JComboBox<Object> cb, int id) {
        for (int i = 0; i < cb.getItemCount(); i++) {
            Object o = cb.getItemAt(i);
            if (o instanceof Db.Item && ((Db.Item) o).id == id) { cb.setSelectedIndex(i); return; }
        }
    }

    public static void error(java.awt.Component p, String m) {
        JOptionPane.showMessageDialog(p, m, "Error", JOptionPane.ERROR_MESSAGE);
    }
    public static void aviso(java.awt.Component p, String m) {
        JOptionPane.showMessageDialog(p, m, "Validación", JOptionPane.WARNING_MESSAGE);
    }
    public static void info(java.awt.Component p, String m) {
        JOptionPane.showMessageDialog(p, m, "Información", JOptionPane.INFORMATION_MESSAGE);
    }
    public static boolean confirmar(java.awt.Component p, String m) {
        return JOptionPane.showConfirmDialog(p, m, "Confirmar", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION;
    }
}
