package controllers;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import models.Employee;
import models.Payroll;
import services.EmployeeService;
import services.PayrollService;

import java.util.List;

public class PayrollController {

    @FXML private TextField txtPayrollEmployeeId;
    @FXML private TextField txtEmpName;      
    @FXML private TextField txtBaseSalary;
    @FXML private TextField txtOvertime;
    @FXML private TextField txtBonus;
    @FXML private TextField txtDeductions;
    @FXML private TextField txtTaxRate;
    @FXML private ComboBox<String> cmbPayPeriod;

    @FXML private Label lblNetSalaryResult;
    @FXML private Label lblPayrollStatus;

    // Breakdown labels
    @FXML private Label lblBreakBase;
    @FXML private Label lblBreakOvertime;
    @FXML private Label lblBreakBonus;
    @FXML private Label lblBreakGross;
    @FXML private Label lblBreakTax;
    @FXML private Label lblBreakDeductions;

    private EmployeeService employeeService;
    private PayrollService  payrollService;

    @FXML
    public void initialize() {
        employeeService = new EmployeeService();
        payrollService  = new PayrollService();
        int currentYear = java.time.LocalDate.now().getYear();
        cmbPayPeriod.setItems(FXCollections.observableArrayList(
            "January " + currentYear,  "February " + currentYear, "March " + currentYear,
            "April " + currentYear,    "May " + currentYear,       "June " + currentYear,
            "July " + currentYear,     "August " + currentYear,    "September " + currentYear,
            "October " + currentYear,  "November " + currentYear,  "December " + currentYear
        ));
    }
    @FXML
    private void handleLookupEmployee() {
        String id = txtPayrollEmployeeId.getText().trim();
        if (id.isEmpty()) {
            showError("Enter an Employee ID first.");
            return;
        }

        List<Employee> employees = employeeService.getAllEmployees();
        Employee found = null;
        for (Employee emp : employees) {
            if (emp.getId().equalsIgnoreCase(id)) {
                found = emp;
                break;
            }
        }

        if (found != null) {
            txtEmpName.setText(found.getName());
            txtBaseSalary.setText(String.format("%.2f", found.getSalary()));
            lblPayrollStatus.setText("Employee found: " + found.getName());
            lblPayrollStatus.setStyle("-fx-text-fill: -color-success;");
        } else {
            txtEmpName.setText("");
            showError("No employee found with ID: " + id);
        }
    }

    @FXML
    private void handleCalculateNetSalary() {
        try {
            String empId = txtPayrollEmployeeId.getText().trim();
            if (empId.isEmpty()) throw new IllegalArgumentException("Employee ID is required.");

            String period = cmbPayPeriod.getValue();
            if (period == null || period.isEmpty()) throw new IllegalArgumentException("Select a pay period.");

            double base       = parseOrZero(txtBaseSalary.getText());
            double overtime   = parseOrZero(txtOvertime.getText());
            double bonus      = parseOrZero(txtBonus.getText());
            double deductions = parseOrZero(txtDeductions.getText());
            double taxRate    = parseOrZero(txtTaxRate.getText()); 

         
            double gross       = base + overtime + bonus;
            double taxAmount   = gross * (taxRate / 100.0);
            double net         = gross - taxAmount - deductions;

         
            lblNetSalaryResult.setText(String.format("$%,.2f", net));

      
            lblBreakBase.setText(String.format("$%,.2f", base));
            lblBreakOvertime.setText(String.format("$%,.2f", overtime));
            lblBreakBonus.setText(String.format("$%,.2f", bonus));
            lblBreakGross.setText(String.format("$%,.2f", gross));
            lblBreakTax.setText(String.format("-$%,.2f", taxAmount));
            lblBreakDeductions.setText(String.format("-$%,.2f", deductions));

       
            Payroll record = new Payroll(empId, base, bonus, deductions, net);
            payrollService.addPayroll(record);

            lblPayrollStatus.setText("Payroll for " + period + " saved!");
            lblPayrollStatus.setStyle("-fx-text-fill: -color-success;");

        } catch (IllegalArgumentException e) {
            showError(e.getMessage());
        }
    }

    @FXML
    private void handleResetPayroll() {
        txtPayrollEmployeeId.clear();
        txtEmpName.clear();
        txtBaseSalary.clear();
        txtOvertime.clear();
        txtBonus.clear();
        txtDeductions.clear();
        txtTaxRate.setText("15");
        cmbPayPeriod.getSelectionModel().clearSelection();

        lblNetSalaryResult.setText("$0.00");
        resetBreakdown();
        lblPayrollStatus.setText("");
    }

    private void showError(String msg) {
        lblPayrollStatus.setText(msg);
        lblPayrollStatus.setStyle("-fx-text-fill: -color-danger;");
    }

    private double parseOrZero(String text) {
        if (text == null || text.trim().isEmpty()) return 0.0;
        return Double.parseDouble(text.trim());
    }

    private void resetBreakdown() {
        String dash = "$0.00";
        lblBreakBase.setText(dash);
        lblBreakOvertime.setText(dash);
        lblBreakBonus.setText(dash);
        lblBreakGross.setText(dash);
        lblBreakTax.setText(dash);
        lblBreakDeductions.setText(dash);
    }
}
