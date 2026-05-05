package models;

public class Employee {
    private String id;
    private String name;
    private String position;
    private double salary;

    public Employee(String id, String name, String position, double salary) {
        this.id = id;
        this.name = name;
        this.position = position;
        this.salary = salary;
    }

    // Getters
    public String getId() { return id; }
    public String getName() { return name; }
    public String getPosition() { return position; }
    public double getSalary() { return salary; }

    // Setters
    public void setId(String id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setPosition(String position) { this.position = position; }
    public void setSalary(double salary) { this.salary = salary; }

    /**
     * Formats the employee data for saving to a CSV/TXT file.
     */
    public String toCsv() {
        return id + "," + name + "," + position + "," + salary;
    }

    /**
     * Creates an Employee object from a CSV string.
     */
    public static Employee fromCsv(String csvLine) {
        String[] parts = csvLine.split(",");
        if (parts.length == 4) {
            return new Employee(parts[0], parts[1], parts[2], Double.parseDouble(parts[3]));
        }
        return null;
    }
}
