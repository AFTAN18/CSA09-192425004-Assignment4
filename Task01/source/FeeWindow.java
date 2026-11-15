import java.awt.*;
import java.math.BigDecimal;
import java.sql.*;
import javax.swing.*;

class FeeService {
    interface Ledger { void record(String registration, BigDecimal amount) throws SQLException; }
    private final Ledger ledger;

    FeeService(Ledger ledger) { this.ledger = ledger; }

    String pay(String registration, String input) throws SQLException {
        String id = registration.trim();
        if (!id.matches("[0-9]{5,20}")) throw new IllegalArgumentException("Enter a valid registration number");
        BigDecimal amount;
        try { amount = new BigDecimal(input.trim()).setScale(2, java.math.RoundingMode.UNNECESSARY); }
        catch (ArithmeticException | NumberFormatException e) { throw new IllegalArgumentException("Enter a valid amount with up to two decimals"); }
        if (amount.signum() <= 0 || amount.compareTo(new BigDecimal("99999999.99")) > 0) {
            throw new IllegalArgumentException("Amount outside allowed range");
        }
        ledger.record(id, amount);
        return id + " paid INR " + amount.toPlainString();
    }

    static Ledger jdbc(String url, String user, String password) {
        return (id, amount) -> {
            try (Connection c = DriverManager.getConnection(url, user, password);
                    PreparedStatement stmt = c.prepareStatement("INSERT INTO fees(reg_no, amount) VALUES (?, ?)")) {
                stmt.setString(1, id);
                stmt.setBigDecimal(2, amount);
                stmt.executeUpdate();
            }
        };
    }
}

public final class FeeWindow extends JFrame {
    final JTextField registration = new JTextField(13);
    final JTextField amount = new JTextField(10);
    final JButton pay = new JButton("Pay");
    final JButton clear = new JButton("Clear");
    final JButton receipt = new JButton("Receipt");
    final JLabel status = new JLabel("Enter registration and amount");
    private final FeeService service;
    private String lastReceipt;

    FeeWindow(FeeService service) {
        super("Campus Fee Payment");
        this.service = service;
        JPanel form = new JPanel(new FlowLayout());
        form.add(new JLabel("Registration")); form.add(registration);
        form.add(new JLabel("Amount (INR)")); form.add(amount);
        form.add(pay); form.add(clear); form.add(receipt);
        add(form, BorderLayout.CENTER); add(status, BorderLayout.SOUTH);
        pay.addActionListener(e -> beginPayment());
        clear.addActionListener(e -> { registration.setText(""); amount.setText(""); status.setText("Cleared"); });
        receipt.addActionListener(e -> JOptionPane.showMessageDialog(this,
                lastReceipt == null ? "No payment recorded" : lastReceipt));
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        pack();
    }

    private void beginPayment() {
        String id = registration.getText();
        String value = amount.getText();
        pay.setEnabled(false);
        status.setText("Processing");
        new SwingWorker<String, Void>() {
            @Override protected String doInBackground() throws Exception { return service.pay(id, value); }
            @Override protected void done() {
                try {
                    lastReceipt = get();
                    status.setText(lastReceipt);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    status.setText("Interrupted");
                } catch (java.util.concurrent.ExecutionException e) {
                    Throwable cause = e.getCause();
                    status.setText(cause instanceof IllegalArgumentException ? cause.getMessage() : "Payment unavailable");
                } finally { pay.setEnabled(true); }
            }
        }.execute();
    }

    public static void main(String[] args) {
        String url = System.getenv("CSA09_JDBC_URL");
        FeeService.Ledger ledger = url == null ? (id, value) -> { }
                : FeeService.jdbc(url, System.getenv("CSA09_DB_USER"), System.getenv("CSA09_DB_PASSWORD"));
        SwingUtilities.invokeLater(() -> new FeeWindow(new FeeService(ledger)).setVisible(true));
    }
}
