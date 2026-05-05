package services;
import models.Payroll;
import utils.FileUtil;
import java.util.ArrayList;
import java.util.List;

public class PayrollService {
    private final String FILE_NAME = "payroll.txt";
    private List<Payroll> payrollRecords;

    public PayrollService() {
        payrollRecords = new ArrayList<>();
        loadFromFile();
    }

    public List<Payroll> getAllRecords() {
        return payrollRecords;
    }

    public void addPayroll(Payroll payroll) {
        payrollRecords.add(payroll);
        saveToFile();
    }

    private void loadFromFile() {
        List<String> lines = FileUtil.readLines(FILE_NAME);
        for (String line : lines) {
            Payroll pay = Payroll.fromCsv(line);
            if (pay != null) {
                payrollRecords.add(pay);
            }
        }
    }

    private void saveToFile() {
        List<String> lines = new ArrayList<>();
        for (Payroll pay : payrollRecords) {
            lines.add(pay.toCsv());
        }
        FileUtil.writeLines(FILE_NAME, lines);
    }
}
