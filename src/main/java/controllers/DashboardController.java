package controllers;

import javafx.animation.FadeTransition;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import models.Attendance;
import models.Employee;
import models.Payroll;
import services.AttendanceService;
import services.EmployeeService;
import services.PayrollService;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class DashboardController {

    @FXML private VBox contentArea;

    // Sidebar Buttons
    @FXML private Button btnDashboard;
    @FXML private Button btnEmployees;
    @FXML private Button btnPayroll;
    @FXML private Button btnAttendance;
    @FXML private Button btnReports;

    // KPI Labels
    @FXML private Label lblTotalEmployees;
    @FXML private Label lblTotalAttendance;
    @FXML private Label lblTotalPayroll;

    // Activity Overview labels (real content)
    @FXML private Label lblActivityLine1;
    @FXML private Label lblActivityLine2;
    @FXML private Label lblActivityLine3;
    @FXML private Label lblActivityLine4;
    @FXML private Label lblActivityLine5;

    // Recent Events labels (real timestamps)
    @FXML private Label lblEvent1Title;
    @FXML private Label lblEvent1Time;
    @FXML private Label lblEvent2Title;
    @FXML private Label lblEvent2Time;
    @FXML private Label lblEvent3Title;
    @FXML private Label lblEvent3Time;

    private boolean isDark = false;
    private Parent  defaultDashboardView;

    @FXML
    public void initialize() {
        refreshDashboard();

        // Save the initial dashboard content to restore on "Dashboard" click
        if (contentArea != null && !contentArea.getChildren().isEmpty()) {
            VBox defaultView = new VBox();
            defaultView.getStyleClass().add("content");
            defaultView.setSpacing(contentArea.getSpacing());
            defaultView.setPadding(contentArea.getPadding());
            // This MOVES the children from contentArea into defaultView
            defaultView.getChildren().addAll(contentArea.getChildren());
            defaultDashboardView = defaultView;
            
            // Put the newly wrapped view back into the contentArea so it shows on startup!
            contentArea.getChildren().setAll(defaultDashboardView);
        }
        
        // Highlight the dashboard button as active
        if (btnDashboard != null) {
            btnDashboard.getStyleClass().add("active");
        }
    }

    /** Refreshes KPIs, activity overview, and recent events with real data */
    private void refreshDashboard() {
        try {
            EmployeeService  empService  = new EmployeeService();
            AttendanceService attService = new AttendanceService();
            PayrollService   payService  = new PayrollService();

            List<Employee>   employees   = empService.getAllEmployees();
            List<Attendance> attendance  = attService.getAllRecords();
            List<Payroll>    payroll     = payService.getAllRecords();

            int empCount = employees.size();
            int attCount = attendance.size();
            int payCount = payroll.size();

            // --- KPI Cards ---
            setText(lblTotalEmployees, String.valueOf(empCount));
            setText(lblTotalAttendance, String.valueOf(attCount));
            setText(lblTotalPayroll, String.valueOf(payCount));

            // --- Activity Overview (real stats) ---
            String today = LocalDate.now().format(DateTimeFormatter.ofPattern("dd MMM yyyy"));
            long presentToday = attendance.stream()
                .filter(a -> a.getDate().equals(LocalDate.now().toString()) && "Present".equals(a.getStatus()))
                .count();
            long absentToday = attendance.stream()
                .filter(a -> a.getDate().equals(LocalDate.now().toString()) && "Absent".equals(a.getStatus()))
                .count();
            double totalPayrollNet = payroll.stream().mapToDouble(Payroll::getNetSalary).sum();

            setText(lblActivityLine1, "📅  Date: " + today);
            setText(lblActivityLine2, "👥  Total Employees: " + empCount);
            setText(lblActivityLine3, "✅  Present Today: " + presentToday);
            setText(lblActivityLine4, "❌  Absent Today: " + absentToday);
            setText(lblActivityLine5, String.format("💰  Total Net Payroll: $%,.2f", totalPayrollNet));

            // --- Recent Events (real timestamps) ---
            String nowTime = LocalTime.now().format(DateTimeFormatter.ofPattern("hh:mm a"));
            String nowDate = LocalDate.now().format(DateTimeFormatter.ofPattern("dd MMM yyyy"));

            if (payCount > 0) {
                Payroll last = payroll.get(payCount - 1);
                setText(lblEvent1Title, "Payroll: " + last.getEmployeeId() + " — $" + String.format("%,.2f", last.getNetSalary()));
                setText(lblEvent1Time, nowDate + " at " + nowTime);
            } else {
                setText(lblEvent1Title, "No payroll records yet");
                setText(lblEvent1Time, "—");
            }

            if (empCount > 0) {
                Employee lastEmp = employees.get(empCount - 1);
                setText(lblEvent2Title, "Employee Added: " + lastEmp.getName());
                setText(lblEvent2Time, "Last registered • " + lastEmp.getId());
            } else {
                setText(lblEvent2Title, "No employees added yet");
                setText(lblEvent2Time, "—");
            }

            setText(lblEvent3Title, "Attendance Logs: " + attCount + " total records");
            setText(lblEvent3Time, "Updated: " + nowTime);

        } catch (Exception e) {
            System.err.println("Dashboard refresh error: " + e.getMessage());
        }
    }

    private void setText(Label lbl, String value) {
        if (lbl != null) lbl.setText(value);
    }

    @FXML
    private void handleShowReports() {
        loadPage("/fxml/reports.fxml", btnReports);
    }

    // =========================
    // NAVIGATION
    // =========================
    private void loadPage(String fxmlPath, Button activeButton) {
        try {
            Parent view;
            if (fxmlPath == null && defaultDashboardView != null) {
                view = defaultDashboardView;
                refreshDashboard();
            } else {
                URL resource = getClass().getResource(fxmlPath);
                if (resource == null) { System.err.println("Cannot find FXML: " + fxmlPath); return; }
                view = FXMLLoader.load(resource);
            }

            setActiveButton(activeButton);
            view.setOpacity(0);
            contentArea.getChildren().setAll(view);

            FadeTransition fade = new FadeTransition(Duration.millis(250), view);
            fade.setFromValue(0);
            fade.setToValue(1);
            fade.play();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void setActiveButton(Button activeBtn) {
        Button[] allButtons = { btnDashboard, btnEmployees, btnPayroll, btnAttendance, btnReports };
        for (Button b : allButtons) {
            if (b != null) {
                b.getStyleClass().remove("active");
            }
        }
        if (activeBtn != null) {
            activeBtn.getStyleClass().add("active");
        }
    }

    @FXML private void handleShowDashboard()  { loadPage(null, btnDashboard); }
    @FXML private void handleShowEmployees()  { loadPage("/fxml/employees.fxml",  btnEmployees);  }
    @FXML private void handleShowPayroll()    { loadPage("/fxml/payroll.fxml",    btnPayroll);    }
    @FXML private void handleShowAttendance() { loadPage("/fxml/attendance.fxml", btnAttendance); }

    // =========================
    // THEME TOGGLE
    // =========================
    @FXML
    private void toggleTheme() {
        Scene scene = contentArea.getScene();
        String light = getClass().getResource("/styles/main.css").toExternalForm();
        String dark  = getClass().getResource("/styles/dark.css").toExternalForm();

        if (!scene.getStylesheets().contains(light)) scene.getStylesheets().add(light);

        if (isDark) {
            scene.getStylesheets().remove(dark);
        } else {
            if (!scene.getStylesheets().contains(dark)) scene.getStylesheets().add(dark);
        }
        isDark = !isDark;
    }
}