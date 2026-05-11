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
/**
 * 
 * class responsible for user interaactiona nd ui
 * @author abdel
 */
public class AttendanceController {

//    compo box select the employee
    @FXML private ComboBox<String> cmbEmployee;   // shows "EMP-001 – Jane Doe"
//    date of attendence
    @FXML private DatePicker       datePicker;
//  apsent or present
    @FXML private ComboBox<String> cmbStatus;
//    fail or not
    @FXML private Label            lblAttendanceStatus;
    // Date filter 3la 7asab el date
    @FXML private TextField txtDateFilter;

    // Attendance log table
    @FXML private TableView<Attendance>        attendanceTable;
    @FXML private TableColumn<Attendance, String> colAttEmpId; // id
    @FXML private TableColumn<Attendance, String> colAttName; // name
    @FXML private TableColumn<Attendance, String> colAttDate; //date
    @FXML private TableColumn<Attendance, String> colAttStatus; //status

    // Stats table جدول الاحصائيات بتاعنا  مختلف عن الحضور كده كده
    @FXML private TableView<AttendanceStat>        statsTable;
    @FXML private TableColumn<AttendanceStat, String>  colStatId;
    @FXML private TableColumn<AttendanceStat, String>  colStatName;
    @FXML private TableColumn<AttendanceStat, Integer> colStatTotal;
    @FXML private TableColumn<AttendanceStat, Integer> colStatPresent;
    @FXML private TableColumn<AttendanceStat, String>  colStatPercentage;

    private AttendanceService attendanceService; // curd , we by3ml file status
    private EmployeeService   employeeService; // employee service

    private ObservableList<Attendance>    attendanceData;
    private FilteredList<Attendance>      filteredData;
    private ObservableList<AttendanceStat> statsData;

//    list of the eomployees
    private List<Employee> employeeList;

    private Attendance selectedAttendance = null;

    @FXML
    public void initialize() {
        attendanceService = new AttendanceService();
        employeeService   = new EmployeeService();
        employeeList      = employeeService.getAllEmployees();

        // list show id + name
        ObservableList<String> allEmpItems = FXCollections.observableArrayList();
        for (Employee emp : employeeList) {
            allEmpItems.add(emp.getId() + " – " + emp.getName());
        }

//        3lshan aktab gowa el compo box
        cmbEmployee.setEditable(true);
//        put the item inside compo box
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
//            bt3ml dropdown ll compo box ,
            cmbEmployee.show(); 

//            lw dost clear yrg3 original data
            if (typed.isEmpty()) {
                cmbEmployee.setItems(allEmpItems);
            }
        });

      
        cmbEmployee.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && !newVal.isEmpty()) {
                cmbEmployee.getEditor().setText(newVal);
                cmbEmployee.setItems(allEmpItems); // restore full list for next time
            }
        });

        cmbStatus.setItems(FXCollections.observableArrayList("Present", "Absent"));
        datePicker.setValue(LocalDate.now());
        attendanceData = FXCollections.observableArrayList(attendanceService.getAllRecords());
        
        filteredData   = new FilteredList<>(attendanceData, p -> true);

//        integrate the column with the model properities
        colAttEmpId.setCellValueFactory(new PropertyValueFactory<>("employeeId"));
        colAttDate.setCellValueFactory(new PropertyValueFactory<>("date"));
        colAttStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
//  lookup table with id not name
        colAttName.setCellValueFactory(cellData -> {
            String id = cellData.getValue().getEmployeeId();
            for (Employee emp : employeeList) {
                if (emp.getId().equals(id)) {
                    return new SimpleStringProperty(emp.getName());
                }
            }
            return new SimpleStringProperty("Unknown");
        });
//هتربط الجدول بالبينانات الحديده
        attendanceTable.setItems(filteredData);

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

//        filter with the date 
        txtDateFilter.textProperty().addListener((obs, oldVal, newVal) -> {
            String f = newVal == null ? "" : newVal.trim();
            filteredData.setPredicate(att ->
                f.isEmpty() || att.getDate().contains(f)
            );
        });

        statsData = FXCollections.observableArrayList();   // Real time refreshment of the changed areas
        colStatId.setCellValueFactory(new PropertyValueFactory<>("employeeId"));
        colStatName.setCellValueFactory(new PropertyValueFactory<>("employeeName"));
        colStatTotal.setCellValueFactory(new PropertyValueFactory<>("totalDays"));
        colStatPresent.setCellValueFactory(new PropertyValueFactory<>("presentDays"));
        colStatPercentage.setCellValueFactory(new PropertyValueFactory<>("percentage"));

        colStatPercentage.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
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

/**
 * add new recoed in the attnedence 
 * select 
 * add 
 * update
 */
    @FXML
    private void handleMarkAttendance() {
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
        String empId = selectedEmp.split("–")[0].trim();
        String date  = datePicker.getValue().toString();
        String status = cmbStatus.getValue();
        if (attendanceService.hasDuplicateEntry(empId, date)) {
            showError("Attendance for " + empId + " on " + date + " already logged!");
            return;
        }
        Attendance record = new Attendance(empId, date, status);
        attendanceService.addRecord(record);
        attendanceData.add(record);
// refresh new data
        refreshStats();

        showSuccess("Attendance saved for " + selectedEmp.split("\u2013")[1].trim() + " (" + date + ")");
        handleClearAttendance();
    }
    
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

        if (!empId.equals(selectedAttendance.getEmployeeId()) || !date.equals(selectedAttendance.getDate())) {
            if (attendanceService.hasDuplicateEntry(empId, date)) {
                showError("Attendance for " + empId + " on " + date + " already logged!");
                return;
            }
        }

        Attendance updated = new Attendance(empId, date, status);
        attendanceService.updateRecord(selectedAttendance, updated);
        
        attendanceData.setAll(attendanceService.getAllRecords());
        refreshStats();
        
        showSuccess("Attendance updated for " + selectedEmp.split("\u2013")[1].trim() + " (" + date + ")");
        handleClearAttendance();
    }

    @FXML
    public void handleRefreshStats() {
        refreshStats();
    }

    private void refreshStats() {
        statsData.clear();
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
        lblAttendanceStatus.setText(" " + msg);
        lblAttendanceStatus.setStyle("-fx-text-fill: -color-danger;");
    }
    private void showSuccess(String msg) {
        lblAttendanceStatus.setText(" " + msg);
        lblAttendanceStatus.setStyle("-fx-text-fill: -color-success;");
    }
}