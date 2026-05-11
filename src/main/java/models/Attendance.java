package models;

public class Attendance {
    private String employeeId;
    private String date; 
    private String status;

//    Constructor
    public Attendance(String employeeId, String date, String status) {
        this.employeeId = employeeId;
        this.date = date;
        this.status = status;
    }

    // Getters
    public String getEmployeeId() { return employeeId; }
    public String getDate() { return date; }
    public String getStatus() { return status; }

    // Setters
    public void setEmployeeId(String employeeId) { this.employeeId = employeeId; }
    public void setDate(String date) { this.date = date; }
    public void setStatus(String status) { this.status = status; }

    public String toCsv() {
        return employeeId + "," + date + "," + status;
    }
// abdelahmed,enginener,19999
    public static Attendance fromCsv(String csvLine) {
        String[] parts = csvLine.split(",");
        if (parts.length == 3) {
            return new Attendance(parts[0], parts[1], parts[2]);
        }
        return null;
    }
}
