# Task 4 · Draw pad forensics

1. AWT calls mouseClicked with capital C. The lower-case method is a distinct method, so MouseAdapter's empty implementation handles the event. Even if invoked, the original changes count but never calls status.setText.
2. Fix the spelling, click several times, minimize using the window controls, then restore or cover and uncover the surface. Immediate getGraphics painting is lost when the window system invalidates and repaints the surface because no points are saved.
3. The completed callback stores each point, updates the label and requests a repaint. paint calls super then draws a six-pixel filled circle centered on each stored point. The screenshot test clicks three positions and verifies that the count and dot pixels survive iconification and restoration.
4. Compile the misspelled adapter twice. Without Override it is a legal unrelated method and compiles without a diagnostic. With Override, javac rejects it. Compare both genuine compiler results.

An off-screen BufferedImage can accumulate dots by rendering each new click into the raster and then blitting it during paint. Point storage uses O(n) space and repaint takes O(n) draw operations. The raster uses O(width × height) space and copying a full raster can take O(width × height) pixel work independent of n. It also needs a resize policy. Point coordinates can be replayed at a new size or exported independently.
