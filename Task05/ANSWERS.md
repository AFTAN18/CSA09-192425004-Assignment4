# Task 5 · Currency converter listeners

The executable comparison selects frame listener, named inner class, anonymous inner class or lambda. Each invokes the shared conversion operation and uses WindowAdapter to dispose. The supplied lambda TODO demo is a separate executable with a fixed INR source. Its button and Enter share one listener, and the label shows two decimal places or Invalid amount. The four-style changed requirement adds two shared behavior lines: register the same listener on the amount field, and display a JOptionPane for the result. The handler bodies need no edits for any style. Run a style with argument original to observe the button-only, label-only baseline.

| Style | Readability and frame access | Reuse |
|---|---|---|
| Frame | Direct fields, but frame implements the action role | Entire frame only |
| Named inner | Explicit type and access to fields | Reusable in multiple registrations on frame |
| Anonymous | Inline explicit callback with captured window | Reusable through the stored reference |
| Lambda | Short delegation with a captured window | Reusable through ActionListener reference |

For a large application use a shared Swing Action per command to centralize labels, enabled state and keyboard bindings. A method reference requires a signature accepting ActionEvent or a small adapter method. It supplies one reusable listener per binding group when stored once. With k triggers and one shared listener or Action, space is O(k) registrations plus O(1) handler state. Separate listener instances also have O(k) objects. Adding a trigger is one O(1) registration or binding; conversion uses constant numeric work plus O(L) parsing of input length L.
