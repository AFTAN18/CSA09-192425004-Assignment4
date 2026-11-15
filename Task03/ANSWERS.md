# Task 3 · Adaptive library layout

```text
JFrame / BorderLayout
├─ NORTH: header / BorderLayout
│  ├─ NORTH: title
│  └─ CENTER: query / FlowLayout → search text, category, Search
├─ CENTER: JScrollPane → results JTextArea
└─ SOUTH: footer / BorderLayout
   ├─ NORTH: actions / FlowLayout → Prev, Next, Reserve
   └─ SOUTH: status
```

BorderLayout reserves preferred-height header and footer and gives remaining space to the scroll pane. FlowLayout keeps controls at preferred sizes at the tested widths. A minimum width prevents the tested query row from clipping. GridLayout would force equal cells and CardLayout would display one card, so neither helps here. Tests search for Java, navigate to the second match and reserve it. The resize check compares control sizes and the result area over three window sizes.

GridBagLayout can set center weightx/weighty to one and use BOTH fill for the scroll pane, leaving zero vertical weights for controls. It can flatten some containers, but carries per-component constraints. A straightforward single layout pass across k widgets is O(k); GridBag's row/column constraint passes have larger constants and depend on grid spans. Nested layouts here use O(k) widget state and O(1) extra panel count. Absolute placement also stores O(k) bounds and initial placement takes O(k), but manual resizing requires custom O(k) updates and fails to adapt by default to fonts or locale.

At 480×280, 640×370 and 820×500 the result pane was 462×119, 622×209 and 802×339 pixels. The Reserve button stayed 81×26 at all sizes.
