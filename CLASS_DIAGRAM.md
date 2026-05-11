# ERP System Class Diagram

This document contains the professional UML Class Diagram for the Employee Management and Attendance Tracking ERP System.

## PlantUML Code

<<<<<<< HEAD
```plantuml
@startuml
skinparam class {
    BackgroundColor White
    ArrowColor #2688d2
    BorderColor #2688d2
}

package "Models" {
    class Employee {
        - String id
        - String name
        - String position
        - double salary
        + Employee(id, name, position, salary)
        + getId(): String
        + getName(): String
        + getPosition(): String
        + getSalary(): double
        + toCsv(): String
        + {static} fromCsv(csvLine: String): Employee
    }

    class Attendance {
        - String employeeId
        - String date
        - String status
        + Attendance(employeeId, date, status)
        + getEmployeeId(): String
        + getDate(): String
        + getStatus(): String
        + toCsv(): String
        + {static} fromCsv(csvLine: String): Attendance
    }

    class Payroll {
        - String employeeId
        - double baseSalary
        - double bonus
        - double deductions
        - double netSalary
        + Payroll(employeeId, baseSalary, bonus, deductions, netSalary)
        + getEmployeeId(): String
        + getBaseSalary(): double
        + getBonus(): double
        + getDeductions(): double
        + getNetSalary(): double
        + toCsv(): String
        + {static} fromCsv(csvLine: String): Payroll
    }
    
    class AttendanceStat {
        - String status
        - int count
        + AttendanceStat(status, count)
        + getStatus(): String
        + getCount(): int
    }
}

package "Services" {
    class EmployeeService {
        - List<Employee> employees
        - final String FILE_NAME
        + EmployeeService()
        + getAllEmployees(): List<Employee>
        + generateNextId(): String
        + addEmployee(emp: Employee)
        + updateEmployee(updatedEmp: Employee)
        + deleteEmployee(id: String)
        - loadFromFile()
        - saveToFile()
    }

    class AttendanceService {
        - List<Attendance> records
        - final String FILE_NAME
        + AttendanceService()
        + getAllRecords(): List<Attendance>
        + addRecord(attendance: Attendance)
        + updateRecord(old: Attendance, new: Attendance)
        + hasDuplicateEntry(id: String, date: String): boolean
        + countPresent(id: String): int
        + countTotal(id: String): int
        + clearAll()
        - loadFromFile()
        - saveToFile()
    }

    class PayrollService {
        - List<Payroll> payrollRecords
        - final String FILE_NAME
        + PayrollService()
        + getAllRecords(): List<Payroll>
        + addPayroll(payroll: Payroll)
        - loadFromFile()
        - saveToFile()
    }

    class ReportService {
        + generatePdfReport(dest: File, title: String, chart: WritableImage, att: List<Attendance>, pay: List<Payroll>)
    }
}

package "Controllers" {
    class DashboardController {
        - VBox contentArea
        - List<Button> sidebarButtons
        + initialize()
        - refreshDashboard()
        - loadPage(fxml: String, btn: Button)
    }

    class EmployeeController {
        - EmployeeService empService
        - TableView<Employee> tblEmployees
        + initialize()
        + handleAddEmployee()
        + handleUpdateEmployee()
        + handleDeleteEmployee()
    }

    class AttendanceController {
        - AttendanceService attService
        - EmployeeService empService
        - TableView<Attendance> tblAttendance
        + initialize()
        + handleMarkAttendance()
        + handleUpdateAttendance()
    }

    class PayrollController {
        - PayrollService payService
        - EmployeeService empService
        - TableView<Payroll> tblPayroll
        + initialize()
        + handleProcessPayroll()
    }

    class ReportsController {
        - ReportService reportService
        - AttendanceService attService
        - PayrollService payService
        + initialize()
        + handleExportPdf()
    }
}

package "Utils" {
    class FileUtil {
        + {static} readLines(fileName: String): List<String>
        + {static} writeLines(fileName: String, lines: List<String>)
    }
}

' Relationships
EmployeeService "1" *-- "*" Employee : manages
AttendanceService "1" *-- "*" Attendance : manages
PayrollService "1" *-- "*" Payroll : manages

EmployeeController --> EmployeeService : uses
AttendanceController --> AttendanceService : uses
AttendanceController --> EmployeeService : uses
PayrollController --> PayrollService : uses
PayrollController --> EmployeeService : uses
ReportsController --> ReportService : uses
ReportsController --> AttendanceService : uses
ReportsController --> PayrollService : uses
DashboardController --> EmployeeService : uses
DashboardController --> AttendanceService : uses
DashboardController --> PayrollService : uses

EmployeeService ..> FileUtil : uses
AttendanceService ..> FileUtil : uses
PayrollService ..> FileUtil : uses

ReportService ..> Attendance : uses
ReportService ..> Payroll : uses

@enduml
```

## 🔄 Sequence Diagram: Add Employee
This diagram shows the interaction between the UI, Controller, Service, and File system when a new employee is added.

```plantuml
@startuml
actor Admin
participant "EmployeeController" as Ctrl
participant "EmployeeService" as Svc
participant "Employee" as Model
participant "FileUtil" as Util

Admin -> Ctrl : Clicks "Add Employee"
Ctrl -> Svc : generateNextId()
Svc --> Ctrl : Returns "EMP-004"
Admin -> Ctrl : Enters details (Name, Position, Salary)
Ctrl -> Model : new Employee("EMP-004", name, pos, sal)
Ctrl -> Svc : addEmployee(employee)
Svc -> Svc : employees.add(employee)
Svc -> Svc : saveToFile()
Svc -> Util : writeLines("employees.txt", lines)
Util --> Svc : Success
Svc --> Ctrl : Return
Ctrl -> Ctrl : refreshTable()
Ctrl --> Admin : Display updated list
@enduml
```

## 🔄 Sequence Diagram: Mark Attendance
This diagram shows the process of recording attendance, including the duplicate check.

```plantuml
@startuml
actor Admin
participant "AttendanceController" as Ctrl
participant "AttendanceService" as Svc
participant "Attendance" as Model
participant "FileUtil" as Util

Admin -> Ctrl : Selects Employee & Date
Admin -> Ctrl : Clicks "Mark Present"
Ctrl -> Svc : hasDuplicateEntry(empId, date)
Svc --> Ctrl : false
Ctrl -> Model : new Attendance(empId, date, "Present")
Ctrl -> Svc : addRecord(attendance)
Svc -> Svc : records.add(attendance)
Svc -> Svc : saveToFile()
Svc -> Util : writeLines("attendance.txt", lines)
Util --> Svc : Success
Svc --> Ctrl : Return
Ctrl -> Ctrl : refreshUI()
Ctrl --> Admin : Show success message
@enduml
```

## 🎯 Use Case Diagram
Describes the high-level functionality available to the system users.

```plantuml
@startuml
left to right direction
actor "Admin / User" as admin

rectangle "ERP System" {
    usecase "Manage Employees (Add/Update/Delete)" as UC1
    usecase "Mark Attendance" as UC2
    usecase "View Attendance Records" as UC3
    usecase "Process Payroll" as UC4
    usecase "View Statistics (Charts)" as UC5
    usecase "Export PDF Reports" as UC6
    usecase "Export Attendance CSV" as UC7
}

admin --> UC1
admin --> UC2
admin --> UC3
admin --> UC4
admin --> UC5
admin --> UC6
admin --> UC7
@enduml
```

## ⚡ Activity Diagram: Mark Attendance Process
Shows the logical flow of the "Mark Attendance" use case.

```plantuml
@startuml
start
:Select Employee from list;
:Select Date (Default to Today);
:Select Status (Present/Absent);
if (Check Duplicate Entry?) then (Exists)
    :Show "Already Marked" Warning;
    stop
else (New Entry)
    :Create Attendance Object;
    :Save to attendance.txt;
    :Update Table View;
    :Calculate New Attendance %;
    :Show Success Notification;
endif
stop
@enduml
```

## Description
These diagrams provide a comprehensive overview of the system:
1.  **Class Diagram**: Static structure and relationships.
2.  **Sequence Diagrams**: Dynamic interaction for core business flows.
3.  **Use Case Diagram**: High-level functional requirements.
4.  **Activity Diagram**: Detailed logic flow for complex operations.
=======
![Architecture](uml_ERP.png)
>>>>>>> 2068560250b17581347720c4b482b459e29db304
