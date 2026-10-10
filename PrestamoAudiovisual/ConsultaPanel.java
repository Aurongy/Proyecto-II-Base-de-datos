package prestamo;

import javax.swing.*;
import java.awt.*;
import java.sql.SQLException;

/** Consultas SELECT requeridas por el enunciado. */
public class ConsultaPanel extends JPanel implements Refrescable {
    private final JTable grid = Util.crearTabla();

    public ConsultaPanel() {
        setLayout(new BorderLayout(6, 6));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        JPanel b = new JPanel(new FlowLayout(FlowLayout.LEFT));
        b.add(boton("Todos los empleados", Main.Q_EMPLEADOS));
        b.add(boton("Todos los equipos", Main.Q_EQUIPOS));
        b.add(boton("Equipos disponibles", Main.Q_EQUIPOS_DISPONIBLES));
        b.add(boton("Solicitudes registradas", SolicitudPanel.Q_SOLICITUDES));
        add(b, BorderLayout.NORTH);
        add(new JScrollPane(grid), BorderLayout.CENTER);
    }

    private JButton boton(String texto, String sql) {
        JButton bt = new JButton(texto);
        bt.addActionListener(e -> {
            try { Util.llenar(grid, Db.consultar(sql)); }
            catch (SQLException ex) { Util.error(this, Db.mensaje(ex)); }
        });
        return bt;
    }

    @Override public void refrescar() { }
}
