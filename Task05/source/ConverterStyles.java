import java.awt.*;
import java.awt.event.*;
import java.util.Locale;
import javax.swing.*;

public class ConverterStyles extends JFrame {
    final JTextField amount = new JTextField(9);
    final JComboBox<String> from = new JComboBox<>(new String[]{"INR", "USD", "EUR"});
    final JComboBox<String> to = new JComboBox<>(new String[]{"USD", "EUR", "INR"});
    final JButton button = new JButton("Convert");
    final JLabel result = new JLabel("Enter an amount");

    private static final class FrameListener extends ConverterStyles implements ActionListener {
        FrameListener(boolean changed) { super("Frame listener", changed); register(this); }
        @Override public void actionPerformed(ActionEvent e) { convert(); }
    }

    private final class NamedListener implements ActionListener {
        @Override public void actionPerformed(ActionEvent e) { convert(); }
    }

    private final boolean changed;

    private ConverterStyles(String style, boolean changed) {
        super("Converter - " + style);
        this.changed = changed;
        setLayout(new FlowLayout());
        add(new JLabel("Amount")); add(amount); add(from);
        add(new JLabel("to")); add(to); add(button); add(result);
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override public void windowClosing(WindowEvent e) { dispose(); }
        });
        setSize(620, 105);
    }

    final void register(ActionListener listener) {
        button.addActionListener(listener);
        if (changed) amount.addActionListener(listener);
    }

    final void convert() {
        String message;
        try {
            double n = Double.parseDouble(amount.getText().trim());
            double value = n / rate((String) from.getSelectedItem()) * rate((String) to.getSelectedItem());
            if (n < 0 || !Double.isFinite(value)) throw new NumberFormatException();
            message = String.format(Locale.ROOT, "%.2f %s", value, to.getSelectedItem());
        } catch (NumberFormatException e) { message = "Invalid amount"; }
        result.setText(message);
        if (changed) JOptionPane.showMessageDialog(this, message, "Conversion", JOptionPane.INFORMATION_MESSAGE);
    }

    private static double rate(String currency) {
        return currency.equals("INR") ? 1 : currency.equals("USD") ? 0.012 : 0.011;
    }

    static ConverterStyles create(String style, boolean changed) {
        if (style.equals("frame")) return new FrameListener(changed);
        ConverterStyles window = new ConverterStyles(style, changed);
        switch (style) {
            case "named": window.register(window.new NamedListener()); break;
            case "anonymous": window.register(new ActionListener() {
                @Override public void actionPerformed(ActionEvent e) { window.convert(); }
            }); break;
            case "lambda": window.register(e -> window.convert()); break;
            default: throw new IllegalArgumentException("Choose frame, named, anonymous, or lambda");
        }
        return window;
    }

    public static void main(String[] args) {
        String style = args.length == 0 ? "lambda" : args[0];
        boolean changed = args.length < 2 || !args[1].equals("original");
        SwingUtilities.invokeLater(() -> create(style, changed).setVisible(true));
    }
}
