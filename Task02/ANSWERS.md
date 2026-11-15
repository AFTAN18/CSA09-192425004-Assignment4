# Task 2 · Hospital appointment desk

| Source | Event | Listener | Action |
|---|---|---|---|
| Department JComboBox | ActionEvent | ActionListener | Replace doctor model |
| Book JButton | ActionEvent | ActionListener | Validate then attempt booking |
| Frame | WindowEvent | WindowAdapter | Log open, confirm close during booking, dispose, log closed |

```text
JFrame (BorderLayout)
├─ JPanel CENTER (GridLayout)
│  ├─ JLabel + JTextField patient
│  ├─ JLabel + JComboBox department
│  ├─ JLabel + JComboBox doctor
│  └─ JLabel + JComboBox time
└─ JPanel SOUTH (FlowLayout) → JButton Book, JLabel status

ALGORITHM RefreshDoctors(department)
    doctors ← CACHE GET department OR empty list
    REPLACE doctor control model with doctors
    SELECT first valid doctor if one exists
END ALGORITHM

ALGORITHM RegisterAppointment(patient, doctor, time)
    IF fields invalid THEN DISPLAY error AND RETURN
    DISABLE booking button AND mark request pending
    TRY reserve doctor/time atomically
    DISPLAY confirmation OR conflict
    CLEAR pending AND ENABLE booking
END ALGORITHM
```

Swing invokes the constructor and initial refresh on the EDT, then setVisible posts windowOpened. Selecting Dental triggers actionPerformed and refreshDoctors. Book starts a fixture timer, which updates status and logs completion. Window close triggers windowClosing, which can confirm if pending, and dispose posts windowClosed. Applets were host-driven through init/start/stop/destroy, with painting invoked as needed. A Swing frame owns window lifecycle and component listeners. The simulated record store is shared only within the current frame.

A cached map uses O(d+m) references for d departments and m doctors and expected O(1) lookup plus O(k) model population for k doctors. A JDBC-on-change design queries an index by department using parameters on a worker, discards responses superseded by newer selections and populates the model. It needs O(k) active client space, approximately O(log m+k) indexed database work and network latency per change. Cached values need a refresh rule.
