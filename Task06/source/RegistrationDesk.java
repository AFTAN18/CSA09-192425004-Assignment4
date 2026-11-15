import java.awt.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.function.Consumer;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

final class Registration {
    final String registration, name, event;
    Registration(String registration, String name, String event) {
        this.registration = registration; this.name = name; this.event = event;
    }
}

final class RegistrationDao {
    private final String url, user, password;
    RegistrationDao(String url, String user, String password) {
        this.url = url; this.user = user; this.password = password;
    }
    private Connection open() throws SQLException { return DriverManager.getConnection(url, user, password); }

    void add(Registration row) throws SQLException {
        try (Connection c = open();
             PreparedStatement ps = c.prepareStatement("INSERT INTO registrations(reg_no, student_name, event_name) VALUES(?,?,?)")) {
            ps.setString(1, row.registration); ps.setString(2, row.name); ps.setString(3, row.event);
            ps.executeUpdate();
        }
    }

    List<Registration> page(String event, int limit, long offset) throws SQLException {
        String sql = "SELECT reg_no, student_name, event_name FROM registrations"
                + (event.equals("All") ? "" : " WHERE event_name=?") + " ORDER BY id LIMIT ? OFFSET ?";
        try (Connection c = open(); PreparedStatement ps = c.prepareStatement(sql)) {
            int param = 1;
            if (!event.equals("All")) ps.setString(param++, event);
            ps.setInt(param++, limit);
            ps.setLong(param, offset);
            try (ResultSet rs = ps.executeQuery()) {
                List<Registration> records = new ArrayList<>();
                while (rs.next()) records.add(new Registration(rs.getString(1), rs.getString(2), rs.getString(3)));
                return records;
            }
        }
    }

    int export(String event, Path path) throws SQLException, IOException {
        String sql = "SELECT reg_no, student_name, event_name FROM registrations"
                + (event.equals("All") ? "" : " WHERE event_name=?") + " ORDER BY id";
        try (Connection c = open(); PreparedStatement ps = c.prepareStatement(sql);
                BufferedWriter writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8)) {
            if (!event.equals("All")) ps.setString(1, event);
            ps.setFetchSize(100);
            writer.write("registration,name,event"); writer.newLine();
            int count = 0;
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    writer.write(csv(rs.getString(1)) + "," + csv(rs.getString(2)) + "," + csv(rs.getString(3)));
                    writer.newLine(); count++;
                }
            }
            return count;
        }
    }

    private static String csv(String text) {
        if (!text.isEmpty() && "=+-@".indexOf(text.charAt(0)) >= 0) text = "'" + text;
        return "\"" + text.replace("\"", "\"\"") + "\"";
    }
}

final class RegistrationService {
    static final String[] EVENTS = {"Music", "Dance", "Quiz"};
    private final RegistrationDao dao;
    RegistrationService(RegistrationDao dao) { this.dao = dao; }

    void register(String id, String name, String event) throws SQLException {
        id = id.trim();
        name = name.trim();
        if (!id.matches("[0-9]{5,20}")) throw new IllegalArgumentException("Invalid registration number");
        if (name.isEmpty() || name.length() > 100 || name.chars().anyMatch(Character::isISOControl)) {
            throw new IllegalArgumentException("Enter a valid student name");
        }
        if (!Arrays.asList(EVENTS).contains(event)) throw new IllegalArgumentException("Select an event");
        dao.add(new Registration(id, name, event));
    }

    List<Registration> page(String event, int limit, long offset) throws SQLException { return dao.page(event, limit, offset); }
    int export(String event, Path file) throws SQLException, IOException { return dao.export(event, file); }
}

public final class RegistrationDesk extends JFrame {
    static final int PAGE = 25;
    final JTextField id = new JTextField(13), name = new JTextField(18);
    final JComboBox<String> event = new JComboBox<>(RegistrationService.EVENTS);
    final JComboBox<String> filter = new JComboBox<>(new String[]{"All","Music","Dance","Quiz"});
    final JButton register = new JButton("Register"), prev = new JButton("Prev"), next = new JButton("Next");
    final JLabel status = new JLabel("Ready");
    final DefaultTableModel table = new DefaultTableModel(new String[]{"Registration","Name","Event"}, 0) {
        @Override public boolean isCellEditable(int row, int col) { return false; }
    };
    private final RegistrationService service;
    private long offset;
    private boolean more, busy;
    private final JMenuItem export = new JMenuItem("Export CSV");

    RegistrationDesk(RegistrationService service) {
        super("Campus Event Registration");
        this.service = service;
        JPanel form = new JPanel(new FlowLayout());
        form.add(new JLabel("Registration")); form.add(id);
        form.add(new JLabel("Name")); form.add(name); form.add(event); form.add(register);
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.LEFT));
        footer.add(new JLabel("Event filter")); footer.add(filter);
        footer.add(prev); footer.add(next); footer.add(status);
        add(form, BorderLayout.NORTH); add(new JScrollPane(new JTable(table)), BorderLayout.CENTER);
        add(footer, BorderLayout.SOUTH);
        JMenuBar bar = new JMenuBar();
        JMenu file = new JMenu("File");
        JMenuItem exit = new JMenuItem("Close");
        file.add(export); file.add(exit); bar.add(file); setJMenuBar(bar);
        exit.addActionListener(e -> dispose());
        export.addActionListener(e -> chooseExport());
        register.addActionListener(e -> {
            String registration = id.getText(), student = name.getText(), category = (String) event.getSelectedItem();
            worker(() -> { service.register(registration, student, category); return null; },
                    unused -> refresh(0, "Registration saved"));
        });
        filter.addActionListener(e -> refresh(0, "Page 1"));
        prev.addActionListener(e -> refresh(Math.max(0L, offset - PAGE), "Previous page"));
        next.addActionListener(e -> refresh(offset + PAGE, "Next page"));
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(780, 400);
        refresh(0, "Page 1");
    }

    private void chooseExport() {
        JFileChooser chooser = new JFileChooser();
        if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            Path target = chooser.getSelectedFile().toPath();
            String category = (String) filter.getSelectedItem();
            worker(() -> service.export(category, target), count -> status.setText("Exported " + count + " rows"));
        }
    }

    private void refresh(long pageOffset, String message) {
        String category = (String) filter.getSelectedItem();
        worker(() -> service.page(category, PAGE + 1, pageOffset), rows -> {
            offset = pageOffset;
            more = rows.size() > PAGE;
            table.setRowCount(0);
            for (Registration row : rows.subList(0, Math.min(PAGE, rows.size()))) {
                table.addRow(new Object[]{row.registration, row.name, row.event});
            }
            status.setText(message);
        });
    }

    private void controls(boolean running) {
        busy = running;
        register.setEnabled(!busy); filter.setEnabled(!busy); export.setEnabled(!busy);
        prev.setEnabled(!busy && offset > 0); next.setEnabled(!busy && more);
    }

    private <T> void worker(Callable<T> task, Consumer<T> success) {
        if (busy) return;
        controls(true);
        status.setText("Working");
        new SwingWorker<T, Void>() {
            @Override protected T doInBackground() throws Exception { return task.call(); }
            @Override protected void done() {
                T result;
                try { result = get(); }
                catch (InterruptedException e) {
                    Thread.currentThread().interrupt(); status.setText("Interrupted"); controls(false); return;
                } catch (java.util.concurrent.ExecutionException e) {
                    Throwable cause = e.getCause();
                    if (cause instanceof IllegalArgumentException) status.setText(cause.getMessage());
                    else if (cause instanceof SQLException && ((SQLException) cause).getErrorCode() == 1062) {
                        status.setText("Already registered for this event");
                    } else status.setText("Database or export unavailable");
                    controls(false); return;
                }
                controls(false);
                success.accept(result);
                if (!busy) controls(false);
            }
        }.execute();
    }

    static RegistrationService configured() {
        String url = System.getenv("CSA09_JDBC_URL");
        if (url == null) throw new IllegalStateException("Set CSA09_JDBC_URL and database credentials");
        return new RegistrationService(new RegistrationDao(url, System.getenv("CSA09_DB_USER"),
                System.getenv("CSA09_DB_PASSWORD")));
    }
    public static void main(String[] args) {
        RegistrationService service = configured();
        SwingUtilities.invokeLater(() -> new RegistrationDesk(service).setVisible(true));
    }
}
