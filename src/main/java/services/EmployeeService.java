package services;

import models.Employee;
import utils.FileUtil;

import java.util.ArrayList;
import java.util.List;
/**
 * store the employee in list
 * add /delete/update
 * store the employee in text file
 * reload the data from the file at the beginning
 * generate auto id for every new empl
 * @author abdelhamed ahmed
 */

public class EmployeeService {
//    asm el file ely feh employee
    private final String FILE_NAME = "employees.txt";
//    list have all the employee 
    private List<Employee> employees;

//    Constructor
/**
 * lma el class yt3ml 
 * create empty list
 * and read the employe from the file
 */
    public EmployeeService() {
        employees = new ArrayList<>();
        loadFromFile();
    }
//return all the employee to ui
    public List<Employee> getAllEmployees() {
        return employees;
    }

//    bygenerate new id , emp-01
    /**
     * search for the bigest id found
     * extract the number 
     * increment 1
     * return new id
     * @return 
     */
    public String generateNextId() {
        int maxNum = 0;
        for (Employee emp : employees) {
            String id = emp.getId();
            if (id != null && id.startsWith("EMP-")) { // EMP-001
                try {
                    int num = Integer.parseInt(id.substring(4)); // 100
                    if (num > maxNum) maxNum = num;
                } catch (NumberFormatException ignored) {}
            }
        }
        return String.format("EMP-%03d", maxNum + 1); 
    }  
/**
 * add new employee 
 * save into the file
 * @param emp 
 */
    public void addEmployee(Employee emp) {
        employees.add(emp);
        saveToFile();
    }
/**
 * search for the accorading to the id
 * replace with new data
 * save the file
 * @param updatedEmp 
 */
    public void updateEmployee(Employee updatedEmp) {
        for (int i = 0; i < employees.size(); i++) {
            if (employees.get(i).getId().equals(updatedEmp.getId())) {
                employees.set(i, updatedEmp);
                break;
            }
        }
        saveToFile();
    }
/**
 * delete employee to the id
 * @param id 
 */
    public void deleteEmployee(String id) {
        employees.removeIf(emp -> emp.getId().equals(id)); // lampda function ,arrow function
        saveToFile();
    }
/**
 * convert to csv file
 * 
 */
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
