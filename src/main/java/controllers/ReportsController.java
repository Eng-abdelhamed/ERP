package controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.StackPane;
import javafx.stage.FileChooser;
import models.Attendance;
import models.Payroll;
import services.AttendanceService;
import services.PayrollService;
import services.ReportService;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class ReportsController {

    @FXML private ComboBox<String> cmbReportType;
    @FXML private Label lblStatus;

    @FXML private PieChart attendancePieChart;
    @FXML private BarChart<String, Number> payrollBarChart;

    @FXML private StackPane chartPaneLeft;
    @FXML private StackPane chartPaneRight;

    private AttendanceService attendanceService;
    private PayrollService payrollService;
    private ReportService reportService;

    @FXML
    public void initialize() {
        attendanceService = new AttendanceService();
        payrollService = new PayrollService();
        reportService = new ReportService();

        cmbReportType.setItems(FXCollections.observableArrayList(
            "Overall Attendance Report",
            "Overall Payroll Report"
        ));
        
        cmbReportType.setValue("Overall Attendance Report");

        cmbReportType.valueProperty().addListener((obs, oldVal, newVal) -> {
            updateCharts();
        });

        // Add a bit of spacing hack via CSS dynamically if needed, but styling is in FXML
        updateCharts();
    }

    private void updateCharts() {
        lblStatus.setText("");

        // 1. Populate Attendance Pie Chart
        List<Attendance> attendanceList = attendanceService.getAllRecords();
        long presentCount = attendanceList.stream().filter(a -> "Present".equals(a.getStatus())).count();
        long absentCount = attendanceList.stream().filter(a -> "Absent".equals(a.getStatus())).count();

        ObservableList<PieChart.Data> pieData = FXCollections.observableArrayList(
            new PieChart.Data("Present (" + presentCount + ")", presentCount),
            new PieChart.Data("Absent (" + absentCount + ")", absentCount)
        );
        attendancePieChart.setData(pieData);

        // 2. Populate Payroll Bar Chart
        List<Payroll> payrollList = payrollService.getAllRecords();
        XYChart.Series<String, Number> seriesGross = new XYChart.Series<>();
        seriesGross.setName("Gross Salary");
        XYChart.Series<String, Number> seriesNet = new XYChart.Series<>();
        seriesNet.setName("Net Salary");

        for (Payroll p : payrollList) {
            double gross = p.getBaseSalary() + p.getBonus();
            seriesGross.getData().add(new XYChart.Data<>(p.getEmployeeId(), gross));
            seriesNet.getData().add(new XYChart.Data<>(p.getEmployeeId(), p.getNetSalary()));
        }

        payrollBarChart.getData().clear();
        payrollBarChart.getData().addAll(seriesGross, seriesNet);
    }

    @FXML
    private void handleGeneratePdf() {
        String reportType = cmbReportType.getValue();
        if (reportType == null) {
            showError("Please select a report type first.");
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save PDF Report");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF Files", "*.pdf"));
        
        String defaultFileName = reportType.replace(" ", "_").toLowerCase() + ".pdf";
        fileChooser.setInitialFileName(defaultFileName);

        File file = fileChooser.showSaveDialog(attendancePieChart.getScene().getWindow());
        if (file != null) {
            try {
                WritableImage chartSnapshot;
                List<Attendance> attData = null;
                List<Payroll> payData = null;

                if (reportType.contains("Attendance")) {
                    chartSnapshot = chartPaneLeft.snapshot(null, null);
                    attData = attendanceService.getAllRecords();
                } else {
                    chartSnapshot = chartPaneRight.snapshot(null, null);
                    payData = payrollService.getAllRecords();
                }

                reportService.generatePdfReport(file, reportType, chartSnapshot, attData, payData);
                
                showSuccess("Report successfully generated: " + file.getName());
            } catch (IOException e) {
                e.printStackTrace();
                showError("Failed to generate PDF: " + e.getMessage());
            } catch (Exception e) {
                e.printStackTrace();
                showError("An unexpected error occurred while generating PDF.");
            }
        }
    }

    private void showError(String msg) {
        lblStatus.setText("⚠ " + msg);
        lblStatus.setStyle("-fx-text-fill: -color-danger; -fx-padding: 10px;");
    }

    private void showSuccess(String msg) {
        lblStatus.setText("✓ " + msg);
        lblStatus.setStyle("-fx-text-fill: -color-success; -fx-padding: 10px;");
    }
}
