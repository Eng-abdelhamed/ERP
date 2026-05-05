package models;
public class Payroll {
    private String employeeId;
    private double baseSalary;
    private double bonus;
    private double deductions;
    private double netSalary;

    public Payroll(String employeeId, double baseSalary, double bonus, double deductions, double netSalary) {
        this.employeeId = employeeId;
        this.baseSalary = baseSalary;
        this.bonus = bonus;
        this.deductions = deductions;
        this.netSalary = netSalary;
    }

    // Getters
    public String getEmployeeId() { return employeeId; }
    public double getBaseSalary() { return baseSalary; }
    public double getBonus() { return bonus; }
    public double getDeductions() { return deductions; }
    public double getNetSalary() { return netSalary; }

    public String toCsv() {
        return employeeId + "," + baseSalary + "," + bonus + "," + deductions + "," + netSalary;
    }

    public static Payroll fromCsv(String csvLine) {
        String[] parts = csvLine.split(",");
        if (parts.length == 5) {
            return new Payroll(parts[0], 
                    Double.parseDouble(parts[1]), 
                    Double.parseDouble(parts[2]), 
                    Double.parseDouble(parts[3]), 
                    Double.parseDouble(parts[4]));
        }
        return null;
    }
}
