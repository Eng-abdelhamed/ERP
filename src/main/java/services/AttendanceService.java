package services;

import models.Attendance;
import utils.FileUtil;

import java.util.ArrayList;
import java.util.List;

public class AttendanceService {
    private final String FILE_NAME = "attendance.txt";
    private List<Attendance> records;

    public AttendanceService() {
        records = new ArrayList<>();
        loadFromFile();
    }

    public List<Attendance> getAllRecords() {
        return records;
    }

    public void addRecord(Attendance attendance) {
        records.add(attendance);
        saveToFile();
    }

    public void updateRecord(Attendance oldRecord, Attendance newRecord) {
        for (int i = 0; i < records.size(); i++) {
            Attendance att = records.get(i);
            if (att.getEmployeeId().equals(oldRecord.getEmployeeId()) && att.getDate().equals(oldRecord.getDate())) {
                records.set(i, newRecord);
                break;
            }
        }
        saveToFile();
    }

    /**
     * Checks if an attendance record already exists for a given employee on a given date.
     * Prevents duplicate daily entries.
     */
    public boolean hasDuplicateEntry(String employeeId, String date) {
        for (Attendance att : records) {
            if (att.getEmployeeId().equals(employeeId) && att.getDate().equals(date)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Counts how many days the employee was "Present".
     */
    public int countPresent(String employeeId) {
        int count = 0;
        for (Attendance att : records) {
            if (att.getEmployeeId().equals(employeeId) && "Present".equals(att.getStatus())) {
                count++;
            }
        }
        return count;
    }

    /**
     * Counts the total number of logged days for an employee (any status).
     */
    public int countTotal(String employeeId) {
        int count = 0;
        for (Attendance att : records) {
            if (att.getEmployeeId().equals(employeeId)) {
                count++;
            }
        }
        return count;
    }

    public void clearAll() {
        records.clear();
        saveToFile();
    }

    private void loadFromFile() {
        List<String> lines = FileUtil.readLines(FILE_NAME);
        for (String line : lines) {
            Attendance att = Attendance.fromCsv(line);
            if (att != null) {
                records.add(att);
            }
        }
    }

    private void saveToFile() {
        List<String> lines = new ArrayList<>();
        for (Attendance att : records) {
            lines.add(att.toCsv());
        }
        FileUtil.writeLines(FILE_NAME, lines);
    }
}
