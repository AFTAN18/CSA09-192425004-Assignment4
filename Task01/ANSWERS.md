# Task 1 · Fee window

## 1.1 Diagnosis

The listener combines widget access, validation, database code and bank confirmation, making rules hard to test independently. Comparing action command strings with == can miss equal text in separate objects. Empty or invalid amounts can throw and negative amounts can slip through. SQL and sleep on the EDT freeze input and painting. Console feedback is invisible to a window user. The Receipt button falls into the Clear branch. String-concatenated SQL permits injection, and an unclosed connection/statement consumes resources. Failure to handle checked exceptions prevents compilation, and the missing window-close policy can leave the process open.

## 1.2 Repair

```text
FeeWindow → FeeService → Ledger → JDBC
EDT action → validate and persist in worker → EDT updates status
```

BigDecimal preserves cent accuracy. A prepared statement uses values as parameters, and try-with-resources closes JDBC resources. Clear, receipt and pay have dedicated listeners. The Pay button is disabled while one request is pending. The service has a replaceable persistence interface and a console test. Run without database settings for a demonstration ledger or configure the isolated database for actual persistence.

## 1.3 Defence

A fee rule can be tested against an in-memory ledger without opening a window. The frame can change layout independently. Background database work leaves Swing free to handle input. Different actions receive distinct behavior and explicit user feedback.

A unified linear dispatcher can compare up to k commands and uses one listener instance plus O(k) branches. Separate registered listeners route each event in O(1) application dispatch with O(k) listener references/objects in the general case. Database work and its memory cost are separate from dispatch.
