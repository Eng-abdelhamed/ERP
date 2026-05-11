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

    @FXML private TextField txtName;
    @FXML private TextField txtPosition;
    @FXML private TextField txtSalary;

    @FXML private TextField txtSearch;

    @FXML private Label lblCurrentId;
    @FXML private Label lblStatus;

    @FXML private TableView<Employee> employeeTable;
    @FXML private TableColumn<Employee, String> colId;
    @FXML private TableColumn<Employee, String> colName;
    @FXML private TableColumn<Employee, String> colPosition;
    @FXML private TableColumn<Employee, Double> colSalary;
//    Service handle the logic file
    private EmployeeService employeeService;
//    main list real data
    private ObservableList<Employee> employeeData;
//    filter version of data ,, what user will sea
    private FilteredList<Employee> filteredData;
//    select employee in table
    private Employee selectedEmployee = null;

    
//    initialization run automatically when the screen loads
    
    @FXML
    public void initialize() {
//    create service
        employeeService = new EmployeeService();
//       get all the data from service and wrap them in observable list , we used observableArrayList UI updates automatically when data changes
        employeeData = FXCollections.observableArrayList(employeeService.getAllEmployees());
        
//        create filter list
        filteredData = new FilteredList<>(employeeData, p -> true);

//        connect table column with the employee field
//        important: name must match getters in employee class

// from this column colID , get the value from the id field in employee object
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colPosition.setCellValueFactory(new PropertyValueFactory<>("position"));
        colSalary.setCellValueFactory(new PropertyValueFactory<>("salary"));
        
//     format salary option add comma and dollar sign
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
// show filter data in table
        employeeTable.setItems(filteredData);

//        search logic bta3na
//  ay 7aga user write it gonna be filtered,listener detect any change in the field
        txtSearch.textProperty().addListener((obs, oldVal, newVal) -> {
//            clean input avoid null and make lowecase
            String filter = newVal == null ? "" : newVal.toLowerCase().trim();
//            bt3ml filter 
            filteredData.setPredicate(emp -> {
//                lw mktbsh 7aga show everything
                if (filter.isEmpty()) return true;
                return emp.getName().toLowerCase().contains(filter)
                    || emp.getId().toLowerCase().contains(filter);
            });
        });

        // table click for edit , lw el user 3ayz y3ml edit
//         kol table 3ndo selectionModel , control which row is selected and what happen when 
//         add listener y3ny run this code when the selected row change
//         obs is the property itself , oldsel is previsoiuly selected employee , newSel is currently selected employee
/**
 * row 1 -> ali 
 * row 2 -> sara
 * oldSel = ali
 * newSel = sara
 */
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

//    byft7 new window to add new employee in system 
    @FXML
    private void handleOpenAddDialog() {
        String newId = employeeService.generateNextId();

        Stage dialog = new Stage();
        dialog.setTitle("Add New Employee");
//        block everything l8ayt el employee window close
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setResizable(false);
        TextField nameField = new TextField();
        nameField.setPromptText("Name");

        TextField posField = new TextField();
        posField.setPromptText("Position");

        TextField salField = new TextField();
        salField.setPromptText("Salary");

        Label errorLbl = new Label("");
        errorLbl.setStyle("-fx-text-fill: red; -fx-font-size: 12px;");

        Button saveBtn = new Button("Submit");
        Button cancelBtn = new Button("Cancel");

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
// Validation , user lazm y put all the details
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
        if (employeeTable.getScene() != null) {
            dialogScene.getStylesheets().addAll(employeeTable.getScene().getStylesheets());
        }
        dialog.setScene(dialogScene);
        dialog.showAndWait();
    }

    
//    update the employee
    /**
     * selected update
     * read input 
     * create update service
     * call service
     * reload the table again to render it
     */
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
            
//            this ensure UI is Sync with The File  , setALL Replace all the data
            employeeData.setAll(employeeService.getAllEmployees());

            lblStatus.setText("Employee updated!");
            lblStatus.setStyle("-fx-text-fill: green;");
            handleClearForm();
        } catch (NumberFormatException e) {
            lblStatus.setText("Invalid salary value.");
            lblStatus.setStyle("-fx-text-fill: red;");
        }
    }
// handle the deleted employee
    /**
     * get the selected row by user
     * h call service
     * h shell the removed row 
     */
    @FXML
    private void handleDeleteEmployee() {
        Employee selected = employeeTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            employeeService.deleteEmployee(selected.getId());
//            ems7 el row
            employeeData.remove(selected);
            lblStatus.setText("Employee " + selected.getId() + " deleted.");
            lblStatus.setStyle("-fx-text-fill: green;");
            handleClearForm();
        } else {
            lblStatus.setText("Select an employee to delete.");
            lblStatus.setStyle("-fx-text-fill: red;");
        }
    }

//    clear field , remove selection , reset state 
    @FXML
    private void handleClearForm() {
        txtName.clear();
        txtPosition.clear();
        txtSalary.clear();
        lblCurrentId.setText("");
        selectedEmployee = null;
//        responsible for the selected Column
        employeeTable.getSelectionModel().clearSelection();
    }
}