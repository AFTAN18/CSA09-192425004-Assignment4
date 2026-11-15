# Assignment 4 · Event-Driven GUI Design

Aftan Faiz · 192425004 · CSA09 Programming in Java

Temurin Java 8u504. JDK 17.0.21 for the modern templates.

## Compile and run

```powershell
pwsh -NoProfile -File scripts/check.ps1
```

Apply [schema](Task06/schema.sql) and [seed data](Task06/seed.sql) to disposable MySQL and set [the environment variables](Task06/.env.example). On a visible desktop use `pwsh -NoProfile -File scripts/check.ps1 -Gui -Connector PATH_TO_CONNECTOR_JAR`.

**Tests.** GUI validation, resizing, dialogs, Draw Pad restore and MySQL workflows verified.

| Task and screenshots | Topic |
|---|---|
| [Task01](Task01/README.md) | Task 1 · Fee window |
| [Task02](Task02/README.md) | Task 2 · Hospital appointment desk |
| [Task03](Task03/README.md) | Task 3 · Adaptive library layout |
| [Task04](Task04/README.md) | Task 4 · Draw pad forensics |
| [Task05](Task05/README.md) | Task 5 · Currency converter listeners |
| [Task06](Task06/README.md) | Task 6 · Campus event registration |

[Assignment report](report/AssignmentReport.md) · [Evidence audit](EVIDENCE_AUDIT.md) · [Test environment](report/TestEnvironment.md)
