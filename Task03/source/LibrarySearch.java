import java.awt.*;
import javax.swing.*;

public final class LibrarySearch extends JFrame {
    final JTextField search = new JTextField(20);
    final JComboBox<String> category = new JComboBox<>(new String[]{"All", "Java", "Databases"});
    final JButton find = new JButton("Search");
    final JButton prev = new JButton("Prev"), next = new JButton("Next"), reserve = new JButton("Reserve");
    final JLabel status = new JLabel("Search the library catalogue");
    final JTextArea results = new JTextArea(10, 35);
    final JScrollPane scroll = new JScrollPane(results);
    private final String[] catalogue = {"Java Programming", "Effective Java", "Database Systems"};
    private java.util.List<String> matches = new java.util.ArrayList<>();
    private int page;

    LibrarySearch() {
        super("Campus Library");
        JPanel header = new JPanel(new BorderLayout());
        JLabel title = new JLabel("Campus Library Search", SwingConstants.CENTER);
        title.setFont(title.getFont().deriveFont(Font.BOLD, 20f));
        JPanel query = new JPanel(new FlowLayout());
        query.add(search); query.add(category); query.add(find);
        header.add(title, BorderLayout.NORTH); header.add(query, BorderLayout.CENTER);
        JPanel footer = new JPanel(new BorderLayout());
        JPanel actions = new JPanel(new FlowLayout());
        actions.add(prev); actions.add(next); actions.add(reserve);
        footer.add(actions, BorderLayout.NORTH); footer.add(status, BorderLayout.SOUTH);
        results.setEditable(false);
        add(header, BorderLayout.NORTH); add(scroll, BorderLayout.CENTER); add(footer, BorderLayout.SOUTH);
        find.addActionListener(e -> {
            matches.clear();
            String queryText = search.getText().trim().toLowerCase(java.util.Locale.ROOT);
            String kind = String.valueOf(category.getSelectedItem());
            for (String book : catalogue) {
                if (book.toLowerCase(java.util.Locale.ROOT).contains(queryText)
                        && (kind.equals("All") || book.contains(kind.equals("Java") ? "Java" : "Database"))) matches.add(book);
            }
            page = 0;
            refresh();
        });
        prev.addActionListener(e -> { if (page > 0) page--; refresh(); });
        next.addActionListener(e -> { if (page + 1 < matches.size()) page++; refresh(); });
        reserve.addActionListener(e -> status.setText(matches.isEmpty() ? "No title selected" : "Reserved: " + matches.get(page)));
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setMinimumSize(new Dimension(480, 250));
        pack();
    }

    private void refresh() {
        results.setText(matches.isEmpty() ? "No matches" : matches.get(page));
        status.setText(matches.isEmpty() ? "0 results" : (page + 1) + " of " + matches.size());
    }

    public static void main(String[] args) { SwingUtilities.invokeLater(() -> new LibrarySearch().setVisible(true)); }
}
