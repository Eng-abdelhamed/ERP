package controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import models.Employee;
import services.EmployeeService;

public class EmployeeController {

    // Edit form fields
    @FXML private TextField txtName;
    @FXML private TextField txtPosition;
    @FXML private TextField txtSalary;

    // Search field
    @FXML private TextField txtSearch;

    // ID label shown when editing
    @FXML private Label lblCurrentId;
    @FXML private Label lblStatus;

    @FXML private TableView<Employee> employeeTable;
    @FXML private TableColumn<Employee, String> colId;
    @FXML private TableColumn<Employee, String> colName;
    @FXML private TableColumn<Employee, String> colPosition;
    @FXML private TableColumn<Employee, Double> colSalary;

    private EmployeeService employeeService;
    private ObservableList<Employee> employeeData;
    private FilteredList<Employee> filteredData;
    private Employee selectedEmployee = null;

    @FXML
    public void initialize() {
        employeeService = new EmployeeService();
        employeeData = FXCollections.observableArrayList(employeeService.getAllEmployees());
        filteredData = new FilteredList<>(employeeData, p -> true);

        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colPosition.setCellValueFactory(new PropertyValueFactory<>("position"));
        colSalary.setCellValueFactory(new PropertyValueFactory<>("salary"));
        
        // Format the salary column with commas
        colSalary.setCellFactory(col -> new TableCell<Employee, Double>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(String.format("$%,.2f", item));
                }
            }
        });

        employeeTable.setItems(filteredData);

        // ✅ SEARCH: live filter by name OR id
        txtSearch.textProperty().addListener((obs, oldVal, newVal) -> {
            String filter = newVal == null ? "" : newVal.toLowerCase().trim();
            filteredData.setPredicate(emp -> {
                if (filter.isEmpty()) return true;
                return emp.getName().toLowerCase().contains(filter)
                    || emp.getId().toLowerCase().contains(filter);
            });
        });

        // Row click → fill edit form
        employeeTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, newSel) -> {
            if (newSel != null) {
                selectedEmployee = newSel;
                txtName.setText(newSel.getName());
                txtPosition.setText(newSel.getPosition());
                txtSalary.setText(String.valueOf(newSel.getSalary()));
                lblCurrentId.setText("Editing: " + newSel.getId());
            }
        });
    }

    /** ✅ Opens a themed modal dialog to add a new employee */
    @FXML
    private void handleOpenAddDialog() {
        String newId = employeeService.generateNextId();

        Stage dialog = new Stage();
        dialog.setTitle("Add New Employee");
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setResizable(false);

        // --- Form fields ---
        TextField nameField = new TextField();
        nameField.setPromptText("e.g. Jane Doe");

        TextField posField = new TextField();
        posField.setPromptText("e.g. Software Engineer");

        TextField salField = new TextField();
        salField.setPromptText("e.g. 75000.00");

        Label errorLbl = new Label("");
        errorLbl.setStyle("-fx-text-fill: red; -fx-font-size: 12px;");

        Button saveBtn = new Button("Add Employee");
        Button cancelBtn = new Button("Cancel");

        // --- Layout ---
        GridPane grid = new GridPane();
        grid.setHgap(12);
        grid.setVgap(14);

        grid.add(new Label("Auto ID:"),   0, 0); grid.add(new Label(newId), 1, 0);
        grid.add(new Label("Full Name:"), 0, 1); grid.add(nameField,        1, 1);
        grid.add(new Label("Position:"),  0, 2); grid.add(posField,         1, 2);
        grid.add(new Label("Salary ($):"),0, 3); grid.add(salField,         1, 3);

        // Make the input column wider
        ColumnConstraints col1 = new ColumnConstraints(110);
        ColumnConstraints col2 = new ColumnConstraints(240);
        grid.getColumnConstraints().addAll(col1, col2);

        HBox btnRow = new HBox(10, saveBtn, cancelBtn);
        btnRow.setAlignment(Pos.CENTER_RIGHT);

        VBox root = new VBox(16, grid, errorLbl, btnRow);
        root.setPadding(new Insets(28));

        saveBtn.setDefaultButton(true);
        saveBtn.setOnAction(e -> {
            String name     = nameField.getText().trim();
            String position = posField.getText().trim();
            String salText  = salField.getText().trim();

            if (name.isEmpty() || position.isEmpty() || salText.isEmpty()) {
                errorLbl.setText("All fields are required.");
                return;
            }
            try {
                double salary = Double.parseDouble(salText);
                Employee newEmp = new Employee(newId, name, position, salary);
                employeeService.addEmployee(newEmp);
                employeeData.add(newEmp);
                lblStatus.setText("Employee " + newId + " added!");
                lblStatus.setStyle("-fx-text-fill: green;");
                dialog.close();
            } catch (NumberFormatException ex) {
                errorLbl.setText("Invalid salary. Use a number like 75000.00");
            }
        });

        cancelBtn.setOnAction(e -> dialog.close());

        Scene dialogScene = new Scene(root);
        // Inherit theme stylesheets from the main window
        if (employeeTable.getScene() != null) {
            dialogScene.getStylesheets().addAll(employeeTable.getScene().getStylesheets());
        }
        dialog.setScene(dialogScene);
        dialog.showAndWait();
    }

    @FXML
    private void handleUpdateEmployee() {
        if (selectedEmployee == null) {
            lblStatus.setText("Select an employee from the table first.");
            lblStatus.setStyle("-fx-text-fill: red;");
            return;
        }
        try {
            String name     = txtName.getText().trim();
            String position = txtPosition.getText().trim();
            double salary   = Double.parseDouble(txtSalary.getText().trim());

            Employee updated = new Employee(selectedEmployee.getId(), name, position, salary);
            employeeService.updateEmployee(updated);
            employeeData.setAll(employeeService.getAllEmployees());

            lblStatus.setText("Employee updated!");
            lblStatus.setStyle("-fx-text-fill: green;");
            handleClearForm();
        } catch (NumberFormatException e) {
            lblStatus.setText("Invalid salary value.");
            lblStatus.setStyle("-fx-text-fill: red;");
        }
    }

    @FXML
    private void handleDeleteEmployee() {
        Employee selected = employeeTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            employeeService.deleteEmployee(selected.getId());
            employeeData.remove(selected);
            lblStatus.setText("Employee " + selected.getId() + " deleted.");
            lblStatus.setStyle("-fx-text-fill: green;");
            handleClearForm();
        } else {
            lblStatus.setText("Select an employee to delete.");
            lblStatus.setStyle("-fx-text-fill: red;");
        }
    }

    @FXML
    private void handleClearForm() {
        txtName.clear();
        txtPosition.clear();
        txtSalary.clear();
        lblCurrentId.setText("");
        selectedEmployee = null;
        employeeTable.getSelectionModel().clearSelection();
    }
}