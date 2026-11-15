# CSA09 Assignment 4 · Event-driven GUI design

Aftan Faiz · 192425004 · CO4

```text
openjdk version "1.8.0_504"
OpenJDK Runtime Environment (Temurin)(build 1.8.0_504-b01)
OpenJDK 64-Bit Server VM (Temurin)(build 25.504-b01, mixed mode)
javac 1.8.0_504
```

Run `pwsh -NoProfile -File scripts/check.ps1` from the repository root to compile the six tasks and run the service and expected compiler experiments. Start GUI programs from the same directory after the check.

| Task | Command | Verified behavior |
|---|---|---|
| [Task01](Task01/ANSWERS.md) | `java -cp build FeeWindow` | Valid/invalid payment, receipt, clear and independent validation test |
| [Task02](Task02/ANSWERS.md) | `java -cp build AppointmentDesk` | Department-driven doctor list, form validation and appointment confirmation |
| [Task03](Task03/ANSWERS.md) | `java -cp build LibrarySearch` | Real screenshots at 480×280, 640×370 and 820×500 |
| [Task04](Task04/ANSWERS.md) | `java -cp build DrawPad` | Three dots preserved after minimize/restore and genuine compiler diagnostic |
| [Task05](Task05/ANSWERS.md) | `java -cp build Converter` and `java -cp build ConverterStyles lambda` | Button, Enter and valid/invalid outputs. Other styles are frame, named and anonymous. Append original for the button-only version. |
| [Task06](Task06/ANSWERS.md) | `java -cp "build;PATH_TO_CONNECTOR_JAR" RegistrationDesk` | Live MySQL insert, validation, duplicate protection, filter and export |

Create a local disposable MySQL database, then apply Task06/schema.sql and Task06/seed.sql to it. Supply the three environment variables described in Task06/.env.example and an absolute path to MySQL Connector/J. Verification used MySQL Community 8.4.13 and Connector/J 8.4.0. Launch Task01 without database variables for a simulated ledger. To recapture live screenshot evidence for all windows, run `pwsh -NoProfile -File scripts/check.ps1 -Gui -Connector "PATH_TO_CONNECTOR_JAR"` on an unlocked desktop with at least 900×600 available. The harness uses AWT Robot desktop capture and real widget events. It inserts a synthetic registration into the disposable database.

See the [assignment report](report/AssignmentReport.md).
