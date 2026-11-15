import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.Arrays;
import java.util.concurrent.FutureTask;
import java.util.concurrent.Callable;
import java.util.function.BooleanSupplier;
import javax.imageio.ImageIO;
import javax.swing.*;

public final class GuiEvidence {
    private static Robot robot;

    private static <T> T edt(Callable<T> action) throws Exception {
        FutureTask<T> result = new FutureTask<>(action);
        EventQueue.invokeAndWait(result);
        return result.get();
    }

    private static void ui(Runnable action) throws Exception {
        edt(() -> { action.run(); return null; });
    }

    private static void await(BooleanSupplier condition, String label) throws Exception {
        for (int i = 0; i < 50; i++) {
            if (edt(condition::getAsBoolean)) return;
            Thread.sleep(100);
        }
        throw new AssertionError(label);
    }

    private static <T extends Window> T display(T window) {
        window.setAlwaysOnTop(true);
        window.setLocation(45, 65);
        window.setVisible(true);
        window.toFront();
        return window;
    }

    private static BufferedImage capture(Window window, String task, String filename) throws Exception {
        robot.waitForIdle();
        Thread.sleep(250);
        Rectangle bounds = edt(() -> new Rectangle(window.getLocationOnScreen(), window.getSize()));
        Rectangle screen = new Rectangle(Toolkit.getDefaultToolkit().getScreenSize());
        if (!screen.contains(bounds)) throw new AssertionError("Window outside primary screen: " + bounds);
        BufferedImage image = robot.createScreenCapture(bounds);
        int center = image.getRGB(bounds.width / 2, bounds.height / 2);
        boolean variation = false;
        for (int y = 0; y < image.getHeight() && !variation; y += 4) {
            for (int x = 0; x < image.getWidth(); x += 4) {
                if (image.getRGB(x, y) != center) { variation = true; break; }
            }
        }
        if (!variation) throw new AssertionError("Uniform capture surface");
        Path evidence = Paths.get(System.getProperty("csa09.gui.capture", ""), task, "screenshots");
        Files.createDirectories(evidence);
        ImageIO.write(image, "png", evidence.resolve(filename + ".png").toFile());
        return image;
    }

    private static void fee() throws Exception {
        FeeWindow frame = edt(() -> display(new FeeWindow(new FeeService((id, amount) -> { }))));
        capture(frame, "Task01", "launch");
        ui(() -> frame.pay.doClick());
        await(() -> frame.status.getText().contains("valid registration"), "Invalid fee");
        capture(frame, "Task01", "invalid_registration");
        ui(() -> { frame.registration.setText("90001"); frame.amount.setText("150.50"); frame.pay.doClick(); });
        await(() -> frame.status.getText().contains("150.50"), "Valid fee");
        capture(frame, "Task01", "successful_payment");
        ui(() -> EventQueue.invokeLater(() -> frame.receipt.doClick()));
        JDialog receipt = dialog(frame);
        capture(receipt, "Task01", "receipt_dialog");
        ui(() -> { receipt.dispose(); frame.clear.doClick(); });
        if (!edt(() -> frame.registration.getText().isEmpty() && frame.amount.getText().isEmpty())) {
            throw new AssertionError("Clear button did not clear fields");
        }
        ui(frame::dispose);
        System.out.println("Fee: invalid registration, payment, receipt and clear verified.");
    }

    private static void appointments() throws Exception {
        AppointmentDesk frame = edt(() -> display(new AppointmentDesk()));
        capture(frame, "Task02", "launch");
        ui(() -> frame.book.doClick());
        if (!edt(() -> frame.status.getText().equals("Invalid patient ID"))) throw new AssertionError();
        capture(frame, "Task02", "invalid_patient");
        ui(() -> { frame.department.setSelectedItem("Dental"); frame.patient.setText("P100"); frame.book.doClick(); });
        await(() -> frame.status.getText().startsWith("Confirmed"), "Appointment completion");
        if (!edt(() -> frame.doctor.getSelectedItem().equals("Dr Khan"))) throw new AssertionError();
        capture(frame, "Task02", "successful_booking");
        ui(() -> {
            frame.slot.setSelectedItem("10:30");
            frame.book.doClick();
            EventQueue.invokeLater(() -> frame.dispatchEvent(new WindowEvent(frame, WindowEvent.WINDOW_CLOSING)));
        });
        JDialog confirmation = dialog(frame);
        capture(confirmation, "Task02", "confirm_close_during_booking");
        ui(confirmation::dispose);
        if (!edt(frame::isShowing)) throw new AssertionError("Closing confirmation disposed the main window");
        await(() -> frame.book.isEnabled(), "Second booking");
        ui(() -> frame.dispatchEvent(new WindowEvent(frame, WindowEvent.WINDOW_CLOSING)));
        System.out.println("Appointments: department refresh, rejection, booking, close confirmation and cancellation verified.");
    }

    private static void library() throws Exception {
        LibrarySearch frame = edt(() -> display(new LibrarySearch()));
        ui(() -> { frame.search.setText("Java"); frame.find.doClick(); frame.next.doClick(); frame.reserve.doClick(); });
        int previousWidth = 0, previousHeight = 0;
        Dimension control = null;
        for (int[] size : new int[][]{{480, 280}, {640, 370}, {820, 500}}) {
            ui(() -> { frame.setSize(size[0], size[1]); frame.validate(); });
            capture(frame, "Task03", "library_" + size[0] + "x" + size[1]);
            Dimension pane = edt(() -> frame.scroll.getSize());
            Dimension buttons = edt(() -> frame.reserve.getSize());
            if (pane.width <= previousWidth || pane.height <= previousHeight
                    || (control != null && !control.equals(buttons))) throw new AssertionError("Resize behavior");
            if (!edt(() -> frame.status.getText().contains("Reserved: Effective Java"))) throw new AssertionError();
            previousWidth = pane.width; previousHeight = pane.height; control = buttons;
            System.out.println("Library " + size[0] + "x" + size[1] + " -> results " + pane.width + "x" + pane.height
                    + ", Reserve " + buttons.width + "x" + buttons.height);
        }
        ui(frame::dispose);
    }

    private static void draw() throws Exception {
        DrawPad frame = edt(() -> display(new DrawPad()));
        capture(frame, "Task04", "launch");
        Point base = edt(frame::getLocationOnScreen);
        int[][] points = {{80, 95}, {152, 136}, {235, 100}};
        for (int index = 0; index < points.length; index++) {
            int[] position = points[index];
            String expectedLabel = "Clicks: " + (index + 1);
            BooleanSupplier recorded = () -> Arrays.stream(frame.getComponents()).filter(c -> c instanceof Label)
                    .anyMatch(c -> ((Label)c).getText().equals(expectedLabel));
            for (int attempt = 0; attempt < 3 && !edt(recorded::getAsBoolean); attempt++) {
                robot.mouseMove(base.x + position[0], base.y + position[1]);
                robot.delay(70);
                robot.mousePress(InputEvent.BUTTON1_DOWN_MASK);
                robot.delay(75);
                robot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
                robot.delay(180);
            }
            if (!edt(recorded::getAsBoolean)) throw new AssertionError("Robot click not recorded: " + expectedLabel);
        }
        BufferedImage before = capture(frame, "Task04", "dots_before_minimize");
        ui(() -> frame.setState(Frame.ICONIFIED));
        await(() -> frame.getState() == Frame.ICONIFIED, "Draw pad iconified");
        Thread.sleep(300);
        ui(() -> { frame.setState(Frame.NORMAL); frame.setAlwaysOnTop(true); frame.toFront(); });
        await(() -> frame.getState() == Frame.NORMAL && frame.isShowing(), "Draw pad restored");
        BufferedImage after = capture(frame, "Task04", "dots_after_restore");
        int painted = 0;
        for (int y = 50; y < 240; y++) {
            for (int x = 30; x < 370; x++) {
                int a = before.getRGB(x, y) & 0xFFFFFF;
                int b = after.getRGB(x, y) & 0xFFFFFF;
                if (a <= 0x333333) {
                    if (b > 0x333333) throw new AssertionError("Painted pixel did not survive");
                    painted++;
                }
            }
        }
        if (painted < 40 || painted > 120) throw new AssertionError("Unexpected ink pixel count " + painted);
        if (!edt(() -> Arrays.stream(frame.getComponents()).filter(c -> c instanceof Label)
                .anyMatch(c -> ((Label)c).getText().equals("Clicks: 3")))) throw new AssertionError("Click count");
        ui(frame::dispose);
        System.out.println("Draw Pad: 3 Robot clicks and raster pixels verified after real minimize/restore.");
    }

    private static JDialog dialog(JFrame frame) throws Exception {
        for (int i = 0; i < 30; i++) {
            JDialog found = edt(() -> {
                for (Window child : frame.getOwnedWindows()) {
                    if (child instanceof JDialog && child.isShowing()) return (JDialog) child;
                }
                return null;
            });
            if (found != null) return found;
            Thread.sleep(100);
        }
        throw new AssertionError("Dialog did not appear");
    }

    private static void converter() throws Exception {
        Converter demo = edt(() -> display(new Converter()));
        capture(demo, "Task05", "todo_launch");
        JTextField input = edt(() -> (JTextField) Arrays.stream(demo.getContentPane().getComponents())
                .filter(c -> c instanceof JTextField).findFirst().get());
        ui(() -> { input.setText("1000"); input.postActionEvent(); demo.pack(); });
        if (!hasLabel(demo,"12.00")) throw new AssertionError("TODO valid");
        capture(demo, "Task05", "todo_valid_conversion");
        ui(() -> { input.setText("bad"); input.postActionEvent(); demo.pack(); });
        if (!hasLabel(demo,"Invalid amount")) throw new AssertionError("TODO invalid");
        capture(demo, "Task05", "todo_invalid_amount");
        ui(demo::dispose);
        for (String style : new String[]{"frame","named","anonymous","lambda"}) {
            ConverterStyles original = edt(() -> display(ConverterStyles.create(style, false)));
            ui(() -> { original.amount.setText("1000"); original.button.doClick(); });
            if (!edt(() -> original.result.getText().equals("12.00 USD")
                    && original.amount.getActionListeners().length == 0)) throw new AssertionError(style);
            capture(original, "Task05", style + "_original");
            ui(original::dispose);
            ConverterStyles frame = edt(() -> display(ConverterStyles.create(style, true)));
            ui(() -> { frame.amount.setText("1000"); EventQueue.invokeLater(frame.amount::postActionEvent); });
            JDialog valid = dialog(frame);
            if (!edt(() -> frame.result.getText().equals("12.00 USD"))) throw new AssertionError(style);
            capture(valid, "Task05", style + "_valid_dialog");
            ui(() -> { valid.dispose(); frame.amount.setText("bad"); EventQueue.invokeLater(() -> frame.button.doClick()); });
            JDialog invalid = dialog(frame);
            if (!edt(() -> frame.result.getText().equals("Invalid amount"))) throw new AssertionError(style);
            capture(invalid, "Task05", style + "_invalid_dialog");
            ui(() -> { invalid.dispose(); frame.dispose(); });
        }
        System.out.println("Converter: supplied TODO and four listener variants verified for valid and invalid input.");
    }

    private static boolean hasLabel(JFrame frame, String value) throws Exception {
        return edt(() -> Arrays.stream(frame.getContentPane().getComponents()).filter(c -> c instanceof JLabel)
                .anyMatch(c -> ((JLabel)c).getText().equals(value)));
    }

    private static void registration() throws Exception {
        RegistrationService service = RegistrationDesk.configured();
        RegistrationDesk frame = edt(() -> display(new RegistrationDesk(service)));
        await(() -> frame.register.isEnabled(), "Initial page");
        if (!edt(() -> frame.status.getText().equals("Page 1"))) throw new AssertionError("JDBC page unavailable");
        capture(frame, "Task06", "database_page");
        ui(() -> frame.register.doClick());
        await(() -> frame.status.getText().equals("Invalid registration number"), "Registration validation");
        capture(frame, "Task06", "invalid_registration");
        String id = Long.toString(System.currentTimeMillis());
        ui(() -> { frame.id.setText(id); frame.name.setText("Sample Candidate"); frame.event.setSelectedItem("Dance"); frame.register.doClick(); });
        await(() -> frame.status.getText().equals("Registration saved"), "Valid registration");
        capture(frame, "Task06", "successful_registration");
        ui(() -> frame.register.doClick());
        await(() -> frame.status.getText().startsWith("Already registered"), "Duplicate detection");
        capture(frame, "Task06", "duplicate_registration");
        ui(() -> frame.filter.setSelectedItem("Dance"));
        await(() -> frame.status.getText().equals("Page 1"), "Filtered page");
        if (!edt(() -> frame.table.getRowCount() > 0 && frame.table.getValueAt(0, 2).equals("Dance"))) throw new AssertionError();
        capture(frame, "Task06", "event_filter");
        for (int i = 0; i < 30; i++) service.register(id + (100 + i), "Quiz fixture " + i, "Quiz");
        ui(() -> frame.filter.setSelectedItem("Quiz"));
        await(() -> frame.next.isEnabled(), "First quiz page");
        if (!edt(() -> frame.table.getRowCount() == RegistrationDesk.PAGE)) throw new AssertionError("First page size");
        ui(() -> frame.next.doClick());
        await(() -> frame.status.getText().equals("Next page"), "Second quiz page");
        if (!edt(() -> frame.prev.isEnabled() && frame.table.getRowCount() > 0
                && frame.table.getRowCount() <= RegistrationDesk.PAGE)) throw new AssertionError("Second page controls");
        capture(frame, "Task06", "second_page");
        ui(() -> frame.prev.doClick());
        await(() -> frame.status.getText().equals("Previous page"), "Return to first page");
        Path exportDir = Files.createTempDirectory("registration-fixture");
        Path exported = exportDir.resolve("quiz.csv");
        try {
            ui(() -> EventQueue.invokeLater(() -> frame.getJMenuBar().getMenu(0).getItem(0).doClick()));
            JDialog picker = dialog(frame);
            JFileChooser chooser = edt(() -> findChooser(picker));
            if (chooser == null) throw new AssertionError("File menu did not open a save chooser");
            ui(() -> { chooser.setSelectedFile(exported.toFile()); chooser.approveSelection(); });
            await(() -> frame.status.getText().startsWith("Exported "), "CSV export");
            int rows = Integer.parseInt(edt(() -> frame.status.getText().split(" ")[1]));
            if (rows < 30 || Files.readAllLines(exported, StandardCharsets.UTF_8).size() != rows + 1) {
                throw new AssertionError("Export result mismatch");
            }
            capture(frame, "Task06", "csv_export_success");
        } finally { Files.deleteIfExists(exported); Files.deleteIfExists(exportDir); }
        ui(frame::dispose);
        System.out.println("Registration: MySQL valid, invalid, duplicate, filter, next/previous and File menu CSV export verified.");
    }

    private static void drawMisspelling() throws Exception {
        DrawPad frame = edt(() -> display(new DrawPad()));
        Point base = edt(frame::getLocationOnScreen);
        robot.mouseMove(base.x + 170, base.y + 105);
        robot.mousePress(InputEvent.BUTTON1_DOWN_MASK);
        robot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
        robot.delay(150);
        if (!edt(() -> Arrays.stream(frame.getComponents()).filter(c -> c instanceof Label)
                .anyMatch(c -> ((Label)c).getText().equals("Clicks: 0")))) {
            throw new AssertionError("Expected misspelled callback to leave count unchanged");
        }
        capture(frame, "Task04", "misspelled_callback_output");
        ui(frame::dispose);
        System.out.println("Misspelled callback compiled without annotation. A real click left the count at zero.");
    }
    private static JFileChooser findChooser(Container parent) {
        for (Component child : parent.getComponents()) {
            if (child instanceof JFileChooser) return (JFileChooser) child;
            if (child instanceof Container) {
                JFileChooser found = findChooser((Container) child);
                if (found != null) return found;
            }
        }
        return null;
    }

    public static void main(String[] args) throws Exception {
        if (GraphicsEnvironment.isHeadless()) throw new IllegalStateException("Desktop unavailable");
        robot = new Robot();
        try {
            if (args.length == 1 && args[0].equals("audit-extra")) {
                appointments();
                registration();
            } else if (args.length == 1 && args[0].equals("misspelling")) {
                drawMisspelling();
            } else {
                fee(); appointments(); library(); draw(); converter(); registration();
            }
        } finally {
            ui(() -> { for (Window window : Window.getWindows()) window.dispose(); });
        }
    }
}
