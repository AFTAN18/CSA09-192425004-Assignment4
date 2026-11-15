import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;

public final class AppointmentDesk extends JFrame {
    final JTextField patient = new JTextField(12);
    final JComboBox<String> department = new JComboBox<>(new String[]{"General", "Dental"});
    final JComboBox<String> doctor = new JComboBox<>();
    final JComboBox<String> slot = new JComboBox<>(new String[]{"09:00", "10:30", "14:00"});
    final JButton book = new JButton("Book");
    final JLabel status = new JLabel("Ready");
    private final Map<String,String[]> doctors = new HashMap<>();
    private final Set<String> reserved = new HashSet<>();
    private boolean booking;

    AppointmentDesk() {
        super("Hospital Appointment Desk");
        doctors.put("General", new String[]{"Dr Iyer", "Dr Roy"});
        doctors.put("Dental", new String[]{"Dr Khan"});
        JPanel form = new JPanel(new GridLayout(4, 2, 8, 8));
        form.add(new JLabel("Patient ID")); form.add(patient);
        form.add(new JLabel("Department")); form.add(department);
        form.add(new JLabel("Doctor")); form.add(doctor);
        form.add(new JLabel("Slot")); form.add(slot);
        JPanel actions = new JPanel(new FlowLayout());
        actions.add(book); actions.add(status);
        add(form, BorderLayout.CENTER); add(actions, BorderLayout.SOUTH);
        department.addActionListener(e -> refresh());
        book.addActionListener(e -> beginBooking());
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override public void windowOpened(WindowEvent e) { System.out.println("windowOpened"); }
            @Override public void windowClosing(WindowEvent e) {
                System.out.println("windowClosing");
                if (!booking || JOptionPane.showConfirmDialog(AppointmentDesk.this, "Close while booking?",
                        "Confirm close", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) dispose();
            }
            @Override public void windowClosed(WindowEvent e) { System.out.println("windowClosed"); }
        });
        refresh();
        setSize(460, 210);
    }

    private void refresh() {
        doctor.setModel(new DefaultComboBoxModel<>(doctors.get(department.getSelectedItem())));
        System.out.println("refreshDoctors");
    }

    private void beginBooking() {
        if (!patient.getText().trim().matches("[A-Za-z0-9]{1,16}")) { status.setText("Invalid patient ID"); return; }
        String key = doctor.getSelectedItem() + " / " + slot.getSelectedItem();
        booking = true; book.setEnabled(false); status.setText("Booking");
        javax.swing.Timer finish = new javax.swing.Timer(300, e -> {
            status.setText(reserved.add(key) ? "Confirmed: " + key : "Slot unavailable");
            booking = false; book.setEnabled(true);
            System.out.println("bookingCompleted");
        });
        finish.setRepeats(false);
        finish.start();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new AppointmentDesk().setVisible(true));
    }
}
