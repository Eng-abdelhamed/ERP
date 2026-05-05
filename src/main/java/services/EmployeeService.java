package services;
import models.Employee;
import utils.FileUtil;
import java.util.ArrayList;
import java.util.List;

public class EmployeeService {
    private final String FILE_NAME = "employees.txt";
    private List<Employee> employees;

    public EmployeeService() {
        employees = new ArrayList<>();
        loadFromFile();
    }

    public List<Employee> getAllEmployees() {
        return employees;
    }

    public String generateNextId() {
        int maxNum = 0;
        for (Employee emp : employees) {
            String id = emp.getId();
            if (id != null && id.startsWith("EMP-")) {
                try {
                    int num = Integer.parseInt(id.substring(4));
                    if (num > maxNum) maxNum = num;
                } catch (NumberFormatException ignored) {}
            }
        }
        return String.format("EMP-%03d", maxNum + 1);
    }

    public void addEmployee(Employee emp) {
        employees.add(emp);
        saveToFile();
    }

    public void updateEmployee(Employee updatedEmp) {
        for (int i = 0; i < employees.size(); i++) {
            if (employees.get(i).getId().equals(updatedEmp.getId())) {
                employees.set(i, updatedEmp);
                break;
            }
        }
        saveToFile();
    }

    public void deleteEmployee(String id) {
        employees.removeIf(emp -> emp.getId().equals(id));
        saveToFile();
    }

    private void loadFromFile() {
        List<String> lines = FileUtil.readLines(FILE_NAME);
        for (String line : lines) {
            Employee emp = Employee.fromCsv(line);
            if (emp != null) {
                employees.add(emp);
            }
        }
    }

    private void saveToFile() {
        List<String> lines = new ArrayList<>();
        for (Employee emp : employees) {
            lines.add(emp.toCsv());
        }
        FileUtil.writeLines(FILE_NAME, lines);
    }
}
