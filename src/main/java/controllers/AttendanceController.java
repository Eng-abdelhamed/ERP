package controllers;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.FileChooser;
import models.Attendance;
import models.AttendanceStat;
import models.Employee;
import services.AttendanceService;
import services.EmployeeService;
import utils.FileUtil;

import java.io.File;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class AttendanceController {

    // Form controls
    @FXML private ComboBox<String> cmbEmployee;   // shows "EMP-001 – Jane Doe"
    @FXML private DatePicker       datePicker;
    @FXML private ComboBox<String> cmbStatus;
    @FXML private Label            lblAttendanceStatus;

    // Date filter
    @FXML private TextField txtDateFilter;

    // Attendance log table
    @FXML private TableView<Attendance>        attendanceTable;
    @FXML private TableColumn<Attendance, String> colAttEmpId;
    @FXML private TableColumn<Attendance, String> colAttName;
    @FXML private TableColumn<Attendance, String> colAttDate;
    @FXML private TableColumn<Attendance, String> colAttStatus;

    // Stats table
    @FXML private TableView<AttendanceStat>        statsTable;
    @FXML private TableColumn<AttendanceStat, String>  colStatId;
    @FXML private TableColumn<AttendanceStat, String>  colStatName;
    @FXML private TableColumn<AttendanceStat, Integer> colStatTotal;
    @FXML private TableColumn<AttendanceStat, Integer> colStatPresent;
    @FXML private TableColumn<AttendanceStat, String>  colStatPercentage;

    private AttendanceService attendanceService;
    private EmployeeService   employeeService;

    private ObservableList<Attendance>    attendanceData;
    private FilteredList<Attendance>      filteredData;
    private ObservableList<AttendanceStat> statsData;

    // Maps "EMP-001 – Jane Doe" back to the employee ID
    private List<Employee> employeeList;

    private Attendance selectedAttendance = null;

    @FXML
    public void initialize() {
        attendanceService = new AttendanceService();
        employeeService   = new EmployeeService();
        employeeList      = employeeService.getAllEmployees();

        // ✅ Populate employee ComboBox with only registered employees
        ObservableList<String> allEmpItems = FXCollections.observableArrayList();
        for (Employee emp : employeeList) {
            allEmpItems.add(emp.getId() + " – " + emp.getName());
        }

        // ✅ Make ComboBox editable so user can type to filter
        cmbEmployee.setEditable(true);
        cmbEmployee.setItems(allEmpItems);

        // When user types, filter the dropdown to matching employees
        cmbEmployee.getEditor().textProperty().addListener((obs, oldVal, newVal) -> {
            // Skip if this change was triggered by selecting an item (not typing)
            if (newVal == null) return;
            String typed = newVal.toLowerCase().trim();

            ObservableList<String> filtered = FXCollections.observableArrayList();
            for (String item : allEmpItems) {
                if (item.toLowerCase().contains(typed)) {
                    filtered.add(item);
                }
            }
            cmbEmployee.setItems(filtered);
            cmbEmployee.show(); // keep dropdown open while typing

            // Restore full list if field is cleared
            if (typed.isEmpty()) {
                cmbEmployee.setItems(allEmpItems);
            }
        });

        // When an item is selected, stop filtering and set the editor text
        cmbEmployee.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && !newVal.isEmpty()) {
                cmbEmployee.getEditor().setText(newVal);
                cmbEmployee.setItems(allEmpItems); // restore full list for next time
            }
        });

        // ✅ Status options — removed "On Leave"
        cmbStatus.setItems(FXCollections.observableArrayList("Present", "Absent"));

        // ✅ Auto-set date to today
        datePicker.setValue(LocalDate.now());

        // --- Attendance log table setup ---
        attendanceData = FXCollections.observableArrayList(attendanceService.getAllRecords());
        filteredData   = new FilteredList<>(attendanceData, p -> true);

        colAttEmpId.setCellValueFactory(new PropertyValueFactory<>("employeeId"));
        colAttDate.setCellValueFactory(new PropertyValueFactory<>("date"));
        colAttStatus.setCellValueFactory(new PropertyValueFactory<>("status"));

        // Name column: look up from employee list by ID
        colAttName.setCellValueFactory(cellData -> {
            String id = cellData.getValue().getEmployeeId();
            for (Employee emp : employeeList) {
                if (emp.getId().equals(id)) {
                    return new SimpleStringProperty(emp.getName());
                }
            }
            return new SimpleStringProperty("Unknown");
        });

        attendanceTable.setItems(filteredData);

        // ✅ When a row is clicked, populate the form for updating
        attendanceTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, newSel) -> {
            if (newSel != null) {
                selectedAttendance = newSel;
                // Find matching employee string and set it
                for (String item : allEmpItems) {
                    if (item.startsWith(newSel.getEmployeeId() + " \u2013")) {
                        cmbEmployee.getEditor().setText(item);
                        break;
                    }
                }
                datePicker.setValue(LocalDate.parse(newSel.getDate()));
                cmbStatus.setValue(newSel.getStatus());
            }
        });

        // ✅ Date filter: filter log table by date as user types
        txtDateFilter.textProperty().addListener((obs, oldVal, newVal) -> {
            String f = newVal == null ? "" : newVal.trim();
            filteredData.setPredicate(att ->
                f.isEmpty() || att.getDate().contains(f)
            );
        });

        // --- Stats table setup ---
        statsData = FXCollections.observableArrayList();
        colStatId.setCellValueFactory(new PropertyValueFactory<>("employeeId"));
        colStatName.setCellValueFactory(new PropertyValueFactory<>("employeeName"));
        colStatTotal.setCellValueFactory(new PropertyValueFactory<>("totalDays"));
        colStatPresent.setCellValueFactory(new PropertyValueFactory<>("presentDays"));
        colStatPercentage.setCellValueFactory(new PropertyValueFactory<>("percentage"));

        // Color-code the percentage column
        colStatPercentage.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    // Green ≥ 80%, Orange 60-79%, Red < 60%
                    if ("N/A".equals(item)) {
                        setStyle("-fx-text-fill: -color-text-muted;");
                    } else {
                        double pct = Double.parseDouble(item.replace("%", ""));
                        if (pct >= 80) {
                            setStyle("-fx-font-weight: 700; -fx-text-fill: #10b981;"); // green
                        } else if (pct >= 60) {
                            setStyle("-fx-font-weight: 700; -fx-text-fill: #f59e0b;"); // amber
                        } else {
                            setStyle("-fx-font-weight: 700; -fx-text-fill: #ef4444;"); // red
                        }
                    }
                }
            }
        });

        statsTable.setItems(statsData);
        refreshStats();
    }

    /** Saves a new attendance record, validating employee and preventing duplicates */
    @FXML
    private void handleMarkAttendance() {
        // Since ComboBox is editable, get value from the editor text
        String selectedEmp = cmbEmployee.getEditor().getText();
        if (selectedEmp == null || selectedEmp.trim().isEmpty()) {
            showError("Please select an employee.");
            return;
        }
        if (datePicker.getValue() == null) {
            showError("Please select a date.");
            return;
        }
        if (cmbStatus.getValue() == null) {
            showError("Please select a status.");
            return;
        }

        // Extract the ID from "EMP-001 – Jane Doe"
        // Also handle the case where the user typed just the ID
        String empId = selectedEmp.split("–")[0].trim();
        String date  = datePicker.getValue().toString();
        String status = cmbStatus.getValue();

        // ✅ Prevent duplicate entries for same employee on same day
        if (attendanceService.hasDuplicateEntry(empId, date)) {
            showError("Attendance for " + empId + " on " + date + " already logged!");
            return;
        }

        Attendance record = new Attendance(empId, date, status);
        attendanceService.addRecord(record);
        attendanceData.add(record);

        // Refresh stats after each new entry
        refreshStats();

        showSuccess("Attendance saved for " + selectedEmp.split("\u2013")[1].trim() + " (" + date + ")");
        handleClearAttendance();
    }

    /** Updates the selected attendance record */
    @FXML
    private void handleUpdateAttendance() {
        if (selectedAttendance == null) {
            showError("Select an attendance record to update.");
            return;
        }

        String selectedEmp = cmbEmployee.getEditor().getText();
        if (selectedEmp == null || selectedEmp.trim().isEmpty()) {
            showError("Please select an employee.");
            return;
        }
        if (datePicker.getValue() == null) {
            showError("Please select a date.");
            return;
        }
        if (cmbStatus.getValue() == null) {
            showError("Please select a status.");
            return;
        }

        String empId = selectedEmp.split("\u2013")[0].trim();
        String date  = datePicker.getValue().toString();
        String status = cmbStatus.getValue();

        // If employee ID or date changed, ensure it's not duplicating another existing record
        if (!empId.equals(selectedAttendance.getEmployeeId()) || !date.equals(selectedAttendance.getDate())) {
            if (attendanceService.hasDuplicateEntry(empId, date)) {
                showError("Attendance for " + empId + " on " + date + " already logged!");
                return;
            }
        }

        Attendance updated = new Attendance(empId, date, status);
        attendanceService.updateRecord(selectedAttendance, updated);
        
        // Refresh table and stats
        attendanceData.setAll(attendanceService.getAllRecords());
        refreshStats();
        
        showSuccess("Attendance updated for " + selectedEmp.split("\u2013")[1].trim() + " (" + date + ")");
        handleClearAttendance();
    }

    /** Recalculates attendance percentages for all employees */
    @FXML
    public void handleRefreshStats() {
        refreshStats();
    }

    private void refreshStats() {
        statsData.clear();
        // Reload employee list in case new employees were added
        employeeList = employeeService.getAllEmployees();

        for (Employee emp : employeeList) {
            int total   = attendanceService.countTotal(emp.getId());
            int present = attendanceService.countPresent(emp.getId());
            statsData.add(new AttendanceStat(emp.getId(), emp.getName(), total, present));
        }
    }

    @FXML
    private void handleClearAttendance() {
        cmbEmployee.getEditor().clear();
        cmbEmployee.getSelectionModel().clearSelection();
        datePicker.setValue(LocalDate.now()); // reset to today
        cmbStatus.getSelectionModel().clearSelection();
        lblAttendanceStatus.setText("");
        selectedAttendance = null;
        attendanceTable.getSelectionModel().clearSelection();
    }

    @FXML
    private void handleExportCsv() {
        FileChooser fc = new FileChooser();
        fc.setTitle("Export Attendance as CSV");
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV Files", "*.csv"));
        fc.setInitialFileName("attendance_export.csv");

        File file = fc.showSaveDialog(attendanceTable.getScene().getWindow());
        if (file != null) {
            List<String> lines = new ArrayList<>();
            lines.add("Employee ID,Employee Name,Date,Status");

            for (Attendance att : attendanceData) {
                String name = "Unknown";
                for (Employee emp : employeeList) {
                    if (emp.getId().equals(att.getEmployeeId())) {
                        name = emp.getName();
                        break;
                    }
                }
                lines.add(att.getEmployeeId() + "," + name + "," + att.getDate() + "," + att.getStatus());
            }

            FileUtil.writeLines(file.getAbsolutePath(), lines);
            showSuccess("Exported " + attendanceData.size() + " records successfully!");
        }
    }

    private void showError(String msg) {
        lblAttendanceStatus.setText("⚠ " + msg);
        lblAttendanceStatus.setStyle("-fx-text-fill: -color-danger;");
    }

    private void showSuccess(String msg) {
        lblAttendanceStatus.setText("✓ " + msg);
        lblAttendanceStatus.setStyle("-fx-text-fill: -color-success;");
    }
}