# Member 2 — Technical Officer Module
## ICT2132 OOP Mini Project — Group 21 (2026)

---

## Role
**Member 2: Technical Officer + Attendance and Medical Logic**

---

## What This Module Does

| Feature | Description |
|---------|-------------|
| **Technical Officer Dashboard** | Swing GUI with 5 tabs |
| **Attendance Recording** | Record PRESENT / ABSENT for 15 sessions (Theory + Practical), 2 hours each |
| **Medical Submission Management** | View all medicals for a course; Approve / Reject with one click |
| **Attendance Percentage Calculations** | Theory %, Practical %, Combined %, Combined % with Medical |
| **Category Summary View** | Filter students into > 80%, = 80%, < 80% (with/without medical) |

---

## OOP Concepts Demonstrated

### 1. Inheritance
- `TechnicalOfficer` → extends `User` (abstract base class)
- `TheoryAttendanceSummary`, `PracticalAttendanceSummary`, `CombinedAttendanceSummary` → extend `AttendanceSummary`

### 2. Polymorphism
- `AttendanceSummary.calculatePercentage()` is **overridden** in all three subclasses:
  - `TheoryAttendanceSummary` — uses `attended / theoryTotal × 100`
  - `PracticalAttendanceSummary` — uses `attended / practicalTotal × 100`
  - `CombinedAttendanceSummary` — uses average of the two
- `User.getRole()` — abstract; overridden in `TechnicalOfficer` to return `"Technical Officer"`

### 3. Abstraction
- `User` is abstract; forces every user subclass to implement `getRole()`
- `AttendanceSummary` is abstract; forces every summary type to implement `calculatePercentage()`

### 4. Encapsulation
- All model fields are `private`; accessed via public getters/setters

### 5. Exception Handling
- `InvalidSessionException` — thrown when session number is outside [1, 15]
- `MedicalSubmissionException` — thrown when approval status is invalid or duplicate approved medical exists
- Both exceptions are caught in the GUI layer and shown to the user as dialog boxes

---

## Package Structure

```
src/
├── model/
│   ├── User.java                      (abstract — Abstraction)
│   ├── TechnicalOfficer.java          (extends User — Inheritance + Polymorphism)
│   ├── Attendance.java                (single session record)
│   ├── Medical.java                   (medical certificate record)
│   ├── AttendanceSummary.java         (abstract — Polymorphism base)
│   ├── TheoryAttendanceSummary.java   (concrete — Theory % calc)
│   ├── PracticalAttendanceSummary.java(concrete — Practical % calc)
│   └── CombinedAttendanceSummary.java (concrete — Combined % calc)
├── dao/
│   ├── TechnicalOfficerDAO.java       (DB read for TO)
│   ├── AttendanceDAO.java             (record + read attendance)
│   ├── MedicalDAO.java                (submit + approve/reject medicals)
│   └── AttendanceSummaryDAO.java      (builds summary objects from DB)
├── exception/
│   ├── InvalidSessionException.java
│   └── MedicalSubmissionException.java
├── db/
│   └── DBConnection.java              (MySQL JDBC connection)
└── gui/
    └── TechnicalOfficerDashboard.java (main Swing GUI — 5 tabs)

database/
└── member2_attendance_data.sql        (test data — run after main schema.sql)
```

---

## Attendance Rules (per project spec)

| Rule | Value |
|------|-------|
| Total sessions per component | **15** |
| Hours per session | **2 hours** |
| Exam eligibility threshold | **≥ 80%** |
| Session types | `THEORY`, `PRACTICAL` |

### Percentage Formulas

$$\text{Theory \%} = \frac{\text{Theory Attended}}{\text{Theory Total}} \times 100$$

$$\text{Practical \%} = \frac{\text{Practical Attended}}{\text{Practical Total}} \times 100$$

$$\text{Combined \%} = \frac{\text{Theory \%} + \text{Practical \%}}{2}$$

$$\text{Combined \% (with Medical)} = \frac{(\text{Theory Attended} + \text{Approved Med}) }{\text{Theory Total}} \text{ averaged with Practical}$$

---

## How to Run

1. Make sure MySQL is running and the **main `schema.sql`** has been executed.
2. Run `database/member2_attendance_data.sql` to insert test data.
3. Update the password in `src/db/DBConnection.java`.
4. Add `lib/mysql-connector-j-8.0.33.jar` to the classpath.
5. Run `src/main/java/gui/TechnicalOfficerDashboard.java` — login with TO ID `to1`.

---

## Lecturer Demo Data

After creating the main `fot_mis` schema, run `database/lecturer_demo_data.sql`
in MySQL Workbench (or another MySQL client). It adds a lecturer, three sample
students, an ICT2132 course, enrollments, assessments, marks, attendance,
medical, results, and a lecturer notice. The script can be re-run without
duplicating those records.

The first time the lecturer or undergraduate materials tab accesses
`course_material`, the application adds the availability columns automatically
and keeps existing materials available. The database account must have
permission to alter that table. Alternatively, run
`database/lecturer_material_availability.sql` once against `fot_mis`.
In the Lecturer Dashboard, choose a course, enter the title, set the inclusive
availability dates, and upload the file. Undergraduates can see and open it
only from the start date through the end date, inclusive.

Use the demo account **lec1** with password **Lecturer@123** to sign in. The
seeded accounts use plain-text demo passwords because the application supports
that format for temporary testing only; do not use it for real accounts.

---

## Dashboard Tabs

| Tab | Purpose |
|-----|---------|
| 📋 Profile | Shows TO name, email, department, role |
| ✅ Record Attendance | Select course, student, session, type, status → Record |
| 🏥 Medical Submissions | Load pending / all medicals; Approve or Reject |
| 📊 Attendance Summary | Theory %, Practical %, Combined % per student |
| 🗂️ Category View | Filter students: > 80%, = 80%, < 80% (with/without medical) |
