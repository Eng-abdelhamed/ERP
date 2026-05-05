package models;
public class AttendanceStat {
    private String employeeId;
    private String employeeName;
    private int    totalDays;
    private int    presentDays;
    private String percentage;  // formatted string like "87.5%"

    public AttendanceStat(String employeeId, String employeeName,
                          int totalDays, int presentDays) {
        this.employeeId   = employeeId;
        this.employeeName = employeeName;
        this.totalDays    = totalDays;
        this.presentDays  = presentDays;

        if (totalDays > 0) {
            double pct = (presentDays * 100.0) / totalDays;
            this.percentage = String.format("%.1f%%", pct);
        } else {
            this.percentage = "N/A";
        }
    }
    public String getEmployeeId()   { return employeeId;   }
    public String getEmployeeName() { return employeeName; }
    public int    getTotalDays()    { return totalDays;    }
    public int    getPresentDays()  { return presentDays;  }
    public String getPercentage()   { return percentage;   }
}
