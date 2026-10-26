 <div align="center">

# 🚑 Emergency Ward Dispatcher

### Java • Swing • SQLite • Object-Oriented Programming

A desktop prototype for coordinating emergency patient triage, vital-sign monitoring, and clinical resource allocation in one place.

![Java](https://img.shields.io/badge/Java-Swing-orange?style=for-the-badge&logo=openjdk&logoColor=white) ![Database](https://img.shields.io/badge/Database-SQLite-003B57?style=for-the-badge&logo=sqlite&logoColor=white) ![Project](https://img.shields.io/badge/Project-OOP%20Demo-2563EB?style=for-the-badge)

</div>

---

## 🏥 What it does

Emergency Ward Dispatcher is a hospital-operations demonstration designed to show how patient urgency and resource availability can be managed through a single desktop interface. It brings patient intake, vital observations, and resource assignments together while storing records in a local SQLite database.

> **Educational prototype only:** This project is not a medical device or a production hospital information system. Its triage rules are simplified examples; real clinical decisions must be made by qualified professionals.

## ✨ Main features

- **Ward overview:** Summary cards and a patient priority queue provide an overview of registered patients, waiting cases, critical cases, and available resources.
- **Patient registration and triage:** Record patient details, intake notes, triage level, and severity score.
- **Vitals tracking:** Record blood pressure, pulse, oxygen saturation, temperature, and respiratory rate, with alerts for configured thresholds.
- **Resource inventory:** View clinical resources and their availability and assignment status.
- **Resource dispatch:** Assign available resources to selected patients and update assignment status.
- **Emergency pre-emption demonstration:** For a critical case, demonstrate pausing an active assignment when resources are unavailable, subject to the application's rules and permissions.
- **Role-aware permissions:** Demonstrate different access levels for coordinator and administrator roles.
- **Audit history:** Keep a record of workflow and resource changes for review.
- **Persistent storage:** Store patient, resource, and audit records in SQLite.

## 🖥️ Interface previews

The following are illustrative interface previews. Sample names and counts are fictional, not live database records.

### 1. Ward overview

View key ward indicators and the patient priority queue.

![Ward overview preview](EmergencyDispatcher_Full/screenshots/overview.png)

### 2. Patient registration and triage

Capture patient intake details and assign a triage category and severity score.

![Patient triage preview](EmergencyDispatcher_Full/screenshots/patient-triage.png)

### 3. Resource inventory and dispatch

View resource status and manage patient-resource assignments.

![Resource dispatch preview](EmergencyDispatcher_Full/screenshots/resources-dispatch.png)

## 🧠 Object-oriented design

| Concept | Where it appears | Why it matters |
|---|---|---|
| **Encapsulation** | `Patient`, `Vitals`, `Resource`, and `User` | Keeps object data together and manages access through methods. |
| **Abstraction** | Abstract `User` and `Resource` classes | Defines common behaviour for related objects. |
| **Inheritance** | `StaffUser extends User`; `ClinicalResource extends Resource` | Reuses common fields and behaviour in specialised classes. |
| **Polymorphism** | Role-specific `canManageResources()` behaviour | Allows a common user type to behave differently depending on the role. |
| **Enums** | `SeverityLevel` and `UserRole` | Represents fixed categories consistently. |
| **Separation of concerns** | `model`, `service`, `data`, and `ui` packages | Separates domain objects, application logic, database access, and interface code. |

## 🗂️ Project structure

```text
EmergencyDispatcher_Full/
├── src/hospital/
│   ├── Main.java
│   ├── model/
│   ├── service/
│   ├── data/
│   └── ui/
├── screenshots/
├── lib/
├── hospital.db
└── README.md
```

## 🧰 Technology

- **Java Swing** — desktop user interface
- **SQLite** — local relational database
- **JDBC** — Java-to-database connectivity
- **JDK 17+** — Java development environment

## 🔐 Safety and data note

This is a classroom demonstration. Use fictional patient data only. Do not use the application or its sample credentials for real patient information or production clinical decisions.

---

<div align="center">

**Built as an Object-Oriented Programming project**

*Organising emergency workflows through classes, service logic, and persistent storage.*

</div>
